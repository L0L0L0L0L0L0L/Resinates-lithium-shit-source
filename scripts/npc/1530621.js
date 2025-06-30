/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
var status = 0;
var reward = 4033320;
var rewamount = 1;
var items = new Array(4033338, 4033339, 4033340);
var amount = new Array(1000, 500, 250);
var Cantidad;
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            var selStr = "";
            for (var i = 0; i < items.length; i++) {
                selStr += "#i" + items[i] + "#  " + cm.getItemName(items[i]) + " (x#b" + amount[i] + "#k) (#b" + cm.convertNumber(cm.getPlayer().countAllItem(items[i])) + "#k)\r\n\ ";
            }
            cm.sendYesNo("I need the following materials to craft #i" + reward + "#: \r\n\r\n\ " + selStr);
        } else if (status == 1) {
            cm.sendGetText("How many times do you want me to do it?\r\n\r\n");
        } else if (status == 2) {
            Cantidad = cm.getNumber();
            if (Cantidad <= 0) {
                cm.sendOk("enter a number greater than 0");
                return;
            }
            var selStr = "";
            for (var i = 0; i < items.length; i++) {
                selStr += "#i" + items[i] + "#  " + cm.getItemName(items[i]) + " (x" + (amount[i]*Cantidad) + ")\r\n\ ";
            }
            cm.sendYesNo("I need the following materials to craft #i" + reward + "#x"+Cantidad+": \r\n\r\n\ " + selStr);
        } else if (status == 3) {
            if (cm.haveItem(items[0], amount[0]*Cantidad) && cm.haveItem(items[1], amount[1]*Cantidad) && cm.haveItem(items[2], amount[2]*Cantidad)) {
                cm.gainItem(items[0], -amount[0]*Cantidad);
                cm.gainItem(items[1], -amount[1]*Cantidad);
                cm.gainItem(items[2], -amount[2]*Cantidad);
                cm.gainItem(reward, rewamount*Cantidad);
                cm.sendOk("You have gained #i" + reward + "#x"+Cantidad+", use this to fight my daddy, Ramu. Sorry Daddy I love you!!");
            } else {
                cm.sendOk("You currently do not have enough materials to craft #i" + reward + "#x"+Cantidad+".");
            }
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}


