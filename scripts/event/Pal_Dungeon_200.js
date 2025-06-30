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
 * @Author Ronan
 * 3rd Job Event - Magician
 **/


var recruitMap = 67000;
var entryMap = 67000;
var exitMap = 870000100;

var eventMapId = 67000;

var startMap = 67000;
var endMap = 67205;

var minPlayers = 3, maxPlayers = 6;
var minLevel = 140, maxLevel = 255;

var eventTime = 20; //30 minutes
var lobbyRange = [0, 8];

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
    return eligible;
}

//setup --------------------------------------------------------------------

function setLobbyRange() {
    return lobbyRange;
}

function init() {
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "Pal_Dungeon_" + player.getName(), true);
    eim.setExitMap(exitMap);
    eim.startEventTimer(eventTime * 60000);
    for (var i = 67000; i <= 67005; i++) {
        var map = eim.getMapInstance(i);
        map.setReturnMapId(exitMap);
        map.spawnPalDataMap(level, eim.getRandom(1, 2), 5, 2);
        eim.setMapInfo(map, exitMap);
    }
    for (var i = 67100; i <= 67105; i++) {
        var map = eim.getMapInstance(i);
        map.setReturnMapId(exitMap);
        map.spawnPalDataMap(level, eim.getRandom(1, 2), 5, 2);
        eim.setMapInfo(map, exitMap);
    }
    for (var i = 67200; i <= 67205; i++) {
        var map = eim.getMapInstance(i);
        map.setReturnMapId(exitMap);
        map.spawnPalDataMap(level, eim.getRandom(1, 3), 5, 2);
        eim.setMapInfo(map, exitMap);
    }
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
}

//event --------------------------------------------------------------------

function start(eim) {
}

function finish(eim) {
    eim.exitParty(exitMap);
}

//timer ending --------------------------------------------------------------------
function scheduledTimeout(eim) {
    eim.exitParty(exitMap);
}

//monsters--------------------------------------------------------------------

function monsterKilled(mob, eim) {
}

function monsterValue(eim, mobId) {
    return 1;
}

function allMonstersDead(eim) {
}

//player leave --------------------------------------------------------------------

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function playerDisconnected(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function clear(eim) {
}

function changedMap(eim, player, mapid) {
    if (mapid < startMap || mapid > endMap) {
        eim.exitPlayer(player, exitMap);
    }
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, exitMap);
}

function cancelSchedule() {
}

function dispose() {
}

function end(eim) {
    eim.exitParty(exitMap);
}


// ---------- FILLER FUNCTIONS ----------

function disbandParty(eim, player) {
}

function afterSetup(eim) {
}

function changedLeader(eim, leader) {
}

function leftParty(eim, player) {
}

function clearPQ(eim) {
}

