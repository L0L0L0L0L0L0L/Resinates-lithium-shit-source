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
/* NPC Base
 Map Name (Map ID)
 Extra NPC info.
 */

var status;
var ticketId = 5052000;
var mapName = ["Henesys", "Ellinia", "Perion", "Kerning City", "Sleepywood", "Mushroom Shrine", "Showa Spa (M)", "Showa Spa (F)", "Ludibrium", "New Leaf City", "El Nath", "Nautilus"];
var curMapName = "";
var option = 0;
var power = 0;
var scale = 1;
var cost = 0;
var pots = 0;
var questid = 5000;
var questtime = 3600;//30 min
var ep = 0;
var time = 0.0;
var coins = 0;
var mpe = 0;

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
    if (status == 0 && mode == 1) {
        pots = cm.getPlayer().getAccVara("POT");
        if (pots > 0) {
            if (cm.getPlayer().getQuestLock(questid) > 0) {
                cm.sendOk("Come see me after, to claim your rewards.\r\n#b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k\r\n\r\n#bLast Reward Claimed#k: #r" + cm.createTime(cm.getPlayer().getAccVar("Magic_Pot")) + "#k\r\n");
            } else {
                time = cm.getPlayer().getAccRewardScale();
                var text = "\r\n";
                if (cm.getPlayer().getAccVar("Magic_Pot") > 0) {
                    text = "#bLast Reward Claim#k: #r" + cm.createTime(cm.getPlayer().getAccVar("Magic_Pot")) + "#k\r\n#bCurrent Multiplier#k: #r" + time.toFixed(2) + "x#k\r\n\r\n";
                }
                mpe = Math.floor(7500 * pots * time);
                text += "#b#i4310502# #t4310502# " + Math.floor(pots * time) + "x#k\r\n";//dp
                text += "#b#i4310500# #t4310500# " + Math.floor(pots * time) + "x#k\r\n";//gmb
                if (time >= 168) {
                    option = 0;
                    var selStr = "#b#L1##fUI/UIWindow2.img/Quest/icon/icon2/0# Collect Rewards#l\r\n\r\n";
                    selStr += "#r#L2##fUI/UIWindow2.img/Quest/icon/icon2/0# Create New Magic Pot#l\r\n\r\n  ";
                    cm.sendSimple("Welcome to the Supreme Pot Rewards?\r\n\#rI see that you have maxxed out your Collection Time.#k\r\nI see that you currently have #b" + pots + "#k #i5052000#\r\n#bWould you like to claim your Rewards?#k\r\n#rAll rewards will be placed into your ETC storage.#k\r\n" + text + "\r\n" + selStr);
                } else {
                    option = 1;
                    cm.sendYesNo("Welcome to the Supreme Pot Rewards?\r\nI see that you currently have #b" + pots + "#k #i5052000#\r\n#bWould you like to claim your Rewards?#k\r\n#rAll rewards will be placed into your ETC storage.#k\r\n" + text);
                }
            }
        } else {
            cm.sendOk("You dont seem to have any #i5052000#.\r\n#bYou buy Supreme Magic Pots via Crypto Donations in Discord.\r\n#rIf you own pots, make sure to activate them by speaking to Abbes#k");
        }
    } else if (status == 1) {
        if (option == 0) {
            option = selection;
        }
        var text = "";
        if (option == 1) {
            text = "You received the following rewards placed in Storage:\r\n\r\n";
            cm.getPlayer().addOverflow(4310502, Math.floor(pots * time));
            text += "#b#i4310502# #t4310502# " + Math.floor(pots * time) + "x#k\r\n";
            cm.getPlayer().addOverflow(4310500, Math.floor(pots * time));
            text += "#b#i4310502# #t4310500# " + Math.floor(pots * time) + "x#k\r\n";
        }
        if (option == 2) {
            text = "You received a New Magic Pot:\r\n\r\n";
            cm.gainItem(5052000, 1);
        }
        cm.getPlayer().setQuestLock(questid, questtime);
        text += "Come see me after\r\n#b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k\r\nto claim more rewards.";
        cm.getPlayer().setAccVar("Magic_Pot", cm.getTime());
        cm.sendOk(text);
    }
}