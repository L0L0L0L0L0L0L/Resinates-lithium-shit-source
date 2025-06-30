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
var reward = 4310015;
var rewamount = 50;
var items = new Array(4000006, 4000037, 4000042, 4000035, 4000036);
var amount = new Array(100, 100, 100, 100, 100);
var exp = 25000;
var questid = 4;
var questtime = 1800;//30 min
var job = "thieves";

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
        if (cm.isJobType(questid)) {
            if (cm.getPlayer().getQuestLock(questid) > 0) {
                cm.sendOk("You can come back to me after #b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k to repeat my quest again.");
            } else {
                var selStr = "";
                for (var i = 0; i < items.length; i++) {
                    selStr += "#i" + items[i] + "#  " + cm.getItemName(items[i]) + " (x" + amount[i] + ")\r\n\ ";
                }
                var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n\#fUI/UIWindow.img/Quest/basic#\r\n";
                rewards += "#i4310015# " + cm.getItemName(4310015) + " (x#b" + rewamount + "#k)\r\n";
                rewards += "#fUI/UIWindow2.img/QuestIcon/7/0# #b" + (cm.convertNumber(exp * cm.getPlayer().getMesoMod())) + "#k (#g+" + cm.getPlayer().getMesoMod() * 100 + "%#k)\r\n\ ";
                if (cm.getPlayer().getTotalLevel() < 999) {
                    rewards += "#fUI/UIWindow2.img/QuestIcon/8/0# #bMini Level Up#k\r\n\ ";
                    rewards += "#fUI/UIWindow2.img/QuestIcon/8/0# #b25,000 Exp#k\r\n\ ";
                }
                cm.sendYesNo("Bring the following items and I will reward you.\r\n\#rThis quest can be repeated every hour of gameplay.#k\r\n\You can find these items outside of town: \r\n\r\n\ " + selStr + "\r\n\r\n" + rewards);
            }
        } else {
            cm.sendOk("This quest is for " + job + " only.");
        }
    } else if (status == 1) {
        var check = 0;
        for (var i = 0; i < items.length; i++) {
            if (cm.haveItem(items[i], amount[i])) {
                check += 1;
            }
        }
        if (check == items.length) {
            cm.gainItem(items[0], -amount[0]);
            cm.gainItem(items[1], -amount[1]);
            cm.gainItem(items[2], -amount[2]);
            cm.gainItem(items[3], -amount[3]);
            cm.gainItem(items[4], -amount[4]);
            cm.gainItem(reward, rewamount);
            cm.getPlayer().miniLevelUp();
            cm.getPlayer().gainExp(exp, true, true, true);
            cm.gainMeso(exp * cm.getPlayer().getMesoMod());
            cm.getPlayer().setQuestLock(questid, questtime);
            cm.sendOk("You have gained " + rewamount + "x #i" + reward + "#. You can exchange these emblems in Free Market at Inkwell. Come back and see me in #b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k.");
        } else {
            cm.sendOk("You currently do not have enough materials for me to give you #i" + reward + "#.");
        }
        cm.dispose();
    } else {
        cm.dispose();
    }
}


