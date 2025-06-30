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
var entryMap = 270050100;
var exitMap = 270050300;
var recruitMap = 270050000;
var clearMap = 270050300;

var eventMapId = 270050100;
var minMapId = 270050100;
var maxMapId = 270050200;

var eventTime = 60;     // 140 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, diff) {
    var eim = em.newInstance(player, "Kaotic_BlackBean_" + player.getName(), true);
    var map = eim.getMapInstance(eventMapId);
    map.setInstanced(true);
    eim.setValue("finished", 0);
    eim.setValue("kills", 0);
    eim.schedule("start", 10000);
    return eim;
}

function start(eim) {
    eim.setValue("level", 9999);
    eim.changeMusic("BgmCustom/TheLegendaryBeast");
    var map = eim.getMapInstance(eventMapId);
    eim.broadcastMapMsg("R u Deaady to PiE!!???", 5120173);
    map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(8820002, eim.getRandom(2000, 3000), eim.getRandom(50, 60), false, false, false, true, 999), eim.newPoint(5, -50));
    map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(8820003, eim.getRandom(2000, 3000), eim.getRandom(50, 60), false, false, false, true, 999), eim.newPoint(5, -50));
    map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(8820004, eim.getRandom(2000, 3000), eim.getRandom(50, 60), false, false, false, true, 999), eim.newPoint(5, -50));
    map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(8820005, eim.getRandom(2000, 3000), eim.getRandom(50, 60), false, false, false, true, 999), eim.newPoint(5, -50));
    map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(8820006, eim.getRandom(2000, 3000), eim.getRandom(50, 60), false, false, false, true, 999), eim.newPoint(5, -50));
    map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9420620, 3000, 75, true, false, true, true, 9999), eim.newPoint(0, -50));
    eim.startEventTimer(eventTime * 60000);
    eim.schedule("bean", 10000);
}

function bean(eim) {
    var map = eim.getMapInstance(eventMapId);
    if (eim.getValue("finished") < 1) {
        if (eim.getMapInstance(eventMapId).getSpawnedMonstersOnMap() < 40) {
            for (var i = 1; i <= 20; i++) {
                map.spawnMonsterOnGround(eim.getKaoticMonster(9430027, eim.getRandom(2000, 3000), eim.getRandom(50, 60), false, false, false, false, 99), eim.newPoint(eim.getRandom(-800, 800), eim.getRandom(-400, -40)));
            }
        }
        eim.schedule("bean", 20000);
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
    eim.exitParty(exitMap);
}

function monsterKilled(mob, eim) {
    var map = eim.getMapInstance(eventMapId);
    if (mob.getId() == 9420620) {
        eim.setValue("finished", 1);
        var rate = Math.floor(Math.pow(mob.getBonus(), 2.5));
        eim.gainPartyItem(4032906, rate);
        eim.victory(exitMap);
    }
    if (eim.getValue("finished") < 1) {
        if (mob.getId() >= 8820002 && mob.getId() <= 8820006) {
            var monster = eim.getKaoticMonster(mob.getId(), eim.getRandom(2000, 3000), eim.getRandom(50, 60), false, false, false, true, 999);
            map.spawnMonsterOnGroundBelow(monster, mob.getPosition());
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
