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
var dp = 4310502;
var amount = 25;
var letter = 4009180;

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
        if (cm.getPlayer().getMapId() == 5002 && !cm.getPlayer().getAchievement(902)) {
            if (cm.getPlayer().countItem(4000019) >= 25) {
                cm.sendYesNo("I see you have collected 25 Snail shells.\r\nDo you wish to turn them into me?");
            } else {
                cm.sendOk("I want 25 #i4000019# Snail Shells right now. If you cant find them in your inventory, check your overflow storage using QUICK MOVE button under your mini-map.");
            }
            return;
        }
        if (cm.getPlayer().getMapId() == 5003 && !cm.getPlayer().getAchievement(903)) {
            if (cm.getPlayer().countItem(4000000) >= 25) {
                cm.sendYesNo("I see you have collected 25 Blue Snail shells.\r\nDo you wish to turn them into me?");
            } else {
                cm.sendOk("I want 25 #i4000000# Snail Shells right now. If you cant find them in your inventory, check your overflow storage using QUICK MOVE button under your mini-map.");
            }
            return;
        }
        cm.sendOk("Seems you finished everything here you little cheater.");
    } else if (status == 1) {
        if (cm.getPlayer().getMapId() == 5002) {
            if (cm.getPlayer().countItem(4000019) >= 25) {
                cm.gainItem(4000019, -25);
                cm.gainItem(2049300, 25);
                cm.gainItem(2005106, 1);
                cm.gainItem(2000005, 25);
                cm.getPlayer().finishAchievement(902);
                cm.sendOk("Ugh.. Here take this reward.");
            } else {
                cm.sendOk("Stop talking to me.");
            }
        }
        if (cm.getPlayer().getMapId() == 5003) {
            if (cm.getPlayer().countItem(4000000) >= 25) {
                cm.gainItem(4000000, -25);
                cm.gainItem(2049301, 10);
                cm.gainItem(2005106, 1);
                cm.gainItem(2000005, 25);
                cm.getPlayer().finishAchievement(903);
                cm.sendOk("Ugh.. Here take this reward.");
            } else {
                cm.sendOk("Stop talking to me.");
            }
        }
    }
}