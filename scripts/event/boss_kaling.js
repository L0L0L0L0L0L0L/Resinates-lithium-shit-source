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
var entryMap = 410007260;
var exitMap = 410007025;

var minMapId = 410007260;
var maxMapId = 410007260;

var eventTime = 60;     // 45 minutes
var boss = 8880907;

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "battle_" + player.getName(), true);
    map = eim.getMapInstance(entryMap);
    map.setInstanced(true);
    eim.startEventTimer(eventTime * 60000);
    eim.setValue("scale", scale);
    eim.setValue("level", level);
    eim.setValue("finished", 0);
    eim.setValue("flower", 0);
    eim.setValue("max", Math.floor(scale * 0.1));
    var mob1 = eim.getKaoticMonster(boss, level, scale, true, false, false, true, scale * 25);
    eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(mob1, eim.newPoint(eim.getRandom(-600, 2500), eim.getRandom(-800, 100)));
    eim.schedule("start", 5 * 1000);
    return eim;
}

function start(eim) {
    eim.broadcastMapMsg("Watch out for the flowers else boss fight will end!!", 5121104);
    eim.schedule("bombs", 10000);
}

function bombs(eim) {
    if (eim.getValue("finished") == 0) {
        if (eim.getValue("flower") < eim.getValue("max")) {
            eim.setValue("flower", eim.getValue("flower") + 1);
            var mob1 = eim.getKaoticMonster(8880915, eim.getValue("level"), eim.getValue("scale"), false, false, false, true, eim.getValue("scale"));
            eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(mob1, eim.newPoint(eim.getRandom(-600, 2500), eim.getRandom(-800, 100)));
            eim.schedule("bombs", eim.getRandom(10000, 30000));
            eim.broadcastPlayerTopMsg("Flowers have appeared!!");
        } else {
            eim.defeat(exitMap);
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
    if (mob.getId() == 8880907) {
        var scale = eim.getValue("scale") + 25;
        var mob1 = eim.getKaoticMonster(8880912, eim.getValue("level"), scale, true, false, true, true, scale * 50);
        eim.getMapInstance(entryMap).spawnMonsterOnGround(mob1, eim.newPoint(eim.getRandom(-600, 2500), eim.getRandom(-800, 100)), true);
    }
    if (mob.getId() == 8880912) {
        eim.gainPartyItem(3991012, mob.getAttackerSize());
        eim.setValue("finished", 1);
        eim.victory(exitMap);
    }
    if (mob.getId() == 8880915) {
        eim.setValue("flower", eim.getValue("flower") - 1);
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


