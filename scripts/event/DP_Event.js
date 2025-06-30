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
var entryMap = 76300;
var exitMap = 102000000;

var minMapId = 76300;
var maxMapId = 76300;
var eventMapId = 76300;

var eventTime = 60;     // 10 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "DP_Event_" + player.getName(), true);
    var map = eim.getMapInstance(entryMap);
    map.setInstanced(true);
    map.setReturnMapId(exitMap);
    eim.setExitMap(exitMap);
    eim.schedule("start", 5000);
    eim.setIntProperty("finished", 0);
    eim.setIntProperty("scale", scale);
    eim.setIntProperty("level", level > 500 ? 500 : level);

    return eim;
}

function start(eim) {
    var scale = eim.getIntProperty("scale");
    var level = eim.getIntProperty("level");
    eim.setIntProperty("spawn", 100);
    eim.broadcastPlayerMsg(5, "You have 10 mins to kill as many monsters as you can. Monsters - Tier " + scale + " - Starting Level: " + level);
    eim.startEventTimer(1000 * 60 * 10);
    eim.schedule("waves", 1000);
}

function waves(eim) {
    if (!eim.isDisposed()) {
        var map = eim.getMapInstance(eventMapId);
        var level = eim.getIntProperty("level");
        var scale = eim.getIntProperty("scale");
        if (map.getSpawnedMonstersOnMap() < eim.getIntProperty("spawn")) {
            var spawncount = 100 - map.getSpawnedMonstersOnMap();
            for (var i = 0; i < spawncount; i++) {
                map.spawnMonsterOnGround(eim.getMonsterNoDrops(9840000, level, scale, false), eim.newPoint(eim.getRandom(-300, 1300), eim.getRandom(-1000, -200)));
            }
        }
        eim.schedule("waves", 4000);
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
