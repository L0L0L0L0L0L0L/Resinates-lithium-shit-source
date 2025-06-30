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
var equip2;
var scroll = 0;
var scrollslot = 0;
var cost = 1;
var slotcount = 0;
var cube = 2049901;
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
        if (cm.haveItem(cube)) {
            equiplist = cm.getPlayer().getEquipCash();
            if (equiplist.size() >= 2) {
                var selStr = "";
                var count = 0;
                for (var i = 0; i < equiplist.size(); i++) {
                    var curEquip = equiplist.get(i);
                    if (curEquip != null) {
                        count += 1;
                        selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + "#l\r\n\ ";
                    }
                }
                if (count > 0) {
                    cm.sendSimple("Which NX equip would you like to transmog?\r\n\ " + selStr);
                } else {
                    cm.sendOk("You currently do not have any Equips to transmog.");
                }
            } else {
                cm.sendOk("You currently do not have enough Equips to transmog.");
            }
        } else {
            cm.sendOk("You currently do not have golden anvils.\r\n\Requires #i" + cube + "# " + cm.getItemName(cube));

        }
    } else if (status == 1) {
        if (equip == null) {
            equip = equiplist.get(selection);
            equiplist.remove(equip);
            var selStr = "";
            var count = 0;
            for (var i = 0; i < equiplist.size(); i++) {
                var curEquip = equiplist.get(i);
                if (curEquip != null) {
                    count += 1;
                    selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + "#l\r\n\ ";
                }
            }
            if (count > 0) {
                cm.sendSimple("Which equip would you like to pull stats from?\r\n\ " + selStr);
            } else {
                cm.sendOk("You currently do not have any Equips to fuse.");
            }
        } else {
            cm.sendOk("You currently do not have any Equips to fuse.");
        }
    } else if (status == 2) {
        if (equip2 == null) {
            equip2 = equiplist.get(selection);
            if (equip != null && equip2 != null) {
                var text = "Do you want to confirm?\r\n\r\n";
                text += "Take Stats from #i" + equip2.getItemId() + "# " + cm.getItemName(equip2.getItemId()) + " \r\n";
                text += "And apply them to #i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + " \r\n";

                text += "\r\n#rTake note that " + cm.getItemName(equip2.getItemId()) + " will be destoried#k\r\n#rFusion anvil will be consumed.#k\r\n";
                cm.sendYesNo(text);
            } else {
                cm.sendOk("Null Errors.");
            }
        } else {
            cm.sendOk("Null Errors.");
        }

    } else if (status == 3) {
        if (cm.haveItem(cube, 1)) {
            if (equip != null && equip2 != null) {
                cm.getPlayer().setFusion(equip2, equip);
                cm.gainItem(cube, -1);
                cm.sendOk("Your #i" + equip2.getItemId() + "# " + cm.getItemName(equip2.getItemId()) + " has been transmogged.");
            } else {
                cm.sendOk("Null Errors.");
            }
        } else {
            cm.sendOk("You currently do not have golden anvils.\r\n\Requires #i" + cube + "# " + cm.getItemName(cube));

        }
    }
}


