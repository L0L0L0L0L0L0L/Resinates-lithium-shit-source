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
var entryMap = 450007250;
var exitMap = 450007240;

var minMapId = 450007250;
var maxMapId = 450007250;

var eventTime = 240;     // 10 minutes
var monsters = new Array(8240100, 8240101, 8240102);

var lobbyRange = [0, 0];
var x = new Array(716, 163, 324, 1116, 727, 796);
var y = new Array(-490, 550, -855, -619, -490, -194);

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, lobbyid) {
    var eim = em.newInstance(player, "will_boss_" + player.getName(), true);
    eim.getMapInstance(450007250).setInstanced(true);//boss
    eim.schedule("start", 10 * 1000);
    eim.setIntProperty("stage", 0);
    eim.setIntProperty("immune", 0);
    eim.setIntProperty("finished", 0);
    return eim;
}

function start(eim) {
    eim.setIntProperty("stage", 1);

    var nMob1 = eim.getKaoticMonster(8880300, 1600, 46, true, false, false, true, 99, true, true, false, true);
    eim.spawnMonsterOnGroundBelow(entryMap, nMob1, eim.newPoint(0, 240));

    eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8880305), eim.newPoint(eim.getRandom(-800, 800), 240));
    eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8880305), eim.newPoint(eim.getRandom(-800, 800), 240));
    eim.startEventTimer(60 * 60000);
    eim.schedule("boss", 10000);
    eim.schedule("boss1", 30000);
}

function boss(eim) {
    if (eim.getIntProperty("finished") == 0) {
        if (eim.getIntProperty("stage") == 1) {
            if (eim.getMapInstance(450007250).getSpawnedMonstersOnMap() < 10) {
                eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8880305), eim.newPoint(eim.getRandom(-800, 800), 240));
            }
            eim.schedule("boss", eim.getRandom(10000, 20000));
        }
    }
}

function boss1(eim) {
    if (eim.getIntProperty("finished") == 0) {
        if (eim.getIntProperty("immune") == 0) {
            if (eim.getIntProperty("stage") == 1) {
                eim.setIntProperty("immune", 1);
                eim.dropMessage(6, "A Protective Barrier has appeared, destory it to lower Will's Defenses!");
                eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8880306), eim.newPoint(eim.getRandom(-800, 800), 240));
            }
        }
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
    if (mob.getId() == 8880300) {
        var nMob1 = eim.getKaoticMonster(8880301, 1600, 48, true, false, false, true, 99, true, true, false, true);
        eim.spawnMonsterOnGroundBelow(entryMap, nMob1, mob.getPosition());
    }
    if (mob.getId() == 8880301) {
        var nMob2 = eim.getKaoticMonster(8880302, 1600, 50, true, false, true, true, 999, true, true, false, true);
        eim.spawnMonsterOnGroundBelow(entryMap, nMob2, mob.getPosition());
    }
    if (mob.getId() == 8880302) {
        eim.setIntProperty("stage", 2);
        eim.setIntProperty("finished", 1);
        eim.victory(exitMap);
    }
    if (eim.getIntProperty("finished") == 0) {
        if (mob.getId() == 8880306) {
            eim.dropMessage(6, "The Barrier has been destoryed!");
            eim.setIntProperty("immune", 0);
            eim.schedule("boss1", eim.getRandom(30000, 60000));
        }
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
