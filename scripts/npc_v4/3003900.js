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

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {

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
            if (cm.getPlayer().isGM()) {
                cm.sendYesNo("Need a ride over to challenge arena?");
            } else {
                if (cm.getPlayer().getTotalLevel() >= 2800) {
                    if (cm.getPlayer().getAchievement(181)) {
                        cm.sendYesNo("Are you ready to face your ultimate challenge?");
                    } else {
                        cm.sendOk("You must have defeated Commander Will before you can venture onward.");
                    }
                } else {
                    cm.sendOk("You must be level 2800 or higher to venture onward.");
                }
            }
        } else if (status == 1) {
            cm.warp(450012500);

        }
    }
}