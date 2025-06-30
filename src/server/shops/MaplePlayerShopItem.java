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

import client.inventory.Item;

public class MaplePlayerShopItem {

    public Item item;
    public short bundles, amount;
    public int price, slot;
    private boolean doesExist;

    public MaplePlayerShopItem(Item item, short amount, short bundles, int price, int slot) {
        //System.out.println("amount = " + amount);
        //System.out.println("bundles = " + bundles);
        this.item = item;
        this.amount = amount;
        this.bundles = bundles;
        this.price = price;
        this.doesExist = true;
    }

    public void setDoesExist(boolean tf) {
        this.doesExist = tf;
    }

    public boolean isExist() {
        return doesExist;
    }

    public short getBundles() {
        return bundles;
    }

    public short getAmount() {
        return amount;
    }

    public short getQuantity() {
        return (short) (bundles * amount);
    }

    public int getPrice() {
        return price;
    }

    public Item getItem() {
        return item;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int id) {
        slot = id;
    }
}
