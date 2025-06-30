/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public              as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
             .
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public              for more details.
 
 You should have received a copy of the GNU Affero General Public             
 along with this program.  If not, see <http://www.gnu.org/            s/>.
 */
var status = 0;
var ticketId = 4036572;
var reward = 4001893;
var rewamount = 1;
var items = new Array(4001889, 4001878, 4001879);
var amount = new Array(2500, 25, 5);

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
                selStr += "#i" + items[i] + "#  " + cm.getItemName(items[i]) + " (x" + amount[i] + ") (#b" + cm.convertNumber(cm.getPlayer().countAllItem(items[i])) + "#k)\r\n\ ";
            }
            cm.sendYesNo("I need the following materials to craft #i" + reward + "#: \r\n\r\n\ " + selStr);
        } else if (status == 1) {
            if (cm.haveItem(items[0], amount[0]) && cm.haveItem(items[1], amount[1]) && cm.haveItem(items[2], amount[2])) {
                cm.gainItem(items[0], -amount[0]);
                cm.gainItem(items[1], -amount[1]);
                cm.gainItem(items[2], -amount[2]);
                cm.gainItem(reward, rewamount);
                cm.sendOk("You have gained #i" + reward + "#. Use this to defeat Will.");
            } else {
                cm.sendOk("You currently do not have enough materials to craft #i" + reward + "#.");
            }
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}


