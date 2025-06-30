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
var level = 250;
var dp = 4310502;
var amount = 25;
var letter = 4009180;
var ach = new Array(0, 0, 0, 0, 0, 0);
var lvl = new Array(0, 2000, 3000, 4000, 5000, 7500);
var ppl = new Array(0, 1, 1, 2, 4, 4);
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
    if (status == 0) {
        if (!cm.getPlayer().isGroup()) {
            var text = "";
            text += "#L1# Kaotic Caves#l\r\n";
            text += "#L2# Kaotic Castle#l\r\n";
            cm.sendYesNo("Which challenge would you like to take on?\r\n" + text);
        } else {
            cm.sendOkS("I must take on this Challenge alone.", 2);
        }
    } else if (status == 1) {
        if (selection == 1) {
            em = cm.getEventManager("Kaotic_Caves");
        }
        if (selection == 2) {
            em = cm.getEventManager("Kaotic_Castle");
        }
        if (em != null) {
            if (!em.startPlayerInstance(cm.getPlayer(), 9999, 99)) {
                cm.sendOkS(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.", 2);
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }
    }
}