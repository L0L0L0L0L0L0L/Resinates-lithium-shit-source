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

function setup(player, map) {
    var eim = em.newInstance(player, "player_bot_" + player.getName(), false);
    eventMapId = map;
    eim.setIntProperty("exit", eim.getMapInstance(eventMapId).getReturnMapId());
    eim.getMapInstance(eventMapId).removeAllNpcs();
    eim.schedule("start", 3000);
    eim.setIntProperty("locked", 0);
    eim.setMiniDungeon(false);
    return eim;
}

function start(eim) {
    eim.broadcastPlayerMsg(5, "[Training] You have 8 hours to kill as many mobs as possible.");
    eim.startEventTimer(8 * 60 * 60 * 1000);
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
    eim.exitParty(eim.getIntProperty("exit"));
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

