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
/* NPC Base
 Map Name (Map ID)
 Extra NPC info.
 
 
 cm.gainItem(4310001, amount);
 cm.getPlayer().getCashShop().gainCash(2, amount);
 
 */

importPackage(Packages.client);
importPackage(Packages.tools);
importPackage(Packages.server.life);

var status;
var option = 0;
var item = 0;
var mPoint = 0;
var amount = 0;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var power = 0;
var cost = 0;




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
        cm.sendGetText("How many Maple Points do you want to buy with NX Cash?\r\n\You currently have " + cm.convertNumber(cm.getPlayer().getCSPoints(1)) + " NX Cash to cash in.\r\n\Each MP costs 1,000 NX Cash.");
    } else if (status == 1) {
        amount = cm.getNumber();
        cost = amount * 1000;
        if (amount > 0 && cm.getPlayer().getCSPoints(1) >= cost) {
            cm.sendYesNo("Do you want to confirm you want to exchange " + cost + " NX Cash for " + amount + " Maple Points?");
        } else {
            cm.sendOk("You do not have enough NX Cash.");

        }
    } else if (status == 2) {
        cm.gainItem(4310501, amount);
        cm.getPlayer().modifyCSPoints(1, -cost, true);
        cm.getPlayer().dropMessage(6, cost + " Nx has been removed from your account. You have " + cm.getPlayer().getCSPoints(1) + " NX remaining.");
        cm.sendOk("Thank you. Come again if you have more NX Cash.\r\n\You can use this MP at FM for free NX gears or Quickmove-MP Shop for goods.");
    }
}