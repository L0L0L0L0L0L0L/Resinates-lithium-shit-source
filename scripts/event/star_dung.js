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


var recruitMap = 52001;
var entryMap = 52001;
var exitMap = 870000000;

var eventMapId = 52001;

var startMap = 52001;
var endMap = 52001;

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

function setup(player, mob, tier) {
    var eim = em.newInstance(player, "Dung_" + player.getName(), true);
    var map = eim.getMapInstance(entryMap);
    eim.setMapInfo(map, exitMap);
    map.setSkillRate(2.5);
    map.setExpRate(5.0);
    eim.setExitMap(player.getMapId());
    eim.setValue("mob", mob);
    eim.setValue("level", 10);
    eim.setValue("tier", tier);
    eim.setValue("finished", 0);
    eim.startEventTimer(30 * 60000);
    map.forceRespawn(mob, 10, 1, false, true, tier);
    return eim;
}

function bomb(eim) {
    if (eim.getValue("finished") == 0) {
        var map = eim.getMapInstance(entryMap);
        var mob = eim.getValue("mob");
        var tier = eim.getValue("tier");
        map.forceRespawn(mob, 10, 1, false, true, tier);
        eim.schedule("bomb", 2000);
    }
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    eim.schedule("bomb", 5000);
    player.dropMessage(6, "Event will begin in few seconds. Mob: " + eim.getValue("mob") + " Tier: " + eim.getValue("tier"));
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

