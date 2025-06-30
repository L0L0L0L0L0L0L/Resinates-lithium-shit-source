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

var questid = 7801;
var questtime = 900;//10 min
var minpal = 8;
var maxpal = 12;
var iv = 250;
var power = 1.2;
var bg = 50;
var ticket = 4202020;
var ach = 3101;
//3600 = 1 hour

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
        if (cm.haveItem(ticket, 1)) {
            var lvl = cm.getPlayer().getPalLevel();
            if (lvl >= 250) {
                if (!cm.getPlayer().battle) {
                    if (cm.getPlayer().getPalStorage().getActivePals().size() > 0) {
                        cm.sendYesNo("Do you want to challenge me?\r\n#rLevel Range: " + cm.getPlayer().getPalLevel() + "#k\r\n#bDifficulty: " + parseInt(power * 100) + "%#k\r\nNumber of Pals: #r" + minpal + " - " + maxpal + "#k");
                    } else {
                        cm.sendOk("Where is your pals at?");
                    }
                } else {
                    cm.sendOk("Seems you are still into battle?");
                }
            } else {
                cm.sendOk("Seems your pals arent strong enough to face me?");
            }
        } else {
            cm.sendOk("I want my #i" + ticket + "# " + cm.getItemName(ticket) + "?");
        }
    } else if (status == 1) {
        if (cm.haveItem(ticket, 1)) {
            cm.gainItem(ticket, -1);
            var lvl = cm.getPlayer().getPalLevel();
            var pow = (lvl * 0.01) + power;
            cm.startSuperBattle(bg, lvl, lvl, lvl * 1.1, minpal, maxpal, iv, pow, true, ach);
        } else {
            cm.sendOk("I want my #i" + ticket + "# " + cm.getItemName(ticket) + "?");
        }
        cm.dispose();
    }
}


