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


var recruitMap = 450001000;
var entryMap = 450001000;
var exitMap = 450001000;

var eventMapId = 2001;

var startMobId = 9305300;
var endMobId = 9305339;

var minPlayers = 3, maxPlayers = 6;
var minLevel = 140, maxLevel = 255;

var eventTime = 60; //30 minutes
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

function setup(player, level, diff) {
    var eim = em.newInstance(player, "Kaotic_Cave" + player.getName(), true);
    eim.setExitMap(exitMap);
    eim.schedule("begin", 10000);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId).getId(), "start");
    player.dropMessage(6, "The door will unlock in 10 seconds.");
}

//event --------------------------------------------------------------------

function begin(eim) {
    var map = eim.getMapInstance(eventMapId);
    map.setStart(true);
    map.mapEffect("Gstar/start");
    eim.startEventTimer(1000 * 60 * 60);
    eim.dropMessage(6, "The door is now unlocked!");
    eim.dropMessage(6, "You have 1 hour to complete this jump quest as many times as you can.");
    eim.dropMessage(6, "Every time you enter the door, spikes timers will change.");
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
    eim.setIntProperty("finished", 1);
    var diff = eim.getIntProperty("scale");
    if (diff <= 10) {
        eim.gainAchievement(113 + (diff / 2));
    }
    eim.gainPartyItem(4310015, eim.getIntProperty("reward"));
    eim.victory(exitMap);
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
    if (mapid != eventMapId) {
        eim.exitPlayer(player, exitMap);
    }
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    player.changeMap(eim.getMapInstance(eventMapId).getId(), "start");
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

