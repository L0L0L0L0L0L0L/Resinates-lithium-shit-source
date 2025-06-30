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

var level = 900;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function enter(pi) {
    var em = pi.getEventManager("blackHeaven_boss");
    if (em != null) {
        if (pi.getPlayer().getStamina() >= 5) {
            if (pi.getPlayer().isGroup()) {
                if (em.getEligiblePartyAch(pi.getPlayer(), level, 66)) {
                    if (!em.startPlayerInstance(pi.getPlayer())) {
                        pi.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        pi.playPortalSE();
                        pi.getPlayer().removeStamina(5);
                        return true;
                    }
                } else {
                    pi.playerMessage(5, "You cannot start this party quest yet, because some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ".");
                }
            } else {
                pi.playerMessage(1, "Event is Party Mode.");
            }
        } else {
            pi.playerMessage(1, "You do not have enough stamina to enter the event, the cost is 5 Stamina.");
        }
    } else {
        pi.playerMessage(5, "Event has already started, Please wait.");
    }
    return false;
}