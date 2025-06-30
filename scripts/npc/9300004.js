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
/* NPC Base
 Map Name (Map ID)
 Extra NPC info.
 
 
 cm.gainItem(4310001, amount);
 cm.getPlayer().getCashShop().gainCash(2, amount);
 
 */

importPackage(Packages.client);
importPackage(Packages.tools);
importPackage(Packages.server.life);

var status;
var option = 0;
var item = 0;
var mPoint = 0;
var amount = 0;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var power = 0;
var cost = 5;
var rewards;



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
        rewards = cm.getNXPool();
        if (!rewards.isEmpty()) {
            var text = "The following items can be obtained from this Gachapon :\r\n\r\n";
            var iter = rewards.iterator();
            while (iter.hasNext()) {
                var i = iter.next();
                text += "#i" + i + "#";
            }
            cm.sendYesNo("Hello#b #h ##k, Current Gachapon Pool: \n\r\n " + text + "\n\r\nWould you like to cash in 5 #i4310501# for one of the random rewards shown above? #rCash Equips generate with no stats#k.");
        } else {
            cm.sendOk("No items currently avaiable.");
        }
    } else if (status == 1) {
        if (cm.haveItem(4310501, cost)) {
            var itemid = rewards.get(cm.random(0, rewards.size() - 1));
            if (cm.canHold(itemid)) {
                var scale = cm.random(1, 4);
                var text = "You have recieved the following items:\r\n\r\n";
                cm.gainEquip(itemid, 1000, scale);
                text += "#i" + itemid + "#";
                cm.gainItem(4310501, -cost);
                cm.sendYesNo(text + "\r\n\r\nWould you like to spend another #i4310501# for Random NX?");
            } else {
                cm.sendOk("Not Enough space. Requires at least 1 free equip slot.");
            }
        } else {
            cm.sendOk("Sorry, you dont seem to have enough maple points. Minimum amount needed is " + cost + " #i4310501#'s.");
        }
    } else if (status == 2) {
        status = 2;
        action(0, 0, 0);
    }
}