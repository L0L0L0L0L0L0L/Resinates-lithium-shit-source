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
/* 9000021 - Gaga
 BossRushPQ recruiter
 @author Ronan
 */

var status;
var level = 180;
var dp = 4310502;
var amount = 25;
var letter = 4009180;
var ach = new Array(0, 0, 0, 0, 0, 0);
var ppl = new Array(0, 4, 4, 4, 4, 4);
var level = new Array(0, 125, 150, 175, 200, 500);
var questid = 7923;
var questtime = 1200;//10 min
var option = 0;
var orb = 2400006;
var count = 0;

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
        var text = "Hello, Stranger...\r\n";
        text += "#L1# #rExchange for #i" + orb + "#" + cm.getItemName(orb) + "#b#l\r\n\r\n";
        //text += "#L2# #bChallenge the Fel Monsters (Requires Party)#k#l\r\n";
        cm.sendSimple(text);

    } else if (status == 1) {
        if (selection == 1) {
            option = 1;
            cm.sendGetText("How many #i" + orb + "#" + cm.getItemName(orb) + " you wish to exchange.\r\n#rEach Package Requires:#k\r\n25x #i" + 2430131 + "# " + cm.getItemName(2430131) + "\r\n5x #i" + 2430130 + "# " + cm.getItemName(2430130) + "\r\n ");
        }
    } else if (status == 2) {
        if (option == 1) {
            count = cm.getNumber();
            if (count > 0 && count < 999) {
                cm.sendYesNo("Are you sure you want to buy #b" + count + "#k #i" + orb + "#" + cm.getItemName(orb) + "?\r\n#rCOst of Each Package:#k\r\n" + (count * 25) + "x #i" + 2430131 + "# " + cm.getItemName(2430131) + "\r\n" + (count * 5) + "x #i" + 2430130 + "# " + cm.getItemName(2430130) + "\r\n ");
            } else {
                cm.sendOkS("You dont have enough #i4000999#", 16);
            }
        }
    } else if (status == 3) {
        if (option == 1) {
            if (cm.haveItem(2430131, count * 25) && cm.haveItem(2430130, count * 5)) {
                cm.gainItem(2430131, -(count * 25));
                cm.gainItem(2430130, -(count * 5));
                cm.gainItem(orb, count);
                cm.sendOkS("You have gained " + count + " #i" + orb + "#" + cm.getItemName(orb) + ".", 16);
            } else {
                cm.sendOkS("You dont have enough #i" + orb + "#", 16);
            }
        }
    } else if (status == 4) {

    }
}