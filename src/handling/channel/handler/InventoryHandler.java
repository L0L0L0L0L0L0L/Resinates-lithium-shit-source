/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package handling.channel.handler;

import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.awt.Point;

import client.inventory.Equip;
import client.inventory.Equip.ScrollResult;
import client.inventory.Item;
import client.Skill;
import client.inventory.ItemFlag;
import client.inventory.MaplePet;
import client.inventory.MaplePet.PetFlag;
import client.inventory.MapleMount;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleDisease;
import client.MapleQuestStatus;
import client.inventory.MapleInventoryType;
import client.inventory.MapleInventory;
import client.MapleStat;
import client.MapleTrait.MapleTraitType;
import client.MonsterFamiliar;
import client.PlayerStats;
import client.SkillEntry;
import constants.GameConstants;
import client.SkillFactory;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.MaplePartyCharacter;
import handling.world.World;
import java.awt.Rectangle;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;
import server.Randomizer;
import server.MapleShopFactory;
import server.MapleStatEffect;
import server.MapleItemInformationProvider;
import server.MapleInventoryManipulator;
import server.StructRewardItem;
import server.quest.MapleQuest;
import server.maps.SavedLocationType;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.life.MapleMonster;
import server.life.MapleLifeFactory;
import scripting.NPCScriptManager;
import server.StructFamiliar;
import server.StructItemOption;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import tools.FileoutputUtil;
import tools.Pair;
import tools.packet.MTSCSPacket;
import tools.packet.PetPacket;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CField.EffectPacket;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.InfoPacket;
import tools.packet.CWvsContext.InventoryPacket;
import tools.packet.MobPacket;

public class InventoryHandler {

