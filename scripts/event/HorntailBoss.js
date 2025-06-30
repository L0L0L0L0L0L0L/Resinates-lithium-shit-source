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
var entryMap = 240060200;
var exitMap = 240050000;

var minMapId = 240060200;
var maxMapId = 240060200;

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
    var eim = em.newInstance(player, "HorntailBoss_" + player.getName(), true);
    map = eim.getMapInstance(entryMap);
    map.setInstanced(true);
    //map.setSummons(false);
    
    eim.startEventTimer(eventTime * 60000);
    //map.spawnMonsterOnGroundBelow(eim.getMonster(8810026), eim.newPoint(71, 260));
    //map.changeMusic("Bgm14/HonTale");
    eim.setIntProperty("left", 0);
    eim.setIntProperty("right", 0);
    eim.setIntProperty("finish", 0);
    eim.schedule("left", eim.getRandom(10, 30) * 1000);
    eim.schedule("right", eim.getRandom(10, 30) * 1000);
    return eim;
}

function left(eim) {
    if (eim.getIntProperty("finish") == 0) {
        if (eim.getIntProperty("left") == 0) {
            eim.setIntProperty("left", 1);
            var map = eim.getMapInstance(entryMap);
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoDrops(8810025), eim.newPoint(-575, 260));
            eim.dropMessage(6, "An additional head has appeared!!");
        }
        eim.schedule("left", eim.getRandom(10, 30) * 1000);
    }
}

function right(eim) {
    if (eim.getIntProperty("finish") == 0) {
        if (eim.getIntProperty("right") == 0) {
            eim.setIntProperty("right", 1);
            var map = eim.getMapInstance(entryMap);
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoDrops(8810024), eim.newPoint(750, 260));
            eim.dropMessage(6, "An additional head has appeared!!");
        }
        eim.schedule("right", eim.getRandom(10, 30) * 1000);
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
        eim.unregisterPlayer(player);
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
    if (mob.getId() == 8810001) {
        eim.setIntProperty("left", 0);
    }
    if (mob.getId() == 8810000) {
        eim.setIntProperty("right", 0);
    }
    if (mob.getId() == 8810018) {
        eim.setIntProperty("finish", 1);
        eim.gainPartyStat(0, 5);
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


