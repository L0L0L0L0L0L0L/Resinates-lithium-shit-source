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
var option;

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
            if (cm.getPlayer().getMapId() == 450005300) {
                if (cm.getPlayer().getTotalLevel() >= 250) {
                    option = 1;
                    cm.sendYesNo("Need a safe passage through the caves?");
                } else {
                    cm.sendOk("You need to be a miniuim of level 250 to enter.");
                                
                }
            }
            if (cm.getPlayer().getMapId() == 450005400) {
                option = 2;
                cm.sendYesNo("Need a way back to the lagoon?");
            }
        } else if (status == 1) {
            if (option == 1) {
                cm.warp(450005400, "inv00");
            }
            if (option == 2) {
                cm.warp(450005300, "inv00");
            }
                        
        }
    }
}