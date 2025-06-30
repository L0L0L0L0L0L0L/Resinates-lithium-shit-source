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
var stam = 50;
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
        var selStr = "Welcome to #bParty Mode#k of Kaotic Dungeons\r\n";
        selStr += "Cost of Entry: #i2000012# #bPrice: 50 Stamina#k\r\n";
        selStr += "Mini-Bosses: #bTrue#k\r\n";
        selStr += "Sub-Bosses: #bTrue#k\r\n";
        selStr += "Final-Boss: #rFalse#k\r\n";
        selStr += "Bot Checks: #bTrue#k\r\n";
        selStr += "Reward Chests: #rFalse#k\r\n";
        selStr += "Damage Cap: #rFalse#k\r\n";
        selStr += "Random Shop: #rFalse#k\r\n";
        selStr += "Level Gain: #rSlow#k\r\n";
        cm.sendYesNo(selStr);
    }
    if (status == 1) {
        if (cm.getPlayer().getStamina() >= stam) {
            if (cm.getPlayer().isGroup()) {

                var selStr = "Welcome to Party Mode of Kaotic Dungeons\r\n#rStamina Cost: " + stam + "#k\r\n#bParty Size: 1-6#k\r\n";
                if (cm.getPlayer().achievementFinished(226)) {
                    selStr += "#L1# #bEasy: Tier: 1 - Waves: 25#k Rank: " + star1 + "#l\r\n";
                } else {
                    selStr += "#L101# #rEasy Tier 1 - (Locked: Tutorial)#k #l\r\n";
                }
                if (cm.getPlayer().achievementFinished(65)) {
                    selStr += "#L2# #bNormal: Tier: 25 - Waves: 50#k Rank: " + star2 + star2 + "#l\r\n";
                } else {
                    selStr += "#L102# #rNormal Tier 25 - (Locked: Lotus)#k #l\r\n";
                }
                if (cm.getPlayer().achievementFinished(181)) {
                    selStr += "#L3# #bHard: Tier: 50 - Waves: 75#k Rank: " + star3 + star3 + star3 + "#l\r\n";
                } else {
                    selStr += "#L103# #rHard Tier 50 - (Locked: C-Will)#k #l\r\n";
                }
                if (cm.getPlayer().achievementFinished(408)) {
                    selStr += "#L4# #bHell: Tier: 75 - Waves: 100#k Rank: " + star4 + star4 + star4 + star4 + star4 + "#l\r\n";
                } else {
                    selStr += "#L104# #rHell Tier 75 - (Locked: Kobold)#k #l\r\n";
                }
                if (cm.getPlayer().achievementFinished(440)) {
                    selStr += "#L5# #bKaotic: Tier: 100 - Waves: 250#k Rank: " + star5 + star5 + star5 + star5 + star5 + star5 + star5 + "#l\r\n";
                } else {
                    selStr += "#L105# #rKaotic Tier 100 - (Locked: Limbo)#k #l\r\n";
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
        if (selection == 101 || selection == 102 || selection == 103 || selection == 104 || selection == 105) {
            if (selection == 101) {
                cm.sendOkS("I must clear Tutorial Zone.", 2);
            }
            if (selection == 102) {
                cm.sendOkS("I must clear Lotus in the Outside World.", 2);
            }
            if (selection == 103) {
                cm.sendOkS("I must clear Command Will in the Outside World.", 2);
            }
            if (selection == 104) {
                cm.sendOkS("I must clear Kobold in the Outside World.", 2);
            }
            if (selection == 105) {
                cm.sendOkS("I must clear Limbo in the Outside World.", 2);
            }
            return;
        }
        var em = cm.getEventManager("MP_Endless");
        if (em != null) {
            if (cm.getPlayer().isGroup()) {
                var floor = 50;
                var tier = 1;
                var mode = 0;
                var level = 10;
                if (selection == 1) {
                    tier = 1;
                    floor = 50;
                    mode = 1;
                    ach = 226;
                    level = 10;
                }
                if (selection == 2) {
                    tier = 25;
                    floor = 100;
                    mode = 2;
                    ach = 65;
                    level = 1000;
                }
                if (selection == 3) {
                    tier = 50;
                    floor = 150;
                    mode = 3;
                    ach = 181;
                    level = 2000;
                }
                if (selection == 4) {
                    tier = 75;
                    floor = 200;
                    mode = 4;
                    ach = 408;
                    level = 4000;
                }
                if (selection == 5) {
                    tier = 100;
                    floor = 250;
                    mode = 5;
                    ach = 440;
                    level = 8000;
                }
                if (cm.getPlayer().getStamina() >= stam) {
                    if (em.getEligiblePartyAch(cm.getPlayer(), level, ach, 1, 6)) {
                        if (!em.startPlayerInstance(cm.getPlayer(), floor, tier, mode)) {
                            cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                        } else {
                            cm.getPlayer().removeStamina(stam);
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