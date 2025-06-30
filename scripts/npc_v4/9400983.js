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
var cost = 1;
var slotcount = 0;
var cube;
var scroll;
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
        equiplist = cm.getPlayer().getItemPets();
        if (!equiplist.isEmpty()) {
            var selStr = "";
            for (var i = 0; i < equiplist.size(); i++) {
                var curEquip = equiplist.get(i);
                if (curEquip != null) {
                    count += 1;
                    selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " ID: "+curEquip.getItemId()+"#l\r\n\ ";
                }
            }
            if (count > 0) {
                cm.sendSimple("Which pet would you like to kill?\r\n\ " + selStr);
            } else {
                cm.sendOk("You currently do not have any Pets to kill.");
            }
        } else {
            cm.sendOk("You currently do not have any Pets to kill.");
        }
    } else if (status == 1) {
        equip = equiplist.get(selection);
        cm.sendYesNo("Are you sure you to murder this pet into obilivion?");
    } else if (status == 2) {
        if (cm.removeItemFromSlot(equip)) {
            var parts = "\r\n #i4009354# " + cm.getItemName(4009354) + " x4\r\n";
            cm.gainItem(4009354, 4);
            parts += "#i4009355# " + cm.getItemName(4009355) + "\r\n";
            cm.gainItem(4009355, 1);
            parts += "\r\n#rDo you want to kill more of your pets#k?\r\n";
            cm.sendYesNo("You have successfully murdered your pet, here are it's remains." + parts);
        } else {
            cm.sendOk("You have failed to murder your pet.");
        }
    } else if (status == 3) {
        status = 1;
        action(0, 0, 0);
    }
}


