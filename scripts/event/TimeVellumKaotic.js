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
 * @event: Zakum Battle
 */

var isPq = true;
var minPlayers = 6, maxPlayers = 30;
var minLevel = 120, maxLevel = 255;
var entryMap = 86044;
var exitMap = 86000;
var eventMapId = 86044;
var minMapId = 86044;
var maxMapId = 86044;
var eventTime = 30; // 140 minutes

var lobbyRange = [0, 0];
var map = 0;
var boss = 8910000;
function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, diff) {
    var eim = em.newInstance(player, "TimeVellum_Kaotic_" + player.getName(), true);
    var map = eim.getMapInstance(eventMapId);
    
    map.setInstanced(true);
    map.setBoosted(player.getVar("boost"));//boss
    eim.setIntProperty("scale", diff);
    eim.setIntProperty("level", level);
    eim.setIntProperty("finished", 0);
    eim.setIntProperty("wave", 0);
    eim.schedule("start", 10000);

    return eim;
}

function start(eim) {
    eim.setIntProperty("wave", 1);
    var map = eim.getMapInstance(eventMapId);
    var hits = Math.floor(Math.pow(eim.getIntProperty("scale"), 1.5));
    map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(8930100, eim.getIntProperty("level"), eim.getIntProperty("scale"), true, false, true, true, hits), eim.newPoint(1000, 400));
    eim.startEventTimer(eventTime * 60000);
    eim.schedule("bean", 10000);
}

function bean(eim) {
    if (eim.getIntProperty("finished") < 1) {
        var map = eim.getMapInstance(eventMapId);
        var level = eim.getIntProperty("level");
        var scale = eim.getIntProperty("scale");
        if (eim.getMapInstance(eventMapId).getSpawnedMonstersOnMap() < 8) {
            eim.dropMessage(6, "[Vellum] You cannot stop me!!!");
            map.spawnMonsterOnGround(eim.getMonsterNoAll(8930101, level, scale), eim.newPoint(eim.getRandom(-300, 1500), 400));
        }
        eim.schedule("bean", 30000);
    }
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(eventMapId);
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
    if (mob.getId() == 8930100) {
        eim.setIntProperty("finished", 1);
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
