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
var ticketId = 4310066;
var reward = 2000012;
var rewamount = 5;
var items = new Array(4310066);
var amount = new Array(25000);
var exp = 250000;
var questid = 98;
var questtime = 14400;//30 min
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
        if (cm.getPlayer().getQuestLock(questid) > 0) {
            cm.sendOk("You can come back to me after #b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k to repeat my quest again.");
        } else {
            var selStr = "";
            selStr += "#i" + ticketId + "#  " + cm.getItemName(ticketId) + " (x250,000)\r\n\ ";
            var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n\#fUI/UIWindow.img/Quest/basic#\r\n";
            rewards += "#i2000012# " + cm.getItemName(reward) + " (x#b" + rewamount + ")#k\r\n";
            rewards += "#fUI/UIWindow2.img/QuestIcon/7/0# #b" + (cm.convertNumber(exp * cm.getPlayer().getMesoMod())) + "#k (#g+" + cm.getPlayer().getMesoMod() * 100 + "%#k)\r\n\ ";
            cm.sendYesNo("Bring the following items and I will reward you.\r\n\#rThis quest can be repeated every 4 hours of gameplay.#k\r\n\You can find these items outside of town: \r\n\r\n\ " + selStr + "\r\n\r\n" + rewards);
        }
    } else if (status == 1) {
        if (cm.haveItem(ticketId, 250000)) {
            cm.gainItem(ticketId, -250000);
            cm.gainItem(reward, rewamount);
            cm.getPlayer().miniLevelUp();
            cm.gainMeso(exp * cm.getPlayer().getMesoMod());
            cm.getPlayer().setQuestLock(questid, questtime);
            cm.sendOk("You have gained " + rewamount + "x #i" + reward + "#. Come back and see me in #b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k.");
        } else {
            cm.sendOk("You currently do not have enough #i4310066#  " + cm.getItemName(ticketId) + " for me to give you #i" + reward + "#.");
        }
        cm.dispose();
    } else {
        cm.dispose();
    }
}


