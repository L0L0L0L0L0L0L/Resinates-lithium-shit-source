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
var ticketId = 2049032;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var cost = 1;
var slotcount = 0;
var cube = 2049032;
var count = 0;
var min = 0;
var max = 0;
var chance = 0;
var safe = 0;
var pscroll = 0;
var tier = 0;
var success = 0;
var max = 100;
var gmb = 4310500;
var gcost = 0;

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
        equiplist = cm.getPlayer().getEquipCash();
        if (!equiplist.isEmpty()) {
            var selStr = "";
            for (var i = 0; i < equiplist.size(); i++) {
                var curEquip = equiplist.get(i);
                if (curEquip != null && curEquip.getUpgradeSlots() < 30000) {
                    count += 1;
                    selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " (#bT: " + curEquip.getPower() + "#k)#l\r\n\ ";
                }
            }
            if (count > 0) {
                cm.sendSimpleS("Which equip would you like to apply Clean Slates too?\r\n\I only accept #i" + cube + "#\r\n" + selStr, 16);
            } else {
                cm.sendOk("You currently do not have any Equips to clean slate.");
            }
        } else {
            cm.sendOk("You currently do not have any Equips to clean slate.");
        }

    } else if (status == 1) {
        if (equip == null) {
            equip = equiplist.get(selection);
        }
        max = 30000 - equip.getUpgradeSlots();
        if (max > 0) {
            cm.sendGetTextS("How many #i" + cube + "# would you like me to apply to this Equip?\r\n#rMax upgrades possible is " + max + "#k", 16);
        } else {
            cm.sendOkS("Equip is already maxed out on slots.", 16);
        }
    } else if (status == 2) {
        cost = Number(cm.getText());
        if (cost > 0 && cost <= max) {
            gcost = cost * 25;
            if (cm.haveItem(cube, cost)) {
                cm.sendYesNoS("Would you like to apply " + cost + " Clean Slates to \r\n#i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + " for price of " + gmb + "? " + cm.getItemName(gmb) + "", 16);
            } else {
                cm.sendOkS("You currently do not have clean slates", 16);
            }
        } else {
            cm.sendOkS("You have exceeded the number of Slates you can use at a time.", 16);
        }
    } else if (status == 3) {
        if (cm.haveItem(cube, cost)) {
            if (cm.haveItem(gmb, gcost)) {
                if (equip != null) {
                    cm.gainItem(gmb, -gcost);
                    cm.gainItem(cube, -cost);
                    equip.setUpgradeSlots(equip.getUpgradeSlots() + cost);
                    cm.getPlayer().updateEquipSlot(equip);
                    cm.sendOkS("#b #i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + " has gained " + cost + " Slots.#k\r\n", 16);
                } else {
                    cm.sendOkS("invalid equip error pls report..", 16);
                }
            } else {
                cm.sendOkS("You currently do not have enough #i4310500#. Requires " + gcost + "  #i" + gmb + "#s", 16);
            }

        } else {
            cm.sendOkS("You currently do not have enough #i2049032#. Requires #i" + cube + "#s", 16);
        }
    } else if (status == 5) {
        status = 3;
        action(0, 0, 0);
    }
}


