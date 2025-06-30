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
        var number1 = cm.random(1, 9);
        var number2 = cm.random(1, 9);
        var number3 = cm.random(1, 9);
        var number4 = cm.random(1, 9);
        password = cm.getCode(number1, number2, number3, number4);
        cm.sendGetText("#rBotting beyond this point is not allowed!\r\n\#kPlease enter the 4 digit #rAnti-Bot#k Code seen below:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "");
    }
    if (status == 1) {
        amount = cm.getNumber();
        if (amount == password) {
            if (cm.getPlayer().getStamina() >= stam) {
                if (cm.getPlayer().isGroup()) {
                    var selStr = "Welcome to Perm Zone of Kaotic Dungeons\r\n#bPrice: " + cm.getItemName(4000435) + "#k\r\n#bParty Size: Solo(1)#k\r\n";
                    selStr += "#L1# #bEasy: Tier: 1 - Waves: 100#k #rCost: 1#k " + star1 + "#l\r\n";
                    if (cm.getPlayer().achievementFinished(270)) {
                        selStr += "#L2# #bNormal: Tier: 20 - Waves: 100#k #rCost: 2#k " + star2 + star2 + "#l\r\n";
                    }
                    if (cm.getPlayer().achievementFinished(271)) {
                        selStr += "#L3# #bHard: Tier: 40 - Waves: 100#k #rCost: 3#k " + star3 + star3 + star3 + "#l\r\n";
                    }
                    if (cm.getPlayer().achievementFinished(272)) {
                        selStr += "#L4# #bHell: Tier: 60 - Waves: 100#k #rCost: 4#k " + star4 + star4 + star4 + star4 + star4 + "#l\r\n";
                    }
                    if (cm.getPlayer().achievementFinished(273)) {
                        selStr += "#L5# #bKaotic: Tier: 80 - Waves: 100#k #rCost: 5#k " + star5 + star5 + star5 + star5 + star5 + star5 + star5 + "#l\r\n";
                    }
                    cm.sendSimple(selStr);
                } else {
                    cm.sendOkS("I must take on this Challenge Party.", 2);
                }
            } else {
                cm.sendOkS("I still need to recover more stamina for entering here...", 2);
            }
        } else {
            cm.sendOk("Wrong password mother fka.");
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
                if (selection == 1) {
                    tier = 1;
                    floor = 50;
                    mode = 21;
                    ach = 226;
                    cost = 1;
                }
                if (selection == 2) {
                    tier = 20;
                    floor = 100;
                    mode = 22;
                    ach = 270;
                    cost = 2;
                }
                if (selection == 3) {
                    tier = 40;
                    floor = 150;
                    mode = 23;
                    ach = 271;
                    cost = 3;
                }
                if (selection == 4) {
                    tier = 60;
                    floor = 200;
                    mode = 24;
                    ach = 272;
                    cost = 4;
                }
                if (selection == 5) {
                    tier = 80;
                    floor = 250;
                    mode = 25;
                    ach = 273;
                    cost = 5;
                }
                if (cm.haveItem(4000435, cost)) {
                    if (em.getEligiblePartyAch(cm.getPlayer(), 10, ach, 1, 1)) {
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