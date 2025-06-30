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
var ticketId = 4310020;
var reward = 4420090;
var rewamount = 5;
var items = new Array(4310066);
var amount = new Array(25000);
var exp = 100;
var questid = 98;
var questtime = 600;//30 min
var job = "thieves";
var e;

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
        e = "Agent_E_" + cm.getPlayer().getMapId();
        if (cm.getPlayer().timeClear(e) > 0) {
            cm.sendOk("I have to wait #b" + cm.secondsToString(cm.getPlayer().timeClear(e)) + "#k \r\n\ before I can claim more here.");
        } else {
            if (cm.canHold(reward)) {
                var selStr = "";
                selStr += "#i" + ticketId + "#  " + cm.getItemName(ticketId) + " (#rx2500#k)\r\n\ ";
                var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n\#fUI/UIWindow.img/Quest/basic#\r\n";
                rewards += "#i4420090# " + cm.getItemName(reward) + " (x#b5-10)#k\r\n";
                cm.sendYesNo("Bring the following items and I will reward you.\r\n\#rThis quest can be repeated every 8 hours of gameplay from various towns I am at.#k\r\n\You can find these items from various monsters: \r\n\r\n\ " + selStr + "\r\n\r\n" + rewards);
            } else {
                cm.sendOk("You currently do not have enough room to hold the shards...");
            }
        }
    } else if (status == 1) {
        if (cm.haveItem(ticketId, 2500)) {
            cm.gainItem(ticketId, -2500);
            var count = cm.random(5, 10);
            cm.gainItem(reward, count);
            cm.getPlayer().setVar(e, cm.getPlayer().getCurrentTime() + (3600 * 8000));
            cm.sendOk("You have gained x#r" + count + "#k  #i" + reward + "#. Come back and see me in\r\n#b" + cm.secondsToString(cm.getPlayer().timeClear(e)) + "#k.");
        } else {
            cm.sendOk("You currently do not have enough #i4310020#  " + cm.getItemName(ticketId) + " for me to give you #i" + reward + "#.");
        }
        cm.dispose();
    } else {
        cm.dispose();
    }
}


