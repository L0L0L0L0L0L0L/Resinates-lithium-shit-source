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
var ticketId = 4310066;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var cost = 0;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var amount = 1000000;
var slotcount = 0;
var ep = 0;
var tier = 0;
var power = 0;
var option = 0;
var multi = 2;
var etc = 0;

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
            etc = cm.getPlayer().getETC() + 1;
            if (cm.getPlayer().getETC() < 10000) {
                cost = parseInt(Math.pow(etc, multi));
                var text = "";
                text += "#L4##bCash in #i4310066##k (#r" + cost + "x#k) #bfor Casino Power Up?#k#l\r\n\r\n#rCurrent Etc%: " + etc + "#k";
                cm.sendSimple("Welcome to Black Market?\r\n" + text);
            } else {
                cm.sendOkS("Your Etc% is currently over my threshold of #r10,000%#k!", 16);
            }
        } else if (status == 1) {
            if (selection == 4) {
                cm.sendYesNoS("Are you sure you want to exchange #i4310066# (#b" + cost + "#k) Etc%: " + etc + "?", 16);
            }
        } else if (status == 2) {
            cost = parseInt(Math.pow(cm.getPlayer().getETC() + 1, multi));
            if (cm.haveItem(4310066, cost)) {
                cm.getPlayer().gainStat(1, 10);
                cm.gainItem(4310066, -cost);
                if (cm.getPlayer().getETC() < 10000) {
                    cost = parseInt(Math.pow(cm.getPlayer().getETC() + 1, multi));
                    if (cm.haveItem(4310066, cost)) {
                        cm.sendYesNoS("Are you sure you want to exchange #i4310066# (#b" + cost + "#k) for Etc% " + cm.getPlayer().getETC() + "?", 16);
                    } else {
                        cm.sendOkS("You have increased your ETC%", 16);
                    }
                } else {
                    cm.sendOkS("Your Etc% is currently over my threshold of #r1000%#k!", 16);
                }
            } else {
                cm.sendOkS("You dont have enough #i4310066#", 16);
            }
        } else if (status == 3) {
            status = 3;
            action(0, 0, 0);
        } else if (status == 4) {
        }
    }
}