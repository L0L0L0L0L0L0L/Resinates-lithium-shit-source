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
var items = new Array(4036520, 4036521, 4036522, 4009189);
var amount = 100;
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
        if (cm.getPlayer().achievementFinished(411)) {
            if (!cm.getPlayer().achievementFinished(2002)) {
                var selStr = "";
                for (var i = 0; i < items.length; i++) {
                    var color = cm.haveItem(items[i], 99) ? "g" : "r";
                    selStr += "#i" + items[i] + "#  " + cm.getItemName(items[i]) + " (x#" + color + "" + 99 + "#k)\r\n\ ";
                }
                var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n\#fUI/UIWindow.img/Quest/basic#\r\n";
                rewards += "#rAchievement: Unlocking Zoolen#k\r\n";
                cm.sendYesNo("Bring the following items and I will reward you.\r\n\You can find these items outside of Grove: \r\n\r\n\ " + selStr + "\r\n\r\n" + rewards);
            } else {
                cm.sendOk("Enjoy your trip at Zoolen!");
            }
        } else {
            cm.sendOk("You havent cleared Demon Spire Boss here yet....\r\nHead over to North of Town to the Spire.");
        }
    } else if (status == 1) {
        var check = true;
        for (var i = 0; i < items.length; i++) {
            if (!cm.haveItem(items[i], amount)) {
                check = false;
                break;
            }
        }
        if (check) {
            for (var i = 0; i < items.length; i++) {
                cm.gainItem(items[i], -amount);
            }
            cm.getPlayer().finishAchievement(2002);
            cm.sendOk("You are clear to pogress into #rZoolen#k");
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


