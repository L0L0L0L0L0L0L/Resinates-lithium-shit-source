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
package server.shops;

import java.util.ArrayList;
import java.util.List;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.MapleMapObjectType;
import tools.FilePrinter;
import tools.packet.CWvsContext;
import tools.packet.PlayerShopPacket;

public class MaplePlayerShop extends AbstractPlayerStore {

    private int boughtnumber = 0;
    private List<String> bannedList = new ArrayList<String>();
    public MapleCharacter ShopOwner;
    private Lock visitorLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.VISITOR_PSHOP, true);
    private Lock itemLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.ITEM, true);
    private List<SoldItem> sold = new LinkedList<>();
    public int standID = 0;
    public boolean stack = false;
    public int hours = 0;

    public MaplePlayerShop(MapleCharacter owner, int itemId, String desc) {
        super(owner, itemId, desc, "", 6);
    }

    private static boolean canBuy(MapleClient c, Item newItem) {
        return MapleInventoryManipulator.checkSpace(c, newItem.getItemId(), newItem.getQuantity(), newItem.getOwner());
    }

    @Override
    public boolean buy(MapleClient c, int item, short quantity) {
        if (c == null) {
            return false;
        }
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return false;
        }
        if (!chr.checkPlayer()) {
            return false;
        }
        //System.out.println("seller");
        itemLock.lock();
        try {
            synchronized (items) {
                if (isVisitor(c.getPlayer())) {
                    MaplePlayerShopItem pItem = items.get(item);
                    if (pItem == null) {
                        System.out.println("[" + Calendar.getInstance().getTime() + "] - " + c.getPlayer().getName() + " trying to buy null item: " + item);
                        c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                        return false;
                    }
                    if (pItem.getAmount() <= 0) {
                        if (pItem.getAmount() < 0) {
                            System.out.println("[" + Calendar.getInstance().getTime() + "] - " + getOwnerName() + " has negative quantity Item: " + MapleItemInformationProvider.getInstance().getName(pItem.getItem().getItemId()) + " - Amount: " + pItem.getAmount());
                        }
                        c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                        return false;
                    }
                    if (canBuy(c, pItem.getItem())) {
                        //System.out.println("shop item amount_a: " + pItem.getAmount());
                        //System.out.println("shop item amount_a_2: " + pItem.getQuantity());
                        //System.out.println("shop item amount_b: " + quantity);
                        if (quantity > pItem.getQuantity()) {
                            System.out.println("[" + Calendar.getInstance().getTime() + "] - " + c.getPlayer().getName() + " trying to buy more items than listed: Quantity: " + quantity + " out of shop max of: " + pItem.getQuantity());
                            c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                            return false;
                        } else {
                            Item newItem;
                            if (pItem.getItem().getInventoryType() != MapleInventoryType.EQUIP) {
                                newItem = pItem.getItem().copy(pItem.getItem().getItemId(), quantity);
                            } else {
                                newItem = pItem.getItem().copy();
                            }
                            //System.out.println("shop item id: " + newItem.getItemId());
                            //System.out.println("shop item amount: " + newItem.getQuantity());
                            //System.out.println("shop item amount2: " + pItemAmount);
                            //System.out.println("shop total item amount: " + pItemAmount);
                            short flag = newItem.getFlag();

                            if (ItemFlag.KARMA_EQ.check(flag)) {
                                newItem.setFlag((short) (flag - ItemFlag.KARMA_EQ.getValue()));
                            } else if (ItemFlag.KARMA_USE.check(flag)) {
                                newItem.setFlag((short) (flag - ItemFlag.KARMA_USE.getValue()));
                            }
                            if (ItemFlag.LUCKS_KEY.check(flag)) {
                                newItem.setFlag((short) (flag - ItemFlag.LUCKS_KEY.getValue()));
                            }
                            visitorLock.lock();
                            try {
                                String itemName = MapleItemInformationProvider.getInstance().getName(getCurrency());
                                String soldName = MapleItemInformationProvider.getInstance().getName(newItem.getItemId());
                                String msg = "";
                                final long pPrice = pItem.getPrice();
                                final long pQ = quantity;
                                final long getMeso = pPrice * pQ;
                                if (getMeso > 2000000000) {
                                    c.getPlayer().dropMessage(1, "You cannot buy this many of this item.");
                                    c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                                    return false;
                                }
                                final int gainmeso = (int) getMeso;
                                if (getCurrency() == 4310999) {
                                    if (c.getPlayer().getMeso() >= gainmeso) {
                                        if (!MapleInventoryManipulator.addFromDrop(c, newItem, false)) {
                                            itemName = MapleItemInformationProvider.getInstance().getName(pItem.getItem().getItemId());
                                            c.getPlayer().dropMessage(1, "You do not have enough room for\r\n" + itemName);
                                            c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                                            return false;
                                        }
                                        itemName = "Mesos";
                                        c.getPlayer().gainMeso(-gainmeso, false);
                                        getMCOwner().gainMeso(gainmeso, false);
                                        getMCOwner().dropMessage(1, "Sold " + (soldName + msg) + "\r\nGained " + gainmeso + " " + itemName + ".\r\nExcess Mesos placed into Bank.");
                                    } else {
                                        c.getPlayer().dropMessage(1, "Your lacking mesos to buy this item.");
                                        c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                                        return false;
                                    }
                                } else {
                                    if (c.getPlayer().havePlayerStoreItem(getCurrency(), gainmeso)) {
                                        if (!MapleInventoryManipulator.addFromDrop(c, newItem, false)) {
                                            itemName = MapleItemInformationProvider.getInstance().getName(pItem.getItem().getItemId());
                                            c.getPlayer().dropMessage(1, "You do not have enough room for\r\n" + itemName);
                                            c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                                            return false;
                                        }
                                        c.getPlayer().gainItem(getCurrency(), -gainmeso, "");
                                        if (newItem.getInventoryType() != MapleInventoryType.EQUIP && quantity > 1) {
                                            msg = " (x" + quantity + ")";
                                        }
                                        getMCOwner().addOverflow(getCurrency(), gainmeso, "Shop Buyer: " + c.getPlayer().getName() + " from sale: Item: " + soldName + " - Quantity: " + newItem.getQuantity() + " From Owner: " + getMCOwner().getName());
                                        getMCOwner().dropMessage(1, "Sold " + (soldName + msg) + "\r\nGained " + gainmeso + " " + itemName + "\r\n" + itemName + "s stored in ETC Storage.");
                                    } else {
                                        itemName = MapleItemInformationProvider.getInstance().getName(getCurrency());
                                        c.getPlayer().dropMessage(1, "You do not have enough " + itemName);
                                        c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                                        return false;
                                    }
                                }
                                FilePrinter.print(FilePrinter.LOG_SHOP, "[" + Calendar.getInstance().getTime() + "] " + c.getPlayer().getName() + " gained " + newItem.getQuantity() + "x " + newItem.getItemName(newItem.getItemId()) + " - for (x" + gainmeso + ") - " + (itemName) + " from shop owner " + getMCOwner().getName() + ".");
                                pItem.amount -= quantity;
                                //SoldItem soldItem = new SoldItem(c.getPlayer().getName(), pItem.getItem().getItemId(), quantity, gainmeso);
                                getMCOwner().getClient().announce(PlayerShopPacket.getPlayerShopOwnerUpdate(pItem, pItem.getSlot()));
                                broadcast(PlayerShopPacket.shopItemUpdate(this));

                                if (pItem.amount < 1) {
                                    pItem.bundles = 0;
                                    pItem.amount = 0;
                                    pItem.setDoesExist(false);
                                    removeItem(pItem);//sql remove item
                                    if (countAllItems() <= 0) {
                                        closeShop(false);
                                        c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                                        c.getPlayer().saveToDB();
                                        return false;
                                    }
                                } else {
                                    updateItem(pItem);//sql update item
                                }
                                c.getPlayer().saveToDB();
                                getMCOwner().saveToDB();
                                return true;
                            } finally {
                                visitorLock.unlock();
                            }

                        }
                    } else {
                        c.getPlayer().dropMessage(1, "Your inventory is full.");
                        c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                        return false;
                    }
                } else {
                    c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                    return false;
                }
            }
        } finally {
            itemLock.unlock();
        }

    }

    @Override
    public byte getShopType() {
        return IMaplePlayerShop.PLAYER_SHOP;
    }

    public List<SoldItem> getSold() {
        synchronized (sold) {
            return Collections.unmodifiableList(sold);
        }
    }

    public int getCurrency() {
        return GameConstants.getShopCurrency(owner.getPlayerShop().getItemId());
    }

    public void setOwner(MapleCharacter owner) {
        ShopOwner = owner;
    }

    public MapleCharacter getOwner() {
        return ShopOwner;
    }

    @Override
    public void closeShop(boolean saveItems) {
        removeAllVisitors(3, 1);
        MapleCharacter shopOwner = getMCOwner();
        long time = shopOwner.shoptime;
        shopOwner.shoptime = 0;
        shopOwner.shopEvent = false;
        getMap().removeMapObject(this, MapleMapObjectType.SHOP);
        for (MaplePlayerShopItem pItem : getItems()) {
            if (pItem.getAmount() > 0) {
                Item newItem;
                if (pItem.getItem().getInventoryType() != MapleInventoryType.EQUIP) {
                    newItem = pItem.getItem().copy(pItem.getItem().getItemId(), pItem.getAmount());
                } else {
                    newItem = pItem.getItem().copy();
                }
                if (MapleInventoryManipulator.addFromDrop(shopOwner.getClient(), newItem, false)) {
                    removeItem(pItem);//sql remove item
                }
            }
        }
        removeAllItems(shopOwner);
        shopOwner.setPlayerShop(null);
        shopOwner.getMap().broadcastMessage(PlayerShopPacket.removeCharBox(shopOwner));
        shopOwner.saveItems();
        if (!GameConstants.shutdown) {
            if (shopOwner.getOffline()) {
                shopOwner.kick();
                return;
            }
        }
        shopOwner.saveToDB();
    }

    public void banPlayer(String name) {
        if (!bannedList.contains(name)) {
            bannedList.add(name);
        }
        for (int i = 0; i < 6; i++) {
            MapleCharacter chr = getVisitor(i);
            if (chr != null && chr.getName().equals(name)) {
                chr.getClient().announce(PlayerShopPacket.shopErrorMessage(5, 1));
                chr.setPlayerShop(null);
                removeVisitor(chr, 5);
            }
        }
    }

    public boolean isBanned(String name) {
        return bannedList.contains(name);
    }

    public void clearBan() {
        bannedList.clear();
    }

    public class SoldItem {

        int itemid, quantity;
        long mesos;
        String buyer;

        public SoldItem(String buyer, int itemid, int quantity, long mesos) {
            this.buyer = buyer;
            this.itemid = itemid;
            this.quantity = quantity;
            this.mesos = mesos;
        }

        public String getBuyer() {
            return buyer;
        }

        public int getItemId() {
            return itemid;
        }

        public int getQuantity() {
            return quantity;
        }

        public long getMesos() {
            return mesos;
        }
    }
}
