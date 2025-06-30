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
var dp = 4000999;
var amount = 25;
var letter = 4009180;
var ach = new Array(0, 0, 0, 0, 0, 0);
var ppl = new Array(0, 4, 4, 4, 4, 4);
var level = new Array(0, 125, 150, 175, 200, 500);
var questid = 7927;
var questtime = 1200;//10 min
var option = 0;
var orb = 4033082;
var count = 0;
var bmode = 0;

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
        var text = "Welcome to Elwin Donation Monster Park, This dungeon offers active ways to obtain Elwin Power Mastery.\r\n";
        var group = cm.getPlayer().getGroupSize();
        text += "#L2# Elwin Monsters (Requires #r" + (group) + "#k #i"+dp+"#)\r\n       #rRequired Level: 100+ - Party Limit: 4 - Exp: 2x#k\r\n       #bMax Waves: 99#k#l\r\n";
        text += "#L3# #bElite#k Elwin Monsters (Requires #r" + (group * 5) + "#k #i"+dp+"#)\r\n       #rRequired Level: 250+ - Party Limit: 4 - Exp: 3x#k\r\n       #bMax Waves: 250#k#l\r\n";
        text += "#L4# #bSuper#k Elwin Monsters (Requires #r" + (group * 25) + "#k #i"+dp+"#)\r\n       #rRequired Level: 1000+ - Party Limit: 4 - Exp: 4x#k\r\n       #bMax Waves: 999#k#l\r\n";
        cm.sendSimple(text);

    } else if (status == 1) {
        if (selection == 1) {
            option = 1;
            cm.sendGetText("Please enter the number of #i" + orb + "#" + cm.getItemName(orb) + " you wish to exchange.\r\n#bEach orb grants 1 Fel Power Up.");
        }
        if (selection == 2 || selection == 3 || selection == 4 || selection == 5) {
            option = 2;
            bmode = selection - 2;
            if (cm.getPlayer().isGroup()) {
                if (cm.getPlayer().isLeader()) {
                    var lvl = parseInt(cm.getPlayer().getTotalLevel());
                    if (bmode == 0) {
                        var exp = 10;
                        var sexp = 5;
                        cm.sendYesNo("You wish to this special #rDP Monster Park#k?\r\nEach stage your clear will increase the rank of the monsters.\r\n#bMonster Level: " + lvl + " (Party Leader)#k\r\n#rMust be in Party of 1-6 players to enter\r\n#bThis dungeon has a 10 minute cooldown.\r\n#bThis dungeon also offers massive Skill Exp gains based on number of Supreme Pots you own.\r\n#rSkill Exp: " + sexp + "#k\r\n");
                    }
                    if (bmode == 1) {
                        var exp = 15;
                        var sexp = 10;
                        cm.sendYesNo("You wish to this special #rDP Elite Monster Park#k?\r\nEach stage your clear will increase the rank of the monsters.\r\nEach stage will contain an Elite DP Monster.\r\n#bMonster Level: " + lvl + " (Party Leader)#k\r\n#rMust be in Party of 1-4 players to enter\r\n#bThis dungeon has a 10 minute cooldown.\r\n#bThis dungeon also offers massive Skill Exp gains based on number of Supreme Pots you own.\r\n#rSkill Exp: " + sexp + "#k\r\n");
                    }
                    if (bmode == 2) {
                        var exp = 20;
                        var sexp = 25;
                        cm.sendYesNo("You wish to this special #rDP Super Monster Park#k?\r\nEach stage your clear will increase the rank of the monsters.\r\nEach stage will contain an Elite DP Monster.\r\nEvery 5 Stages is a Boss Challenge.\r\n#bMonster Level: " + lvl + " (Party Leader)#k\r\n#rMust be in Party of 1-2 players to enter\r\n#bThis dungeon has a 10 minute cooldown.\r\n#bThis dungeon also offers massive Skill Exp gains based on number of Supreme Pots you own.\r\n#rSkill Exp: " + sexp + "#k\r\n");
                    }
                    if (bmode == 3) {
                        var exp = 25;
                        var sexp = 100;
                        cm.sendYesNo("You wish to this special #rDP Boss Monster Park#k?\r\nEach stage your clear will increase the rank of the monsters.\r\nEach stage will be a DP Boss.\r\n#bMonster Level: " + lvl + " (Party Leader)#k\r\n#rMust be in Party of 1 players to enter\r\n#bThis dungeon has a 10 minute cooldown.\r\n#bThis dungeon also offers massive Skill Exp gains based on number of Supreme Pots you own.\r\n#rSkill Exp: " + sexp + "#k\r\n");
                    }
                } else {
                    cm.sendOkS("Have the party leader talk to me.", 2);
                }
            } else {
                cm.sendOkS("I must take on this Challenge in a Solo Party.", 2);
            }
        }
    } else if (status == 2) {
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
            var size = cm.getPlayer().getGroupSize();
            var item = dp, cost, level, party;
            var em = cm.getEventManager("EP_Endless");
            if (bmode == 0) {
                cost = 1;
                level = 100;
                party = 4;
            }
            if (bmode == 1) {
                em = cm.getEventManager("EP_Endless_Elite");
                cost = 5;
                level = 250;
                party = 4;
            }
            if (bmode == 2) {
                em = cm.getEventManager("EP_Endless_Super");
                cost = 25;
                level = 1000;
                party = 4;
            }
            if (bmode == 3) {
                em = cm.getEventManager("EP_Endless_Boss");
                cost = 500;
                level = 5000;
                party = 4;
            }
            if (em != null) {
                if (cm.haveItem(item, cost * size)) {
                    if (em.getEligibleParty(cm.getPlayer(), level, 1, 4)) {
                        if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getTotalLevel(), 1)) {
                            cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                        } else {
                            cm.getPlayer().setQuestLock(questid, questtime);
                            cm.gainItem(item, -(cost * size));
                        }
                    } else {
                        cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map.");
                    }
                } else {
                    cm.sendOk("Event requires " + (cost * size) + "x #i" + item + "#" + cm.getItemName(item) + ".");
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        }
    } else if (status == 3) {
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