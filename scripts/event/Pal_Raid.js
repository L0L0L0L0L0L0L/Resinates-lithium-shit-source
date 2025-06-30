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


var recruitMap = 75004;
var entryMap = 75004;
var exitMap = 870000100;

var eventMapId = 75004;

var startMap = 75004;
var endMap = 75004;

var minPlayers = 3, maxPlayers = 6;
var minLevel = 140, maxLevel = 255;

var eventTime = 10; //30 minutes
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

function setup(player, type, evo) {
    var eim = em.newInstance(player, "Pal_Raid_" + player.getName(), true);
    var map = eim.getMapInstance(entryMap);
    map.setReturnMapId(player.getMapId());
    eim.setExitMap(player.getMapId());
    map.spawnRaidPal(10, eim.newPoint(eim.getRandom(1200, 1500), 400), 999, true);
    eim.setMapInfo(map, exitMap);
    eim.setValue("finished", 0);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    eim.changeMusic("BgmCustom/Poke");
}

function start(eim) {
}

function balls(eim) {
}

//event --------------------------------------------------------------------

function finish(eim) {
    eim.exitParty(eim.getExitMap());
}

//timer ending --------------------------------------------------------------------
function scheduledTimeout(eim) {
    eim.setValue("finished", 1);
    eim.exitParty(eim.getExitMap());
}

//monsters--------------------------------------------------------------------

function monsterKilled(mob, eim) {
    if (mob.getId() == 2030) {
        eim.setValue("finished", 1);
        eim.sendServerMsg("Raid Pal has been captured by " + eim.getOwner().getName() + "!!");
        eim.victory(exitMap);
    }
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
    eim.exitPlayer(player, eim.getExitMap());
}

function playerDisconnected(eim, player) {
    eim.exitPlayer(player, eim.getExitMap());
}

function clear(eim) {
}

function changedMap(eim, player, mapid) {
    if (mapid < startMap || mapid > endMap) {
        eim.exitPlayer(player, eim.getExitMap());
    }
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, eim.getExitMap());
}

function cancelSchedule() {
}

function dispose() {
}

function end(eim) {
    eim.exitParty(eim.getExitMap());
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

