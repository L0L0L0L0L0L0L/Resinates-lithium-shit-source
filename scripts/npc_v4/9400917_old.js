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

var bosses = new Array("Von Leon", "Empress", "Damien", "Magnus", "Lotus", "Lucid", "Will", "Hilla", "Darknell", "Crazy Will", "Ramuramu", "Atilla", "Aragami", "Butcher", "Asura", "Khan", "Riverson", "Capo", "Kobold", "Ark", "Vellum", "Black Bean");
var jobs = new Array("Any Warrior Job", "Any Mage Job", "Any Pirate Job", "Demon Slayer Job", "Mechanic Job", "Any Thief Job", "Ark (Thunder Breaker) Job", "Path Finder (Wind Archer) Job", "Kain (Dawn Warrior) Job", "Any Job");
//var ems = new Array("Von Leon", );
var status;
var battle = 0;
var lap = 0;
var trials = 0;
var triallevel = 0;

var allowed = false;

var item = 4034380;
var status = 0;
var password = 0;
var boost = 0;

var emi;
var level = 0;
var rlevel = 0;
var tier = 0;
var price = 0;

function start() {
    password = cm.random(1000, 9999);
    cm.sendGetText("Please enter the 4 digit Code seen below: \r\n\ " + cm.botTest(password) + "\r\n");
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
            if (cm.getPlayer().isGroup()) {
                var count = Number(cm.getText());
                if (count == password) {
                    var text = "";
                    for (var i = 0; i < bosses.length; i++) {
                        text += "#L" + i + "##bMega " + bosses[i] + "#k#l\r\n";
                    }
                    boost = parseInt(cm.getPlayer().getLevelDataPerc(100) * 0.1);
                    if (boost < 1) {
                        boost = 1;
                    }
                    cm.sendSimpleS("Which Challenge would you like take on?\r\n\#rBosses are enhanced with Elwin Power\r\nto increase number of USE-ETC drops.\r\nEach Boss Cost is Based on Party Size#k\r\n#bElwin Boost:#k #r" + boost + "x#k\r\n" + text + " ", 16);

                } else {
                    cm.sendOkS("Wrong password.", 16);
                }
            } else {
                cm.sendOkS("These battles are for Group or Raid only.", 16);
            }
        } else if (status == 2) {
            emi = bosses[selection];
            var text = "";
            text += "#L0##rTier 80#k - #bPrice 1 Key#k - #rR.Lvl: 5000#k#l\r\n";
            text += "#L1##rTier 85#k - #bPrice 2 Key#k - #rR.Lvl: 6000#k#l\r\n";
            text += "#L2##rTier 90#k - #bPrice 5 Key#k - #rR.Lvl: 6500#k#l\r\n";
            text += "#L3##rTier 95#k - #bPrice 10 Key#k - #rR.Lvl: 7000#k#l\r\n";
            text += "#L4##rTier 99#k - #bPrice 25 Key#k - #rR.Lvl: 7500#k#l\r\n";
            cm.sendSimple("Select which mode you wish to challenge.\r\n" + text);
        } else if (status == 3) {
            if (selection == 0) {
                tier = 80;
                level = 8000;
                rlevel = 5000;
                price = 1;
            }
            if (selection == 1) {
                tier = 85;
                level = 8500;
                rlevel = 6000;
                price = 2;
            }
            if (selection == 2) {
                tier = 90;
                level = 9000;
                rlevel = 6500;
                price = 5;
            }
            if (selection == 3) {
                tier = 95;
                level = 9500;
                rlevel = 7000;
                price = 10;
            }
            if (selection == 4) {
                tier = 99;
                level = 9999;
                rlevel = 7500;
                price = 25;
            }
            if (cm.getPlayer().isGroup()) {
                var em = cm.getEventManager("BMT_" + emi);
                if (em != null) {
                    if (em.getEligiblePartyAch(cm.getPlayer(), rlevel, 408, 1, 4)) {
                        if (cm.haveItem(item, cm.getPlayer().getGroupSize() * price)) {
                            cm.getPlayer().setVar("boost", boost);
                            if (!em.startPlayerInstance(cm.getPlayer(), level, tier)) {
                                cm.sendOkS("Error with event, please report: event: BMT_" + emi, 16);
                            } else {
                                cm.gainItem(item, -(cm.getPlayer().getGroupSize() * price));
                                cm.dispose();
                            }
                        } else {
                            cm.sendOkS("I must have at least " + (cm.getPlayer().getGroupSize() * price) + " #i4034380# to challenge this battle.", 16);
                        }
                    } else {
                        cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map.");
                    }
                } else {
                    cm.sendOkS("Event has already started, Please wait.", 16);
                }
            } else {
                cm.sendOkS("I must take on this Challenge with alone.", 16);
            }
        } else {
            cm.sendOkS("I must take on this Trial alone.", 16);
        }
    }
}