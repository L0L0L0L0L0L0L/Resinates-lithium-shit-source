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
var level = 250;
var dp = 4310502;
var amount = 25;
var letter = 4009180;
var apple = 2005106;
var stam = 2000012;

function start() {
    cm.sendYesNo("Would you like to exchange 10000 #i" + apple + "# for 1 #i" + stam + "#.");
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status == 0) {
            return;
        }
        status--;
    }
    if (status == 1) {
        cm.sendGetText("How many times do you want me to do it?\r\n\r\n");
    } else if (status == 2) {
        amount = cm.getNumber();
        if (amount > 0 && amount <= 1000) {
            cm.sendYesNoS("Are you sure you want to exchange " + (10000 * amount) + " #i" + apple + "# for #i" + stam + "# x(" + amount + ").", 2);
        } else {
            cm.sendOk("enter a number greater than 0 and less than 1000.");
        }
    } else if (status == 3) {
        if (amount > 0 && amount <= 1000) {
            if (cm.haveItem(apple, 10000 * amount)) {
                if (cm.canHold(stam)) {
                    cm.gainItem(apple, (10000 * amount) * -1);
                    cm.gainItem(stam, amount);
                    cm.sendOk("Use this potion on various bosses.");

                } else {
                    cm.sendOk("Please make room in ETC Inventory.");

                }
            } else {
                cm.sendOk("Please bring me " + (10000 * amount) + " #i" + apple + "# to craft more stamina potions.");
            }
        } else {
            cm.sendOk("enter a number greater than 0 and less than 1000.");
        }
    }
}