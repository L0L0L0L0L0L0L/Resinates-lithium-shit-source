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
    //pi.getMap().getClear()
    var eim = pi.getEventInstance();
    if (eim != null) {
        var allow = false;
        if (pi.getMap().getId() == 4501) {
            allow = true;
        }
        if (pi.getMap().getId() == 4503 || pi.getMap().getId() == 4504 || pi.getMap().getId() == 4505 || pi.getMap().getId() == 4506 || pi.getMap().getId() == 4508) {
            if (pi.getMap().getClear()) {
                allow = true;
            }
        }
        if (pi.getMap().getId() == 4507) {
            if (eim.getMapInstance(4503).getClear() && eim.getMapInstance(4504).getClear() && eim.getMapInstance(4505).getClear() && eim.getMapInstance(4506).getClear()) {
                if (pi.getMap().getPlayerCount() < 4) {
                    pi.getPlayer().dropMessage("Everyone in party must be present to proceed.");
                    return;
                } else {
                    pi.playPortalSound();
                    eim.warpEventTeam(4508);
                }
            } else {
                pi.getPlayer().dropMessage("All rooms must be completed to proceed.");
            }
            return;
        }
        if (allow) {
            pi.playPortalSound();
            if (pi.getMap().getId() == 4503 || pi.getMap().getId() == 4504 || pi.getMap().getId() == 4505 || pi.getMap().getId() == 4506) {
                pi.warp(4507, "exit");
            } else {
                pi.warp(pi.getMap().getId() + 1, "exit");
            }

            return true;
        } else {
            pi.getPlayer().dropMessage("Must clear the map before progressing.");
            return false;
        }
    } else {
        pi.playPortalSound();
        pi.getPlayer().dropMessage("Portal is Closed. Map is not Cleared.");
    }
    return true;
}
