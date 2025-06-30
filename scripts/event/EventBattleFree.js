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
 * @event: Vs Dunas
 */

var isPq = true;
var minPlayers = 2, maxPlayers = 6;
var minLevel = 150, maxLevel = 255;

var entryMap = 79100;
var exitMap = 101000000;

var minMapId = 79100;
var maxMapId = 79100;
var eventMapId = 79100;

var tdBossId = 8220010;

var eventTime = 240;     // 10 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, scale) {
    var eim = em.newInstance(player, "EventBattleFree_" + player.getName(), true);
    eim.getMapInstance(eventMapId).setInstanced(true);
    eim.setExitMap(exitMap);
    var map = eim.getMapInstance(entryMap);
    map.setReturnMapId(exitMap);
    eim.schedule("start", 10 * 1000);
    eim.createEventTimer(10 * 1000);
    eim.setIntProperty("finish", 0);
    eim.setIntProperty("mode", scale);
    eim.setIntProperty("scale", 1);
    eim.setIntProperty("level", 1);
    return eim;
}

function start(eim) {
    eim.setIntProperty("level", eim.eimLevel());
    var map = eim.getMapInstance(eventMapId);
    var level = Math.floor(1 + (100 / eim.getIntProperty("level")));
    eim.setIntProperty("scale", level);
    var mode = eim.getIntProperty("mode");
    eim.broadcastPlayerMsg(5, "You have 10 minutes to kill as many monsters as you can. Monsters Level: " + level + " - Tier " + eim.getIntProperty("scale"));
    eim.startEventTimer(600000);
    eim.schedule("waves", 1000);
}

function waves(eim) {
    if (!eim.isDisposed()) {
        var map = eim.getMapInstance(eventMapId);
        if (map.getSpawnedMonstersOnMap() < 100) {
            var level = eim.getIntProperty("level");
            var scale = eim.getIntProperty("scale");
            var mode = eim.getIntProperty("mode");
            var spawncount = 100 - map.getSpawnedMonstersOnMap();
            var mobId = 0;
            for (var i = 0; i < spawncount; i++) {
                if (mode == 0) {
                    mobId = 9400920 + eim.getRandom(0, 3);
                } else if (mode == 1) {
                    mobId = 9300700 + eim.getRandom(0, 7);
                } else if (mode == 2) {
                    mobId = 9303104 + eim.getRandom(0, 4);
                } else if (mode == 3) {
                    mobId = 9001039 + eim.getRandom(0, 3);
                } else if (mode == 4) {
                    mobId = 9840000;
                } else if (mode == 5) {
                    mobId = 9302021;
                }
                var mob = eim.getMonster(mobId, level, scale);
                mob.setMonsterEventType(2);
                map.spawnMonsterOnGroundBelow(mob, eim.newPoint(eim.getRandom(0, 1400), -25));
            }
        }
        eim.schedule("waves", 4000);
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
    if (mapid != eventMapId) {
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
