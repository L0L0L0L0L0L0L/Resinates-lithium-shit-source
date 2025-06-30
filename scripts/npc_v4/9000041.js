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
var reward = 4420015;
var rewamount = 5;
var items = new Array(4310066);
var amount = new Array(25000);
var exp = 250000;
var questid = 70;
var questtime = 28800;//30 min
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
            cm.sendOk("You can come back to me after\r\n#b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k\r\nto claim more free rewards.");
        } else {
            var number1 = cm.random(1, 9);
            var number2 = cm.random(1, 9);
            var number3 = cm.random(1, 9);
            var number4 = cm.random(1, 9);
            password = cm.getCode(number1, number2, number3, number4);
            cm.sendGetText("#rBotting beyond this point is not allowed!\r\n\#kPlease enter the 4 digit #rAnti-Bot#k Code seen below:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "");
        }
    } else if (status == 1) {
        amount = cm.getNumber();
        if (amount == password) {
            var wanted = cm.getPlayer().getVar("Reward");
            var rewards = "";
            if (wanted == -1) {
                wanted += 1;
                rewards += "You have currently claimed #b" + (wanted + 1) + "#k rewards.\r\n\r\n";
            } else {
                if (wanted > 0) {
                    rewards += "You have currently claimed #b" + (wanted) + "#k rewards.\r\n\r\n";
                }
            }
            rewards += "#fUI/UIWindow.img/Quest/reward#\r\n";
            rewards += "#i4420015# " + cm.getItemName(4420015) + " (x#b10#k)\r\n";
            rewards += "#i4420008# " + cm.getItemName(4420008) + " (x#b250#k)\r\n";
            rewards += "#i4420006# " + cm.getItemName(4420006) + " (x#b10#k)\r\n";
            if (wanted > 1 && wanted % 5 == 0) {
                rewards += "#i4420007# " + cm.getItemName(4420007) + " (x#b1#k)\r\n";
            }
            cm.sendYesNo("#rThis reward can be claimed every 8 hours.#k\r\n" + rewards);
        } else {
            cm.sendOk("Wrong password mother fka.");
        }
    } else if (status == 2) {
        cm.getPlayer().setQuestLock(questid, questtime);
        var wanted = cm.getPlayer().getVar("Reward");
        var rewards = "#i4420015# " + cm.getItemName(4420015) + " (x#b1#k)\r\n";
        cm.gainItem(4420015, 1);
        rewards += "#i4420008# " + cm.getItemName(4420008) + " (x#b250#k)\r\n";
        cm.gainItem(4420008, 250);
        rewards += "#i4420006# " + cm.getItemName(4420006) + " (x#b10#k)\r\n";
        cm.gainItem(4420006, 10);
        if (wanted > 1 && wanted % 5 == 0) {
            rewards += "#i4420007# " + cm.getItemName(4420007) + " (x#b1#k)\r\n";
            cm.gainItem(4420007, 1);
        }
        cm.getPlayer().addVar("Reward", 1);
        cm.sendOk("You have gained following items:\r\n\r\n" + rewards + "\r\nCome back and see me in #b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k.\r\nYou have Currently claimed #b" + cm.getPlayer().getVar("Reward") + "#k Rewards");
    } else {
        cm.dispose();
    }
}


