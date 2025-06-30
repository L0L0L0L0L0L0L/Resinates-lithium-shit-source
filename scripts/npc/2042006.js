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
var level = 10;
var dp = 4310502;
var amount = 50;
var letter = 4009180;
var ach = 226;
var ppl = new Array(0, 4, 4, 4, 4, 4);
var stam = 0;
var passowrd = 0;
var star1 = "#fUI/Custom.img/star/1#";
var star2 = "#fUI/Custom.img/star/2#";
var star3 = "#fUI/Custom.img/star/3#";
var star4 = "#fUI/Custom.img/star/4#";
var star5 = "#fUI/Custom.img/star/5#";
var questid = 7081;
var questtime = 86400;//30 min

function start() {
    status = -1;
    action(1, 0, 0);
}

function getRank(rank) {
    if (rank == 1) {
        return star1;
    }
    if (rank == 2) {
        return star1 + star1;
    }
    if (rank == 3) {
        return star1 + star1 + star2;
    }
    if (rank == 4) {
        return star1 + star1 + star2 + star2;
    }
    if (rank == 5) {
        return star1 + star1 + star2 + star2 + star3;
    }
    if (rank == 6) {
        return star1 + star1 + star2 + star2 + star3 + star3;
    }
    if (rank == 7) {
        return star1 + star1 + star2 + star2 + star3 + star3 + star4;
    }
    if (rank == 8) {
        return star1 + star1 + star2 + star2 + star3 + star3 + star4 + star4;
    }
    if (rank == 9) {
        return star1 + star1 + star2 + star2 + star3 + star3 + star4 + star4 + star5;
    }
    if (rank == 10) {
        return star1 + star1 + star2 + star2 + star3 + star3 + star4 + star4 + star5 + star5;
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
            if (!cm.getPlayer().isGroup()) {
                if (cm.getPlayer().achievementFinished(270)) {
                    if (cm.getPlayer().getTotalLevel() >= 250) {
                        var selStr = "#bDaily Trails Features#k:\r\n";
                        selStr += "Completing the trails will reward the player with\r\n#b1-5x #z2049032##k.\r\n";
                        selStr += "This Trial is #r50#k Waves.#k\r\n";
                        selStr += "#rThis Trial scales to what rank your job is. Rewards do not scale to the tier of the dungeon.#k\r\n";
                        selStr += "Quest Cooldown begins once final boss is defeated.#k";
                        cm.sendYesNo(selStr);
                    } else {
                        cm.sendOkS("I must take on this Daily Challenge being level 250+", 2);
                    }
                } else {
                    cm.sendOkS("I must clear Party Mode Easy first.", 2);
                }
            } else {
                cm.sendOkS("I must take on this Challenge Alone.", 2);
            }
    }
    if (status == 1) {
            if (!cm.getPlayer().isGroup()) {
                var em = cm.getEventManager("MP_Daily");
                if (em != null) {
                    var tier = 1 + cm.getPlayer().getVarZero(cm.getPlayer().getJob());
                    if (!em.startPlayerInstance(cm.getPlayer(), 1, tier, 500)) {
                        cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("Event has already started, Please wait.");
                }
            } else {
                cm.sendOkS("I must take on this Challenge Alone.", 2);
            }
    }
}