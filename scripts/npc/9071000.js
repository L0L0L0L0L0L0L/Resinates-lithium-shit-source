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
var ticket = 0;

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
        var selStr = "Welcome to #bParty Endless Mode#k of Kaotic Dungeons\r\n";
        selStr += "Cost of Entry: #i2000012# #bPrice: 100 Stamina#k\r\n";
        selStr += "Waves: #rEndless#k\r\n";
        selStr += "Mini-Bosses: #bTrue#k\r\n";
        selStr += "Sub-Bosses: #bTrue#k\r\n";
        selStr += "Final-Boss: #rFalse#k\r\n";
        selStr += "Bot Checks: #bTrue#k\r\n";
        selStr += "Reward Chests: #rFalse#k\r\n";
        selStr += "Damage Cap: #rTrue#k\r\n";
        selStr += "Random Shop: #bTrue#k\r\n";
        selStr += "Level Gain: #rFast#k\r\n";
        cm.sendYesNo(selStr);
    }
    if (status == 1) {
        if (cm.getPlayer().getStamina() >= stam) {
            if (cm.getPlayer().isGroup()) {
                if (cm.getPlayer().achievementFinished(270)) {
                    var selStr = "Welcome to Party Mode of Kaotic Dungeons\r\n#rStamina Cost: " + stam + "#k\r\n#bParty Size: 1-6#k\r\n";
                    selStr += "#L1##i4420058##l ";
                    if (cm.getPlayer().achievementFinished(271)) {
                        selStr += "#L2##i4420059##l ";
                    }
                    if (cm.getPlayer().achievementFinished(272)) {
                        selStr += "#L3##i4420060##l ";
                    }
                    if (cm.getPlayer().achievementFinished(273)) {
                        selStr += "#L4##i4420061##l ";
                    }
                    if (cm.getPlayer().achievementFinished(274)) {
                        selStr += "#L5##i4420062##l ";
                    }
                    cm.sendSimple(selStr);
                } else {
                    cm.sendOkS("I must clear Party Mode Easy first.", 2);
                }
            } else {
                cm.sendOkS("I must take on this Challenge Party.", 2);
            }
        } else {
            cm.sendOkS("I still need to recover more stamina for entering here...", 2);
        }
    }
    if (status == 2) {
        var em = cm.getEventManager("MP_Endless_End");
        if (em != null) {
            if (cm.getPlayer().isGroup()) {
                var tier = 1;
                var mode = 0;
                if (selection == 1) {
                    tier = 1;
                    mode = 10;
                    ach = 270;
                    ticket = 4420058;
                }
                if (selection == 2) {
                    tier = 25;
                    mode = 11;
                    ach = 271;
                    ticket = 4420059;
                }
                if (selection == 3) {
                    tier = 50;
                    mode = 12;
                    ach = 272;
                    ticket = 4420060;
                }
                if (selection == 4) {
                    tier = 75;
                    mode = 13;
                    ach = 273;
                    ticket = 4420061;
                }
                if (selection == 5) {
                    tier = 100;
                    mode = 14;
                    ach = 274;
                    ticket = 4420062;
                }
                if (cm.haveItem(ticket, 1)) {
                    if (cm.getPlayer().getStamina() >= stam) {
                        if (em.getEligiblePartyAch(cm.getPlayer(), 10, ach, 1, 6)) {
                            if (!em.startPlayerInstance(cm.getPlayer(), 999, tier, mode)) {
                                cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                            } else {
                                cm.gainItem(ticket, -1);
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
                    cm.sendOk("Not enough tickets.");
                }
            } else {
                cm.sendOkS("I must take on this Challenge Party.", 2);
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }
    }
}