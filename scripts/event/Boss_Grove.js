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


var recruitMap = 863000105;
var entryMap = 863000106;
var exitMap = 863000105;

var eventMapId = 863000106;

var startMobId = 9305300;
var endMobId = 9305339;

var minPlayers = 3, maxPlayers = 6;
var minLevel = 140, maxLevel = 255;

var eventTime = 30; //30 minutes
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

function setup(player) {
    var eim = em.newInstance(player, "Boss_Grove_" + player.getName(), true);
    eim.setExitMap(exitMap);
    var map = eim.getMapInstance(eventMapId);
    map.setReturnMapId(exitMap);
    eim.schedule("start", 5000);
    eim.createEventTimer(5000);
    eim.setIntProperty("finished", 0);
    eim.setIntProperty("wave", 1);
    eim.setScale(false);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    player.dropMessage(6, "[Boss PQ] Event will begin in 5 seconds.");
}

//event --------------------------------------------------------------------

function start(eim) {
    var r = eim.getRandom(1, 4);
    var map = eim.getMapInstance(eventMapId);
    var mob;
    if (r == 1) {
        mob = eim.getKaoticMonster(9601280, 8000, 80, true, true, true, true, 999);
    } else {
        mob = eim.getKaoticMonster(9601282, 7500, 75, true, true, true, true, 99);
    }
    mob.getStats().setExplosiveReward(true);
    map.spawnMonsterWithEffectBelow(mob, 15, eim.newPoint(82, 410));
    eim.startEventTimer(eventTime * 60000);
}

function finish(eim) {
    eim.exitParty(exitMap);
}

//timer ending --------------------------------------------------------------------
function scheduledTimeout(eim) {
    eim.exitParty(exitMap);
}

//monsters--------------------------------------------------------------------

function monsterKilled(mob, eim) {
    if (mob.getId() == 9601280 || mob.getId() == 9601282) {
        eim.setIntProperty("finished", 1);
        eim.gainPartyItem(4310015, 250);
        eim.victory(exitMap);
    }
}

function monsterValue(eim, mobId) {
    return 1;
}

function allMonstersDead(eim) {
}

//player leave --------------------------------------------------------------------

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function playerDisconnected(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function clear(eim) {
}

function changedMap(eim, player, mapid) {
    if (mapid != eventMapId) {
        eim.exitPlayer(player, exitMap);
    }
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, exitMap);
}

function cancelSchedule() {
}

function dispose() {
}

function end(eim) {
    eim.exitParty(exitMap);
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

