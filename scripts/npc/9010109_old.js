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
var mapName = ["Henesys", "Ellinia", "Perion", "Kerning City", "Sleepywood", "Mushroom Shrine", "Showa Spa (M)", "Showa Spa (F)", "Ludibrium", "New Leaf City", "El Nath", "Nautilus"];
var curMapName = "";
var option = 0;
var power = 0;

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
        cm.sendSimple("Welcome to the Mega Gachapon. How may I help you?\r\n\r\n#L5#Cash in #i5220020#?#l\r\n#L4#What are the rewards in this Gold Gachapon?#l");
    } else if (status == 1) {
        if (selection == 4) {
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
        if (selection == 5) {
            power = cm.getPlayer().getTotalLevel() * 25;
            if (cm.haveItem(ticketId, 1)) {
                option = 1;
                cm.sendYesNo("Hello#b #h ##k, Would you like to exchange a gachapon ticket for some random rewards?\r\n#kEach reward is based on your True level and Item Drop Power with random tier bonus added.\r\n\r\n#kYour current Gachapon Power is " + power);
                //cm.sendYesNo("You may use the " + curMapName + " Gachapon. Would you like to use your Gachapon ticket?");
            } else {
                option = 2;
                cm.sendOk("You don't have enough #i5220020#. You can find these by Talking to Dame.");

            }
        }


    } else if (status == 2 && option == 1) {
        var amount = 1;
        if (cm.haveItem(ticketId, amount) && amount > 0) {
            //make sure you check for the gachapon tickets being present in their inventory first
            var rewards = cm.getRewardsByPower(9110010, cm.getPlayer(), power, 26);
            if (rewards == null) {
                cm.sendOk("#rPlease make sure you have at least 1 free slot in each tab.#k");

            } else {
                cm.gainItem(ticketId, -amount);
                if (rewards.size() == 0) {
                    cm.sendOk("You didn't get anything. Better luck next time.");
                } else {
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
            cm.sendOk("You don't have enough #i5220020#. You can find these by Talking to Dame.");

        }

    } else if (status == 1 && option == 2) {
        if (selection == 0) {
            cm.sendOk("Play Gachapon to earn rare scrolls, equipment, chairs, mastery books, and other cool items! All you need is a #bGachapon Ticket#k to be the winner of a random mix of items.");
        } else {
            cm.sendOk("You don't have enough #i5220020#. You can find these by Talking to Dame.");
        }
    } else if (status == 2 && option == 2) {
        cm.sendOk("You'll find a variety of items from the Gachapon, but you'll most likely find items and scrolls related to.");
    } else {

    }
}