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
 * @event: Vs Bergamot
 */


var isPq = true;
var minPlayers = 2, maxPlayers = 6;
var minLevel = 150, maxLevel = 255;
var entryMap = 450012210;
var exitMap = 450012200;

var minMapId = 450012210;
var maxMapId = 450012210;

var eventTime = 60;     // 10 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, lobbyid) {
    var eim = em.newInstance(player, "Darknell_boss_" + player.getName(), true);
    eim.getMapInstance(450012210).setInstanced(true);//boss
    eim.schedule("start", 10 * 1000);
    eim.setValue("stage", 0);
    return eim;
}

function start(eim) {
    eim.setValue("stage", 1);
    var nMob3 = eim.getKaoticMonster(8645009, 2400, 65, true, false, true, true, 999, true, true, false, true);
    eim.spawnMonsterOnGroundBelow(entryMap, nMob3, eim.newPoint(0, 25));
    eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8645004, 2300, 60, false, false, true, true, 125), eim.newPoint(eim.getRandom(-750, 750), 25));
    eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8645005, 2300, 60, false, false, true, true, 125), eim.newPoint(eim.getRandom(-750, 750), 25));
    eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8645006, 2300, 60, false, false, true, true, 125), eim.newPoint(eim.getRandom(-750, 750), 25));
    eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8645007, 2300, 60, false, false, true, true, 125), eim.newPoint(eim.getRandom(-750, 750), 25));
    eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8645008, 2300, 60, false, false, true, true, 125), eim.newPoint(eim.getRandom(-750, 750), 25));
    eim.startEventTimer(60 * 60000);
    eim.schedule("bombs", 1000);
}

function bombs(eim) {
    if (eim.getValue("stage") == 1) {
        for (var i = 0; i < 8; i++) {
            eim.getMapInstance(entryMap).spawnMonsterOnGround(eim.getMonsterNoAll(9601278, 1000, 20, false), eim.newPoint(eim.getRandom(-750, 750), eim.getRandom(-500, 25)));
        }
        eim.schedule("bombs", 5000);
    }
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
    if (mob.getId() == 8645009) {
        eim.setValue("stage", 2);
        eim.victory(exitMap);
    }
    if (eim.getValue("stage") == 1) {
        if (mob.getId() >= 8645004 && mob.getId() <= 8645008) {
            var id = mob.getId();
            eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(id, 2300, 60, false, false, true, true, 125), eim.newPoint(eim.getRandom(-750, 750), 25));
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
