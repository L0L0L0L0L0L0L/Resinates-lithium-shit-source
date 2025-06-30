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


var recruitMap = 970030000;
var entryMap = 970030000;
var exitMap = 970030000;

var eventMapId = 78001;

var startMobId = 9305300;
var endMobId = 9305338;

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

function setup(player, level, diff) {
    var eim = em.newInstance(player, "BossPQ_" + player.getName(), true);
    eim.setExitMap(exitMap);
    var map = eim.getMapInstance(entryMap);
    map.setReturnMapId(exitMap);
    eim.setValue("scale", diff);
    eim.setValue("level", level);
    if (diff == 5) {
        eim.setValue("reward", 10);
        eim.setValue("maxlevel", 200);
        eim.setValue("cap", 1);
        eim.setValue("lvl", 1);
    } else if (diff == 6) {
        eim.setValue("reward", 20);
        eim.setValue("maxlevel", 300);
        eim.setValue("cap", 2);
        eim.setValue("lvl", 2);
    } else if (diff == 7) {
        eim.setValue("reward", 30);
        eim.setValue("maxlevel", 400);
        eim.setValue("cap", 4);
        eim.setValue("lvl", 3);
    } else if (diff == 8) {
        eim.setValue("reward", 40);
        eim.setValue("maxlevel", 500);
        eim.setValue("cap", 5);
        eim.setValue("lvl", 4);
    } else if (diff == 10) {
        eim.setValue("reward", 50);
        eim.setValue("maxlevel", 750);
        eim.setValue("cap", 10);
        eim.setValue("lvl", 5);
    }
    eim.schedule("start", 10 * 1000);
    eim.createEventTimer(10 * 1000);
    eim.setValue("finished", 0);
    eim.setValue("wave", 1);
    eim.setScale(false);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    player.dropMessage(6, "[Boss PQ] Event will begin in 10 seconds.");
}

//event --------------------------------------------------------------------

function start(eim) {
    eim.changeMusic("BgmCustom/EDM");
    var map = eim.getMapInstance(eventMapId);
    var level = eim.getValue("level");
    var scale = eim.getValue("scale");
    var mob = eim.getKaoticMonster(startMobId, level, scale, true, false, false, true, eim.getValue("cap"));
    var wave = eim.getValue("wave");
    eim.dropMessage(6, "[Boss PQ] Boss Battles has begun. Good Luck!!!");
    eim.broadcastMapMsg("[Round: " + wave + "] " + mob.getStats().getName() + " (Power: " + level + ")", 5120187);
    mob.setForceBar();
    map.spawnMonsterWithEffectBelow(mob, 15, eim.newPoint(eim.getRandom(-380, 1300), -10));
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
    var map = eim.getMapInstance(eventMapId);
    if (mob.getId() == endMobId) {
        eim.setValue("finished", 1);
        var count = eim.getValue("reward");
        var scale = eim.getValue("scale");
        var ticket = 0;
        if (scale == 5) {
            eim.gainAchievement(88);
            ticket = 5;
        } else if (scale == 6) {
            eim.gainAchievement(89);
            ticket = 10;
        } else if (scale == 7) {
            eim.gainAchievement(90);
            ticket = 15;
        } else if (scale == 8) {
            eim.gainAchievement(91);
            ticket = 20;
        } else if (scale == 10) {
            eim.gainAchievement(92);
            ticket = 25;
        }
        eim.gainPartyItem(4420015, ticket);
        eim.gainPartyItem(4310028, count);
        eim.gainPartyEquip(1143033, scale, scale);
        eim.victory(exitMap);
    } else {
        if (mob.getId() >= startMobId && mob.getId() < endMobId) {
            var wave = eim.getValue("wave") + 1;
            eim.setValue("wave", wave);
            var scale = eim.getValue("scale");
            eim.setValue("level", eim.getValue("level") + eim.getValue("lvl"));
            var level = eim.getValue("level");
            var x = eim.getRandom(-380, 1300);
            var mob = eim.getKaoticMonster(mob.getId() + 1, level, scale, true, false, false, true, eim.getValue("cap") * wave);
            eim.broadcastMapMsg("[Round: " + wave + "] " + mob.getStats().getName() + " (Power: " + level + ")", 5120187);
            mob.setForceBar();
            eim.getMapInstance(eventMapId).spawnMonsterWithEffectBelow(mob, 15, eim.newPoint(x, -10));
            eim.gainPowerExp(103, scale * wave);
        }
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

