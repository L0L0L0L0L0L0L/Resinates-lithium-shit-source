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
 * @event: Vs Papulatus
 */


var isPq = true;
var entryMap = 220080001;
var eventMapId = 220080001;
var exitMap = 220080000;
var recruitMap = 220080000;
var clearMap = 220080000;

var minMapId = 220080001;
var maxMapId = 220080001;

var eventTime = 45;     // 45 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player) {
    var eim = em.newInstance(player, "Papu_" + player.getName(), true);
    eim.getMapInstance(eventMapId).setInstanced(true);//boss
    eim.setMapInfo(eim.getMapInstance(eventMapId), exitMap);
    eim.setValue("scale", 10);
    eim.setValue("level", 100);
    eim.schedule("start", 10 * 1000);
    eim.setValue("stage", 0);
    eim.setValue("finished", 0);
    return eim;
}

function start(eim) {
    eim.setValue("stage", 1);
    var mob = eim.getKaoticMonster(8500000, eim.getValue("level"), eim.getValue("scale"), true, false, false, true, 99, false, false, false, false);
    eim.spawnMonsterOnGroundBelow(eventMapId, mob, eim.newPoint(-400, -400));
    eim.startEventTimer(30 * 60000);
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
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
    if (mapid != entryMap) {
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
}

function monsterKilled(mob, eim) {
    var level = eim.getValue("level");
    var scale = eim.getValue("scale");
    if (mob.getId() == 8500000) {
        var nMob = eim.getKaoticMonster(8500001, level, scale + 1, true, false, false, true, 99, true, false, false, false);
        eim.spawnMonsterOnGroundBelow(eventMapId, nMob, eim.newPoint(-400, -400));
    }
    if (mob.getId() == 8500001) {
        var nMob = eim.getKaoticMonster(8500002, level, scale + 2, true, false, true, true, 99, true, false, false, true);
        eim.spawnMonsterOnGroundBelow(eventMapId, nMob, eim.newPoint(-400, -400));
    }
    if (mob.getId() == 8500002) {
        eim.setValue("finished", 1);
        eim.victory(exitMap);
    }
}

function finish(eim) {
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function dispose(eim) {
}

function afterSetup(eim) {
}


