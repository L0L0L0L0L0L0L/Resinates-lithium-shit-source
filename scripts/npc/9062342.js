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

var status = 0;
var item = 0;
var amount = 0;
var star = "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";

function start() {
    cm.sendYesNo("Do you wish exchange your #bPower Cores#k?");
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
            var text = "";
            for (var i = 0; i < 5; i++) {
                var itd = 4420040 + i;
                text += "#L" + itd + "##i" + itd + "##l ";
            }
            text += "\r\n";
            for (var i = 0; i < 5; i++) {
                var itd = 4420045 + i;
                text += "#L" + itd + "##i" + itd + "##l ";
            }
            cm.sendSimpleS("Which Power Core do you wish to use?\r\n" + text + " ", 16);
        } else if (status == 2) {
            item = selection;
            cm.sendGetTextS("How many #i" + item + "# #b" + cm.getItemName(item) + "#k would you like use?\r\n", 16);
        } else if (status == 3) {
            amount = cm.getNumber();
            if (amount > 0 && cm.haveItem(item, amount)) {
                cm.sendYesNo("Are you sure you want to apply #r" + amount + "#k #i" + item + "# #b" + cm.getItemName(item) + "#k?");
            } else {
                cm.sendOk("Your missing cores.");
            }

        } else if (status == 4) {
            if (cm.haveItem(item, amount)) {
                cm.gainItem(item, -amount);
                var stat = 0;
                var statAmount = 0;
                var statName = "";
                if (item == 4420040) {
                    stat = 3;
                    statAmount = 5 * amount;
                    statName = "Int";
                }
                if (item == 4420041) {
                    stat = 2;
                    statAmount = 5 * amount;
                    statName = "Dex";
                }
                if (item == 4420042) {
                    stat = 4;
                    statAmount = 5 * amount;
                    statName = "Luk";
                }
                if (item == 4420043) {
                    stat = 1;
                    statAmount = 5 * amount;
                    statName = "Str";
                }
                if (item == 4420044) {
                    stat = 5;
                    statAmount = 100 * amount;
                    statName = "HP/MP";
                }
                if (item == 4420045) {
                    stat = 3;
                    statAmount = 25 * amount;
                    statName = "Int";
                }
                if (item == 4420046) {
                    stat = 2;
                    statAmount = 25 * amount;
                    statName = "Dex";
                }
                if (item == 4420047) {
                    stat = 4;
                    statAmount = 25 * amount;
                    statName = "Luk";
                }
                if (item == 4420048) {
                    stat = 1;
                    statAmount = 25 * amount;
                    statName = "Str";
                }
                if (item == 4420049) {
                    stat = 5;
                    statAmount = 1000 * amount;
                    statName = "HP/MP";
                }
                if (stat > 0 && statAmount > 0) {
                    cm.getPlayer().updateStat(stat, statAmount);
                    cm.sendOk("You have gained #b" + statAmount + "#k #r" + statName + "#k");
                } else {
                    cm.sendOk("Quitting Cheating man....");
                }

            } else {
                cm.sendOk("Your misssing Cores....");
            }

        }
    }
}