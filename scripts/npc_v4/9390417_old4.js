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

var bosses = new Array("Von Leon", "Empress", "Damien", "Magnus", "Lotus", "Lucid", "Will", "Hilla", "Darknell", "Crazy Will", "Ramuramu", "Atilla", "Aragami", "Butcher", "Asura", "Khan", "Riverson", "Capo", "Kobold", "Tree", "Horseman");
var jobs = new Array("Any Warrior Job", "Any Mage Job", "Any Pirate Job", "Demon Slayer Job", "Mechanic Job", "Any Thief Job", "Ark (Thunder Breaker) Job", "Path Finder (Wind Archer) Job", "Kain (Dawn Warrior) Job", "Any Job");
//var ems = new Array("Von Leon", );
var tiers = new Array(50, 50, 55, 55, 55, 60, 60, 65, 65, 65, 70, 75, 75, 75, 75, 80, 80, 85, 90, 95, 99);
var cost = new Array(1, 1, 2, 2, 2, 3, 3, 4, 4, 4, 5, 6, 6, 6, 6, 7, 8, 8, 9, 10, 10);
var level = new Array(2500, 2500, 2500, 2500, 3000, 3500, 4000, 4500, 5000, 5500, 6000, 6500, 7000, 7500, 7600, 7700, 7800, 7900, 8000, 8250, 8500);
var ach = new Array(51, 39, 70, 66, 65, 64, 50, 69, 180, 181, 400, 401, 402, 403, 404, 405, 406, 407, 408, 157, 158);
var status;
var battle = 0;
var lap = 0;
var trials = 0;
var triallevel = 0;
var emi;
var allowed = false;
var tier = 0;
var item = 4000999;
var status = 0;
var password = 0;
var boost = 0;

function start() {
    cm.sendYesNo("Are you sure you want to cash in #i" + item + "# for some Bosses?");
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
                if (!cm.getPlayer().isGroup()) {
                    var count = cm.getNumber();
                    if (count == password) {
                        var text = "";
                        for (var i = 0; i < bosses.length; i++) {
                            if (cm.getPlayer().getAchievement(ach[i])) {
                                if (cm.getPlayer().getTotalLevel() >= level[i]) {
                                    text += "#L" + i + "##bKaotic " + bosses[i] + " - (T: " + tiers[i] + " - Lvl: " + level[i] + " Cost: " + cost[i] + ")#k#l\r\n";
                                }
                            }
                        }
                        cm.sendSimpleS("Which Challenge would you like take on?\r\nEach boss costs #i" + item + "#.#k\r\n" + text + " ", 16);
                    } else {
                        cm.sendOkS("Wrong password.", 16);
                    }
                } else {
                    cm.sendOkS("These battles are for Solo only.", 16);
                }
            } else {
                cm.sendOk("Wrong password.");
            }
        } else if (status == 2) {
            emi = bosses[selection];
            tier = tiers[selection];
            if (!cm.getPlayer().isGroup()) {
                var em = cm.getEventManager("BMT_" + emi);
                if (em != null) {
                    if (cm.haveItem(item, cost[selection])) {
                        cm.getPlayer().setVar("boost", boost);
                        if (!em.startPlayerInstance(cm.getPlayer(), level[selection], tier)) {
                            cm.sendOkS("Error with event, please report: event: BMT_" + emi, 16);
                        } else {
                            cm.gainItem(item, -cost[selection]);
                            cm.dispose();
                        }
                    } else {
                        cm.sendOkS("I must have at least " + cost[selection] + " Donation to challenge this battle.", 16);
                    }
                } else {
                    cm.sendOkS("Event has already started, Please wait.", 16);
                }
            } else {
                cm.sendOkS("I must take on this Challenge alone.", 16);
            }
        } else {
            cm.sendOkS("I must take on this Trial alone.", 16);
        }
    }
}