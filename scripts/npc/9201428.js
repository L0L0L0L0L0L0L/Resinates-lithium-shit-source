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
var items = new Array(4001238, 4032906);
var amount = new Array(5000, 1000);

var bitems = 4001240;
var bamount = 1;
var option = 0;

var boss = "Khan";
var ach = 404;
var instance = "BGC_Khan";

var Cantidad;
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
            if (cm.getPlayer().getMapId() == 610050000) {
                option = 1;
                if (!cm.getPlayer().achievementFinished(185)) {
                    var selStr = "";
                    for (var i = 0; i < items.length; i++) {
                        selStr += "#i" + items[i] + "#  " + cm.getItemName(items[i]) + " (x" + amount[i] + ") (#b" + cm.convertNumber(cm.getPlayer().countAllItem(items[i])) + "#k)\r\n\ ";
                    }
                    cm.sendYesNo("I need the following materials to unlock Battle Gate City: \r\n\r\n\ " + selStr);
                } else {
                    cm.sendOk("You currently already unlocked Battlegate City.");
                }
            } else if (cm.getPlayer().getMapId() == 610052100) {
                option = 2;
                if (cm.getPlayer().getEventInstance() == null && cm.getPlayer().getMapId() == 610052100) {
                    if (cm.getPlayer().isGroup()) {
                        if (cm.getPlayer().isLeader()) {
                            if (cm.getPlayer().achievementFinished(404)) {
                                var selStr = "";
                                selStr += "#i" + bitems + "#  " + cm.getItemName(bitems) + " (x" + bamount + ") (#b" + cm.convertNumber(cm.getPlayer().countAllItem(bitems)) + "#k)\r\n\r\n";
                                selStr += "#L10# #rReq. #k#bLvl. 4000 Kaotic Tier: 75#k (Keys: Free)#l\r\n";
                                selStr += "#L11# #rReq. #k#bLvl. 5000 Kaotic Tier: 80#k (Keys: 5)#l\r\n";
                                selStr += "#L12# #rReq. #k#bLvl. 6000 Kaotic Tier: 85#k (Keys: 10)#l\r\n";
                                selStr += "#L13# #rReq. #k#bLvl. 8000 Kaotic Tier: 90#k (Keys: 25)#l\r\n";
                                selStr += "#L14# #rReq. #k#bLvl. 9000 Kaotic Tier: 95#k (Keys: 50)#l\r\n";
                                selStr += "#L15# #rReq. #k#bLvl. 9999 Kaotic Tier: 99#k (Keys: 100)#l\r\n";
                                cm.sendYesNo("Are you ready to take on mighty Khan?\r\n\r\n\ " + selStr);
                            } else {
                                cm.sendOk("You need to clear all surrounding bosses to take on the leader.\r\nAlso clear #rHard Mode in Dungeon Room#k.");
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
            } else {
                cm.sendOk("For Mordor!!!");
            }
        } else if (status == 1) {
            if (option == 1) {
                if (cm.haveItem(items[0], amount[0]) && cm.haveItem(items[1], amount[1])) {
                    cm.gainItem(items[0], -amount[0]);
                    cm.gainItem(items[1], -amount[1]);
                    cm.getPlayer().finishAchievement(185);
                    cm.sendOk("You have unlocked access to Battlegate City!");
                } else {
                    cm.sendOk("You currently do not have enough materials to unlock Battlegate City");
                }
            }
            if (option == 2) {
                if (selection == 10) {
                    cost = 0;
                    scale = 75;
                    level = 4000;
                }
                if (selection == 11) {
                    cost = 5;
                    scale = 80;
                    level = 5000;
                }
                if (selection == 12) {
                    cost = 10;
                    scale = 85;
                    level = 6000;
                }
                if (selection == 13) {
                    cost = 25;
                    scale = 90;
                    level = 8000;
                }
                if (selection == 14) {
                    cost = 50;
                    scale = 95;
                    level = 9000;
                }
                if (selection == 15) {
                    cost = 100;
                    scale = 99;
                    level = 9999;
                }
                var key = true;
                var price = false;
                if (cost > 0) {
                    if (cm.haveItem(bitems, cost)) {
                        key = true;
                        price = true;
                    } else {
                        key = false;
                    }
                }
                if (key) {
                    em = cm.getEventManager(instance);
                    if (em != null) {
                        if (em.getEligiblePartyAch(cm.getPlayer(), level, 185) && em.getEligiblePartyAch(cm.getPlayer(), level, 272)) {
                            if (!em.startPlayerInstance(cm.getPlayer(), level, scale)) {
                                cm.sendOkS("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.", 16);
                            } else {
                                if (price) {
                                    cm.gainItem(bitems, -cost);
                                }
                            }
                        } else {
                            cm.sendOkS("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level 2800+, 1+ Raid members.", 16);
                        }
                    } else {
                        cm.sendOkS("Event has already started, Please wait.", 16);
                    }
                } else {
                    cm.sendOkS("Sorry, your team does not have enough keys to enter.", 16);
                }
            } else {
                cm.dispose();
            }
        }
    }
}


