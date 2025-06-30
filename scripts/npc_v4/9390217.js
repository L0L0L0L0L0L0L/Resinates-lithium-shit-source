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
/* 9000021 - Gaga
 BossRushPQ recruiter
 @author Ronan
 */

var status;
var level = 5000;
var dp = 4310502;
var amount = 25;
var letter = 4009180;
var ach = new Array(0, 0, 0, 0, 0, 0);
var ppl = new Array(0, 4, 4, 4, 4, 4);

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
        if (cm.getPlayer().achievementFinished(293) && cm.getPlayer().getTotalLevel() >= 5000) {
            if (cm.getPlayer().isGroup()) {
                if (cm.getPlayer().isLeader()) {
                    var lvl = parseInt(cm.getPlayer().getTotalLevel() + (cm.getPlayer().getTotalLevel() * 0.1));
                    cm.sendYesNo("You wish to this special #rMonster Park#k?\r\nEach stage your clear will increase the rank of the monsters\r\n#rThis PQ requires #i4036002#" + cm.getItemName(4036002) + "#k\r\n\r\n");
                } else {
                    cm.sendOkS("Have the party leader talk to me.", 2);
                }
            } else {
                cm.sendOkS("I must take on this Challenge in a party of 1-4.", 2);
            }
        } else {
            cm.sendOkS("I am not ready to venture off yet.", 2);
        }
    } else if (status == 1) {
        var em = cm.getEventManager("MP_Endless");
        if (em != null) {
            if (cm.getPlayer().isGroup()) {
                if (cm.haveItem(4036002, 1)) {
                    if (em.getEligibleParty(cm.getPlayer(), level, 1, 6)) {
                        if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getTotalLevel(), 1)) {
                            cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                        } else {
                            cm.gainItem(4036002, -1);
                        }
                    } else {
                        cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map.");
                    }
                } else {
                    cm.sendOkS("You currently do not have any #i4036002# to take on this challenge.", 2);
                }
            } else {
                cm.sendOkS("I must take on this Challenge in a party of 1-4.", 2);
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }
    }
}