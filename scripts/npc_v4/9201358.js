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
var level = 250;
var dp = 4310502;
var amount = 25;
var letter = 4009180;
var ach = new Array(0, 0, 0, 0, 0);
var red = 0, blue = 0, yellow = 0;

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
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                cm.sendGetText("Hello there, as you may have noticed there is a ton of glowing stars on this map.\r\n#rHow many Red Stars did you find?#k\r\n\r\n");
            } else {
                cm.sendOkS("Only the Party leader can speak to me.", 2);
            }
        } else {
            cm.sendOkS("I must take on this Challenge with a party.", 2);
        }
    } else if (status == 1) {
        red = cm.getNumber();
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                cm.sendGetText("Hello there, as you may have noticed there is a ton of glowing stars on this map.\r\n#rHow many Blue Stars did you find?#k\r\n\r\n");
            } else {
                cm.sendOkS("Only the Party leader can speak to me.", 2);
            }
        } else {
            cm.sendOkS("I must take on this Challenge with a party.", 2);
        }
    } else if (status == 2) {
        blue = cm.getNumber();
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                cm.sendGetText("Hello there, as you may have noticed there is a ton of glowing stars on this map.\r\n#rHow many Yellow Stars did you find?#k\r\n\r\n");
            } else {
                cm.sendOkS("Only the Party leader can speak to me.", 2);
            }
        } else {
            cm.sendOkS("I must take on this Challenge with a party.", 2);
        }
    } else if (status == 3) {
        yellow = cm.getNumber();
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                var selStr = "Do you want to confirm the following:\r\n\r\n";
                selStr += "I saw #b" + red + "#k Red Stars!\r\n";
                selStr += "I saw #b" + blue + "#k Blue Stars!\r\n";
                selStr += "I saw #b" + yellow + "#k Yellow Stars!\r\n";
                cm.sendYesNo(selStr);
            } else {
                cm.sendOkS("Only the Party leader can speak to me.", 2);
            }
        } else {
            cm.sendOkS("I must take on this Challenge with a party.", 2);
        }
    } else if (status == 4) {
        if (red == cm.getMap().getObjectInt("Red") && blue == cm.getMap().getObjectInt("Blue") && yellow == cm.getMap().getObjectInt("Yellow")) {
            cm.getMap().broadcastMapMsg("All the stars have been accounted for. Map is Clear to progress.", 5120150);
            cm.getMap().showClear();
            cm.dispose();
        } else {
            cm.sendOkS("I must go back and learn how to count the stars.", 2);
        }
    }
}