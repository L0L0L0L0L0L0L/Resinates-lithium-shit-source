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
var ticketId = 4310500;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var cost = 1;
var slotcount = 0;
var cube = 4310500;
var count = 0;
var min = 0;
var max = 0;
var chance = 0;
var safe = 0;
var pscroll = 0;
var tier = 0;
var success = 0;

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
        if (cm.getPlayer().getBaseTier() > 1) {
            equiplist = cm.getPlayer().getEquipItems();
            if (!equiplist.isEmpty()) {
                var selStr = "";
                for (var i = 0; i < equiplist.size(); i++) {
                    var curEquip = equiplist.get(i);
                    if (curEquip != null && curEquip.getPower() < cm.getPlayer().getBaseTier() && curEquip.getPower() > 1) {
                        count += 1;
                        selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " (#bT: " + curEquip.getPower() + "#k)#l\r\n\ ";
                    }
                }
                if (count > 0) {
                    cm.sendSimpleS("Which equip would you like to upgrade?\r\n\I only accept 1 #i" + cube + "# per Upgrade.\r\n#rMax Tier is " + cm.getPlayer().getBaseTier() + "#k.\r\n#rHigher tiers can be unlocked defeating various raid bosses#k\r\n\ " + selStr, 16);
                } else {
                    cm.sendOkS("You currently do not have any Equips to soul enchance.", 16);
                }
            } else {
                cm.sendOkS("You currently do not have any Equips to soul enhance.", 16);
            }
        } else {
            cm.sendOkS("You currently do not have enough power to upgrade.\r\n#rStart by defeating Zakum#k.", 16);
        }
    } else if (status == 1) {
        if (equip == null) {
            equip = equiplist.get(selection);
        }
        cm.sendGetTextS("How many #i" + cube + "# would you like me to apply to this Equip?\r\n#rMax Scrolls that can be used is 9999#k", 16);
    } else if (status == 2) {
        cost = cm.getNumber();
        if (cost > 0 && cost <= 9999) {
            if (cm.haveItem(cube, cost)) {
                cm.sendYesNoS("ARe you sure you wish to apply " + cost + " #i" + cube + "# to \r\n#i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + "?", 16);
            } else {
                cm.sendOkS("You currently do not have enough #i" + cube + "#", 16);
            }
        } else {
            cm.sendOkS("You currently do not have enough slots on your equip for this many scrolls", 16);
        }
    } else if (status == 3) {
        if (cm.haveItem(cube, cost)) {
            if (equip != null) {
                var text = "";
                var chance = Math.floor((1.0 / equip.getPower()) * 100.0);
                var upgrades = cm.getPlayer().upgradeTiers(equip, cost, chance, cube);
                if (upgrades > 0) {
                    text = "#b #i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + " has consumed " + upgrades + " " + cm.getItemName(cube) + " to upgrade Tier: " + equip.getPower() + ".#k\r\n";
                } else {
                    text = "#b #i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + " has failed to upgrade to Tier: " + equip.getPower() + ".#k\r\n";
                }
                cm.sendOkS(text, 16);

            } else {
                cm.sendOkS("invalid equip error pls report..", 16);
            }
        } else {
            cm.sendOkS("You currently do not have enough #i4310500#. Requires " + cube + "#s", 16);

        }
    }
}


