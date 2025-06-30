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
var ticketId = 4310101;
var mapName = ["Henesys", "Ellinia", "Perion", "Kerning City", "Sleepywood", "Mushroom Shrine", "Showa Spa (M)", "Showa Spa (F)", "Ludibrium", "New Leaf City", "El Nath", "Nautilus"];
var curMapName = "";
var option = 0;
var power = 0;
var scale = 1;
var cost = 0;

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
        var text = "Current Gambling Power: #b+" + cm.getPlayer().getLevelData(101) + "#k\r\n";
        text += "#L1##bWhat are the rewards in this Casino Gachapon?#k#l\r\n\r\n";
        text += "#L3#Cash in #r1000#k #i4310101# for #bTier: 10#k?#l\r\n";
        text += "#L4#Cash in #r2500#k #i4310101# for #bTier: 20#k?#l\r\n";
        text += "#L5#Cash in #r10000#k #i4310101# for #bTier: 30#k?#l\r\n";
        text += "#L7#Cash in #r50000#k #i4310101# for #bTier: 40#k?#l\r\n";
        text += "#L9#Cash in #r250000#k #i4310101# for #bTier: 50#k?#l\r\n ";

        cm.sendSimple("Welcome to the Casino Gachapon.\r\nHow may I help you?\r\n" + text);
    } else if (status == 1) {
        if (selection == 1) {
            var rewards = cm.getAllRewards(9110010);
            if (!rewards.isEmpty()) {
                var text = "The following items can be obtained from this Gachapon :\r\n\r\n";
                var iter = rewards.iterator();
                while (iter.hasNext()) {
                    var i = iter.next();
                    text += "#i" + i + "#";
                }
            }
            cm.sendOk(text);
        }
        if (selection >= 3) {
            if (selection == 3) {
                cost = 1000;
                scale = 10;
            }
            if (selection == 4) {
                cost = 2500;
                scale = 20;
            }
            if (selection == 5) {
                cost = 10000;
                scale = 30;
            }
            if (selection == 7) {
                cost = 50000;
                scale = 40;
            }
            if (selection == 9) {
                cost = 250000;
                scale = 50;
            }
            if (cm.haveItem(ticketId, cost)) {
                option = 1;
                cm.sendYesNo("Hello#b #h ##k, Would you like to exchange #r" + cost + "#k - #i4310101# for a random Tier: #b" + scale + "#k Equip?");
            } else {
                option = 2;
                cm.sendOk("You don't have enough #i4310101#. You can find these by winning at the casino.");
            }
        }


    } else if (status == 2 && option == 1) {
        if (cm.haveItem(ticketId, cost)) {
            //make sure you check for the gachapon tickets being present in their inventory first

            var rewards = cm.getRewardsByPower2(9110010, cm.getPlayer(), 100 + cm.getPlayer().getLevelData(101), scale);
            if (rewards == null) {
                cm.sendOk("#rPlease make sure you have at least 1 free slot in each tab.#k");
            } else {
                if (rewards.size() == 0) {
                    cm.sendOk("You didn't get anything. Better luck next time.");
                } else {
                    cm.gainItem(ticketId, -cost);
                    var text = "You received the following :\r\n\r\n";
                    var iter = rewards.iterator();
                    while (iter.hasNext()) {
                        var i = iter.next();
                        text += "#i" + i.getItemId() + "# #t" + i.getItemId() + "# " + i.getQuantity() + "x\r\n\r\n";
                    }
                    cm.sendOk(text);
                }
            }
        } else {
            cm.sendOk("You don't have enough #i4310101#. You can find these by winning at the casino.");
        }

    } else if (status == 1 && option == 2) {
        if (selection == 0) {
            cm.sendOk("Come back if your interested.");
        } else {
            cm.sendOk("You don't have enough #i4310101#. You can find these by winning at the casino.");
        }
    } else if (status == 2 && option == 2) {
        cm.sendOk("You don't have enough #i4310101#. You can find these by winning at the casino.");
    } else if (status == 3) {
        status = 3;
        action(0, 0, 0);
    }
}