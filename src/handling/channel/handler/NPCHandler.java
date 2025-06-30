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

import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.MapleClient;
import client.MapleCharacter;
import constants.GameConstants;
import client.MapleQuestStatus;
import client.RockPaperScissors;
import client.inventory.MapleInventory;
import constants.ServerConstants;
import handling.SendPacketOpcode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleShop;
import server.MapleInventoryManipulator;
import server.MapleStorage;
import server.life.MapleNPC;
import server.quest.MapleQuest;
import scripting.NPCScriptManager;
import scripting.NPCConversationManager;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import tools.packet.CField;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CField.NPCPacket;
import tools.packet.CWvsContext;

public class NPCHandler {

    public static final void NPCAnimation(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null || c.getPlayer() == null || c.getPlayer().getNPC()) {
            return;
        }
        if (c.getPlayer().isChangingMaps()) {
            return;
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_ACTION.getValue());
        final int length = (int) slea.available();
        mplew.writeInt(slea.readInt());
        if (length == 6 || length == 10) { // NPC Talk
            mplew.writeShort(slea.readShort());
            if (length == 10) {
                mplew.writeInt(slea.readInt());
            }
        } else if (length > 10) { // NPC Move
            mplew.write(slea.read(length - 9));
        } else {
            if (c.getPlayer() != null) {
                System.out.println(c.getPlayer().getName() + " triggered false error with npc. Length: " + length + " Packet: " + slea);
            }
            c.announce(CWvsContext.enableActions());
            return;
        }
        c.announce(mplew.getPacket());
    }

    public static final void NPCShop(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null) {
            return;
        }
        final MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        final byte bmode = slea.readByte();
        final MapleShop shop = chr.getShop();
        if (shop == null) {
            chr.dropMessage("Missing Shop");
            c.announce(CWvsContext.enableActions());
            return;
        }
        chr.open(true);
        // chr.dropMessage("SHop ID: " + shop.getId());
        switch (bmode) {
            case 0: {// buy
                final short slot = slea.readShort();
                final int itemId = slea.readInt();
                final short quantity = slea.readShort();
                if (itemId == shop.getItem(slot).getItemId()) {
                    shop.buy(c, slot, itemId, quantity);
                    if (!chr.update) {
                        chr.update = true;
                    }
                } else {
                    c.announce(CWvsContext.enableActions());
                }
                break;
            }
            case 1: {// sell
                final byte slot = (byte) slea.readShort();
                final int itemId = slea.readInt();
                final short quantity = slea.readShort();
                if (chr.haveItem(itemId, slot, quantity)) {
                    shop.sell(c, GameConstants.getInventoryType(itemId), slot, quantity);
                    if (!chr.update) {
                        chr.update = true;
                    }
                } else {
                    c.announce(CWvsContext.enableActions());
                }
                break;
            }
            case 2: {
                final byte slot = (byte) slea.readShort();
                shop.recharge(c, slot);
                break;
            }
            default:

                chr.setConversation(0);
                chr.open(false);
                chr.setShop(null);
                c.setChat(false);
                if (c.getCMS() != null) {
                    c.getCMS().dispose();
                }
                if (chr.update) {
                    chr.dropMessage(5, "Saving Char....");
                    chr.saveItems();
                    chr.update = false;
                    chr.dropMessage(1, "Save Complete.");
                }
                break;
        }
    }

    public static final void NPCTalk(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        // System.out.println("talk");
        if (c == null || GameConstants.getLock()) {
            return;
        }
        if (System.currentTimeMillis() <= c.getNpcCoolDown()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        if (c.getCMS() == null) {
            MapleCharacter chr = c.getPlayer();

            if (chr == null || chr.getMap() == null) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            if (chr.isStatLock()) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            chr.setStatLock(System.currentTimeMillis() + 100);
            if (c.getChat()) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            int oid = slea.readInt();
            final MapleNPC npc = chr.getMap().getNPCByOid(oid);

            if (npc == null) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            if (chr.hasBlockedInventory()) {
                c.announce(CWvsContext.enableActions());
                return;
            }
            // chr.dropMessage(5, "Talking to CMS " + c.getCMS());
            if (npc.hasShop()) {
                chr.setConversation(1);
                chr.dropMessage(5, "NPC ID: " + npc.getId() + " - NPC Shop: " + MapleShopFactory.getInstance().getShop(npc.getId()).getShopId());
                npc.sendShop(c);

            } else {
                chr.dropMessage(5, "Talking to NPC " + npc.getId());
                chr.setNpcOid(oid);
                NPCScriptManager.getInstance().start(c, npc.getId());
            }
            // chr.dropMessage(5, "Talking to CMS2 " + c.getCMS());
        }

    }

    public static final void NPCMoreTalk(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null || GameConstants.getLock()) {
            return;
        }
        final byte lastMsg = slea.readByte(); // 00 (last msg type I think)
        final byte action = slea.readByte(); // 00 = end chat, 01 == follow

        // todo legend
        /*
         * if (((lastMsg == 0x12 && c.getPlayer().getDirection() >= 0) || (lastMsg ==
         * 0x13 && c.getPlayer().getDirection() == -1)) && action == 1 &&
         * GameConstants.GMS) {
         * 
         * MapScriptMethods.startDirectionInfo(c.getPlayer(), lastMsg == 0x13); return;
         * }
         */
        //System.out.println("Lastmsg: " + lastMsg + " - action: " + action);
        final NPCConversationManager cm = c.getCMS();
        if (cm == null) {
            return;
        }
        if (((lastMsg == 2 || lastMsg == 5 || lastMsg == 9 || lastMsg == 17) && action == 0) || cm.getOk() || action == -1) {// selection END
            // CHAT
            //System.out.println("disposed");
            cm.dispose();
            c.announce(CWvsContext.enableActions());
            return;
        }

        if (c.getPlayer().isStorageOpened() || c.getPlayer().getShop() != null) {
            cm.dispose();
            return;
        }
        cm.setLastMsg((byte) -1);
        if (lastMsg == 3) {
            if (action != 0) {
                cm.setGetText(slea.readMapleAsciiString());
                if (!cm.getText().equals("")) {
                    NPCScriptManager.getInstance().action(c, action, lastMsg, -1);
                } else {
                    cm.dispose();
                }
            } else {
                cm.dispose();
            }
        } else {
            int selection = -1;
            if (slea.available() >= 4) {
                selection = slea.readInt();
            } else if (slea.available() > 0) {
                selection = slea.readByte();
            }
            if (lastMsg == 4 && selection == -1) {
                cm.dispose();
                return;// h4x
            }
            if (selection >= -1 && action != -1) {
                NPCScriptManager.getInstance().action(c, action, lastMsg, selection);
            } else {
                cm.dispose();
            }
        }
    }

    public static final void QuestAction(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        c.announce(CWvsContext.enableActions());
    }

    public static boolean itemCheck(int id) {
        switch (id) {
            case 2049300:
            case 2340000:
            case 2049185:
            case 2049186:
            case 4310501:
                return true;
            default:
                return false;
        }
    }

    public static boolean blocked(int id) {
        return id >= 4030000 && id < 4040000;
    }

    public static final void Storage(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null || GameConstants.getLock()) {
            return;
        }
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        if (!chr.checkPlayer()) {
            return;
        }
        if (chr.isStatLock()) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        chr.setStatLock(System.currentTimeMillis() + 100);
        final byte mode = slea.readByte();
        final MapleStorage storage = chr.getStorage();
        if (storage == null) {
            c.announce(CWvsContext.enableActions());
            return;
        }
        //System.out.println("mode: " + mode);
        switch (mode) {

            case 4: { // Take Out

                byte type = slea.readByte();
                byte slot = slea.readByte();
                //System.out.println("type: " + type + " - slot: " + slot);
                //slot = storage.getSlot(MapleInventoryType.getByType(type), slot);

                //final Item item = storage.takeOut(slot);
                if (c.getPlayer().canHold(storage.getItem(slot).getItemId())) {
                    Item item = storage.takeOut(slot);//actually the same but idc
                    if (item != null) {
                        int quantity = item.getQuantity();
                        item.setGMLog("withdrew from storage NPC");
                        if (MapleInventoryManipulator.addFromDrop(c, item, false)) {
                            storage.saveToDB(false);
                            chr.saveItems();
                            if (item.getQuantity() > 1) {
                                chr.dropMessage(1, item.getQuantity() + " " + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + "'s Successfully withdrawn.");
                            } else {
                                chr.dropMessage(1, MapleItemInformationProvider.getInstance().getName(item.getItemId()) + " Successfully withdrawn.");
                            }
                        } else {
                            storage.store(item, quantity);
                            storage.sendError(c, 10);
                            storage.update(c);
                            return;
                        }
                        c.getPlayer().setCoolDown(System.currentTimeMillis());
                    } else {
                        storage.sendError(c, 10);
                        return;
                    }
                } else {
                    storage.sendError(c, 10);
                    return;
                }
                storage.update(c);
                break;
            }
            case 5: { // Store
                final byte slot = (byte) slea.readShort();
                final int itemId = slea.readInt();
                MapleInventoryType type = GameConstants.getInventoryType(itemId);
                short quantity = slea.readShort();
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                MapleInventory inv = chr.getInventory(type);
                if (type == MapleInventoryType.EQUIP && quantity > 1) {
                    System.out.println("Possible dupe Equip Item amount gen with storage from " + chr.getName());
                    quantity = 1;
                }
                if (type != GameConstants.getInventoryType(itemId)) {
                    System.out.println("Possible dupe Item TYPE gen with storage from " + chr.getName());
                    storage.sendError(c, 10);
                    return;
                }
                if (quantity < 1) {
                    System.out.println("Possible Negative Item amount gen with storage from " + chr.getName());
                    storage.sendError(c, 10);
                    return;
                }
                if (storage.isFull()) {
                    c.announce(NPCPacket.getStorageFull());
                    return;
                }
                if (chr.getInventory(type).getItem(slot) == null) {
                    System.out.println("Item Null Error with storage from: Account: " + c.getAccountName() + " Char: " + c.getPlayer().getName());
                    storage.sendError(c, 10);
                    return;
                }
                if (!chr.haveItem(type, slot, quantity)) {
                    System.out.println("Possible Item gen with storage from: Account: " + c.getAccountName() + " Char: " + c.getPlayer().getName());
                    storage.sendError(c, 10);
                    return;
                }
                if (chr.getMeso() < 100) {
                    chr.dropMessage("You don't have enough mesos to store the item");
                    storage.sendError(c, 10);
                    return;
                }
                inv.lockInventory();
                try {
                    Item item = chr.getInventory(type).getItem(slot).copy();
                    // System.out.println("test: " + item.getInventoryId());
                    if (GameConstants.isPet(item.getItemId())) {
                        storage.sendError(c, 10);
                        return;
                    }

                    if (GameConstants.getInventoryType(itemId) != MapleInventoryType.EQUIP) {
                        chr.dropMessage("Storage only accepts Equips");
                        storage.sendError(c, 10);
                        c.announce(CWvsContext.enableActions());
                        return;
                    }

                    if (item.getItemId() == itemId && (item.getQuantity() >= quantity || GameConstants.isThrowingStar(itemId) || GameConstants.isBullet(itemId))) {
                        if (GameConstants.isThrowingStar(itemId) || GameConstants.isBullet(itemId)) {
                            quantity = item.getQuantity();
                        }
                        if (MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false)) {
                            chr.gainMeso(-100, false, false);
                            item.setQuantity(quantity);
                            storage.store(item, quantity);
                            chr.saveItems();
                            storage.saveToDB(false);
                            if (quantity > 1) {
                                chr.dropMessage(1, quantity + " " + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + "'s Successfully stored.");
                            } else {
                                chr.dropMessage(1, MapleItemInformationProvider.getInstance().getName(item.getItemId()) + " Successfully stored.");
                            }
                            //storage.sendStored(c, item.getInventoryType());
                            c.getPlayer().setCoolDown(System.currentTimeMillis());
                        } else {
                            System.out.println("Possible Item gen with storage from: Account: " + c.getAccountName() + " Char: " + c.getPlayer().getName());
                            storage.sendError(c, 10);
                        }
                    } else {
                        System.out.println("Possible Item gen with storage from " + chr.getName());
                        storage.sendError(c, 10);
                    }
                } finally {
                    inv.unlockInventory();
                }
                storage.update(c);
                break;
            }
            case 6: { // arrange
                storage.sendError(c, 10);
                return;
            }
            case 7: {
                int meso = slea.readInt();
                final int storageMesos = storage.getMeso();
                final int playerMesos = chr.getMeso();

                if ((meso > 0 && storageMesos >= meso) || (meso < 0 && playerMesos >= -meso)) {
                    if (meso < 0 && (storageMesos - meso) < 0) { // storing with overflow

                        meso = -(Integer.MAX_VALUE - storageMesos);
                        if ((-meso) > playerMesos) { // should never happen just a failsafe
                            storage.sendError(c, 10);
                            return;
                        }
                    } else if (meso > 0 && (playerMesos + meso) < 0) { // taking out with overflow
                        meso = (Integer.MAX_VALUE - playerMesos);
                        if ((meso) > storageMesos) { // should never happen just a failsafe
                            storage.sendError(c, 10);
                            return;
                        }
                    }
                    storage.setMeso(storageMesos - meso);
                    chr.gainMeso(meso, false, false);
                    storage.saveMesos();
                    chr.saveMesos();
                } else {
                    System.out.println("Possible Meso gen with storage from " + chr.getName());
                    storage.sendError(c, 10);
                    return;
                }
                storage.sendMeso(c);
                break;
            }
            case 8: {
                chr.dropMessage(1, "Save Complete!");
                chr.setConversation(0);
                storage.close();
                chr.storageOpen(false);
                c.setChat(false);
                if (c.getCMS() != null) {
                    c.getCMS().dispose();
                }
                chr.setStatLock(System.currentTimeMillis() + 100);
                break;
            }
            default:
                System.out.println("Unhandled Storage mode : " + mode);
                storage.sendError(c, 10);
                break;
        }

    }

    public static final void repairAll(final MapleClient c) {
        if (c.getPlayer().getMapId() != 240000000) {
            return;
        }
        Equip eq;
        double rPercentage;
        int price = 0;
        Map<String, Integer> eqStats;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Map<Equip, Integer> eqs = new HashMap<Equip, Integer>();
        final MapleInventoryType[] types = {MapleInventoryType.EQUIP, MapleInventoryType.EQUIPPED};
        for (MapleInventoryType type : types) {
            for (Item item : c.getPlayer().getInventory(type).newList()) {
                if (item instanceof Equip) { // redundant
                    eq = (Equip) item;
                    if (eq.getDurability() >= 0) {
                        eqStats = ii.getEquipStats(eq.getItemId());
                        if (eqStats.containsKey("durability") && eqStats.get("durability") > 0
                                && eq.getDurability() < eqStats.get("durability")) {
                            rPercentage = (100.0
                                    - Math.ceil((eq.getDurability() * 1000.0) / (eqStats.get("durability") * 10.0)));
                            eqs.put(eq, eqStats.get("durability"));
                            price += (int) Math.ceil(rPercentage * ii.getPrice(eq.getItemId())
                                    / (ii.getReqLevel(eq.getItemId()) < 70 ? 100.0 : 1.0));
                        }
                    }
                }
            }
        }
        if (eqs.size() <= 0 || c.getPlayer().getMeso() < price) {
            return;
        }
        c.getPlayer().gainMeso(-price, true);
        Equip ez;
        for (Entry<Equip, Integer> eqqz : eqs.entrySet()) {
            ez = eqqz.getKey();
            ez.setDurability(eqqz.getValue());
            c.getPlayer().forceReAddItem(ez.copy(),
                    ez.getPosition() < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP);
        }
    }

    public static final void repair(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c.getPlayer().getMapId() != 240000000 || slea.available() < 4) { // leafre for now
            return;
        }
        final int position = slea.readInt(); // who knows why this is a int
        final MapleInventoryType type = position < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP;
        final Item item = c.getPlayer().getInventory(type).getItem((byte) position);
        if (item == null) {
            return;
        }
        final Equip eq = (Equip) item;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Map<String, Integer> eqStats = ii.getEquipStats(item.getItemId());
        if (eq.getDurability() < 0 || !eqStats.containsKey("durability") || eqStats.get("durability") <= 0
                || eq.getDurability() >= eqStats.get("durability")) {
            return;
        }
        final double rPercentage = (100.0 - Math.ceil((eq.getDurability() * 1000.0) / (eqStats.get("durability") * 10.0)));
        // drpq level 105 weapons - ~420k per %; 2k per durability point
        // explorer level 30 weapons - ~10 mesos per %
        final int price = (int) Math.ceil(rPercentage * ii.getPrice(eq.getItemId()) / (ii.getReqLevel(eq.getItemId()) < 70 ? 100.0 : 1.0)); // /
        // 100
        // for
        // level
        // 30?
        // TODO: need more data on calculating off client
        if (c.getPlayer().getMeso() < price) {
            return;
        }
        c.getPlayer().gainMeso(-price, false);
        eq.setDurability(eqStats.get("durability"));
        c.getPlayer().forceReAddItem(eq.copy(), type);
    }

    public static final void UpdateQuest(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        final MapleQuest quest = MapleQuest.getInstance(slea.readShort());
        if (quest != null) {
            c.getPlayer().updateQuest(c.getPlayer().getQuest(quest), true);
        }
    }

    public static final void UseItemQuest(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        final short slot = slea.readShort();
        final int itemId = slea.readInt();
        final Item item = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem(slot);
        final int qid = slea.readInt();
        final MapleQuest quest = MapleQuest.getInstance(qid);
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Pair<Integer, List<Integer>> questItemInfo = null;
        boolean found = false;
        for (Item i : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
            if (i.getItemId() / 10000 == 422) {
                questItemInfo = ii.questItemInfo(i.getItemId());
                if (questItemInfo != null && questItemInfo.getLeft() == qid && questItemInfo.getRight() != null
                        && questItemInfo.getRight().contains(itemId)) {
                    found = true;
                    break; // i believe it's any order
                }
            }
        }
        if (quest != null && found && item != null && item.getQuantity() > 0 && item.getItemId() == itemId) {
            final int newData = slea.readInt();
            final MapleQuestStatus stats = c.getPlayer().getQuestNoAdd(quest);
            if (stats != null && stats.getStatus() == 1) {
                stats.setCustomData(String.valueOf(newData));
                c.getPlayer().updateQuest(stats, true);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, slot, (short) 1, false);
            }
        }
    }

    public static final void RPSGame(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (slea.available() == 0 || c.getPlayer() == null || c.getPlayer().getMap() == null
                || !c.getPlayer().getMap().containsNPC(9000019)) {
            if (c.getPlayer() != null && c.getPlayer().getRPS() != null) {
                c.getPlayer().getRPS().dispose(c);
            }
            return;
        }
        final byte mode = slea.readByte();
        switch (mode) {
            case 0: // start game
            case 5: // retry
                if (c.getPlayer().getRPS() != null) {
                    c.getPlayer().getRPS().reward(c);
                }
                if (c.getPlayer().getMeso() >= 1000) {
                    c.getPlayer().setRPS(new RockPaperScissors(c, mode));
                } else {
                    c.announce(CField.getRPSMode((byte) 0x08, -1, -1, -1));
                }
                break;
            case 1: // answer
                if (c.getPlayer().getRPS() == null || !c.getPlayer().getRPS().answer(c, slea.readByte())) {
                    c.announce(CField.getRPSMode((byte) 0x0D, -1, -1, -1));
                }
                break;
            case 2: // time over
                if (c.getPlayer().getRPS() == null || !c.getPlayer().getRPS().timeOut(c)) {
                    c.announce(CField.getRPSMode((byte) 0x0D, -1, -1, -1));
                }
                break;
            case 3: // continue
                if (c.getPlayer().getRPS() == null || !c.getPlayer().getRPS().nextRound(c)) {
                    c.announce(CField.getRPSMode((byte) 0x0D, -1, -1, -1));
                }
                break;
            case 4: // leave
                if (c.getPlayer().getRPS() != null) {
                    c.getPlayer().getRPS().dispose(c);
                } else {
                    c.announce(CField.getRPSMode((byte) 0x0D, -1, -1, -1));
                }
                break;
        }

    }

    public static final void OpenPublicNpc(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null || GameConstants.getLock()) {
            return;
        }
        final int npcid = slea.readInt();
        if (c.getPlayer().getMapId() >= 5000 && c.getPlayer().getMapId() <= 5005) {
            for (int i = 0; i < GameConstants.tutNpcIds.length; i++) {
                if (GameConstants.tutNpcIds[i] == npcid) { // for now
                    NPCScriptManager.getInstance().startNPC(c, npcid, null);
                    return;
                }
            }
        } else {
            if (c.getPlayer().hasBlockedInventory() || c.getPlayer().isInBlockedMap() || c.getPlayer().getLevel() < 10) {
                return;
            } else {
                for (int i = 0; i < GameConstants.publicNpcIds.length; i++) {
                    if (GameConstants.publicNpcIds[i] == npcid) { // for now
                        NPCScriptManager.getInstance().startNPC(c, npcid, null);
                        return;
                    }
                }
            }
        }
    }
}
