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
var ticketId = 4032521;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var amount = 1000000;
var slotcount = 0;
var option = 0;
var cost = 0;
var price = 0;
var time = 0;

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
        if (cm.getPlayer().vipBuff < 99999999) {
            var selStr = "How much VIP would you like to buy? VIP Costs #i4032521# (Plex)\r\n\ ";
            selStr += "#rVIP cannot be paused!#k\r\n\ ";
            selStr += "#L0# Buy VIP Buff #b1 Days#k (#rCost: 1 Plex) #k#l\r\n\ ";
            selStr += "#L1# Buy VIP Buff #b10 Days#k (#rCost: 5 Plex) #k#l\r\n\ ";
            selStr += "#L2# Buy VIP Buff #b25 Days#k (#rCost: 10 Plex) #k#l\r\n\ ";
            selStr += "#L3# Buy VIP Buff #b75 Days#k (#rCost: 25 Plex) #k#l\r\n\ ";
            selStr += "#L4# Buy VIP Buff #b160 Days#k (#rCost: 50 Plex) #k#l\r\n\ ";
            selStr += "#L5# Buy VIP Buff #b365 Days#k (#rCost: 100 Plex) #k#l\r\n\ ";
            cm.sendSimple("Welcome to VIP Shop. VIP Grants double EXP-MESO-DROP-ETC buff while online. Time does not get reduced while offline.\r\n\ " + selStr);
        } else {
            cm.sendOk("You currently have too much VIP time stored.");
        }



    } else if (status == 1) {
        option = selection;
        var selStr = "";
        if (option == 0) {
            cost = 1;
            time = 1;
        }
        if (option == 1) {
            cost = 5;
            time = 10;
        }
        if (option == 2) {
            cost = 10;
            time = 25;
        }
        if (option == 3) {
            cost = 25;
            time = 75;
        }
        if (option == 4) {
            cost = 50;
            time = 160;
        }
        if (option == 5) {
            cost = 100;
            time = 365;
        }
        cm.sendYesNo("Do you want to confirm that you want to spend " + cost + " #i4032521# on " + time + " Days of VIP?");
    } else if (status == 2) {
        if (cm.haveItem(ticketId, cost)) {
            cm.getPlayer().addBuff(0, time);
            cm.gainItem(ticketId, -cost);
            cm.getPlayer().dropMessage(time + " days of VIP time successfully added.");
            cm.sendOk("You have successfully bought " + time + " Days of VIP for " + cost + " #i4032521#? Use @buff to check your vip time.");
        } else {
            cm.sendOk("You currently do not have PLEX. Requires " + cost + " #i4032521#.");
        }
    }
}


