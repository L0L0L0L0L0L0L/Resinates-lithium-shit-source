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
 * @event: Zakum Battle
 */


var isPq = true;
var minPlayers = 6, maxPlayers = 30;
var minLevel = 120, maxLevel = 255;
var eventMapId = 450010800;
var exitMap = 450011990;



var minMapId = 450010800;
var maxMapId = 450010800;

var eventTime = 60;     // 140 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player) {
    var eim = em.newInstance(player, "Evil_Hilla" + player.getName(), true);
    var map = eim.getMapInstance(eventMapId);
    map.setInstanced(true);
    eim.setValue("finished", 0);
    eim.schedule("start", 10000);
    eim.setValue("kills", 0);
    return eim;
}

function start(eim) {
    var map = eim.getMapInstance(eventMapId);
    eim.dropMessage(6, "[Hilla] Come forth my servants!");

    var nMob1 = eim.getKaoticMonster(8880403, 2000, 58, true, false, false, true, 99, true, true, false, true);
    eim.spawnMonsterOnGroundBelow(eventMapId, nMob1, eim.newPoint(eim.getRandom(-800, 800), 250));

    var nMob2 = eim.getKaoticMonster(8880404, 2000, 58, true, false, false, true, 99, true, true, false, true);
    eim.spawnMonsterOnGroundBelow(eventMapId, nMob2, eim.newPoint(eim.getRandom(-800, 800), 250));

    var nMob3 = eim.getKaoticMonster(8880405, 2200, 60, true, false, true, true, 999, true, true, false, true);
    eim.spawnMonsterOnGroundBelow(eventMapId, nMob3, eim.newPoint(0, 250));

    eim.startEventTimer(eventTime * 60000);
    eim.schedule("bombs", 1000);
}

function bombs(eim) {
    if (eim.getValue("finished") == 0) {
        for (var i = 0; i < 8; i++) {
            eim.getMapInstance(eventMapId).spawnMonsterOnGround(eim.getMonsterNoAll(9601278, 1000, 20, false), eim.newPoint(eim.getRandom(-750, 750), eim.getRandom(-500, 250)));
        }
        eim.schedule("bombs", 5000);
    }
}

function necro_dem(eim) {
    if (eim.getValue("finished") == 0) {
        eim.dropMessage(6, "[Hilla] Come forth my servant!");
        var nMob1 = eim.getKaoticMonster(8880403, 2000, 58, true, false, false, true, 99, true, true, false, true);
        eim.spawnMonsterOnGroundBelow(eventMapId, nMob1, eim.newPoint(eim.getRandom(-800, 800), 250));
    }
}

function necro_lotus(eim) {
    if (eim.getValue("finished") == 0) {
        eim.dropMessage(6, "[Hilla] Come forth my servant!");
        var nMob2 = eim.getKaoticMonster(8880404, 2000, 58, true, false, false, true, 99, true, true, false, true);
        eim.spawnMonsterOnGroundBelow(eventMapId, nMob2, eim.newPoint(eim.getRandom(-800, 800), 250));
    }
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(eventMapId);
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

function changedLeader(eim, leader) {
    eim.changeEventLeader(leader);
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, exitMap);
}

function playerDisconnected(eim, player) {
    eim.exitPlayer(player, exitMap);
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
    var map = eim.getMapInstance(eventMapId);
    if (mob.getId() == 8880405) {
        eim.setValue("finished", 1);
        eim.victory(exitMap);
    }
    if (eim.getValue("finished") < 1) {
        if (mob.getId() == 8880403) {
            eim.dropMessage(6, "[Hilla] How dare You!!");
            eim.schedule("necro_dem", 30 * 1000);
        }
        if (mob.getId() == 8880404) {
            eim.dropMessage(6, "[Hilla] How dare You!!");
            eim.schedule("necro_lotus", 30 * 1000);
        }
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
