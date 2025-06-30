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
var level = 180;
var dp = 4310502;
var amount = 25;
var letter = 4009180;
var ach = new Array(0, 0, 0, 0, 0, 0);
var ppl = new Array(0, 4, 4, 4, 4, 4);
var level = new Array(0, 125, 150, 175, 200, 500);
var questid = 7929;
var questtime = 60;//10 min
var option = 0;
var orb = 2400006;
var count = 0;
var Tplayer;
var item;
var itemamount;
var equiplist;
var name;

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
        if (cm.getPlayer().getQuestLock(questid) > 0) {
            cm.sendOk("Come see me after #b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k \r\n\      to send more items.");
        } else {
            cm.sendGetText("Type in Player name of who you wish to send #rETC#k to.\r\n\r\n ");
        }
    } else if (status == 1) {
        name = cm.getText();
        Tplayer = cm.getChannelServer().getPlayerStorage().getCharacterByName(name);
        if (Tplayer != null && !Tplayer.getEtcLock() && Tplayer.getMapId() == 910000000) {
            equiplist = cm.getPlayer().getOverflow();
            if (!equiplist.isEmpty()) {
                var selStr = "";
                for (var i = 0; i < equiplist.size(); i++) {
                    var curEquip = equiplist.get(i);
                    count += 1;
                    selStr += "#L" + i + "##i" + curEquip + "##l";
                }
                if (count > 0) {
                    cm.sendSimple("Which ETC would you like to Send?\r\n" + selStr);
                } else {
                    cm.sendOk("You currently do not have any Equips to Flame.");
                }
            } else {
                cm.sendOk("You currently do not have any Etc to Store.");
            }
        } else {
            cm.sendOk("That player is not currently online or busy.");
        }
    } else if (status == 2) {
        equip = equiplist.get(selection);
        itemamount = cm.getPlayer().getOverflowAmount(equip);
        cm.sendGetText("How many #i" + equip + "# - #r" + cm.getItemName(equip) + "#k\r\nThat you wish to send to #b" + name + "#k.\r\nYou currently have #b" + itemamount + "#k to send.\r\n ");
    } else if (status == 3) {
        count = cm.getNumber();
        if (count > 0 && count <= itemamount) {
            if (cm.getPlayer().hasEtc(equip, count)) {
                cm.sendYesNo("Are you sure you want to send #b" + count + "#k #i" + equip + "# to " + name + "?");
            } else {
                cm.sendOkS("#rYou dont have enough junk in the trunk!#k. COunt: " + itemamount, 16);
            }
        } else {
            cm.sendOkS("#rYou dont have enough junk in the trunk!#k", 16);
        }
    } else if (status == 4) {
        if (Tplayer != null && cm.getPlayer().hasEtc(equip, count)) {
            cm.getPlayer().removeOverflow(equip, count);
            Tplayer.addOverflow(equip, count);
            cm.getPlayer().dropMessage("you have sent " + count + "x - " + cm.getItemName(equip) + " to " + name);
            Tplayer.dropMessage(cm.getPlayer().getName() + " has sent you " + count + "x - " + cm.getItemName(equip));
            cm.sendOkS("You have sent " + count + " #i" + equip + "# " + cm.getItemName(equip) + " to " + name + ".", 16);
            cm.getPlayer().setQuestLock(questid, questtime);
        } else {
            cm.sendOkS("#rYou dont have enough junk in the trunk!#k\r\n" + count + " #i" + equip + "#" + cm.getItemName(equip) + " ", 16);
        }
    } else if (status == 5) {

    }
}