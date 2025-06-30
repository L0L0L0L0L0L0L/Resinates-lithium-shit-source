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
var level = 180;
var dp = 4310502;
var amount = 25;
var letter = 4009180;
var ach = new Array(0, 0, 0, 0, 0, 0);
var ppl = new Array(0, 4, 4, 4, 4, 4);
var level = new Array(0, 125, 150, 175, 200, 500);
var questid = 7923;
var questtime = 600;//10 min
var option = 0;
var orb = 4033082;
var count = 0;
var bmode = 0;
var passowrd = 0;

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
        var number1 = cm.random(1, 9);
        var number2 = cm.random(1, 9);
        var number3 = cm.random(1, 9);
        var number4 = cm.random(1, 9);
        password = cm.getCode(number1, number2, number3, number4);
        cm.sendGetText("#rBotting beyond this point is not allowed!\r\n\#kPlease enter the 4 digit #rAnti-Bot#k Code seen below:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "");
    } else if (status == 1) {
        amount = cm.getNumber();
        if (amount == password) {
            var text = "";
            var group = cm.getPlayer().getGroupSize();
            //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
            text += "#L2# Challenge the Fel Monsters (Requires #r" + group + "#k #i2000012#)\r\n       #rRequired Level: 100+ - Party Limit: 4 - Exp: 1x#k\r\n       #bMax Waves: 99#k#l\r\n";
            text += "#L3# Challenge the #bElite#k Fel Monsters (Requires #r" + group * 10 + "#k #i2000012#)\r\n       #rRequired Level: 250+ - Party Limit: 4 - Exp: 2x#k\r\n       #bMax Waves: 250#k#l\r\n";
            text += "#L4# Challenge the #bSuper#k Fel Monsters (Requires #r" + group * 100 + "#k #i2000012#)\r\n       #rRequired Level: 1000+ - Party Limit: 4 - Exp: 3x#k\r\n       #bMax Waves: 999#k#l\r\n";
            cm.sendSimple(text);
        } else {
            cm.sendOk("Wrong password mother fka.");
        }
    } else if (status == 2) {
        if (selection == 1) {
            option = 1;
            cm.sendGetText("Please enter the number of #i" + orb + "#" + cm.getItemName(orb) + " you wish to exchange.\r\n#bEach orb grants 1 Fel Power Up.");
        }
        if (selection == 2 || selection == 3 || selection == 4 || selection == 5) {
            option = 2;
            bmode = selection - 2;
            if (cm.getPlayer().getQuestLock(questid) > 0) {
                cm.sendOk("Come see me after #b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k \r\n\      to repeat my challenge again.\r\n\ Quest ID: " + questid);
            } else {
                if (cm.getPlayer().isGroup()) {
                    if (cm.getPlayer().isLeader()) {
                        var lvl = parseInt(cm.getPlayer().getTotalLevel());
                        if (bmode == 0) {
                            cm.sendYesNo("You wish to this special #rFel Monster Park#k?\r\nEach stage your clear will increase the rank of the monsters.\r\n#bMonster Level: " + lvl + " (Party Leader)#k\r\n#rMust be in Party of 1-6 players to enter\r\n#bThis dungeon has a 10 minute cooldown.\r\n");
                        }
                        if (bmode == 1) {
                            cm.sendYesNo("You wish to this special #rFel Elite Monster Park#k?\r\nEach stage your clear will increase the rank of the monsters.\r\nEach stage will contain an Elite Fel Monster.\r\n#bMonster Level: " + lvl + " (Party Leader)#k\r\n#rMust be in Party of 1-4 players to enter\r\n#bThis dungeon has a 10 minute cooldown.\r\n");
                        }
                        if (bmode == 2) {
                            cm.sendYesNo("You wish to this special #rFel Super Monster Park#k?\r\nEach stage your clear will increase the rank of the monsters.\r\nEach stage will contain an Elite Fel Monster.\r\nEvery 5 Stages is a Boss Challenge.\r\n#bMonster Level: " + lvl + " (Party Leader)#k\r\n#rMust be in Party of 1-2 players to enter\r\n#bThis dungeon has a 10 minute cooldown.\r\n");
                        }
                        if (bmode == 3) {
                            cm.sendYesNo("You wish to this special #rFel Boss Monster Park#k?\r\nEach stage your clear will increase the rank of the monsters.\r\nEach stage will be a Fel Boss.\r\n#bMonster Level: " + lvl + " (Party Leader)#k\r\n#rMust be in Party of 1 players to enter\r\n#bThis dungeon has a 10 minute cooldown.\r\n");
                        }
                    } else {
                        cm.sendOkS("Have the party leader talk to me.", 2);
                    }
                } else {
                    cm.sendOkS("I must take on this Challenge in a Solo Party.", 2);
                }
            }
        }
    } else if (status == 3) {
        if (option == 1) {
            count = cm.getNumber();
            if (count > 0 && count < 999) {
                if (cm.haveItem(orb, count)) {
                    cm.sendYesNo("Are you sure you want to exchange #b" + count + "#k #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power Level Ups?");
                } else {
                    cm.sendOkS("You dont have enough #i4000999#", 16);
                }
            } else {
                cm.sendOkS("You dont have enough #i4000999#", 16);
            }
        }
        if (option == 2) {
            var item, cost, level, party;
            var group = cm.getPlayer().getGroupSize();
            var em = cm.getEventManager("DP_Endless");
            if (bmode == 0) {
                item = 2000012;
                level = 100;
                party = 4;
                cost = group;
            }
            if (bmode == 1) {
                em = cm.getEventManager("DP_Endless_Elite");
                item = 2000012;
                level = 250;
                party = 4;
                cost = group * 10;
            }
            if (bmode == 2) {
                em = cm.getEventManager("DP_Endless_Super");
                item = 2000012;
                level = 1000;
                party = 4;
                cost = group * 100;
            }
            if (bmode == 3) {
                em = cm.getEventManager("DP_Endless_Boss");
                item = 4034380;
                level = 5000;
                party = 4;
            }
            if (em != null) {
                if (cm.haveItem(item, cost)) {
                    if (em.getEligibleParty(cm.getPlayer(), level, 1, 4)) {
                        if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getTotalLevel(), 1)) {
                            cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                        } else {
                            cm.getPlayer().setQuestLock(questid, questtime);
                            cm.gainItem(item, -cost);
                        }
                    } else {
                        cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map.");
                    }
                } else {
                    cm.sendOk("Event requires " + cost + "x #i" + item + "#" + cm.getItemName(item) + ".");
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        }
    } else if (status == 4) {
        if (option == 1) {
            if ((cm.getPlayer().getLevelData(105) + count) <= 99999) {
                if (cm.haveItem(orb, count)) {
                    cm.getPlayer().gainLevelsData(105, count);
                    cm.gainItem(orb, -count);
                    cm.sendOkS("You have gained " + count + " Fel Power Levels", 16);
                } else {
                    cm.sendOkS("You dont have enough #i" + orb + "#", 16);
                }
            } else {
                cm.sendOkS("You alrdy have maxxed out this Mastery.", 16);
            }
        }
    } else if (status == 4) {

    }
}