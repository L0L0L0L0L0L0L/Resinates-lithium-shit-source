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


var recruitMap = 410007700;
var entryMap = 410007700;
var exitMap = 410007618;

var eventMapId = 410007700;

var startMap = 410007700;
var endMap = 410007760;

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
    var trash = 99;
    var mini = 999;
    var final = 9999;
    eim.getMapInstance(410007710).forceCapRespawn(eim, 8899002, level, tier, false, false, false, false, trash);
    eim.getMapInstance(410007711).forceCapRespawn(eim, 8899002, level, tier, false, false, false, false, trash);
    eim.getMapInstance(410007712).forceCapRespawn(eim, 8899006, level, tier, true, false, true, true, mini);
    eim.getMapInstance(410007720).forceCapRespawn(eim, 8899003, level, tier, false, false, false, false, trash);
    eim.getMapInstance(410007721).forceCapRespawn(eim, 8899003, level, tier, false, false, false, false, trash);
    eim.getMapInstance(410007722).forceCapRespawn(eim, 8899007, level, tier, true, false, true, true, mini);
    eim.getMapInstance(410007730).forceCapRespawn(eim, 8899004, level, tier, false, false, false, false, trash);
    eim.getMapInstance(410007731).forceCapRespawn(eim, 8899004, level, tier, false, false, false, false, trash);
    eim.getMapInstance(410007732).forceCapRespawn(eim, 8899008, level, tier, true, false, true, true, mini);
    eim.getMapInstance(410007740).forceCapRespawn(eim, 8899005, level, tier, false, false, false, false, trash);
    eim.getMapInstance(410007741).forceCapRespawn(eim, 8899005, level, tier, false, false, false, false, trash);
    eim.getMapInstance(410007742).forceCapRespawn(eim, 8899009, level, tier, true, false, true, true, mini);
    var rand_boss = eim.getRandom(tier, tier + 50);
    eim.getMapInstance(410007750).forceCapRandomRespawn(eim, 8899001, level, tier, rand_boss, false, false, false, false, trash);
    eim.getMapInstance(410007751).forceCapRespawn(eim, 8899000, level, rand_boss, true, false, true, true, final);

    eim.setValue("finished", 0);
    eim.setValue("limbo", 0);
    eim.setValue("limbo_clear", 0);
    eim.startEventTimer(60 * 60000);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    player.dropMessage(6, "[LimboPQ] Pick a colored portal and clear that section to proceed.");
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
        eim.setValue("limbo", eim.getValue("limbo") + 1);
        var limbo = "";
        if (mob.getId() == 8899006) {
            limbo = "Purple Limbo";
        }
        if (mob.getId() == 8899007) {
            limbo = "Blue Limbo";
        }
        if (mob.getId() == 8899008) {
            limbo = "Red Limbo";
        }
        if (mob.getId() == 8899009) {
            limbo = "Green Limbo";
        }
        eim.broadcastPlayerMsg(6, "[LIMBO] " + limbo + " has been defeated!");
        if (eim.getValue("limbo") >= 4) {
            eim.setValue("limbo_clear", 1);
            eim.broadcastPlayerMsg(6, "[LIMBO] All Colored Limbos have been defeated.!");
        }
    }
    if (mob.getId() == 8899000) {
        eim.setValue("finished", 1);
        eim.victory(exitMap);
    }
}

function monsterValue(eim, mobId) {
    return 1;
}

function allMonstersDead(eim) {
}

function afterChangedMap(eim, player, mapid) {
    if (mapid >= 410007710 && mapid <= 410007751) {
        if (mapid == 410007750) {
            player.dropMessage(6, "[LimboPQ] Clear all the monsters to proceed.");
            player.dropMessage(6, "[LimboPQ] All players must be here to proceed to next stage.");
        } else {
            player.dropMessage(6, "[LimboPQ] Clear all the monsters to proceed.");
        }

    }
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

