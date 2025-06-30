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
var status = 0;
var items = 4009217;
var amount = 250000;
var reward = 4034031;
var rewamount = 100;
var exp = 250000;
var questid = 712;
var questtime = 28800;//30 min
var job = "thieves";
var option = 0;


//cm.sendYesNo("Hello#b #h ##k, Would you like to travel to Arboren?");
//cm.sendOk("You");
//cm.getPlayer()

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
        if (cm.getPlayer().isGM()) {
            cm.sendYesNo("If you wish to speak to my Boss, you must have #i4034031#k to get by...");
        } else {
            if (cm.getPlayer().achievementFinished(406) && cm.getPlayer().achievementFinished(407)) {
                cm.sendYesNo("You must have #i4034031#k to get by...");
            } else {
                cm.sendOk("Go away peasent, and finish Commiceri Bosses.");
            }
        }
    } else if (status == 1) {
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                if (cm.getPlayer().getTotalLevel() >= 5000) {
                    var text = "Present your #i4034031# #b#z4034031#'s#k:\r\n\r\n";
                    text += "#L1# #bTier: 80#k (#rPrice: FREE#k)#l\r\n";
                    if (cm.getPlayer().isGM() || cm.getPlayer().achievementFinished(408)) {
                        text += "#L2# #bTier: 85#k (#rPrice: 10#k)#l\r\n";
                        text += "#L3# #bTier: 90#k (#rPrice: 100#k)#l\r\n";
                    }
                    cm.sendSimple(text);
                } else {
                    cm.sendOk("The leader of the party must be level 5000 or higher to complete this event.");
                }
            } else {
                cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
            }
        } else {
            cm.sendOk("Event is Party Mode Only.");
        }
    } else if (status == 2) {
        em = cm.getEventManager("Kobold");
        var scale = 0;
        var ach = 407;
        level = 5000;
        if (selection == 1) {
            cost = 0;
            scale = 80;
        } else if (selection == 2) {
            cost = 10;
            scale = 85;
            ach = 408;
        } else if (selection == 3) {
            cost = 100;
            scale = 90;
            ach = 408;
        } else if (selection == 4) {
            cost = 100;
            scale = 95;
            ach = 408;
        } else if (selection == 5) {
            cost = 250;
            scale = 99;
            ach = 408;
        }
        var key = true;
        var price = false;
        if (cost > 0) {
            if (cm.haveItem(4034031, cost)) {
                key = true;
                price = true;
            } else {
                key = false;
            }
        }
        if (key) {
            if (em != null) {
                if (em.getEligiblePartyAch(cm.getPlayer(), 5000, ach)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level, scale)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        if (price) {
                            cm.gainItem(4034031, -cost);
                        }
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        } else {
            cm.sendOk("Sorry, your team does not have enough keys to enter.");
        }
    } else if (status == 3) {
    } else {
        cm.dispose();
    }
}


