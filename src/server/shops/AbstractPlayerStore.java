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

import constants.GameConstants;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.ref.WeakReference;

import client.inventory.Item;
import client.inventory.ItemLoader;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.MapleImp;
import client.inventory.MapleInventoryType;
import database.DatabaseConnection;

import handling.channel.ChannelServer;
import handling.world.World;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import server.Randomizer;
import server.maps.MapleMapObject;
import server.maps.MapleMap;
import server.maps.MapleMapObjectType;
import tools.Pair;
import tools.packet.CField;
import tools.packet.PlayerShopPacket;

public abstract class AbstractPlayerStore extends MapleMapObject implements IMaplePlayerShop {

    protected boolean open = false, available = false;
    protected String ownerName, des, pass;
    protected int ownerId, owneraccount, itemId, channel, map, itemSlot = 1;
    protected MapleCharacter owner;
    protected AtomicInteger meso = new AtomicInteger(0);
    protected MapleCharacter[] visitors = new MapleCharacter[6];
    protected List<BoughtItem> bought = new LinkedList<BoughtItem>();
    protected List<MaplePlayerShopItem> items = new LinkedList<MaplePlayerShopItem>();

    public AbstractPlayerStore(MapleCharacter owner, int itemId, String desc, String pass, int slots) {
        this.setPosition(owner.getTruePosition());
        this.owner = owner;
        this.ownerName = owner.getName();
        this.ownerId = owner.getId();
        this.owneraccount = owner.getAccountID();
        this.itemId = itemId;
        this.des = desc;
        this.pass = pass;
        this.map = owner.getMapId();
        this.channel = owner.getClient().getChannel();
        //this.players.put(owner, 0);
    }

    @Override
    public int getMaxSize() {
        return 7;
    }

    @Override
    public int getSize() {
        return getFreeSlot() == -1 ? getMaxSize() : getFreeSlot();
    }

    public MapleCharacter getStoreOwner() {
        return owner;
    }

    @Override
    public void broadcast(final byte[] packet) {
        if (getStoreOwner().getClient() != null) {
            getStoreOwner().getClient().announce(packet);
        }
        broadcastToVisitors(packet);

    }

    public int countAllItems() {
        int count = 0;

        for (MaplePlayerShopItem item : items) {
            count += item.getBundles();
        }

        return count;
    }

    @Override
    public void broadcastToVisitors(byte[] packet) {
        for (MapleCharacter chr : visitors) {
            if (chr != null) {
                chr.getClient().announce(packet);
            }
        }
    }

    public void broadcastToVisitors(byte[] packet, byte slot) {
        for (MapleCharacter chr : visitors) {
            if (chr != null && chr != getVisitor(slot)) {
                chr.getClient().announce(packet);
            }
        }
    }

    @Override
    public int getMeso() {
        return meso.get();
    }

    @Override
    public void setMeso(int meso) {
        this.meso.set(meso);
    }

    @Override
    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    public MapleCharacter getVisitor(int num) {
        return visitors[num];
    }

    @Override
    public void update() {
        if (isAvailable()) {
            if (getShopType() == IMaplePlayerShop.HIRED_MERCHANT) {
                getMap().broadcastMessage(PlayerShopPacket.updateHiredMerchant((HiredMerchant) this));
            } else if (getMCOwner() != null) {
                getMap().broadcastMessage(PlayerShopPacket.sendPlayerShopBox(getMCOwner()));
            }
        }
    }

    @Override
    public void addVisitor(MapleCharacter visitor) {
        if (visitor.getShop() == null) {
            int i = getFreeSlot();
            if (i > 0) {
                visitors[i - 1] = visitor;
                if (getShopType() >= 3) {
                    broadcastToVisitors(PlayerShopPacket.getMiniGameNewVisitor(visitor, i, (MapleMiniGame) this));
                } else {
                    broadcast(PlayerShopPacket.shopVisitorAdd(visitor, i));
                }
                if (i == 6) {
                    update();
                }
            }
        }
        //System.out.println("player added to visitor: " + visitor.getName());
    }

