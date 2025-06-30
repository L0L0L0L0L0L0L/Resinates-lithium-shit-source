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
    var room = 1;
    if (!pi.getMap().getClear()) {
        var password = "";
        if (pi.PortalName() == "Portal_1_1") {
            room = 2;
        }
        if (pi.PortalName() == "Portal_2_1" || pi.PortalName() == "Portal_2_2") {
            if (pi.PortalName() == "Portal_2_" + pi.getMap().getObjectInt("Portal_2_")) {
                room = 3;
                password += pi.getMap().getObjectInt("Portal_2_") + " - ";
            }
        }
        if (pi.PortalName() == "Portal_3_1" || pi.PortalName() == "Portal_3_2" || pi.PortalName() == "Portal_3_3") {
            if (pi.PortalName() == "Portal_3_" + pi.getMap().getObjectInt("Portal_3_")) {
                room = 4;
                password += pi.getMap().getObjectInt("Portal_3_") + " - ";
            }
        }
        if (pi.PortalName() == "Portal_4_1" || pi.PortalName() == "Portal_4_2") {
            if (pi.PortalName() == "Portal_4_" + pi.getMap().getObjectInt("Portal_4_")) {
                room = 5;
                password += pi.getMap().getObjectInt("Portal_4_") + " - ";
            }
        }
        if (pi.PortalName() == "Portal_5_1" || pi.PortalName() == "Portal_5_2" || pi.PortalName() == "Portal_5_3") {
            if (pi.PortalName() == "Portal_5_" + pi.getMap().getObjectInt("Portal_5_")) {
                room = 6;
                password += pi.getMap().getObjectInt("Portal_5_") + " - ";
            }
        }
        if (pi.PortalName() == "Portal_6_1" || pi.PortalName() == "Portal_6_2" || pi.PortalName() == "Portal_6_3") {
            if (pi.PortalName() == "Portal_6_" + pi.getMap().getObjectInt("Portal_6_")) {
                room = 7;
                password += pi.getMap().getObjectInt("Portal_6_") + " - ";
            }
        }
        if (pi.PortalName() == "Portal_7_1" || pi.PortalName() == "Portal_7_2") {
            if (pi.PortalName() == "Portal_7_" + pi.getMap().getObjectInt("Portal_7_")) {
                room = 8;
                password += pi.getMap().getObjectInt("Portal_7_");
            }
        }
        if (pi.PortalName() == "Portal_8_1" || pi.PortalName() == "Portal_8_2" || pi.PortalName() == "Portal_8_3") {
            if (pi.PortalName() == "Portal_8_" + pi.getMap().getObjectInt("Portal_8_")) {
                pi.getMap().broadcastMapMsg("Portal is now open!", 5120205);
                pi.getMap().showClear();
                room = 9;
            }
        }
        
        
        
        
        pi.warp(4002, "Portal" + room);
        //pi.mapMessage(6, pi.PortalName());
        pi.playPortalSound();
    } else {
        pi.warp(4002, "Portal" + 9);
        pi.playPortalSound();
    }
    return true;
}
