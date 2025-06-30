/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
var status = 0;
var reward = 4033320;
var rewamount = 1;
var option = 0;
var box = 0;
var amount = 0;
var job = new Array(112, 122, 132, 212, 222, 232, 312, 322, 412, 422, 434, 512, 522, 532, 1112, 1212, 1312, 1412, 1512, 2218, 3112, 3212, 3312, 3512, 900);


var questid = 7900;
var questtime = 3600;//7 days
var questItem = new Array(0, 4036084, 4310054, 4310100, 4310150, 4310028, 4310500, 4310501);
var amount = new Array(0, 500000, 10000000, 250000, 100000, 50000, 25000, 10000);
var glory = 4310272;
var casino = 4310101;
var count = 0;
var total = 0;

var Cantidad;
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.getPlayer().getQuestLock(questid) > 0) {
                cm.sendOk("Come see me after #b" + cm.secondsLongToString(cm.getPlayer().getQuestLock(questid)) + "#k\r\nTo collect more casino coins.\r\n\Quest ID: " + questid);
            } else {
                cm.sendYesNo("Psst wanna ditch those glory coins for some straight ass casino coins????");
            }
        } else if (status == 1) {
            cm.sendGetText("Please enter the amount of #i" + glory + "#" + cm.getItemName(glory) + " you wish to exchange.");
        } else if (status == 2) {
            count = cm.getNumber();
            if (count > 0 && count <= 50000) {
                if (cm.haveItem(glory, count)) {
                    total = cm.getPlayer().getLevelData(101) * count;
                    cm.sendYesNo("Are you sure you want to cash in #b" + count + "#k #i" + glory + "#" + cm.getItemName(glory) + " for some #i" + casino + "#?\r\nMy Rates are #r" + count + " Glory#k for #b" + total + " Casino Coins?");
                } else {
                    cm.sendOkS("You dont have enough #i"+glory+"#", 16);
                }
            } else {
                cm.sendOkS("You dont have enough #i"+glory+"#.\r\nMinimum is 1, Maximum is 50,000", 16);
            }
        } else if (status == 3) {
            if (cm.haveItem(glory, count)) {
                cm.gainItem(glory, -count);
                cm.getPlayer().addOverflow(casino, total);
                cm.getPlayer().setQuestLock(questid, questtime);
                cm.sendOk("I have successfully deposited " + total + " #i" + casino + "# into your overflow system. Come back after an hour for more.");
            } else {
                cm.sendOk("Seems like you dont have enough #i" + questItem[cm.getDayofWeek()] + "# for the tranformation!\r\nI Really want those shimmering stars bring me them!");
            }
        } else {
            cm.dispose();
        }
    }
}


