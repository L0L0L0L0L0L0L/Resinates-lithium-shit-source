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
        if (cm.isCloseNpc(7500)) {
            var selStr = "Welcome to Kaotic Maple. I am here to help you learn how this server works compared to a normal maplestory server.\r\n";
            selStr += "#L1##bI want to help your friend#k#l\r\n";
            selStr += "#L2##rNot interested#k#l\r\n";
            cm.sendSimple(selStr);
        } else {
            cm.sendOk("Come closer to me. Cupcakes.");
        }
    } else if (status == 1) {
        if (selection == 1) {
            if (cm.getPlayer().countItem(4009180) == 0) {
                cm.gainItem(4009180, 1);
                cm.sendOk("Please take this letter over to my buddy on other side of gorge.");
            } else {
                cm.sendOk("Stop talking to me, I alrdy gave you the letter.");
            }
        } else {
            cm.sendYesNo("Are you sure you want forfit helping my friends for some awesome rewards. Once you leave you cannot come back.\r\n\r\n#rAre you sure you want to leave?");
        }
    } else if (status == 2) {
        if (cm.getPlayer().countItem(4009180) > 0) {
            cm.gainItem(4009180, -1);
        }
        cm.getPlayer().finishAchievement(901);
        cm.warp(5002);
    }
}