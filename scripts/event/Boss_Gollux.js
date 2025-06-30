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


var recruitMap = 863010000;
var entryMap = 863010700;
var exitMap = 863010000;

var eventMapId = 863010700;

var startMobId = 9305300;
var endMobId = 9305339;

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

function setup(player) {
    var eim = em.newInstance(player, "Boss_Gollux_" + player.getName(), true);
    eim.setExitMap(exitMap);
    eim.setValue("finished", 0);
    eim.setValue("wave", 1);
    eim.setValue("middle", 0);
    eim.setValue("left", 0);
    eim.setValue("right", 0);
    eim.setValue("clear", 0);
    eim.setValue("lives", 10);
    eim.setValue("boss", 0);
    eim.setValue("hits1", 3000);
    eim.setValue("hits2", 3500);
    eim.setValue("hits3", 4000);
    eim.setScale(false);
    eim.getMapInstance(863010100).mapSpawn(8000, 70);
    eim.getMapInstance(863010200).mapSpawn(8000, 70);
    eim.getMapInstance(863010210).mapSpawn(8000, 70);
    eim.getMapInstance(863010220).mapSpawn(8000, 70);
    eim.getMapInstance(863010230).mapSpawn(8000, 70);

    var mob1 = eim.getKaoticMonster(9390612, 8100, 81, true, false, true, true, 2000);
    eim.getMapInstance(863010240).setReturnMapId(863010700);
    eim.getMapInstance(863010240).spawnMonsterOnGroundBelow(mob1, eim.newPoint(11, 89));

    eim.getMapInstance(863010300).mapSpawn(8000, 75);
    eim.getMapInstance(863010310).mapSpawn(8100, 75);
    eim.getMapInstance(863010320).mapSpawn(8200, 75);

    var mob2 = eim.getKaoticMonster(9390611, 8200, 82, true, false, false, true, 250);
    eim.getMapInstance(863010330).setReturnMapId(863010700);
    eim.getMapInstance(863010330).spawnMonsterOnGroundBelow(mob2, eim.newPoint(7, 67));

    eim.getMapInstance(863010400).mapSpawn(8000, 75);
    eim.getMapInstance(863010410).mapSpawn(8100, 75);
    eim.getMapInstance(863010420).mapSpawn(8200, 75);

    var mob3 = eim.getKaoticMonster(9390610, 8200, 82, true, false, false, true, 250);
    eim.getMapInstance(863010430).setReturnMapId(863010700);
    eim.getMapInstance(863010430).spawnMonsterOnGroundBelow(mob3, eim.newPoint(115, 69));

    eim.getMapInstance(863000900).setReturnMapId(863010700);
    var mob4 = eim.getKaoticMonster(9390600, 8300, 83, true, false, false, true, 2500);
    eim.getMapInstance(863000900).spawnMonsterOnGroundBelow(mob4, eim.newPoint(311, 199));
    eim.startEventTimer(eventTime * 60000);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    player.dropMessage(6, "[Boss PQ] Event will begin in 5 seconds.");
}

//event --------------------------------------------------------------------

function finish(eim) {
    eim.exitParty(exitMap);
}

//timer ending --------------------------------------------------------------------
function scheduledTimeout(eim) {
    eim.exitParty(exitMap);
}

//monsters--------------------------------------------------------------------

function monsterKilled(mob, eim) {
    if (mob.getId() == 9390610 || mob.getId() == 9390611 || mob.getId() == 9390612) {
        if (mob.getId() == 9390610) {
            eim.setValue("right", 1);
        }
        if (mob.getId() == 9390611) {
            eim.setValue("left", 1);
        }
        if (mob.getId() == 9390612) {
            eim.setValue("middle", 1);
        }
        eim.getMapInstance(mob.getMap().getId()).showClear();
    }
    if (mob.getId() == 9390600) {
        var mob4 = eim.getKaoticMonster(9390601, 8400, 84, true, false, false, true, 3500);
        eim.getMapInstance(863000900).spawnMonsterOnGroundBelow(mob4, eim.newPoint(311, 199));
    }
    if (mob.getId() == 9390601) {
        var mob4 = eim.getKaoticMonster(9390602, 8500, 85, true, false, true, true, 5000);
        eim.getMapInstance(863000900).spawnMonsterOnGroundBelow(mob4, eim.newPoint(311, 199));
    }
    if (mob.getId() == 9390602) {
        eim.setIntProperty("finished", 1);
        eim.gainPartyItem(4310015, 2500);
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
    if (mapid == exitMap) {
        eim.exitPlayer(player, exitMap);
    }
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    if (eim.getValue("lives") > 0) {
        eim.setValue("lives", eim.getValue("lives") - 1);
        player.changeMap(eim.getMapInstance(eventMapId), 0);
        player.dropMessage(6, "[Gollux] " + eim.getValue("lives") + " lives remaining.");
    } else {
        eim.exitPlayer(player, exitMap);
    }
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

