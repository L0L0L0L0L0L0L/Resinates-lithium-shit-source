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
var ticketId = 4310502;
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
    if (mode == -1) {

    } else {
        if (mode == 0 && type > 0) {

            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.EQUIP).getNumFreeSlot() >= 1) {
                if (cm.haveItem(4280002) && cm.haveItem(5490002)) {
                    cm.sendYesNo("Hello#b #h ##k, Would you like to exchange #i4280002# + #i5490002# for #i1142654#?\r\n\r\n#k This is EXTREMELY Powerful medal.");
                } else {
                    cm.sendOk("You don't have Required items. You can find #i4280002# From golden boss pigs found around the world and you can find #i5490002# in donation shop for 50 DP.");

                }
            } else {
                cm.sendOk("You don't have enough inventory space inside EQUIP to collect the reward.");

            }
        } else if (status == 1) {
            cm.gainItem(4280002, -1);
            cm.gainItem(5490002, -1);
            cm.gainEquipPend(1142654, 1000000, 26, 50749, 5);
            cm.sendOk("Enjoy your new medal.");

        } else {

        }
    }
}


