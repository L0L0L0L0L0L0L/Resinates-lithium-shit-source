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
var entryMap = 450013300;
var exitMap = 450012300;

var minMapId = 450013300;
var maxMapId = 450013300;

var eventTime = 60;     // 10 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, lobbyid) {
    var eim = em.newInstance(player, "comm_will_boss_" + player.getName(), true);
    eim.getMapInstance(450013300).setInstanced(true);//boss
    eim.schedule("start", 10 * 1000);
    eim.setValue("stage", 0);
    return eim;
}

function start(eim) {
    eim.setValue("stage", 1);
    var nMob3 = eim.getKaoticMonster(2600800, 2750, 70, true, false, true, true, 999, true, true, false, true);
    eim.spawnMonsterOnGroundBelow(entryMap, nMob3, eim.newPoint(0, 75));
    eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8645043, 2500, 65, false, false, false, false, 250), eim.newPoint(-975, 75));
    eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8645043, 2500, 65, false, false, false, false, 250), eim.newPoint(975, 75));
    eim.startEventTimer(60 * 60000);
    eim.schedule("boss", 10000);
    eim.schedule("boss1", 30000);
    eim.schedule("bombs", 1000);
}

function bombs(eim) {
    if (eim.getValue("stage") == 1) {
        for (var i = 0; i < 8; i++) {
            eim.getMapInstance(entryMap).spawnMonsterOnGround(eim.getMonsterNoAll(9601278, 2500, 65, false), eim.newPoint(eim.getRandom(-800, 800), eim.getRandom(-500, 75)));
        }
        eim.schedule("bombs", 5000);
    }
}

function boss(eim) {
    if (eim.getValue("stage") == 1) {
        if (eim.getMapInstance(450013300).getSpawnedMonstersOnMap() < 10) {
            eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8880305, 2000, 65, false, false, false, false, 250), eim.newPoint(eim.getRandom(-800, 800), 75));
        }
        eim.schedule("boss", eim.getRandom(10000, 20000));
    }
}

function boss1(eim) {
    if (eim.getValue("stage") == 1) {
        eim.dropMessage(6, "A Protective Barrier has appeared, destory it to lower Will's Defenses!");
        eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8880306, 3000, 75, false, false, false, false, 750), eim.newPoint(eim.getRandom(-800, 800), 75));
    }
}

function will(eim) {
    if (eim.getValue("stage") == 1) {
        eim.dropMessage(6, "[Commander Will] Come forth my servant!");
        eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8645043, 2500, 65, false, false, false, false, 250), eim.newPoint(975, 75));
    }
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
    if (mob.getId() == 2600800) {
        eim.setValue("stage", 2);
        eim.victory(exitMap);
    }
    if (eim.getValue("stage") == 1) {
        if (mob.getId() == 8880306) {
            eim.schedule("boss1", eim.getRandom(30000, 60000));
        }
        if (mob.getId() == 8880306) {
            eim.schedule("will", eim.getRandom(10000, 30000));
        }
    }
}

function finish(eim) {
    eim.exitParty(exitMap);
}

function allMonstersDead(eim) {
    eim.victory(exitMap);
}

function cancelSchedule() {
}

function dispose(eim) {
}

function afterSetup(eim) {
}
