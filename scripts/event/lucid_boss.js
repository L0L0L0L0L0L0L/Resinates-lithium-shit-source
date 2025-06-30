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
var entryMap = 450004150;
var exitMap = 450003600;

var minMapId = 450004150;
var maxMapId = 450004250;

var eventTime = 60;     // 10 minutes
var monsters = new Array(8240100, 8240101, 8240102);

var lobbyRange = [0, 0];
var x = new Array(715, 163, 324, 1116, 796);
var y = new Array(-495, 555, -860, -620, -200);

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player) {
    var eim = em.newInstance(player, "lucid_boss_" + player.getName(), true);
    eim.getMapInstance(450004150).setInstanced(true);//boss
    eim.getMapInstance(450004250).setInstanced(true);//boss
    //eim.getMapInstance(450004250).toggleconsume(false);
    eim.schedule("start", 10 * 1000);
    eim.setIntProperty("stage", 0);
    eim.setIntProperty("immune", 0);
    return eim;
}

function start(eim) {
    //eim.dropMessage(6, "[Event] All consumables have been disabled!");
    eim.setIntProperty("stage", 1);

    var nMob1 = eim.getKaoticMonster(8880158, 1400, 45, true, false, false, true, 99999, true, true, false, true);
    eim.spawnFakeMonster(450004150, nMob1, eim.newPoint(1000, 0));

    var nMob2 = eim.getKaoticMonster(8880140, 1400, 45, true, false, false, true, 99, true, false, false, true);
    eim.spawnMonsterOnGroundBelow(450004150, nMob2, eim.newPoint(1000, 0));

    var nMob3 = eim.getKaoticMonster(8880150, 1400, 45, true, false, true, true, 999, true, true, false, true);
    eim.spawnMonsterOnGroundBelow(450004250, nMob3, eim.newPoint(605, -50));

    eim.startEventTimer(30 * 60000);
    eim.schedule("boss", 30000);
    eim.schedule("boss1", 1000);
}

function boss(eim) {
    if (eim.getIntProperty("immune") == 0) {
        if (eim.getIntProperty("stage") == 1) {
            eim.setIntProperty("immune", 1);
            eim.getMapInstance(450004150).spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8880166), eim.newPoint(eim.getRandom(100, 1900), 0));
        }
        if (eim.getIntProperty("stage") == 2) {
            eim.setIntProperty("immune", 1);
            var i = eim.getRandom(0, 4);
            eim.getMapInstance(450004250).spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8880166), eim.newPoint(x[i], y[i]));
        }
    }
}

function boss1(eim) {
    if (eim.getIntProperty("stage") == 1) {
        if (eim.getMapInstance(450004150).getSpawnedMonstersOnMap() < 20) {
            eim.getMapInstance(450004150).spawnMonsterOnGround(eim.getMonsterNoAll(8880165), eim.newPoint(eim.getRandom(100, 1900), eim.getRandom(-1150, 25)));
        }
        eim.schedule("boss1", eim.getRandom(500, 1500));
    }
    if (eim.getIntProperty("stage") == 2) {
        if (eim.getMapInstance(450004250).getSpawnedMonstersOnMap() < 20) {
            eim.getMapInstance(450004250).spawnMonsterOnGround(eim.getMonsterNoAll(8880165), eim.newPoint(eim.getRandom(0, 1300), eim.getRandom(-1150, -50)));
        }
        eim.schedule("boss1", eim.getRandom(500, 1500));
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
    if (mob.getId() == 8880166) {
        eim.dropMessage(6, "The Flower has been destoryed!");
        eim.setIntProperty("immune", 0);
        eim.schedule("boss", eim.getRandom(30000, 60000));
    }
    if (mob.getId() == 8880140) {
        eim.warpEventTeam(450004250);
        eim.setIntProperty("stage", 2);
    }
    if (mob.getId() == 8880150) {
        eim.setIntProperty("stage", 3);
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
