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
var status = 0;
var reward = 4033320;
var rewamount = 1;
var option = 0;
var box = 0;
var amount = 0;
var job = 0;
var price = 0;
var jobs = new Array("#L112##b Hero #k#l\r\n", "#L122##b Paladin #k#l\r\n", "#L132##b Dark Knight #k#l\r\n", "#L212##b Fire Mage #k#l\r\n", "#L222##b Ice Mage #k#l\r\n", "#L232##b Bishop #k#l\r\n", "#L312##b Bowman #k#l\r\n", "#L322##b X-Bowman #k#l\r\n",
        "#L412##b Hermit #k#l\r\n", "#L422##b Shadower #k#l\r\n", "#L434##b Dual-Blade #k#l\r\n", "#L512##b Buccaneer #k#l\r\n", "#L522##b Corsair #k#l\r\n", "#L532##b Cannon Master #k#l\r\n", "#L1112##b Kain (DW) #k#l\r\n", "#L1212##b Kanna (BW) #k#l\r\n",
        "#L1312##b Path Finder (WA) #k#l\r\n", "#L1412##b NightWalker (NW) #k#l\r\n", "#L1512##b Ark (TB) #k#l\r\n", "#L2218##b Evan #k#l\r\n", "#L3212##b Battle Mage #k#l\r\n",
        "#L3312##b Wild Hunter #k#l\r\n", "#L2312##b Jett #k#l\r\n", "#L3512##b Burster#k#l\r\n", "#L2112##b Aran#k#l\r\n");
var star1 = "#fUI/Custom.img/star/6#";
var star2 = "#fUI/Custom.img/star/6#";
var star3 = "#fUI/Custom.img/star/6#";
var star4 = "#fUI/Custom.img/star/6#";
var star5 = "#fUI/Custom.img/star/6#";
var star6 = "#fUI/Custom.img/star/7#";
var jobName = new Array("Hero", "Paladin", "Dark Knight", "Fire Mage", "Ice Mage", "Bishop", "Bowman", "X-Bowman", "Hermit", "Shadower", "Dual-Blade", "Buccaneer", "Corsair", "Cannon Master", "Kain", "Kanna", "Path Finder", "NightWalker", "Ark", "Evan", "Battle Mage", "Wild Hunter", "Jett", "Burster", "Aran");
var job = new Array(112, 122, 132, 212, 222, 232, 312, 322, 412, 422, 434, 512, 522, 532, 1112, 1212, 1312, 1412, 1512, 2218, 3212, 3312, 2312, 3512, 2112);

var boss = "Khan";
var ach = 404;
var instance = "BGC_Khan";

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
        return "(#rNew!#k)";
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
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            var text = "";
            for (var i = 0; i < 3; i++) {
                var index = cm.nextInt(job.length);
                var job1 = job[index];
                var jobName1 = jobName[index];
                text += "#L" + job1 + "#" + getIcon(job1) + " #b" + jobName1 + " #k" + getJob(cm.getPlayer(), job1) + "#l\r\n";
            }
            cm.sendSimpleS("Select which job you would like to pick as your reward!\r\n" + text, 1);
        }
        if (status == 1) {
            var eim = cm.getPlayer().getEventInstance();
            if (eim != null) {
                var index = job.indexOf(selection);
                var jobName1 = jobName[index];
                eim.setValue("finished", 1);
                eim.setValue("clear", 2);
                cm.getPlayer().upgradeJob(selection);
                cm.sendOk("You have successfully unlocked:\r\n" + getIcon(selection) + " #b" + jobName1 + " #k" + getJob(cm.getPlayer(), selection));
                eim.victory(eim.getValue("exit"));
            } else {
                cm.sendOk("Error with script.");
            }

        }
    }
}


