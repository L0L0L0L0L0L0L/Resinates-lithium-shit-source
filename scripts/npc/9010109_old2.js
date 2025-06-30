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
var cube = 4310501;
var price = 1;
var ach = new Array(0, 1, 2, 3, 4, 5, 6, 7, 8);
var ach2 = new Array(0, 10, 20, 30, 40, 50, 60, 70);
var level = new Array(0, 0, 0, 250, 500, 750, 1000, 1500, 2000);
var cost = new Array(50, 100, 250, 500, 1000, 2500, 5000, 7500, 10000);
var names = new Array("Exp", "Drop", "Meso", "All Stat", "Overower", "Damage", "Boss Damage", "Ignore Defense");
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";

var option = 0;
var rounds = 0;
var base = 0;

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
        var selStr = "Welcome to #rMAPLE POINT#k Bonus Stat System. These stats are one time buy and are perm applied across the account.\r\nWhich Stat type would you like to explore?\r\n";
        selStr += "#L0#" + star + "#bExp Bonuses#l#k\r\n";
        selStr += "#L10#" + star + "#bDrop Bonuses#l#k\r\n";
        selStr += "#L20#" + star + "#bMeso Bonuses#l#k\r\n";
        selStr += "#L30#" + star + "#bStat Bonuses#l#k\r\n";
        selStr += "#L40#" + star + "#bOverpower Bonuses#l#k\r\n";
        selStr += "#L50#" + star + "#bDamage Bonuses#l#k\r\n";
        selStr += "#L60#" + star + "#bBoss Damage Bonuses#l#k\r\n";
        selStr += "#L70#" + star + "#bIgnore Defense Bonuses#l#k\r\n";
        cm.sendSimple(selStr);
    } else if (status == 1) {
        base = 1000 + selection;
        option = selection / 10;
        var selStr = "Which Stat type would you like to explore?\r\n";
        for (var i = 0; i < ach.length; i++) {
            var amount = cm.getAchievement(base + i).getAmount();
            var txt = "%";
            if (selection == 70) {
                txt = "";
            }
            if (!cm.getPlayer().getAchievement(base + i)) {
                if (cm.getPlayer().getTotalLevel() >= level[i]) {
                    selStr += "#L" + i + "#" + star + "#b" + names[option] + " +" + amount + "" + txt + "#k (#rPrice: " + cost[i] + " MP#k)#l\r\n";
                } else {
                    selStr += "#L" + (200 + i) + "#" + star + "#r" + names[option] + " +" + amount + "" + txt + "#k (#rReq. Lvl: " + level[i] + "#k)#l#k\r\n";
                }
            } else {
                selStr += "#L" + (210 + i) + "#" + star + "#g" + names[option] + " +" + amount + "" + txt + " (Unlocked)#l#k\r\n";
            }
        }
        cm.sendSimple(selStr);
    } else if (status == 2) {
        base += selection;
        if (selection >= 210) {
            cm.sendOk("#b" + cm.getAchievementName(base - 210) + "#k is already unlocked.");
        } else {
            if (selection >= 200) {
                var opt = selection - 200;
                cm.sendOk("This bonus requires level " + level[opt] + " or higher to unlock.");
            } else {
                price = cost[selection];
                cm.sendYesNo("Do you want to confirm that you want to spend #r" + price + " Maple Points#k on #b" + cm.getAchievementName(base) + "#k?");
            }
        }
    } else if (status == 3) {
        if (cm.haveItem(cube, price)) {
            cm.gainItem(cube, -price);
            cm.getPlayer().finishAchievement(base);
            cm.sendOk("You have successfully unlocked #b" + cm.getAchievementName(base) + "#k.");
        } else {
            cm.sendOk("You currently do not have enough Maple Points for this bonus.");
        }
    }
}