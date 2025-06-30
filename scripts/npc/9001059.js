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
var ticketId = 2430131;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var slotcount = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {

    } else {
        if (mode == 0 && type > 0) {

            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            var text = "";
            text += "#L2430131# #i2430131# #b" + cm.getItemName(2430131) + "#k#l\r\n";
            text += "#L2430130# #i2430130# #b" + cm.getItemName(2430130) + "#k#l\r\n";
            cm.sendSimpleS("Select which booster you want to consume.\r\n" + text, 16);
        }
        if (status == 1) {
            ticketId = selection;
            cm.sendGetText("How many #i" + ticketId + "# do you want to consume?");
        }
        if (status == 2) {
            var amount = cm.getNumber();
            if (amount > 0 && amount <= 999) {
                if (ticketId == 2430131) {
                    if (cm.haveItem(ticketId, amount)) {
                        cm.gainItem(ticketId, -amount);
                        cm.getPlayer().miniLevelUp(amount);
                        cm.sendOk("You have gained " + amount + " Mini-Levels.");
                    } else {
                        cm.sendOk("You currently do not have enough Energy Charges.");
                    }
                }
                if (ticketId == 2430130) {
                    if (cm.haveItem(ticketId, amount)) {
                        cm.gainItem(ticketId, -amount);
                        cm.getPlayer().skillBoost(amount);
                        cm.sendOk("You have gained " + (amount * 5) + " Skill Points.");
                    } else {
                        cm.sendOk("You currently do not have enough Energy Charges.");
                    }
                }
            } else {
                cm.sendOk("You currently can only consume upto 500 at a time.");
            }
        } else {

        }
    }
}


