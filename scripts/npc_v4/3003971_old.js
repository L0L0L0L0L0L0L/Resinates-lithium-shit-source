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
var ticketId = 4001895;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var cost = 1;
var slotcount = 0;
var cube = 4001895;
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
        equiplist = cm.getPlayer().getEquipItems();
        if (!equiplist.isEmpty()) {
            var selStr = "";
            for (var i = 0; i < equiplist.size(); i++) {
                var curEquip = equiplist.get(i);
                if (curEquip != null && curEquip.getPower() < 60) {
                    count += 1;
                    var price = curEquip.getPower() * curEquip.getPower() * curEquip.getPower();
                    selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " (#bT: " + curEquip.getPower() + "#k - #rCost: " + price + "#k)#l\r\n\ ";
                }
            }
            if (count > 0) {
                cm.sendSimple("Which equip would you like to upgrade?\r\n\I only accept #i" + cube + "# #rMax Tier is 60#k.\r\n\ " + selStr);
            } else {
                cm.sendOk("You currently do not have any Equips to upgrade.");
            }
        } else {
            cm.sendOk("You currently do not have any Equips to upgrade.");
        }
    } else if (status == 1) {
        if (equip == null) {
            equip = equiplist.get(selection);
        }
        cost = equip.getPower() * equip.getPower() * equip.getPower();
        if (cm.haveItem(cube, cost)) {
            cm.sendYesNo("Would you like to upgrade\r\n#i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + " to #bTier " + (equip.getPower() + 1) + "#k? The Price is " + cost + " #i4001895#");
        } else {
            cm.sendOk("You currently do not have " + cost + " #i" + cube + "# for this upgrade.");
        }
    } else if (status == 2) {
        if (cm.haveItem(cube, cost)) {
            if (equip != null) {
                cm.gainItem(cube, -cost);
                cm.getPlayer().upgradeTier(equip);
                var text = "#b #i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + " has been upgraded to Tier: " + equip.getPower() + ".#k\r\n";
                cost = equip.getPower() * equip.getPower() * equip.getPower();
                if (cm.haveItem(cube, cost) && equip.getPower() < 60) {
                    cm.sendYesNo(text + "Would you like to upgrade to #bTier " + (equip.getPower() + 1) + "#k?\r\nNext Tier price is " + cost + " #i4001895#.");
                } else {
                    cm.sendOk(text + "You currently do not have " + cost + " #i" + cube + "# for this upgrade.");
                }
            } else {
                cm.sendOk("invalid equip error pls report..");
            }
        } else {
            cm.sendOk("You currently do not have enough #i4001895#. Requires " + cube + "#s");

        }
    } else if (status == 3) {
        status = 2;
        action(0, 0, 0);
    }
}


