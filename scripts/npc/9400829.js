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
var status;
var reward = 4033320;
var rewamount = 1;
var option = 0;
var box = 0;
var amount = 0;
var job = 0;
var price = 1;

var boss = "Khan";
var ach = 404;
var instance = "BGC_Khan";
var star1 = "#fUI/Custom.img/star/6#";
var star2 = "#fUI/Custom.img/star/6#";
var star3 = "#fUI/Custom.img/star/6#";
var star4 = "#fUI/Custom.img/star/6#";
var star5 = "#fUI/Custom.img/star/6#";
var star6 = "#fUI/Custom.img/star/7#";
var icon = "#fUI/Custom.img/job/#";
var jobName = new Array("Hero", "Paladin", "Dark Knight", "Fire Mage", "Ice Mage", "Bishop", "Bowman", "X-Bowman", "Hermit", "Shadower", "Dual-Blade", "Buccaneer", "Corsair", "Cannon Master", "Kain", "Kanna", "Path Finder", "NightWalker", "Ark", "Evan", "Battle Mage", "Wild Hunter", "Jett", "Burster", "Aran");
var job = new Array(112, 122, 132, 212, 222, 232, 312, 322, 412, 422, 434, 512, 522, 532, 1112, 1212, 1312, 1412, 1512, 2218, 3212, 3312, 2312, 3512, 2112);
var jb;
var js;

var Cantidad;
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
        return "(#rLOCKED#k)";
    } else {
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
    }
    return text;
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        if (cm.getPlayer().getQuestLock(6529) > 0) {
            cm.sendOk("Come back later.\r\n#b" + cm.secondsToString(cm.getPlayer().getQuestLock(6529)) + "#k.");
            return;
        }
        if (cm.getPlayer().isGMJob()) {
            cm.sendYesNoS("Would you like to change jobs?", 16);
        } else {
            cm.sendYesNoS("Would you like to revert back to #rBase Job (GM)#k?", 16);
        }
    }
    if (status == 1) {
        if (cm.getPlayer().isGMJob()) {
            if (cm.getPlayer().achievementFinished(270)) {
                if (cm.getPlayer().getTotalLevel() >= 200) {
                    var text = "";
                    for (var i = 0; i < job.length; i++) {
                        var j = job[i];
                        var k = jobName[i];
                        if (cm.getPlayer().getVarZero(j) <= 100) {
                            text += "#L" + i + "#" + getIcon(j) + " #b" + k + " #k" + getJob(cm.getPlayer(), j) + "#l\r\n";
                        }
                    }
                    cm.sendSimpleS("Select which job you would like to become!\r\n#bEvery Rank of job grants +10% Damage#k\r\n#bEvery Rank of other jobs grants +1% Damage#k\r\n" + text, 16);
                } else {
                    cm.sendOkS("You must be at least #rLevel 200#k or higher to be able to change jobs! You can level up in Party Easy Mode or Endless Mode.", 16);
                }
            } else {
                cm.sendOkS("I must clear #rParty Mode Easy#k in #bDungeon Room#k.", 16);
            }
        } else {
            cm.getPlayer().switchJob(910);
            cm.getPlayer().setQuestLock(6529, 10);
            cm.sendOkS("You have been reverted back to #rBase Job (GM)#k.", 16);
        }
    }
    if (status == 2) {
        jb = job[selection];
        js = jobName[selection];
        if (cm.getPlayer().getVarZero(jb) > 0) {
            cm.sendYesNoS("Are you sure you want to switch to #r" + js + "#k?\r\n#rAll skills set to macro and keybinds will be cleared from keybindings#k.", 16);
        } else {
            cm.sendOkS("This job has not been unlocked.\r\n#rHead over to Dungeon Room (6) and complete Job Trials to unlock more jobs.#k", 16);
        }
    }
    if (status == 3) {
        if (cm.getPlayer().getVarZero(jb) > 0) {
            cm.getPlayer().switchJob(jb);
            cm.getPlayer().setQuestLock(6529, 10);
            cm.sendOkS("You have successfully changed to #r" + js + "#k!", 16);
        } else {
            cm.sendOkS("This job has not been unlocked.\r\n#rHead over to Dungeon Room (6) and complete Job Trials to unlock more jobs.#k", 16);
        }
    }
}


