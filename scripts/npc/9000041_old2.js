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
        amount = Number(cm.getText());
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
            rewards += "#i4420015# " + cm.getItemName(4420015) + " (x#b" + rewamount + ")#k\r\n";
            rewards += "#i4310272# " + cm.getItemName(4310272) + " (x#b" + Math.floor(cm.getPlayer().getTotalLevel() * 0.5) + ")#k\r\n";
            if (cm.getPlayer().getTotalLevel() > 100) {
                var bag = Math.floor(cm.getPlayer().getTotalLevel() * 0.1);
                rewards += "#i4310500# " + cm.getItemName(4310500) + " (x#b" + bag + ")#k\r\n";
            }
            if (cm.getPlayer().achievementFinished(293) && cm.getPlayer().getTotalLevel() >= 5000) {
                rewards += "#i4036002# " + cm.getItemName(4036002) + "\r\n";
            }
            var skin = 0;
            wanted += 1;
            if (wanted == 1 || wanted == 3) {
                skin = 4420002;
            }
            if (wanted == 2 || wanted == 4) {
                skin = 4420003;
            }
            if (wanted == 5 || wanted == 7 || wanted == 9) {
                skin = 4420004;
            }
            if (wanted == 10 || wanted == 15 || wanted == 20 || wanted == 30 || wanted == 35 || wanted == 40) {
                skin = 4420005;
            }
            if (wanted == 25 || wanted == 50 || wanted == 75 || wanted == 100 || wanted == 150 || wanted == 200 || wanted == 250) {
                skin = 4420006;
            }
            if (skin != 0) {
                rewards += "#i" + skin + "# " + cm.getItemName(skin) + " (x#b1#k)\r\n\ ";
            }
            cm.sendYesNo("#rThis reward can be claimed every 4 hours.#k\r\n" + rewards);
        } else {
            cm.sendOk("Wrong password mother fka.");
        }
    } else if (status == 2) {
        cm.gainItem(4420015, 5);
        cm.getPlayer().miniLevelUp();
        cm.getPlayer().setQuestLock(questid, questtime);
        cm.getPlayer().addVar("Reward", 1);
        var skin = 0;
        var wanted = cm.getPlayer().getVar("Reward");
        if (wanted == 1 || wanted == 3) {
            skin = 4420002;
        }
        if (wanted == 2 || wanted == 4) {
            skin = 4420003;
        }
        if (wanted == 5 || wanted == 7 || wanted == 9) {
            skin = 4420004;
        }
        if (wanted == 10 || wanted == 15 || wanted == 20 || wanted == 30 || wanted == 35 || wanted == 40) {
            skin = 4420005;
        }
        if (wanted == 25 || wanted == 50 || wanted == 75 || wanted == 100 || wanted == 150 || wanted == 200 || wanted == 250) {
            skin = 4420006;
        }
        var rewards = "#i" + reward + "# " + cm.getItemName(reward) + " (x#b" + rewamount + "#k)\r\n";

        rewards += "#i4310272# " + cm.getItemName(4310272) + " (x#b" + Math.floor(cm.getPlayer().getTotalLevel() * 0.5) + ")#k\r\n";
        cm.gainItem(4310272, Math.floor(cm.getPlayer().getTotalLevel() * 0.5));
        if (cm.getPlayer().getTotalLevel() > 100) {
            var bag = Math.floor(cm.getPlayer().getTotalLevel() * 0.1);
            rewards += "#i4310500# " + cm.getItemName(4310500) + " (x#b" + bag + ")#k\r\n";
            cm.gainItem(4310500, bag);
        }
        if (cm.getPlayer().achievementFinished(293) && cm.getPlayer().getTotalLevel() >= 5000) {
            rewards += "#i4036002# " + cm.getItemName(4036002) + "\r\n";
            cm.gainItem(4036002, 1);
        }
        if (skin != 0) {
            rewards += "#i" + skin + "# " + cm.getItemName(skin) + " (x#b1#k)\r\n";
            cm.gainItem(skin, 1);
        }
        cm.sendOk("You have gained following items:\r\n\r\n" + rewards + "\r\nCome back and see me in #b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k.\r\nYou have Currently claimed #b" + cm.getPlayer().getVar("Reward") + "#k Rewards");
    } else {
        cm.dispose();
    }
}


