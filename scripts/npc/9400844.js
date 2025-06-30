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
var cost = 0;
var slotcount = 0;
var cube = 4310505;
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
        var range = 250 + cm.getPlayer().getAccVara("Pickup");
        var selStr = "Using my Advanced technogoly, I can improve your Item Vaccing Range at a certain cost of course (#r" + cm.getItemName(cube) + "#k).\r\nCurrent Range: #b" + range + "#k\r\n";
        selStr += "#L5# #bIncrease +25 (Price: 5 IP)#k#l\r\n";
        selStr += "#L10# #bIncrease +50 (Price: 10 IP)#k#l\r\n";
        selStr += "#L20# #bIncrease +100 (Price: 20 IP)#k#l\r\n";
        selStr += "#L50# #bIncrease +250 (Price: 50 IP)#k#l\r\n";
        selStr += "#L100# #bIncrease +1000 (Price: 100 IP)#k (#rSALE#k)#l\r\n";
        cm.sendSimple(selStr);
    } else if (status == 1) {
        cost = selection;
        if (selection == 5) {
            count = 25;
        }
        if (selection == 10) {
            count = 50;
        }
        if (selection == 20) {
            count = 100;
        }
        if (selection == 50) {
            count = 250;
        }
        if (selection == 100) {
            count = 1000;
        }
        if (cm.haveItem(cube, cost)) {
            cm.sendYesNoS("Are you sure you want to spend " + cost + " (#r" + cm.getItemName(cube) + "#k) to expand your Item Vac range by " + count + "?", 16);
        } else {
            cm.sendOkS("You currently do not have " + cost + " #i" + cube + "# for this upgrade.", 16);
        }
    } else if (status == 2) {
        if (cm.haveItem(cube, cost)) {
            cm.gainItem(cube, -cost);
            cm.getPlayer().addAccVar("Pickup", count);
            cm.sendOkS("Your item vac range has been increased by " + count, 16);
        } else {
            cm.sendOkS("You currently do not have enough #i4310505#. Requires " + cube + "#s", 16);

        }
    } else if (status == 3) {
        status = 3;
        action(0, 0, 0);
    }
}


