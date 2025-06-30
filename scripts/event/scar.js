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
var entryMap = 551030200;
var exitMap = 551030100;

var minMapId = 551030200;
var maxMapId = 551030200;

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
    var eim = em.newInstance(player, "scar_" + player.getName(), true);
    map = eim.getMapInstance(entryMap);
    map.setInstanced(true);
    //map.setSummons(false);
    eim.startEventTimer(eventTime * 60000);
    var boss = eim.getRandom(0, 1);
    if (boss == 0) {
        eim.setValue("boss", 0);
        var nMob = eim.getKaoticMonster(9420547, 100, 5, true, false, false, true, 99, true, false, false, true);
        eim.spawnMonsterOnGroundBelow(entryMap, nMob, eim.newPoint(400, 160));
    } else {
        eim.setValue("boss", 1);
        var nMob = eim.getKaoticMonster(9420542, 100, 5, true, false, false, true, 99, true, false, false, true);
        eim.spawnMonsterOnGroundBelow(entryMap, nMob, eim.newPoint(400, 160));
    }
    return eim;
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
    eim.unregisterPlayer(player);
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
    if (mob.getId() == 9420547) {
        var nMob = eim.getKaoticMonster(9420548, 100, 6, true, false, false, true, 99, true, false, false, true);
        eim.spawnMonsterOnGroundBelow(entryMap, nMob, mob.getPosition());
    }
    if (mob.getId() == 9420548) {
        var nMob = eim.getKaoticMonster(9420549, 100, 7, true, false, true, true, 999, true, true, false, true);
        eim.spawnMonsterOnGroundBelow(entryMap, nMob, mob.getPosition());
    }
    if (mob.getId() == 9420544) {
        var nMob = eim.getKaoticMonster(9420543, 100, 6, true, false, false, true, 99, true, false, false, true);
        eim.spawnMonsterOnGroundBelow(entryMap, nMob, mob.getPosition());
    }
    if (mob.getId() == 9420545) {
        var nMob = eim.getKaoticMonster(9420544, 100, 7, true, false, true, true, 999, true, true, false, true);
        eim.spawnMonsterOnGroundBelow(entryMap, nMob, mob.getPosition());
    }
    if (mob.getId() == 9420549 || mob.getId() == 9420544) {
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


