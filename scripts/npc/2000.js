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
var cube = 4310502;
var option = -1;
var count = 0;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var ach = 0;
var limit = 16;
var page = 0;

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
        if (cm.getPlayer().achievementFinished(906)) {
            option = 1;
            cm.sendYesNo("Would you like to skip this tutorial?");
        } else {
            option = 2;
            page = 0;
            var selStr = "Welcome to Kaotic Maple. I am here to help you learn how this server works compared to a normal maplestory server.\r\n#rYou can use @help or discord for any help#k.\r\n#bThis server is best played on 1280x720.#k\r\n";
            if (cm.getPlayer().achievementFinished(900)) {
                selStr += "#bI see you have already read all guides,\r\nYou can just progress through the portal#k\r\n";
                cm.sendOk(selStr);
            } else {
                selStr += "\r\n#L99# #gI have read enough. Let me play#k#l\r\n";
                cm.sendSimple(selStr);
            }
        }
    } else if (status == 1) {
        if (option == 1) {
            cm.gainItem(2049300, 100);
            cm.gainItem(2049301, 50);
            cm.gainItem(2049302, 5);
            cm.gainEquip(1112920, 1);
            cm.gainEquip(1112920, 1);
            cm.gainEquip(1112920, 1);
            cm.gainEquip(1112920, 1);
            cm.gainEquip(1002419, 2);
            cm.getPlayer().addLevel(9);
            cm.warp(5004);
        }
        if (option == 2) {
            var selStr = "";
            option = 3;
            cm.sendYesNo("Are you ready to get going?!");
        }
    } else if (status == 2) {
        if (option == 2) {
            if (page == 0) {
                cm.getPlayer().finishAchievement(909 + ach);
                status = 1;
                action(0, 0, 0);
            } else {
                var selStr = "";
                if (ach == 3) {
                    selStr = star + "#r#eBoss Progression:#k#n\r\n";
                    selStr += "     " + star + "Bosses must be defeated and completed in proper order to progress. The portals when entering bosses will always say what boss your missing. You can refer to #rDamien in Free Market#k for full boss order.\r\n\r\n";
                    selStr += star + "#r#eBoss Material Events:#k#n\r\n";
                    selStr += "     " + star + "Special events used to farm materials for bosses.\r\n";
                    selStr += "     " + star + "Lucid Boss Material Event can be activated once your defeat Lucid. This event can be found by talking to Dark Mask on Lucid Tower 3rd Floor.\r\n";
                    selStr += "     " + star + "Normal Will Boss Material Event can be activated once your defeat Will. This event can be found by talking to Melange at Radiant Temple 1st Map.\r\n";
                    selStr += "     " + star + "Commander Will Boss Material Event can be activated once your defeat Commander Will. This event can be found by talking to Cygnus Soilders at End of the World.\r\n";
                }
                cm.sendNext(selStr);
            }
        }
        if (option == 3) {
            var selStr = "You have successfully comeplete your first quest.\r\n";
            cm.getPlayer().finishAchievement(900);
            cm.gainItem(2000005, 100);
            cm.sendOk(selStr + "#rTake the portal to next map to continue your journey.#k");

        }
    } else if (status == 3) {
        if (option == 2) {
            if (page == 1) {
                cm.getPlayer().finishAchievement(909 + ach);
                status = 1;
                action(0, 0, 0);
            } else {

            }

        }
    } else if (status == 4) {
        if (option == 2) {
            if (page == 2) {
                cm.getPlayer().finishAchievement(909 + ach);
                status = 1;
                action(0, 0, 0);
            } else {

            }

        }
    }
}