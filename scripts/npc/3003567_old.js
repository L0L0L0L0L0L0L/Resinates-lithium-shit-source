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
        if (cm.getPlayer().isGM()) {
            cm.sendYesNo("Need a ride over to World's End?");
        } else {
            if (cm.getPlayer().getMapId() == 450009050) {
                if (cm.getPlayer().getTotalLevel() >= level) {
                    if (cm.getPlayer().getAchievement(180)) {
                        cm.sendYesNo("Need a ride over to World's End?");
                    } else {
                        cm.sendOk("You must have defeated Darknell before you can venture to World's End.");
                    }
                } else {
                    cm.sendOk("You must be a minimum of level "+level+" to journey here.");
                }
            } else {
                cm.sendYesNo("Need a ride back to the Outpost?");
            }
        }

    } else if (status == 1) {
        if (cm.getPlayer().getMapId() == 450009050) {
            cm.warp(450012300);
        } else {
            cm.warp(450009050, "pt_back");
        }
    }
}