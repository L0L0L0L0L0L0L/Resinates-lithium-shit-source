/*
 This file is part of the HeavenMS MapleStory Server
 Copyleft (L) 2016 - 2018 RonanLana
 
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

/**
 * @author: Ronan
 * @event: Vs Papulatus
 */

var isPq = true;
var entryMap = 4301;
var exitMap = 4300;

var minMapId = 4301;
var maxMapId = 4305;

var eventTime = 30;     // 45 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "BMPQ_" + player.getName(), true);
    eim.setValue("scale", scale);
    eim.setValue("orbs", 70);
    eim.setValue("level", level);

    for (var i = minMapId; i <= maxMapId; i++) {
        var map = eim.getMapInstance(i);
        map.closeDoor();
        if (i == minMapId) {
            map.setObjectInt("Portal_1_", eim.getRandom(1, 3));
            map.setObjectInt("Portal_2_", eim.getRandom(1, 4));
            map.setObjectInt("Portal_3_", eim.getRandom(1, 4));
            map.setObjectInt("Portal_4_", eim.getRandom(1, 4));
            map.setObjectInt("Portal_5_", eim.getRandom(1, 4));
            map.setObjectInt("Portal_6_", eim.getRandom(1, 3));
            map.setObjectInt("Portal_7_", eim.getRandom(1, 3));
            map.setObjectInt("Portal_8_", eim.getRandom(1, 3));
            map.setObjectInt("Portal_9_", eim.getRandom(1, 4));
        }
        if (i == minMapId + 1) {
            map.setFlagsBM_2();
        }
    }
    //map.setSummons(false);
    eventTime = 60 * eim.getValue("scale");
    eim.startEventTimer(eventTime * 60000);

    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
    eim.exitParty(exitMap);
}

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function playerLeft(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function changedMap(eim, player, mapid) {
    if (mapid < minMapId || mapid > maxMapId) {
        eim.exitPlayer(player, exitMap);
    }
}

function afterChangedMap(eim, player, mapid) {
    if (mapid == 4301) {
        eim.playerItemMsg(player, "Complete the portal maze by entering the portals", 5120150);
    }
    if (mapid == 4302) {
        eim.playerItemMsg(player, "Count the Stars and talk to npc to unlock the map", 5120150);
    }
    if (mapid == 4303) {
        eim.playerItemMsg(player, "Destory all the orbs to progress", 5120150);
    }
    if (mapid == 4304) {
        eim.playerItemMsg(player, "Defeat the boss!", 5120150);
    }
    if (mapid == 4305) {
        eim.playerItemMsg(player, "Crack open the treasure chest to get your rewards", 5120150);
    }
}

function changedLeader(eim, leader) {
    eim.changeEventLeader(leader);
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, exitMap);
}

function playerDisconnected(eim, player) {
    eim.unregisterPlayer(player);
}

function leftParty(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function disbandParty(eim) {
    eim.exitParty(exitMap);
}

function monsterValue(eim, mobId) {
    return 1;
}

function end(eim) {
    eim.exitParty(exitMap);
}

function monsterKilled(mob, eim) {
    if (mob.getId() == 1003) {
        eim.getMapInstance(4304).showClear();
    }
    if (mob.getId() == 9601204 || mob.getId() == 9601205 || mob.getId() == 9601206) {
        eim.gainAchievement(319 + eim.getValue("scale"));
        eim.gainPartyItem(4420015, eim.getPlayerCount() * 5);
        eim.gainPartyItem(4310150, eim.getPlayerCount() * eim.getValue("scale") * 500);
        eim.victory(exitMap);
    }


}

function finish(eim) {
    eim.exitParty(exitMap);
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function dispose(eim) {
}

function afterSetup(eim) {
}


