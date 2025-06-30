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


var entryMap = 105090311;
var exitMap = 105090311;

var eventMapId = 105090320;

var eventTime = 60; //60 minutes

var lobbyRange = [0, 8];

function setLobbyRange() {
    return lobbyRange;
}

function init() {
}

function setup(player) {
    var eim = em.newInstance("Tower_Train" + player.getName());
    eim.getMapInstance(eventMapId).setMini(true);
    eim.startEventTimer(eventTime * 30 * 1000);
    eim.setIntProperty("locked", 0);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    player.dropMessage(6, "[Training] You have 60 minutes to gather as many items as you can. Use @spawn ID.");
    player.dropMessage(6, "[Training] You can find the list of mob names on discord under server info section.");
}

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function scheduledTimeout(eim) {
    eim.exitPlayer(player, exitMap);
}

function playerDisconnected(eim, player) {
    eim.unregisterPlayer(player);
}

function clear(eim) {

}

function changedMap(eim, player, mapid) {
    if (mapid != eventMapId) {
        eim.unregisterPlayer(player);
    }
}

function monsterValue(eim, mobId) {
    return 1;
}

function monsterKilled(mob, eim) {
}

function allMonstersDead(eim) {
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

