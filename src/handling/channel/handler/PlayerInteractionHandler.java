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

import java.util.Arrays;

import client.inventory.Item;
import client.inventory.ItemFlag;
import constants.GameConstants;
import client.MapleClient;
import client.MapleCharacter;
import client.inventory.Equip;
import client.inventory.MapleInventoryType;
import handling.world.World;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleTrade;
import server.Randomizer;
import server.maps.FieldLimitType;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import server.shops.MaplePlayerShop;
import server.shops.MaplePlayerShopItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.shops.MapleMiniGame;
import tools.StringUtil;
import tools.packet.PlayerShopPacket;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CWvsContext;

public class PlayerInteractionHandler {

    public enum Interaction {

        CREATE(6),
        INVITE_TRADE(11),
        DENY_TRADE(12),
        VISIT(9),
        HIRED_MERCHANT_MAINTENANCE(21), // ?
        CHAT(14),
        EXIT(18),
        OPEN(16),
        SET_ITEMS(0),
        SET_MESO(1),
        CONFIRM_TRADE(2),
        PLAYER_SHOP_ADD_ITEM(42),
        PLAYER_SHOP_REMOVE_ITEM(50),
        PLAYER_SHOP_BAN(51),
        BUY_ITEM_PLAYER_SHOP(43), // (BUY_ITEM_STORE(22) // unknown: 53
        ADD_ITEM(23),
        BUY_ITEM_HIREDMERCHANT(24), // ? was 26
        UPDATE_PLAYERSHOP(26),
        REMOVE_ITEM(30),
        MAINTANCE_OFF(31), // ?
        MAINTANCE_ORGANISE(32), // ?
        CLOSE_MERCHANT(33), // fix this
        TAKE_MESOS(35), // ?
        ADMIN_STORE_NAMECHANGE(37), // ?
        VIEW_MERCHANT_VISITOR(38),
        VIEW_MERCHANT_BLACKLIST(39),
        MERCHANT_BLACKLIST_ADD(40),
        MERCHANT_BLACKLIST_REMOVE(41),
        REQUEST_TIE(56),
        ANSWER_TIE(57),
        GIVE_UP(58),
        REQUEST_REDO(60), // code this
        ANSWER_REDO(61), // code this
        EXIT_AFTER_GAME(62),
        CANCEL_EXIT(63),
        READY(64),
        UN_READY(65),
        EXPEL(66),
        START(67),
        SKIP(69),
        MOVE_OMOK(70),
        SELECT_CARD(74); // working (74), possibly 73
        public int action;

        private Interaction(int action) {
            this.action = action;
        }

        public static Interaction getByAction(int i) {
            for (Interaction s : Interaction.values()) {
                if (s.action == i) {
                    return s;
                }
            }
            return null;
        }
    }

