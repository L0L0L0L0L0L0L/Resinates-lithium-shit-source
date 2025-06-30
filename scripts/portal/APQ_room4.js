/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
function enter(pi) {
    //pi.mapMessage(6, pi.PortalName());
    if (pi.PortalName() == "HP_1") {
        if (!pi.getMap().getClear()) {
            pi.getMap().setObjectFlag("Star_0", true);
            pi.getMap().setObjectFlag("RStar_0", false);
            pi.playPortalSound();
            pi.warp(4004, "Link_" + pi.random(2, 9));
        } else {
            pi.getPlayer().dropMessage("This map has already been cleared.");
            return;
        }
    }
    if (pi.PortalName() == "HP_2") {
        pi.getMap().setObjectFlag("Star_1", true);
        pi.getMap().setObjectFlag("RStar_1", false);
    }
    if (pi.PortalName() == "HP_3") {
        pi.getMap().setObjectFlag("Star_2", true);
        pi.getMap().setObjectFlag("RStar_2", false);
    }
    if (pi.PortalName() == "HP_4") {
        pi.getMap().setObjectFlag("Star_3", true);
        pi.getMap().setObjectFlag("RStar_3", false);
    }
    if (pi.PortalName() == "HP_5") {
        pi.getMap().setObjectFlag("Star_4", true);
        pi.getMap().setObjectFlag("RStar_4", false);
    }
    if (pi.PortalName() == "HP_6") {
        pi.getMap().setObjectFlag("Star_5", true);
        pi.getMap().setObjectFlag("RStar_5", false);
    }
    if (pi.PortalName() == "HP_7") {
        pi.getMap().setObjectFlag("Star_6", true);
        pi.getMap().setObjectFlag("RStar_6", false);
    }
    if (pi.PortalName() == "HP_8") {
        pi.getMap().setObjectFlag("Star_7", true);
        pi.getMap().setObjectFlag("RStar_7", false);
    }
    if (pi.PortalName() == "HP_9") {
        pi.getMap().setObjectFlag("Star_8", true);
        pi.getMap().setObjectFlag("RStar_8", false);
    }
    if (!pi.getMap().getClear()) {
        var cleared = 0;
        if (pi.getMap().getObjectFlag("Star_0") == true) {
            cleared++;
        }
        if (pi.getMap().getObjectFlag("Star_1") == true) {
            cleared++;
        }
        if (pi.getMap().getObjectFlag("Star_2") == true) {
            cleared++;
        }
        if (pi.getMap().getObjectFlag("Star_3") == true) {
            cleared++;
        }
        if (pi.getMap().getObjectFlag("Star_4") == true) {
            cleared++;
        }
        if (pi.getMap().getObjectFlag("Star_5") == true) {
            cleared++;
        }
        if (pi.getMap().getObjectFlag("Star_6") == true) {
            cleared++;
        }
        if (pi.getMap().getObjectFlag("Star_7") == true) {
            cleared++;
        }
        if (pi.getMap().getObjectFlag("Star_8") == true) {
            cleared++;
        }
        if (cleared >= 9) {
            pi.getMap().setObjectFlag("D_1", false);//door
            pi.getMap().setObjectFlag("D_2", true);//door
            pi.getMap().broadcastMapMsg("The Final portal has been unlocked.", 5120205);
            pi.getMap().showClear();
        }
        pi.getMap().getVisibleObjects();
    }
    //pi.warp(4002, "Portal" + room);
    //pi.mapMessage(6, pi.PortalName());
    //pi.playPortalSound();
    return true;
}
