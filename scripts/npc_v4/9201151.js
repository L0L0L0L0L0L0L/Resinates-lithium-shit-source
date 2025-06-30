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
var items = new Array(4009209, 4009210, 4009218);
var amount = new Array(10000, 10000, 10000);
var reward = 4310100;
var rewamount = 2500;
var exp = 250000;
var questid = 729;
var questtime = 28800;//30 min
var job = "thieves";
var option = 0;

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
        var selStr = "#rCurrent Quest Locks#k:\r\n";
        for (var i = 700; i <= 730; i++) {
            if (cm.getPlayer().getQuestLock(i) > 0) {
                selStr += "Quest ID: "+i+" - #r" + cm.secondsToString(cm.getPlayer().getQuestLock(i)) + "#k\r\n";
            } else {
                selStr += "Quest ID: "+i+" is #bCLEAR#k\r\n";
            }
        }
        cm.sendOk(selStr);
    } else if (status == 1) {
        cm.dispose();
    } else {
        cm.dispose();
    }
}


