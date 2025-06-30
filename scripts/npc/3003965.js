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
        if (cm.getPlayer().getAchievement(181)) {
            var text = "";
            text += "#L1# #bForeberion#k#l\r\n";
            text += "#L2# #bAscendion#k#l\r\n";
            text += "#L3# #bEmbrion#k#l\r\n";

            cm.sendSimple("Would you like to enter my secret training ground?\r\nIf so, which Map would you like to train with?\r\n" + text);
        } else {
            cm.sendOk("You have defeated Command Will before you can access my lands.");
        }

    } else if (status == 1) {
        if (selection == 1) {
            cm.warp(450012301);
        }
        if (selection == 2) {
            cm.warp(450012302);
        }
        if (selection == 3) {
            cm.warp(450012303);
        }
    }
}