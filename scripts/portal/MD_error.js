/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
MiniDungeon - Critical Error
*/ 

var level = 10;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var event = "MD_event_error";

var baseid = 261020300;
var dungeonid = 261020301;
var dungeons = 30;

function enter(pi) {
    if (pi.getMapId() == baseid) {
        var em = pi.getEventManager(event);
        if (em != null) {
			if (!pi.getPlayer().isGroup()) {
            if (!em.startPlayerInstance(pi.getPlayer())) {
                            pi.playerMessage(5, "Someone is already attempting the Mini-Dungeon or your instance is currently being reseted. Try again in few seconds.");
                        } else {
                            pi.playPortalSound();
                            return true;
                        }
			} else {
				pi.playerMessage(5, "Mini-Dungeon is solo mode only.");
			}
        } else {
            pi.playerMessage(5, "Mini-Dungeon has already started, Please wait.");
        }
        return false;
    } else {
        pi.playPortalSound();
        pi.warp(baseid, "MD00");
        return true;
    }
}