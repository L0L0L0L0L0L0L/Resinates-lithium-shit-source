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
package client.inventory;

import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.io.Serializable;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import server.MapleInventoryManipulator;
import server.ThreadManager;
import tools.Pair;

public class MapleInventory implements Iterable<Item>, Serializable {

    protected Map<Short, Item> inventory = new LinkedHashMap<>();
    private byte slotLimit = 0;
    public MapleInventoryType type;
    protected Lock lock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.INVENTORY, true);
    protected boolean checked = false;

    /**
     * Creates a new instance of MapleInventory
     */
    public MapleInventory(MapleInventoryType type) {
        this.inventory = new LinkedHashMap<>();
        this.type = type;
    }

    public void addSlot(byte slot) {
        this.slotLimit += slot;

        if (slotLimit > 96) {
            slotLimit = 96;
        }
    }

    public byte getSlotLimit() {
        return slotLimit;
    }

    public void setSlotLimit(byte slot) {
        if (slot > 96) {
            slot = 96;
        }
        slotLimit = slot;
    }

    public Collection<Item> list() {
        lock.lock();
        try {
            return Collections.unmodifiableCollection(inventory.values());
        } finally {
            lock.unlock();
        }
    }

    public Collection<Item> sortedList() {
        lock.lock();
        try {
            List<Item> items = new ArrayList<>(inventory.values());
            Collections.sort(items, new Comparator<Item>() {

                @Override
                public int compare(Item o1, Item o2) {
                    return Short.compare(o1.getPosition(), o2.getPosition());
                }

            });
            return items;
        } finally {
            lock.unlock();
        }
    }

    public Collection<Item> sortedFlipList() {
        lock.lock();
        try {
            List<Item> items = new ArrayList<>(inventory.values());
            Collections.sort(items, new Comparator<Item>() {

                @Override
                public int compare(Item o1, Item o2) {
                    return Short.compare(o2.getPosition(), o1.getPosition());
                }

            });
            return items;
        } finally {
            lock.unlock();
        }
    }

    public Collection<Item> sortedOrderedList() {
        lock.lock();
        try {
            List<Item> items = new ArrayList<>(inventory.values());
            Collections.sort(items, new Comparator<Item>() {

                @Override
                public int compare(Item o1, Item o2) {
                    return Integer.compare(o1.getItemId(), o2.getItemId());
                }

            });
            return items;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the item with its slot id if it exists within the inventory,
     * otherwise null is returned
     */
    public Item findById(int itemId) {
        for (Item item : inventory.values()) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public Item findByUniqueId(int itemId) {
        for (Item item : inventory.values()) {
            if (item.getUniqueId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public Item findByInventoryId(long itemId, int itemI) {
        for (Item item : inventory.values()) {
            if (item.getInventoryId() == itemId && item.getItemId() == itemI) {
                return item;
            }
        }
        return findById(itemI);
    }

    public Item findByInventoryIdOnly(long itemId, int itemI) {
        for (Item item : inventory.values()) {
            if (item.getInventoryId() == itemId && item.getItemId() == itemI) {
                return item;
            }
        }
        return null;
    }

    public int countById(int itemId) {
        int possesed = 0;
        for (Item item : inventory.values()) {
            if (item.getItemId() == itemId) {
                possesed += item.getQuantity();
            }
        }
        return possesed;
    }

    public List<Item> listById(int itemId) {
        List<Item> ret = new ArrayList<>();
        for (Item item : list()) {
            if (item.getItemId() == itemId) {
                ret.add(item);
            }
        }

        if (ret.size() > 1) {
            Collections.sort(ret, new Comparator<Item>() {
                @Override
                public int compare(Item i1, Item i2) {
                    return i1.getPosition() - i2.getPosition();
                }
            });
        }

        return ret;
    }

    public List<Item> linkedListById(int itemId) {
        List<Item> ret = new LinkedList<>();
        for (Item item : list()) {
            if (item.getItemId() == itemId) {
                ret.add(item);
            }
        }

        if (ret.size() > 1) {
            Collections.sort(ret, new Comparator<Item>() {
                @Override
                public int compare(Item i1, Item i2) {
                    return i1.getPosition() - i2.getPosition();
                }
            });
        }

        return ret;
    }

    public List<Item> newList() {
        if (inventory.size() <= 0) {
            return Collections.emptyList();
        }
        return new LinkedList<Item>(inventory.values());
    }

    public List<Integer> listIds() {
        List<Integer> ret = new ArrayList<Integer>();
        for (Item item : inventory.values()) {
            if (!ret.contains(Integer.valueOf(item.getItemId()))) {
                ret.add(Integer.valueOf(item.getItemId()));
            }
        }
        if (ret.size() > 1) {
            Collections.sort(ret);
        }
        return ret;
    }

    public short addItem(Item item) {
        short slotId = addSlot(item);
        if (slotId == -1) {
            return -1;
        }
        item.setPosition(slotId);
        return slotId;
    }

    /**
     * Adds the item to the inventory and returns the assigned slot id
     *
     * @param item
     * @return
     */
    protected short addSlot(Item item) {
        if (item == null) {
            return -1;
        }

        short slotId;
        lock.lock();
        try {
            slotId = getNextFreeSlot();
            if (slotId < 0) {
                return -1;
            }

            inventory.put(slotId, item);
        } finally {
            lock.unlock();
        }

        return slotId;
    }

    public void addFromDB(Item item) {
        if (item.getPosition() < 0 && !type.equals(MapleInventoryType.EQUIPPED)) {
            return;
        }
        addSlotFromDB(item.getPosition(), item);
    }

    public void addItemFromDB(Item item) {
        if (item.getPosition() < 0 && !type.equals(MapleInventoryType.EQUIPPED)) {
            return;
        }
        addSlotFromDB(item.getPosition(), item);
    }

    protected void addSlotFromDB(short slot, Item item) {
        lock.lock();
        try {
            inventory.put(slot, item);
        } finally {
            lock.unlock();
        }
    }

    public void removeSlot(short slot) {
        Item item;
        lock.lock();
        try {
            item = inventory.remove(slot);
        } finally {
            lock.unlock();
        }
    }

    public void move(short sSlot, short dSlot, short slotMax) {
        Item source = (Item) inventory.get(sSlot);
        Item target = (Item) inventory.get(dSlot);
        if (source == null) {
            throw new InventoryException("Trying to move empty slot");
        }
        if (target == null) {
            if (dSlot < 0 && !type.equals(MapleInventoryType.EQUIPPED)) {
                // This causes a lot of stuck problem, until we are done with position checking
                return;
            }
            if (dSlot > 0 && type.equals(MapleInventoryType.EQUIPPED)) {
                // This causes a lot of stuck problem, until we are done with position checking
                return;
            }
            source.setPosition(dSlot);
            inventory.put(dSlot, source);
            inventory.remove(sSlot);
        } else if (target.getItemId() == source.getItemId() && !GameConstants.isThrowingStar(source.getItemId()) && !GameConstants.isBullet(source.getItemId()) && target.getOwner().equals(source.getOwner()) && target.getExpiration() == source.getExpiration()) {
            if (type.getType() == MapleInventoryType.EQUIP.getType() || type.getType() == MapleInventoryType.CASH.getType()) {
                swap(target, source);
            } else if (source.getQuantity() + target.getQuantity() > slotMax) {
                source.setQuantity((short) ((source.getQuantity() + target.getQuantity()) - slotMax));
                target.setQuantity(slotMax);
            } else {
                target.setQuantity((short) (source.getQuantity() + target.getQuantity()));
                inventory.remove(sSlot);
            }
        } else {
            swap(target, source);
        }
    }

    private void swap(Item source, Item target) {
        inventory.remove(source.getPosition());
        inventory.remove(target.getPosition());
        short swapPos = source.getPosition();
        source.setPosition(target.getPosition());
        target.setPosition(swapPos);
        inventory.put(source.getPosition(), source);
        inventory.put(target.getPosition(), target);
    }

    public Item getItem(short slot) {
        return inventory.get(slot);
    }

    public void removeItem(short slot) {
        removeItem(slot, (short) 1, false);
    }

    public void removeAllItem(short slot) {
        removeItem(slot);
    }

    public void removeItem(short slot, short quantity, boolean allowZero) {
        Item item = getItem(slot);
        if (item == null) {// TODO is it ok not to throw an exception here?
            return;
        }
        item.setQuantity((short) (item.getQuantity() - quantity));
        if (item.getQuantity() < 0) {
            item.setQuantity((short) 0);
        }
        if (item.getQuantity() == 0 && !allowZero) {
            removeSlot(slot);
        }
    }

    public boolean isFull() {
        return inventory.size() >= slotLimit;
    }

    public boolean isFull(int margin) {
        return inventory.size() + margin >= slotLimit;
    }

    /**
     * Returns the next empty slot id, -1 if the inventory is full
     */
    public short getNextFreeSlot() {
        if (isFull()) {
            return -1;
        }
        lock.lock();
        try {
            for (short i = 1; i <= slotLimit; i++) {
                if (!inventory.containsKey(i)) {
                    return i;
                }
            }
            return -1;
        } finally {
            lock.unlock();
        }
    }

    public short getNumFreeSlot() {
        if (isFull()) {
            return 0;
        }
        lock.lock();
        try {
            byte free = 0;
            for (short i = 1; i <= slotLimit; i++) {
                if (!inventory.containsKey(i)) {
                    free++;
                }
            }
            return free;
        } finally {
            lock.unlock();
        }
    }

    public MapleInventoryType getType() {
        return type;
    }

    @Override
    public Iterator<Item> iterator() {
        return Collections.unmodifiableCollection(inventory.values()).iterator();
    }

    public boolean checked() {
        lock.lock();
        try {
            return checked;
        } finally {
            lock.unlock();
        }
    }

    public void checked(boolean yes) {
        lock.lock();
        try {
            checked = yes;
        } finally {
            lock.unlock();
        }
    }

    public void lockInventory() {
        lock.lock();
    }

    public void unlockInventory() {
        lock.unlock();
    }

    public static boolean checkSpot(MapleCharacter chr, Item item) {    // thanks Vcoc for noticing pshops not checking item stacks when taking item back
        return checkSpot(chr, Collections.singletonList(item));
    }

    public static boolean checkSpot(MapleCharacter chr, List<Item> items) {
        List<Pair<Item, MapleInventoryType>> listItems = new LinkedList<>();
        for (Item item : items) {
            listItems.add(new Pair<>(item, item.getInventoryType()));
        }

        return checkSpotsAndOwnership(chr, listItems);
    }

    public static boolean checkSpots(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items) {
        return checkSpots(chr, items, false);
    }

    public static boolean checkSpots(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items, boolean useProofInv) {
        int invTypesSize = MapleInventoryType.values().length;
        List<Integer> zeroedList = new ArrayList<>(invTypesSize);
        for (byte i = 0; i < invTypesSize; i++) {
            zeroedList.add(0);
        }

        return checkSpots(chr, items, zeroedList, useProofInv);
    }

    public static boolean checkSpots(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items, List<Integer> typesSlotsUsed, boolean useProofInv) {
        // assumption: no "UNDEFINED" or "EQUIPPED" items shall be tested here, all counts are >= 0.

        Map<Integer, List<Integer>> rcvItems = new LinkedHashMap<>();
        Map<Integer, Byte> rcvTypes = new LinkedHashMap<>();

        for (Pair<Item, MapleInventoryType> item : items) {
            Integer itemId = item.left.getItemId();
            List<Integer> qty = rcvItems.get(itemId);

            if (qty == null) {
                List<Integer> itemQtyList = new LinkedList<>();
                itemQtyList.add((int) item.left.getQuantity());

                rcvItems.put(itemId, itemQtyList);
                rcvTypes.put(itemId, item.right.getType());
            } else {
                if (!GameConstants.isRechargeable(itemId)) {
                    qty.set(0, qty.get(0) + item.left.getQuantity());
                } else {
                    qty.add((int) item.left.getQuantity());
                }
            }
        }

        MapleClient c = chr.getClient();
        for (Map.Entry<Integer, List<Integer>> it : rcvItems.entrySet()) {
            int itemType = rcvTypes.get(it.getKey()) - 1;

            for (Integer itValue : it.getValue()) {
                int usedSlots = typesSlotsUsed.get(itemType);

                int result = MapleInventoryManipulator.checkSpaceProgressively(c, it.getKey(), itValue, "", usedSlots, useProofInv);
                boolean hasSpace = ((result % 2) != 0);

                if (!hasSpace) {
                    return false;
                }
                typesSlotsUsed.set(itemType, (result >> 1));
            }
        }

        return true;
    }

    private static long fnvHash32(final String k) {
        final int FNV_32_INIT = 0x811c9dc5;
        final int FNV_32_PRIME = 0x01000193;

        int rv = FNV_32_INIT;
        final int len = k.length();
        for (int i = 0; i < len; i++) {
            rv ^= k.charAt(i);
            rv *= FNV_32_PRIME;
        }

        return rv >= 0 ? rv : (2L * Integer.MAX_VALUE) + rv;
    }

    private static Long hashKey(Integer itemId, String owner) {
        return (itemId.longValue() << 32L) + fnvHash32(owner);
    }

    public static boolean checkSpotsAndOwnership(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items) {
        return checkSpotsAndOwnership(chr, items, false);
    }

    public static boolean checkSpotsAndOwnership(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items, boolean useProofInv) {
        List<Integer> zeroedList = new ArrayList<>(5);
        for (byte i = 0; i < 5; i++) {
            zeroedList.add(0);
        }

        return checkSpotsAndOwnership(chr, items, zeroedList, useProofInv);
    }

    public static boolean checkSpotsAndOwnership(MapleCharacter chr, List<Pair<Item, MapleInventoryType>> items, List<Integer> typesSlotsUsed, boolean useProofInv) {
        //assumption: no "UNDEFINED" or "EQUIPPED" items shall be tested here, all counts are >= 0 and item list to be checked is a legal one.

        Map<Long, List<Integer>> rcvItems = new LinkedHashMap<>();
        Map<Long, Byte> rcvTypes = new LinkedHashMap<>();
        Map<Long, String> rcvOwners = new LinkedHashMap<>();

        for (Pair<Item, MapleInventoryType> item : items) {
            Long itemHash = hashKey(item.left.getItemId(), item.left.getOwner());
            List<Integer> qty = rcvItems.get(itemHash);

            if (qty == null) {
                List<Integer> itemQtyList = new LinkedList<>();
                itemQtyList.add((int) item.left.getQuantity());

                rcvItems.put(itemHash, itemQtyList);
                rcvTypes.put(itemHash, item.right.getType());
                rcvOwners.put(itemHash, item.left.getOwner());
            } else {
                // thanks BHB88 for pointing out an issue with rechargeable items being stacked on inventory check
                if (!GameConstants.isRechargeable(item.left.getItemId())) {
                    qty.set(0, qty.get(0) + item.left.getQuantity());
                } else {
                    qty.add((int) item.left.getQuantity());
                }
            }
        }

        MapleClient c = chr.getClient();
        for (Map.Entry<Long, List<Integer>> it : rcvItems.entrySet()) {
            int itemType = rcvTypes.get(it.getKey()) - 1;
            int itemId = (int) (it.getKey() >> 32L);

            for (Integer itValue : it.getValue()) {
                int usedSlots = typesSlotsUsed.get(itemType);

                //System.out.print("inserting " + itemId.intValue() + " with type " + itemType + " qty " + it.getValue() + " owner '" + rcvOwners.get(it.getKey()) + "' current usedSlots:");
                //for(Integer i : typesSlotsUsed) System.out.print(" " + i);
                int result = MapleInventoryManipulator.checkSpaceProgressively(c, itemId, itValue, rcvOwners.get(it.getKey()), usedSlots, useProofInv);
                boolean hasSpace = ((result % 2) != 0);
                //System.out.print(" -> hasSpace: " + hasSpace + " RESULT : " + result + "\n");

                if (!hasSpace) {
                    return false;
                }
                typesSlotsUsed.set(itemType, (result >> 1));
            }
        }

        return true;
    }

    public boolean isFullAfterSomeItems(int margin, int used) {
        lock.lock();
        try {
            //System.out.print("(" + inventory.size() + " " + margin + " <> " + slotLimit + " -" + used + ")");
            return inventory.size() + margin >= slotLimit - used;
        } finally {
            lock.unlock();
        }
    }
}
