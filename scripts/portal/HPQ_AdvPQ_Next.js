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
    var eim = pi.getEventInstance();
    if (eim != null && pi.getMap().getClear()) {
        pi.playPortalSound();
        if (pi.getMap().getId() == 4402) {
            pi.warp(4403, "exit");
            return true;
        }
        if (pi.getMap().getId() == 4403) {
            if (pi.getMap().getPlayerCount() != eim.getPlayerCount()) {
                pi.getPlayer().dropMessage("Everyone in party must be present to proceed.");
            } else {
                eim.warpEventTeam(4405, "exit");
            }
            return true;
        }
        if (pi.getMap().getId() == 4404) {
            pi.warp(4402, "exit");
            return true;
        }
        if (pi.getMap().getId() == 4405) {
            eim.warpEventTeam(4406, "exit");
            return true;
        }
        if (pi.getMap().getId() == 4406) {
            eim.warpEventTeam(4407, "exit");
            return true;
        }
    } else {
        pi.playPortalSound();
        pi.getPlayer().dropMessage("Portal is Closed. Map is not Cleared.");
    }
    return true;
}
