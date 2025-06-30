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

var entryMap = 98007;
var exitMap = 98006;
var recruitMap = 98006;
var clearMap = 98006;

var eventMapId = 98007;

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

function setup(player) {
    var eim = em.newInstance(player, "Ark_Battle_" + player.getName(), true);
    var map = eim.getMapInstance(eventMapId);
    map.setInstanced(true);
    eim.schedule("start", 10 * 1000);
    eim.setIntProperty("finish", 0);

    return eim;
}

function start(eim) {
    var map = eim.getMapInstance(eventMapId);
    for (var i = 1; i <= 5; i++) {
        map.spawnMonsterWithEffect(eim.getMonsterNoAll(8860002), 15, eim.newPoint(-380, -590));
    }
    for (var i = 1; i <= 5; i++) {
        map.spawnMonsterWithEffect(eim.getMonsterNoAll(8860002), 15, eim.newPoint(650, -590));
    }
    for (var i = 1; i <= 10; i++) {
        map.spawnMonsterWithEffect(eim.getMonsterNoAll(8860001), 15, eim.newPoint(eim.getRandom(-380, 650), -590));
    }
    map.spawnMonsterWithEffect(eim.getMonsterNoLink(8860000), 15, eim.newPoint(130, -700));
    eim.startEventTimer(eventTime * 60000);
    eim.schedule("waves", 30 * 1000);
}

function waves(eim) {
    var map = eim.getMapInstance(eventMapId);
    var count = eim.getMapInstance(eventMapId).getSpawnedMonstersOnMap();
    if (eim.getIntProperty("finish") < 1) {
        if (count < 80) {
            for (var i = 1; i <= 5; i++) {
                map.spawnMonsterWithEffect(eim.getMonsterNoAll(8860002), 15, eim.newPoint(-380, -590));
            }
            for (var i = 1; i <= 5; i++) {
                map.spawnMonsterWithEffect(eim.getMonsterNoAll(8860002), 15, eim.newPoint(650, -590));
            }
            for (var i = 1; i <= 10; i++) {
                map.spawnMonsterWithEffect(eim.getMonsterNoAll(8860001), 15, eim.newPoint(eim.getRandom(-380, 650), -590));
            }
        }
        eim.schedule("waves", 30 * 1000);
    }
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(eventMapId);
    map = eim.getMapInstance(entryMap);
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

function scheduledTimeout(eim) {
    eim.exitParty(exitMap);
}

function monsterKilled(mob, eim) {
    var map = eim.getMapInstance(eventMapId);
    if (mob.getId() == 8860000) {
        eim.setIntProperty("finish", 1);
        eim.gainPartyStat(1, 5);
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