    /*
     3 = shop is closed
     5 = kick
     14 = out of stock
    
     */
    @Override
    public void removeVisitor(MapleCharacter visitor, int reason) {
        int slot = (byte) (getVisitorSlot(visitor) - 1);
        visitors[slot] = null;
        broadcast(PlayerShopPacket.shopVisitorLeave((byte) (slot + 1), reason));
        //broadcastToVisitors(PlayerShopPacket.getPlayerStore(owner, false));
        /*
         for (int i = 0; i < 6; i++) {
         if (visitors[i] != null) {
         visitors[i].announce(MaplePacketCreator.getPlayerShop(this, false));
         }
         }
         /*
         for (int i = 0; i < 6; i++) {
         if (visitors[i] != null) {
         if (visitors[i].getId() == visitor.getId()) {
         for (int j = i; j < 5; j++) {
         if (visitors[j] != null) {
         owner.getClient().announce(PlayerShopPacket.shopVisitorLeave((byte) (j + 1)));
         //visitors[j].getClient().announce(PlayerShopPacket.shopVisitorLeave((byte) (j + 1)));
         }
         visitors[j] = visitors[j + 1];
         if (visitors[j] != null) {
         visitors[j].setSlot(j);
         }
         }
         visitors[5] = null;
         for (int j = i; j < 5; j++) {
         if (visitors[j] != null) {
         owner.getClient().announce(PlayerShopPacket.shopVisitorAdd(visitors[j], (byte) (j + 1)));
         //visitors[j].getClient().announce(PlayerShopPacket.shopVisitorAdd(visitors[j], (byte) (j + 1)));
         }
         }
         }

         //owner.getMap().broadcastMessage(MaplePacketCreator.updatePlayerShopBox(this));
         //return;
         } else {
         System.out.println("null Slot: " + i);
         }
         }

         */
        //updateVisitors();
    }

    @Override
    public byte getVisitorSlot(MapleCharacter visitor) {
        for (int i = 0; i < visitors.length; i++) {
            if (visitors[i] == visitor) {
                return (byte) (i + 1);
            }
        }
        return -1;
    }

    @Override
    public void removeAllVisitors(int error, int type) {
        //System.out.println("close:");
        /*
         for (int i = 1; i < 6; i++) {
         MapleCharacter visitor = getVisitor(i);
         if (visitor != null) {
         if (type != -1) {
         visitor.getClient().announce(PlayerShopPacket.shopErrorMessage(error, type));
         }
         broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(getVisitorSlot(visitor)), getVisitorSlot(visitor));
         visitor.setPlayerShop(null);
         chrs[i] = new WeakReference<MapleCharacter>(null);
         }
         }
         */

        for (MapleCharacter visitor : visitors) {
            if (visitor != null) {
                int slot = (byte) (getVisitorSlot(visitor) - 1);
                broadcast(PlayerShopPacket.shopVisitorLeave((byte) (slot + 1), error));

                visitors[slot].setPlayerShop(null);

                visitors[slot] = null;
                //visitor.getClient().announce(PlayerShopPacket.shopVisitorLeave((byte) (slot + 1)));
                //getStoreOwner().getClient().announce(PlayerShopPacket.shopVisitorLeave((byte) (slot + 1)));
            }
            //getStoreOwner().getClient().announce(PlayerShopPacket.shopVisitorLeave((byte) (slot + 1)));
        }

        //update();
    }

    @Override
    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public int getOwnerAccId() {
        return owneraccount;
    }

    @Override
    public String getDescription() {
        if (des == null) {
            return "";
        }
        return des;
    }

