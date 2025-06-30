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
var cube = 4310505;
var option = -1;
var count = 0;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var ach = 0;
var limit = 16;
var page = 0;
var questid = 98172;
var option = 0;
var star1 = "#fUI/Custom.img/star/6#";
var star2 = "#fUI/Custom.img/star/6#";
var star3 = "#fUI/Custom.img/star/6#";
var star4 = "#fUI/Custom.img/star/6#";
var star5 = "#fUI/Custom.img/star/6#";
var star6 = "#fUI/Custom.img/star/7#";
var jobName = new Array("Hero", "Paladin", "Dark Knight", "Fire Mage", "Ice Mage", "Bishop", "Bowman", "X-Bowman", "Hermit", "Shadower", "Dual-Blade", "Buccaneer", "Corsair", "Cannon Master", "Kain", "Kanna", "Path Finder", "NightWalker", "Ark", "Evan", "Battle Mage", "Wild Hunter", "Jett", "Burster", "Aran");
var job = new Array(112, 122, 132, 212, 222, 232, 312, 322, 412, 422, 434, 512, 522, 532, 1112, 1212, 1312, 1412, 1512, 2218, 3212, 3312, 2312, 3512, 2112);
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

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        if (cm.getPlayer().getAchievement(270)) {
            var text = "";
            text += "#L4420050# #i4420050# #b" + cm.getItemName(4420050) + "#k (#rRandom Job#k)#l\r\n";
            text += "#L4420051# #i4420051# #b" + cm.getItemName(4420051) + "#k (#rSelect Job#k)#l\r\n";
            cm.sendSimpleS("Select which Soul you want to use:\r\n" + text, 16);
        } else {
            cm.sendOkS("I must clear Party Zone Easy to unlock this.", 16);
        }
    } else if (status == 1) {
        if (selection == 4420050) {
            option = 1;
            cm.sendYesNoS("Are you sure you want to consume #b" + cm.getItemName(4420050) + "#k for random job?", 16);
        }
        if (selection == 4420051) {
            option = 2;
            var text = "";
            for (var i = 0; i < job.length; i++) {
                var j = job[i];
                var js = jobName[i];
                if (cm.getPlayer().getVarZero(j) < 100) {
                    text += "#L" + i + "#" + getIcon(j) + " #b" + js + " #k" + getJob(cm.getPlayer(), j) + "#l\r\n";
                }
            }
            cm.sendSimpleS("Select which job you would like to unlock or expand?\r\n" + text, 16);
        }
    } else if (status == 2) {
        if (option == 1) {
            if (cm.haveItem(4420050, 1)) {
                cm.gainItem(4420050, -1);
                var roll = cm.nextInt(job.length);
                var j = job[roll];
                cm.getPlayer().upgradeJob(j);
                var js = jobName[roll];
                cm.sendOkS("" + getIcon(j) + " #r" + js + "#k has been upgraded.\r\n" + getIcon(j) + " #r" + js + "#k " + getJob(cm.getPlayer(), j) + " ", 16);
            } else {
                cm.sendOkS("I dont have any tickets.", 16);
            }
        }
        if (option == 2) {
            if (cm.haveItem(4420051, 1)) {
                cm.gainItem(4420051, -1);
                var roll = selection;
                var j = job[roll];
                cm.getPlayer().upgradeJob(j);
                var js = jobName[roll];
                cm.sendOkS("" + getIcon(j) + " #r" + js + "#k has been upgraded.\r\n" + getIcon(j) + " #r" + js + "#k " + getJob(cm.getPlayer(), j) + " ", 16);
            } else {
                cm.sendOkS("I dont have any tickets.", 16);
            }
        }
    } else if (status == 3) {


    } else if (status == 4) {

    }
}