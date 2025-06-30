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
var count = 0;
var min = 0;
var max = 0;
var chance = 0;
var safe = 0;
var pscroll = 0;
var tier = 1;
var level = 1;

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
        equiplist = cm.getPlayer().getItemsByOverflow();
        if (!equiplist.isEmpty()) {
            var selStr = "";
            for (var i = 0; i < equiplist.size(); i++) {
                var curEquip = equiplist.get(i);
                if (curEquip != null) {
                    count += 1;
                    selStr += "#L" + i + "##i" + curEquip.getItemId() + "##l";
                }
            }
            if (count > 0) {
                cm.sendSimple("Which ETC would you like to Store?\r\n" + selStr);
            } else {
                cm.sendOk("You currently do not have any Equips to Flame.");
            }
        } else {
            cm.sendOk("You currently do not have any Etc to Store.");
        }
    } else if (status == 1) {
        equip = equiplist.get(selection);
        var eid = equip.getItemId();
        var eamount = equip.getQuantity();
        if (cm.getPlayer().isTimeLimited(equip)) {
            if (cm.getPlayer().addOverflowNPC(equip, eamount, " From ace of spades.")) {
                cm.sendYesNo("#i" + eid + "# #b" + cm.getItemName(eid) + " (" + eamount + "x) successfully stored.#k\r\n\Do you wish to store more items?");
            } else {
                cm.sendOk("#i" + eid + "# #b" + cm.getItemName(eid) + " (" + eamount + "x) successfully stored.#k");
            }
        } else {
            cm.sendOk("Items that expire cannot be stored.");
        }
    } else if (status == 2) {
        status = 1;
        action(0, 0, 0);
    }
}


