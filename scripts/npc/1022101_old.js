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
var rewamount = 250;
var items = new Array(4000010, 4000026, 4000031, 4000013, 4000041);
var amount = new Array(1000, 1000, 1000, 1000, 1000);
var exp = 250000;
var questid = 3;
var questtime = 14400;//4 hours
var level = 0;

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
        if (cm.getPlayer().getQuestStatus() == 0) {
            if (!cm.generateQuest()) {
                cm.sendOk("#rError with generating Items#k.");
                return;
            }
        }
        level = cm.getPlayer().getQuestlevel();
        var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n\#fUI/UIWindow.img/Quest/basic#\r\n";
        rewards += "#i4310015# " + cm.getItemName(4310015) + " (x#b" + level + "#k), ";
        rewards += "#i4310501# " + cm.getItemName(4310501) + " (x#b" + Math.floor(5 + (level / 10)) + "#k)\r\n\ ";
        rewards += "#fUI/UIWindow2.img/QuestIcon/7/0# #b" + (cm.convertNumber(2500 + (5 * level * level * cm.getPlayer().getMesoMod()))) + "#k (#g+" + cm.getPlayer().getMesoMod() * 100 + "%#k)\r\n\ ";
        rewards += "#fUI/UIWindow2.img/QuestIcon/6/0# #b+10#k\r\n\ ";
        
        if (cm.getPlayer().getTotalLevel() < 999) {
            rewards += "#fUI/UIWindow2.img/QuestIcon/8/0# #b+1 Level#k\r\n\ ";
        }
        if (cm.getPlayer().isGM()) {
            cm.sendYesNo("I see you have collected the following items:\r\n\r\n" + cm.getQuestPool() + "\r\n\r\n" + rewards);
        } else {
            if (cm.hasQuestItems()) {
                cm.sendYesNo("I see you have collected the following items:\r\n\r\n" + cm.getQuestPool() + "\r\n\r\n" + rewards);
            } else {
                cm.sendOk("Bring me the following items and I will reward you.\r\n\#rThis quest can be repeated as many times as you want.#k\r\n#bQuest Level#k: " + level + "\r\n\r\n" + cm.getQuestPool() + "\r\n\r\n" + rewards);
            }
        }

    } else if (status == 1) {
        if (cm.getPlayer().isGM() || cm.completeQuest()) {
            cm.gainItem(reward, (level));
            cm.gainMeso(2500 + (5 * level * level * cm.getPlayer().getMesoMod()));
            cm.getPlayer().addFame(10);
            cm.gainItem(4310501, Math.floor(5 + (level / 10)));
            if (cm.getPlayer().getTotalLevel() < 999) {
                cm.getPlayer().levelUp();
            }
            cm.sendOk("You have gained the following rewards:\r\n#b#i" + reward + "# (x" + level + ")#k\r\n#b+" + (cm.convertNumber(2500 + (level * level * cm.getPlayer().getMesoMod()))) + " Mesos#k (#g+" + cm.getPlayer().getMesoMod() * 100 + "%#k)\r\n#b+10 Fame#k\r\n\r\nYou can exchange these emblems in Free Market at Inkwell. Come back and see me for another quest.");
        } else {
            cm.sendOk("You currently missing some quest items.");
        }
        cm.dispose();
    } else {
        cm.dispose();
    }
}


