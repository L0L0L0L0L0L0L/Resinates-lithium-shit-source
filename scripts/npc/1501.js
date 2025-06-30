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
var items = new Array(4001087, 4001088, 4001089, 4001090, 4001091);
var ivs = new Array("Hp", "Str", "Dex", "Int", "Luk", "Attack", "Defense", "Magic Attack", "Magic Defense");
var park = new Array("Meadow", "Emerald Cave", "Swamp", "Hell Lands", "Griseous Mines", "Augurite Mines", "Adamant Mines");
var dung = new Array("Forest", "Diamond Caves");
var deep_dung = new Array("Forest");
var amount = 99;
var reward = 4001063;
var rewamount = 10;
var exp = 250000;
var questid = 6152;
var questtime = 1800;//60 = 1 min
var job = "thieves";
var option = 0;
var option2 = 0;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var bigstar = " #fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
var pals;
var pal;
var acc = 0;
var energy = 0;
var eAmount = 0;
var upgrade = 0;
var uAmount = 0;
var exp = 0;
var IV = 0;
var scroll = 0;
var em;

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
    if (status == 0) {//-------------------------------------------------------------------------------------------
        //cm.sendOk("Seems you cannot hold any more eggs.");

        var text = "Hello there I am Professor Elm, how can I help you?\r\n#rAny Mistakes you make with my services will not be refunded.#k\r\n";
        text += "#rSelect an Option#k:\r\n";
        text += "#L100#" + bigstar + "Enter the Ball Park (#rRequires: " + cm.getItemName(4202025) + "#k)#l\r\n";
        text += "#L200#" + bigstar + "Enter the Dungeon (#rRequires: " + cm.getItemName(4202025) + "#k)#l\r\n";
        text += "#L300#" + bigstar + "Enter the Dark Dungeon (#rRequires: " + cm.getItemName(4202024) + "#k)#l\r\n";
        //text += "#L99#DEBUG PALS#l\r\n";
        cm.sendSimple(text);
    } else if (status == 1) {//-------------------------------------------------------------------------------------------
        option = selection;
        if (option == 100 || option == 200 || option == 300) {
            option = selection;
            scroll = 4202025;
            if (option == 300) {
                scroll = 4202024;
            }
            if (cm.haveItem(scroll, 1)) {
                cm.sendYesNo("Are you sure you want to use #i" + scroll + "# to enter this dungeon? ");
            } else {
                cm.sendOkS("You dont have enough #i" + scroll + "#", 16);
            }
        }
    } else if (status == 2) {//-------------------------------------------------------------------------------------------
        if (option == 100 || option == 200 || option == 300) {
            if (cm.haveItem(scroll, 1)) {
                var txt = "#rSelect a Basic Dungeon:#k\r\n";
                if (option == 100) {
                    for (var i = 0; i < park.length; i++) {
                        var d = 1600 + i;
                        if (cm.getPlayer().getQuestLock(d) > 0) {
                            txt += "#L" + d + "#" + star + "#r" + park[i] + " (Closed)#k#l\r\n";
                        } else {
                            txt += "#L" + d + "#" + star + "#b" + park[i] + " (Open)#k#l\r\n";
                        }
                    }
                }
                if (option == 200) {
                    for (var i = 0; i < dung.length; i++) {
                        var d = 1700 + i;
                        if (cm.getPlayer().getQuestLock(d) > 0) {
                            txt += "#L" + d + "#" + star + "#r" + dung[i] + " (Closed)#k#l\r\n";
                        } else {
                            txt += "#L" + d + "#" + star + "#b" + dung[i] + " (Open)#k#l\r\n";
                        }
                    }
                }
                if (option == 300) {
                    for (var i = 0; i < deep_dung.length; i++) {
                        var d = 1800 + i;
                        if (cm.getPlayer().getQuestLock(d) > 0) {
                            txt += "#L" + d + "#" + star + "#r" + deep_dung[i] + " (Closed)#k#l\r\n";
                        } else {
                            txt += "#L" + d + "#" + star + "#b" + deep_dung[i] + " (Open)#k#l\r\n";
                        }
                    }
                }
                cm.sendSimple(txt);
            } else {
                cm.sendOkS("You dont have enough #i" + scroll + "#", 16);
            }
        }
    } else if (status == 3) {//-------------------------------------------------------------------------------------------
        option = selection;
        if (option >= 1600 && option < 2000) {
            if (cm.getPlayer().getQuestLock(option) > 0) {
                cm.sendOk("Come see me after #b" + cm.secondsToString(cm.getPlayer().getQuestLock(option)) + "#k \r\n\      to repeat my quest again.\r\n\ Quest ID: " + option);
            } else {
                var h = (option - 1500);
                if (h >= 100 && h < 200) {
                    em = cm.getEventManager("Pal_Park_" + h);
                }
                if (h >= 200 && h < 300) {
                    em = cm.getEventManager("Pal_Dungeon_" + h);
                }
                if (h >= 300 && h < 400) {
                    em = cm.getEventManager("Pal_Dark_Dungeon_" + h);
                }
                if (em != null) {
                    if (cm.haveItem(scroll, 1)) {
                        if (em.getEligibleParty(cm.getPlayer(), 10, 1, 1)) {
                            if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getTotalLevel(), 1)) {
                                cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                            } else {
                                cm.gainItem(scroll, -1);
                                cm.getPlayer().setQuestLock(option, questtime);
                            }
                        } else {
                            cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map.");
                        }
                    } else {
                        cm.sendOk("Event requires #i" + scroll + "#" + cm.getItemName(scroll) + " to enter this dungeon.");
                    }
                } else {
                    cm.sendOk("Event is bugged contact support. " + h);
                }
            }
        }
        //cm.sendOk("Event Option: " + option);
    } else {
        cm.dispose();
    }
}


