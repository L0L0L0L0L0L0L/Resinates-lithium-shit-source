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
    var max = 7;
    if (!pi.getMap().getClear()) {
        for (var i = 0; i < max; i++) {
            if (pi.PortalName() == "floor_" + i + "_" + pi.getMap().getObjectInt("floor_" + i + "_")) {
                room = i + 2;
                if (i == 6) {
                    pi.getMap().broadcastMapMsg("Portals are now open!", 5120205);
                    pi.getMap().showClear();
                }
            }
        }
        pi.warp(4508, "Portal" + room);
        //pi.mapMessage(6, pi.PortalName());
        pi.playPortalSound();
    } else {
        pi.warp(4508, "Portal" + (max + 1));
        pi.playPortalSound();
    }
    return true;
}
