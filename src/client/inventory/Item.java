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

import constants.GameConstants;
import java.io.Serializable;
import server.MapleItemInformationProvider;
import server.Randomizer;

public class Item implements Comparable<Item>, Serializable {

    private int id;
    private short position;
    private short quantity;
    private short flag;
    private long expiration = -1, inventoryitemid = 0;
    private MaplePet pet = null;
    private int uniqueid;
    private String owner = "";
    private String GameMaster_log = "";
    private String giftFrom = "";
    private int maxcount = 0;
    private boolean special = false, summoned = false;

    public Item(final int id, final short position, final short quantity, final short flag, final int uniqueid) {
        this.id = id;
        this.position = position;
        this.quantity = Randomizer.MaxShort(quantity, (short) 30000);
        this.flag = flag;
        this.uniqueid = uniqueid;
    }

    public Item(final int id, final short position, final short quantity, final short flag, final String log) {
        this.id = id;
        this.position = position;
        this.quantity = Randomizer.MaxShort(quantity, (short) 30000);
        this.flag = flag;
        this.uniqueid = -1;
        this.GameMaster_log = log;
    }

    public Item(final int id, final short position, final short quantity, final short flag) {
        this.id = id;
        this.position = position;
        this.quantity = Randomizer.MaxShort(quantity, (short) 30000);
        this.flag = flag;
        this.uniqueid = -1;
    }

    public Item(int id, byte position, short quantity) {
        this.id = id;
        this.position = position;
        this.quantity = Randomizer.MaxShort(quantity, (short) 30000);
        this.uniqueid = -1;
    }

    public Item copy() {
        Item ret = new Item(id, position, quantity, flag, uniqueid);
        ret.pet = pet;
        ret.owner = owner;
        ret.GameMaster_log = GameMaster_log;
        ret.expiration = expiration;
        ret.giftFrom = giftFrom;
        return ret;
    }

    public Item copy(short qq) {
        Item ret = new Item(id, position, qq, flag, uniqueid);
        ret.pet = pet;
        ret.owner = owner;
        ret.GameMaster_log = GameMaster_log;
        ret.expiration = expiration;
        ret.giftFrom = giftFrom;
        return ret;
    }

    public Item copy(int iid, short qq) {
        Item ret = new Item(iid, position, qq, flag, uniqueid);
        ret.pet = pet;
        ret.owner = owner;
        ret.GameMaster_log = GameMaster_log;
        ret.expiration = expiration;
        ret.giftFrom = giftFrom;
        return ret;
    }

    public Item copy(int iid, byte position, short qq) {
        Item ret = new Item(iid, position, qq, flag, uniqueid);
        ret.pet = pet;
        ret.owner = owner;
        ret.GameMaster_log = GameMaster_log;
        ret.expiration = expiration;
        ret.giftFrom = giftFrom;
        return ret;
    }

    public Item copyWithQuantity(final short qq) {
        Item ret = new Item(id, position, qq, flag, uniqueid);
        ret.pet = pet;
        ret.owner = owner;
        ret.GameMaster_log = GameMaster_log;
        ret.expiration = expiration;
        ret.giftFrom = giftFrom;
        return ret;
    }

    public Item copyWithQuantity(final short qq, boolean equip) {
        Item ret = new Item(id, position, equip ? quantity : qq, flag, uniqueid);
        ret.pet = pet;
        ret.owner = owner;
        ret.GameMaster_log = GameMaster_log;
        ret.expiration = expiration;
        ret.giftFrom = giftFrom;
        return ret;
    }

    public final void setPosition(final short position) {
        this.position = position;

        if (pet != null) {
            pet.setInventoryPosition(position);
        }
    }

    public void setItemId(int id) {
        this.id = id;
    }

    public void setQuantity(final short quantity) {
        this.quantity = quantity;
    }

    public void setMaxCount(final int quantity) {
        this.maxcount = quantity;
    }

    public int getMaxCount() {
        return this.maxcount;
    }

    public void setSpecial(final boolean toggle) {
        this.special = toggle;
    }

    public boolean getSpecial() {
        return this.special;
    }

    public final int getItemId() {
        return id;
    }

    public final short getPosition() {
        return position;
    }

    public final short getFlag() {
        return flag;
    }

    public boolean isSummoned() {
        return summoned;
    }

    public void setSummoned(boolean toggle) {
        summoned = toggle;
    }

    public MapleInventoryType getInventoryType() {
        return GameConstants.getInventoryType(id);
    }

    public final short getQuantity() {
        return quantity;
    }

    public byte getType() {
        return 2; // An Item
    }

    public final String getOwner() {
        return owner;
    }

    public final void setOwner(final String owner) {
        this.owner = owner;
    }

    public final void setFlag(final short flag) {
        this.flag = flag;
    }

    public final long getExpiration() {
        return expiration;
    }

    public final void setExpiration(final long expire) {
        this.expiration = expire;
    }

    public final String getGMLog() {
        return GameMaster_log;
    }

    public void setGMLog(final String GameMaster_log) {
        this.GameMaster_log = GameMaster_log;
    }

    public final int getUniqueId() {
        return uniqueid;
    }

    public void setUniqueId(int ui) {
        this.uniqueid = ui;
    }

    public final long getInventoryId() { //this doesn't need to be 100% accurate, just different
        return inventoryitemid;
    }

    public void setInventoryId(long ui) {
        this.inventoryitemid = ui;
    }

    public final MaplePet getPet() {
        return pet;
    }

    public final void setPet(final MaplePet pet) {
        this.pet = pet;
        if (pet != null) {
            this.uniqueid = pet.getUniqueId();
        }
    }

    public void setGiftFrom(String gf) {
        this.giftFrom = gf;
    }

    public String getGiftFrom() {
        return giftFrom;
    }

    @Override
    public int compareTo(Item other) {
        if (Math.abs(position) < Math.abs(other.getPosition())) {
            return -1;
        } else if (Math.abs(position) == Math.abs(other.getPosition())) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Item)) {
            return false;
        }
        final Item ite = (Item) obj;
        return uniqueid == ite.getUniqueId() && id == ite.getItemId() && quantity == ite.getQuantity() && Math.abs(position) == Math.abs(ite.getPosition());
    }

    @Override
    public String toString() {
        return "Item: " + id + " quantity: " + quantity;
    }

    public String getItemName() {
        return MapleItemInformationProvider.getInstance().getName(id);
    }

    public String getItemName(int id) {
        return MapleItemInformationProvider.getInstance().getName(id);
    }

    public boolean isCash(int id) {
        return MapleItemInformationProvider.getInstance().isCash(id);
    }
}
