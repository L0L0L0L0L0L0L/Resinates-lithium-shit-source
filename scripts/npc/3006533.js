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
var ticketId = 4310066;
var item = 4036018;
var amount = 100;
var reward = 4036518;
var mbag = 4310500;
var mAmount = 100000;
var rewamount = 1;
var multi = 1;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";

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
        if (cm.getPlayer().achievementFinished(439)) {
            var time = cm.getPlayer().getVaraLock("Moral_" + cm.getNpc());
            var morale = cm.getPlayer().getAccVara("Morale");
            if (time > 0) {
                cm.sendOk("Come back later.\r\n#b" + cm.secondsToString(time) + "#k.");
            } else {
                amount = 1000000;
                rewamount = 10000;
                cm.sendYesNo("Bring me #b" + amount + "#k #i" + item + "# + #b" + mAmount + "#k #i" + mbag + "#\r\n#rRewards#k\r\n#b10000x #k#i" + reward + "# + #r10000 Morale#k (#b24h Cooldown#k)\r\nYou currently have #b" + morale + "#k Morale\r\nCurrent Morale-ETC Amount: (#b" + cm.convertNumber(cm.getPlayer().countAllItem(item)) + "#k)\r\nCurrent Meso Bags: (#b" + cm.convertNumber(cm.getPlayer().countAllItem(mbag)) + "#k)");
            }
        } else {
            cm.sendOk("Come back later when you purged this cave.");
        }
    } else if (status == 1) {
        if (cm.haveItem(item, amount) && cm.haveItem(mbag, mAmount)) {
            cm.gainItem(item, -amount);
            cm.gainItem(mbag, -mAmount);
            cm.gainItem(reward, 10000);
            cm.getPlayer().setVaraLock("Moral_" + cm.getNpc(), 3600 * 4);
            cm.getPlayer().setAccVar("Morale", cm.setMaxValue(cm.getPlayer().getAccVara("Morale") + 10000, 9999999));
            cm.getPlayer().updateStats();
            var time = cm.getPlayer().getVaraLock("Moral_" + cm.getNpc());
            cm.sendOk("Come back later.\r\n#b" + cm.secondsToString(time) + "#k\r\nfor more crystals!\r\nYour Morale has increased to #b" + cm.getPlayer().getAccVara("Morale") + "#k");
        } else {
            cm.sendOk("Why do you have to do that for, get my hopes up for nothing!!!");
        }
        cm.dispose();
    } else if (status == 2) {
    } else {
        cm.dispose();
    }
}


