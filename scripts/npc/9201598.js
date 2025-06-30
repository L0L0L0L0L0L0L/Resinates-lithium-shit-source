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
var pollName = new Array("Poll_Equip_System", "Poll_Exp_System");
var pollOption = new Array(2, 2, 4);
var poll = "";
var vote = 0;
var option = 0;

function start() {
    //status = -1;
    //action(1, 0, 0);
    cm.sendOk("Coming Soon...");
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        if (cm.getPlayer().getTotalLevel() >= 250) {
            var selStr = "Select a Poll:\r\n";
            //selStr += "#L0# #bRemove Boom on Equip Scrolling?#k#l\r\n";
            //selStr += "#L1# #bChange Exp from Dungeons to Killing?#k#l\r\n";
            if (cm.getPlayer().isGM()) {
                selStr += "#L99# #bPoll Results#k#l\r\n";
            }
            cm.sendSimple(selStr);
        } else {
            cm.sendOk("You need level 1000+ to be able to access Polls on here.");
        }
    } else if (status == 1) {
        option = selection;
        if (option == 99) {
            var selStr = "";
            for (var p = 0; p < pollName.length; p++) {
                poll = pollName[p];
                selStr += "#bPoll Results from " + poll + "#k\r\n";
                var results = pollOption[p];
                for (var i = 0; i < results; i++) {
                    var total = cm.convertNumber(cm.getServerVar(poll + "_" + i));
                    selStr += "Option " + (1 + i) + ": #b" + total + "#k\r\n";
                }
                selStr += "\r\n";
            }
            cm.sendOk(selStr);
        } else {
            poll = pollName[option];
            vote = cm.getPlayer().getAccVara(poll);
            if (vote == 0) {
                if (option == 0) {
                    var selStr = "#bShould Boom effect on manual drag and drop scrolls blow up equips?#k\r\n";
                    selStr += "#L0# #rYes Blow them up#k#l\r\n";
                    selStr += "#L1# #rNo keep items when failing scrolls#k#l\r\n";
                    cm.sendSimple(selStr);
                }
                if (option == 1) {
                    var selStr = "#bShould Level gains come from dunegons or killing monsters gaining exp?#k\r\n";
                    selStr += "#L0# #rLevel from Dungeons#k#l\r\n";
                    selStr += "#L1# #rLevel from killing monsters with exp#k#l\r\n";
                    cm.sendSimple(selStr);
                }
            } else {
                cm.sendOk("You have alrdy voted on this poll.");
            }
        }
    } else if (status == 2) {
        option = selection;
        cm.getPlayer().addAccVar(poll, 1);
        cm.addServerVar(poll + "_" + option, 1);
        cm.gainItem(4310505, 5);
        cm.getPlayer().addAccVar("Poll_Completed", 1);
        cm.systemMsg(cm.getPlayer().getName() + " has voted on " + poll + "_" + option + "!");
        cm.sendOk("Your vote has been successfully casted. You have gained 5 Infinity Points!");
    } else {
        cm.dispose();
    }
}


