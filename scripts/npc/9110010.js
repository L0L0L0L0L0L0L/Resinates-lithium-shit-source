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
/* NPC Base
 Map Name (Map ID)
 Extra NPC info.
 */

var status;
var ticketId = 5220020;
var cost = 10;
var mapName = ["Henesys", "Ellinia", "Perion", "Kerning City", "Sleepywood", "Mushroom Shrine", "Showa Spa (M)", "Showa Spa (F)", "Ludibrium", "New Leaf City", "El Nath", "Nautilus"];
var curMapName = "";
var option = 0;
var power = 0;
var scale = 1.0;

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
    if (status == 0 && mode == 1) {
        var text = "\r\n";
        text += "#L1##bWhat are the Gacha Rewards?#k#l\r\n";
        text += "#L4#Cash in #bx1#k #i5220020# (#rT:1-10#k)#l\r\n";
        text += "#L5#Cash in #bx10#k #i5220020# (#rT:5-15#k)#l\r\n";
        text += "#L6#Cash in #bx100#k #i5220020# (#rT:10-25#k)#l\r\n";
        text += "#L7#Cash in #bx1000#k #i5220020# (#rT:10-50#k)#l\r\n";
        text += "#L8#Cash in #bx10000#k #i5220020# (#rT:10-99#k)#l\r\n";
        cm.sendSimple("Welcome to the Mega Gachapon. How may I help you?" + text);
    } else if (status == 1) {
        if (selection == 1) {
            var rewards = cm.getAllRewards(9110100);
            if (!rewards.isEmpty()) {
                var text = "The following items can be obtained from this Gachapon.\r\n";
                text += "#rEquips will have random number of Bonus Pots based on Tier#k\r\n\r\n";
                var iter = rewards.iterator();
                while (iter.hasNext()) {
                    var i = iter.next();
                    text += "#i" + i + "#";
                }
            }
            cm.sendOk(text);

        }
        if (selection == 4 || selection == 5 || selection == 6 || selection == 7 || selection == 8) {
            if (selection == 4) {
                cost = 1;
            }
            if (selection == 5) {
                cost = 10;
            }
            if (selection == 6) {
                cost = 100;
            }
            if (selection == 7) {
                cost = 1000;
            }
            if (selection == 8) {
                cost = 10000;
            }
            if (cm.haveItem(ticketId, cost)) {
                option = 1;
                cm.sendYesNo("Hello#b #h ##k, Would you like to exchange a gachapon ticket for some random rewards?");
            } else {
                option = 2;
                cm.sendOk("You don't have #r" + cost + "#k #i5220020#. You can find these by Talking to Dame or finding them extremely rare from monsters.");
            }
        }
    } else if (status == 2 && option == 1) {
        if (cm.haveItem(ticketId, cost)) {
            if (cost == 1) {
                scale = cm.random(1, 10);
            }
            if (cost == 10) {
                scale = cm.random(5, 15);
            }
            if (cost == 100) {
                scale = cm.random(10, 25);
            }
            if (cost == 1000) {
                scale = cm.random(10, 50);
            }
            if (cost == 10000) {
                scale = cm.random(10, 99);
            }
            //make sure you check for the gachapon tickets being present in their inventory first
            var rewards = cm.getRewards(9110100, cm.getPlayer(), 1, scale);
            if (rewards == null) {
                cm.sendOk("#rPlease make sure you have at least 1 free slot in each tab.#k");
            } else {
                cm.gainItem(ticketId, -cost);
                if (rewards.size() == 0) {
                    cm.sendOk("You didn't get anything. Better luck next time.");
                } else {
                    var text = "You received the following :\r\n\r\n";
                    var iter = rewards.iterator();
                    while (iter.hasNext()) {
                        var i = iter.next();
                        text += "#i" + i.getItemId() + "# #t" + i.getItemId() + "# " + i.getQuantity() + "x\r\n\r\n";
                    }
                    if (cm.haveItem(ticketId, cost) && cm.canHoldGachReward()) {
                        cm.sendYesNo(text + " Would you like to use another #r" + cost + "#k Gold Gachapon Tickets?");
                    } else {
                        cm.sendOk(text);
                    }
                }
            }
        } else {
            cm.sendOk("You don't have #r" + cost + "#k #i5220020#.");

        }

    } else if (status == 1 && option == 2) {
        if (selection == 0) {
            cm.sendOk("Play Gachapon to earn rare scrolls, equipment, chairs, mastery books, and other cool items! All you need is a #bGachapon Ticket#k to be the winner of a random mix of items.");
        } else {
            cm.sendOk("You don't have enough #i5220020#. You can find these by Talking to Dame.");
        }
    } else if (status == 2 && option == 2) {
        cm.sendOk("You'll find a variety of items from the Gachapon, but you'll most likely find items and scrolls related to.");
    } else if (status == 3) {
        status = 3;
        action(0, 0, 0);
    }
}