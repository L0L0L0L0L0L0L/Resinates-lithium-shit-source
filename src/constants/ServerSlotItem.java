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
package constants;

public class ServerSlotItem {

    public int icon, id, amount, amount2, amount3, frame, rounds;

    public ServerSlotItem(int icon, int id, int amount, int amount2, int amount3, int frame, int rounds) {
        this.icon = icon;
        this.id = id;
        this.amount = amount;
        this.amount2 = amount2;
        this.amount3 = amount3;
        this.frame = frame;
        this.rounds = rounds;
    }

    public int getIcon() {
        return icon;
    }

    public int getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public int getAmount2() {
        return amount2;
    }

    public int getAmount3() {
        return amount3;
    }

    public int getFrame() {
        return frame;
    }

    public int getWeight() {
        return rounds;
    }

}
