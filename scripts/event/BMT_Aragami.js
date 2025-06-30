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

var entryMap = 410007609;
var exitMap = 870000010;
var eventMapId = 410007609;
var minMapId = 410007609;
var maxMapId = 410007609;

var boss = 9480236;

var eventTime = 60;     // 10 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "BMT_Aragami_" + player.getName(), true);
    eim.getMapInstance(eventMapId).setInstanced(true);//boss
    eim.getMapInstance(eventMapId).setBoosted(player.getVar("boost"));//boss
    eim.setValue("scale", scale);
    eim.setValue("level", level);
    eim.schedule("start", 10 * 1000);
    eim.setValue("stage", 0);
    eim.setValue("finished", 0);
    return eim;
}

function start(eim) {
    eim.setValue("stage", 1);
    var mob = eim.getKaoticMonster(boss, eim.getValue("level"), eim.getValue("scale"), true, false, true, true, 999, true, true, false, false);
    eim.getMapInstance(eventMapId).spawnMonsterOnGroundBelow(mob, eim.newPoint(330, 750));
    eim.startEventTimer(30 * 60000);
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
    player.dropColorMessage(6, "Event Starting soon....");
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
    if (mob.getId() == boss) {
        eim.gainPartyItem(4310296, eim.getValue("scale"));
        eim.setValue("finished", 1);
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
