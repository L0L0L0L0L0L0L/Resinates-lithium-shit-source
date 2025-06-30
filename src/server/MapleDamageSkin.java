/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
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
package server;

import client.MapleCharacter;

/**
 *
 * @author KyleShum
 */
public class MapleDamageSkin {

    public int id, tier, element, stat, stat_amount, stat2, stat2_amount, stat3, stat3_amount;
    public int level;
    public boolean notice;
    public String[] Stat_type = {"Exp Rate", "Drop Rate", "Drop Rate", "All Stats", "Overpower", "Meso Rate", "Total Damage", "Boss Damage", "Ignore Defense"};

    public MapleDamageSkin(int id, int tier, int element, int stat, int stat_amount, int stat2, int stat2_amount, int stat3, int stat3_amount) {
        this.id = id;
        this.tier = tier;
        this.element = element;
        this.stat = stat;
        this.stat_amount = stat_amount;
        this.stat2 = stat2;
        this.stat2_amount = stat2_amount;
        this.stat3 = stat3;
        this.stat3_amount = stat3_amount;
    }

    public int getId() {
        return id;
    }

    public int getTier() {
        return tier;
    }

    public int getElement() {
        return element;
    }

    public int getStat() {
        return stat;
    }

    public int getStat2() {
        return stat2;
    }

    public int getStat3() {
        return stat3;
    }

    public int getAmount() {
        return stat_amount;
    }

    public int getStat(int index) {
        if (index == 0) {
            return stat;
        } else if (index == 1) {
            return stat2;
        } else if (index == 2) {
            return stat3;
        }
        throw new IllegalArgumentException("invalid index");
    }

    public int getAmount(int index) {
        if (index == 0) {
            return stat_amount;
        } else if (index == 1) {
            return stat2_amount;
        } else if (index == 2) {
            return stat3_amount;
        }
        throw new IllegalArgumentException("invalid index");
    }

    public int getAmount2() {
        return stat2_amount;
    }

    public int getAmount3() {
        return stat3_amount;
    }

    public int getMaxLevel() {
        if (tier == 7) {
            return 9999;
        }
        return 999;
    }

    public int getSoftMaxLevel() {
        if (tier == 7) {
            return 9999;
        }
        return 999;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int value) {
        level = value;
    }

    public void gainLevel() {
        level += 1;
    }
}
