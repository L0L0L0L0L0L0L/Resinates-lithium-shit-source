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
var items = new Array(4001087, 4001088, 4001089, 4001090, 4001091);
var amount = 99;
var reward = 4001063;
var rewamount = 10;
var exp = 250000;
var questid = 782;
var questtime = 3600;//30 min
var job = "thieves";
var option = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        var selStr = "";
        for (var i = 0; i < items.length; i++) {
            var color = cm.haveItem(items[i], 99) ? "g" : "r";
            selStr += "#i" + items[i] + "#  " + cm.getItemName(items[i]) + " (x#" + color + "" + 99 + "#k)\r\n\ ";
        }
        var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n\#fUI/UIWindow.img/Quest/basic#\r\n";
        rewards += "#i" + reward + "# " + cm.getItemName(reward) + " (x#b" + rewamount + ")#k\r\n";
        cm.sendYesNo("Bring the following items and I will reward you.\r\n\You can find these items outside of Grove: \r\n\r\n\ " + selStr + "\r\n\r\n" + rewards);
    } else if (status == 1) {
        var check = true;
        for (var i = 0; i < items.length; i++) {
            if (!cm.haveItem(items[i], 99)) {
                check = false;
                break;
            }
        }

        if (check) {
            cm.gainItem(items[0], -99);
            cm.gainItem(items[1], -99);
            cm.gainItem(items[2], -99);
            cm.gainItem(items[3], -99);
            cm.gainItem(items[4], -99);
            cm.gainItem(reward, rewamount);
            cm.sendOk("You have gained " + rewamount + "x #i" + reward + "#.\r\n\#rUse #i4001063# to access the mini-boss at end of the grove.#k");
        } else {
            var selStr = "";
            for (var i = 0; i < items.length; i++) {
                selStr += "#i" + items[i] + "#  " + cm.getItemName(items[i]) + " (x#r" + 99 + "#k)\r\n\ ";
            }
            cm.sendOk("You currently do not have enough \r\n\r\n" + selStr + "\r\n for me to give you #i" + reward + "#.");
        }

        cm.dispose();
    } else {
        cm.dispose();
    }
}


