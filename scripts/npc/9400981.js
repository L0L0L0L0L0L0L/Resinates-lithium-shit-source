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
var ticketId = 4310505;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var amount = 1000000;
var slotcount = 0;

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
        if (cm.haveItem(ticketId, 1)) {
            cm.sendSimple("Welcome to the Item of Choice Shop. Pick a category of NX you would like to purchase from?\r\n\ Warning some list takes upto 10 second load do not close npc.\r\n\I only accept 1 #i" + ticketId + "# " + cm.getItemName(ticketId) + ".\r\n\r\n#L0#Weapons?#l\r\n#L1#Hats?#l\r\n#L2#Overall?#l\r\n#L3#Top?#l\r\n#L4#Bottom?#l\r\n#L5#Gloves?#l\r\n#L6#Shield?#l\r\n#L7#Shoes?#l\r\n#L8#Cape?#l");
        } else {
            cm.sendOk("You currently do not have any #i" + ticketId + "# " + cm.getItemName(ticketId) + " for NX of choice.");

        }
    } else if (status == 1) {
        if (selection == 0) {
            equiplist = cm.getNxWeapons();
        } else if (selection == 1) {
            equiplist = cm.getNxHat();
        } else if (selection == 2) {
            equiplist = cm.getNxOverall();
        } else if (selection == 3) {
            equiplist = cm.getNxTop();
        } else if (selection == 4) {
            equiplist = cm.getNxBottom();
        } else if (selection == 5) {
            equiplist = cm.getNxGlove();
        } else if (selection == 6) {
            equiplist = cm.getNxShield();
        } else if (selection == 7) {
            equiplist = cm.getNxShoe();
        } else if (selection == 8) {
            equiplist = cm.getNxCape();
        }
        if (equiplist != null && !equiplist.isEmpty()) {
            var selStr = "";
            for (var i = 0; i < equiplist.size(); i++) {
                var curEquip = equiplist.get(i);
                if (curEquip != null && cm.isCash(curEquip)) {
                    selStr += "#L" + i + "##i" + curEquip + "##l";
                } else {
                    continue;
                }
            }
            cm.sendSimple("Which NX equip would you like to Buy?\r\n\ " + selStr);
        } else {
            cm.sendOk("You currently do not have any Equips to reset.");

        }
    } else if (status == 2) {
        equip = equiplist.get(selection);
        cm.gainItem(ticketId, -1);
        cm.gainEquip(equip, 1);
        cm.sendOk("You have successfully exchanged #i" + ticketId + "# " + cm.getItemName(ticketId) + " for #i" + equip + "#.\r\n\ If weapon appears invisible do not drop or trade it, save it and report the item and place item in very last slot of inventory.");
    }
}


