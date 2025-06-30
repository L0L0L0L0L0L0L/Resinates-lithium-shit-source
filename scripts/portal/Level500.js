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
    if (pi.getPlayer().getTotalLevel() >= 1000 && pi.getPlayer().achievementFinished(65)) {
        pi.warp(pi.PortalMap(), pi.TargetPortal());
        return true;
    } else {
        //pi.playerMessage(5, "Arcane River is currently closed to the public.");
        pi.playerMessage(1, "Requires Achivements:\r\nLevel 1000+\r\nDefeating Lotus");
        return false;
    }
}