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
            if (cm.getPlayer().getMapId() == 4500) {
                if (cm.getPlayer().isGroup()) {
                    if (cm.getPlayer().isLeader()) {
                        cm.sendSimple("Which Terra Challenge do want to challenge?\r\n#L1##v3994115##l#L2##v3994116##l#L3##v3994117##l#L4##v3994118##l\r\n\r\n#rEach Mode costs (10-15-20-25) Stamina Potions#k.\r\n#rEach Mode requires 4 players#k.\r\n#rEach Mode requires Level Minimum of Level 250#k.");
                    } else {
                        cm.sendOkS("Only the Party leader can speak to me.", 2);
                    }
                } else {
                    cm.sendOkS("I must take on this Challenge with a party.", 2);
                }
            } else {
                cm.sendOkS("The air is so cool here", 2);
            }
        } else {
            cm.sendOk("Wrong password mother fka.");
        }
    } else if (status == 2) {
        var em = cm.getEventManager("TPQ");
        if (em != null) {
            var stamina = 5 + (5 * selection);
            if (cm.haveItem(2000012, stamina)) {
                if (em.getEligibleParty(cm.getPlayer(), 250, 4, 4)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level[selection], selection)) {
                        cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(2000012, -stamina);
                    }
                } else {
                    cm.sendOkS("You cannot start this party quest yet, because either your party is not within the range limits, some of your party members are not eligible to attempt it or they are not in this map.", 2);
                }
            } else {
                cm.sendOkS("You currently do not have enough stamina to take on this challenge. Requires " + stamina + " Stamina.", 2);
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }
    }
}