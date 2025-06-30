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
var option = 0;

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
        if (cm.isCloseNpc(7500)) {
            if (cm.getPlayer().countItem(4009180) > 0) {
                cm.sendYesNo("Hey this is a letter from my friend. Give it to me?");
            } else {
                option = 1;
                cm.sendYesNo("Hey you want to get out of here?");
            }
        } else {
            cm.sendOk("Come closer to me. Cupcakes.");
        }
    } else if (status == 1) {
        if (cm.isCloseNpc(7500)) {
            if (cm.getPlayer().countItem(4009180) > 0 && option < 1) {
                cm.gainItem(4009180, -1);
                cm.gainItem(2049185, 10);
                if (!cm.getPlayer().achievementFinished(901)) {
                    cm.gainEquip(1002419, 1);
                }
                cm.getPlayer().finishAchievement(901);
                cm.sendOk("Ugh.. Here take this reward.");
            } else {
                cm.getPlayer().finishAchievement(901);
                cm.sendOk("Ugh.. get out of here will ya.");
            }
        } else {
            cm.sendOk("Come closer to me. Cupcakes.");
        }
    }
}