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
var entryMap = 450006450;
var exitMap = 450006440;

var minMapId = 450006450;
var maxMapId = 450006450;
var eventMapId = 450006450;

var eventTime = 30;     // 10 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, diff) {
    var eim = em.newInstance(player, "ark2_battle" + player.getName(), true);
    var map = eim.getMapInstance(entryMap);
    eim.setIntProperty("scale", diff);
    eim.setIntProperty("level", level);
    var hits = Math.floor(Math.pow(eim.getIntProperty("scale"), 1.5));
    map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(8860000, eim.getIntProperty("level"), eim.getIntProperty("scale"), true, false, true, true, hits), eim.newPoint(-1220, -275));
    eim.startEventTimer(eventTime * 60000);
    eim.schedule("waves", 10 * 1000);
    eim.setIntProperty("finish", 0);
    return eim;
}

function waves(eim) {
    if (eim.getIntProperty("finish") < 1) {
        var map = eim.getMapInstance(eventMapId);
        var count = map.getSpawnedMonstersOnMap();
        if (count < 40) {
            var level = eim.getIntProperty("level");
            var scale = eim.getIntProperty("scale");
            for (var i = 1; i <= 10; i++) {
                map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8860002, level, scale - 1), eim.newPoint(eim.getRandom(-2500, -300), -500));
            }
            for (var i = 1; i <= 5; i++) {
                map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8860001, level, scale), eim.newPoint(eim.getRandom(-2500, -300), -500));
            }
        }
        eim.schedule("waves", eim.getRandom(2, 4) * 1000);
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
    var map = eim.getMapInstance(eventMapId);
    if (mob.getId() == 8860000) {
        eim.setIntProperty("finish", 1);
        eim.gainPartyStat(5, 25);
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
