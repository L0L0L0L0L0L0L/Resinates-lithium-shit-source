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
var white = 2340000;
var count = 0;
var min = 0;
var max = 0;
var chance = 0;
var safe = 0;
var pscroll = 0;
var tier = 0;
var success = 0;
var power = 0;
var tp = 0;

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
                if (curEquip != null && curEquip.getUpgradeSlots() > 0) {
                    count += 1;
                    selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " (#rTier: " + curEquip.getPower() + "#k) (#bSlots: " + curEquip.getUpgradeSlots() + "#k) #l\r\n";
                }
            }
            if (count > 0) {
                cm.sendSimpleS("Which equip would you like to auto scroll?\r\n#rStats are focused around Shard and Tier of equip#k\r\n" + selStr, 16);
            } else {
                cm.sendOkS("You currently do not have any Cash Equips to upgrade.", 16);
            }
        } else {
            cm.sendOkS("You currently do not have any Cash Equips to upgrade.", 16);
        }
    } else if (status == 1) {
        if (equip == null) {
            equip = equiplist.get(selection);
        }
        var text = ":\r\n";
        text += "#L2585000##i2585000##l";
        text += "#L2585001##i2585001##l";
        text += "#L2585002##i2585002##l";
        text += "#L2585003##i2585003##l";
        text += "r\n\r\n"
        text += "#L2585004##i2585004##l";
        text += "#L2585005##i2585005##l";
        text += "#L2585006##i2585006##l";
        text += "#L2585007##i2585007##l";
        cm.sendSimple("Which Shard would you like to use?\r\n" + text);
    } else if (status == 2) {
        cube = selection;
        var rate = 0;
        if (cube == 2585000) {
            power = 5;
            rate = 0;
        }
        if (cube == 2585001) {
            power = 10;
            rate = 0.01;
        }
        if (cube == 2585002) {
            power = 15;
            rate = 0.02;
        }
        if (cube == 2585003) {
            power = 20;
            rate = 0.03;
        }
        if (cube == 2585004) {
            power = 25;
            rate = 0.04;
        }
        if (cube == 2585005) {
            power = 50;
            rate = 0.05;
        }
        if (cube == 2585006) {
            power = 100;
            rate = 0.1;
        }
        if (cube == 2585007) {
            power = 250;
            rate = 0.25;
        }
        var ip = Math.floor(1 + (equip.getPower() * rate));
        tp = Math.floor(power * ip);
        cm.sendGetTextS("How many #i" + cube + "# would you like me to apply to this Equip?\r\n#rMax Scrolls that can be used is " + equip.getUpgradeSlots() + "#k\r\nThis Scroll will apply Randomly Upto #b+" + tp + "#k stats per scroll", 16);
    } else if (status == 3) {
        cost = cm.getNumber();
        if (cost > 0 && cost <= equip.getUpgradeSlots()) {
            if (cm.haveItem(cube, cost)) {
                cm.sendYesNoS("ARe you sure you wish to apply " + cost + " #i" + cube + "# to \r\n#i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + "?", 16);
            } else {
                cm.sendOkS("You currently do not have enough #i" + cube + "#", 16);
            }
        } else {
            cm.sendOkS("You currently do not have enough slots on your equip for this many scrolls", 16);
        }
    } else if (status == 4) {
        if (cm.haveItem(cube, cost)) {
            if (equip != null) {
                cm.gainItem(cube, -cost);
                var total = parseInt(cost * tp);
                equip.addBaseNXStats(total);
                equip.setUpgradeSlots(equip.getUpgradeSlots() - cost);
                cm.getPlayer().updateEquipSlot(equip);
                cm.sendOkS("#b #i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + " has gained " + total + " Stats.#k\r\n", 16);
            } else {
                cm.sendOkS("invalid equip error pls report..", 16);
            }
        } else {
            cm.sendOkS("You currently do not have enough #i" + cube + "# or #i" + white + "#", 16);

        }
    } else if (status == 5) {
        status = 3;
        action(0, 0, 0);
    }
}


