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

var bosses = new Array(30, 31, 32, 33, 37, 38, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 52, 53);
var jobs = new Array("One-Handed Swords", "One-Handed Axes", "One-Handed Blunts", "Daggers", "Wands", "Staves", "Two-Handed Swords", "Two-Handed Axes", "Two-Handed Blunts", "Spears", "Pole-Arms", "Bows", "Cross-Bows", "Claws", "Knuckles", "Guns", "Dual-Bows", "Cannons", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
//var ems = new Array("Von Leon", );
var status;
var battle = 0;
var lap = 0;
var trials = 0;
var triallevel = 0;

var allowed = false;

var item = 4034380;
var status = 0;
var password = 0;
var boost = 0;

var emi;
var level = 0;
var rlevel = 0;
var tier = 0;
var price = 0;
var mastery1 = 0;
var mast1 = 0;
var masteryLevel1 = 0;
var mastery2 = 0;
var mast2 = 0;

function start() {
    cm.sendYesNoS("Would you like to change your #bWeapon Mastery#k?", 16);
}

function action(mode, type, selection) {
    if (mode < 0) {
    } else {
        if (mode == 0 && type > 0) {
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 1) {
            var count = 0;
            var text = "";
            var player = cm.getPlayer();
            for (var i = 0; i < bosses.length; i++) {
                var dlvl = player.getLevelData(bosses[i]);
                if (dlvl > 1) {
                    var cost = dlvl * 100;
                    text += "#L" + bosses[i] + "##b" + jobs[i] + "#k - #r" + (dlvl + 100) + "%#k - #bPrice: " + cost + "#k#l\r\n";
                    count++;
                }
            }
            if (count > 0) {
                cm.sendSimple("Select which Weapon Mastery you want to change.\r\n#rPrices are based on level of Mastery in Meso bags#k.\r\n" + text);
            } else {
                cm.sendOkS("You haven't learned any weapon materies.", 16);
            }
        } else if (status == 2) {
            mastery1 = selection;
            var dlvl = cm.getPlayer().getLevelData(selection);
            price = dlvl * 100;
            var text = "";
            for (var i = 0; i < bosses.length; i++) {
                if (bosses[i] != mastery1) {
                    text += "#L" + bosses[i] + "##b" + jobs[i] + "#k#l\r\n";
                }
            }
            cm.sendSimple("Select which Weapon Mastery you want to apply to.\r\n" + text);
        } else if (status == 3) {
            mastery2 = selection;
            cm.sendYesNoS("Do you wish to change following Weapon Mastery:\r\n#b" + cm.getPlayer().getWeaponName(mastery1) + "#k to #r" + cm.getPlayer().getWeaponName(mastery2) + "#k\r\n#bPrice: " + price + " Meso Bags#k\r\n\r\n#rThis Action will erase the currect level of #b" + cm.getPlayer().getWeaponName(mastery1) + "#k\r\n#rThis Action cannot be undone!#k", 16);
        } else if (status == 4) {
            if (cm.haveItem(4310500, price)) {
                if (cm.getPlayer().changeLevelsData(mastery1, mastery2)) {
                    cm.gainItem(4310500, -price);
                    cm.sendOkS("You have successfully changed Weapon Mastery To:\r\n#r" + cm.getPlayer().getWeaponName(mastery2) + "#k - #b" + (cm.getPlayer().getLevelData(mastery2) + 100) + "%#k", 16);
                } else {
                    cm.sendOkS("Error with changing levels. Please contact Rara or Cookies in Discord", 16);
                }
            } else {
                cm.sendOkS("Seems like you dont have enough meso bags.", 16);
            }
        } else {
            cm.sendOkS("I must take on this Trial alone.", 16);
        }
    }
}