    public static final void ItemMove(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null || c.getPlayer() == null || GameConstants.getLock() || c.getPlayer().hasBlockedInventory() || c.getPlayer().getConversation() > 0 || c.getPlayer().run) {
            return;
        }
        if (c.getPlayer().getTrade() != null) {
            c.getPlayer().dropMessage(6, "You are currently in a trade with someone.");
            return;
        }
        if (c.getPlayer().isStorageOpened()) {
            c.getPlayer().dropMessage(6, "You are currently in storage system right now.");
            return;
        }
        long cooldown = 100;
        long curr = System.currentTimeMillis();
        if (curr - c.getPlayer().getCoolDown() >= cooldown) {
            c.getPlayer().setScrolledPosition((short) 0);
            c.getPlayer().updateTick(slea.readInt());
            final MapleInventoryType type = MapleInventoryType.getByType(slea.readByte()); //04
            MapleInventory inv = c.getPlayer().getInventory(type);
            inv.lockInventory();
            try {
                final short src = slea.readShort();                                            //01 00
                final short dst = slea.readShort();                                            //00 00
                final short quantity = slea.readShort();   //53 01
                if (src < 0 && dst > 0) {
                    MapleInventoryManipulator.unequip(c, src, dst);
                    c.getPlayer().setBattlePoints(c.getPlayer().getStat().getStarForce());
                    c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
                } else if (dst < 0) {
                    MapleInventoryManipulator.equip(c, src, dst);
                    c.getPlayer().setBattlePoints(c.getPlayer().getStat().getStarForce());
                    c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
                } else if (dst == 0) {
                    Item item = c.getPlayer().getInventory(type).getItem(src);
                    if (item != null) {
                        if (c.getPlayer().haveItem(type, (byte) src, quantity)) {
                            if (GameConstants.isPet(item.getItemId())) {
                                c.getPlayer().getInventory(type).removeSlot(item.getPosition());
                                c.announce(InventoryPacket.dropInventoryItem((src < 0 ? MapleInventoryType.EQUIP : type), src));
                                c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer().getId(), item, c.getPlayer().getPosition());
                            } else {
                                if (type == MapleInventoryType.USE || type == MapleInventoryType.ETC) {
                                    if (quantity >= item.getQuantity()) {
                                        c.getPlayer().getInventory(type).removeSlot(item.getPosition());
                                        c.announce(InventoryPacket.dropInventoryItem((src < 0 ? MapleInventoryType.EQUIP : type), src));
                                    } else {
                                        item.setQuantity((short) (item.getQuantity() - quantity));
                                        c.announce(InventoryPacket.dropInventoryItemUpdate(type, item));
                                    }
                                    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer().getId(), item, c.getPlayer().getPosition());
                                } else {
                                    if (isAllowed(c, item)) {
                                        MapleInventoryManipulator.drop(c, type, src, quantity);
                                    } else {
                                        if (quantity >= item.getQuantity()) {
                                            c.getPlayer().getInventory(type).removeSlot(item.getPosition());
                                            c.announce(InventoryPacket.dropInventoryItem((src < 0 ? MapleInventoryType.EQUIP : type), src));
                                        } else {
                                            item.setQuantity((short) (item.getQuantity() - quantity));
                                            c.announce(InventoryPacket.dropInventoryItemUpdate(type, item));
                                        }
                                        c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer().getId(), item, c.getPlayer().getPosition());
                                    }
                                    //c.getPlayer().dropMessage(1, "This item cannot be dropped.");
                                }
                            }
                        } else {
                            if (item != null) {
                                c.getPlayer().getInventory(type).removeSlot(item.getPosition());
                                c.announce(InventoryPacket.dropInventoryItem((src < 0 ? MapleInventoryType.EQUIP : type), src));
                            }
                            c.getPlayer().dropErrorMessage("Error with dropping items pls report.");
                        }
                    }
                } else {
                    MapleInventoryManipulator.move(c, type, src, dst);
                }
                c.getPlayer().setCoolDown(System.currentTimeMillis());
            } finally {
                inv.unlockInventory();
            }
        } else {
            c.getPlayer().dropMessage(6, "Time until next available Item Move-Equip-Drop " + ((long) (cooldown - (curr - c.getPlayer().getCoolDown()))) + " mili-seconds.");
        }
    }

    public static boolean isCanDrop(final MapleClient c, Item item) {
        return GameConstants.isItemBuff(item.getItemId());
    }

    public static boolean isAllowed(final MapleClient c, Item item) {
        MapleInventoryType type = item.getInventoryType();
        switch (item.getItemId()) {//allowed
            case 4032246, 4000138, 4001017, 4031179 -> {
                return true;
            }
        }
        switch (item.getItemId()) {//blocked
            case 5052000 -> {
                return false;
            }
        }
        if (type == MapleInventoryType.EQUIP || type == MapleInventoryType.ETC || type == MapleInventoryType.USE || type == MapleInventoryType.CASH) {
            return false;
        }
        return true;
    }

    public static final void SwitchBag(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c.getPlayer().hasBlockedInventory()) { //hack
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        c.getPlayer().updateTick(slea.readInt());
        final short src = (short) slea.readInt();                                       //01 00
        final short dst = (short) slea.readInt();                                            //00 00
        if (src < 100 || dst < 100) {
            return;
        }
        MapleInventoryManipulator.move(c, MapleInventoryType.ETC, src, dst);
    }

    public static final void MoveBag(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c.getPlayer().hasBlockedInventory()) { //hack
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        c.getPlayer().updateTick(slea.readInt());
        final boolean srcFirst = slea.readInt() > 0;
        short dst = (short) slea.readInt();                                       //01 00
        if (slea.readByte() != 4) { //must be etc) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        short src = slea.readShort();                                            //00 00
        MapleInventoryManipulator.move(c, MapleInventoryType.ETC, srcFirst ? dst : src, srcFirst ? src : dst);
    }

    public static final void ItemSort(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        long cooldown = 1000 * 10;
        long curr = System.currentTimeMillis();
        if (curr - c.getPlayer().getCoolDown() >= cooldown) {
            c.getPlayer().updateTick(slea.readInt());
            c.getPlayer().setScrolledPosition((short) 0);
            final MapleInventoryType pInvType = MapleInventoryType.getByType(slea.readByte());
            if (pInvType == MapleInventoryType.UNDEFINED || c.getPlayer().hasBlockedInventory()) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            final MapleInventory pInv = c.getPlayer().getInventory(pInvType); //Mode should correspond with MapleInventoryType
            boolean sorted = false;

            while (!sorted) {
                final byte freeSlot = (byte) pInv.getNextFreeSlot();
                if (freeSlot != -1) {
                    byte itemSlot = -1;
                    for (byte i = (byte) (freeSlot + 1); i <= pInv.getSlotLimit(); i++) {
                        if (pInv.getItem(i) != null) {
                            itemSlot = i;
                            break;
                        }
                    }
                    if (itemSlot > 0) {
                        MapleInventoryManipulator.move(c, pInvType, itemSlot, freeSlot);
                    } else {
                        sorted = true;
                    }
                } else {
                    sorted = true;
                }
            }
            c.announce(CWvsContext.finishedSort(pInvType.getType()));
            c.getPlayer().setCoolDown(System.currentTimeMillis());
        } else {
            c.getPlayer().dropMessage(6, "Time until next available @save " + ((long) (cooldown - (curr - c.getPlayer().getCoolDown())) / 1000) + " seconds.");
        }

        c.announce(CWvsContext.enableActions());
    }

    public static final void ItemGather(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        /*
         // [41 00] [E5 1D 55 00] [01]
         // [32 00] [01] [01] // Sent after

         c.getPlayer().updateTick(slea.readInt());
         c.getPlayer().setScrolledPosition((short) 0);
         if (c.getPlayer().hasBlockedInventory()) {
         c.announce(CWvsContext.enableActions());
         return;
         }
         final byte mode = slea.readByte();
         final MapleInventoryType invType = MapleInventoryType.getByType(mode);
         MapleInventory Inv = c.getPlayer().getInventory(invType);

         final List<Item> itemMap = new LinkedList<Item>();
         for (Item item : Inv.list()) {
         itemMap.add(item.copy()); // clone all  items T___T.
         }
         for (Item itemStats : itemMap) {
         MapleInventoryManipulator.removeFromSlot(c, invType, itemStats.getPosition(), itemStats.getQuantity(), true, false);
         }

         final List<Item> sortedItems = sortItems(itemMap);
         for (Item item : sortedItems) {
         MapleInventoryManipulator.addFromDrop(c, item, false);
         }
         c.announce(CWvsContext.finishedGather(mode));
         c.announce(CWvsContext.enableActions());
         itemMap.clear();
         sortedItems.clear();
         /*
        
         */
        c.announce(CWvsContext.enableActions());
    }

    private static List<Item> sortItems(final List<Item> passedMap) {
        final List<Integer> itemIds = new ArrayList<Integer>(); // empty list.
        for (Item item : passedMap) {
            itemIds.add(item.getItemId()); // adds all item ids to the empty list to be sorted.
        }
        Collections.sort(itemIds); // sorts item ids

        final List<Item> sortedList = new LinkedList<Item>(); // ordered list pl0x <3.

        for (Integer val : itemIds) {
            for (Item item : passedMap) {
                if (val == item.getItemId()) { // Goes through every index and finds the first value that matches
                    sortedList.add(item);
                    passedMap.remove(item);
                    break;
                }
            }
        }
        return sortedList;
    }

    public static final boolean UseRewardItem(final byte slot, final int itemId, final MapleClient c, final MapleCharacter chr) {
        final Item toUse = c.getPlayer().getInventory(GameConstants.getInventoryType(itemId)).getItem(slot);
        c.announce(CWvsContext.enableActions());
        if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId && !chr.hasBlockedInventory()) {
            if (chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() > -1 && chr.getInventory(MapleInventoryType.USE).getNextFreeSlot() > -1 && chr.getInventory(MapleInventoryType.SETUP).getNextFreeSlot() > -1 && chr.getInventory(MapleInventoryType.ETC).getNextFreeSlot() > -1) {
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final Pair<Integer, List<StructRewardItem>> rewards = ii.getRewardItem(itemId);

                if (rewards != null && rewards.getLeft() > 0) {
                    while (true) {
                        for (StructRewardItem reward : rewards.getRight()) {
                            if (reward.prob > 0 && Randomizer.nextInt(rewards.getLeft()) < reward.prob) { // Total prob
                                if (GameConstants.getInventoryType(reward.itemid) == MapleInventoryType.EQUIP) {
                                    final Item item = ii.getEquipById(reward.itemid);
                                    if (reward.period > 0) {
                                        item.setExpiration(System.currentTimeMillis() + (reward.period * 60 * 60 * 10));
                                    }
                                    item.setGMLog("Reward item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                                    MapleInventoryManipulator.addbyItem(c, item);
                                } else {
                                    MapleInventoryManipulator.addById(c, reward.itemid, reward.quantity, "Reward item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                                }
                                MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemId), itemId, 1, false, false);

                                c.announce(EffectPacket.showRewardItemAnimation(reward.itemid, reward.effect));
                                chr.getMap().broadcastMessage(chr, EffectPacket.showRewardItemAnimation(reward.itemid, reward.effect, chr.getId()), false);
                                return true;
                            }
                        }
                    }
                } else {
                    chr.dropMessage(6, "Unknown error.");
                }
            } else {
                chr.dropMessage(6, "Insufficient inventory slot.");
            }
        }
        return false;
    }

    public static final void UseItem(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null) {
            return;
        }
        MapleCharacter chr = c.getPlayer();

        if (chr == null || !chr.isAlive() || chr.getMapId() == 749040100 || chr.getMap() == null || chr.hasDisease(MapleDisease.POTION) || chr.hasBlockedInventory() || chr.inPVP()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (chr.isStatLock()) {
            c.announce(CWvsContext.enableActions());
            chr.dropMessage(5, "You may not use this item yet.");
            return;
        }
        chr.setStatLock(System.currentTimeMillis() + 100);
        final long time = System.currentTimeMillis();
        if (chr.getNextConsume() > time) {
            chr.dropMessage(5, "You may not use this item yet.");
            c.announce(CWvsContext.enableActions());
            return;
        }
        c.getPlayer().updateTick(slea.readInt());
        final byte slot = (byte) slea.readShort();
        final int itemId = slea.readInt();
        final Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) { //cwk quick hack
            if (itemId >= 2005000 && itemId <= 2005010) {
                Point pos = chr.getMap().calcPointBelow(chr.getPosition());
                if (pos != null) {
                    if (chr.getMap().getTotem(chr) != null) {
                        if (chr.getMap().getTotem(chr).getOwner() == chr) {
                            chr.getMap().removeTotem(chr);
                        }
                    }
                    chr.getMap().spawnTotem(chr, itemId, chr.getPosition());
                } else {
                    chr.dropMessage(6, "Invalid position to spawn totem");
                }
                c.announce(CWvsContext.enableActions());
                return;
            }

            if (itemId == 2000012) {
                if (chr.getStamina() < chr.getMaxStamFromChar()) {
                    chr.gainStamina(100, false);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                } else {
                    chr.dropMessage("Your stamina is currently full.");
                    c.announce(CWvsContext.enableActions());
                }
                return;
            }
            if (itemId == 2000013) {
                if (chr.getStamina() < chr.getMaxStamFromChar()) {
                    chr.recoverStamina();
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                } else {
                    chr.dropMessage("Your stamina is currently full.");
                    c.announce(CWvsContext.enableActions());
                }
                return;
            }

            if (itemId >= 2473000 && itemId < 2474000) { //cubes
                c.getPlayer().dropMessage(5, "Take this item to Fredrick to cube items");
                c.announce(CWvsContext.enableActions());
                return;
            }
            if (itemId == 2450024 || itemId == 2450025 || itemId == 2450050 || itemId == 2450051 || itemId == 2450052 || itemId == 2450053) {
                final long currenttime = System.currentTimeMillis();
                if (toUse.getExpiration() > 0 && currenttime >= toUse.getExpiration()) {
                    c.getPlayer().dropMessage("Buff coupon has expired.");
                    c.announce(CWvsContext.enableActions());
                    return;
                }
            }
            if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()) != null) {
                if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
                    if (itemId == 2005107) { //God Apple
                        c.getPlayer().superbuff();
                        c.announce(CWvsContext.enableActions());
                        return;
                    }
                    if (itemId == 2005106) { //Apple
                        c.getPlayer().buff();
                    }
                }
            }
            if (itemId == 2450024 || itemId == 2450025 || itemId == 2450050 || itemId == 2450051 || itemId == 2450052 || itemId == 2450053) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);

        } else {
            c.announce(CWvsContext.enableActions());
        }
    }

    public static final void UseCosmetic(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory() || chr.inPVP()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        final byte slot = (byte) slea.readShort();
        final int itemId = slea.readInt();
        final Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || itemId / 10000 != 254 || (itemId / 1000) % 10 != chr.getGender()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
    }

    public static final void UseReturnScroll(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        if (!chr.isAlive() || chr.getMapId() == 749040100 || chr.hasBlockedInventory() || chr.isInBlockedMap() || chr.inPVP()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        c.getPlayer().updateTick(slea.readInt());
        final byte slot = (byte) slea.readShort();
        final int itemId = slea.readInt();
        final Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) {
            if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyReturnScroll(chr)) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
            } else {
                c.announce(CWvsContext.enableActions());
            }
        } else {
            c.announce(CWvsContext.enableActions());
        }
    }

    public static final void UseHammerScroll(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        if (!chr.isAlive() || chr.getMapId() == 749040100 || chr.hasBlockedInventory() || chr.isInBlockedMap() || chr.inPVP()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        c.getPlayer().updateTick(slea.readInt());
        final int slot = slea.readInt();
        final int itemId = slea.readInt();
        slea.readInt();//item type
        final Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((byte) slot);
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        final Equip equip = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readInt());
        c.announce(CWvsContext.enableActions());
        c.getPlayer().forceReAddItem(equip, MapleInventoryType.EQUIP);
        c.announce(MTSCSPacket.ViciousHammer(true, (byte) 0));
        c.announce(InventoryPacket.updateItemslot(equip));

        slea.readInt(); // Inventory type, Hammered eq is always EQ.
        final Equip item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readInt());
        // another int here, D3 49 DC 00
        if (item != null) {
            if (GameConstants.canHammer(item.getItemId()) && MapleItemInformationProvider.getInstance().getSlots(item.getItemId()) > 0 && item.getViciousHammer() < 2) {
                item.setViciousHammer((byte) (item.getViciousHammer() + 1));
                item.setUpgradeSlots((short) (item.getUpgradeSlots() + 1));
                c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIP);
                c.announce(MTSCSPacket.ViciousHammer(true, (byte) item.getViciousHammer()));
            } else {
                c.getPlayer().dropMessage(5, "You may not use it on this item.");
                c.announce(MTSCSPacket.ViciousHammer(true, (byte) 0));
            }
        }

    }

    public static final void UseAlienSocket(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        c.getPlayer().updateTick(slea.readInt());
        c.getPlayer().setScrolledPosition((short) 0);
        final Item alienSocket = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((byte) slea.readShort());
        final int alienSocketId = slea.readInt();
        final Item toMount = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readShort());
        if (alienSocket == null || alienSocketId != alienSocket.getItemId() || toMount == null || c.getPlayer().hasBlockedInventory()) {
            c.announce(InventoryPacket.getInventoryFull());
            return;
        }
        // Can only use once-> 2nd and 3rd must use NPC.
        final Equip eqq = (Equip) toMount;
        if (eqq.getSocketState() != 0) { // Used before
            c.getPlayer().dropMessage(1, "This item already has a socket.");
        } else {
            eqq.setSocket1(0); // First socket, GMS removed the other 2
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, alienSocket.getPosition(), (short) 1, false);
            c.getPlayer().forceReAddItem(toMount, MapleInventoryType.EQUIP);
        }
        c.announce(MTSCSPacket.useAlienSocket(true));
    }

    public static final void UseNebulite(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        c.getPlayer().updateTick(slea.readInt());
        c.getPlayer().setScrolledPosition((short) 0);
        final Item nebulite = c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((byte) slea.readShort());
        final int nebuliteId = slea.readInt();
        final Item toMount = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readShort());
        if (nebulite == null || nebuliteId != nebulite.getItemId() || toMount == null || c.getPlayer().hasBlockedInventory()) {
            c.announce(InventoryPacket.getInventoryFull());
            return;
        }
        final Equip eqq = (Equip) toMount;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        boolean success = false;
        if (eqq.getSocket1() == 0/* || eqq.getSocket2() == 0 || eqq.getSocket3() == 0*/) { // GMS removed 2nd and 3rd sockets, we can put into npc.
            final StructItemOption pot = ii.getSocketInfo(nebuliteId);
            if (pot != null && GameConstants.optionTypeFits(pot.optionType, eqq.getItemId())) {
                //if (eqq.getSocket1() == 0) { // priority comes first
                eqq.setSocket1(pot.opID);
                //}// else if (eqq.getSocket2() == 0) {
                //    eqq.setSocket2(pot.opID);
                //} else if (eqq.getSocket3() == 0) {
                //    eqq.setSocket3(pot.opID);
                //}
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.SETUP, nebulite.getPosition(), (short) 1, false);
                c.getPlayer().forceReAddItem(toMount, MapleInventoryType.EQUIP);
                success = true;
            }
        }
        c.getPlayer().getMap().broadcastMessage(CField.showNebuliteEffect(c.getPlayer().getId(), success));
        c.announce(CWvsContext.enableActions());
    }

    public static final void UseNebuliteFusion(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        c.getPlayer().updateTick(slea.readInt());
        c.getPlayer().setScrolledPosition((short) 0);
        final int nebuliteId1 = slea.readInt();
        final Item nebulite1 = c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((byte) slea.readShort());
        final int nebuliteId2 = slea.readInt();
        final Item nebulite2 = c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((byte) slea.readShort());
        final int mesos = slea.readInt();
        final int premiumQuantity = slea.readInt();
        if (nebulite1 == null || nebulite2 == null || nebuliteId1 != nebulite1.getItemId() || nebuliteId2 != nebulite2.getItemId() || (mesos == 0 && premiumQuantity == 0) || (mesos != 0 && premiumQuantity != 0) || mesos < 0 || premiumQuantity < 0 || c.getPlayer().hasBlockedInventory()) {
            c.getPlayer().dropMessage(1, "Failed to fuse Nebulite.");
            c.announce(InventoryPacket.getInventoryFull());
            return;
        }
        final int grade1 = GameConstants.getNebuliteGrade(nebuliteId1);
        final int grade2 = GameConstants.getNebuliteGrade(nebuliteId2);
        final int highestRank = grade1 > grade2 ? grade1 : grade2;
        if (grade1 == -1 || grade2 == -1 || (highestRank == 3 && premiumQuantity != 2) || (highestRank == 2 && premiumQuantity != 1)
                || (highestRank == 1 && mesos != 5000) || (highestRank == 0 && mesos != 3000) || (mesos > 0 && c.getPlayer().getMeso() < mesos)
                || (premiumQuantity > 0 && c.getPlayer().getItemQuantity(4420000, false) < premiumQuantity) || grade1 >= 4 || grade2 >= 4
                || (c.getPlayer().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < 1)) { // 4000 + = S, 3000 + = A, 2000 + = B, 1000 + = C, else = D
            c.announce(CField.useNebuliteFusion(c.getPlayer().getId(), 0, false));
            return; // Most of them were done in client, so we just send the unsuccessfull packet, as it is only here when they packet edit.
        }
        final int avg = (grade1 + grade2) / 2; // have to revise more about grades.
        final int rank = Randomizer.nextInt(100) < 4 ? (Randomizer.nextInt(100) < 70 ? (avg != 3 ? (avg + 1) : avg) : (avg != 0 ? (avg - 1) : 0)) : avg;
        // 4 % chance to up/down 1 grade, (70% to up, 30% to down), cannot up to S grade. =)
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final List<StructItemOption> pots = new LinkedList<>(ii.getAllSocketInfo(rank).values());
        int newId = 0;
        while (newId == 0) {
            StructItemOption pot = pots.get(Randomizer.nextInt(pots.size()));
            if (pot != null) {
                newId = pot.opID;
            }
        }
        if (mesos > 0) {
            c.getPlayer().gainMeso(-mesos, true);
        } else if (premiumQuantity > 0) {
            MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4420000, premiumQuantity, false, false);
        }
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.SETUP, nebulite1.getPosition(), (short) 1, false);
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.SETUP, nebulite2.getPosition(), (short) 1, false);
        MapleInventoryManipulator.addById(c, newId, (short) 1, "Fused from " + nebuliteId1 + " and " + nebuliteId2 + " on " + FileoutputUtil.CurrentReadable_Date());
        c.announce(CField.useNebuliteFusion(c.getPlayer().getId(), newId, true));
    }

    public static final void UseMagnify(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        c.getPlayer().updateTick(slea.readInt());
        c.getPlayer().setScrolledPosition((short) 0);
        final byte src = (byte) slea.readShort();
        final boolean insight = src == 127 && c.getPlayer().getTrait(MapleTraitType.sense).getLevel() >= 30;
        final Item magnify = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(src);
        final Item toReveal = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readShort());
        if ((magnify == null && !insight) || toReveal == null || c.getPlayer().hasBlockedInventory()) {
            c.announce(InventoryPacket.getInventoryFull());
            return;
        }
        final Equip eqq = (Equip) toReveal;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final int reqLevel = ii.getReqLevel(eqq.getItemId()) / 10;
        if (eqq.getState() == 1 && (insight || magnify.getItemId() == 2460003 || (magnify.getItemId() == 2460002 && reqLevel <= 12) || (magnify.getItemId() == 2460001 && reqLevel <= 7) || (magnify.getItemId() == 2460000 && reqLevel <= 3))) {
            final List<List<StructItemOption>> pots = new LinkedList<>(ii.getAllPotentialInfo().values());
            int new_state = Math.abs(eqq.getPotential1());
            if (new_state > 20 || new_state < 17) { // incase overflow
                new_state = 17;
            }
            int lines = 2; // default
            if (eqq.getPotential2() != 0) {
                lines++;
            }
            if (eqq.getPotential3() != 0) {
                lines++;
            }
            if (eqq.getPotential4() != 0) {
                lines++;
            }
            while (eqq.getState() != new_state) {
                //31001 = haste, 31002 = door, 31003 = se, 31004 = hb, 41005 = combat orders, 41006 = advanced blessing, 41007 = speed infusion
                for (int i = 0; i < lines; i++) { // minimum 2 lines, max 5
                    boolean rewarded = false;
                    while (!rewarded) {
                        StructItemOption pot = pots.get(Randomizer.nextInt(pots.size())).get(reqLevel);
                        if (pot != null && pot.reqLevel / 10 <= reqLevel && GameConstants.optionTypeFits(pot.optionType, eqq.getItemId()) && GameConstants.potentialIDFits(pot.opID, new_state, i)) { //optionType
                            //have to research optionType before making this truely official-like
                            if (i == 0) {
                                eqq.setPotential1(pot.opID);
                            } else if (i == 1) {
                                eqq.setPotential2(pot.opID);
                            } else if (i == 2) {
                                eqq.setPotential3(pot.opID);
                            } else if (i == 3) {
                                eqq.setPotential4(pot.opID);
                            } else if (i == 4) {
                                eqq.setPotential5(pot.opID);
                            }
                            rewarded = true;
                        }
                    }
                }
            }
            c.getPlayer().getTrait(MapleTraitType.insight).addExp((insight ? 10 : ((magnify.getItemId() + 2) - 2460000)) * 2, c.getPlayer());
            c.getPlayer().getMap().broadcastMessage(CField.showMagnifyingEffect(c.getPlayer().getId(), eqq.getPosition()));
            if (!insight) {
                c.announce(InventoryPacket.scrolledItem(magnify, toReveal, false, true));
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, magnify.getPosition(), (short) 1, false);
            } else {
                c.getPlayer().forceReAddItem(toReveal, MapleInventoryType.EQUIP);
            }
            c.announce(CWvsContext.enableActions());
        } else {
            c.announce(InventoryPacket.getInventoryFull());
            return;
        }
    }

    public static final void addToScrollLog(int accountID, int charID, int scrollID, int itemID, byte oldSlots, byte newSlots, byte viciousHammer, String result, boolean ws, boolean ls, int vega) {
        try {
            PreparedStatement ps = DatabaseConnection.getPlayerConnection().prepareStatement("INSERT INTO scroll_log VALUES(DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, accountID);
            ps.setInt(2, charID);
            ps.setInt(3, scrollID);
            ps.setInt(4, itemID);
            ps.setByte(5, oldSlots);
            ps.setByte(6, newSlots);
            ps.setByte(7, viciousHammer);
            ps.setString(8, result);
            ps.setByte(9, (byte) (ws ? 1 : 0));
            ps.setByte(10, (byte) (ls ? 1 : 0));
            ps.setInt(11, vega);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
            e.printStackTrace();
        }
    }

    public static final boolean UseUpgradeScroll(final short slot, final short dst, final short ws, final MapleClient c, final MapleCharacter chr, final boolean legendarySpirit) {
        return UseUpgradeScroll(slot, dst, ws, c, chr, 0, legendarySpirit);
    }

    public static final boolean UseUpgradeScroll(final short slot, final short dst, final short ws, final MapleClient c, final MapleCharacter chr, final int vegas, final boolean legendarySpirit) {
        boolean whiteScroll = false; // white scroll being used?
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        chr.setScrolledPosition((short) 0);
        if ((ws & 2) == 2) {
            whiteScroll = true;
        }
        Equip toScroll = null;
        if (dst < 0) {
            toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
        } else if (legendarySpirit) {
            toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(dst);
        }
        if (toScroll == null || c.getPlayer().hasBlockedInventory()) {
            c.announce(CWvsContext.enableActions());
            return false;
        }
        final short oldLevel = toScroll.getLevel();
        final int oldEnhance = toScroll.getEnhance();
        final byte oldState = toScroll.getState();
        final short oldFlag = toScroll.getFlag();
        final short oldSlots = toScroll.getUpgradeSlots();

        Item scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if (scroll == null) {
            scroll = chr.getInventory(MapleInventoryType.CASH).getItem(slot);
            if (scroll == null) {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(CWvsContext.enableActions());
                return false;
            }
        }
        if (scroll.getItemId() >= 2049300 && scroll.getItemId() <= 2049305) {
            c.getPlayer().dropMessage("This Scroll cannot be used here, please visit Cygnus Mentality in the Free Market.");
            c.announce(CWvsContext.enableActions());
            return false;
        }
        if (!GameConstants.isSpecialScroll(scroll.getItemId()) && !GameConstants.isCleanSlate(scroll.getItemId()) && !GameConstants.isEquipScroll(scroll.getItemId()) && !GameConstants.isPotentialScroll(scroll.getItemId())) {
            if (toScroll.getUpgradeSlots() < 1) {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(CWvsContext.enableActions());
                return false;
            }
        } else if (GameConstants.isEquipScroll(scroll.getItemId())) {
            if (toScroll.getUpgradeSlots() > 0 && toScroll.getEnhance() > 250) {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(CWvsContext.enableActions());
                return false;
            }
        } else if (GameConstants.isPotentialScroll(scroll.getItemId())) {
            final boolean isEpic = scroll.getItemId() / 100 == 20497;
            if ((!isEpic && toScroll.getState() >= 1) || (isEpic && toScroll.getState() >= 18) || (toScroll.getLevel() == 0 && toScroll.getUpgradeSlots() == 0 && toScroll.getItemId() / 10000 != 135 && !isEpic) || vegas > 0 || ii.isCash(toScroll.getItemId())) {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(CWvsContext.enableActions());
                return false;
            }
        } else if (GameConstants.isSpecialScroll(scroll.getItemId())) {
            if (ii.isCash(toScroll.getItemId())) {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(CWvsContext.enableActions());
                return false;
            }
        }
        if (!GameConstants.canScroll(toScroll.getItemId()) && !GameConstants.isChaosScroll(toScroll.getItemId())) {
            c.announce(InventoryPacket.getInventoryFull());
            c.announce(CWvsContext.enableActions());
            return false;
        }
        if ((GameConstants.isCleanSlate(scroll.getItemId()) || GameConstants.isTablet(scroll.getItemId()) || GameConstants.isGeneralScroll(scroll.getItemId()) || GameConstants.isChaosScroll(scroll.getItemId())) && (vegas > 0 || ii.isCash(toScroll.getItemId()))) {
            c.announce(InventoryPacket.getInventoryFull());
            c.announce(CWvsContext.enableActions());
            return false;
        }
        if (GameConstants.isTablet(scroll.getItemId()) && toScroll.getDurability() < 0) { //not a durability item
            c.announce(InventoryPacket.getInventoryFull());
            c.announce(CWvsContext.enableActions());
            return false;
        }

        /*
         if (toScroll.getDurability() >= 0) {
         if (GameConstants.isTablet(scroll.getItemId())) {
         System.out.println("test");
         c.announce(InventoryPacket.getInventoryFull());
         c.announce(CWvsContext.enableActions());
         return false;
         }
         if (!GameConstants.isPotentialScroll(scroll.getItemId())) {
         System.out.println("test1");
         c.announce(InventoryPacket.getInventoryFull());
         c.announce(CWvsContext.enableActions());
         return false;
         }
         if (!GameConstants.isEquipScroll(scroll.getItemId())) {
         System.out.println("test2");
         c.announce(InventoryPacket.getInventoryFull());
         c.announce(CWvsContext.enableActions());
         return false;
         }
         if (!GameConstants.isCleanSlate(scroll.getItemId())) {
         System.out.println("test3");
         c.announce(InventoryPacket.getInventoryFull());
         c.announce(CWvsContext.enableActions());
         return false;
         }
         if (!GameConstants.isSpecialScroll(scroll.getItemId())) {
         System.out.println("test4");
         c.announce(InventoryPacket.getInventoryFull());
         c.announce(CWvsContext.enableActions());
         return false;
         }
         if (!GameConstants.isChaosScroll(scroll.getItemId())) {
         System.out.println("test5");
         c.announce(InventoryPacket.getInventoryFull());
         c.announce(CWvsContext.enableActions());
         return false;
         }
         }
         */
        Item wscroll = null;

        // Anti cheat and validation
        List<Integer> scrollReqs = ii.getScrollReqs(scroll.getItemId());
        if (scrollReqs != null && scrollReqs.size() > 0 && !scrollReqs.contains(toScroll.getItemId())) {
            c.announce(InventoryPacket.getInventoryFull());
            c.announce(CWvsContext.enableActions());
            return false;
        }

        if (whiteScroll) {
            wscroll = chr.getInventory(MapleInventoryType.USE).findById(2340000);
            if (wscroll == null) {
                whiteScroll = false;
            }
        }
        if (GameConstants.isTablet(scroll.getItemId()) || GameConstants.isGeneralScroll(scroll.getItemId())) {
            switch (scroll.getItemId() % 1000 / 100) {
                case 0: //1h
                    if (GameConstants.isTwoHanded(toScroll.getItemId()) || !GameConstants.isWeapon(toScroll.getItemId())) {
                        c.announce(CWvsContext.enableActions());
                        return false;
                    }
                    break;
                case 1: //2h
                    if (!GameConstants.isTwoHanded(toScroll.getItemId()) || !GameConstants.isWeapon(toScroll.getItemId())) {
                        c.announce(CWvsContext.enableActions());
                        return false;
                    }
                    break;
                case 2: //armor
                    if (GameConstants.isAccessory(toScroll.getItemId()) || GameConstants.isWeapon(toScroll.getItemId())) {
                        c.announce(CWvsContext.enableActions());
                        return false;
                    }
                    break;
                case 3: //accessory
                    if (!GameConstants.isAccessory(toScroll.getItemId()) || GameConstants.isWeapon(toScroll.getItemId())) {
                        c.announce(CWvsContext.enableActions());
                        return false;
                    }
                    break;
            }
        }
        /*else if (!GameConstants.isAccessoryScroll(scroll.getItemId()) && !GameConstants.isChaosScroll(scroll.getItemId()) && !GameConstants.isCleanSlate(scroll.getItemId()) && !GameConstants.isEquipScroll(scroll.getItemId()) && !GameConstants.isPotentialScroll(scroll.getItemId()) && !GameConstants.isSpecialScroll(scroll.getItemId())) {
         if (!ii.canScroll(scroll.getItemId(), toScroll.getItemId())) {
         System.out.println("scroll: " + (scroll.getItemId() / 100) % 100);
         System.out.println("to scroll: " + toScroll.getItemId());
         System.out.println("test");
         c.announce(CWvsContext.enableActions());
         return false;
         }
         }
         */
        if (GameConstants.isAccessoryScroll(scroll.getItemId()) && !GameConstants.isAccessory(toScroll.getItemId())) {
            c.announce(CWvsContext.enableActions());
            return false;
        }

        if (scroll.getQuantity() <= 0) {
            c.announce(CWvsContext.enableActions());
            return false;
        }

        if (legendarySpirit && vegas == 0) {
            if (chr.getSkillLevel(SkillFactory.getSkill(chr.getStat().getSkillByJob(1003, chr.getJob()))) <= 0) {
                c.announce(CWvsContext.enableActions());
                return false;
            }
        }

        // Scroll Success/ Failure/ Curse
        Equip scrolled = (Equip) ii.scrollEquipWithId(toScroll, scroll, whiteScroll, chr, vegas);
        ScrollResult scrollSuccess;
        if (scrolled == null) {
            if (ItemFlag.SHIELD_WARD.check(oldFlag)) {
                scrolled = toScroll;
                scrollSuccess = Equip.ScrollResult.FAIL;
                scrolled.setFlag((short) (oldFlag - ItemFlag.SHIELD_WARD.getValue()));
            } else {
                scrollSuccess = Equip.ScrollResult.CURSE;
            }
        } else if ((scroll.getItemId() / 100 == 20497 && scrolled.getState() == 1) || scrolled.getLevel() > oldLevel || scrolled.getEnhance() > oldEnhance || scrolled.getState() > oldState || scrolled.getFlag() > oldFlag) {
            scrollSuccess = Equip.ScrollResult.SUCCESS;
            if (scrolled.getLevel() > oldLevel) {
                scrolled.setLevel((short) (scrolled.getLevel() - 1));
            }
        } else if ((GameConstants.isCleanSlate(scroll.getItemId()) && scrolled.getUpgradeSlots() > oldSlots)) {
            scrollSuccess = Equip.ScrollResult.SUCCESS;
        } else {
            scrollSuccess = Equip.ScrollResult.FAIL;
        }
        // Update
        chr.getInventory(GameConstants.getInventoryType(scroll.getItemId())).removeItem(scroll.getPosition(), (short) 1, false);
        if (whiteScroll) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, wscroll.getPosition(), (short) 1, false, false);
        } else if (scrollSuccess == Equip.ScrollResult.FAIL && scrolled.getUpgradeSlots() < oldSlots && c.getPlayer().getInventory(MapleInventoryType.CASH).findById(5640000) != null) {
            chr.setScrolledPosition(scrolled.getPosition());
            if (vegas == 0) {
                c.announce(CWvsContext.pamSongUI());
            }
        }

        if (scrollSuccess == Equip.ScrollResult.CURSE) {
            c.announce(InventoryPacket.scrolledItem(scroll, toScroll, true, false));
            if (dst < 0) {
                chr.getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
            } else {
                chr.getInventory(MapleInventoryType.EQUIP).removeItem(toScroll.getPosition());
            }
        } else if (vegas == 0) {
            c.announce(InventoryPacket.scrolledItem(scroll, scrolled, false, false));
        }

        chr.getMap().broadcastMessage(chr, CField.getScrollEffect(c.getPlayer().getId(), scrollSuccess, legendarySpirit, whiteScroll), vegas == 0);
        //addToScrollLog(chr.getAccountID(), chr.getId(), scroll.getItemId(), itemID, oldSlots, (byte)(scrolled == null ? -1 : scrolled.getUpgradeSlots()), oldVH, scrollSuccess.name(), whiteScroll, legendarySpirit, vegas);
        // equipped item was scrolled and changed
        if (dst < 0 && (scrollSuccess == Equip.ScrollResult.SUCCESS || scrollSuccess == Equip.ScrollResult.CURSE) && vegas == 0) {
            chr.equipChanged();
        }
        return true;
    }

    public static final boolean UseSkillBook(final byte slot, final int itemId, final MapleClient c, final MapleCharacter chr) {
        final Item toUse = chr.getInventory(GameConstants.getInventoryType(itemId)).getItem(slot);

        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || chr.hasBlockedInventory()) {
            return false;
        }
        final Map<String, Integer> skilldata = MapleItemInformationProvider.getInstance().getEquipStats(toUse.getItemId());
        if (skilldata == null) { // Hacking or used an unknown item
            return false;
        }
        boolean canuse = false, success = false;
        int skill = 0, maxlevel = 0;

        final Integer SuccessRate = skilldata.get("success");
        final Integer ReqSkillLevel = skilldata.get("reqSkillLevel");
        final Integer MasterLevel = skilldata.get("masterLevel");

        byte i = 0;
        Integer CurrentLoopedSkillId;
        while (true) {
            CurrentLoopedSkillId = skilldata.get("skillid" + i);
            i++;
            if (CurrentLoopedSkillId == null || MasterLevel == null) {
                break; // End of data
            }
            final Skill CurrSkillData = SkillFactory.getSkill(CurrentLoopedSkillId);
            if (CurrSkillData != null && CurrSkillData.canBeLearnedBy(chr.getJob()) && (ReqSkillLevel == null || chr.getSkillLevel(CurrSkillData) >= ReqSkillLevel)) {
                if (chr.getMasterLevel(CurrSkillData) < CurrSkillData.getMaxLevel()) {
                    canuse = true;
                    success = true;
                    chr.changeSingleSkillLevel(CurrSkillData, chr.getSkillLevel(CurrSkillData), (chr.getMasterLevel(CurrSkillData) + 5));
                    MapleInventoryManipulator.removeFromSlot(c, GameConstants.getInventoryType(itemId), slot, (short) 1, false);
                }
                break;
            }
        }
        c.getPlayer().getMap().broadcastMessage(CWvsContext.useSkillBook(chr, skill, maxlevel, canuse, success));
        c.announce(CWvsContext.enableActions());
        return canuse;
    }

    public static final void UseCatchItem(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        c.getPlayer().updateTick(slea.readInt());
        c.getPlayer().setScrolledPosition((short) 0);
        final byte slot = (byte) slea.readShort();
        final int itemid = slea.readInt();
        final MapleMonster mob = chr.getMap().getMonsterByOid(slea.readInt());
        final Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        final MapleMap map = chr.getMap();

        if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemid && mob != null && !chr.hasBlockedInventory() && itemid / 10000 == 227 && MapleItemInformationProvider.getInstance().getCardMobId(itemid) == mob.getId()) {
            if (!MapleItemInformationProvider.getInstance().isMobHP(itemid) || mob.getHp() <= mob.getMobMaxHp() / 2) {
                map.broadcastMessage(MobPacket.catchMonster(mob.getObjectId(), itemid, (byte) 1));
                map.killMonster(mob, chr, true, false, (byte) 1);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false, false);
                if (MapleItemInformationProvider.getInstance().getCreateId(itemid) > 0) {
                    MapleInventoryManipulator.addById(c, MapleItemInformationProvider.getInstance().getCreateId(itemid), (short) 1, "Catch item " + itemid + " on " + FileoutputUtil.CurrentReadable_Date());
                }
            } else {
                map.broadcastMessage(MobPacket.catchMonster(mob.getObjectId(), itemid, (byte) 0));
                c.announce(CWvsContext.catchMob(mob.getId(), itemid, (byte) 0));
            }
        }
        c.announce(CWvsContext.enableActions());
    }

    public static final void UseMountFood(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        c.getPlayer().updateTick(slea.readInt());
        final byte slot = (byte) slea.readShort();
        final int itemid = slea.readInt(); //2260000 usually
        final Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        final MapleMount mount = chr.getMount();

        if (itemid / 10000 == 226 && toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemid && mount != null && !c.getPlayer().hasBlockedInventory()) {
            final int fatigue = mount.getFatigue();

            boolean levelup = false;
            mount.setFatigue((byte) -30);

            if (fatigue > 0) {
                mount.increaseExp();
                final int level = mount.getLevel();
                if (level < 30 && mount.getExp() >= GameConstants.getMountExpNeededForLevel(level + 1)) {
                    mount.setLevel((byte) (level + 1));
                    levelup = true;
                }
            }
            chr.getMap().broadcastMessage(CWvsContext.updateMount(chr, levelup));
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
        c.announce(CWvsContext.enableActions());
    }

    public static final void UseScriptedNPCItem(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        c.getPlayer().updateTick(slea.readInt());
        final byte slot = (byte) slea.readShort();
        final int itemId = slea.readInt();
        final Item toUse = chr.getInventory(GameConstants.getInventoryType(itemId)).getItem(slot);
        long expiration_days = 0;
        int mountid = 0;

        if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId && !chr.hasBlockedInventory() && !chr.inPVP()) {
            switch (toUse.getItemId()) {
                case 2430007: { // Blank Compass
                    final MapleInventory inventory = chr.getInventory(MapleInventoryType.SETUP);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);

                    if (inventory.countById(3994102) >= 20 // Compass Letter "North"
                            && inventory.countById(3994103) >= 20 // Compass Letter "South"
                            && inventory.countById(3994104) >= 20 // Compass Letter "East"
                            && inventory.countById(3994105) >= 20) { // Compass Letter "West"
                        MapleInventoryManipulator.addById(c, 2430008, (short) 1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date()); // Gold Compass
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994102, 20, false, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994103, 20, false, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994104, 20, false, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994105, 20, false, false);
                    } else {
                        MapleInventoryManipulator.addById(c, 2430007, (short) 1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date()); // Blank Compass
                    }
                    NPCScriptManager.getInstance().start(c, 2084001);
                    break;
                }
                case 2430008: { // Gold Compass
                    chr.saveLocation(SavedLocationType.RICHIE);
                    MapleMap map;
                    boolean warped = false;

                    for (int i = 390001000; i <= 390001004; i++) {
                        map = c.getChannelServer().getMapFactory().getMap(i);

                        if (map.getCharactersSize() == 0) {
                            chr.changeMap(map, map.getPortal(0));
                            warped = true;
                            break;
                        }
                    }
                    if (warped) { // Removal of gold compass
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                    } else { // Or mabe some other message.
                        c.getPlayer().dropMessage(5, "All maps are currently in use, please try again later.");
                    }
                    break;
                }
                case 2430112: //miracle cube fragment
                    if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430112) >= 25) {
                            if (MapleInventoryManipulator.checkSpace(c, 2049400, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 25, true, false)) {
                                MapleInventoryManipulator.addById(c, 2049400, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430112) >= 10) {
                            if (MapleInventoryManipulator.checkSpace(c, 2049400, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 10, true, false)) {
                                MapleInventoryManipulator.addById(c, 2049401, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "There needs to be 10 Fragments for a Potential Scroll, 25 for Advanced Potential Scroll.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make some space.");
                    }
                    break;
                case 2430481: //super miracle cube fragment
                    c.getPlayer().dropMessage(5, "Please take these shards to coco in nearest town.");
                    break;
                case 2430691: // nebulite diffuser fragment
                    if (c.getPlayer().getInventory(MapleInventoryType.CASH).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430691) >= 10) {
                            if (MapleInventoryManipulator.checkSpace(c, 5750001, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 10, true, false)) {
                                MapleInventoryManipulator.addById(c, 5750001, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "There needs to be 10 Fragments for a Nebulite Diffuser.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make some space.");
                    }
                    break;
                case 2430748: // premium fusion ticket 
                    if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430748) >= 20) {
                            if (MapleInventoryManipulator.checkSpace(c, 4420000, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 20, true, false)) {
                                MapleInventoryManipulator.addById(c, 4420000, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "There needs to be 20 Fragments for a Premium Fusion Ticket.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make some space.");
                    }
                    break;
                case 2430692: // nebulite box
                    if (c.getPlayer().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430692) >= 1) {
                            final int rank = Randomizer.nextInt(100) < 30 ? (Randomizer.nextInt(100) < 4 ? 2 : 1) : 0;
                            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                            final List<StructItemOption> pots = new LinkedList<>(ii.getAllSocketInfo(rank).values());
                            int newId = 0;
                            while (newId == 0) {
                                StructItemOption pot = pots.get(Randomizer.nextInt(pots.size()));
                                if (pot != null) {
                                    newId = pot.opID;
                                }
                            }
                            if (MapleInventoryManipulator.checkSpace(c, newId, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false)) {
                                MapleInventoryManipulator.addById(c, newId, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                                c.announce(InfoPacket.getShowItemGain(newId, (short) 1, true));
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "You do not have a Nebulite Box.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make some space.");
                    }
                    break;
                case 5680019: {//starling hair 
                    //if (c.getPlayer().getGender() == 1) {
                    int hair = 32150 + (c.getPlayer().getHair() % 10);
                    c.getPlayer().setHair(hair);
                    c.getPlayer().updateSingleStat(MapleStat.HAIR, hair);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, slot, (byte) 1, false);
                    //}
                    break;
                }
                case 5680020: {//starling hair 
                    //if (c.getPlayer().getGender() == 0) {
                    int hair = 32160 + (c.getPlayer().getHair() % 10);
                    c.getPlayer().setHair(hair);
                    c.getPlayer().updateSingleStat(MapleStat.HAIR, hair);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, slot, (byte) 1, false);
                    //}
                    break;
                }
                case 3994225:
                    c.getPlayer().dropMessage(5, "Please bring this item to the NPC.");
                    break;
                case 2430212: //energy drink
                    MapleQuestStatus marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.ENERGY_DRINK));
                    if (marr.getCustomData() == null) {
                        marr.setCustomData("0");
                    }
                    long lastTime = Long.parseLong(marr.getCustomData());
                    if (lastTime + (600000) > System.currentTimeMillis()) {
                        c.getPlayer().dropMessage(5, "You can only use one energy drink per 10 minutes.");
                    } else if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 5);
                    }
                    break;
                case 2430213: //energy drink
                    marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.ENERGY_DRINK));
                    if (marr.getCustomData() == null) {
                        marr.setCustomData("0");
                    }
                    lastTime = Long.parseLong(marr.getCustomData());
                    if (lastTime + (600000) > System.currentTimeMillis()) {
                        c.getPlayer().dropMessage(5, "You can only use one energy drink per 10 minutes.");
                    } else if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 10);
                    }
                    break;
                case 2430220: //energy drink
                case 2430214: //energy drink
                    if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 30);
                    }
                    break;
                case 2430227: //energy drink
                    if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 50);
                    }
                    break;
                case 2430231: //energy drink
                    marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.ENERGY_DRINK));
                    if (marr.getCustomData() == null) {
                        marr.setCustomData("0");
                    }
                    lastTime = Long.parseLong(marr.getCustomData());
                    if (lastTime + (600000) > System.currentTimeMillis()) {
                        c.getPlayer().dropMessage(5, "You can only use one energy drink per 10 minutes.");
                    } else if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 40);
                    }
                    break;
                case 2430144: //smb
                    final int itemid = Randomizer.nextInt(373) + 2290000;
                    if (MapleItemInformationProvider.getInstance().itemExists(itemid) && !MapleItemInformationProvider.getInstance().getName(itemid).contains("Special") && !MapleItemInformationProvider.getInstance().getName(itemid).contains("Event")) {
                        MapleInventoryManipulator.addById(c, itemid, (short) 1, "Reward item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                    }
                    break;
                case 2430370:
                    if (MapleInventoryManipulator.checkSpace(c, 2028062, (short) 1, "")) {
                        MapleInventoryManipulator.addById(c, 2028062, (short) 1, "Reward item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                    }
                    break;
                case 2430158: //lion king
                    if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000630) >= 100) {
                            if (MapleInventoryManipulator.checkSpace(c, 4310010, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false)) {
                                MapleInventoryManipulator.addById(c, 4310010, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000630) >= 50) {
                            if (MapleInventoryManipulator.checkSpace(c, 4310009, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false)) {
                                MapleInventoryManipulator.addById(c, 4310009, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "There needs to be 50 Purification Totems for a Noble Lion King Medal, 100 for Royal Lion King Medal.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make some space.");
                    }
                    break;
                case 2430159:
                    MapleQuest.getInstance(3182).forceComplete(c.getPlayer(), 2161004);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                    break;
                case 2430200: //thunder stone
                    if (c.getPlayer().getQuestStatus(31152) != 2) {
                        c.getPlayer().dropMessage(5, "You have no idea how to use it.");
                    } else {
                        if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= 1) {
                            if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000660) >= 1 && c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000661) >= 1 && c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000662) >= 1 && c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000663) >= 1) {
                                if (MapleInventoryManipulator.checkSpace(c, 4032923, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false) && MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000660, 1, true, false) && MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000661, 1, true, false) && MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000662, 1, true, false) && MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000663, 1, true, false)) {
                                    MapleInventoryManipulator.addById(c, 4032923, (short) 1, "Scripted item: " + toUse.getItemId() + " on " + FileoutputUtil.CurrentReadable_Date());
                                } else {
                                    c.getPlayer().dropMessage(5, "Please make some space.");
                                }
                            } else {
                                c.getPlayer().dropMessage(5, "There needs to be 1 of each Stone for a Dream Key.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "Please make some space.");
                        }
                    }
                    break;
                case 2430130://skill boster
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                    c.getPlayer().skillBoost();
                    c.getPlayer().dropTopMessage("Gained +5 SP");
                    return;
                case 2430131: //energy charge
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                    c.getPlayer().miniLevelUp();
                    break;
                case 2430132:
                case 2430133:
                case 2430134: //resistance box
                case 2430142:
                    if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getJob() == 3200 || c.getPlayer().getJob() == 3210 || c.getPlayer().getJob() == 3211 || c.getPlayer().getJob() == 3212) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                            MapleInventoryManipulator.addById(c, 1382101, (short) 1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                        } else if (c.getPlayer().getJob() == 3300 || c.getPlayer().getJob() == 3310 || c.getPlayer().getJob() == 3311 || c.getPlayer().getJob() == 3312) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                            MapleInventoryManipulator.addById(c, 1462093, (short) 1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                        } else if (c.getPlayer().getJob() == 3500 || c.getPlayer().getJob() == 3510 || c.getPlayer().getJob() == 3511 || c.getPlayer().getJob() == 3512) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                            MapleInventoryManipulator.addById(c, 1492080, (short) 1, "Scripted item: " + itemId + " on " + FileoutputUtil.CurrentReadable_Date());
                        } else {
                            c.getPlayer().dropMessage(5, "You may not use this item.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Make some space.");
                    }
                    break;
                case 2430036: //croco 1 day
                    mountid = 1027;
                    expiration_days = 1;
                    break;
                case 2430170: //croco 7 day
                    mountid = 1027;
                    expiration_days = 7;
                    break;
                case 2430037: //black scooter 1 day
                    mountid = 1028;
                    expiration_days = 1;
                    break;
                case 2430038: //pink scooter 1 day
                    mountid = 1029;
                    expiration_days = 1;
                    break;
                case 2430039: //clouds 1 day
                    mountid = 1030;
                    expiration_days = 1;
                    break;
                case 2430040: //balrog 1 day
                    mountid = 1031;
                    expiration_days = 1;
                    break;
                case 2430223: //balrog 1 day
                    mountid = 1031;
                    expiration_days = 15;
                    break;
                case 2430259: //balrog 1 day
                    mountid = 1031;
                    expiration_days = 3;
                    break;
                case 2430242: //motorcycle
                    mountid = 80001018;
                    expiration_days = 10;
                    break;
                case 2430243: //power suit
                    mountid = 80001019;
                    expiration_days = 10;
                    break;
                case 2430261: //power suit
                    mountid = 80001019;
                    expiration_days = 3;
                    break;
                case 2430249: //motorcycle
                    mountid = 80001027;
                    expiration_days = 3;
                    break;
                case 2430225: //balrog 1 day
                    mountid = 1031;
                    expiration_days = 10;
                    break;
                case 2430053: //croco 30 day
                    mountid = 1027;
                    expiration_days = 1;
                    break;
                case 2430054: //black scooter 30 day
                    mountid = 1028;
                    expiration_days = 30;
                    break;
                case 2430055: //pink scooter 30 day
                    mountid = 1029;
                    expiration_days = 30;
                    break;
                case 2430257: //pink
                    mountid = 1029;
                    expiration_days = 7;
                    break;
                case 2430056: //mist rog 30 day
                    mountid = 1035;
                    expiration_days = 30;
                    break;
                case 2430057:
                    mountid = 1033;
                    expiration_days = 30;
                    break;
                case 2430072: //ZD tiger 7 day
                    mountid = 1034;
                    expiration_days = 7;
                    break;
                case 2430073: //lion 15 day
                    mountid = 1036;
                    expiration_days = 15;
                    break;
                case 2430074: //unicorn 15 day
                    mountid = 1037;
                    expiration_days = 15;
                    break;
                case 2430272: //low rider 15 day
                    mountid = 1038;
                    expiration_days = 3;
                    break;
                case 2430275: //spiegelmann
                    mountid = 80001033;
                    expiration_days = 7;
                    break;
                case 2430075: //low rider 15 day
                    mountid = 1038;
                    expiration_days = 15;
                    break;
                case 2430076: //red truck 15 day
                    mountid = 1039;
                    expiration_days = 15;
                    break;
                case 2430077: //gargoyle 15 day
                    mountid = 1040;
                    expiration_days = 15;
                    break;
                case 2430080: //shinjo 20 day
                    mountid = 1042;
                    expiration_days = 20;
                    break;
                case 2430082: //orange mush 7 day
                    mountid = 1044;
                    expiration_days = 7;
                    break;
                case 2430260: //orange mush 7 day
                    mountid = 1044;
                    expiration_days = 3;
                    break;
                case 2430091: //nightmare 10 day
                    mountid = 1049;
                    expiration_days = 10;
                    break;
                case 2430092: //yeti 10 day
                    mountid = 1050;
                    expiration_days = 10;
                    break;
                case 2430263: //yeti 10 day
                    mountid = 1050;
                    expiration_days = 3;
                    break;
                case 2430093: //ostrich 10 day
                    mountid = 1051;
                    expiration_days = 10;
                    break;
                case 2430101: //pink bear 10 day
                    mountid = 1052;
                    expiration_days = 10;
                    break;
                case 2430102: //transformation robo 10 day
                    mountid = 1053;
                    expiration_days = 10;
                    break;
                case 2430103: //chicken 30 day
                    mountid = 1054;
                    expiration_days = 30;
                    break;
                case 2430266: //chicken 30 day
                    mountid = 1054;
                    expiration_days = 3;
                    break;
                case 2430265: //chariot
                    mountid = 1151;
                    expiration_days = 3;
                    break;
                case 2430258: //law officer
                    mountid = 1115;
                    expiration_days = 365;
                    break;
                case 2430117: //lion 1 year
                    mountid = 1036;
                    expiration_days = 365;
                    break;
                case 2430118: //red truck 1 year
                    mountid = 1039;
                    expiration_days = 365;
                    break;
                case 2430119: //gargoyle 1 year
                    mountid = 1040;
                    expiration_days = 365;
                    break;
                case 2430120: //unicorn 1 year
                    mountid = 1037;
                    expiration_days = 365;
                    break;
                case 2430271: //owl 30 day
                    mountid = 1069;
                    expiration_days = 3;
                    break;
                case 2430136: //owl 30 day
                    mountid = 1069;
                    expiration_days = 30;
                    break;
                case 2430137: //owl 1 year
                    mountid = 1069;
                    expiration_days = 365;
                    break;
                case 2430145: //mothership
                    mountid = 1070;
                    expiration_days = 30;
                    break;
                case 2430146: //mothership
                    mountid = 1070;
                    expiration_days = 365;
                    break;
                case 2430147: //mothership
                    mountid = 1071;
                    expiration_days = 30;
                    break;
                case 2430148: //mothership
                    mountid = 1071;
                    expiration_days = 365;
                    break;
                case 2430135: //os4
                    mountid = 1065;
                    expiration_days = 15;
                    break;
                case 2430149: //leonardo 30 day
                    mountid = 1072;
                    expiration_days = 30;
                    break;
                case 2430262: //leonardo 30 day
                    mountid = 1072;
                    expiration_days = 3;
                    break;
                case 2430179: //witch 15 day
                    mountid = 1081;
                    expiration_days = 15;
                    break;
                case 2430264: //witch 15 day
                    mountid = 1081;
                    expiration_days = 3;
                    break;
                case 2430201: //giant bunny 60 day
                    mountid = 1096;
                    expiration_days = 60;
                    break;
                case 2430228: //tiny bunny 60 day
                    mountid = 1101;
                    expiration_days = 60;
                    break;
                case 2430276: //tiny bunny 60 day
                    mountid = 1101;
                    expiration_days = 15;
                    break;
                case 2430277: //tiny bunny 60 day
                    mountid = 1101;
                    expiration_days = 365;
                    break;
                case 2430283: //trojan
                    mountid = 1025;
                    expiration_days = 10;
                    break;
                case 2430291: //hot air
                    mountid = 1145;
                    expiration_days = -1;
                    break;
                case 2430293: //nadeshiko
                    mountid = 1146;
                    expiration_days = -1;
                    break;
                case 2430295: //pegasus
                    mountid = 1147;
                    expiration_days = -1;
                    break;
                case 2430297: //dragon
                    mountid = 1148;
                    expiration_days = -1;
                    break;
                case 2430299: //broom
                    mountid = 1149;
                    expiration_days = -1;
                    break;
                case 2430301: //cloud
                    mountid = 1150;
                    expiration_days = -1;
                    break;
                case 2430303: //chariot
                    mountid = 1151;
                    expiration_days = -1;
                    break;
                case 2430305: //nightmare
                    mountid = 1152;
                    expiration_days = -1;
                    break;
                case 2430307: //rog
                    mountid = 1153;
                    expiration_days = -1;
                    break;
                case 2430309: //mist rog
                    mountid = 1154;
                    expiration_days = -1;
                    break;
                case 2430311: //owl
                    mountid = 1156;
                    expiration_days = -1;
                    break;
                case 2430313: //helicopter
                    mountid = 1156;
                    expiration_days = -1;
                    break;
                case 2430315: //pentacle
                    mountid = 1118;
                    expiration_days = -1;
                    break;
                case 2430317: //frog
                    mountid = 1121;
                    expiration_days = -1;
                    break;
                case 2430319: //turtle
                    mountid = 1122;
                    expiration_days = -1;
                    break;
                case 2430321: //buffalo
                    mountid = 1123;
                    expiration_days = -1;
                    break;
                case 2430323: //tank
                    mountid = 1124;
                    expiration_days = -1;
                    break;
                case 2430325: //viking
                    mountid = 1129;
                    expiration_days = -1;
                    break;
                case 2430327: //pachinko
                    mountid = 1130;
                    expiration_days = -1;
                    break;
                case 2430329: //kurenai
                    mountid = 1063;
                    expiration_days = -1;
                    break;
                case 2430331: //horse
                    mountid = 1025;
                    expiration_days = -1;
                    break;
                case 2430333: //tiger
                    mountid = 1034;
                    expiration_days = -1;
                    break;
                case 2430335: //hyena
                    mountid = 1136;
                    expiration_days = -1;
                    break;
                case 2430337: //ostrich
                    mountid = 1051;
                    expiration_days = -1;
                    break;
                case 2430339: //low rider
                    mountid = 1138;
                    expiration_days = -1;
                    break;
                case 2430341: //napoleon
                    mountid = 1139;
                    expiration_days = -1;
                    break;
                case 2430343: //croking
                    mountid = 1027;
                    expiration_days = -1;
                    break;
                case 2430346: //lovely
                    mountid = 1029;
                    expiration_days = -1;
                    break;
                case 2430348: //retro
                    mountid = 1028;
                    expiration_days = -1;
                    break;
                case 2430350: //f1
                    mountid = 1033;
                    expiration_days = -1;
                    break;
                case 2430352: //power suit
                    mountid = 1064;
                    expiration_days = -1;
                    break;
                case 2430354: //giant rabbit
                    mountid = 1096;
                    expiration_days = -1;
                    break;
                case 2430356: //small rabit
                    mountid = 1101;
                    expiration_days = -1;
                    break;
                case 2430358: //rabbit rickshaw
                    mountid = 1102;
                    expiration_days = -1;
                    break;
                case 2430360: //chicken
                    mountid = 1054;
                    expiration_days = -1;
                    break;
                case 2430362: //transformer
                    mountid = 1053;
                    expiration_days = -1;
                    break;
                case 2430292: //hot air
                    mountid = 1145;
                    expiration_days = 90;
                    break;
                case 2430294: //nadeshiko
                    mountid = 1146;
                    expiration_days = 90;
                    break;
                case 2430296: //pegasus
                    mountid = 1147;
                    expiration_days = 90;
                    break;
                case 2430298: //dragon
                    mountid = 1148;
                    expiration_days = 90;
                    break;
                case 2430300: //broom
                    mountid = 1149;
                    expiration_days = 90;
                    break;
                case 2430302: //cloud
                    mountid = 1150;
                    expiration_days = 90;
                    break;
                case 2430304: //chariot
                    mountid = 1151;
                    expiration_days = 90;
                    break;
                case 2430306: //nightmare
                    mountid = 1152;
                    expiration_days = 90;
                    break;
                case 2430308: //rog
                    mountid = 1153;
                    expiration_days = 90;
                    break;
                case 2430310: //mist rog
                    mountid = 1154;
                    expiration_days = 90;
                    break;
                case 2430312: //owl
                    mountid = 1156;
                    expiration_days = 90;
                    break;
                case 2430314: //helicopter
                    mountid = 1156;
                    expiration_days = 90;
                    break;
                case 2430316: //pentacle
                    mountid = 1118;
                    expiration_days = 90;
                    break;
                case 2430318: //frog
                    mountid = 1121;
                    expiration_days = 90;
                    break;
                case 2430320: //turtle
                    mountid = 1122;
                    expiration_days = 90;
                    break;
                case 2430322: //buffalo
                    mountid = 1123;
                    expiration_days = 90;
                    break;
                case 2430326: //viking
                    mountid = 1129;
                    expiration_days = 90;
                    break;
                case 2430328: //pachinko
                    mountid = 1130;
                    expiration_days = 90;
                    break;
                case 2430330: //kurenai
                    mountid = 1063;
                    expiration_days = 90;
                    break;
                case 2430332: //horse
                    mountid = 1025;
                    expiration_days = 90;
                    break;
                case 2430334: //tiger
                    mountid = 1034;
                    expiration_days = 90;
                    break;
                case 2430336: //hyena
                    mountid = 1136;
                    expiration_days = 90;
                    break;
                case 2430338: //ostrich
                    mountid = 1051;
                    expiration_days = 90;
                    break;
                case 2430340: //low rider
                    mountid = 1138;
                    expiration_days = 90;
                    break;
                case 2430342: //napoleon
                    mountid = 1139;
                    expiration_days = 90;
                    break;
                case 2430344: //croking
                    mountid = 1027;
                    expiration_days = 90;
                    break;
                case 2430347: //lovely
                    mountid = 1029;
                    expiration_days = 90;
                    break;
                case 2430349: //retro
                    mountid = 1028;
                    expiration_days = 90;
                    break;
                case 2430351: //f1
                    mountid = 1033;
                    expiration_days = 90;
                    break;
                case 2430353: //power suit
                    mountid = 1064;
                    expiration_days = 90;
                    break;
                case 2430355: //giant rabbit
                    mountid = 1096;
                    expiration_days = 90;
                    break;
                case 2430357: //small rabit
                    mountid = 1101;
                    expiration_days = 90;
                    break;
                case 2430359: //rabbit rickshaw
                    mountid = 1102;
                    expiration_days = 90;
                    break;
                case 2430361: //chicken
                    mountid = 1054;
                    expiration_days = 90;
                    break;
                case 2430363: //transformer
                    mountid = 1053;
                    expiration_days = 90;
                    break;
                case 2430324: //high way
                    mountid = 1158;
                    expiration_days = -1;
                    break;
                case 2430345: //high way
                    mountid = 1158;
                    expiration_days = 90;
                    break;
                case 2430367: //law off
                    mountid = 1115;
                    expiration_days = 3;
                    break;
                case 2430365: //pony
                    mountid = 1025;
                    expiration_days = 365;
                    break;
                case 2430366: //pony
                    mountid = 1025;
                    expiration_days = 15;
                    break;
                case 2430369: //nightmare
                    mountid = 1049;
                    expiration_days = 10;
                    break;
                case 2430392: //speedy
                    mountid = 80001038;
                    expiration_days = 90;
                    break;
                case 2430476: //red truck? but name is pegasus?
                    mountid = 1039;
                    expiration_days = 15;
                    break;
                case 2430477: //red truck? but name is pegasus?
                    mountid = 1039;
                    expiration_days = 365;
                    break;
                case 2430232: //fortune
                    mountid = 1106;
                    expiration_days = 10;
                    break;
                case 2430511: //spiegel
                    mountid = 80001033;
                    expiration_days = 15;
                    break;
                case 2430512: //rspiegel
                    mountid = 80001033;
                    expiration_days = 365;
                    break;
                case 2430536: //buddy buggy
                    mountid = 80001114;
                    expiration_days = 365;
                    break;
                case 2430537: //buddy buggy
                    mountid = 80001114;
                    expiration_days = 15;
                    break;
                case 2430229: //bunny rickshaw 60 day
                    mountid = 1102;
                    expiration_days = 60;
                    break;
                case 2430199: //santa sled
                    mountid = 1102;
                    expiration_days = 60;
                    break;
                case 2430206: //race
                    mountid = 1089;
                    expiration_days = 7;
                    break;
                case 2430211: //race
                    mountid = 80001009;
                    expiration_days = 30;
                    break;
                case 5680001: {
                    c.getPlayer().superbuff();
                    break;
                }
                default:
                    System.out.println("New scripted item : " + toUse.getItemId());
                    break;
            }
        }
        if (mountid > 0) {
            mountid = c.getPlayer().getStat().getSkillByJob(mountid, c.getPlayer().getJob());
            final int fk = GameConstants.getMountItem(mountid, c.getPlayer());
            if (GameConstants.GMS && fk > 0 && mountid < 80001000) { //TODO JUMP
                for (int i = 80001001; i < 80001999; i++) {
                    final Skill skill = SkillFactory.getSkill(i);
                    if (skill != null && GameConstants.getMountItem(skill.getId(), c.getPlayer()) == fk) {
                        mountid = i;
                        break;
                    }
                }
            }
            if (c.getPlayer().getSkillLevel(mountid) > 0) {
                c.getPlayer().dropMessage(5, "You already have this skill.");
            } else if (SkillFactory.getSkill(mountid) == null || GameConstants.getMountItem(mountid, c.getPlayer()) == 0) {
                c.getPlayer().dropMessage(5, "The skill could not be gained.");
            } else if (expiration_days > 0) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(mountid), (byte) 1, (byte) 1, System.currentTimeMillis() + (long) (expiration_days * 24 * 60 * 60 * 1000));
                c.getPlayer().dropMessage(5, "The skill has been attained.");
            }
        }
        c.announce(CWvsContext.enableActions());
    }

    public static final void UseSummonBag(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        if (!chr.isAlive() || chr.hasBlockedInventory() || chr.inPVP()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        c.getPlayer().updateTick(slea.readInt());
        final byte slot = (byte) slea.readShort();
        final int itemId = slea.readInt();
        final Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

        if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId && (c.getPlayer().getMapId() < 910000000 || c.getPlayer().getMapId() > 910000022)) {
            final Map<String, Integer> toSpawn = MapleItemInformationProvider.getInstance().getEquipStats(itemId);

            if (toSpawn == null) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            MapleMonster ht = null;
            int type = 0;
            for (Entry<String, Integer> i : toSpawn.entrySet()) {
                if (i.getKey().startsWith("mob") && Randomizer.nextInt(99) <= i.getValue()) {
                    ht = MapleLifeFactory.getMonster(Integer.parseInt(i.getKey().substring(3)));
                    chr.getMap().spawnMonster_sSack(ht, chr.getPosition(), type);
                }
            }
            if (ht == null) {
                c.announce(CWvsContext.enableActions());
                return;
            }

            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
        c.announce(CWvsContext.enableActions());
    }

    public static final void UseTreasureChest(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        chr.dropMessage(5, "disabled.");
        c.announce(CWvsContext.enableActions());
    }

    public static final void UseCashItem(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c.getPlayer() == null || c.getPlayer().getMap() == null || c.getPlayer().inPVP() || c.getPlayer().run) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        c.getPlayer().updateTick(slea.readInt());
        c.getPlayer().setScrolledPosition((short) 0);
        final byte slot = (byte) slea.readShort();
        final int itemId = slea.readInt();

        final Item toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot);
        if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1 || c.getPlayer().hasBlockedInventory()) {
            c.announce(CWvsContext.enableActions());
            return;
        }

        boolean used = false, cc = false;

        switch (itemId) {
            case 5043001: // NPC Teleport Rock
            case 5043000: { // NPC Teleport Rock
                final short questid = slea.readShort();
                final int npcid = slea.readInt();
                final MapleQuest quest = MapleQuest.getInstance(questid);

                if (c.getPlayer().getQuest(quest).getStatus() == 1 && quest.canComplete(c.getPlayer(), npcid)) {
                    final int mapId = MapleLifeFactory.getNPCLocation(npcid);
                    if (mapId != -1) {
                        final MapleMap map = c.getChannelServer().getMapFactory().getMap(mapId);
                        if (map.containsNPC(npcid) && !FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit()) && !FieldLimitType.VipRock.check(map.getFieldLimit()) && !c.getPlayer().isInBlockedMap()) {
                            c.getPlayer().changeMap(map, map.getPortal(0));
                        }
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, "Unknown error has occurred.");
                    }
                }
                break;
            }
            case 5041001:
            case 5040004:
            case 5040003:
            case 5040002:
            case 2320000: // The Teleport Rock
            case 5041000: // VIP Teleport Rock
            case 5040000: // The Teleport Rock
            case 5040001: { // Teleport Coke
                //used = UseTeleRock(slea, c, itemId);
                break;
            }
            case 5450100: {
                if (c.getPlayer().getEventInstance() != null) {
                    c.getPlayer().dropMessage(5, "Not usable inside instance/events.");
                } else {
                    if (c.getPlayer().isAlive()) {
                        NPCScriptManager.getInstance().startNPC(c, 9010106, "warp");
                    } else {
                        c.getPlayer().dropMessage(5, "Not usable while dead.");
                    }
                }
                break;
            }
            case 5450200:
                if (c.getPlayer().getEventInstance() != null) {
                    c.getPlayer().dropMessage(5, "Not usable inside instance/events.");
                } else {
                    if (c.getPlayer().isAlive()) {
                        NPCScriptManager.getInstance().startNPC(c, 9010106, "pot");
                    } else {
                        c.getPlayer().dropMessage(5, "Not usable while dead.");
                    }
                }
                break;
            case 5052000: {
                if (c.getPlayer().getEventInstance() == null) {
                    NPCScriptManager.getInstance().start(c, 9400845);
                } else {
                    c.getPlayer().dropMessage(5, "Not usable inside instance/events.");
                }
                return;
            }
            case 5450005: {
                NPCScriptManager.getInstance().startNPC(c, 9010106, "boost");

                //c.getPlayer().setConversation(4);
                //c.getPlayer().getStorage().sendStorage(c, 1022005);
                break;
            }
            case 5200003: {
                if (MapleInventoryManipulator.removeById_Lock(c, GameConstants.getInventoryType(5200003), 5200003)) {
                    c.announce(CWvsContext.InfoPacket.getShowItemGain(5200003, (short) -1, true));//5060026
                    c.getPlayer().buff();
                } else {
                    c.getPlayer().dropMessage(5, "Error pls report this error msg.");
                }
                break;
            }
            case 5050000: { // AP Reset
                Map<MapleStat, Integer> statupdate = new EnumMap<MapleStat, Integer>(MapleStat.class);
                final int apto = GameConstants.GMS ? (int) slea.readLong() : slea.readInt();
                final int apfrom = GameConstants.GMS ? (int) slea.readLong() : slea.readInt();

                if (apto == apfrom) {
                    break; // Hack
                }
                final int job = c.getPlayer().getJob();
                final PlayerStats playerst = c.getPlayer().getStat();
                used = true;

                switch (apto) { // AP to
                    case 64: // str
                        if (playerst.getStr() >= Integer.MAX_VALUE) {
                            used = false;
                        }
                        break;
                    case 128: // dex
                        if (playerst.getDex() >= Integer.MAX_VALUE) {
                            used = false;
                        }
                        break;
                    case 256: // int
                        if (playerst.getInt() >= Integer.MAX_VALUE) {
                            used = false;
                        }
                        break;
                    case 512: // luk
                        if (playerst.getLuk() >= Integer.MAX_VALUE) {
                            used = false;
                        }
                        break;
                    case 2048: // hp
                        c.getPlayer().dropMessage(1, "Not usable here.");
                        used = false;
                        break;
                    case 8192: // mp
                        c.getPlayer().dropMessage(1, "Not usable here.");
                        used = false;
                        break;
                }
                switch (apfrom) { // AP to
                    case 64: // str
                        if (playerst.getStr() <= 4 || (c.getPlayer().getJob() % 1000 / 100 == 1 && playerst.getStr() <= 35)) {
                            used = false;
                        }
                        break;
                    case 128: // dex
                        if (playerst.getDex() <= 4 || (c.getPlayer().getJob() % 1000 / 100 == 3 && playerst.getDex() <= 25) || (c.getPlayer().getJob() % 1000 / 100 == 4 && playerst.getDex() <= 25) || (c.getPlayer().getJob() % 1000 / 100 == 5 && playerst.getDex() <= 20)) {
                            used = false;
                        }
                        break;
                    case 256: // int
                        if (playerst.getInt() <= 4 || (c.getPlayer().getJob() % 1000 / 100 == 2 && playerst.getInt() <= 20)) {
                            used = false;
                        }
                        break;
                    case 512: // luk
                        if (playerst.getLuk() <= 4) {
                            used = false;
                        }
                        break;
                    case 2048: // hp
                        if (/*playerst.getMaxMp() < ((c.getPlayer().getLevel() * 14) + 134) || */c.getPlayer().getHpApUsed() <= 0 || c.getPlayer().getHpApUsed() >= 10000) {
                            used = false;
                            c.getPlayer().dropMessage(1, "You need points in HP or MP in order to take points out.");
                        }
                        break;
                    case 8192: // mp
                        if (/*playerst.getMaxMp() < ((c.getPlayer().getLevel() * 14) + 134) || */c.getPlayer().getHpApUsed() <= 0 || c.getPlayer().getHpApUsed() >= 10000) {
                            used = false;
                            c.getPlayer().dropMessage(1, "You need points in HP or MP in order to take points out.");
                        }
                        break;
                }
                if (used) {
                    switch (apto) { // AP to
                        case 64: { // str
                            final int toSet = (int) (playerst.getStr() + 1);
                            playerst.setStr(toSet, c.getPlayer());
                            statupdate.put(MapleStat.STR, toSet);
                            break;
                        }
                        case 128: { // dex
                            final int toSet = (int) (playerst.getDex() + 1);
                            playerst.setDex(toSet, c.getPlayer());
                            statupdate.put(MapleStat.DEX, toSet);
                            break;
                        }
                        case 256: { // int
                            final int toSet = (int) (playerst.getInt() + 1);
                            playerst.setInt(toSet, c.getPlayer());
                            statupdate.put(MapleStat.INT, toSet);
                            break;
                        }
                        case 512: { // luk
                            final int toSet = (int) (playerst.getLuk() + 1);
                            playerst.setLuk(toSet, c.getPlayer());
                            statupdate.put(MapleStat.LUK, toSet);
                            break;
                        }
                        case 2048: // hp
                            c.getPlayer().dropMessage(1, "Not usable here.");
                            used = false;
                            break;

                        case 8192: // mp
                            c.getPlayer().dropMessage(1, "Not usable here.");
                            used = false;
                            break;
                    }
                    switch (apfrom) { // AP from
                        case 64: { // str
                            final int toSet = (int) (playerst.getStr() - 1);
                            playerst.setStr(toSet, c.getPlayer());
                            statupdate.put(MapleStat.STR, toSet);
                            break;
                        }
                        case 128: { // dex
                            final int toSet = (int) (playerst.getDex() - 1);
                            playerst.setDex(toSet, c.getPlayer());
                            statupdate.put(MapleStat.DEX, toSet);
                            break;
                        }
                        case 256: { // int
                            final int toSet = (int) (playerst.getInt() - 1);
                            playerst.setInt(toSet, c.getPlayer());
                            statupdate.put(MapleStat.INT, toSet);
                            break;
                        }
                        case 512: { // luk
                            final int toSet = (int) (playerst.getLuk() - 1);
                            playerst.setLuk(toSet, c.getPlayer());
                            statupdate.put(MapleStat.LUK, toSet);
                            break;
                        }
                        case 2048: // HP
                            c.getPlayer().dropMessage(1, "Not usable here.");
                            used = false;
                            break;
                        case 8192: // MP
                            c.getPlayer().dropMessage(1, "Not usable here.");
                            used = false;
                            break;
                    }
                    c.announce(CWvsContext.updatePlayerStats(statupdate, true, c.getPlayer()));
                }
                break;
            }
            case 5220083: {//starter pack
                used = true;
                for (Entry<Integer, StructFamiliar> f : MapleItemInformationProvider.getInstance().getFamiliars().entrySet()) {
                    if (f.getValue().itemid == 2870055 || f.getValue().itemid == 2871002 || f.getValue().itemid == 2870235 || f.getValue().itemid == 2870019) {
                        MonsterFamiliar mf = c.getPlayer().getFamiliars().get(f.getKey());
                        if (mf != null) {
                            if (mf.getVitality() >= 3) {
                                mf.setExpiry((long) Math.min(System.currentTimeMillis() + 90 * 24 * 60 * 60000L, mf.getExpiry() + 30 * 24 * 60 * 60000L));
                            } else {
                                mf.setVitality(mf.getVitality() + 1);
                                mf.setExpiry((long) (mf.getExpiry() + 30 * 24 * 60 * 60000L));
                            }
                        } else {
                            mf = new MonsterFamiliar(c.getPlayer().getId(), f.getKey(), (long) (System.currentTimeMillis() + 30 * 24 * 60 * 60000L));
                            c.getPlayer().getFamiliars().put(f.getKey(), mf);
                        }
                        c.announce(CField.registerFamiliar(mf));
                    }
                }
                break;
            }
            case 5220084: {//booster pack
                if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() < 3) {
                    c.getPlayer().dropMessage(5, "Make 3 USE space.");
                    break;
                }
                used = true;
                int[] familiars = new int[3];
                while (true) {
                    for (int i = 0; i < familiars.length; i++) {
                        if (familiars[i] > 0) {
                            continue;
                        }
                        for (Map.Entry<Integer, StructFamiliar> f : MapleItemInformationProvider.getInstance().getFamiliars().entrySet()) {
                            if (Randomizer.nextInt(500) == 0 && ((i < 2 && f.getValue().grade == 0 || (i == 2 && f.getValue().grade != 0)))) {
                                MapleInventoryManipulator.addById(c, f.getValue().itemid, (short) 1, "Booster Pack");
                                //c.announce(CField.getBoosterFamiliar(c.getPlayer().getId(), f.getKey(), 0));
                                familiars[i] = f.getValue().itemid;
                                break;
                            }
                        }
                    }
                    if (familiars[0] > 0 && familiars[1] > 0 && familiars[2] > 0) {
                        break;
                    }
                }
                c.announce(MTSCSPacket.getBoosterPack(familiars[0], familiars[1], familiars[2]));
                c.announce(MTSCSPacket.getBoosterPackClick());
                c.announce(MTSCSPacket.getBoosterPackReveal());
                break;
            }
            case 5050001: // SP Reset (1st job)
            case 5050002: // SP Reset (2nd job)
            case 5050003: // SP Reset (3rd job)
            case 5050004:  // SP Reset (4th job)
            case 5050005: //evan sp resets
            case 5050006:
            case 5050007:
            case 5050008:
            case 5050009: {
                if (itemId >= 5050005 && !GameConstants.isEvan(c.getPlayer().getJob())) {
                    c.getPlayer().dropMessage(1, "This reset is only for Evans.");
                    break;
                } //well i dont really care other than this o.o
                if (itemId < 5050005 && GameConstants.isEvan(c.getPlayer().getJob())) {
                    c.getPlayer().dropMessage(1, "This reset is only for non-Evans.");
                    break;
                } //well i dont really care other than this o.o
                int skill1 = slea.readInt();
                int skill2 = slea.readInt();
                for (int i : GameConstants.blockedSkills) {
                    if (skill1 == i) {
                        c.getPlayer().dropMessage(1, "You may not add this skill.");
                        return;
                    }
                }

                Skill skillSPTo = SkillFactory.getSkill(skill1);
                Skill skillSPFrom = SkillFactory.getSkill(skill2);

                if (skillSPTo.isBeginnerSkill() || skillSPFrom.isBeginnerSkill()) {
                    c.getPlayer().dropMessage(1, "You may not add beginner skills.");
                    break;
                }
                if (GameConstants.getSkillBookForSkill(skill1) != GameConstants.getSkillBookForSkill(skill2)) { //resistance evan
                    c.getPlayer().dropMessage(1, "You may not add different job skills.");
                    break;
                }
                //if (GameConstants.getJobNumber(skill1 / 10000) > GameConstants.getJobNumber(skill2 / 10000)) { //putting 3rd job skillpoints into 4th job for example
                //    c.getPlayer().dropMessage(1, "You may not add skillpoints to a higher job.");
                //    break;
                //}
                if ((c.getPlayer().getSkillLevel(skillSPTo) + 1 <= skillSPTo.getMaxLevel()) && c.getPlayer().getSkillLevel(skillSPFrom) > 0 && skillSPTo.canBeLearnedBy(c.getPlayer().getJob())) {
                    if (skillSPTo.isFourthJob() && (c.getPlayer().getSkillLevel(skillSPTo) + 1 > c.getPlayer().getMasterLevel(skillSPTo))) {
                        c.getPlayer().dropMessage(1, "You will exceed the master level.");
                        break;
                    }
                    if (itemId >= 5050005) {
                        if (GameConstants.getSkillBookForSkill(skill1) != (itemId - 5050005) * 2 && GameConstants.getSkillBookForSkill(skill1) != (itemId - 5050005) * 2 + 1) {
                            c.getPlayer().dropMessage(1, "You may not add this job SP using this reset.");
                            break;
                        }
                    } else {
                        int theJob = GameConstants.getJobNumber(skill2 / 10000);
                        switch (skill2 / 10000) {
                            case 430:
                                theJob = 1;
                                break;
                            case 432:
                            case 431:
                                theJob = 2;
                                break;
                            case 433:
                                theJob = 3;
                                break;
                            case 434:
                                theJob = 4;
                                break;
                        }
                        if (theJob != itemId - 5050000) { //you may only subtract from the skill if the ID matches Sp reset
                            c.getPlayer().dropMessage(1, "You may not subtract from this skill. Use the appropriate SP reset.");
                            break;
                        }
                    }
                    final Map<Skill, SkillEntry> sa = new HashMap<>();
                    sa.put(skillSPFrom, new SkillEntry((byte) (c.getPlayer().getSkillLevel(skillSPFrom) - 1), c.getPlayer().getMasterLevel(skillSPFrom), SkillFactory.getDefaultSExpiry(skillSPFrom)));
                    sa.put(skillSPTo, new SkillEntry((byte) (c.getPlayer().getSkillLevel(skillSPTo) + 1), c.getPlayer().getMasterLevel(skillSPTo), SkillFactory.getDefaultSExpiry(skillSPTo)));
                    c.getPlayer().changeSkillsLevel(sa);
                    used = true;
                }
                break;
            }
            case 5500000: { // Magic Hourglass 1 day
                final Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final int days = 1;
                if (item != null && !GameConstants.isAccessory(item.getItemId()) && item.getExpiration() > -1 && !ii.isCash(item.getItemId()) && System.currentTimeMillis() + (100 * 24 * 60 * 60 * 1000L) > item.getExpiration() + (days * 24 * 60 * 60 * 1000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getPlayer().getName().indexOf(z) != -1 || item.getOwner().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + (days * 24 * 60 * 60 * 1000));
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5500001: { // Magic Hourglass 7 day
                final Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final int days = 7;
                if (item != null && !GameConstants.isAccessory(item.getItemId()) && item.getExpiration() > -1 && !ii.isCash(item.getItemId()) && System.currentTimeMillis() + (100 * 24 * 60 * 60 * 1000L) > item.getExpiration() + (days * 24 * 60 * 60 * 1000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getPlayer().getName().indexOf(z) != -1 || item.getOwner().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + (days * 24 * 60 * 60 * 1000));
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5500002: { // Magic Hourglass 20 day
                final Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final int days = 20;
                if (item != null && !GameConstants.isAccessory(item.getItemId()) && item.getExpiration() > -1 && !ii.isCash(item.getItemId()) && System.currentTimeMillis() + (100 * 24 * 60 * 60 * 1000L) > item.getExpiration() + (days * 24 * 60 * 60 * 1000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getPlayer().getName().indexOf(z) != -1 || item.getOwner().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + (days * 24 * 60 * 60 * 1000));
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5500005: { // Magic Hourglass 50 day
                final Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final int days = 50;
                if (item != null && !GameConstants.isAccessory(item.getItemId()) && item.getExpiration() > -1 && !ii.isCash(item.getItemId()) && System.currentTimeMillis() + (100 * 24 * 60 * 60 * 1000L) > item.getExpiration() + (days * 24 * 60 * 60 * 1000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getPlayer().getName().indexOf(z) != -1 || item.getOwner().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + (days * 24 * 60 * 60 * 1000));
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5500006: { // Magic Hourglass 99 day
                final Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final int days = 99;
                if (item != null && !GameConstants.isAccessory(item.getItemId()) && item.getExpiration() > -1 && !ii.isCash(item.getItemId()) && System.currentTimeMillis() + (100 * 24 * 60 * 60 * 1000L) > item.getExpiration() + (days * 24 * 60 * 60 * 1000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getPlayer().getName().indexOf(z) != -1 || item.getOwner().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + (days * 24 * 60 * 60 * 1000));
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5060000: { // Item Tag
                final Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());

                if (item != null && item.getOwner().equals("")) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getPlayer().getName().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setOwner(c.getPlayer().getName());
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    }
                }
                break;
            }
            case 5680015: {
                if (c.getPlayer().getFatigue() > 0) {
                    c.getPlayer().setFatigue(0);
                    used = true;
                }
                break;
            }
            case 5534000: { //tims lab
                final Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readInt());
                if (item != null) {
                    final Equip eq = (Equip) item;
                    if (eq.getState() == 0) {
                        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, itemId));
                        c.announce(InventoryPacket.scrolledItem(toUse, item, false, true));
                        c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(5, "This item's Potential cannot be reset.");
                    }
                } else {
                    c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), false, itemId));
                }
                break;
            }
            case 5062000: { //miracle cube
                if (c.getPlayer().getLevel() < 50) {
                    c.getPlayer().dropMessage(1, "You may not use this until level 50.");
                } else {
                    final Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readInt());
                    if (item != null && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                        final Equip eq = (Equip) item;
                        if (eq.getState() >= 17 && eq.getState() != 20) {
                            eq.renewPotential(0);
                            c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, itemId));
                            c.announce(InventoryPacket.scrolledItem(toUse, item, false, true));
                            c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                            MapleInventoryManipulator.addById(c, 2430112, (short) 1, "Cube" + " on " + FileoutputUtil.CurrentReadable_Date());
                            used = true;
                        } else {
                            c.getPlayer().dropMessage(5, "This item's Potential cannot be reset.");
                        }
                    } else {
                        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), false, itemId));
                    }
                }
                break;
            }
            case 5062100:
            case 5062001:
                break;
            case 5062002: { //super miracle cube
                final Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readInt());
                if (item != null) {
                    final Equip eq = (Equip) item;
                    if (eq.getPotential1() != 0) {
                        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, itemId));
                        c.announce(InventoryPacket.scrolledItem(toUse, item, false, true));
                        c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(5, "This item has no Potential");
                    }
                } else {
                    c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), false, itemId));
                }
                break;
            }
            case 5062003: { //premium cube
                break;
            }
            case 5062006: { //pltium cube
                final Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readInt());
                if (item != null) {
                    final Equip eq = (Equip) item;
                    if (eq.getPotential1() != 0) {
                        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, itemId));
                        c.announce(InventoryPacket.scrolledItem(toUse, item, false, true));
                        c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(5, "This item has no Potential");
                    }
                } else {
                    c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), false, itemId));
                }
                break;
            }
            case 5750000: { //alien cube
                final Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readInt());
                if (item != null) {
                    final Equip eq = (Equip) item;
                    if (eq.getPotential1() != 0) {
                        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, itemId));
                        c.announce(InventoryPacket.scrolledItem(toUse, item, false, true));
                        c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(5, "This item has no Potential");
                    }
                } else {
                    c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), false, itemId));
                }
                break;
            }
            case 5750001: { // socket diffuser
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(1, "You may not use this until level 10.");
                } else {
                    final Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readInt());
                    if (item != null) {
                        final Equip eq = (Equip) item;
                        if (eq.getSocket1() > 0) { // first slot only.
                            eq.setSocket1(0);
                            c.announce(InventoryPacket.scrolledItem(toUse, item, false, true));
                            c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                            used = true;
                        } else {
                            c.getPlayer().dropMessage(5, "This item do not have a socket.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "This item's nebulite cannot be removed.");
                    }
                }
                break;
            }
            case 5521000: { // Karma
                final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                final Item item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());

                if (item != null && !ItemFlag.KARMA_ACC.check(item.getFlag()) && !ItemFlag.KARMA_ACC_USE.check(item.getFlag())) {
                    if (MapleItemInformationProvider.getInstance().isShareTagEnabled(item.getItemId())) {
                        short flag = item.getFlag();
                        if (ItemFlag.UNTRADEABLE.check(flag)) {
                            flag -= ItemFlag.UNTRADEABLE.getValue();
                        } else if (type == MapleInventoryType.EQUIP) {
                            flag |= ItemFlag.KARMA_ACC.getValue();
                        } else {
                            flag |= ItemFlag.KARMA_ACC_USE.getValue();
                        }
                        item.setFlag(flag);
                        c.getPlayer().forceReAddItem_NoUpdate(item, type);
                        c.announce(InventoryPacket.updateSpecialItemUse(item, type.getType(), item.getPosition(), true, c.getPlayer()));
                        used = true;
                    }
                }
                break;
            }
            case 5520001: //p.karma
            case 5520000: { // Karma
                final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                final Item item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());

                if (item != null && !ItemFlag.KARMA_EQ.check(item.getFlag()) && !ItemFlag.KARMA_USE.check(item.getFlag())) {
                    if ((itemId == 5520000 && MapleItemInformationProvider.getInstance().isKarmaEnabled(item.getItemId())) || (itemId == 5520001 && MapleItemInformationProvider.getInstance().isPKarmaEnabled(item.getItemId()))) {
                        short flag = item.getFlag();
                        if (ItemFlag.UNTRADEABLE.check(flag)) {
                            flag -= ItemFlag.UNTRADEABLE.getValue();
                        } else if (type == MapleInventoryType.EQUIP) {
                            flag |= ItemFlag.KARMA_EQ.getValue();
                        } else {
                            flag |= ItemFlag.KARMA_USE.getValue();
                        }
                        item.setFlag(flag);
                        c.getPlayer().forceReAddItem_NoUpdate(item, type);
                        c.announce(InventoryPacket.updateSpecialItemUse(item, type.getType(), item.getPosition(), true, c.getPlayer()));
                        used = true;
                    }
                }
                break;
            }
            case 5570000: { // Vicious Hammer
                slea.readInt(); // Inventory type, Hammered eq is always EQ.
                final Equip item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) slea.readInt());
                // another int here, D3 49 DC 00
                if (item != null) {
                    if (GameConstants.canHammer(item.getItemId()) && MapleItemInformationProvider.getInstance().getSlots(item.getItemId()) > 0 && item.getViciousHammer() < 2) {
                        item.setViciousHammer((byte) (item.getViciousHammer() + 1));
                        item.setUpgradeSlots((short) (item.getUpgradeSlots() + 1));
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIP);
                        c.announce(MTSCSPacket.ViciousHammer(true, (byte) item.getViciousHammer()));
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(5, "You may not use it on this item.");
                        c.announce(MTSCSPacket.ViciousHammer(true, (byte) 0));
                    }
                }

                break;
            }
            case 5610001:
            case 5610000: { // Vega 30
                slea.readInt(); // Inventory type, always eq
                final short dst = (short) slea.readInt();
                slea.readInt(); // Inventory type, always use
                final short src = (short) slea.readInt();
                used = UseUpgradeScroll(src, dst, (short) 2, c, c.getPlayer(), itemId, false); //cannot use ws with vega but we dont care
                cc = used;
                break;
            }
            case 5060001: { // Sealing Lock
                final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                final Item item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getExpiration() == -1) {
                    short flag = item.getFlag();
                    flag |= ItemFlag.LOCK.getValue();
                    item.setFlag(flag);

                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5061000: { // Sealing Lock 7 days
                final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                final Item item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getExpiration() == -1) {
                    short flag = item.getFlag();
                    flag |= ItemFlag.LOCK.getValue();
                    item.setFlag(flag);
                    item.setExpiration(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000));

                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5061001: { // Sealing Lock 30 days
                final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                final Item item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getExpiration() == -1) {
                    short flag = item.getFlag();
                    flag |= ItemFlag.LOCK.getValue();
                    item.setFlag(flag);
                    item.setExpiration(System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000));
                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5061002: { // Sealing Lock 90 days
                final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                final Item item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getExpiration() == -1) {
                    short flag = item.getFlag();
                    flag |= ItemFlag.LOCK.getValue();
                    item.setFlag(flag);

                    item.setExpiration(System.currentTimeMillis() + (90 * 24 * 60 * 60 * 1000));

                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5061003: { // Sealing Lock 365 days
                final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                final Item item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getExpiration() == -1) {
                    short flag = item.getFlag();
                    flag |= ItemFlag.LOCK.getValue();
                    item.setFlag(flag);

                    item.setExpiration(System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000));

                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5063000: {
                final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                final Item item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getType() == 1) { //equip
                    short flag = item.getFlag();
                    flag |= ItemFlag.LUCKS_KEY.getValue();
                    item.setFlag(flag);
                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5064000: {
                System.out.println("slea..." + slea.toString());
                final MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                final Item item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
                // another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
                if (item != null && item.getType() == 1) { //equip
                    if (((Equip) item).getEnhance() >= 8) {
                        break; //cannot be used
                    }
                    short flag = item.getFlag();
                    flag |= ItemFlag.SHIELD_WARD.getValue();
                    item.setFlag(flag);

                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5060004:
            case 5060003: {//peanut
                Item item = c.getPlayer().getInventory(MapleInventoryType.ETC).findById(itemId == 5060003 ? 4170023 : 4170024);
                if (item == null || item.getQuantity() <= 0) { // hacking{
                    return;
                }
                if (getIncubatedItems(c, itemId)) {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, item.getPosition(), (short) 1, false);
                    used = true;
                }
                break;
            }

            case 5070000: { // Megaphone
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getPlayer().getMapId() == GameConstants.JAIL) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = slea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getPlayer(), sb);
                    sb.append(c.getPlayer().getName());
                    sb.append(" : ");
                    sb.append(message);

                    c.getPlayer().getMap().broadcastMessage(CWvsContext.serverNotice(2, sb.toString()));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5071000: { // Megaphone
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getPlayer().getMapId() == GameConstants.JAIL) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = slea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getPlayer(), sb);
                    sb.append(c.getPlayer().getName());
                    sb.append(" : ");
                    sb.append(message);

                    c.getChannelServer().broadcastSmegaPacket(CWvsContext.serverNotice(2, sb.toString()));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5077000: { // 3 line Megaphone
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getPlayer().getMapId() == GameConstants.JAIL) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final byte numLines = slea.readByte();
                    if (numLines > 3) {
                        return;
                    }
                    final List<String> messages = new LinkedList<String>();
                    String message;
                    for (int i = 0; i < numLines; i++) {
                        message = slea.readMapleAsciiString();
                        if (message.length() > 65) {
                            break;
                        }
                        messages.add(c.getPlayer().getName() + " : " + message);
                    }
                    final boolean ear = slea.readByte() > 0;

                    World.Broadcast.broadcastSmega(CWvsContext.tripleSmega(messages, ear, c.getChannel()));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5079004: { // Heart Megaphone
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getPlayer().getMapId() == GameConstants.JAIL) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = slea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    World.Broadcast.broadcastSmega(CWvsContext.echoMegaphone(c.getPlayer().getName(), message));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5073000: { // Heart Megaphone
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getPlayer().getMapId() == GameConstants.JAIL) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = slea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getPlayer(), sb);
                    sb.append(c.getPlayer().getName());
                    sb.append(" : ");
                    sb.append(message);

                    final boolean ear = slea.readByte() != 0;
                    World.Broadcast.broadcastSmega(CWvsContext.serverNotice(9, c.getChannel(), sb.toString(), ear));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5074000: { // Skull Megaphone
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getPlayer().getMapId() == GameConstants.JAIL) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = slea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getPlayer(), sb);
                    sb.append(c.getPlayer().getName());
                    sb.append(" : ");
                    sb.append(message);

                    final boolean ear = slea.readByte() != 0;

                    World.Broadcast.broadcastSmega(CWvsContext.serverNotice(22, c.getChannel(), sb.toString(), ear));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5072000: { // Super Megaphone
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getPlayer().getMapId() == GameConstants.JAIL) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = slea.readMapleAsciiString();

                    if (message.length() > 65) {
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getPlayer(), sb);
                    sb.append(c.getPlayer().getName());
                    sb.append(" : ");
                    sb.append(message);

                    final boolean ear = slea.readByte() != 0;

                    World.Broadcast.broadcastSmega(CWvsContext.serverNotice(3, c.getChannel(), sb.toString(), ear));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5076000: { // Item Megaphone
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getPlayer().getMapId() == GameConstants.JAIL) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final String message = GameConstants.convertText(slea.readMapleAsciiString());
                    if (message.length() > 65) {
                        c.getPlayer().dropMessage(5, "Message cannot be this long.");
                        break;
                    }
                    final StringBuilder sb = new StringBuilder();
                    addMedalString(c.getPlayer(), sb);
                    sb.append(c.getPlayer().getName());
                    sb.append(" : ");
                    sb.append(message);

                    final boolean ear = slea.readByte() > 0;

                    Item item = null;
                    if (slea.readByte() == 1) { //item
                        byte invType = (byte) slea.readInt();
                        byte pos = (byte) slea.readInt();
                        if (pos <= 0) {
                            invType = -1;
                        }
                        item = c.getPlayer().getInventory(MapleInventoryType.getByType(invType)).getItem(pos);
                    }
                    World.Broadcast.broadcastSmega(CWvsContext.itemMegaphone(sb.toString(), ear, c.getChannel(), item));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5075000: // MapleTV Messenger
            case 5075001: // MapleTV Star Messenger
            case 5075002: { // MapleTV Heart Messenger
                c.getPlayer().dropMessage(5, "There are no MapleTVs to broadcast the message to.");
                break;
            }
            case 5075003:
            case 5075004:
            case 5075005: {
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getPlayer().getMapId() == GameConstants.JAIL) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                    break;
                }
                int tvType = itemId % 10;
                if (tvType == 3) {
                    slea.readByte(); //who knows
                }
                boolean ear = tvType != 1 && tvType != 2 && slea.readByte() > 1; //for tvType 1/2, there is no byte. 
                MapleCharacter victim = tvType == 1 || tvType == 4 ? null : c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString()); //for tvType 4, there is no string.
                if (tvType == 0 || tvType == 3) { //doesn't allow two
                    victim = null;
                } else if (victim == null) {
                    c.getPlayer().dropMessage(1, "That character is not in the channel.");
                    break;
                }
                String message = slea.readMapleAsciiString();
                World.Broadcast.broadcastSmega(CWvsContext.serverNotice(3, c.getChannel(), c.getPlayer().getName() + " : " + message, ear));
                used = true;
                break;
            }
            case 5090100: // Wedding Invitation Card
            case 5090000: { // Note
                final String sendTo = slea.readMapleAsciiString();
                final String msg = slea.readMapleAsciiString();
                if (MapleCharacterUtil.canCreateChar(sendTo, false)) { // Name does not exist 
                    c.announce(MTSCSPacket.OnMemoResult((byte) 5, (byte) 1));
                } else {
                    int ch = World.Find.findChannel(sendTo);
                    if (ch <= 0) { // offline 
                        c.getPlayer().sendNote(sendTo, msg);
                        c.announce(MTSCSPacket.OnMemoResult((byte) 4, (byte) 0));
                        used = true;
                    } else {
                        c.announce(MTSCSPacket.OnMemoResult((byte) 5, (byte) 0));
                    }
                }
                break;
            }
            case 5100000: { // Congratulatory Song
                c.getPlayer().getMap().broadcastMessage(CField.musicChange("Jukebox/Congratulation"));
                used = true;
                break;
            }
            case 5190001:
            case 5190002:
            case 5190003:
            case 5190004:
            case 5190005:
            case 5190006:
            case 5190007:
            case 5190008:
            case 5190000: { // Pet Flags
                final int uniqueid = (int) slea.readLong();
                MaplePet pet = c.getPlayer().getPet(0);
                int slo = 0;

                if (pet == null) {
                    break;
                }
                if (pet.getUniqueId() != uniqueid) {
                    pet = c.getPlayer().getPet(1);
                    slo = 1;
                    if (pet != null) {
                        if (pet.getUniqueId() != uniqueid) {
                            pet = c.getPlayer().getPet(2);
                            slo = 2;
                            if (pet != null) {
                                if (pet.getUniqueId() != uniqueid) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
                PetFlag zz = PetFlag.getByAddId(itemId);
                if (zz != null && !zz.check(pet.getFlags())) {
                    pet.setFlags(pet.getFlags() | zz.getValue());
                    c.announce(PetPacket.updatePet(pet, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition()), true));
                    c.announce(CWvsContext.enableActions());
                    c.announce(MTSCSPacket.changePetFlag(uniqueid, true, zz.getValue()));
                    used = true;
                }
                break;
            }
            case 5191001:
            case 5191002:
            case 5191003:
            case 5191004:
            case 5191000: { // Pet Flags
                final int uniqueid = (int) slea.readLong();
                MaplePet pet = c.getPlayer().getPet(0);
                int slo = 0;

                if (pet == null) {
                    break;
                }
                if (pet.getUniqueId() != uniqueid) {
                    pet = c.getPlayer().getPet(1);
                    slo = 1;
                    if (pet != null) {
                        if (pet.getUniqueId() != uniqueid) {
                            pet = c.getPlayer().getPet(2);
                            slo = 2;
                            if (pet != null) {
                                if (pet.getUniqueId() != uniqueid) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
                PetFlag zz = PetFlag.getByDelId(itemId);
                if (zz != null && zz.check(pet.getFlags())) {
                    pet.setFlags(pet.getFlags() - zz.getValue());
                    c.announce(PetPacket.updatePet(pet, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition()), true));
                    c.announce(CWvsContext.enableActions());
                    c.announce(MTSCSPacket.changePetFlag(uniqueid, false, zz.getValue()));
                    used = true;
                }
                break;
            }
            case 5501001:
            case 5501002: { //expiry mount
                final Skill skil = SkillFactory.getSkill(slea.readInt());
                if (skil == null || skil.getId() / 10000 != 8000 || c.getPlayer().getSkillLevel(skil) <= 0 || !skil.isTimeLimited() || GameConstants.getMountItem(skil.getId(), c.getPlayer()) <= 0) {
                    break;
                }
                final long toAdd = (itemId == 5501001 ? 30 : 60) * 24 * 60 * 60 * 1000L;
                final long expire = c.getPlayer().getSkillExpiry(skil);
                if (expire < System.currentTimeMillis() || (long) (expire + toAdd) >= System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L)) {
                    break;
                }
                c.getPlayer().changeSingleSkillLevel(skil, c.getPlayer().getSkillLevel(skil), c.getPlayer().getMasterLevel(skil), (long) (expire + toAdd));
                used = true;
                break;
            }
            case 5170000: { // Pet name change
                try {
                    MaplePet pet = c.getPlayer().getPet(0);
                    String nName = slea.readMapleAsciiString();
                    if (pet != null && MapleCharacterUtil.canChangePetName(nName)) {
                        pet.setName(nName);
                        pet.saveToDb();
                        c.announce(PetPacket.updatePet(pet, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) pet.getInventoryPosition()), true));
                        c.announce(CWvsContext.enableActions());
                        c.getPlayer().getMap().broadcastMessage(MTSCSPacket.changePetName(c.getPlayer(), nName, 1));
                        used = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case 5700000: {
                slea.skip(8);
                if (c.getPlayer().getAndroid() == null) {
                    break;
                }
                String nName = slea.readMapleAsciiString();
                for (String z : GameConstants.RESERVED) {
                    if (c.getPlayer().getAndroid().getName().indexOf(z) != -1 || nName.indexOf(z) != -1) {
                        break;
                    }
                }
                if (MapleCharacterUtil.canChangePetName(nName)) {
                    c.getPlayer().getAndroid().setName(nName);
                    c.getPlayer().getMap().updateAndroid(c.getPlayer());
                    used = true;
                }
                break;
            }
            case 5240000:
            case 5240001:
            case 5240002:
            case 5240003:
            case 5240004:
            case 5240005:
            case 5240006:
            case 5240007:
            case 5240008:
            case 5240009:
            case 5240010:
            case 5240011:
            case 5240012:
            case 5240013:
            case 5240014:
            case 5240015:
            case 5240016:
            case 5240017:
            case 5240018:
            case 5240019:
            case 5240020:
            case 5240021:
            case 5240022:
            case 5240023:
            case 5240024:
            case 5240025:
            case 5240026:
            case 5240027:
            case 5240029:
            case 5240030:
            case 5240031:
            case 5240032:
            case 5240033:
            case 5240034:
            case 5240035:
            case 5240036:
            case 5240037:
            case 5240038:
            case 5240039:
            case 5240040:
            case 5240028: { // Pet food
                MaplePet pet = c.getPlayer().getPet(0);

                if (pet == null) {
                    break;
                }
                if (!pet.canConsume(itemId)) {
                    pet = c.getPlayer().getPet(1);
                    if (pet != null) {
                        if (!pet.canConsume(itemId)) {
                            pet = c.getPlayer().getPet(2);
                            if (pet != null) {
                                if (!pet.canConsume(itemId)) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
                final byte petindex = c.getPlayer().getPetIndex(pet);
                pet.setFullness(100);
                if (pet.getCloseness() < 30000) {
                    if (pet.getCloseness() + (100 * c.getChannelServer().getTraitRate()) > 30000) {
                        pet.setCloseness(30000);
                    } else {
                        pet.setCloseness(pet.getCloseness() + (100 * c.getChannelServer().getTraitRate()));
                    }
                    if (pet.getCloseness() >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                        pet.setLevel(pet.getLevel() + 1);
                        c.announce(EffectPacket.showOwnPetLevelUp(c.getPlayer().getPetIndex(pet)));
                        c.getPlayer().getMap().broadcastMessage(PetPacket.showPetLevelUp(c.getPlayer(), petindex));
                    }
                }
                c.announce(PetPacket.updatePet(pet, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), true));
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), PetPacket.commandResponse(c.getPlayer().getId(), (byte) 1, petindex, true, true), true);
                used = true;
                break;
            }
            case 5230001:
            case 5230000: {// owl of minerva
                final int itemSearch = slea.readInt();
                final List<HiredMerchant> hms = c.getChannelServer().searchMerchant(itemSearch);
                if (hms.size() > 0) {
                    c.announce(CWvsContext.getOwlSearched(itemSearch, hms));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(1, "Unable to find the item.");
                }
                break;
            }
            case 5281001: //idk, but probably
            case 5280001: // Gas Skill
            case 5281000: { // Passed gas
                Rectangle bounds = new Rectangle((int) c.getPlayer().getPosition().getX(), (int) c.getPlayer().getPosition().getY(), 1, 1);
                MapleMist mist = new MapleMist(bounds, c.getPlayer());
                c.getPlayer().getMap().spawnMist(mist, 10000, true);
                c.announce(CWvsContext.enableActions());
                used = true;
                break;
            }
            case 5370001:
            case 5370000: { // Chalkboard
                c.getPlayer().dropMessage("Temp disabled");
                /*
                for (MapleEventType t : MapleEventType.values()) {
                    final MapleEvent e = ChannelServer.getInstance(c.getChannel()).getEvent(t);
                    if (e.isRunning()) {
                        for (int i : e.getType().mapids) {
                            if (c.getPlayer().getMapId() == i) {
                                c.getPlayer().dropMessage(5, "You may not use that here.");
                                c.announce(CWvsContext.enableActions());
                                return;
                            }
                        }
                    }
                }
                c.getPlayer().setChalkboard(slea.readMapleAsciiString());
                 */
                break;
            }
            case 5079000:
            case 5079001:
            case 5390007:
            case 5390008:
            case 5390009:
            case 5390000: // Diablo Messenger
            case 5390001: // Cloud 9 Messenger
            case 5390002: // Loveholic Messenger
            case 5390003: // New Year Megassenger 1
            case 5390004: // New Year Megassenger 2
            case 5390005: // Cute Tiger Messenger
            case 5390006: { // Tiger Roar's Messenger
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                    break;
                }
                if (c.getPlayer().getMapId() == GameConstants.JAIL) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    final List<String> lines = new LinkedList<>();
                    for (int i = 0; i < 4; i++) {
                        final String text = slea.readMapleAsciiString();
                        if (text.length() > 55) {
                            continue;
                        }
                        lines.add(text);
                    }
                    final boolean ear = slea.readByte() != 0;
                    World.Broadcast.broadcastSmega(CWvsContext.getAvatarMega(c.getPlayer(), c.getChannel(), itemId, lines, ear));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            }
            case 5452001:
            case 5450003:
            case 5450000: { // Mu Mu the Travelling Merchant
                for (int i : GameConstants.blockedMaps) {
                    if (c.getPlayer().getMapId() == i) {
                        c.getPlayer().dropMessage(5, "You may not use this command here.");
                        c.announce(CWvsContext.enableActions());
                        return;
                    }
                }
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "You must be over level 10 to use this command.");
                } else if (c.getPlayer().hasBlockedInventory() || c.getPlayer().getMap().getSquadByMap() != null || c.getPlayer().getEventInstance() != null || c.getPlayer().getMap().getEMByMap() != null || c.getPlayer().getMapId() >= 990000000) {
                    c.getPlayer().dropMessage(5, "You may not use this command here.");
                } else if ((c.getPlayer().getMapId() >= 680000210 && c.getPlayer().getMapId() <= 680000502) || (c.getPlayer().getMapId() / 1000 == 980000 && c.getPlayer().getMapId() != 980000000) || (c.getPlayer().getMapId() / 100 == 1030008) || (c.getPlayer().getMapId() / 100 == 922010) || (c.getPlayer().getMapId() / 10 == 13003000)) {
                    c.getPlayer().dropMessage(5, "You may not use this command here.");
                } else {
                    MapleShopFactory.getInstance().getShop(61).sendShop(c);
                }
                //used = true;
                break;
            }
            case 5300000:
            case 5300001:
            case 5300002: { // Cash morphs
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                ii.getItemEffect(itemId).applyTo(c.getPlayer());
                used = true;
                break;
            }
            default:
                if (itemId / 10000 == 512) {
                    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    String msg = ii.getMsg(itemId);
                    final String ourMsg = slea.readMapleAsciiString();
                    if (!msg.contains("%s")) {
                        msg = ourMsg;
                    } else {
                        msg = msg.replaceFirst("%s", c.getPlayer().getName());
                        if (!msg.contains("%s")) {
                            msg = ii.getMsg(itemId).replaceFirst("%s", ourMsg);
                        } else {
                            try {
                                msg = msg.replaceFirst("%s", ourMsg);
                            } catch (Exception e) {
                                msg = ii.getMsg(itemId).replaceFirst("%s", ourMsg);
                            }
                        }
                    }
                    c.getPlayer().getMap().startMapEffect(msg, itemId);
                    used = true;
                } else if (itemId / 10000 == 510) {
                    c.getPlayer().getMap().startJukebox(c.getPlayer().getName(), itemId);
                    used = true;
                } else if (itemId / 10000 == 520) {
                    final int mesars = MapleItemInformationProvider.getInstance().getMeso(itemId);
                    if (mesars > 0 && c.getPlayer().getMeso() < (Integer.MAX_VALUE - mesars)) {
                        used = true;
                        if (Math.random() > 0.1) {
                            final int gainmes = Randomizer.nextInt(mesars);
                            c.getPlayer().gainMeso(gainmes, false);
                            c.announce(MTSCSPacket.sendMesobagSuccess(gainmes));
                        } else {
                            c.announce(MTSCSPacket.sendMesobagFailed(false)); // not random
                        }
                    }
                } else if (itemId / 10000 == 562) {
                    if (UseSkillBook(slot, itemId, c, c.getPlayer())) {
                        c.getPlayer().gainSP(1);
                    } //this should handle removing
                } else if (itemId / 10000 == 553) {
                    UseRewardItem(slot, itemId, c, c.getPlayer());// this too
                } else if (itemId / 10000 != 519) {
                    System.out.println("Unhandled CS item : " + itemId);
                    System.out.println(slea.toString(true));
                }
                break;
        }

        if (used) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, slot, (short) 1, false, true);
        }
        c.announce(CWvsContext.enableActions());
        if (cc) {
            if (!c.getPlayer().isAlive() || c.getPlayer().getEventInstance() != null || FieldLimitType.ChannelSwitch.check(c.getPlayer().getMap().getFieldLimit())) {
                c.getPlayer().dropMessage(1, "Auto relog failed.");
                return;
            }
            c.getPlayer().dropMessage(5, "Auto relogging. Please wait.");
            c.getPlayer().fakeRelog();
            if (c.getPlayer().getScrolledPosition() != 0) {
                c.announce(CWvsContext.pamSongUI());
            }
        }
    }

    public static boolean watchedItem(int id) {
        switch (id) {
            case 2049175:
            case 2049176:
            case 2049177:
            case 2049306:
            case 2049307:
            case 2583007:
            case 2583005:
            case 2585005:
            case 2586002:
            case 2586003:
            case 2586004:
            case 2340000:
            case 2587000:
            case 2587001:
            case 2430130:
            case 2430131:
            case 2000012:
            case 4310500:
            case 4310502:
            case 4310503:
            case 4420008:
            case 4420007:
            case 4420006:
            case 4420020:
            case 4420009:
                return true;
        }
        return false;
    }

    public static final boolean canHold(MapleClient c, final int itemid) {
        return c.getPlayer().getInventory(GameConstants.getInventoryType(itemid)).getNextFreeSlot() > -1;
    }

    public static final boolean canHold(MapleClient c, final int itemid, final int quantity) {
        return MapleInventoryManipulator.checkSpace(c, itemid, Randomizer.Max(quantity, Integer.MAX_VALUE), "");
    }

    public static final void Pickup_Player(final SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c == null || GameConstants.getLock()) {
            return;
        }

        MapleCharacter chr = c.getPlayer();
        if (chr == null || c.getPlayer().hasBlockedInventory()) { //hack
            return;
        }
        //Lock itemLock = chr.getMap().getItemLock();
        //itemLock.lock();
        //try {
        chr.updateTick(slea.readInt());
        c.getPlayer().setScrolledPosition((short) 0);
        slea.skip(1); // or is this before tick?
        final Point Client_Reportedpos = slea.readPos();
        if (chr.getMap() == null) {
            return;
        }
        MapleMapObject ob = chr.getMap().getMapObject(slea.readInt());

        if (ob == null || ob.getType() != MapleMapObjectType.ITEM) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        MapleMapItem mapitem = (MapleMapItem) ob;
        if (mapitem.isPickedUp()) {
            c.announce(CWvsContext.enableActions());
            return;
        } else {
            if (mapitem.getMeso() > 0) {
                removeItem(chr, mapitem);
                chr.gainMeso(mapitem.getMeso(), true);
            } else {
                if (mapitem.getItem().getQuantity() > 0) {
                    if (canHold(c, mapitem.getItemId(), mapitem.getItem().getQuantity())) {
                        if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItemId())) {
                            c.getSession().write(InventoryPacket.getInventoryFull());
                            c.getSession().write(InventoryPacket.getShowInventoryFull());
                            c.getSession().write(CWvsContext.enableActions());
                            return;
                        } else if (useItem(c, mapitem.getItemId())) {
                            //another hack
                            removeItem(c.getPlayer(), mapitem);
                            if (mapitem.getItemId() / 10000 == 291) {
                                c.getPlayer().getMap().broadcastMessage(CField.getCapturePosition(c.getPlayer().getMap()));
                                c.getPlayer().getMap().broadcastMessage(CField.resetCapture());
                            }
                        } else {
                            if (!MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true)) {
                                c.getSession().write(InventoryPacket.getInventoryFull());
                                c.getSession().write(InventoryPacket.getShowInventoryFull());
                            } else {
                                Item item = mapitem.getItem();

                                if (chr.getItemWarning(item.getItemId(), item.getQuantity())) {
                                    //System.out.println(c.getPlayer().getName() + " picked up " + item.getQuantity() + "x " + item.getItemId() + " - " + item.getItemName(item.getItemId()));
                                    mapitem.getItem().setGMLog("Picked up: " + item.getQuantity());
                                }
                                removeItem(chr, mapitem);
                            }
                        }
                    } else {
                        if (GameConstants.getInventoryType(mapitem.getItemId()) == MapleInventoryType.ETC) {
                            Item item = mapitem.getItem();
                            chr.addOverflow(item.getItemId(), item.getQuantity());
                            if (chr.getItemWarning(item.getItemId(), item.getQuantity())) {
                                //System.out.println(c.getPlayer().getName() + " picked up " + item.getQuantity() + "x " + item.getItemId() + " - " + item.getItemName(item.getItemId()));
                                mapitem.getItem().setGMLog("Picked up: " + item.getQuantity());
                            }
                            removeItem(chr, mapitem);
                        }
                    }
                } else {
                    System.out.println(c.getPlayer().getName() + " tried to picked up " + mapitem.getItem().getQuantity() + "x " + mapitem.getItem().getItemId() + " - " + mapitem.getItem().getItemName(mapitem.getItem().getItemId()));
                }
            }
        }
        //} finally {
        //    itemLock.unlock();
        //}
        c.announce(CWvsContext.enableActions());

    }

    public static final void Pickup_Pet(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        /*
         if (chr == null) {
         return;
         }
         if (c.getPlayer().hasBlockedInventory() || c.getPlayer().inPVP()) { //hack
         return;
         }
         c.getPlayer().setScrolledPosition((short) 0);
         final byte petz = (byte) (GameConstants.GMS ? (c.getPlayer().getPetIndex((int) slea.readLong())) : slea.readInt());
         final MaplePet pet = chr.getPet(petz);
         slea.skip(1); // [4] Zero, [4] Seems to be tickcount, [1] Always zero
         chr.updateTick(slea.readInt());
         final Point Client_Reportedpos = slea.readPos();
         final MapleMapObject ob = chr.getMap().getMapObject(slea.readInt());

         if (ob == null || pet == null) {
         return;
         }
         final MapleMapItem mapitem = (MapleMapItem) ob;
         final Lock lock = mapitem.getLock();
         lock.lock();
         try {
         if (mapitem.isPickedUp()) {
         c.announce(InventoryPacket.getInventoryFull());
         return;
         }
         if (mapitem.getOwner() != chr.getId() && mapitem.isPlayerDrop()) {
         return;
         }
         if (mapitem.getOwner() != chr.getId() && ((!mapitem.isPlayerDrop() && mapitem.getDropType() == 0) || (mapitem.isPlayerDrop() && chr.getMap().getEverlast()))) {
         c.announce(CWvsContext.enableActions());
         return;
         }
         if (!mapitem.isPlayerDrop() && mapitem.getDropType() == 1 && mapitem.getOwner() != chr.getId() && (chr.getParty() == null || chr.getParty().getMemberById(mapitem.getOwner()) == null)) {
         c.announce(CWvsContext.enableActions());
         return;
         }
         final double Distance = Client_Reportedpos.distanceSq(mapitem.getPosition());

         if (mapitem.getMeso() > 0) {
         if (chr.getParty() != null && mapitem.getOwner() != chr.getId()) {
         final List<MapleCharacter> toGive = new LinkedList<MapleCharacter>();
         final int splitMeso = mapitem.getMeso() * 40 / 100;
         for (MaplePartyCharacter z : chr.getParty().getMembers()) {
         MapleCharacter m = chr.getMap().getCharacterById(z.getId());
         if (m != null && m.getId() != chr.getId()) {
         toGive.add(m);
         }
         }
         for (final MapleCharacter m : toGive) {
         m.gainMeso(splitMeso / toGive.size() + (m.getStat().hasPartyBonus ? (int) (mapitem.getMeso() / 20.0) : 0), true);
         }
         chr.gainMeso(mapitem.getMeso() - splitMeso, true);
         } else {
         chr.gainMeso(mapitem.getMeso(), true);
         }
         removeItem_Pet(chr, mapitem, petz);
         } else {
         if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItemId()) || mapitem.getItemId() / 10000 == 291) {
         c.announce(CWvsContext.enableActions());
         } else if (useItem(c, mapitem.getItemId())) {
         removeItem_Pet(chr, mapitem, petz);
         } else if (MapleInventoryManipulator.checkSpace(c, mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
         if (mapitem.getItem().getQuantity() >= 50 && mapitem.getItemId() == 2340000) {
         c.setMonitored(true); //hack check
         }
         MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true, mapitem.getDropper() instanceof MapleMonster);
         removeItem_Pet(chr, mapitem, petz);
         }
         }
         } finally {
         lock.unlock();
         }
         */
    }

    public static final boolean useItem(final MapleClient c, final int id) {
        if (c == null || c.getPlayer() == null || !c.getPlayer().isAlive()) {
            return false;
        }
        if (GameConstants.isUse(id)) { // TO prevent caching of everything, waste of mem
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final MapleStatEffect eff = ii.getItemEffect(id);
            if (eff == null) {
                return false;
            }
            //must hack here for ctf
            if (id / 10000 == 291) {
                boolean area = false;
                for (Rectangle rect : c.getPlayer().getMap().getAreas()) {
                    if (rect.contains(c.getPlayer().getTruePosition())) {
                        area = true;
                        break;
                    }
                }
                if (!c.getPlayer().inPVP() || (c.getPlayer().getTeam() == (id - 2910000) && area)) {
                    return false; //dont apply the consume
                }
            }
            final int consumeval = eff.getConsume();

            if (consumeval > 0) {
                consumeItem(c, eff);
                consumeItem(c, ii.getItemEffectEX(id));
                c.announce(InfoPacket.getShowItemGain(id, (byte) 1));
                return true;
            }
        }
        return false;
    }

    public static final void consumeItem(final MapleClient c, final MapleStatEffect eff) {
        if (eff == null) {
            return;
        }
        if (eff.getConsume() == 2) {
            if (c.getPlayer().getParty() != null && c.getPlayer().isAlive()) {
                for (final MaplePartyCharacter pc : c.getPlayer().getParty().getMembers()) {
                    final MapleCharacter chr = c.getPlayer().getMap().getCharacterById(pc.getId());
                    if (chr != null && chr.isAlive()) {
                        eff.applyTo(chr);
                    }
                }
            } else {
                eff.applyTo(c.getPlayer());
            }
        } else if (c.getPlayer().isAlive()) {
            eff.applyTo(c.getPlayer());
        }
    }

    public static final void removeItem_Pet(final MapleCharacter chr, final MapleMapItem mapitem, int pet) {
        mapitem.setPickedUp(true);
        chr.getMap().broadcastMessage(CField.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), pet));
        chr.getMap().removeMapObject(mapitem, MapleMapObjectType.ITEM);
    }

    private static void removeItem(final MapleCharacter chr, final MapleMapItem mapitem) {
        mapitem.setPickedUp(true);
        chr.getMap().broadcastMessage(CField.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
        chr.getMap().removeMapObject(mapitem, MapleMapObjectType.ITEM);
        /*
         if (mapitem.isRandDrop()) {
         chr.getMap().spawnRandDrop();
         }
         */
    }

    public static void addMedalString(final MapleCharacter c, final StringBuilder sb) {
        final Item medal = c.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -46);
        if (medal != null) { // Medal
            sb.append("<");
            if (medal.getItemId() == 1142257 && GameConstants.isAdventurer(c.getJob())) {
                MapleQuestStatus stat = c.getQuestNoAdd(MapleQuest.getInstance(GameConstants.ULT_EXPLORER));
                if (stat != null && stat.getCustomData() != null) {
                    sb.append(stat.getCustomData());
                    sb.append("'s Successor");
                } else {
                    sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
                }
            } else {
                sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
            }
            sb.append("> ");
        }
    }

    private static boolean getIncubatedItems(MapleClient c, int itemId) {
        c.getPlayer().dropMessage(5, "disabled.");
        return false;
    }

    public static final void OwlMinerva(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        final byte slot = (byte) slea.readShort();
        final int itemid = slea.readInt();
        final Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemid && itemid == 2310000 && !c.getPlayer().hasBlockedInventory()) {
            final int itemSearch = slea.readInt();
            final List<HiredMerchant> hms = c.getChannelServer().searchMerchant(itemSearch);
            if (hms.size() > 0) {
                c.announce(CWvsContext.getOwlSearched(itemSearch, hms));
                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, true, false);
            } else {
                c.getPlayer().dropMessage(1, "Unable to find the item.");
            }
        }
        c.announce(CWvsContext.enableActions());
    }

    public static final void Owl(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c.getPlayer().haveItem(5230000, 1, true, false) || c.getPlayer().haveItem(2310000, 1, true, false)) {
            if (c.getPlayer().getMapId() >= 910000000 && c.getPlayer().getMapId() <= 910000022) {
                c.announce(CWvsContext.getOwlOpen());
            } else {
                c.getPlayer().dropMessage(5, "This can only be used inside the Free Market.");
                c.announce(CWvsContext.enableActions());
            }
        }
    }
    public static final int OWL_ID = 2; //don't change. 0 = owner ID, 1 = store ID, 2 = object ID

    public static final void OwlWarp(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.announce(CWvsContext.getOwlMessage(4));
            return;
        } else if (c.getPlayer().getTrade() != null) {
            c.announce(CWvsContext.getOwlMessage(7));
            return;
        }
        if (c.getPlayer().getMapId() >= 910000000 && c.getPlayer().getMapId() <= 910000022 && !c.getPlayer().hasBlockedInventory()) {
            final int id = slea.readInt();
            final int map = slea.readInt();
            if (map >= 910000001 && map <= 910000022) {
                c.announce(CWvsContext.getOwlMessage(0));
                final MapleMap mapp = c.getChannelServer().getMapFactory().getMap(map);
                c.getPlayer().changeMap(mapp, mapp.getPortal(0));
                HiredMerchant merchant = null;
                List<HiredMerchant> objects;
                switch (OWL_ID) {
                    case 0:
                        objects = mapp.getAllHiredMerchants();
                        for (MapleMapObject ob : objects) {
                            if (ob instanceof IMaplePlayerShop) {
                                final IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                                if (ips instanceof HiredMerchant) {
                                    final HiredMerchant merch = (HiredMerchant) ips;
                                    if (merch.getOwnerId() == id) {
                                        merchant = merch;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    case 1:
                        objects = mapp.getAllHiredMerchants();
                        for (MapleMapObject ob : objects) {
                            if (ob instanceof IMaplePlayerShop) {
                                final IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                                if (ips instanceof HiredMerchant) {
                                    final HiredMerchant merch = (HiredMerchant) ips;
                                    if (merch.getStoreId() == id) {
                                        merchant = merch;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        final MapleMapObject ob = mapp.getMapObject(id);
                        if ((HiredMerchant) ob instanceof IMaplePlayerShop) {
                            final IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                            if (ips instanceof HiredMerchant) {
                                merchant = (HiredMerchant) ips;
                            }
                        }
                        break;
                }
                if (merchant != null) {
                    c.getPlayer().dropMessage(1, "The room is already closed.");
                } else {
                    c.getPlayer().dropMessage(1, "The room is already closed.");
                }
            } else {
                c.announce(CWvsContext.getOwlMessage(23));
            }
        } else {
            c.announce(CWvsContext.getOwlMessage(23));
        }
    }

    public static final void PamSong(SeekableLittleEndianAccessor slea, MapleClient c) {
        final Item pam = c.getPlayer().getInventory(MapleInventoryType.CASH).findById(5640000);
        if (slea.readByte() > 0 && c.getPlayer().getScrolledPosition() != 0 && pam != null && pam.getQuantity() > 0) {
            final MapleInventoryType inv = c.getPlayer().getScrolledPosition() < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP;
            final Item item = c.getPlayer().getInventory(inv).getItem(c.getPlayer().getScrolledPosition());
            c.getPlayer().setScrolledPosition((short) 0);
            if (item != null) {
                final Equip eq = (Equip) item;
                eq.setUpgradeSlots((short) (eq.getUpgradeSlots() + 1));
                c.getPlayer().forceReAddItem_Flag(eq, inv);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, pam.getPosition(), (short) 1, true, false);
                c.getPlayer().getMap().broadcastMessage(CField.pamsSongEffect(c.getPlayer().getId()));
            }
        } else {
            c.getPlayer().setScrolledPosition((short) 0);
        }
    }

    public static final void TeleRock(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().isJailed()) {
            c.getPlayer().dropMessage(6, "You are currently jailed");
            return;
        }
        final byte slot = (byte) slea.readShort();
        final int itemId = slea.readInt();
        final Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);

        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || itemId / 10000 != 232 || c.getPlayer().hasBlockedInventory()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        boolean used = UseTeleRock(slea, c, itemId);
        if (used) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
        c.announce(CWvsContext.enableActions());
    }

    public static final boolean UseTeleRock(SeekableLittleEndianAccessor slea, MapleClient c, int itemId) {
        boolean used = false;
        if (c.getPlayer().isJailed()) {
            c.getPlayer().dropMessage(6, "You are currently jailed");
        }
        if (c.getPlayer().getEventInstance() != null) {
            c.getPlayer().dropMessage(1, "You cannot while inside an instance");
        }
        if (slea.readByte() == 0) { // Rocktype
            final MapleMap target = c.getChannelServer().getMapFactory().getMap(slea.readInt());
            if (target != null && ((itemId == 5041000 && c.getPlayer().isRockMap(target.getId())) || ((itemId == 5040000 || itemId == 5040001) && c.getPlayer().isRegRockMap(target.getId())) || ((itemId == 5040004 || itemId == 5041001) && (c.getPlayer().isHyperRockMap(target.getId()) || GameConstants.isHyperTeleMap(target.getId()))))) {
                if (!FieldLimitType.VipRock.check(target.getFieldLimit())) {
                    if (!c.getPlayer().isInBlockedMap()) {
                        c.getPlayer().changeMap(target, target.getPortal(0));
                    } else {
                        c.getPlayer().dropMessage(1, "You cannot go to a blocked place.");
                    }
                } else {
                    c.getPlayer().dropMessage(1, "You cannot go to a blocked place either.");
                }
            } else {
                c.getPlayer().dropMessage(1, "You cannot go to that place either.");
            }
        } else {
            c.getPlayer().dropMessage(1, "You cannot warp to players with this rock.");
        }
        return used;
    }
}
