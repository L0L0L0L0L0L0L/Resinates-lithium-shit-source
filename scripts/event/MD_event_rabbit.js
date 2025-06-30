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


var event = "MD_event_rabbit_";
var exitMap = 221023400;
var eventMapId = 221023401;

var eventTime = 60; //60 minutes
var lobbyRange = [0, 8];

function setLobbyRange() {
    return lobbyRange;
}

function init() {
}

function setup(player) {
    var eim = em.newInstance(player, event + player.getName(), false);
    eim.getMapInstance(eventMapId).setMini(true);
    eim.startEventTimer(eventTime * 30 * 1000);
    eim.setIntProperty("locked", 0);
    //respawnStages(eim);
    return eim;
}

function respawnStages(eim) {
    eim.getMapInstance(eventMapId).instanceRespawn();
    eim.schedule("respawnStages", 1000);
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(eventMapId);
    player.changeMap(map, map.getPortal(0));
    player.dropMessage(6, "[Training] You have 30 minutes to gather as many mobs as possible.");
}

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function scheduledTimeout(eim) {
    eim.exitParty(exitMap);
}

function playerDisconnected(eim, player) {
    eim.unregisterPlayer(player);
}

function clear(eim) {

}

function changedMap(eim, player, mapid) {
    if (mapid != eventMapId) {
        eim.exitPlayer(player, exitMap);
    }
}

function monsterValue(eim, mobId) {
    return 1;
}

function monsterKilled(mob, eim) {
}

function allMonstersDead(eim) {
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, exitMap);
}

function cancelSchedule() {
}

function dispose() {
}

function end(eim) {
    eim.exitPlayer(player, exitMap);
}
// ---------- FILLER FUNCTIONS ----------

function disbandParty(eim, player) {
}

function afterSetup(eim) {
}

function changedLeader(eim, leader) {
}

function leftParty(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function clearPQ(eim) {
}

