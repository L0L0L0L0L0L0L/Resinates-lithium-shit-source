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
var entryMap = 87301;
var exitMap = 87300;

var eventMapId = 87301;
var minMapId = 87301;
var maxMapId = 87301;

var eventTime = 60;     // 140 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player) {
    var eim = em.newInstance(player, "TimeDam_" + player.getName(), true);
    var map = eim.getMapInstance(eventMapId);
    map.setInstanced(true);
    eim.setIntProperty("finished", 0);
    eim.schedule("start", 5000);
    eim.setIntProperty("kills", 0);
    return eim;
}

function start(eim) {
    var map = eim.getMapInstance(eventMapId);
    map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8880102), eim.newPoint(eim.getRandom(-660, 1600), eim.getRandom(-600, 0)));
    map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8880102), eim.newPoint(eim.getRandom(-660, 1600), eim.getRandom(-600, 0)));
    map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8880102), eim.newPoint(eim.getRandom(-660, 1600), eim.getRandom(-600, 0)));
    map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8880102), eim.newPoint(eim.getRandom(-660, 1600), eim.getRandom(-600, 0)));
    map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(8880100, 850, 29, true, false, false, true, 100), eim.newPoint(450, 0));
    eim.startEventTimer(eventTime * 60000);
    eim.schedule("bean", 30000);
}

function bean(eim) {
    var map = eim.getMapInstance(eventMapId);
    if (eim.getIntProperty("finished") < 1) {
        if (map.getSpawnedMonstersOnMap() < 20) {
            for (var i = 0; i < 3; i++) {
                map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8880102), eim.newPoint(eim.getRandom(-660, 1600), eim.getRandom(-600, 0)));
            }
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
    if (mob.getId() == 8880100) {
        eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8880101, 850, 30, true, false, true, true, 100), eim.newPoint(450, 0));
    }
    if (mob.getId() == 8880101) {
        eim.gainPartyStat(6, 10);
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
