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

/* @author RonanLana */

var level = 2200;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function enter(pi) {
        var em = pi.getEventManager("Darknell");
        if (em != null) {
            if (pi.getPlayer().isGroup()) {
                if (pi.getPlayer().isLeader()) {
                    if (em.getEligiblePartyAch(pi.getPlayer(), level, 69) && em.getEligiblePartyAch(pi.getPlayer(), level, 271)) {
                        if (!em.startPlayerInstance(pi.getPlayer())) {
                            pi.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                        } else {
                            pi.playPortalSE();
                            return true;
                        }
                    } else {
                        pi.playerMessage(5, "You cannot start this party quest yet, because some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ".");
                    }
                } else {
                    pi.playerMessage(1, "Only the leader of party can start the instance.");
                }
            } else {
                pi.playerMessage(1, "Event is Party/Raid Mode.");
                pi.playerMessage(5, "Party [minlvl=" + level + ", minplayers=" + minparty + ", maxplayers=" + maxparty + "]");
                pi.playerMessage(5, "Raid [minlvl=" + level + ", minplayers=" + minraid + ", maxplayers=" + maxraid + "]");
            }
        } else {
            pi.playerMessage(5, "Event has already started, Please wait.");
        }
    return false;
}