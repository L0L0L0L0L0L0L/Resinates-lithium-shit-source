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
var ppl = new Array(0, 1, 2, 3, 4, 4);
var level = new Array(0, 100, 150, 200, 250, 500);
var levelz = new Array(0, 250, 250, 250, 250, 500);

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
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                cm.sendSimple("Which #bHalloween#k Challenge do want to challenge?\r\n#L1# #v3994115##l #L2# #v3994116##l #L3# #v3994117##l #L4# #v3994118##l #L5# #v3994119##l\r\n\r\n#rEach Mode costs (10-15-20-25) Stamina#k.\r\n#rEach Mode requires (1-2-3-4) players#k.\r\n#rEach Mode requires Levels (100-150-200-250)#k.");
            } else {
                cm.sendOkS("Only the Party leader can speak to me.", 2);
            }
        } else {
            cm.sendOkS("I must take on this Challenge with a party.", 2);
        }
    } else if (status == 1) {
        var em = cm.getEventManager("HPQ");
        if (em != null) {
            var stamina = 5 + (5 * selection);
            if (cm.getPlayer().getStamina() >= stamina) {
                if (em.getEligibleParty(cm.getPlayer(), level[selection], ppl[selection], 6)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level[selection], selection)) {
                        cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.getPlayer().removeStamina(stamina);
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