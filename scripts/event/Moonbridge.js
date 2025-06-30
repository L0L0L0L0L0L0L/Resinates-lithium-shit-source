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
var eventMapId = 450009500;
var exitMap = 450009300;



var minMapId = 450009500;
var maxMapId = 450009500;

var eventTime = 480;     // 140 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player) {
    var eim = em.newInstance(player, "Moonbridge_" + player.getName(), true);
    var map = eim.getMapInstance(eventMapId);
    map.setInstanced(true);
    eim.setValue("finished", 0);
    eim.schedule("start", 10000);
    eim.setValue("kills", 0);
    return eim;
}

function start(eim) {
    eim.getMapInstance(eventMapId).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8220100, 1600, 51, true, false, false, true, 100, true, false), eim.newPoint(0, -160));
    eim.startEventTimer(eventTime * 60000);
    eim.schedule("bean", 20000);
    eim.schedule("bomb", 10000);
    eim.schedule("bean2", 5000);
}

function bomb(eim) {
    if (eim.getValue("finished") < 1) {
        if (eim.getMapInstance(eventMapId).getSpawnedMonstersOnMap() < 40) {
            var map = eim.getMapInstance(eventMapId);
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220112, 1600, 45, false), eim.newPoint(eim.getRandom(-580,580), -160));
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220112, 1600, 45, false), eim.newPoint(eim.getRandom(-580,580), -160));
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220112, 1600, 45, false), eim.newPoint(eim.getRandom(-580,580), -160));
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220112, 1600, 45, false), eim.newPoint(eim.getRandom(-580,580), -160));
        }
        eim.schedule("bomb", 10000);
    }
}

function bean(eim) {

    if (eim.getValue("finished") < 1) {
        if (eim.getMapInstance(eventMapId).getSpawnedMonstersOnMap() < 40) {
            var map = eim.getMapInstance(eventMapId);
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220109, 1600, 42, false), eim.newPoint(eim.getRandom(-580,580), -160));
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220109, 1600, 42, false), eim.newPoint(eim.getRandom(-580,580), -160));
        }
        eim.schedule("bean", 20000);
    }
}

function bean2(eim) {
    if (eim.getValue("finished") < 1) {
        if (eim.getMapInstance(eventMapId).getSpawnedMonstersOnMap() < 40) {
            var map = eim.getMapInstance(eventMapId);
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220111, 1600, 50, false), eim.newPoint(eim.getRandom(-580,580), eim.getRandom(-500,-160)));
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220111, 1600, 50, false), eim.newPoint(eim.getRandom(-580,580), eim.getRandom(-500,-160)));
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220111, 1600, 50, false), eim.newPoint(eim.getRandom(-580,580), eim.getRandom(-500,-160)));
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220111, 1600, 50, false), eim.newPoint(eim.getRandom(-580,580), eim.getRandom(-500,-160)));
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220111, 1600, 50, false), eim.newPoint(eim.getRandom(-580,580), eim.getRandom(-500,-160)));
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220111, 1600, 50, false), eim.newPoint(eim.getRandom(-580,580), eim.getRandom(-500,-160)));
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220111, 1600, 50, false), eim.newPoint(eim.getRandom(-580,580), eim.getRandom(-500,-160)));
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8220111, 1600, 50, false), eim.newPoint(eim.getRandom(-580,580), eim.getRandom(-500,-160)));
        }
        eim.schedule("bean2", 5000);
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
    var map = eim.getMapInstance(eventMapId);
    if (mob.getId() == 8220100) {
        eim.getMapInstance(eventMapId).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8220101, 1650, 52, true, false, false, true, 110, true, false), eim.newPoint(0, -160));
    }
    if (mob.getId() == 8220101) {
        eim.getMapInstance(eventMapId).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8220102, 1675, 53, true, false, false, true, 120, true, false), eim.newPoint(0, -160));
    }
    if (mob.getId() == 8220102) {
        eim.getMapInstance(eventMapId).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8220103, 1700, 54, true, false, false, true, 130, true, false), eim.newPoint(0, -160));
    }
    if (mob.getId() == 8220103) {
        eim.getMapInstance(eventMapId).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8220104, 1750, 55, true, false, true, true, 140, true, true), eim.newPoint(0, -160));
    }
    if (mob.getId() == 8220104) {
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
