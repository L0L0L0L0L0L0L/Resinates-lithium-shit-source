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
var questid = 87124;
var questtime = 43200;//30 min
var job = "thieves";
var tier1 = new Array(2049189, 2049305, 2585005, 2586002, 4430003);
var tier2 = new Array(2049175, 2049306, 2585006, 2586003, 4430004);
var tier3 = new Array(2049175, 2049307, 2585007, 2586004, 4430005);
var cost = 0;
var wanted = 0;

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
        if (cm.getPlayer().getAccVaraLock(questid) > 0) {
            cm.sendOk("You can come back to me after\r\n#b" + cm.secondsToString(cm.getPlayer().getAccVaraLock(questid)) + "#k\r\nto claim more free rewards.");
        } else {
            var rewards = "";
            var total = wanted = cm.getPlayer().getAccVara("IPReward");
            wanted = cm.getPlayer().getAccVara("IPReward");
            if (wanted > 50) {
                wanted = 50;
                rewards += "You have currently claimed #b" + (total) + "#k (#rMAX#k) rewards.\r\n";
            } else {
                rewards += "You have currently claimed #b" + (total) + "#k rewards.\r\n";
            }
            cost = 1 + wanted;
            if (cost > 25) {
                cost = 25;
            }
            rewards += "Here is a list of possible rewards obtained at the cost of #r" + (cost) + " " + cm.getItemName(4310505) + "#k.\r\n";
            rewards += "Rewards are Randomzied on what type and amount you collect.\r\n";
            rewards += "More Rewards you collect increases the amount of rewards gained.\r\n";
            rewards += "#rMake Sure you have enough room in ETC and USE to collect the rewards, refunds will not be handed out#k.\r\n\r\n";
            rewards += "#fUI/UIWindow.img/Quest/reward#\r\n";
            rewards += "#i4310506# " + cm.getItemName(4310506) + " (x#b250 -> " + (500 + wanted * 5) + "#k)\r\n";//mag
            rewards += "#i4310510# " + cm.getItemName(4310510) + " (x#b50 -> " + (100 + wanted * 2) + "#k)\r\n";//doll
            rewards += "#i4420090# " + cm.getItemName(4420090) + " (x#b10 -> " + (25 + wanted) + "#k)\r\n\r\n";//hyper
            rewards += "#fUI/UIWindow.img/Quest/reward# #rRandomized:#k\r\n";
            rewards += "Tier 1 (#b100-" + (250 + wanted * 5) + "#k): #i2049189# #i2049305# #i2585005# #i2586002# #i4430003#\r\n";//upgrades
            rewards += "Tier 2 (#b50-" + (100 + wanted * 2) + "#k): #i2049175# #i2049306# #i2585006# #i2586003# #i4430004#\r\n";//upgrades
            rewards += "Tier 3 (#b10-" + (25 + wanted * 1) + "#k): #i2049176# #i2049307# #i2585007# #i2586004# #i4430005#\r\n";//upgrades
            cm.sendYesNo("#rThis reward can be claimed every 12 hours.#k\r\n" + rewards);
        }
    } else if (status == 1) {
        if (cm.haveItem(4310505, cost)) {
            cm.gainItem(4310505, -cost);
            cm.getPlayer().setAccVaraLock(questid, questtime);
            cm.getPlayer().addAccVar("IPReward", 1);

            var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n";

            var rand_1 = cm.random(250, 500 + (wanted * 5));
            rewards += "#i4310506# " + cm.getItemName(4310506) + " (x#b" + rand_1 + "#k)\r\n";
            cm.gainItem(4310506, rand_1);

            var rand_2 = cm.random(50, 100 + (wanted * 2));
            rewards += "#i4310510# " + cm.getItemName(4310510) + " (x#b" + rand_2 + "#k)\r\n";
            cm.gainItem(4310510, rand_2);

            var rand_3 = cm.random(10, 25 + (wanted));
            rewards += "#i4420090# " + cm.getItemName(4420090) + " (x#b" + rand_3 + "#k)\r\n";
            cm.gainItem(4420090, rand_3);

            //upgrades
            var rand_4 = cm.random(100, 250 + (wanted * 5));
            var item_1 = tier1[cm.random(0, 4)];
            rewards += "#i" + item_1 + "# " + cm.getItemName(item_1) + " (x#b" + rand_4 + "#k)\r\n";
            cm.gainItem(item_1, rand_4);

            var rand_5 = cm.random(50, 100 + (wanted * 2));
            var item_2 = tier2[cm.random(0, 4)];
            rewards += "#i" + item_2 + "# " + cm.getItemName(item_2) + " (x#b" + rand_5 + "#k)\r\n";
            cm.gainItem(item_2, rand_5);

            var rand_6 = cm.random(10, 25 + (wanted));
            var item_3 = tier3[cm.random(0, 4)];
            rewards += "#i" + item_3 + "# " + cm.getItemName(item_3) + " (x#b" + rand_6 + "#k)\r\n";
            cm.gainItem(item_3, rand_6);

            cm.getPlayer().recoverStamina();
            cm.sendOk("You have gained following items:\r\n" + rewards + "\r\nCome back and see me in #b" + cm.secondsToString(cm.getPlayer().getAccVaraLock(questid)) + "#k.\r\nYou have Currently claimed #b" + wanted + "#k Rewards");
        } else {
            cm.sendOk("You currently do not have any #i4310505# " + cm.getItemName(4310505) + ".");

        }
    } else if (status == 2) {

    } else {
        cm.dispose();
    }
}


