/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
var status = 0;
var reward = 4033320;
var rewamount = 1;
var items = new Array(4033337, 4001238);
var amount = new Array(150000, 2500);
var Cantidad;
var boss = "Aragami";
var ach = 401;
var instance = "BGC_Aragami";


function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.getPlayer().getEventInstance() == null && cm.getPlayer().getMapId() == 610051000) {
                if (cm.getPlayer().isGroup()) {
                    if (cm.getPlayer().isLeader()) {
                        if (cm.getPlayer().achievementFinished(ach)) {
                            var selStr = "";
                            for (var i = 0; i < items.length; i++) {
                                selStr += "#i" + items[i] + "#  " + cm.getItemName(items[i]) + " (x" + amount[i] + ") (#b" + cm.convertNumber(cm.getPlayer().countAllItem(items[i])) + "#k)\r\n\ ";
                            }
                            cm.sendYesNo("I need the following materials to battle " + boss + ": \r\n\r\n\ " + selStr);
                        } else {
                            cm.sendOk("You must complete the other bosses before you can challenge this one");
                        }
                    } else {
                        cm.sendOkS("The leader of the party must be the to talk to me about joining the event.", 16);
                    }
                } else {
                    cm.sendOkS("Event is Party Mode Only.", 16);
                }
            } else {
                cm.sendOk("For Mordor!!!");
            }
        } else if (status == 1) {
            var stam = 5 + (cm.getPlayer().getGroupMembers().size() * 5);
            if (stam > 50) {
                stam = 50;
            }
            if (cm.haveItem(items[0], amount[0]) && cm.haveItem(items[1], amount[1])) {
                if (cm.getPlayer().getStamina() >= stam) {
                    em = cm.getEventManager(instance);
                    if (em != null) {
                        if (em.getEligiblePartyAch(cm.getPlayer(), 3100, ach)) {
                            if (!em.startPlayerInstance(cm.getPlayer(), 3400, 34)) {
                                cm.sendOkS("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.", 16);
                            } else {
                                cm.getPlayer().removeStamina(stam);
                                cm.gainItem(items[0], -amount[0]);
                                cm.gainItem(items[1], -amount[1]);
                            }
                        } else {
                            cm.sendOkS("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level 2800+, 1+ Raid members.", 16);
                        }
                    } else {
                        cm.sendOkS("Event has already started, Please wait.", 16);
                    }
                } else {
                    cm.sendOkS("You do not have enough Stamina to fight. Requires at least "+stam+" or more to fight.", 16);
                }
            } else {
                cm.sendOk("You currently do not have enough materials to battle.");
            }
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}


