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
var ticketId = 2530000;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var cost = 1;
var slotcount = 0;
var cube = 2530000;
var white = 2530000;
var count = 0;
var min = 0;
var max = 0;
var chance = 0;
var safe = 0;
var pscroll = 0;
var tier = 0;
var success = 0;
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
    if (status == 0) {
        equiplist = cm.getPlayer().getEquipItems();
        if (!equiplist.isEmpty()) {
            var selStr = "";
            for (var i = 0; i < equiplist.size(); i++) {
                var curEquip = equiplist.get(i);
                if (curEquip != null && !cm.getPlayer().hasLucky(curEquip)) {
                    count += 1;
                    selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " (#rTier: " + curEquip.getPower() + "#k) (#bSlots: " + curEquip.getUpgradeSlots() + "#k) #l\r\n";
                }
            }
            if (count > 0) {
                cm.sendSimpleS("Which equip would you like to apply #i2530000#?\r\n" + selStr, 16);
            } else {
                cm.sendOkS("You currently do not have any Equips to lucky day.", 16);
            }
        } else {
            cm.sendOkS("You currently do not have any Equips to lucky day.", 16);
        }
    } else if (status == 1) {
        if (equip == null) {
            equip = equiplist.get(selection);
        }
        cm.sendYesNoS("ARe you sure you wish to apply #i" + cube + "# to \r\n#i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + "?", 16);
    } else if (status == 2) {
        if (cm.haveItem(cube, 1)) {
            cm.gainItem(cube, -1);
            cm.getPlayer().addLucky(equip);
            cm.sendOk("#i" + cube + "# has been applied to #i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + ".");
        } else {
            cm.sendOk("Seems you dont have enough #i" + cube + "#");
        }
       
    } else if (status == 3) {

    } else if (status == 4) {

    }
}


