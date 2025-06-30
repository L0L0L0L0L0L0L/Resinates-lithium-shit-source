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
 * @event: Vs Dunas
 */

var isPq = true;
var minPlayers = 2, maxPlayers = 6;
var minLevel = 150, maxLevel = 255;

var entryMap = 75100;
var exitMap = 450001000;
var recruitMap = 450001000;
var clearMap = 450001000;
var eventMapId = 75100;

var tdBossId = 8220010;

var eventTime = 240;     // 10 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level) {
    var eim = em.newInstance(player, "TimeBattle_" + player.getName(), true);
    var map = eim.getMapInstance(eventMapId);
    map.setInstanced(true);
    eim.schedule("start", 20 * 1000);
    eim.createEventTimer(20 * 1000);
    eim.setIntProperty("finish", 0);
    eim.setIntProperty("level", level);
	var levels = Math.floor(1 + (100 / level));
	eim.setIntProperty("scale", levels);
    return eim;
}

function start(eim) {
    eim.setIntProperty("spawn", 100);
    var map = eim.getMapInstance(eventMapId);
    var level = eim.getIntProperty("level");
    eim.broadcastPlayerMsg(5, "You have 10 minutes to kill as many monsters as you can. Monsters Level: " + level + " - Tier 3");
    eim.startEventTimer(600000);
    eim.schedule("waves", 1000);
    for (var i = 0; i < eim.getIntProperty("spawn"); i++) {
        map.spawnMonsterOnGroundBelow(eim.getMonster(8230067 + eim.getRandom(0, 1), level, 3), eim.newRandomPoint(-700, 1600, -1300, 80));
    }
}

function waves(eim) {
    if (!eim.isDisposed()) {
        var map = eim.getMapInstance(eventMapId);
        var level = eim.getIntProperty("level");
        if (eim.getMapInstance(eventMapId).getSpawnedMonstersOnMap() < eim.getIntProperty("spawn")) {
            var spawncount = eim.getIntProperty("spawn") - eim.getMapInstance(eventMapId).getSpawnedMonstersOnMap();
            for (var i = 0; i < spawncount; i++) {
                map.spawnMonsterOnGroundBelow(eim.getMonster(8230067 + eim.getRandom(0, 1), level, 3), eim.newRandomPoint(-700, 1600, -1300, 80));
            }
        }
        eim.schedule("waves", 4000);
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
    if (mapid != eventMapId) {
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
