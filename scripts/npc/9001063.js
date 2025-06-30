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
var level = 10;
var dp = 4310502;
var amount = 50;
var letter = 4009180;
var ach = 226;
var ppl = new Array(0, 4, 4, 4, 4, 4);
var stam = 100;
var passowrd = 0;
var star1 = "#fUI/Custom.img/star/1#";
var star2 = "#fUI/Custom.img/star/2#";
var star3 = "#fUI/Custom.img/star/3#";
var star4 = "#fUI/Custom.img/star/4#";
var star5 = "#fUI/Custom.img/star/5#";

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
        var selStr = "Welcome to #bPremium Party Mode#k of Kaotic Dungeons\r\n";
        selStr += "Cost of Entry: #bPrice: #i4000435# " + cm.getItemName(4000435) + "#k\r\n";
        selStr += "Waves: #r100#k\r\n";
        selStr += "Mini-Bosses: #bTrue#k\r\n";
        selStr += "Sub-Bosses: #bTrue#k\r\n";
        selStr += "Final-Boss: #bTrue#k\r\n";
        selStr += "Bot Checks: #bTrue#k\r\n";
        selStr += "Reward Chests: #bTrue#k\r\n";
        selStr += "Damage Cap: #rFalse#k\r\n";
        selStr += "Random Shop: #rFalse#k\r\n";
        selStr += "Level Gain: #rFalse#k\r\n";
        selStr += "Party Size: #r1-3#k\r\n";
        cm.sendYesNo(selStr);
    }
    if (status == 1) {
        if (cm.getPlayer().getStamina() >= stam) {
            if (cm.getPlayer().isGroup()) {
                var selStr = "Welcome to Perm Zone of Kaotic Dungeons\r\n#bPrice: #i4000435# " + cm.getItemName(4000435) + "#k\r\n#bParty Size: Solo(1)#k\r\n";
                if (cm.getPlayer().achievementFinished(226)) {
                    selStr += "#L1# #bEasy: Tier: 1 - Waves: 100#k #rCost: 1#k " + star1 + "#l\r\n";
                }
                if (cm.getPlayer().achievementFinished(271)) {
                    selStr += "#L2# #bNormal: Tier: 25 - Waves: 100#k #rCost: 2#k " + star2 + star2 + "#l\r\n";
                }
                if (cm.getPlayer().achievementFinished(272)) {
                    selStr += "#L3# #bHard: Tier: 50 - Waves: 100#k #rCost: 3#k " + star3 + star3 + star3 + "#l\r\n";
                }
                if (cm.getPlayer().achievementFinished(273)) {
                    selStr += "#L4# #bHell: Tier: 100 - Waves: 100#k #rCost: 4#k " + star4 + star4 + star4 + star4 + star4 + "#l\r\n";
                }
                if (cm.getPlayer().achievementFinished(274)) {
                    selStr += "#L5# #bKaotic: Tier: 250 - Waves: 100#k #rCost: 5#k " + star5 + star5 + star5 + star5 + star5 + star5 + star5 + "#l\r\n";
                }
                cm.sendSimple(selStr);
            } else {
                cm.sendOkS("I must take on this Challenge Party.", 2);
            }
        } else {
            cm.sendOkS("I still need to recover more stamina for entering here...", 2);
        }
    }
    if (status == 2) {
        var em = cm.getEventManager("MP_Endless");
        if (em != null) {
            if (cm.getPlayer().isGroup()) {
                var floor = 50;
                var tier = 1;
                var mode = 0;
                var cost = 1;
                var level = 10;
                if (selection == 1) {
                    tier = 1;
                    floor = 50;
                    mode = 20;
                    ach = 226;
                    cost = 1;
                    level = 10;
                }
                if (selection == 2) {
                    tier = 25;
                    floor = 100;
                    mode = 21;
                    ach = 270;
                    cost = 2;
                    level = 100;
                }
                if (selection == 3) {
                    tier = 50;
                    floor = 150;
                    mode = 22;
                    ach = 271;
                    cost = 3;
                    level = 250;
                }
                if (selection == 4) {
                    tier = 75;
                    floor = 200;
                    mode = 23;
                    ach = 272;
                    cost = 4;
                    level = 500;
                }
                if (selection == 5) {
                    tier = 100;
                    floor = 250;
                    mode = 24;
                    ach = 273;
                    cost = 5;
                    level = 1000;
                }
                if (cm.haveItem(4000435, cost)) {
                    if (em.getEligiblePartyAch(cm.getPlayer(), level, ach, 1, 3)) {
                        if (!em.startPlayerInstance(cm.getPlayer(), floor, tier, mode)) {
                            cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                        } else {
                            cm.gainItem(4000435, -cost);
                            cm.dispose();
                        }
                    } else {
                        cm.sendOk("Someone is missing something.");
                    }
                } else {
                    cm.sendOk("Not enough stamina.");
                }
            } else {
                cm.sendOkS("I must take on this Challenge Party.", 2);
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }
    }
}