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
var entryMap = 75004;
var exitMap = 4000;

var minMapId = 75004;
var maxMapId = 75004;
var eventMapId = 75004;

var eventTime = 60;     // 10 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, tier) {
    var eim = em.newInstance(player, "Kaotic_Event_" + player.getName(), true);
    eim.setValue("exit", 4000);
    var map = eim.getMapInstance(entryMap);
    map.setInstanced(true);
    map.setReturnMapId(eim.getValue("exit"));
    eim.setValue("finished", 0);
    eim.setValue("scale", tier);
    eim.setValue("level", level);
    eim.setValue("kill", 0);
    eim.setValue("multi", player.getVar("multi"));
    eim.schedule("start", 5000);
    return eim;
}

function start(eim) {
    eim.changeMusic("BgmFF8/Legendary_Beast");
    var scale = eim.getValue("scale");
    var level = eim.getValue("level");
    eim.setValue("spawn", 100);
    eim.broadcastPlayerMsg(5, "You have 60 mins to kill as many super dragons as you can. Monsters - Kaotic Tier " + scale + " - Starting Level: " + level);
    eim.broadcastPlayerMsg(5, "@boost has no effect in this dungeon!");
    eim.broadcastPlayerMsg(5, "Elwin Exp rate: " + eim.getValue("multi"));
    eim.startEventTimer(1000 * 60 * 60);
    eim.schedule("waves", 1000);
}

function waves(eim) {
    if (!eim.isDisposed()) {
        var map = eim.getMapInstance(eventMapId);
        var level = eim.eimLevel();
        var lvl = level * 1 + ((level * 0.1));
        var scale = eim.getValue("scale");
        if (map.getSpawnedMonstersOnMap() < eim.getValue("spawn")) {
            var spawncount = 100 - map.getSpawnedMonstersOnMap();
            for (var i = 0; i < spawncount; i++) {
                var mob = eim.getKaoticMonster(9840000, lvl, scale, false, false, false, true);
                mob.setMonsterEventType(5);
                mob.setAggro(true);
                map.spawnMonsterOnGround(mob, eim.newPoint(eim.getRandom(0, 1000), 400));
            }
        }
        eim.schedule("waves", 2000);
    }
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
    eim.exitParty(eim.getValue("exit"));
}

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.exitPlayer(player, eim.getValue("exit"));
}

function playerLeft(eim, player) {
    eim.exitPlayer(player, eim.getValue("exit"));
}

function changedMap(eim, player, mapid) {
    if (mapid < minMapId || mapid > maxMapId) {
        eim.exitPlayer(player, eim.getValue("exit"));
    }
}

function changedLeader(eim, leader) {
    eim.changeEventLeader(leader);
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, eim.getValue("exit"));
}

function playerDisconnected(eim, player) {
    eim.unregisterPlayer(player);
}

function leftParty(eim, player) {
    eim.exitPlayer(player, eim.getValue("exit"));
}

function disbandParty(eim) {
    eim.exitParty(eim.getValue("exit"));
}

function monsterValue(eim, mobId) {
    return 1;
}

function end(eim) {
    eim.exitParty(eim.getValue("exit"));
}

function monsterKilled(mob, eim) {
    if (mob.getKiller() != null) {
        mob.getKiller().gainLevelData(100, eim.getValue("multi"));
    }
}

function mobKilled(mob, eim) {
}

function finish(eim) {
    eim.exitParty(eim.getValue("exit"));
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function dispose(eim) {
}

function afterSetup(eim) {
}
