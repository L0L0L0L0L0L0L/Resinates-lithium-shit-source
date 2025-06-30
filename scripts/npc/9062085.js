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
var star1 = "#fUI/Custom.img/star/6#";
var star2 = "#fUI/Custom.img/star/6#";
var star3 = "#fUI/Custom.img/star/6#";
var star4 = "#fUI/Custom.img/star/6#";
var star5 = "#fUI/Custom.img/star/6#";
var star6 = "#fUI/Custom.img/star/7#";
var jobName = new Array("Hero", "Paladin", "Dark Knight", "Fire Mage", "Ice Mage", "Bishop", "Bowman", "X-Bowman", "Hermit", "Shadower", "Dual-Blade", "Buccaneer", "Corsair", "Cannon Master", "Kain", "Kanna", "Path Finder", "NightWalker", "Ark", "Evan", "Battle Mage", "Wild Hunter", "Jett", "Burster", "Aran");
var job = new Array(112, 122, 132, 212, 222, 232, 312, 322, 412, 422, 434, 512, 522, 532, 1112, 1212, 1312, 1412, 1512, 2218, 3212, 3312, 2312, 3512, 2112);
var jb, js;

function start() {
    status = -1;
    action(1, 0, 0);
}

function getIcon(value) {
    return "#fUI/Custom.img/job/" + value + "#";
}

function getJob(player, value) {
    var rank = player.getVarZero(value);
    var text = "";
    var starG = rank % 10;
    var starR = Math.floor(rank / 10);
    if (rank <= 0) {
        return "(#rNew!#k)";
    } else {
        if (rank < 100) {
            if (starR > 0) {
                for (var i = 0; i < starR; i++) {
                    text += star6;
                }
            }
            if (starG > 0) {
                for (var i = 0; i < starG; i++) {
                    text += star1;
                }
            }
        } else {
            text = star3;
        }
    }
    return text;
}
//getJob()
function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        if (cm.haveItem(4420053, 1)) {
            if (!cm.getPlayer().isGroup()) {
                if (cm.getPlayer().achievementFinished(270)) {
                    if (cm.getPlayer().getTotalLevel() >= 250) {
                        var text = "This Trial Requires #b#i4420053# " + cm.getItemName(4420053) + "#k\r\n";
                        for (var i = 0; i < job.length; i++) {
                            var j = job[i];
                            var js = jobName[i];
                            if (cm.getPlayer().getVarZero(j) > 0 && cm.getPlayer().getVarZero(j) < 100) {
                                text += "#L" + i + "#" + getIcon(j) + " #b" + js + " #k" + getJob(cm.getPlayer(), j) + "#l\r\n";
                            }
                        }
                        cm.sendSimpleS("Select which job you would like to expand on?\r\n" + text, 16);
                    } else {
                        cm.sendOkS("I must take on this Challenge being level 250+", 2);
                    }
                } else {
                    cm.sendOkS("I must clear Party Mode Easy first.", 2);
                }
            } else {
                cm.sendOkS("I must take on this Challenge Alone.", 2);
            }
        } else {
            cm.sendOkS("This trail requires #b#i4420053# " + cm.getItemName(4420053) + "#k", 2);
        }
    }
    if (status == 1) {
        jb = job[selection];
        js = jobName[selection];
        if (cm.getPlayer().getJob() == jb) {
            cm.sendYesNo("Are you Ready to begin your job trails as a #r" + js + "#k?");
        } else {
            cm.sendOkS("This is not the correct job to take on this challenge.", 2);
        }
    }
    if (status == 2) {
        if (cm.haveItem(4420053, 1)) {
            if (cm.getPlayer().getJob() == jb) {
                if (!cm.getPlayer().isGroup()) {
                    var em = cm.getEventManager("MP_Endless_Job2");
                    if (em != null) {
                        if (cm.getPlayer().getStamina() >= stam) {
                            var tier = 1 + cm.getPlayer().getVarZero(cm.getPlayer().getJob());
                            if (!em.startPlayerInstance(cm.getPlayer(), 1, tier, 51)) {
                                cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                            } else {
                                cm.gainItem(4420053, -1);
                                cm.dispose();
                            }
                        } else {
                            cm.sendOk("Not enough stamina.");
                        }
                    } else {
                        cm.sendOk("Event has already started, Please wait.");
                    }
                } else {
                    cm.sendOkS("I must take on this Challenge Alone.", 2);
                }
            } else {
                cm.sendOkS("This is not the correct job to take on this challenge.", 2);
            }
        } else {
            cm.sendOkS("This trail requires #b#i4420053# " + cm.getItemName(4420053) + "#k", 2);
        }
    }
}