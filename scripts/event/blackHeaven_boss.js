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
var entryMap = 350060160;
var exitMap = 350060300;
var eventMapId = 350060160;

var minMapId = 350060160;
var maxMapId = 350060200;

var eventTime = 240;     // 10 minutes
var maps = new Array(350062000, 350062110, 350062130, 350062150);

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, lobbyid) {
    var eim = em.newInstance(player, "blackHeaven_boss_" + player.getName(), true);
    eim.getMapInstance(350060160).setInstanced(true);//boss
    eim.getMapInstance(350060180).setInstanced(true);//boss
    eim.getMapInstance(350060200).setInstanced(true);//boss
    for (var i = 0; i < maps.length; i++) {
        var map = maps[i];
        eim.getMapInstance(map).setInstanced(true);//pq
        eim.getMapInstance(map).spawnMap();
        eim.getMapInstance(map).setPQLock(true);
    }
    eim.schedule("start", 1000);
    eim.schedule("bombs", 1000);
    eim.setIntProperty("stage", 0);
    eim.setIntProperty("finished", 0);
    eim.setIntProperty("crates", 0);
    eim.setIntProperty("clear", 0);
    eim.startEventTimer(120 * 60000);
    return eim;
}

function start(eim) {
    eim.setIntProperty("clear", 1);
    eim.setIntProperty("stage", 1);
    eim.getMapInstance(350060160).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8240097, 925, 38, true, false, false, true, 95), eim.newPoint(0, -20));
    eim.getMapInstance(350060180).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8240098, 950, 39, true, false, false, true, 100), eim.newPoint(0, -20));
    eim.getMapInstance(350060200).spawnMonsterOnGroundBelow(eim.getKaoticMonster(8240099, 975, 40, true, false, true, true, 105), eim.newPoint(0, -20));
    eim.schedule("boss", 1000);
}

function bombs(eim) {
    if (eim.getIntProperty("finished") < 1) {
        eim.getMapInstance(350062110).spawnMonsterOnGroundBelow(eim.getMonsterNoAll(eim.getRandom(8240200, 8240202)), eim.newPoint(eim.getRandom(-300, 2200), eim.getRandom(-150, 250)));
        eim.getMapInstance(350062130).spawnMonsterOnGroundBelow(eim.getMonsterNoAll(eim.getRandom(8240200, 8240202)), eim.newPoint(eim.getRandom(-300, 2300), eim.getRandom(-350, 250)));
        eim.getMapInstance(350062150).spawnMonsterOnGroundBelow(eim.getMonsterNoAll(eim.getRandom(8240200, 8240202)), eim.newPoint(eim.getRandom(-300, 2500), eim.getRandom(-200, 250)));
        eim.schedule("bombs", 250);
    }
}

function boss(eim) {
    if (eim.getIntProperty("finished") < 1) {
        if (eim.getIntProperty("stage") == 1) {
            var map = eim.getMapInstance(350060160);
            map.spawnMonsterOnGround(eim.getMonsterNoAll(eim.getRandom(8240100, 8240102)), eim.newPoint(-512, -280));
            map.spawnMonsterOnGround(eim.getMonsterNoAll(eim.getRandom(8240100, 8240102)), eim.newPoint(512, -280));
            map.spawnMonsterOnGround(eim.getMonsterNoAll(eim.getRandom(8240100, 8240102)), eim.newPoint(-430, -440));
            map.spawnMonsterOnGround(eim.getMonsterNoAll(eim.getRandom(8240100, 8240102)), eim.newPoint(430, -440));
            map.spawnMonsterOnGround(eim.getMonsterNoAll(eim.getRandom(8240100, 8240102)), eim.newPoint(0, -256));
            eim.schedule("boss", 2500);
        }
        if (eim.getIntProperty("stage") == 2) {
            eim.getMapInstance(350060180).spawnMonsterOnGroundBelow(eim.getMonsterNoAll(8240200), eim.newPoint(eim.getRandom(-600, 600), -20));
            eim.schedule("boss", 250);
        }
    }
}

function boss1(eim) {
    if (eim.getIntProperty("finished") < 1) {
        if (eim.getIntProperty("stage") == 3) {
            eim.getMapInstance(350060200).spawnMonsterOnGroundBelow(eim.getMonsterNoAll(eim.getRandom(8240200, 8240202)), eim.newPoint(eim.getRandom(-600, 600), -20));
            eim.schedule("boss1", eim.getRandom(250, 500));
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
    if (mapid == 350060300) {
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
    if (mob.getId() == 8240099) {
        eim.setIntProperty("stage", 4);
        eim.setIntProperty("finished", 1);
        eim.gainPartyStat(7, 10);
        eim.victory(exitMap);
    } else if (mob.getId() == 8240098) {
        eim.warpEventTeam(350060200);
        eim.setIntProperty("stage", 3);
        eim.schedule("boss1", 1000);
    } else if (mob.getId() == 8240097) {
        eim.warpEventTeam(350060180);
        eim.setIntProperty("stage", 2);
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
