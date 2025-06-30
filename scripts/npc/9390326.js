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
var ticketId = 4036084;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var equips;
var equipslist;
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
var acc = 1;

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
        cost = 1000;
        equiplist = cm.getPlayer().getWeaponItems();
        if (!equiplist.isEmpty()) {
            var selStr = "";
            for (var i = 0; i < equiplist.size(); i++) {
                var curEquip = equiplist.get(i);
                if (curEquip != null && !curEquip.isCash(curEquip.getItemId())) {
                    selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " (#bT: " + curEquip.getPower() + "#k)#l\r\n\ ";
                    count++;
                }
            }
            if (count > 0) {
                cm.sendSimpleS("Which equip would you like to fuse?\r\n\Current Price: #r" + cost + "#k #i" + cube + "#\r\n" + selStr, 16);
            } else {
                cm.sendOkS("You currently do not have any Equips to merge.", 16);
            }
        } else {
            cm.sendOkS("You currently do not have any Equips to merge.", 16);
        }
    } else if (status == 1) {
        if (equip == null) {
            equip = equiplist.get(selection);
        }
        selection = 0;
        equipslist = cm.getWeaponsofType(cm.getPlayer(), equip);
        if (!equipslist.isEmpty()) {
            var selStr = "";
            for (var i = 0; i < equipslist.size(); i++) {
                var curEquip = equipslist.get(i);
                if (curEquip != null && !curEquip.isCash(curEquip.getItemId())) {
                    selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " (#bT: " + curEquip.getPower() + "#k)#l\r\n\ ";
                    count++;
                }
            }
            if (count > 0) {
                cm.sendSimpleS("Selection which equip type you want to apply these to?\r\n" + selStr, 16);
            } else {
                cm.sendOkS("You currently do not have any Equips to merge.", 16);
            }
        } else {
            cm.sendOkS("You currently do not have any Equips to merge.", 16);
        }
    } else if (status == 2) {
        if (equips == null) {
            equips = equipslist.get(selection);
        }
        if (equips != null) {
            cm.sendYesNoS("Are you sure you want to spend #b" + cost + "#k #i" + cube + "# to change\r\n#i" + equip.getItemId() + "# to #i" + equips.getItemId() + "# ", 16);
        } else {
            cm.sendOkS("invalid select equip error pls report..", 16);
        }
    } else if (status == 3) {
        if (cm.haveItem(cube, cost)) {
            cm.gainItem(cube, -cost);
            cm.changeEquipItem(cm.getPlayer(), equip, equips.getItemId());
            cm.sendOkS("Your #i" + equip.getItemId() + "# has been updated. ", 16);
        } else {
            cm.sendOkS("You currently do not have " + cost + " #i" + cube + "# for this system.", 16);

        }
    }
}


