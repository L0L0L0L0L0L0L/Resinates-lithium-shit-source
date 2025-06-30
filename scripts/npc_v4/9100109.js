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
var ticketId = 5220000;
var mapName = ["Henesys", "Ellinia", "Perion", "Kerning City", "Sleepywood", "Mushroom Shrine", "Showa Spa (M)", "Showa Spa (F)", "Ludibrium", "New Leaf City", "El Nath", "Nautilus"];
var curMapName = "";
var option = 0;

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
        cm.sendSimple("Welcome to the Great Gachapon. How may I help you?\r\n\r\n#L5#Cash in Gachapon tickets?#l\r\n#L4#What are the rewards in this Gachapon?#l");
    } else if (status == 1) {
        if (selection == 4) {
            var rewards = cm.getAllRewards(cm.getNpc());
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
        if (selection == 0 || selection == 5) {
            if (cm.canHoldGachReward()) {
                if (cm.haveItem(ticketId)) {
                    cm.sendYesNo("Hello#b #h ##k, Would you like to exchange a gachapon ticket for some random rewards?");
                    //cm.sendYesNo("You may use the " + curMapName + " Gachapon. Would you like to use your Gachapon ticket?");
                } else {
                    cm.sendOk("You don't have enough Gachapon Tickets. You can find these tickets from monster drops or SHOP.");

                }
            } else {
                cm.sendOk("You don't have enough inventory space inside EQUIP or USE to collect the reward.");

            }
        }


    } else if (status == 2) {
        var amount = 1;
        if (cm.canHoldGachReward()) {
            if (cm.haveItem(ticketId, amount) && amount > 0) {
                //make sure you check for the gachapon tickets being present in their inventory first
                var scale = cm.random(1, 3);
                var power = (cm.getPlayer().getTotalLevel());
                var rewards = cm.getRewards(cm.getNpc(), cm.getPlayer(), 1, scale);
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
                            if (scale == 1 && cm.isEquip(i)) {
                                cm.getPlayer().yellowMessage("Congratulations! You have obtained a common Equip!");
                            }
                            if (scale == 2 && cm.isEquip(i)) {
                                cm.getPlayer().yellowMessage("Congratulations! You have obtained a Rare Equip!");
                            }
                            if (scale == 3 && cm.isEquip(i)) {
                                cm.getPlayer().yellowMessage("Congratulations! You have obtained a Epic Equip!");
                            }
                        }
                        if (cm.haveItem(ticketId, amount) && cm.canHoldGachReward()) {
                            cm.sendYesNo(text + " Would you like to use another Gachapon Ticket?");
                        } else {
                            cm.sendOk(text);
                        }
                    }
                }
            } else {
                cm.sendOk("You don't have enough Gachapon Tickets.");
            }
        } else {
            cm.sendOk("You don't have enough inventory space inside EQUIP or USE to collect the reward.");
        }

    } else if (status == 3) {
        status = 3;
        action(0, 0, 0);
    } else {

    }
}