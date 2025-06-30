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

var bosses2 = new Array("Black Bean", "Von Leon", "Empress", "Damien", "Magnus", "Lotus", "Lucid", "Will", "Hilla", "Darknell",
        "C-Will", "Ramu", "Atilla", "Aragami", "Butcher", "Asura", "Khan", "Riverson", "Capo", "Kobold", "Tree", "Horseman", "Slime",
        "Seren", "Kalos", "Kaling", "Lotus Hard", "Limbo", "Limbo Hard", "Grand Ape");
var bosses = new Array("Black Bean", "Von Leon", "Empress", "Damien", "Magnus", "Lotus", "Lucid", "Will", "Hilla", "Darknell",
        "C-Will", "Ramu", "Atilla", "Aragami", "Butcher", "Asura", "Khan", "Riverson", "Capo", "Kobold", "Tree", "Horseman");
//var ems = new Array("Von Leon", );
var tiers = new Array(5, 10, 15, 15, 20, 20, 25, 25, 30, 30, 35, 40, 45, 50, 50, 50, 50, 55, 60, 65, 70, 75, 80, 80, 85, 85, 90, 95, 100, 100);
var cost = new Array(1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 6, 6, 7, 7, 8, 9, 9, 10);
var lvl = new Array(100, 200, 300, 500, 750, 1000, 1250, 1500, 1750, 2000, 2500, 3000, 3200, 3400, 3600, 3800, 4000, 4250, 4500, 5000, 5500, 6000, 7000, 7500, 8000, 8500, 9000, 9500, 9999, 9999);
var ach = new Array(226, 270, 270, 270, 270, 270, 271, 271, 271, 271, 271, 271, 272, 272, 272, 272, 272, 272, 272, 272, 272, 273, 273, 273, 273, 273, 274, 274, 274, 274, 274, 274, 274, 274);
var ach2 = new Array(72, 51, 39, 70, 66, 65, 64, 50, 69, 180, 181, 400, 401, 402, 403, 404, 405, 406, 407, 408, 157, 158,422, 417, 425, 435, 438, 440, 441, 442);
var status;
var battle = 0;
var lap = 0;
var trials = 0;
var triallevel = 0;
var emi;
var allowed = false;
var tier = 0;
var item = 4420091;
var status = 0;
var password = 0;
var boost = 0;
var password = 0;
var stam = 10;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";

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
            var count = 0;
            if (amount == password) {
                if (cm.getPlayer().isGroup()) {
                    var text = "";
                    for (var i = 0; i < bosses.length; i++) {
                        if (cm.getPlayer().getAchievement(ach[i])) {
                            if (cm.getPlayer().getAchievement(ach2[i])) {
                                text += "#L" + i + "#" + star + "#rT: " + tiers[i] + "#k: #b" + bosses[i] + "#k (#rL. " + lvl[i] + "#k - #bBoss Souls: " + (cost[i]) + "#k)#l\r\n";
                            } else {
                                text += "#L" + i + "##rT: " + tiers[i] + "#k: #b" + bosses[i] + "#k (#rL. " + lvl[i] + "#k - #bBoss Souls: " + (cost[i]) + "#k)#l\r\n";
                            }
                            count++;
                       }
                    }
                    if (count > 0) {
                        cm.sendSimpleS("Which #rParty Challenge#k would you like take on?\r\n#rMax Party Size: 6#k\r\n#rBoss Souls required to enter #i" + item + "# " + cm.getItemName(item) + "#k\r\n" + text + " ", 16);
                    } else {
                        cm.sendOkS("I must clear Tutorial Zone to unlock this mission.", 16);
                    }
                } else {
                    cm.sendOkS("These battles are for Groups only.", 16);
                }
            } else {
                cm.sendOk("Wrong password.");
            }
        } else if (status == 2) {
            emi = bosses[selection];
            tier = tiers[selection];
            stam = cost[selection] * 10;
            var price = cost[selection];
            if (cm.haveItem(item, price)) {
                if (cm.getPlayer().isGroup()) {
                    var em = cm.getEventManager("BMT_" + emi);
                    if (em != null) {
                        if (em.getEligiblePartyAchReborn(cm.getPlayer(), lvl[selection], ach[selection], 1, 6, 0)) {
                            if (!em.startPlayerInstance(cm.getPlayer(), lvl[selection], tier)) {
                                cm.sendOkS("Error with event, please report: event: BMT_" + emi, 16);
                            } else {
                                cm.gainItem(item, -price);
                                cm.dispose();
                            }
                        } else {
                            cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: #rReborns " + level[selection] + "#k, 1+ Raid members.");
                        }
                    } else {
                        cm.sendOkS("Event has already started, Please wait.", 16);
                    }
                } else {
                    cm.sendOkS("I must take on this Challenge in a group.", 16);
                }
            } else {
                cm.sendOkS("Key Item required to enter x" + cost[selection] + " " + cm.getItemName(item), 16);
            }
        } else {
            cm.sendOkS("I must take on this Trial alone.", 16);
        }
    }
}