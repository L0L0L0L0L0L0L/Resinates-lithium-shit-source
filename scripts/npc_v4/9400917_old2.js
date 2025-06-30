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
/* Author: Xterminator
 NPC Name: 		Shanks
 Map(s): 		Maple Road : Southperry (60000)
 Description: 		Brings you to Victoria Island
 */
var status = 0;

function start() {
    if (cm.haveItem(4310502, 1)) {
        cm.sendYesNo("Would you like to spend 1 DP for random +5% Kaotic Perma Stat? Random stat list is: Exp Rate, Drop Rate, All Stat, Overpower, Meso Rate, Total Damage, Boss Damage, Ignroe Defense.");
    } else {
        cm.sendOk("Sorry, you dont have any donation points.");
                    
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        var stat = cm.random(0, 8);
        cm.gainItem(4310502, -1);
        cm.getPlayer().addStat(5, stat, true);
        if (stat == 0) {
            cm.sendOkS("You have exchanged 1 Donation Point for +5% Exp Rate.", 2);
        } else if (stat == 1) {
            cm.sendOkS("You have exchanged 1 Donation Point for +5% Drop Rate.", 2);
        } else if (stat == 2) {
            cm.sendOkS("You have exchanged 1 Donation Point for +5% Drop Rate.", 2);
        } else if (stat == 3) {
            cm.sendOkS("You have exchanged 1 Donation Point for +5% All Stats.", 2);
        } else if (stat == 4) {
            cm.sendOkS("You have exchanged 1 Donation Point for +5% OverPower.", 2);
        } else if (stat == 5) {
            cm.sendOkS("You have exchanged 1 Donation Point for +5% Meso Rate.", 2);
        } else if (stat == 6) {
            cm.sendOkS("You have exchanged 1 Donation Point for +5% Total Damage.", 2);
        } else if (stat == 7) {
            cm.sendOkS("You have exchanged 1 Donation Point for +5% Boss Damage.", 2);
        } else if (stat == 8) {
            cm.sendOkS("You have exchanged 1 Donation Point for +5% Ignore Defense.", 2);
        }
                    
    } else {
                    
    }

}

