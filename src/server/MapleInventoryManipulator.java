package server;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import client.inventory.MapleInventoryIdentifier;
import constants.GameConstants;
import client.inventory.Equip;
import client.inventory.InventoryException;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.PlayerStats;
import client.MapleBuffStat;
import client.inventory.MaplePet;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import client.MapleTrait.MapleTraitType;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.MapleInventoryType;
import server.maps.AramiaFireWorks;
import tools.packet.MTSCSPacket;
import tools.StringUtil;
import client.inventory.EquipAdditions.RingSet;
import client.inventory.MapleAndroid;
import client.inventory.MapleInventory;
import client.inventory.MapleWeaponType;
import constants.EquipSlot;
import static handling.channel.handler.NPCHandler.itemCheck;
import java.util.HashMap;
import provider.MapleData;
import provider.MapleDataTool;
import server.quest.MapleQuest;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.InfoPacket;
import tools.packet.CWvsContext.InventoryPacket;

public class MapleInventoryManipulator {

    public static void addRing(MapleCharacter chr, int itemId, int ringId, int sn, String partner) {
        CashItemInfo csi = CashItemFactory.getInstance().getItem(sn);
        if (csi == null) {
            return;
        }
        Item ring = chr.getCashInventory().toItem(csi, ringId);
        if (ring == null || ring.getUniqueId() != ringId || ring.getUniqueId() <= 0 || ring.getItemId() != itemId) {
            return;
        }
        chr.getCashInventory().addToInventory(ring);
        chr.getClient().announce(MTSCSPacket.sendBoughtRings(GameConstants.isCrushRing(itemId), ring, sn, chr.getClient().getAccID(), partner));
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

    public static boolean addbyItem(final MapleClient c, final Item item) {
        return addbyItem(c, item, false) >= 0;
    }

    public static short addbyItem(final MapleClient c, final Item item, final boolean fromcs) {
        final MapleInventoryType type = GameConstants.getInventoryType(item.getItemId());
        final short newSlot = c.getPlayer().getInventory(type).addItem(item);
        if (newSlot == -1) {
            if (!fromcs) {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(InventoryPacket.getShowInventoryFull());
            }
            return newSlot;
        }
        if (GameConstants.isHarvesting(item.getItemId())) {
            c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
        }
        c.announce(InventoryPacket.addInventorySlot(type, item));
        c.getPlayer().havePartyQuest(item.getItemId());
        return newSlot;
    }

    public static int getUniqueId(int itemId, MaplePet pet) {
        int uniqueid = -1;
        if (GameConstants.isPet(itemId)) {
            if (pet != null) {
                uniqueid = pet.getUniqueId();
            } else {
                uniqueid = MapleInventoryIdentifier.getInstance();
            }
        }
        return uniqueid;
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String gmLog) {

        return addById(c, itemId, quantity, null, null, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, String gmLog) {
        return addById(c, itemId, quantity, owner, null, 0, gmLog);
    }

    public static byte addId(MapleClient c, int itemId, short quantity, String owner, String gmLog) {
        return addId(c, itemId, quantity, owner, null, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, MaplePet pet, String gmLog) {
        return addById(c, itemId, quantity, owner, pet, 0, gmLog);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, MaplePet pet, long period, String gmLog) {
        return addId(c, itemId, quantity, owner, pet, period, gmLog) >= 0;
    }

    public static byte addId(MapleClient c, int itemId, short quantity, String owner, MaplePet pet, long period, String gmLog) {
        if (c.getPlayer() != null) {

            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if ((ii.isPickupRestricted(itemId) && c.getPlayer().haveItem(itemId, 1, true, false)) || (!ii.itemExists(itemId))) {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(InventoryPacket.showItemUnavailable());
                return -1;
            }
            final MapleInventoryType type = GameConstants.getInventoryType(itemId);
            int uniqueid = getUniqueId(itemId, pet);
            short newSlot = -1;
            if (!type.equals(MapleInventoryType.EQUIP)) {
                final short slotMax = ii.getSlotMax(itemId);
                final List<Item> existing = c.getPlayer().getInventory(type).listById(itemId);
                if (!GameConstants.isRechargable(itemId)) {
                    if (!c.getPlayer().canHold(itemId, quantity)) {
                        c.getPlayer().dropMessage("Cannot hold anymore " + MapleItemInformationProvider.getInstance().getName(itemId));
                        return -1;
                    }
                    if (existing.size() > 0) { // first update all existing slots to slotMax
                        Iterator<Item> i = existing.iterator();
                        while (quantity > 0) {
                            if (i.hasNext()) {
                                Item eItem = (Item) i.next();
                                short oldQ = eItem.getQuantity();
                                if (oldQ < slotMax && eItem.getExpiration() == -1) {
                                    short newQ = (short) Math.min(oldQ + quantity, slotMax);
                                    quantity -= (newQ - oldQ);
                                    eItem.setQuantity(newQ);
                                    c.announce(InventoryPacket.updateInventorySlot(type, eItem, false));
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    Item nItem;
                    // add new slots if there is still something left
                    while (quantity > 0) {
                        short newQ = (short) Math.min(quantity, slotMax);
                        if (newQ != 0) {
                            quantity -= newQ;
                            nItem = new Item(itemId, (byte) 0, newQ, (byte) 0, uniqueid);
                            newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                            if (newSlot == -1) {
                                c.announce(InventoryPacket.getInventoryFull());
                                c.announce(InventoryPacket.getShowInventoryFull());
                                return -1;
                            }
                            if (gmLog != null) {
                                nItem.setGMLog(gmLog);
                            }
                            if (owner != null) {
                                nItem.setOwner(owner);
                            }
                            if (period > 0) {
                                nItem.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
                            }
                            switch (itemId) {//kaotic buff timer
                                case 2450024, 2450025, 2450050, 2450051, 2450052, 2450053 ->
                                    nItem.setExpiration(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
                            }
                            if (pet != null) {
                                nItem.setPet(pet);
                                pet.setInventoryPosition(newSlot);
                                nItem.setExpiration(Long.MAX_VALUE);
                                c.getPlayer().addPet(pet);
                            }
                            c.announce(InventoryPacket.addInventorySlot(type, nItem));
                            if (GameConstants.isRechargable(itemId) && quantity == 0) {
                                break;
                            }
                        } else {
                            c.getPlayer().havePartyQuest(itemId);
                            c.announce(CWvsContext.enableActions());
                            return (byte) newSlot;
                        }
                    }
                } else {
                    // Throwing Stars and Bullets - Add all into one slot regardless of quantity.
                    final Item nItem = new Item(itemId, (byte) 0, quantity, (byte) 0, uniqueid);
                    newSlot = c.getPlayer().getInventory(type).addItem(nItem);

                    if (newSlot == -1) {
                        c.announce(InventoryPacket.getInventoryFull());
                        c.announce(InventoryPacket.getShowInventoryFull());
                        return -1;
                    }
                    if (period > 0) {
                        nItem.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
                    }
                    if (gmLog != null) {
                        nItem.setGMLog(gmLog);
                    }
                    c.announce(InventoryPacket.addInventorySlot(type, nItem));
                    c.announce(CWvsContext.enableActions());
                }
            } else {
                if (quantity == 1) {
                    final Item nEquip = ii.getEquipById(itemId, uniqueid);
                    if (owner != null) {
                        nEquip.setOwner(owner);
                    }
                    if (gmLog != null) {
                        nEquip.setGMLog(gmLog);
                    }
                    if (period > 0) {
                        nEquip.setExpiration(System.currentTimeMillis() + (period * 24 * 60 * 60 * 1000));
                    }
                    newSlot = c.getPlayer().getInventory(type).addItem(nEquip);
                    if (newSlot == -1) {
                        c.announce(InventoryPacket.getInventoryFull());
                        c.announce(InventoryPacket.getShowInventoryFull());
                        return -1;
                    }
                    c.announce(InventoryPacket.addInventorySlot(type, nEquip));
                    if (GameConstants.isHarvesting(itemId)) {
                        c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
                    }
                } else {
                    throw new InventoryException("Trying to create equip with non-one quantity");
                }
            }
            c.getPlayer().havePartyQuest(itemId);
            return (byte) newSlot;
        }
        return -1;
    }

    public static Item addbyId_Gachapon(final MapleClient c, final int itemId, short quantity) {
        if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.USE).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.ETC).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.SETUP).getNextFreeSlot() == -1) {
            return null;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((ii.isPickupRestricted(itemId) && c.getPlayer().haveItem(itemId, 1, true, false)) || (!ii.itemExists(itemId))) {
            c.announce(InventoryPacket.getInventoryFull());
            c.announce(InventoryPacket.showItemUnavailable());
            return null;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemId);

        if (!type.equals(MapleInventoryType.EQUIP)) {
            short slotMax = ii.getSlotMax(itemId);
            final List<Item> existing = c.getPlayer().getInventory(type).listById(itemId);

            if (!GameConstants.isRechargable(itemId)) {
                Item nItem = null;
                boolean recieved = false;

                if (existing.size() > 0) { // first update all existing slots to slotMax
                    Iterator<Item> i = existing.iterator();
                    while (quantity > 0) {
                        if (i.hasNext()) {
                            nItem = (Item) i.next();
                            short oldQ = nItem.getQuantity();

                            if (oldQ < slotMax) {
                                recieved = true;

                                short newQ = (short) Math.min(oldQ + quantity, slotMax);
                                quantity -= (newQ - oldQ);
                                nItem.setQuantity(newQ);
                                c.announce(InventoryPacket.updateInventorySlot(type, nItem, false));
                            }
                        } else {
                            break;
                        }
                    }
                }
                // add new slots if there is still something left
                while (quantity > 0) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    if (newQ != 0) {
                        quantity -= newQ;
                        nItem = new Item(itemId, (byte) 0, newQ, (byte) 0);
                        final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                        if (newSlot == -1 && recieved) {
                            return nItem;
                        } else if (newSlot == -1) {
                            return null;
                        }
                        recieved = true;
                        c.announce(InventoryPacket.addInventorySlot(type, nItem));
                        if (GameConstants.isRechargable(itemId) && quantity == 0) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (recieved) {
                    c.getPlayer().havePartyQuest(nItem.getItemId());
                    return nItem;
                }
            } else {
                // Throwing Stars and Bullets - Add all into one slot regardless of quantity.
                final Item nItem = new Item(itemId, (byte) 0, quantity, (byte) 0);
                final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);

                if (newSlot == -1) {
                    return null;
                }
                c.announce(InventoryPacket.addInventorySlot(type, nItem));
                c.getPlayer().havePartyQuest(nItem.getItemId());
                return nItem;
            }
        } else {
            if (quantity == 1) {
                final Item item = ii.randomizeStats((Equip) ii.getEquipById(itemId));
                final short newSlot = c.getPlayer().getInventory(type).addItem(item);

                if (newSlot == -1) {
                    return null;
                }
                c.announce(InventoryPacket.addInventorySlot(type, item, true));
                c.getPlayer().havePartyQuest(item.getItemId());
                return item;
            } else {
                throw new InventoryException("Trying to create equip with non-one quantity");
            }
        }
        return null;
    }

    public static boolean addFromDropNoCheck(final MapleClient c, final Item item, final boolean show, final boolean check) {
        return addFromDrop(c, item, show, false, check);
    }

    public static boolean addFromDrop(MapleClient c, Item item) {
        return addFromDrop(c, item, true);
    }

    public static boolean addFromDrop(final MapleClient c, final Item item, final boolean show) {
        return addFromDrop(c, item, show, false);
    }

    public static boolean addFromDrop(final MapleClient c, final Item item, final boolean show, final boolean enhance) {
        return addFromDrop(c, item, show, enhance, true);
    }

    public static boolean addFromDrop(final MapleClient c, Item item, final boolean show, final boolean enhance, boolean check) {
        //Thread.dumpStack();
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        if (c.getPlayer() == null) {
            c.announce(InventoryPacket.getInventoryFull());
            c.announce(InventoryPacket.showItemUnavailable());
            return false;
        }
        if (item == null) {
            c.announce(InventoryPacket.getInventoryFull());
            c.announce(InventoryPacket.showItemUnavailable());
            return false;
        }
        if (check) {
            if (!ii.itemExists(item.getItemId())) {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(InventoryPacket.showItemUnavailable());
                return false;
            }
        }
        final int before = c.getPlayer().itemQuantity(item.getItemId());
        short quantity = item.getQuantity();
        final MapleInventoryType type = GameConstants.getInventoryType(item.getItemId());
        if (!type.equals(MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(item.getItemId());
            final List<Item> existing = c.getPlayer().getInventory(type).listById(item.getItemId());
            if (!GameConstants.isRechargable(item.getItemId())) {
                if (quantity <= 0) { //wth
                    c.announce(InventoryPacket.getInventoryFull());
                    c.announce(InventoryPacket.showItemUnavailable());
                    return false;
                }
                if (existing.size() > 0) { // first update all existing slots to slotMax
                    Iterator<Item> i = existing.iterator();
                    while (quantity > 0) {
                        if (i.hasNext()) {
                            final Item eItem = (Item) i.next();
                            final short oldQ = eItem.getQuantity();
                            if (oldQ < slotMax && item.getOwner().equals(eItem.getOwner()) && item.getExpiration() == eItem.getExpiration()) {
                                final short newQ = (short) Math.min(oldQ + quantity, slotMax);
                                quantity -= (newQ - oldQ);
                                eItem.setQuantity(newQ);
                                c.announce(InventoryPacket.updateInventorySlot(type, eItem, true));
                            }
                        } else {
                            break;
                        }
                    }
                }
                // add new slots if there is still something left
                while (quantity > 0) {
                    final short newQ = (short) Math.min(quantity, slotMax);
                    quantity -= newQ;
                    final Item nItem = new Item(item.getItemId(), (byte) 0, newQ, item.getFlag());
                    nItem.setExpiration(item.getExpiration());
                    nItem.setOwner(item.getOwner());
                    nItem.setPet(item.getPet());
                    nItem.setGMLog(item.getGMLog());
                    short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                    if (newSlot == -1) {
                        c.announce(InventoryPacket.getInventoryFull());
                        c.announce(InventoryPacket.getShowInventoryFull());
                        item.setQuantity((short) (quantity + newQ));
                        return false;
                    }
                    c.announce(InventoryPacket.addInventorySlot(type, nItem, true));
                }
            } else {
                // Throwing Stars and Bullets - Add all into one slot regardless of quantity.
                final Item nItem = new Item(item.getItemId(), (byte) 0, quantity, item.getFlag());
                nItem.setExpiration(item.getExpiration());
                nItem.setOwner(item.getOwner());
                nItem.setPet(item.getPet());
                nItem.setGMLog(item.getGMLog());
                final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                if (newSlot == -1) {
                    c.announce(InventoryPacket.getInventoryFull());
                    c.announce(InventoryPacket.getShowInventoryFull());
                    return false;
                }
                c.announce(InventoryPacket.addInventorySlot(type, nItem));
                c.getSession().write(CWvsContext.enableActions());
            }
            //c.getSession().write(CWvsContext.enableActions());
        } else {
            if (quantity == 1) {
                final short newSlot = c.getPlayer().getInventory(type).addItem(item);

                if (newSlot == -1) {
                    c.announce(InventoryPacket.getInventoryFull());
                    c.announce(InventoryPacket.getShowInventoryFull());
                    return false;
                }
                c.announce(InventoryPacket.addInventorySlot(type, item, true));
                if (GameConstants.isHarvesting(item.getItemId())) {
                    c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
                }
            } else {
                throw new RuntimeException("Trying to create equip with non-one quantity");
            }
        }
        /*
         if (before == 0) {
         switch (item.getItemId()) {
         case AramiaFireWorks.KEG_ID:
         c.getPlayer().dropMessage(5, "You have gained a Powder Keg, you can give this in to Aramia of Henesys.");
         break;
         case AramiaFireWorks.SUN_ID:
         c.getPlayer().dropMessage(5, "You have gained a Warm Sun, you can give this in to Maple Tree Hill through @joyce.");
         break;
         case AramiaFireWorks.DEC_ID:
         c.getPlayer().dropMessage(5, "You have gained a Tree Decoration, you can give this in to White Christmas Hill through @joyce.");
         break;
         }
         }
         */
        //c.getPlayer().havePartyQuest(item.getItemId());
        if (show) {
            c.announce(InfoPacket.getShowItemGain(item.getItemId(), item.getQuantity()));
        }

        return true;
    }

    public static boolean addFromMonster(final MapleClient c, Item item) {
        //System.out.println("itemid " + item.getItemId());
        if (c.getPlayer() == null) {
            return false;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!ii.itemExists(item.getItemId())) {
            return false;
        }
        //if ((ii.isPickupRestricted(item.getItemId()) && c.getPlayer().haveItem(item.getItemId(), 1, true, false)) || )) {
        //    return false;
        //}

        final int before = c.getPlayer().itemQuantity(item.getItemId());
        short quantity = item.getQuantity();
        final MapleInventoryType type = GameConstants.getInventoryType(item.getItemId());

        if (!type.equals(MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(item.getItemId());
            final List<Item> existing = c.getPlayer().getInventory(type).listById(item.getItemId());
            if (!GameConstants.isRechargable(item.getItemId())) {
                if (quantity <= 0) { //wth
                    return false;
                }
                if (existing.size() > 0) { // first update all existing slots to slotMax
                    Iterator<Item> i = existing.iterator();
                    while (quantity > 0) {
                        if (i.hasNext()) {
                            final Item eItem = (Item) i.next();
                            final short oldQ = eItem.getQuantity();
                            if (oldQ < slotMax && item.getFlag() == eItem.getFlag() && item.getOwner().equals(eItem.getOwner()) && item.getExpiration() == eItem.getExpiration()) {
                                final short newQ = (short) Math.min(oldQ + quantity, slotMax);
                                quantity -= (newQ - oldQ);
                                eItem.setQuantity(newQ);
                                item.setPosition(eItem.getPosition());
                                c.announce(InventoryPacket.updateInventorySlot(type, eItem, true));
                            }
                        } else {
                            break;
                        }
                    }
                }
                // add new slots if there is still something left
                while (quantity > 0) {
                    final short newQ = (short) Math.min(quantity, slotMax);
                    quantity -= newQ;
                    final Item nItem = new Item(item.getItemId(), (byte) 0, newQ, item.getFlag());
                    nItem.setExpiration(item.getExpiration());
                    nItem.setOwner(item.getOwner());
                    nItem.setPet(item.getPet());
                    nItem.setGMLog(item.getGMLog());
                    short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                    if (newSlot == -1) {
                        return false;
                    }
                    c.announce(InventoryPacket.addInventorySlot(type, nItem, true));
                }
            } else {
                // Throwing Stars and Bullets - Add all into one slot regardless of quantity.
                final Item nItem = new Item(item.getItemId(), (byte) 0, quantity, item.getFlag());
                nItem.setExpiration(item.getExpiration());
                nItem.setOwner(item.getOwner());
                nItem.setPet(item.getPet());
                nItem.setGMLog(item.getGMLog());
                final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                if (newSlot == -1) {
                    return false;
                }
                c.announce(InventoryPacket.addInventorySlot(type, nItem));
            }
        } else {
            if (quantity == 1) {
                final short newSlot = c.getPlayer().getInventory(type).addItem(item);

                if (newSlot == -1) {
                    c.announce(InventoryPacket.getInventoryFull());
                    c.announce(InventoryPacket.getShowInventoryFull());
                    c.announce(CWvsContext.enableActions());
                    return false;
                }
                c.announce(InventoryPacket.addInventorySlot(type, item, true));
                if (GameConstants.isHarvesting(item.getItemId())) {
                    c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
                }
            } else {
                throw new RuntimeException("Trying to create equip with non-one quantity");
            }
        }
        if (before == 0) {
            switch (item.getItemId()) {
                case AramiaFireWorks.KEG_ID:
                    c.getPlayer().dropMessage(5, "You have gained a Powder Keg, you can give this in to Aramia of Henesys.");
                    break;
                case AramiaFireWorks.SUN_ID:
                    c.getPlayer().dropMessage(5, "You have gained a Warm Sun, you can give this in to Maple Tree Hill through @joyce.");
                    break;
                case AramiaFireWorks.DEC_ID:
                    c.getPlayer().dropMessage(5, "You have gained a Tree Decoration, you can give this in to White Christmas Hill through @joyce.");
                    break;
            }
        }
        c.getPlayer().havePartyQuest(item.getItemId());
        return true;
    }

    public static boolean checkSpace(final MapleClient c, final int itemid, int quantity, final String owner) {
        if (c == null || c.getPlayer() == null) { //wtf is causing this?
            return false;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!ii.itemExists(itemid)) {
            c.announce(CWvsContext.enableActions());
            return false;
        }
        if (quantity <= 0 && !GameConstants.isRechargable(itemid)) {
            c.announce(CWvsContext.enableActions());
            return false;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        if (c.getPlayer().getInventory(type) == null) { //wtf is causing this?
            c.announce(CWvsContext.enableActions());
            return false;
        }
        if (!type.equals(MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(itemid);
            final List<Item> existing = c.getPlayer().getInventory(type).listById(itemid);
            if (!GameConstants.isRechargable(itemid)) {
                if (!existing.isEmpty()) { // first update all existing slots to slotMax
                    for (Item eItem : existing) {
                        final short oldQ = eItem.getQuantity();
                        if (oldQ < slotMax && owner != null && owner.equals(eItem.getOwner())) {
                            final short newQ = (short) Math.min(oldQ + quantity, slotMax);
                            quantity -= (newQ - oldQ);
                        }
                        if (quantity <= 0) {
                            break;
                        }
                    }
                }
            }
            // add new slots if there is still something left
            final int numSlotsNeeded;
            if (slotMax > 0 && !GameConstants.isRechargable(itemid)) {
                numSlotsNeeded = (int) (Math.ceil(((double) quantity) / slotMax));
            } else {
                numSlotsNeeded = 1;
            }
            return !c.getPlayer().getInventory(type).isFull(numSlotsNeeded - 1);
        } else {
            return !c.getPlayer().getInventory(type).isFull();
        }
    }

    public static boolean removeFromSlot(final MapleClient c, final MapleInventoryType type, final short slot, final short quantity, final boolean fromDrop) {
        return removeFromSlot(c, type, slot, quantity, fromDrop, false);
    }

    public static boolean removeFromSlot(final MapleClient c, final MapleInventoryType type, final short slot, short quantity, final boolean fromDrop, final boolean consume) {
        if (c.getPlayer() == null || c.getPlayer().getInventory(type) == null) {
            return false;
        }
        final Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item != null) {
            final boolean allowZero = consume && GameConstants.isRechargable(item.getItemId());
            c.getPlayer().getInventory(type).removeItem(slot, quantity, allowZero);
            if (GameConstants.isHarvesting(item.getItemId())) {
                c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
            }

            if (item.getQuantity() == 0 && !allowZero) {
                c.announce(InventoryPacket.clearInventoryItem(type, item.getPosition(), fromDrop));
            } else {
                c.announce(InventoryPacket.updateInventorySlot(type, (Item) item, fromDrop));
            }
            return true;
        }
        return false;
    }

    public static boolean removeById(final MapleClient c, final MapleInventoryType type, final int itemId, final int quantity, final boolean fromDrop, final boolean consume) {
        return removeByIdCount(c, type, itemId, quantity, fromDrop, consume) <= 0;
    }

    public static int removeByIdCount(final MapleClient c, final MapleInventoryType type, final int itemId, final int quantity, final boolean fromDrop, final boolean consume) {
        int remremove = quantity;
        if (c.getPlayer() == null || c.getPlayer().getInventory(type) == null) {
            return 0;
        }
        for (Item item : c.getPlayer().getInventory(type).listById(itemId)) {
            int theQ = item.getQuantity();
            if (remremove <= theQ && removeFromSlot(c, type, item.getPosition(), (short) remremove, fromDrop, consume)) {
                remremove = 0;
                break;
            } else if (remremove > theQ && removeFromSlot(c, type, item.getPosition(), item.getQuantity(), fromDrop, consume)) {
                remremove -= theQ;
            }
        }
        return remremove;
    }

    public static boolean removeFromSlot_Lock(final MapleClient c, final MapleInventoryType type, final short slot, short quantity, final boolean fromDrop, final boolean consume) {
        if (c.getPlayer() == null || c.getPlayer().getInventory(type) == null) {
            return false;
        }
        final Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item != null) {
            if (ItemFlag.LOCK.check(item.getFlag()) || ItemFlag.UNTRADEABLE.check(item.getFlag())) {
                return false;
            }
            return removeFromSlot(c, type, slot, quantity, fromDrop, consume);
        }
        return false;
    }

    public static boolean removeById_Lock(final MapleClient c, final MapleInventoryType type, final int itemId) {
        for (Item item : c.getPlayer().getInventory(type).listById(itemId)) {
            if (removeFromSlot_Lock(c, type, item.getPosition(), (short) 1, false, false)) {
                return true;
            }
        }
        return false;
    }

    public static void move(final MapleClient c, final MapleInventoryType type, final short src, final short dst) {
        if (src < 0 || dst < 0 || src == dst || type == MapleInventoryType.EQUIPPED) {
            return;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Item source = c.getPlayer().getInventory(type).getItem(src);
        final Item initialTarget = c.getPlayer().getInventory(type).getItem(dst);
        if (source == null) {
            return;
        }
        boolean bag = false, switchSrcDst = false, bothBag = false;
        short eqIndicator = -1;
        if (dst > c.getPlayer().getInventory(type).getSlotLimit()) {
            if (type == MapleInventoryType.ETC && dst > 100 && dst % 100 != 0) {
                final int eSlot = c.getPlayer().getExtendedSlot((dst / 100) - 1);
                if (eSlot > 0) {
                    final MapleStatEffect ee = ii.getItemEffect(eSlot);
                    if (dst % 100 > ee.getSlotCount() || ee.getType() != ii.getBagType(source.getItemId()) || ee.getType() <= 0) {
                        c.getPlayer().dropMessage(1, "You may not move that item to the bag.");
                        c.announce(CWvsContext.enableActions());
                        return;
                    } else {
                        eqIndicator = 0;
                        bag = true;
                    }
                } else {
                    c.getPlayer().dropMessage(1, "You may not move it to that bag.");
                    c.announce(CWvsContext.enableActions());
                    return;
                }
            } else {
                c.getPlayer().dropMessage(1, "You may not move it there.");
                c.announce(CWvsContext.enableActions());
                return;
            }
        }
        if (src > c.getPlayer().getInventory(type).getSlotLimit() && type == MapleInventoryType.ETC && src > 100 && src % 100 != 0) {
            //source should be not null so not much checks are needed
            if (!bag) {
                switchSrcDst = true;
                eqIndicator = 0;
                bag = true;
            } else {
                bothBag = true;
            }
        }
        short olddstQ = -1;
        if (initialTarget != null) {
            olddstQ = initialTarget.getQuantity();
        }
        final short oldsrcQ = source.getQuantity();
        final short slotMax = ii.getSlotMax(source.getItemId());
        c.getPlayer().getInventory(type).move(src, dst, slotMax);
        if (GameConstants.isHarvesting(source.getItemId())) {
            c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
        }
        if (!type.equals(MapleInventoryType.EQUIP) && initialTarget != null
                && initialTarget.getItemId() == source.getItemId()
                && initialTarget.getOwner().equals(source.getOwner())
                && initialTarget.getExpiration() == source.getExpiration()
                && !GameConstants.isRechargable(source.getItemId())
                && !type.equals(MapleInventoryType.CASH)) {
            if (GameConstants.isHarvesting(initialTarget.getItemId())) {
                c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
            }
            if ((olddstQ + oldsrcQ) > slotMax) {
                c.announce(InventoryPacket.moveAndMergeWithRestInventoryItem(type, src, dst, (short) ((olddstQ + oldsrcQ) - slotMax), slotMax, bag, switchSrcDst, bothBag));
            } else {
                c.announce(InventoryPacket.moveAndMergeInventoryItem(type, src, dst, ((Item) c.getPlayer().getInventory(type).getItem(dst)).getQuantity(), bag, switchSrcDst, bothBag));
            }
        } else {
            c.announce(InventoryPacket.moveInventoryItem(type, switchSrcDst ? dst : src, switchSrcDst ? src : dst, eqIndicator, bag, bothBag));
        }
    }

    public static void equip(final MapleClient c, final short src, short dst) {
        try {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final MapleCharacter chr = c.getPlayer();
            if (chr == null || (GameConstants.GMS && dst == -55)) {
                c.getPlayer().dropMessage("Test 1");
                return;
            }
            final PlayerStats statst = c.getPlayer().getStat();
            Equip source = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(src);
            if (source == null || !ii.canWearEquipment(chr, source, dst)) {
                c.getPlayer().dropMessage("Test 69: " + dst);
                c.announce(CWvsContext.enableActions());
                return;
            }
            //Equip target = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst);

            if (source.getDurability() == 0 || GameConstants.isHarvesting(source.getItemId())) {
                c.getPlayer().dropMessage("Test 2");
                c.announce(CWvsContext.enableActions());
                return;
            }
            /*
            if (ii.getReqLevel(source.getItemId()) > c.getPlayer().getTotalLevel()) {
                c.getPlayer().dropMessage(1, "Requires Base Level: " + ii.getReqLevel(source.getItemId()) + " to equip");
                c.announce(CWvsContext.enableActions());
                return;
            }
             */
 /*
            long rb = c.getPlayer().getReborns();
            if (source.getPower() > 250 && rb < 10) {
                c.getPlayer().dropMessage(1, "Requires 10 Reborns to equip");
                c.announce(CWvsContext.enableActions());
                return;
            }
            if (source.getPower() >= 500 && rb < 25) {
                c.getPlayer().dropMessage(1, "Requires 25 Reborns to equip");
                c.announce(CWvsContext.enableActions());
                return;
            }
            if (source.getPower() >= 750 && rb < 50) {
                c.getPlayer().dropMessage(1, "Requires 50 Reborns to equip");
                c.announce(CWvsContext.enableActions());
                return;
            }
            if (source.getPower() >= 900 && rb < 75) {
                c.getPlayer().dropMessage(1, "Requires 75 Reborns to equip");
                c.announce(CWvsContext.enableActions());
                return;
            }
            if (source.getPower() >= 999 && rb < 99) {
                c.getPlayer().dropMessage(1, "Requires 99 Reborns to equip");
                c.announce(CWvsContext.enableActions());
                return;
            }
             */
            final Map<String, Integer> stats = ii.getEquipStats(source.getItemId());

            if (stats == null) {
                c.getPlayer().dropMessage("Test 3");
                c.announce(CWvsContext.enableActions());
                return;
            }

            if (GameConstants.isWeapon(source.getItemId())) {
                if (!GameConstants.canWearEquip(c, source)) {
                    c.getPlayer().dropMessage(1, "Requires Job:\r\n " + GameConstants.getClassesCombination(stats.get("reqJob")));
                    c.announce(CWvsContext.enableActions());
                    return;
                }

                if (dst > -1200 && dst < -999 && !GameConstants.isEvanDragonItem(source.getItemId()) && !GameConstants.isMechanicItem(source.getItemId())) {
                    c.announce(CWvsContext.enableActions());
                    c.getPlayer().dropMessage("Test 4");
                    return;
                } else if ((dst <= -1200 || (dst >= -999 && dst < -99)) && !stats.containsKey("cash")) {
                    c.announce(CWvsContext.enableActions());
                    c.getPlayer().dropMessage("Test 5");
                    return;
                } else if (dst <= -1300 && c.getPlayer().getAndroid() == null) {
                    c.announce(CWvsContext.enableActions());
                    c.getPlayer().dropMessage("Test 6");
                    return;
                }
            }
            if (!ii.canEquip(stats, source.getItemId(), chr.getTotalLevel(), chr.getJob(), chr.getFame(), (int) (statst.getTotalStr()), (int) (statst.getTotalDex()), (int) (statst.getTotalLuk()), (int) (statst.getTotalInt()), c.getPlayer().getStat().levelBonus)) {
                c.announce(CWvsContext.enableActions());
                c.getPlayer().dropMessage("Test 7");
                return;
            }
            if (dst == -53 && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -54) == null) {
                c.announce(CWvsContext.enableActions());
                c.getPlayer().dropMessage("Requires Andriod Heart Equipped to enable andriod.");
                return;
            }
            if (GameConstants.isWeapon(source.getItemId())) {
                boolean check = false;
                if (dst == -10 || dst == -11 || dst == -111) {
                    check = true;
                }
                if (!check) {
                    c.announce(CWvsContext.enableActions());
                    c.getPlayer().dropMessage("Test 8: Slot: " + dst + "- Item ID: " + GameConstants.isWeapon(source.getItemId()));
                    return;
                }
            }
            if (dst == (GameConstants.GMS ? -18 : -23) && !GameConstants.isMountItemAvailable(source.getItemId(), c.getPlayer().getJob())) {
                c.announce(CWvsContext.enableActions());
                c.getPlayer().dropMessage("Test 9");
                return;
            }
            if (dst == (GameConstants.GMS ? -118 : -123) && source.getItemId() / 10000 != 190) {
                c.announce(CWvsContext.enableActions());
                c.getPlayer().dropMessage("Test 10");
                return;
            }
            if (dst == (GameConstants.GMS ? -59 : -55)) { //pendant
                MapleQuestStatus stat = c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT));
                if (stat == null || stat.getCustomData() == null || Long.parseLong(stat.getCustomData()) < System.currentTimeMillis()) {
                    c.announce(CWvsContext.enableActions());
                    c.getPlayer().dropMessage("Test 11");
                    return;
                }
            }
            if (GameConstants.isKatara(source.getItemId()) || source.getItemId() / 10000 == 135) {
                dst = (byte) -10; //shield slot
            }
            if (GameConstants.isEvanDragonItem(source.getItemId()) && (chr.getJob() < 2200 || chr.getJob() > 2218)) {
                c.announce(CWvsContext.enableActions());
                c.getPlayer().dropMessage("Test 12");
                return;
            }

            if (GameConstants.isMechanicItem(source.getItemId()) && (chr.getJob() < 3500 || chr.getJob() > 3512)) {
                c.announce(CWvsContext.enableActions());
                c.getPlayer().dropMessage("Test 13");
                return;
            }

            if (source.getItemId() / 1000 == 1112) { //ring
                for (RingSet s : RingSet.values()) {
                    if (s.id.contains(Integer.valueOf(source.getItemId()))) {
                        List<Integer> theList = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).listIds();
                        for (Integer i : s.id) {
                            if (theList.contains(i)) {
                                c.getPlayer().dropMessage(1, "You may not equip this item because you already have a " + (StringUtil.makeEnumHumanReadable(s.name())) + " equipped.");
                                c.announce(CWvsContext.enableActions());
                                return;
                            }
                        }
                    }
                }
            }
            switch (dst) {
                case -6: { // Top
                    final Item top = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -5);
                    if (top != null && GameConstants.isOverall(top.getItemId())) {
                        if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                            c.announce(InventoryPacket.getInventoryFull());
                            c.announce(InventoryPacket.getShowInventoryFull());
                            return;
                        }
                        unequip(c, (byte) -5, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                    }
                    break;
                }
                case -5: {
                    final Item bottom = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -6);
                    if (bottom != null && GameConstants.isOverall(source.getItemId())) {
                        if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                            c.announce(InventoryPacket.getInventoryFull());
                            c.announce(InventoryPacket.getShowInventoryFull());
                            return;
                        }
                        unequip(c, (byte) -6, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                    }
                    break;
                }
                case -10: { // Shield
                    Item weapon = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
                    if (GameConstants.isKatara(source.getItemId())) {
                        if ((chr.getJob() != 900 && (chr.getJob() < 430 || chr.getJob() > 434)) || weapon == null || !GameConstants.isDagger(weapon.getItemId())) {
                            c.announce(InventoryPacket.getInventoryFull());
                            c.announce(InventoryPacket.getShowInventoryFull());
                            return;
                        }
                    } else if (weapon != null && GameConstants.isTwoHanded(weapon.getItemId())) {
                        if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                            c.announce(InventoryPacket.getInventoryFull());
                            c.announce(InventoryPacket.getShowInventoryFull());
                            return;
                        }
                        unequip(c, (byte) -11, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                    }
                    break;
                }
                case -11: { // Weapon
                    Item shield = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
                    if (shield != null && GameConstants.isTwoHanded(source.getItemId())) {
                        if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                            c.announce(InventoryPacket.getInventoryFull());
                            c.announce(InventoryPacket.getShowInventoryFull());
                            return;
                        }
                        unequip(c, (byte) -10, chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                    }
                    break;
                }
            }
            source = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(src); // Equip
            Equip target = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst); // Currently equipping
            if (source == null) {
                c.announce(CWvsContext.enableActions());
                c.getPlayer().dropMessage("Test 14");
                return;
            }
            short flag = source.getFlag();
            if (stats.get("equipTradeBlock") != null || source.getItemId() / 10000 == 167) { // Block trade when equipped.
                if (!ItemFlag.UNTRADEABLE.check(flag)) {
                    flag |= ItemFlag.UNTRADEABLE.getValue();
                    source.setFlag(flag);
                    c.announce(InventoryPacket.updateSpecialItemUse_(source, MapleInventoryType.EQUIP.getType(), c.getPlayer()));
                }
            }
            if (source.getItemId() / 10000 == 166) {
                if (source.getAndroid() == null) {
                    final int uid = MapleInventoryIdentifier.getInstance();
                    source.setUniqueId(uid);
                    source.setAndroid(MapleAndroid.create(source.getItemId(), uid));
                    flag |= ItemFlag.LOCK.getValue();
                    flag |= ItemFlag.UNTRADEABLE.getValue();
                    flag |= ItemFlag.ANDROID_ACTIVATED.getValue();
                    source.setFlag(flag);
                    c.announce(InventoryPacket.updateSpecialItemUse_(source, MapleInventoryType.EQUIP.getType(), c.getPlayer()));
                }
                chr.removeAndroid();
                chr.setAndroid(source.getAndroid());
            } else if (dst <= -1300 && chr.getAndroid() != null) {
                chr.setAndroid(chr.getAndroid()); //respawn it
            }
            if (source.getCharmEXP() > 0 && !ItemFlag.CHARM_EQUIPPED.check(flag)) {
                chr.getTrait(MapleTraitType.charm).addExp(source.getCharmEXP(), chr);
                source.setCharmEXP((short) 0);
                flag |= ItemFlag.CHARM_EQUIPPED.getValue();
                source.setFlag(flag);
                c.announce(InventoryPacket.updateSpecialItemUse_(source, GameConstants.getInventoryType(source.getItemId()).getType(), c.getPlayer()));
            }
            chr.getInventory(MapleInventoryType.EQUIP).removeSlot(src);
            if (target != null) {
                chr.getInventory(MapleInventoryType.EQUIPPED).removeSlot(dst);
                if (target.getState() >= 17) {
                    final Map<Skill, SkillEntry> ss = new HashMap<>();
                    int[] potentials = {target.getPotential1(), target.getPotential2(), target.getPotential3(), target.getPotential4(), target.getPotential5()};
                    for (int i : potentials) {
                        if (i > 0) {
                            int itemLevel = (int) ((double) ii.getReqLevel(target.getItemId()) / (double) 10);
                            StructItemOption pot = ii.getPotentialInfo(i).get(Randomizer.Min(itemLevel - 1, 0));

                            if (pot != null && pot.get("skillID") > 0) {
                                ss.put(SkillFactory.getSkill(PlayerStats.getSkillByJob(pot.get("skillID"), c.getPlayer().getJob())), new SkillEntry((byte) 0, (byte) 0, -1));
                            }
                        }
                    }
                    c.getPlayer().changeSkillLevel_Skip(ss, true);
                }
                if (target.getSocketState() > 15) {
                    final Map<Skill, SkillEntry> ss = new HashMap<>();
                    int[] sockets = {target.getSocket1(), target.getSocket2(), target.getSocket3()};
                    for (int i : sockets) {
                        if (i > 0) {
                            StructItemOption soc = ii.getSocketInfo(i);
                            if (soc != null && soc.get("skillID") > 0) {
                                ss.put(SkillFactory.getSkill(PlayerStats.getSkillByJob(soc.get("skillID"), c.getPlayer().getJob())), new SkillEntry((byte) 1, (byte) 0, -1));
                            }
                        }
                    }
                    c.getPlayer().changeSkillLevel_Skip(ss, true);
                }
            }
            source.setPosition(dst);
            chr.getInventory(MapleInventoryType.EQUIPPED).addFromDB(source);
            if (target != null) {
                target.setPosition(src);
                chr.getInventory(MapleInventoryType.EQUIP).addFromDB(target);
            }
            if (GameConstants.isWeapon(source.getItemId())) {
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.BOOSTER);
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.SPIRIT_CLAW);
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.SOULARROW);
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.WK_CHARGE);
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.LIGHTNING_CHARGE);
            }
            if (source.getItemId() / 10000 == 190 || source.getItemId() / 10000 == 191) {
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.MECH_CHANGE);
            }
            if (GameConstants.isReverseItem(source.getItemId())) {
                chr.finishAchievement(9);
            }
            if (GameConstants.isTimelessItem(source.getItemId())) {
                chr.finishAchievement(10);
            }
            if (stats.containsKey("reqLevel") && stats.get("reqLevel") >= 140) {
                chr.finishAchievement(41);
            }
            if (stats.containsKey("reqLevel") && stats.get("reqLevel") >= 130) {
                chr.finishAchievement(40);
            }
            if (stats.containsKey("reqLevel") && stats.get("reqLevel") >= 160) {
                chr.finishAchievement(42);
            }
            if (stats.containsKey("reqLevel") && stats.get("reqLevel") >= 200) {
                chr.finishAchievement(43);
            }
            if (source.getItemId() == 1122017) {
                chr.startFairySchedule(true, true);
            }
            if (source.getState() >= 17) {
                final Map<Skill, SkillEntry> ss = new HashMap<>();
                int[] potentials = {source.getPotential1(), source.getPotential2(), source.getPotential3(), source.getPotential4(), source.getPotential5()};
                for (int i : potentials) {
                    if (i > 0) {
                        int itemLevel = (int) ((double) ii.getReqLevel(source.getItemId()) / (double) 10);
                        StructItemOption pot = ii.getPotentialInfo(i).get(Randomizer.Min(itemLevel - 1, 0));
                        if (pot != null && pot.get("skillID") > 0) {
                            ss.put(SkillFactory.getSkill(PlayerStats.getSkillByJob(pot.get("skillID"), c.getPlayer().getJob())), new SkillEntry((byte) 1, (byte) 0, -1));
                        }
                    }
                }
                c.getPlayer().changeSkillLevel_Skip(ss, true);
            }
            if (source.getSocketState() > 15) {
                final Map<Skill, SkillEntry> ss = new HashMap<>();
                int[] sockets = {source.getSocket1(), source.getSocket2(), source.getSocket3()};
                for (int i : sockets) {
                    if (i > 0) {
                        StructItemOption soc = ii.getSocketInfo(i);
                        if (soc != null && soc.get("skillID") > 0) {
                            ss.put(SkillFactory.getSkill(PlayerStats.getSkillByJob(soc.get("skillID"), c.getPlayer().getJob())), new SkillEntry((byte) 1, (byte) 0, -1));
                        }
                    }
                }
                c.getPlayer().changeSkillLevel_Skip(ss, true);
            }
            c.announce(InventoryPacket.moveInventoryItem(MapleInventoryType.EQUIP, src, dst, (byte) 2, false, false));
            chr.equipChanged();
            if (dst == -11) {
                c.getPlayer().weaponType = GameConstants.getWeaponType(source.getItemId()).getMaxDamageMultiplier();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unequip(final MapleClient c, final short src, final short dst) {
        try {
            MapleInventory equip = c.getPlayer().getInventory(MapleInventoryType.EQUIP);
            Equip source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(src);
            Equip target = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);

            if (dst < 0 || source == null || (GameConstants.GMS && src == -55)) {
                return;
            }
            if (target != null && src <= 0) { // do not allow switching with equip
                c.announce(InventoryPacket.getInventoryFull());
                return;
            }

            if (src == -54 && c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -53) != null) {
                if (equip.getNumFreeSlot() < 2) {
                    c.announce(InventoryPacket.getInventoryFull());
                    return;
                }
            }
            c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot(src);
            if (target != null) {
                c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(dst);
            }
            source.setPosition(dst);
            c.getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(source);
            if (target != null) {
                target.setPosition(src);
                c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(target);
            }

            if (GameConstants.isWeapon(source.getItemId())) {
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.BOOSTER);
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.SPIRIT_CLAW);
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.SOULARROW);
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.WK_CHARGE);
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.LIGHTNING_CHARGE);
            } else if (source.getItemId() / 10000 == 190 || source.getItemId() / 10000 == 191) {
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
                c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.MECH_CHANGE);
            } else if (source.getItemId() / 10000 == 166) {
                c.getPlayer().removeAndroid();
            } else if (src <= -1300 && c.getPlayer().getAndroid() != null) {
                c.getPlayer().setAndroid(c.getPlayer().getAndroid());
            } else if (source.getItemId() == 1122017) {
                c.getPlayer().cancelFairySchedule(true);
            }
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (source.getState() >= 17) {
                final Map<Skill, SkillEntry> ss = new HashMap<>();
                int[] potentials = {source.getPotential1(), source.getPotential2(), source.getPotential3(), source.getPotential4(), source.getPotential5()};
                for (int i : potentials) {
                    if (i > 0) {
                        int itemLevel = (int) ((double) ii.getReqLevel(source.getItemId()) / (double) 10);
                        StructItemOption pot = ii.getPotentialInfo(i).get(Randomizer.Min(itemLevel - 1, 0));
                        if (pot != null && pot.get("skillID") > 0) {
                            ss.put(SkillFactory.getSkill(PlayerStats.getSkillByJob(pot.get("skillID"), c.getPlayer().getJob())), new SkillEntry((byte) 0, (byte) 0, -1));
                        }
                    }
                }
                c.getPlayer().changeSkillLevel_Skip(ss, true);
            }
            if (source.getSocketState() > 15) {
                final Map<Skill, SkillEntry> ss = new HashMap<>();
                int[] sockets = {source.getSocket1(), source.getSocket2(), source.getSocket3()};
                for (int i : sockets) {
                    if (i > 0) {
                        StructItemOption soc = ii.getSocketInfo(i);
                        if (soc != null && soc.get("skillID") > 0) {
                            ss.put(SkillFactory.getSkill(PlayerStats.getSkillByJob(soc.get("skillID"), c.getPlayer().getJob())), new SkillEntry((byte) 1, (byte) 0, -1));
                        }
                    }
                }
                c.getPlayer().changeSkillLevel_Skip(ss, true);
            }
            if (src == -54 && c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -53) != null) {
                MapleInventoryManipulator.unequip(c, (short) -53, equip.getNextFreeSlot());
            }
            if (src == -53) {
                c.getPlayer().removeAndroid();
            }
            c.announce(InventoryPacket.moveInventoryItem(MapleInventoryType.EQUIP, src, dst, (byte) 1, false, false));
            c.getPlayer().equipChanged();
            if (src == -11) {
                c.getPlayer().weaponType = 1.0f;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean drop(final MapleClient c, MapleInventoryType type, final short src, final short quantity) {
        return drop(c, type, src, quantity, false);
    }

    public static boolean drop(final MapleClient c, MapleInventoryType type, final short src, short quantity, final boolean npcInduced) {
        if (c.getPlayer() != null && c.getPlayer().getMap().getItemsSize() > 250) {
            c.getPlayer().dropMessage(1, "Too many items on the map to drop more");
            c.announce(CWvsContext.enableActions());
            return false;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (src < 0) {
            type = MapleInventoryType.EQUIPPED;
        }
        if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
            return false;
        }
        final Item source = c.getPlayer().getInventory(type).getItem(src);
        if (quantity < 0 || source == null || (GameConstants.GMS && src == -55) || (quantity == 0 && !GameConstants.isRechargable(source.getItemId())) || c.getPlayer().inPVP()) {
            c.announce(CWvsContext.enableActions());
            return false;
        }

        final short flag = source.getFlag();
        if (quantity > source.getQuantity() && !GameConstants.isRechargable(source.getItemId())) {
            c.announce(CWvsContext.enableActions());
            return false;
        }
        if (ItemFlag.LOCK.check(flag) || (quantity != 1 && type == MapleInventoryType.EQUIP)) { // hack
            c.announce(CWvsContext.enableActions());
            return false;
        }

        if (type == MapleInventoryType.EQUIP) { // hack
            Equip eqp = (Equip) source;
            if (!eqp.isCash(eqp.getItemId())) {
                if (eqp.getPower() > 50) {
                    c.getPlayer().dropMessage(1, "This equip is too powerful to drop");
                    c.announce(CWvsContext.enableActions());
                    return false;
                }
            } else {
                c.getPlayer().dropMessage(1, "This equip is too powerful to drop");
                c.announce(CWvsContext.enableActions());
                return false;
            }
        }

        final Point dropPos = new Point(c.getPlayer().getPosition());
        //c.getPlayer().getCheatTracker().checkDrop();
        if (quantity < source.getQuantity() && !GameConstants.isRechargable(source.getItemId())) {
            final Item target = source.copy();
            target.setQuantity(quantity);
            source.setQuantity((short) (source.getQuantity() - quantity));
            c.announce(InventoryPacket.dropInventoryItemUpdate(type, source));

            if (ii.isDropRestricted(target.getItemId()) || ii.isAccountShared(target.getItemId())) {
                if (ItemFlag.KARMA_EQ.check(flag)) {
                    target.setFlag((byte) (flag - ItemFlag.KARMA_EQ.getValue()));
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                } else if (ItemFlag.KARMA_USE.check(flag)) {
                    target.setFlag((byte) (flag - ItemFlag.KARMA_USE.getValue()));
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                } else {
                    //c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                }
            } else {
                if (GameConstants.isPet(source.getItemId())) {
                    c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer().getId(), target, dropPos);
                } else {
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                }
            }
        } else {
            c.getPlayer().getInventory(type).removeSlot(src);
            if (GameConstants.isHarvesting(source.getItemId())) {
                c.getPlayer().getStat().handleProfessionTool(c.getPlayer());
            }
            c.announce(InventoryPacket.dropInventoryItem((src < 0 ? MapleInventoryType.EQUIP : type), src));
            if (src < 0) {
                c.getPlayer().equipChanged();
            }
            c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
        }
        return true;
    }

    private static boolean haveItemWithId(MapleInventory inv, int itemid) {
        return inv.findById(itemid) != null;
    }

    public static int checkSpaceProgressively(MapleClient c, int itemid, int quantity, String owner, int usedSlots, boolean useProofInv) {
        // return value --> bit0: if has space for this one;
        //                  value after: new slots filled;
        // assumption: equipments always have slotMax == 1.

        int returnValue;

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = !useProofInv ? GameConstants.getInventoryType(itemid) : MapleInventoryType.CANHOLD;
        MapleCharacter chr = c.getPlayer();
        MapleInventory inv = chr.getInventory(type);

        if (ii.isPickupRestricted(itemid)) {
            if (haveItemWithId(inv, itemid)) {
                return 0;
            } else if (GameConstants.isEquipment(itemid) && haveItemWithId(chr.getInventory(MapleInventoryType.EQUIPPED), itemid)) {
                return 0;   // thanks Captain & Aika & Vcoc for pointing out inventory checkup on player trades missing out one-of-a-kind items.
            }
        }

        if (!type.equals(MapleInventoryType.EQUIP)) {
            int slotMax = ii.getSlotMax(itemid);
            final int numSlotsNeeded;

            if (GameConstants.isRechargeable(itemid)) {
                numSlotsNeeded = 1;
            } else {
                List<Item> existing = inv.listById(itemid);

                if (existing.size() > 0) // first update all existing slots to slotMax
                {
                    for (Item eItem : existing) {
                        int oldQ = eItem.getQuantity();
                        if (oldQ < slotMax && owner.equals(eItem.getOwner())) {
                            int newQ = Math.min(oldQ + quantity, slotMax);
                            quantity -= (newQ - oldQ);
                        }
                        if (quantity <= 0) {
                            break;
                        }
                    }
                }

                if (slotMax > 0) {
                    numSlotsNeeded = (int) (Math.ceil(((double) quantity) / slotMax));
                } else {
                    numSlotsNeeded = 1;
                }
            }

            returnValue = ((numSlotsNeeded + usedSlots) << 1);
            returnValue += (numSlotsNeeded == 0 || !inv.isFullAfterSomeItems(numSlotsNeeded - 1, usedSlots)) ? 1 : 0;
            //System.out.print(" needed " + numSlotsNeeded + " used " + usedSlots + " rval " + returnValue);
        } else {
            returnValue = ((quantity + usedSlots) << 1);
            returnValue += (!inv.isFullAfterSomeItems(0, usedSlots)) ? 1 : 0;
            //System.out.print(" eqpneeded " + 1 + " used " + usedSlots + " rval " + returnValue);
        }

        return returnValue;
    }
}
