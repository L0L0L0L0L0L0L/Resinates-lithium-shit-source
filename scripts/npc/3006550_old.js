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

var bosses = new Array("Black Bean", "Von Leon", "Empress", "Damien", "Magnus", "Lotus", "Lucid", "Will", "Hilla", "Darknell", "Crazy Will", "Ramuramu", "Atilla", "Aragami", "Butcher", "Asura", "Khan", "Riverson", "Capo", "Kobold", "Tree", "Horseman", "Slime", "Seren", "Kalos", "Kaling", "Lotus Hard", "Limbo");
var jobs = new Array("Any Warrior Job", "Any Mage Job", "Any Pirate Job", "Demon Slayer Job", "Mechanic Job", "Any Thief Job", "Ark (Thunder Breaker) Job", "Path Finder (Wind Archer) Job", "Kain (Dawn Warrior) Job", "Any Job");
//var ems = new Array("Von Leon", );
var tiers = new Array(275, 275, 300, 300, 325, 350, 375, 400, 425, 450, 475, 500, 525, 550, 575, 600, 625, 650, 675, 700, 725, 750, 775, 800, 820, 840, 860, 880, 900);
var cost = new Array(1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 6, 6, 7, 7, 8, 9, 10);
var level = new Array(10, 10, 15, 15, 20, 20, 25, 25, 30, 30, 35, 40, 45, 50, 50, 50, 50, 55, 60, 65, 70, 75, 80, 85, 90, 90, 90, 90, 90);
var ach = new Array(51, 39, 70, 66, 65, 64, 50, 69, 180, 181, 400, 401, 402, 403, 404, 405, 406, 407, 408, 157, 158);
var status;
var battle = 0;
var lap = 0;
var trials = 0;
var triallevel = 0;
var emi;
var allowed = false;
var tier = 0;
var item = 4001016;
var status = 0;
var password = 0;
var boost = 0;
var password = 0;
var stam = 10;

function start() {
    var number1 = cm.random(1, 9);
    var number2 = cm.random(1, 9);
    var number3 = cm.random(1, 9);
    var number4 = cm.random(1, 9);
    password = cm.getCode(number1, number2, number3, number4);
    cm.sendGetText("#rWhen you see a Gacha Code like this, it means botting inside these instances or event is not allowed.#k\r\nFailing the code does not cause any harm but to your ego.\r\n\Please enter the 4 digit #rAnti-Bot#k Code seen below:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "");
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

        if (status == 1) {
            amount = cm.getNumber();
            if (amount == password) {
                if (cm.getPlayer().isGroup()) {
                    var text = "";
                    for (var i = 0; i < bosses.length; i++) {
                        if (cm.getPlayer().getAchievement(441)) {
                            text += "#L" + i + "##bKaotic " + bosses[i] + "#k - (#rT: " + tiers[i] + " - Reborn: " + level[i] + " KEY: " + (cost[i] * 5) + "#k)#l\r\n";
                        }
                    }
                    cm.sendSimpleS("Which #rParty Challenge#k would you like take on?\r\n#rMax Party Size: 6#k\r\n" + text + " ", 16);
                } else {
                    cm.sendOkS("These battles are for Raids only.", 16);
                }
            } else {
                cm.sendOk("Wrong password.");
            }
        } else if (status == 2) {
            emi = bosses[selection];
            tier = tiers[selection];
            stam = cost[selection] * 250;
            var price = cost[selection] * 5;
            if (cm.getPlayer().isGroup()) {
                if (cm.haveItem(4001016, price)) {
                        var em = cm.getEventManager("BMT_" + emi);
                        if (em != null) {
                            cm.getPlayer().setVar("boost", boost);
                            if (em.getEligiblePartyAchReborn(cm.getPlayer(), 9000, 441, 1, 6, level[selection])) {
                                if (!em.startPlayerInstance(cm.getPlayer(), 9999, tier)) {
                                    cm.sendOkS("Error with event, please report: event: BMT_" + emi, 16);
                                } else {
                                    cm.gainItem(4001016, -price);
                                    cm.dispose();
                                }
                            } else {
                                cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: #rReborns " + level[selection] + "#k, 1+ Raid members.");
                            }
                        } else {
                            cm.sendOkS("Event has already started, Please wait.", 16);
                        }
                } else {
                    cm.sendOkS("You do not have enough stamina to enter the event, the cost is #r" + price + "#k #i4001016#.", 16);
                }
            } else {
                cm.sendOkS("I must take on this Challenge alone.", 16);
            }
        } else {
            cm.sendOkS("I must take on this Trial alone.", 16);
        }
    }
}