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
var ticketId = 5062002;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var cost = 5;
var slotcount = 0;
var cube = 4310501;
var count = 0;
var power = 0;
var chance = 0;
var questid = 0;
var questtime = 14400;//4 hours

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
        if (cm.haveItem(cube, cost)) {
            equiplist = cm.getPlayer().getEquipCash();
            if (!equiplist.isEmpty()) {
                var selStr = "";
                for (var i = 0; i < equiplist.size(); i++) {
                    var curEquip = equiplist.get(i);
                    if (curEquip != null) {
                        count += 1;
                        selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + "#l\r\n\ ";
                    }
                }
                if (count > 0) {
                    cm.sendSimple("Which NX equip would you like to #rReset#k? I only accept #i" + cube + "#\r\n\#rNX Equip will be completely reset with NO stats!#k\r\n\ " + selStr);
                } else {
                    cm.sendOk("You currently do not have any Equips to enhance.");

                }
            } else {
                cm.sendOk("You currently do not have any NX Equips to Reset.");

            }
        } else {
            cm.sendOk("I require " + cost + " #i" + cube + "#s to reset NX Items.");
        }
    } else if (status == 1) {
        if (cm.haveItem(cube, cost)) {
            equip = equiplist.get(selection);
            if (equip != null) {
                cm.sendYesNo("#rAre you sure you want to reset:#k \r\n\#i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + "?");
            } else {
                cm.sendOk("invalid equip error pls report..");
            }
        } else {
            cm.sendOk("You currently do not have enough Scrolls. Requires " + cost + " #i" + cube + "#s");

        }
    } else if (status == 2) {
        if (equip != null) {
            cm.gainItem(cube, -cost);
            cm.resetEquip(equip);
            cm.sendOk("#i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + " has been fully reset.");
        }
    }
}


