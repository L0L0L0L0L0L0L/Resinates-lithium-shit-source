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
        if (cm.getPlayer().getVar("kobold") < 1) {
            cm.sendOk("Welcome to our precious Stump Town, the rotten kobolds have been kidnapping our town slaves....");
        } else {
            if (cm.getPlayer().getVar("kobold") == 1) {
                cm.getPlayer().setVar("kobold", 2);
                cm.sendOk("You menetioned you found Mama Tom locked up in a cage? Maybe you should go talk to her... nearby...");
            } else if (cm.getPlayer().getVar("kobold") == 2) {
                cm.sendOk("Go speak to Mama Tom in town..");
            } else if (cm.getPlayer().getVar("kobold") == 3) {
                if (cm.haveItem(4033993, 10)) {
                    cm.gainItem(4033993, -10);
                    cm.getPlayer().setVar("kobold", 4);
                    cm.sendOk("AHH Great you have the 10 Fragments...After assembling the fragments, it tells about how Capo holds a RARE Delfinos Hideout Map. Go get it for me....It might take few attempts go prepared.");
                } else {
                    cm.sendOk("Run back to Capo Hideout, see what you can find there...I heard about a hidden map. Capos minions might contain a clue. Bring me back 10 of those clues...");
                }
            } else if (cm.getPlayer().getVar("kobold") == 4) {
                if (cm.haveItem(4033994, 1)) {
                    cm.gainItem(4033994, -1);
                    cm.gainItem(4033995, 1);
                    cm.getPlayer().setVar("kobold", 5);
                    cm.sendOk("AHH Great you have the Delfinos Hideout Map...After handing over the map, I will give this. take this back Mama Tom.");
                } else {
                    cm.sendOk("I need the file before you can continue.. You must get it from Capo himself...");
                }
            } else {
                cm.sendOk(".....");
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


