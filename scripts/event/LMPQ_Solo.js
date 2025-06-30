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
 * @Author Ronan
 * 3rd Job Event - Magician
 **/


var recruitMap = 52000;
var entryMap = 52000;
var exitMap = 410007606;

var eventMapId = 52000;

var startMap = 52000;
var endMap = 52000;

var minPlayers = 3, maxPlayers = 6;
var minLevel = 140, maxLevel = 255;

var eventTime = 60; //30 minutes
var lobbyRange = [0, 8];

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
    return eligible;
}

//setup --------------------------------------------------------------------

function setLobbyRange() {
    return lobbyRange;
}

function init() {
}

function setup(player, level, tier) {
    var eim = em.newInstance(player, "LimboPQ_" + player.getName(), true);
    eim.setExitMap(player.getMapId());
    //forceCapRespawn(EventInstanceManager eim, int id, int level, int scale, boolean bar, boolean link, boolean drops, boolean script, long fixed)
    //trash
    var white = eim.getRandom(1, 10);
    var color = eim.getRandom(0, 3);
    var rand_trash = 8899002 + color;
    var rand_boss = 8899006 + color;
    var hits = 99;
    var bhits = 999;
    eim.setValue("boss", rand_boss);
    eim.setValue("level", level);
    eim.setValue("tier", tier);
    eim.setValue("hits", hits);
    eim.setValue("bhits", bhits);

    eim.getMapInstance(52000).forceCapRespawn(eim, rand_trash, level, tier, false, false, false, false, hits);

    eim.setValue("finished", 0);
    eim.setValue("limbo", 0);
    eim.setValue("limbo_clear", 0);
    eim.startEventTimer(10 * 60000);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
}

function start(eim) {
}

function balls(eim) {
}

//event --------------------------------------------------------------------

function finish(eim) {
    eim.exitParty(eim.getExitMap());
}

//timer ending --------------------------------------------------------------------
function scheduledTimeout(eim) {
    eim.setValue("finished", 1);
    eim.exitParty(eim.getExitMap());
}

//monsters--------------------------------------------------------------------

function monsterKilled(mob, eim) {
    if (mob.getId() == 8899006 || mob.getId() == 8899007 || mob.getId() == 8899008 || mob.getId() == 8899009) {
        eim.setValue("finished", 1);
        eim.victory(exitMap);
    }
}

function monsterValue(eim, mobId) {
    return 1;
}

function allMonstersKilled(eim) {
    if (eim.getValue("limbo_clear") == 0) {
        eim.setValue("limbo_clear", 1);
        eim.broadcastMapMsg("[LimboPQ] Kaotic Limbo has appeared", 5120205);
        var map = eim.getMapInstance(eventMapId);
        map.spawnMonsterWithEffectBelow(eim.getKaoticMonster(eim.getValue("boss"), eim.getValue("level"), eim.getValue("tier"), true, false, true, true, eim.getValue("bhits"), true, true), 15, map.getRandomMonsterSpawnPointPos());//boss_0 - cannon
    }
}

function allMonstersDead(eim) {
}

function afterChangedMap(eim, player, mapid) {
    player.dropMessage(6, "[LimboPQ] Clear all the monsters to summon Limbo.");
}

//player leave --------------------------------------------------------------------

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.exitPlayer(player, eim.getExitMap());
}

function playerDisconnected(eim, player) {
    eim.exitPlayer(player, eim.getExitMap());
}

function clear(eim) {
}

function changedMap(eim, player, mapid) {
    if (mapid < startMap || mapid > endMap) {
        eim.exitPlayer(player, eim.getExitMap());
    }
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, eim.getExitMap());
}

function cancelSchedule() {
}

function dispose() {
}

function end(eim) {
    eim.exitParty(eim.getExitMap());
}


// ---------- FILLER FUNCTIONS ----------

function disbandParty(eim, player) {
}

function afterSetup(eim) {
}

function changedLeader(eim, leader) {
}

function leftParty(eim, player) {
}

function clearPQ(eim) {
}

