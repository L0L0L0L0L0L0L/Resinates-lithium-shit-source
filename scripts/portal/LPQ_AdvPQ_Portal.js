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
        if (pi.PortalName() == "floor_0_0" || pi.PortalName() == "floor_0_1" || pi.PortalName() == "floor_0_2" || pi.PortalName() == "floor_0_3") {
            if (pi.PortalName() == "floor_0_" + pi.getMap().getObjectInt("floor_0_")) {
                room = 2;
            }
        }
        if (pi.PortalName() == "floor_1_0" || pi.PortalName() == "floor_1_1" || pi.PortalName() == "floor_1_2" || pi.PortalName() == "floor_1_3" || pi.PortalName() == "floor_1_4") {
            if (pi.PortalName() == "floor_1_" + pi.getMap().getObjectInt("floor_1_")) {
                room = 3;
            }
        }
        if (pi.PortalName() == "floor_2_0" || pi.PortalName() == "floor_2_1" || pi.PortalName() == "floor_2_2" || pi.PortalName() == "floor_2_3" || pi.PortalName() == "floor_2_4") {
            if (pi.PortalName() == "floor_2_" + pi.getMap().getObjectInt("floor_2_")) {
                room = 4;
            }
        }
        if (pi.PortalName() == "floor_3_0" || pi.PortalName() == "floor_3_1" || pi.PortalName() == "floor_3_2" || pi.PortalName() == "floor_3_3" || pi.PortalName() == "floor_3_4") {
            if (pi.PortalName() == "floor_3_" + pi.getMap().getObjectInt("floor_3_")) {
                room = 5;
            }
        }
        if (pi.PortalName() == "floor_4_0" || pi.PortalName() == "floor_4_1" || pi.PortalName() == "floor_4_2" || pi.PortalName() == "floor_4_3" || pi.PortalName() == "floor_4_4") {
            if (pi.PortalName() == "floor_4_" + pi.getMap().getObjectInt("floor_4_")) {
                room = 6;
            }
        }
        if (pi.PortalName() == "floor_5_0" || pi.PortalName() == "floor_5_1" || pi.PortalName() == "floor_5_2" || pi.PortalName() == "floor_5_3" || pi.PortalName() == "floor_5_4") {
            if (pi.PortalName() == "floor_5_" + pi.getMap().getObjectInt("floor_5_")) {
                room = 7;
            }
        }
        if (pi.PortalName() == "floor_6_0" || pi.PortalName() == "floor_6_1" || pi.PortalName() == "floor_6_2" || pi.PortalName() == "floor_6_3" || pi.PortalName() == "floor_6_4") {
            if (pi.PortalName() == "floor_6_" + pi.getMap().getObjectInt("floor_6_")) {
                room = 8;
            }
        }
        if (pi.PortalName() == "floor_7_0" || pi.PortalName() == "floor_7_1" || pi.PortalName() == "floor_7_2" || pi.PortalName() == "floor_7_3" || pi.PortalName() == "floor_7_4") {
            if (pi.PortalName() == "floor_7_" + pi.getMap().getObjectInt("floor_7_")) {
                room = 9;
            }
        }
        if (pi.PortalName() == "floor_8_0" || pi.PortalName() == "floor_8_1" || pi.PortalName() == "floor_8_2" || pi.PortalName() == "floor_8_3" || pi.PortalName() == "floor_8_4") {
            if (pi.PortalName() == "floor_8_" + pi.getMap().getObjectInt("floor_8_")) {
                pi.getMap().broadcastMapMsg("Portal is now open!", 5120182);
                pi.getMap().showClear();
                room = 10;
            }
        }
        //pi.getMap().broadcastMapMsg(pi.PortalName(), 5120182);
        pi.warp(pi.getMap().getId(), "portal" + room);
        //pi.mapMessage(6, pi.PortalName());
        pi.playPortalSound();
    }
    return true;
}
