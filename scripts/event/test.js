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


var recruitMap = 450009050;
var entryMap = 450009050;
var exitMap = 450009050;

var eventMapId = 75200;

var startMobId = 9305300;
var endMobId = 9305339;

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

function setup(player, level, diff, mode) {
    var eim = em.newInstance(player, "Test_" + player.getName(), true);
    eim.setExitMap(player.getMapId());
    eim.setIntProperty("exit", player.getMapId());
    eim.setScale(false);
    var map = eim.getMapInstance(eventMapId);
    map.setReturnMapId(player.getMapId());
    var mega = mode >= 4;
    var kaotic = (mode == 2 || mode == 3 || mode == 5 || mode == 6);
    var ult = (mode == 3 || mode == 6);
    var mob = eim.getKaoticMonster(9402134, level, diff, true, false, false, true, 999, mega, kaotic, ult);
    mob.disableExp();
    map.spawnMonsterOnGroundBelow(mob, eim.newPoint(0, 80));
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId));
    player.dropMessage(6, "Test out your damage on this dummy");
    player.dropMessage(6, "Dummy cannot be destoryed!");
}

//event --------------------------------------------------------------------

function start(eim) {

}

function finish(eim) {
    eim.exitParty(eim.getIntProperty("exit"));
}

//timer ending --------------------------------------------------------------------
function scheduledTimeout(eim) {
    eim.exitParty(eim.getIntProperty("exit"));
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
    eim.exitPlayer(player, eim.getIntProperty("exit"));
}

function playerDisconnected(eim, player) {
    eim.exitPlayer(player, eim.getIntProperty("exit"));
}

function clear(eim) {
}

function changedMap(eim, player, mapid) {
    if (mapid != eventMapId) {
        eim.exitPlayer(player, eim.getIntProperty("exit"));
    }
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, eim.getIntProperty("exit"));
}

function cancelSchedule() {
}

function dispose() {
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
}

function clearPQ(eim) {
}

