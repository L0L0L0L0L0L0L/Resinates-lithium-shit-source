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

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {

    } else {
        if (mode == 0 && type > 0) {

            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (cm.getPlayer().getAchievement(170)) {
                var text = "Welcome traveler! Which area do you wish to travel to?\r\n";
                text += "#L1# #bWhite Spear Ship?#k#l\r\n";
                if (cm.getPlayer().getAchievement(152)) {
                    text += "#L2# #bLabyrinth of Suffering?#k#l\r\n";
                }
                if (cm.getPlayer().getAchievement(69)) {
                    text += "#L3# #bWorld's Sorrow?#k#l\r\n";
                }
                if (cm.getPlayer().getAchievement(180)) {
                    text += "#L4# #bWorld's End?#k#l\r\n";
                }
                cm.sendSimple(text);
            } else {
                cm.sendOk("Head back to #bEsfera#k and Complete the #rIron Key Quest#k.");
            }
        } else if (status == 1) {
            var town = 0;
            if (selection == 1) {
                town = 450009100;
            } else if (selection == 2) {
                town = 450011120;
            } else if (selection == 3) {
                town = 450012000;
            } else if (selection == 4) {
                town = 450012300;
            }
            cm.warp(town);
        }
    }
}