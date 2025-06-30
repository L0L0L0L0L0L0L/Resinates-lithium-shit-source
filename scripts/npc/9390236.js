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
var items = 4009211;
var bitems = 4009218;
var amount = 2500;
var bamount = 250;
var reward = 4310100;
var rewamount = 25;
var brewamount = 100;
var exp = 250000;
var questid = 707;
var bquestid = 710;
var questtime = 28800;//30 min
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
        if (cm.getPlayer().getMapId() == 865030500) {
            option = 1;
            if (cm.getPlayer().getQuestLock(bquestid) > 0) {
                cm.sendOk("Come see me after #b" + cm.secondsToString(cm.getPlayer().getQuestLock(bquestid)) + "#k \r\n\      to repeat my quest again.\r\n\ Quest ID: " + bquestid);
            } else {
                var selStr = "";
                var color = cm.haveItem(bitems, bamount) ? "g" : "r";
                selStr += "#i" + bitems + "#  " + cm.getItemName(bitems) + " (x#" + color + "" + bamount + "#k)\r\n\ ";
                var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n\#fUI/UIWindow.img/Quest/basic#\r\n";
                rewards += "#i" + reward + "# " + cm.getItemName(reward) + " (x#b" + brewamount + ")#k\r\n";
                cm.sendYesNo("Bring the following items and I will reward you.\r\n\#rThis quest can be repeated every 8 hours of gameplay.#k\r\n\You can find these items outside of town: \r\n\r\n\ " + selStr + "\r\n\r\n" + rewards);
            }
        } else {
            option = 2;
            if (cm.getPlayer().getQuestLock(questid) > 0) {
                cm.sendOk("Come see me after #b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k \r\n\      to repeat my quest again.\r\n\ Quest ID: " + questid);
            } else {
                var selStr = "";
                var color = cm.haveItem(items, amount) ? "g" : "r";
                selStr += "#i" + items + "#  " + cm.getItemName(items) + " (x#" + color + "" + amount + "#k)\r\n\ ";
                var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n\#fUI/UIWindow.img/Quest/basic#\r\n";
                rewards += "#i" + reward + "# " + cm.getItemName(reward) + " (x#b" + rewamount + ")#k\r\n";
                cm.sendYesNo("Bring the following items and I will reward you.\r\n\#rThis quest can be repeated every 8 hours of gameplay.#k\r\n\You can find these items outside of town: \r\n\r\n\ " + selStr + "\r\n\r\n" + rewards);
            }
        }
    } else if (status == 1) {
        if (option == 1) {
            if (cm.haveItem(bitems, bamount)) {
                cm.gainItem(bitems, -bamount);
                cm.gainItem(reward, brewamount);
                cm.getPlayer().miniLevelUp();
                cm.getPlayer().setQuestLock(bquestid, questtime);
                cm.sendOk("You have gained " + brewamount + "x #i" + reward + "#. Come back and see me in \r\n\#b" + cm.secondsToString(cm.getPlayer().getQuestLock(bquestid)) + "#k.\r\n\ Quest ID: " + bquestid);
            } else {
                cm.sendOk("You currently do not have enough #i" + bitems + "#  " + cm.getItemName(bitems) + " for me to give you #i" + reward + "#.");
            }
        }
        if (option == 2) {
            if (cm.haveItem(items, amount)) {
                cm.gainItem(items, -amount);
                cm.gainItem(reward, rewamount);
                cm.getPlayer().miniLevelUp();
                cm.getPlayer().setQuestLock(questid, questtime);
                cm.sendOk("You have gained " + rewamount + "x #i" + reward + "#. Come back and see me in \r\n\#b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k.\r\n\ Quest ID: " + questid);
            } else {
                cm.sendOk("You currently do not have enough #i" + items + "#  " + cm.getItemName(items) + " for me to give you #i" + reward + "#.");
            }
        }
        cm.dispose();
    } else {
        cm.dispose();
    }
}


