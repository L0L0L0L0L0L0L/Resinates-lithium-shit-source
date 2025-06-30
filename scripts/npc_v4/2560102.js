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
var items = new Array(4009215, 4009216, 4009217, 4009218);
var reward = 4036084;
var rewamount = 25;
var exp = 250000;
var questid = 3704;
var questtime = 18000;//30 min
var job = "thieves";
var option = 0;
var peanut = 4000754;
var amount = 5;
var hour = 5;

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
        if (cm.getPlayer().getQuestLock(questid) > 0) {
            cm.sendOk("Come see me after #b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k \r\n\      to repeat my quest again.\r\n\ Quest ID: " + questid);
        } else {
            var selStr = "";
            var color = cm.haveItem(peanut, amount) ? "g" : "r";
            selStr += "#i" + peanut + "#  " + cm.getItemName(peanut) + " (x#" + color + "" + amount + "#k)\r\n\ ";
            var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n\#fUI/UIWindow.img/Quest/basic#\r\n";
            rewards += "#rMax Combo Expanded by #k(x#b" + rewamount + ")#k\r\n";
            cm.sendYesNo("Bring the following items and I will reward you.\r\n\#rThis quest can be repeated every " + hour + " hours of gameplay.#k\r\n\You can find " + cm.getItemName(peanut) + " from #rCombo Ape#k: \r\n\r\n\ " + selStr + "\r\n\r\n" + rewards);
        }
    } else if (status == 1) {
        if (cm.haveItem(peanut, amount)) {
            cm.gainItem(peanut, -amount);
            cm.getPlayer().addCombo(rewamount);
            cm.getPlayer().setQuestLock(questid, questtime);
            cm.sendOk("I have expanded your max combo by " + rewamount + ". Come back and see me in \r\n\#b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k.\r\n\ Quest ID: " + questid);
        } else {
            var selStr = "";
            selStr += "#i" + peanut + "#  " + cm.getItemName(peanut) + " (x#r" + amount + "#k)\r\n\ ";
            cm.sendOk("You currently do not have enough \r\n\r\n" + selStr + "\r\n for me to expand your max combo!");
        }

        cm.dispose();
    } else {
        cm.dispose();
    }
}


