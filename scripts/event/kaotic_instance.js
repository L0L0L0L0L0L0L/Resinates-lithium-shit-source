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


var eventMapId;

var eventTime = 480; //60 minutes
var lobbyRange = [0, 8];

function setLobbyRange() {
    return lobbyRange;
}

function init() {
}

function setup(player, map, scale) {
    var eim = em.newInstance(player, "kaotic_instance_" + player.getName(), false);
    eim.setKaotic(true);
    eventMapId = map;
    eim.setIntProperty("exit", eim.getMapInstance(eventMapId).getReturnMapId());
    eim.setIntProperty("locked", 0);
    eim.setMiniDungeon(true);
    eim.startEventTimer(240 * 60000);
    return eim;
}

function start(eim) {
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(eventMapId);
    player.changeMap(map, map.getRandomSpawnPortal());
}

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.exitPlayer(player, eim.getIntProperty("exit"));
}

function scheduledTimeout(eim) {
    eim.exitParty(eim.getIntProperty("exit"));
}

function playerDisconnected(eim, player) {
    eim.unregisterPlayer(player);
}

function clear(eim) {

}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, eim.getIntProperty("exit"));
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

function scheduledTimeout(eim) {
    eim.exitParty(eim.getIntProperty("exit"));
}

function end(eim) {
    eim.exitParty(eim.getIntProperty("exit"));
}
// ---------- FILLER FUNCTIONS ----------

function disbandParty(eim, player) {
}

function afterSetup(eim) {
}

function changedLeader(eim, leader) {
}

function leftParty(eim, player) {
    eim.exitPlayer(player, eim.getIntProperty("exit"));
}

function clearPQ(eim) {
}

