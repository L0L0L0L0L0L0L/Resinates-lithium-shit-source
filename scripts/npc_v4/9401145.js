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
var ticketId = 4310100;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var cost = 10;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var amount = 250000;
var slotcount = 0;
var ep = 0;
var tier = 0;
var power = 0;
var option = 0;
var multi = 2;
var etc = 0;

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
            if (cm.getPlayer().achievementFinished(408)) {
                cm.sendYesNoS("Do you want to exchange #i4310504# (#b" + cost + "x#k) for #b250,000#k #i" + ticketId + "#?", 16);
            } else {
                cm.sendOkS("You must defeat the mighty kobold to unlock my services!", 16);
            }
        } else if (status == 1) {
            if (cm.haveItem(4310504, cost) && cm.getPlayer().canHold(ticketId, amount)) {
                cm.gainItem(4310504, -cost);
                cm.gainItem(ticketId, amount);
                cm.sendOkS("Thank you, Your #i" + ticketId + "# has been delivered.", 16);
            } else {
                cm.sendOkS("You dont have enough #i4310504# or not enough room to hold #i" + ticketId + "#", 16);
            }
        } else if (status == 2) {
        } else if (status == 3) {
        } else if (status == 4) {
        }
    }
}