    public boolean isVisitor(MapleCharacter visitor) {
        for (MapleCharacter chr : visitors) {
            if (chr == visitor) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Pair<Byte, MapleCharacter>> getVisitors() {

        List<Pair<Byte, MapleCharacter>> chrz = new LinkedList<Pair<Byte, MapleCharacter>>();

        for (byte i = 0; i < 6; i++) { //include owner or no
            if (visitors[i] != null) {
                chrz.add(new Pair<Byte, MapleCharacter>((byte) (i + 1), visitors[i])); //TODO: double check
            }
        }

        return chrz;
    }

    @Override
    public List<MaplePlayerShopItem> getItems() {
        return items;
    }

    @Override
    public void addItem(MaplePlayerShopItem item) {
        item.setSlot(itemSlot);
        items.add(item);
        saveItem(item);
        itemSlot++;
    }

    @Override
    public boolean removeItem(int item) {
        return false;
    }

    @Override
    public void removeFromSlot(int slot) {
        items.remove(slot);
    }

    public MaplePlayerShopItem getItemFromSlot(int slot) {
        return items.get(slot);
    }

    @Override
    public byte getFreeSlot() {
        for (int i = 0; i < visitors.length; i++) {
            if (visitors[i] == null) {
                return (byte) (i + 1);
            }
        }
        return -1;
    }

    @Override
    public int getItemId() {
        return itemId;
    }

    @Override
    public boolean isOwner(MapleCharacter chr) {
        return chr == getStoreOwner();
    }

    @Override
    public String getPassword() {
        if (pass == null) {
            return "";
        }
        return pass;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SHOP;
    }

    public MapleCharacter getMCOwnerWorld() {
        int ourChannel = World.Find.findChannel(ownerId);
        if (ourChannel <= 0) {
            return null;
        }
        return ChannelServer.getInstance(ourChannel).getPlayerStorage().getCharacterById(ownerId);
    }

    public MapleCharacter getMCOwnerChannel() {
        return ChannelServer.getInstance(channel).getPlayerStorage().getCharacterById(ownerId);
    }

    public MapleCharacter getMCOwner() {
        return getMap().getCharacterById(ownerId);
    }

    public MapleMap getMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(map);
    }

    @Override
    public int getGameType() {
        if (getShopType() == IMaplePlayerShop.HIRED_MERCHANT) { //hiredmerch
            return 5;
        } else if (getShopType() == IMaplePlayerShop.PLAYER_SHOP) { //shop lol
            return 4;
        } else if (getShopType() == IMaplePlayerShop.OMOK) { //omok
            return 1;
        } else if (getShopType() == IMaplePlayerShop.MATCH_CARD) { //matchcard
            return 2;
        }
        return 0;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void setAvailable(boolean b) {
        this.available = b;
    }

    @Override
    public List<BoughtItem> getBoughtItems() {
        return bought;
    }

    public static final class BoughtItem {

        public int id;
        public int quantity;
        public int totalPrice;
        public String buyer;

        public BoughtItem(final int id, final int quantity, final int totalPrice, final String buyer) {
            this.id = id;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.buyer = buyer;
        }

        public String getBuyer() {
            return buyer;
        }

        public int getItemId() {
            return id;
        }

        public int getQuantity() {
            return quantity;
        }

        public long getMesos() {
            return totalPrice;
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
    }

    @Override
    public void sendSpawnData(MapleClient client) {
    }
    
    public void saveItem(MaplePlayerShopItem shopItem) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO `inventoryshop` VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                Item item = shopItem.getItem();
                ps.setInt(1, getOwnerId());
                ps.setInt(2, item.getItemId());
                ps.setString(3, item.getItemName(item.getItemId()));
                MapleInventoryType type = GameConstants.getInventoryType(item.getItemId());

                ps.setInt(4, type.getType());
                ps.setInt(5, shopItem.getSlot());
                ps.setInt(6, item.getQuantity());
                ps.setString(7, item.getOwner());
                if (item.getPet() != null) { //expensif?
                    ps.setInt(8, Math.max(item.getUniqueId(), item.getPet().getUniqueId()));
                } else {
                    ps.setInt(8, item.getUniqueId());
                }
                ps.setShort(9, item.getFlag());
                ps.setLong(10, item.getExpiration());
                if (type == MapleInventoryType.EQUIP) {
                    Equip equip = (Equip) item;
                    ps.setInt(11, equip.getUpgradeSlots());
                    ps.setInt(12, equip.getLevel());
                    ps.setInt(13, equip.getStr());
                    ps.setInt(14, equip.getDex());
                    ps.setInt(15, equip.getInt());
                    ps.setInt(16, equip.getLuk());
                    ps.setInt(17, equip.getHp());
                    ps.setInt(18, equip.getHpr());
                    ps.setInt(19, equip.getMp());
                    ps.setInt(20, equip.getMpr());
                    ps.setInt(21, equip.getWatk());
                    ps.setInt(22, equip.getMatk());
                    ps.setInt(23, equip.getWdef());
                    ps.setInt(24, equip.getMdef());
                    ps.setInt(25, equip.getAcc());
                    ps.setInt(26, equip.getAvoid());
                    ps.setInt(27, equip.getHands());
                    ps.setInt(28, equip.getSpeed());
                    ps.setInt(29, equip.getJump());
                    ps.setInt(30, equip.getViciousHammer());
                    ps.setInt(31, equip.getItemEXP());
                    ps.setInt(32, equip.getDurability());
                    ps.setInt(33, equip.getEnhance());
                    ps.setInt(34, equip.getPotential1());
                    ps.setInt(35, equip.getPotential2());
                    ps.setInt(36, equip.getPotential3());
                    ps.setInt(37, equip.getPotential4());
                    ps.setInt(38, equip.getPotential5());
                    ps.setInt(39, equip.getSocket1());
                    ps.setInt(40, equip.getSocket2());
                    ps.setInt(41, equip.getSocket3());
                    ps.setInt(42, equip.getIncSkill());
                    ps.setInt(43, equip.getCharmEXP());
                    ps.setInt(44, equip.getPVPDamage());
                    ps.setInt(45, equip.getPower());
                    ps.setInt(46, equip.getOverPower());
                    ps.setInt(47, equip.getTotalDamage());
                    ps.setInt(48, equip.getBossDamage());
                    ps.setInt(49, equip.getIED());
                    ps.setInt(50, equip.getCritDamage());
                    ps.setInt(51, equip.getAllStat());
                    ps.setLong(52, equip.getOStr());
                    ps.setLong(53, equip.getODex());
                    ps.setLong(54, equip.getOInt());
                    ps.setLong(55, equip.getOLuk());
                    ps.setLong(56, equip.getOAtk());
                    ps.setLong(57, equip.getOMatk());
                    ps.setLong(58, equip.getODef());
                    ps.setLong(59, equip.getOMdef());
                } else {
                    for (int i = 11; i < 60; i++) {
                        ps.setInt(i, 0);
                    }
                }
                ps.executeUpdate();
            }

        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void removeAllItems(MapleCharacter owner) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM inventoryshop WHERE charid = ?")) {
                ps.setInt(1, owner.getId());
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void removeAllItems() {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM inventoryshop WHERE charid = ?")) {
                ps.setInt(1, getOwnerId());
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void removeItem(MaplePlayerShopItem shopItem) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM inventoryshop WHERE charid = ? and position = ?")) {
                ps.setInt(1, getOwnerId());
                ps.setInt(2, shopItem.getSlot());

            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void updateItem(MaplePlayerShopItem shopItem) {
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE inventoryshop SET quantity = ? WHERE charid = ? AND position = ?")) {
                ps.setInt(1, shopItem.getItem().getQuantity());
                ps.setInt(2, getOwnerId());
                ps.setInt(3, shopItem.getSlot());
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
