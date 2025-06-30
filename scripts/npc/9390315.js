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
        if (cm.getPlayer().getVar("kobold") < 2) {
            cm.sendOk("Welcome to our precious Stump Town, the rotten kobolds have been kidnapping our town slaves....");
        } else {
            if (cm.getPlayer().getVar("kobold") == 2) {
                if (cm.haveItem(4033996)) {
                    cm.gainItem(4033996, -1);
                    cm.getPlayer().setVar("kobold", 3);
                    cm.sendOk("#r[QUEST]#k I see that you have found Confidential Document....Take this letter over to my father.");
                } else {
                    cm.sendOk("#r[QUEST]#k The kobolds are playing games with your mind. That is not real me in that cage....\r\n\r\n#bYou should slay some kobolds outside of town and find out the real reason, then come speak with me#k.");
                }
            } else if (cm.getPlayer().getVar("kobold") == 3 || cm.getPlayer().getVar("kobold") == 4) {
                cm.sendOk("#r[QUEST]#k Speak to my father....");
            } else if (cm.getPlayer().getVar("kobold") == 5) {
                if (cm.haveItem(4033995, 1)) {
                    cm.gainItem(4033995, -1);
                    cm.gainItem(4033997, 1);
                    cm.getPlayer().setVar("kobold", 6);
                    cm.sendOk("#r[QUEST]#k Take this to the imposter in the cage....");
                } else {
                    cm.sendOk("#r[QUEST]#k Take the file over to Mama Tom.");
                }
            } else {
                cm.sendOk("You");
            }
        }




    } else if (status == 1) {

    } else {
        cm.dispose();
    }
}