    public static final void PlayerInteraction(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        if (c == null) {
            return;
        }
        MapleCharacter chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        if (!chr.checkPlayer()) {
            return;
        }

        //System.out.println("player interaction.." + slea.toString());
        byte a = slea.readByte();
        final Interaction action = Interaction.getByAction(a);
        if (action == null) {
            //System.out.println("action null" + a);
            return;
        }
        try {
            //System.out.println("action: " + a);
            c.getPlayer().setScrolledPosition((short) 0);
            //System.out.println("Action + " + action);
            switch (action) { // Mode
                case CREATE: {
                    if (c.getPlayer().isJailed()) {
                        c.getPlayer().dropMessage(6, "You are currently jailed");
                        return;
                    }
                    if (chr.getPlayerShop() != null || c.getChannelServer().isShutdown() || chr.hasBlockedInventory()) {
                        c.announce(CWvsContext.enableActions());
                        return;
                    }
                    final byte createType = slea.readByte();
                    if (createType == 3) { // trade
                        if (c.getPlayer().run) {
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        MapleTrade.startTrade(chr);
                    } else if (createType == 1 || createType == 2 || createType == 4 || createType == 5) { // shop
                        if (chr.getTrade() != null) {
                            chr.dropMessage(1, "You may not use shop while trading.");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        if (chr.getShop() != null) {
                            chr.dropMessage(1, "You may not use shop while trading.");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        if (chr.getEventInstance() != null) {
                            chr.dropMessage(1, "You may not use shop inside instances.");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        if (chr.getMap().shopPlace(chr.getPosition(), 20000)) {
                            chr.dropMessage(1, "You may not establish a store here.");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        if (createType == 1 || createType == 2) {
                            if (FieldLimitType.Minigames.check(chr.getMap().getFieldLimit()) || chr.getMap().allowPersonalShop()) {
                                chr.dropMessage(1, "You may not use minigames here.");
                                c.announce(CWvsContext.enableActions());
                                return;
                            }
                        }
                        final String desc = slea.readMapleAsciiString();
                        String pass = "";
                        if (slea.readByte() > 0) {
                            pass = slea.readMapleAsciiString();
                        }
                        if (c.getPlayer().getMapId() < 870000001 || c.getPlayer().getMapId() > 870000004) {
                            chr.dropMessage(1, "You may not use minigames here.");
                            return;
                        }
                        if (createType == 1 || createType == 2) {
                            chr.dropMessage(1, "You may not use minigames here.");
                            c.announce(CWvsContext.enableActions());
                        } else if (chr.getMap().allowPersonalShop()) {
                            Item shop = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) slea.readShort());
                            if (shop == null || shop.getQuantity() <= 0 || shop.getItemId() != slea.readInt()) {
                                return;
                            }
                            if (createType == 4) {
                                if (chr.getShopItems().isEmpty()) {
                                    MaplePlayerShop mps = new MaplePlayerShop(chr, shop.getItemId(), desc);
                                    chr.setPlayerShop(mps);
                                    chr.getMap().addMapObject(mps);
                                    c.getSession().write(PlayerShopPacket.getPlayerStore(chr, true));
                                    //System.out.println("[Player Shop] Player: " + chr.getName() + " created shop in FM Room: " + (chr.getMapId() - 910000000));
                                } else {
                                    chr.dropMessage(1, "You have unclaimed items from prev shop. Please claim them before you can place a shop.");
                                }
                                //c.announce(PlayerShopPacket.shopVisitorLeave((byte) 1));
                                //chr.dropMessage(-2, "System : Use @add slot price to put in forbidden equips. Command only avaible while shop is closed.");
                            }
                        }
                    }
                    break;
                }
                case INVITE_TRADE: {
                    if (chr.getEventInstance() != null) {
                        c.announce(CWvsContext.enableActions());
                        chr.dropMessage(1, "Trading has been disabled in instances/dungeons.");
                        return;
                    } else {
                        if (chr.getMap() == null || chr.getClient().getCMS() != null || chr.getShop() != null || chr.run) {
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        MapleCharacter chrr = chr.getMap().getCharacterById(slea.readInt());
                        if (chrr == null || c.getChannelServer().isShutdown() || chrr.hasBlockedInventory() || chrr.getClient().getCMS() != null || chrr.getShop() != null || chrr.run) {
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        MapleTrade.inviteTrade(chr, chrr);
                        break;
                    }
                }
                case DENY_TRADE: {
                    MapleTrade.declineTrade(chr);
                    break;
                }
                case VISIT: {
                    if (c.getChannelServer().isShutdown()) {
                        c.getSession().write(CWvsContext.enableActions());
                        return;
                    }
                    if (chr.getTrade() != null && chr.getTrade().getPartner() != null && !chr.getTrade().inTrade()) {
                        MapleTrade.visitTrade(chr, chr.getTrade().getPartner().getChr());
                    } else if (chr.getMap() != null && chr.getTrade() == null) {
                        final int obid = slea.readInt();
                        MapleMapObject ob = chr.getMap().getMerchantByOid(obid);
                        if (ob == null) {
                            ob = chr.getMap().getPlayerShopByOid(obid);
                        }

                        if (ob instanceof MaplePlayerShop && chr.getPlayerShop() == null) {
                            final MaplePlayerShop ips = (MaplePlayerShop) ob;

                            if (ips instanceof MaplePlayerShop && ((MaplePlayerShop) ips).isBanned(chr.getName())) {
                                chr.dropMessage(1, "You have been banned from this store.");
                            } else {
                                if (ips.getFreeSlot() < 0 || ips.getVisitorSlot(chr) > -1 || !ips.isOpen() || !ips.isAvailable()) {
                                    c.getSession().write(PlayerShopPacket.getMiniGameFull());
                                } else {
                                    if (slea.available() > 0 && slea.readByte() > 0) { //a password has been entered
                                        String pass = slea.readMapleAsciiString();
                                        if (!pass.equals(ips.getPassword())) {
                                            c.getPlayer().dropMessage(1, "The password you entered is incorrect.");
                                            return;
                                        }
                                    } else if (ips.getPassword().length() > 0) {
                                        c.getPlayer().dropMessage(1, "The password you entered is incorrect.");
                                        return;
                                    }
                                    ips.addVisitor(chr);
                                    chr.setPlayerShop(ips);
                                    c.getSession().write(PlayerShopPacket.getPlayerStore(chr, false));
                                    if (ips.getMCOwner().shoplock) {
                                        chr.dropShopMessage("Remaining lock time:", ips.getVisitorSlot(chr));
                                        chr.dropShopMessage(StringUtil.secondsToString(ips.getMCOwner().shoptime), ips.getVisitorSlot(chr));
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
                case HIRED_MERCHANT_MAINTENANCE: {
                    c.announce(CWvsContext.enableActions());
                    break;
                }
                case CHAT: {
                    chr.updateTick(slea.readInt());
                    final String message = slea.readMapleAsciiString();

                    if (chr.getTrade() != null) {
                        chr.getTrade().chat(message);
                    } else if (chr.getPlayerShop() != null) {
                        final MaplePlayerShop ips = chr.getPlayerShop();
                        if (ips != null && ips.getVisitorSlot(chr) == -1) {
                            if (ips.isAvailable()) {
                                if (message.toLowerCase().startsWith("@say ")) {
                                    String msg = message.substring(5);
                                    World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, "[" + chr.getName() + "'s Shop] " + msg));
                                }
                                if (message.toLowerCase().startsWith("@clearban")) {
                                    ips.clearBan();
                                    chr.dropMessage(1, "All bans have been cleared");
                                }
                                if (message.toLowerCase().startsWith("@lock")) {
                                    String[] splitted = message.split(" ");
                                    if (splitted.length >= 2) {
                                        long time = 0;
                                        try {
                                            time = Long.parseLong(splitted[1]);
                                        } catch (NumberFormatException nfe) {
                                            chr.dropMessage(1, "Lock command must be a Number IE: @lock 24");
                                        }
                                        if (time > 0) {
                                            if (time > 144) {
                                                chr.dropMessage(1, "Time must be set under 144 hours.");
                                            } else {
                                                chr.shoptime = (time * 3600);
                                                chr.dropMessage(1, "Shop is now locked for " + time + " hours.");
                                                System.out.println("Player: " + chr.getName() + " has set shop lock to time: " + time + " hours.");
                                            }
                                        } else {
                                            chr.dropMessage(1, "Time must be set over 0 hours.");
                                        }
                                    } else {
                                        chr.dropMessage(1, "Lock command must be a Number IE: @lock 24");
                                    }
                                }
                            } else {
                                chr.dropMessage(1, "System : command not available while shop is closed.");
                            }
                        }
                        ips.broadcast(PlayerShopPacket.shopChat(chr.getName() + " : " + message, ips.getVisitorSlot(chr)));
                        if (chr.getClient().isMonitored()) { //Broadcast info even if it was a command.
                            World.Broadcast.broadcastGMMessage(CWvsContext.serverNotice(6, chr.getName() + " said in " + ips.getOwnerName() + " shop : " + message));
                        }
                    }
                    break;
                }
                case EXIT: {
                    if (chr.getTrade() != null) {
                        MapleTrade.cancelTrade(chr.getTrade(), chr.getClient(), chr);
                    } else {
                        final MaplePlayerShop ips = chr.getPlayerShop();
                        if (ips == null) { //should be null anyway for owners of hired merchants (maintenance_off)
                            return;
                        }
                        if (ips.isOwner(chr) && ips.getShopType() != 1) {
                            ips.closeShop(false); //how to return the items?
                        } else {
                            ips.removeVisitor(chr, 0);
                        }
                        chr.setPlayerShop(null);
                    }
                    break;
                }
                case OPEN: {
                    // c.getPlayer().haveItem(mode, 1, false, true)
                    final MaplePlayerShop shop = chr.getPlayerShop();
                    if (shop != null && shop.isOwner(chr) && shop.getShopType() < 3 && !shop.isAvailable()) {
                        if (c.getChannelServer().isShutdown()) {
                            chr.dropMessage(1, "The server is about to shut down.");
                            c.announce(CWvsContext.enableActions());
                            shop.closeShop(false);
                            return;
                        }
                        if (chr.getTrade() != null) {
                            chr.dropMessage(1, "You may not use shop while trading.");
                            c.announce(CWvsContext.enableActions());
                            shop.closeShop(false);
                            return;
                        }
                        if (chr.getShop() != null) {
                            chr.dropMessage(1, "You may not use shop while trading.");
                            c.announce(CWvsContext.enableActions());
                            shop.closeShop(false);
                            return;
                        }
                        if (chr.getEventInstance() != null) {
                            chr.dropMessage(1, "You may not use shop inside instances.");
                            c.announce(CWvsContext.enableActions());
                            shop.closeShop(false);
                            return;
                        }
                        if (shop.getShopType() == 1 && HiredMerchantHandler.UseHiredMerchant(chr.getClient(), false)) {
                            return;
                            /*
                                 final HiredMerchant merchant = (HiredMerchant) shop;
                                 merchant.setStoreid(c.getChannelServer().addMerchant(merchant));
                                 merchant.setOpen(true);
                                 merchant.setAvailable(true);
                                 chr.getMap().broadcastMessage(PlayerShopPacket.spawnHiredMerchant(merchant));
                                 chr.setPlayerShop(null);
                             */

                        } else if (shop.getShopType() == 2) {
                            shop.setOpen(true);
                            shop.setAvailable(true);
                            //System.out.println("Player: " + chr.getName() + " Opened a shop");
                            shop.update();
                            chr.saveItems();
                            c.getChannelServer().dropMessage(chr.getName() + " has opened Shop: " + shop.getDescription() + " on Map: " + chr.getMap().getStreetName());
                        }
                    } else {
                        chr.dropMessage(1, "error with shop?.");
                    }

                    break;
                }
                case SET_ITEMS: {
                    if (c.getPlayer().getEventInstance() != null) {
                        c.getPlayer().dropMessage(1, "Trades not allowed in instances.");
                        c.announce(CWvsContext.enableActions());
                        return;
                    }
                    if (c.getPlayer().isStorageOpened()) {
                        c.getPlayer().dropMessage(1, "Trades not allowed while storage is opened");
                        c.announce(CWvsContext.enableActions());
                        return;
                    }
                    if (c.getPlayer().getShop() != null) {
                        c.getPlayer().dropMessage(1, "Trades not allowed while a store is opened");
                        c.announce(CWvsContext.enableActions());
                        return;
                    }
                    final MapleInventoryType ivType = MapleInventoryType.getByType(slea.readByte());
                    final Item item = chr.getInventory(ivType).getItem((byte) slea.readShort());
                    final short quantity = slea.readShort();
                    final byte targetSlot = slea.readByte();
                    final boolean hasItem = quantity <= item.getQuantity();
                    if (!hasItem) {
                        c.getPlayer().dropMessage(1, "Wtf you doing bruh");
                        c.announce(CWvsContext.enableActions());
                        return;
                    }
                    if (chr.getTrade() != null && item != null) {
                        if (MapleItemInformationProvider.getInstance().isBanned(item.getItemId())) {
                            c.getPlayer().dropMessage(1, "This item is un-tradable");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        if (ivType == MapleInventoryType.CASH) {
                            c.getPlayer().dropMessage(1, "Cash Tab Items cannot be traded");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        chr.getTrade().setItems(c, item, targetSlot, quantity);
                    }
                    break;
                }
                case SET_MESO: {
                    final MapleTrade trade = chr.getTrade();
                    if (trade != null) {
                        trade.setMeso(slea.readInt());
                    }
                    break;
                }
                case PLAYER_SHOP_ADD_ITEM:
                case ADD_ITEM: {
                    try {
                        if (chr.getTrade() != null) {
                            c.getPlayer().dropMessage(1, "Cannot place items while trade is opened.");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        final MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
                        final byte slot = (byte) slea.readShort();
                        short amount = slea.readShort(); // How many items per bundle
                        short b = slea.readShort();
                        final short bundle = 1; // number of bundles
                        final int price = slea.readInt();
                        //System.out.println("b: " + b);
                        //System.out.println("amount: " + amount);
                        //System.out.println("per bundles: " + bundle);
                        //System.out.println("price: " + price);
                        Item item = c.getPlayer().getInventory(type).getItem(slot);
                        if (item == null || item.getInventoryType() == null) {
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        if (MapleItemInformationProvider.getInstance().isBanned(item.getItemId())) {
                            c.getPlayer().dropMessage(1, "This item is un-tradable");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        /*
                        if (GameConstants.isCurrency(item.getItemId())) {
                            c.getPlayer().dropMessage(1, "Cannot sell game currencies for other currenices.");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                         */
                        if (bundle > 1) {
                            c.getPlayer().dropMessage(1, "Bundles are currently disabled.");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        if (!c.getPlayer().haveItem(item.getItemId(), (bundle * amount), false, true)) {
                            c.getPlayer().dropMessage(1, "You dont have enough of this item to sell.");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        if (type == MapleInventoryType.CASH) {
                            c.getPlayer().dropMessage(1, "Cash Tab Items cannot be sold");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        if (type == MapleInventoryType.USE || type == MapleInventoryType.ETC || type == MapleInventoryType.SETUP) {
                            c.getPlayer().dropMessage(1, "Cannot Sell Etc or Use Items.");
                            c.announce(CWvsContext.enableActions());
                            return;
                        }
                        if (item.getInventoryType() == MapleInventoryType.EQUIP) {
                            amount = 1;
                            Equip eqp = (Equip) item;
                        }
                        if (price <= 0 || amount <= 0 || bundle <= 0) {
                            return;
                        }
                        final MaplePlayerShop shop = chr.getPlayerShop();

                        if (shop == null || !shop.isOwner(chr)) {
                            return;
                        }
                        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        if (item != null) {
                            if (amount > 30000 || amount <= 0) { //This is the better way to check.
                                c.getPlayer().dropMessage(1, "You cant sell more than 30000 items PER slot.");
                                c.announce(CWvsContext.enableActions());
                                return;
                            }
                            if (GameConstants.getShopCurrency(shop.getItemId()) != 4310999) {
                                if (price * amount > 999999999) {
                                    c.getPlayer().dropMessage(1, "Price for amount of items is too much.");
                                    c.announce(CWvsContext.enableActions());
                                    return;
                                }
                            }
                            if (item.getItemId() == GameConstants.getShopCurrency(shop.getItemId())) {
                                c.getPlayer().dropMessage(1, "Same Currency Coins cannot be sold in this shop.");
                                c.announce(CWvsContext.enableActions());
                                return;
                            }
                            if (item.getQuantity() >= amount) {
                                if (price < 1) {
                                    c.getPlayer().dropMessage(1, "You cant give away items for free lol.");
                                    c.announce(CWvsContext.enableActions());
                                    return;
                                }
                                Item sellItem = item.copy(amount);
                                if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId()) || item.getInventoryType() == MapleInventoryType.EQUIP) {
                                    amount = item.getQuantity();
                                    sellItem.setQuantity((short) 1);
                                }
                                if (MapleInventoryManipulator.removeFromSlot(c, type, slot, amount, true)) {
                                    shop.addItem(new MaplePlayerShopItem(sellItem, sellItem.getQuantity(), (short) 1, price, slot));
                                } else {
                                    c.getPlayer().dropMessage(1, "You dont have that many items to sell.");
                                    c.announce(CWvsContext.enableActions());
                                    return;
                                }
                                //System.out.println("[Player Shop] Player: " + chr.getName() + " placed " + sellItem.getItemName(sellItem.getItemId()) + " (x" + bundles_perbundle + ") in his shop.");
                                c.announce(PlayerShopPacket.shopItemUpdate(shop));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case CONFIRM_TRADE:
                case BUY_ITEM_PLAYER_SHOP:
                case BUY_ITEM_HIREDMERCHANT: { // Buy and Merchant buy
                    if (chr.getTrade() != null) {
                        if (!chr.isStorageOpened()) {
                            MapleTrade.completeTrade(chr);
                        }
                        c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                        return;
                    }
                    final int item = slea.readByte();
                    final short quantity = slea.readShort();
                    if (quantity > 0) {
                        final MaplePlayerShop shop = chr.getPlayerShop();
                        if (shop != null && shop.isVisitor(chr)) {
                            int price = shop.getItemFromSlot(item).getQuantity();
                            if (price > 0) {
                                if (quantity > price) {
                                    System.out.println(c.getPlayer().getName() + " trying to buy illegal amount of items: Amount: " + quantity + " - Shop Item Amount: " + shop.getItemFromSlot(item).getQuantity());
                                } else {
                                    if (chr.getTrade() != null || chr.isStorageOpened()) {
                                        c.getPlayer().dropMessage(1, "Cannot buy items while trade is opened.");
                                    } else {
                                        if (shop.buy(c, item, quantity)) {
                                            shop.broadcast(PlayerShopPacket.shopItemUpdate(shop));
                                        }
                                    }
                                }
                            }
                        }
                    }
                    c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                    break;
                }
                case PLAYER_SHOP_REMOVE_ITEM:
                case REMOVE_ITEM: {
                    byte type = slea.readByte(); // ?
                    //action
                    //System.out.println("type: " + type);
                    //System.out.println("action: " + action);
                    int slot = slea.readShort(); //0
                    final MaplePlayerShop shop = chr.getPlayerShop();

                    if (shop == null || !shop.isOwner(chr) || shop.getItems().size() <= 0 || shop.getItems().size() <= slot || slot < 0) {
                        System.out.println(chr.getName() + " possible dupe attempts with item shop: Null Items");
                        return;
                    }
                    final MaplePlayerShopItem item = shop.getItems().get(slot);

                    if (item != null) {
                        if (item.bundles == 1) {
                            Item item_get = item.item.copy();
                            //long check = item.bundles * item.item.getQuantity();
                            if (item.bundles > 1) {
                                System.out.println(chr.getName() + " possible dupe attempts with item shop: Item: " + item_get.getItemName(item_get.getItemId()) + " - Bundles: " + item.bundles);
                                return;
                            }
                            //item_get.setQuantity((short) check);
                            if (MapleInventoryManipulator.checkSpace(c, item_get.getItemId(), item_get.getQuantity(), item_get.getOwner())) {
                                MapleInventoryManipulator.addFromDrop(c, item_get, false);
                                item.bundles = 0;
                                shop.removeFromSlot(slot);
                            }
                        }
                    }
                    c.announce(PlayerShopPacket.shopItemUpdate(shop));
                    break;
                }
                case MAINTANCE_OFF: {
                    final IMaplePlayerShop shop = chr.getPlayerShop();
                    if (shop != null && shop instanceof HiredMerchant && shop.isOwner(chr) && shop.isAvailable()) {
                        shop.setOpen(true);
                        shop.removeAllVisitors(-1, -1);
                    }
                    break;
                }
                case MAINTANCE_ORGANISE: {
                    final MaplePlayerShop imps = chr.getPlayerShop();
                    if (imps != null && imps.isOwner(chr)) {
                        for (int i = 0; i < imps.getItems().size(); i++) {
                            if (imps.getItems().get(i).bundles == 0) {
                                imps.getItems().remove(i);
                            }
                        }
                        if (chr.getMeso() + imps.getMeso() > 0) {
                            chr.gainMeso(imps.getMeso(), false);
                            imps.setMeso(0);
                        }
                        c.announce(PlayerShopPacket.shopItemUpdate(imps));
                    }
                    break;
                }
                case CLOSE_MERCHANT: {
                    final IMaplePlayerShop merchant = chr.getPlayerShop();
                    if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr) && merchant.isAvailable()) {
                        c.announce(CWvsContext.serverNotice(1, "Please visit Fredrick for your items."));
                        c.announce(CWvsContext.enableActions());
                        merchant.removeAllVisitors(-1, -1);
                        chr.setPlayerShop(null);
                        merchant.closeShop(false);
                    }
                    break;
                }
                case ADMIN_STORE_NAMECHANGE: { // Changing store name, only Admin
                    // 01 00 00 00
                    break;
                }
                case VIEW_MERCHANT_VISITOR: {
                    final IMaplePlayerShop merchant = chr.getPlayerShop();
                    if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
                        ((HiredMerchant) merchant).sendVisitor(c);
                    }
                    break;
                }
                case VIEW_MERCHANT_BLACKLIST: {
                    final IMaplePlayerShop merchant = chr.getPlayerShop();
                    if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
                        ((HiredMerchant) merchant).sendBlackList(c);
                    }
                    break;
                }
                case MERCHANT_BLACKLIST_ADD: {
                    final IMaplePlayerShop merchant = chr.getPlayerShop();
                    if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
                        ((HiredMerchant) merchant).addBlackList(slea.readMapleAsciiString());
                    }
                    break;
                }
                case MERCHANT_BLACKLIST_REMOVE: {
                    final IMaplePlayerShop merchant = chr.getPlayerShop();
                    if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
                        ((HiredMerchant) merchant).removeBlackList(slea.readMapleAsciiString());
                    }
                    break;
                }
                case GIVE_UP: {
                    final IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips != null && ips instanceof MapleMiniGame) {
                        MapleMiniGame game = (MapleMiniGame) ips;
                        if (game.isOpen()) {
                            break;
                        }
                        game.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game, 0, game.getVisitorSlot(chr)));
                        game.nextLoser();
                        game.setOpen(true);
                        game.update();
                        game.checkExitAfterGame();
                    }
                    break;
                }
                case EXPEL: {
                    System.out.println("expell??");
                    final MaplePlayerShop ips = chr.getPlayerShop();
                    if (ips != null) {
                        ips.removeAllVisitors(3, 1); //no msg
                    }
                    break;
                }
                case PLAYER_SHOP_BAN: {
                    //System.out.println(slea);
                    final MaplePlayerShop shop = chr.getPlayerShop();
                    if (shop != null && shop.isOwner(chr)) {
                        slea.readByte();
                        ((MaplePlayerShop) shop).banPlayer(slea.readMapleAsciiString());
                    }
                    break;
                }
                case READY:
                case UN_READY: {
                    final IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips != null && ips instanceof MapleMiniGame) {
                        MapleMiniGame game = (MapleMiniGame) ips;
                        if (!game.isOwner(chr) && game.isOpen()) {
                            game.setReady(game.getVisitorSlot(chr));
                            game.broadcastToVisitors(PlayerShopPacket.getMiniGameReady(game.isReady(game.getVisitorSlot(chr))));
                        }
                    }
                    break;
                }
                case START: {
                    final IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips != null && ips instanceof MapleMiniGame) {
                        MapleMiniGame game = (MapleMiniGame) ips;
                        if (game.isOwner(chr) && game.isOpen()) {
                            for (int i = 1; i < ips.getSize(); i++) {
                                if (!game.isReady(i)) {
                                    return;
                                }
                            }
                            game.setGameType();
                            game.shuffleList();
                            if (game.getGameType() == 1) {
                                game.broadcastToVisitors(PlayerShopPacket.getMiniGameStart(game.getLoser()));
                            } else {
                                game.broadcastToVisitors(PlayerShopPacket.getMatchCardStart(game, game.getLoser()));
                            }
                            game.setOpen(false);
                            game.update();
                        }
                    }
                    break;
                }
                case REQUEST_TIE: {
                    final IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips != null && ips instanceof MapleMiniGame) {
                        MapleMiniGame game = (MapleMiniGame) ips;
                        if (game.isOpen()) {
                            break;
                        }
                        if (game.isOwner(chr)) {
                            game.broadcastToVisitors(PlayerShopPacket.getMiniGameRequestTie());
                        } else {
                            game.getMCOwner().getClient().announce(PlayerShopPacket.getMiniGameRequestTie());
                        }
                        game.setRequestedTie(game.getVisitorSlot(chr));
                    }
                    break;
                }
                case ANSWER_TIE: {
                    final IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips != null && ips instanceof MapleMiniGame) {
                        MapleMiniGame game = (MapleMiniGame) ips;
                        if (game.isOpen()) {
                            break;
                        }
                        if (game.getRequestedTie() > -1 && game.getRequestedTie() != game.getVisitorSlot(chr)) {
                            if (slea.readByte() > 0) {
                                game.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game, 1, game.getRequestedTie()));
                                game.nextLoser();
                                game.setOpen(true);
                                game.update();
                                game.checkExitAfterGame();
                            } else {
                                game.broadcastToVisitors(PlayerShopPacket.getMiniGameDenyTie());
                            }
                            game.setRequestedTie(-1);
                        }
                    }
                    break;
                }
                case SKIP: {
                    final IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips != null && ips instanceof MapleMiniGame) {
                        MapleMiniGame game = (MapleMiniGame) ips;
                        if (game.isOpen()) {
                            break;
                        }
                        if (game.getLoser() != ips.getVisitorSlot(chr)) {
                            ips.broadcastToVisitors(PlayerShopPacket.shopChat("Turn could not be skipped by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + ips.getVisitorSlot(chr), ips.getVisitorSlot(chr)));
                            return;
                        }
                        ips.broadcastToVisitors(PlayerShopPacket.getMiniGameSkip(ips.getVisitorSlot(chr)));
                        game.nextLoser();
                    }
                    break;
                }
                case MOVE_OMOK: {
                    final IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips != null && ips instanceof MapleMiniGame) {
                        MapleMiniGame game = (MapleMiniGame) ips;
                        if (game.isOpen()) {
                            break;
                        }
                        if (game.getLoser() != game.getVisitorSlot(chr)) {
                            game.broadcastToVisitors(PlayerShopPacket.shopChat("Omok could not be placed by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr), game.getVisitorSlot(chr)));
                            return;
                        }
                        game.setPiece(slea.readInt(), slea.readInt(), slea.readByte(), chr);
                    }
                    break;
                }
                case SELECT_CARD: {
                    final IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips != null && ips instanceof MapleMiniGame) {
                        MapleMiniGame game = (MapleMiniGame) ips;
                        if (game.isOpen()) {
                            break;
                        }
                        if (game.getLoser() != game.getVisitorSlot(chr)) {
                            game.broadcastToVisitors(PlayerShopPacket.shopChat("Card could not be placed by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr), game.getVisitorSlot(chr)));
                            return;
                        }
                        if (slea.readByte() != game.getTurn()) {
                            game.broadcastToVisitors(PlayerShopPacket.shopChat("Omok could not be placed by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr) + " Turn: " + game.getTurn(), game.getVisitorSlot(chr)));
                            return;
                        }
                        final int slot = slea.readByte();
                        final int turn = game.getTurn();
                        final int fs = game.getFirstSlot();
                        if (turn == 1) {
                            game.setFirstSlot(slot);
                            if (game.isOwner(chr)) {
                                game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, turn));
                            } else {
                                game.getMCOwner().getClient().announce(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, turn));
                            }
                            game.setTurn(0); //2nd turn nao
                            return;
                        } else if (fs > 0 && game.getCardId(fs + 1) == game.getCardId(slot + 1)) {
                            game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, game.isOwner(chr) ? 2 : 3));
                            game.setPoints(game.getVisitorSlot(chr)); //correct.. so still same loser. diff turn tho
                        } else {
                            game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, game.isOwner(chr) ? 0 : 1));
                            game.nextLoser();//wrong haha

                        }
                        game.setTurn(1);
                        game.setFirstSlot(0);

                    }
                    break;
                }
                case EXIT_AFTER_GAME:
                case CANCEL_EXIT: {
                    final IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips != null && ips instanceof MapleMiniGame) {
                        MapleMiniGame game = (MapleMiniGame) ips;
                        if (game.isOpen()) {
                            break;
                        }
                        game.setExitAfter(chr);
                        game.broadcastToVisitors(PlayerShopPacket.getMiniGameExitAfter(game.isExitAfter(chr)));
                    }
                    break;
                }
                default: {
                    //some idiots try to send huge amounts of data to this (:
                    //System.out.println("Unhandled interaction action by " + chr.getName() + " : " + action + ", " + slea.toString());
                    //19 (0x13) - 00 OR 01 -> itemid(maple leaf) ? who knows what this is
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(chr.getName() + " crashed from player shop " + action);
            e.printStackTrace();
        }
    }
}
