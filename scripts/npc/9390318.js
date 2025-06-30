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
var status = 0;
var items = 4009217;
var amount = 250000;
var reward = 4310100;
var rewamount = 100;
var exp = 250000;
var questid = 712;
var questtime = 28800;//30 min
var job = "thieves";
var option = 0;


//cm.sendYesNo("Hello#b #h ##k, Would you like to travel to Arboren?");
//cm.sendOk("You");
//cm.getPlayer()

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
        if (cm.getPlayer().getVar("stumpy") < 5) {
            if (cm.getPlayer().getVar("abreon") == 3) {
                cm.getPlayer().setVar("abreon", 4);
                cm.sendOk("Welcome to Stumy Town. We are constantly at war with kobolds, Please head over Canopy Town and speak with Old Man Otis.");
            } else if (cm.getPlayer().getVar("abreon") == 4) {
                cm.sendOk("Welcome to Stumy Town. We are constantly at war with kobolds, Please head over Canopy Town and speak with Old Man Otis.");
            } else {
                cm.sendOk("Welcome to Stumy Town.");
            }
        } else {
            if (cm.getPlayer().getVar("stumpy") == 5) {
                cm.getPlayer().setVar("stumpy", 6);
                cm.sendOk("Kobolds have been running in mass all through out the forest, Please take care of them");
            } else if (cm.getPlayer().getVar("stumpy") == 6) {
                cm.sendOk("Go slay those Kobolds!!");
            } else if (cm.getPlayer().getVar("stumpy") == 7) {
                cm.getPlayer().setVar("stumpy", 8);
                cm.sendOk("Go into the forest and find Grandma Tom?");
            } else {
                //code event script here
                cm.sendOk("Welcome to Stumy Town.");
            }
        }




    } else if (status == 1) {

        if (option == 1) {
            cm.warp(866000220, "out00");
        }
        if (option == 2) {
            if (cm.haveItem(items, amount)) {
                cm.gainItem(items, -amount);
                cm.gainItem(reward, rewamount);
                cm.getPlayer().miniLevelUp();
                cm.getPlayer().setQuestLock(questid, questtime);
                cm.sendOk("You have gained " + rewamount + "x #i" + reward + "#. Come back and see me in \r\n\#b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k.\r\n\ Quest ID: " + questid);
            } else {
                cm.sendOk("You currently do not have enough #i" + items + "#  " + cm.getItemName(items) + " for me to give you #i" + reward + "#.");
            }
        }
    } else {
        cm.dispose();
    }
}


