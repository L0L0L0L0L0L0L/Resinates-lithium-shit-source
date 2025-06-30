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
var cube = 4310502;
var price = 5;
var items = new Array(4310015, 4310020, 2583007, 2585005, 2586002, 2587001, 2340000, 2049305, 2049189, 4001895, 5220020, 4032521, 4001760, 4310260, 4430003, 4000313, 2430131);
var amount = new Array(5, 10, 1, 1, 1, 1, 1, 1, 1, 5, 1, 1, 1, 5, 1, 5, 1);
var amounts = new Array(100, 250, 25, 5, 5, 5, 10, 10, 5, 100, 5, 5, 25, 50, 5, 25, 5);
var amountss = new Array(2500, 5000, 100, 25, 25, 25, 50, 100, 25, 1000, 25, 25, 250, 500, 25, 250, 25);
var option = 0;
var rounds = 0;

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
        var selStr = "Welcome to #rDONATION#k Kaotic Slot Machine. Every single spin rewards items, match 2 symbols and whin extra amount. Match 3 Symbols win a jackpot for that item.\r\nWhich option would you like to pick from?\r\n";
        selStr += "#L1#What items does slots give?#l\r\n";
        selStr += "#L2#I want to spin some slots? (#bPrice: 5 DP#k)#l\r\n";
        cm.sendSimple(selStr);
    } else if (status == 1) {
        option = selection;
        if (option == 1) {
            selStr = "Blue = Min Amount, Red = Combo Amount, = Jackpot amount:\r\n\\r\n\ ";
            for (var i = 0; i < items.length; i++) {
                selStr += "#i" + items[i] + "# " + cm.getItemName(items[i]) + " - #b" + amount[i] + "#k  - #r" + amounts[i] + "#k - #g" + amountss[i] + "#k\r\n\ ";
            }
            cm.sendOk("Here is comeplete list of all the items:\r\n\ " + selStr);
        } else {
            cm.sendGetText("Do you want to play slots and win some possible rewards or #rJACKPOTS#k?\r\n\Each spin costs " + price + " #i" + cube + "# " + cm.getItemName(cube) + ".\r\n\How many spins would like you like to buy.\r\n\#rMaximum spin count is 250#k.\r\n\#rIF YOU DO NOT HAVE ENOUGH SPACE TO HOLD THE ITEMS,\r\n\THEY WILL BE LOST#k");
        }

    } else if (status == 2) {
        rounds = cm.getNumber();
        if (rounds > 0 && rounds <= 250) {
            if (cm.haveItem(cube, rounds * price)) {
                cm.sendYesNo("Are sure that you want to spend " + (rounds * price) + " #i" + cube + "# on " + rounds + " slot spins?\r\n\How many spins would like you like to buy.\r\n\#rIF YOU DO NOT HAVE ENOUGH SPACE TO HOLD THE ITEMS,\r\n\THEY WILL BE LOST#k");
            } else {
                cm.sendOk("You dont have enough donation points to play with.");
            }
        } else {
            cm.sendOk("You dont have enough brains to cheat this.");
        }
        //cm.warp(450009050, "pt_back");
    } else if (status == 3) {
        var rounds = cm.getNumber();
        if (rounds > 0 && rounds <= 250) {
            if (cm.haveItem(cube, rounds * price)) {
                cm.getPlayer().runSlot(4000, rounds, cube, price);
                cm.delayRewardNPC(4000 * rounds);
            } else {
                cm.sendOk("You dont have enough donation points to play with.");
            }
        } else {
            cm.sendOk("You dont have enough brains to cheat this.");
        }
        //cm.warp(450009050, "pt_back");
    }
}