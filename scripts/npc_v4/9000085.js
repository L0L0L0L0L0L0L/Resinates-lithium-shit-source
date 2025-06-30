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
var leaf = 4000313;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var scroll = 0;
var scrollslot = 0;
var amount = 0;
var slotcount = 0;
var power = 0;
var equip;
var equips;
var options = 0;
var itemcount = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    }
    if (status == 0) {
        var rate = (cm.getPlayer().getFinishedAchievements().size() / cm.getAchievements().size()) * 100;
        var text = "Achievements Compeleted - " + cm.getPlayer().getFinishedAchievements().size() + " #B" + rate + "#  " + cm.getAchievements().size() + "\r\n\Current Achievement Points: #b" + cm.getPlayer().getCSPoints(1) + "#k\r\n\Which Achievement Category?\r\n";
        text += "#L6#  Beginner Achievements#l\r\n";
        text += "#L0#  Player Achievements#l\r\n";
        text += "#L1#  Monster Achievements#l\r\n";
        text += "#L2#  Monster Park Achievements#l\r\n";
        text += "#L4#  Quest Achievements#l\r\n";
        text += "#L3#  Party Quest Achievements#l\r\n";
        text += "#L8#  Extra Achievements#l\r\n";
        text += "#L9#  MaplePal Achievements#l\r\n";
        cm.sendSimpleS(text, 2);
    }

    if (status == 1) {
        var list = cm.getAchievementsbyCag(selection);
        var selStr = "";
        if (selection == 0) {
            selStr += ("Player Achievements:\r\n\r\n  ");
        } else if (selection == 1) {
            selStr += ("Monster Achievements:\r\n\r\n  ");
        } else if (selection == 2) {
            selStr += ("Monster Park Achievements:\r\n\r\n  ");
        } else if (selection == 4) {
            selStr += ("Quest Achievements:\r\n\r\n  ");
        } else if (selection == 5) {
            selStr += ("Jump Quest Achievements:\r\n\r\n  ");
        } else if (selection == 3) {
            selStr += ("Party Quest Achievements:\r\n\r\n  ");
        } else if (selection == 8) {
            selStr += ("Extra Achievements:\r\n\r\n  ");
        } else if (selection == 6) {
            selStr += ("Beginner Achievements:\r\n\r\n  ");
        } else if (selection == 9) {
            selStr += ("MaplePal Achievements:\r\n\r\n  ");
        }
        for (var i = 0; i < list.size(); i++) {
            var Ach = list.get(i);
            if (Ach != null) {
                if (cm.getTypeId(Ach) == 8) {
                    if (cm.getPlayer().getAchievement(cm.getAchievementId(Ach))) {
                        selStr += ("*#g" + Ach.getName() + "#k  (+#b" + Ach.getAmount() + " " + cm.getType(Ach) + "#k)\r\n\  ");
                    } else {
                        selStr += ("*#r" + Ach.getName() + "#k  (+#b" + Ach.getAmount() + " " + cm.getType(Ach) + "#k)\r\n\  ");
                    }
                } else {
                    if (cm.getPlayer().getAchievement(cm.getAchievementId(Ach))) {
                        selStr += ("*#g" + Ach.getName() + "#k  (+#b" + Ach.getAmount() + "% " + cm.getType(Ach) + "#k)\r\n\  ");
                    } else {
                        selStr += ("*#r" + Ach.getName() + "#k  (+#b" + Ach.getAmount() + "% " + cm.getType(Ach) + "#k)\r\n\  ");
                    }
                }
            }
        }
        cm.sendOkS(selStr + "\r\n\You can turn in your points by talking to Mia at Henesys for Random NX Equips with Stats.", 2);

    }
}

