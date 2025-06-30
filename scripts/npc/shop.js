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
var shop = 0;
var title = "";

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
        if (cm.getPlayer().achievementFinished(270)) {
            var text = "";
            for (var i = 0; i <= 13; i++) {
                if (i != 2 && i != 5) {
                    var shopid = 5140000 + i;
                    text += "#L" + shopid + "# #b" + cm.getItemName(shopid) + "#k#l\r\n";
                }
            }
            cm.sendSimpleS("Select Type of shop you wish to open:\r\n" + text, 16);
        } else {
            cm.sendOkS("I must clear #rParty Mode Easy#k in #bDungeon Room#k in order to create shops.", 16);
        }
    }
    if (status == 1) {
        shop = selection;
        cm.sendGetTextS("Whats the title of your shop gonna be?\r\n\r\n", 16);
    }
    if (status == 2) {
        if (!cm.getPlayer().createShop(shop, cm.getText())) {
            cm.sendOkS("Unable to shop here.", 16);
        }
        cm.dispose();
    }
}


