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


var recruitMap = 400000001;
var entryMap = 400000003;
var exitMap = 400000001;

var eventMapId = 400000003;

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

function setup(player, level, diff) {
    var eim = em.newInstance(player, "Blake_" + player.getName(), true);
    eim.setExitMap(exitMap);
    var map = eim.getMapInstance(eventMapId);
    map.setReturnMapId(exitMap);
    map.setSpawnCap(999);

    for (var i = 0; i < 10; i++) {
        map.setObjectFlag("Boss_Clear_" + i, false);
        map.setObjectFlag("Boss_" + i, false);
    }

    eim.setValue("scale", diff);
    eim.setValue("level", level);
    eim.schedule("start", 5000);
    eim.createEventTimer(5000);
    eim.setValue("finished", 0);
    eim.setValue("wave", 0);
    eim.setValue("boss", 0);
    eim.setScale(false);

    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    player.dropMessage(6, "[Boss PQ] Event will begin in 5 seconds.");
}

//event --------------------------------------------------------------------

function start(eim) {
    var map = eim.getMapInstance(eventMapId);
    map.setObjectFlag("Boss_0", true);
    map.spawnMonsterWithEffectBelow(eim.getKaoticMonster(9410200, 999, 50, true, false, false, true, 999), 15, eim.newPoint(-425, 110));//boss_0 - cannon
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
    if (mob.getId() == 9410210) {
        map.setPQLock(true);
        eim.setValue("finished", 1);
        eim.gainPartyItem(4310015, 250);
        eim.giveAchievement(1600);
        eim.victory(exitMap);
    }
    if (eim.getValue("finished") == 0) {
        if (mob.getId() == 9410200 || mob.getId() == 9410206 || mob.getId() == 9410207 || mob.getId() == 9410208 || mob.getId() == 9410209 || mob.getId() == 9410211 || mob.getId() == 9410213) {
            eim.setValue("wave", eim.getValue("wave") + 1);
            eim.gainPartyItem(4310015, 25);
            if (mob.getId() == 9410200) {
                map.setObjectFlag("Boss_Clear_0", true);
                map.setObjectFlag("Boss_0", false);
                map.setObjectFlag("Boss_3", true);
                eim.broadcastMapMsg("Jack has been defeated", 5120205);
                map.spawnMonsterWithEffectBelow(eim.getKaoticMonster(9410208, 999, 50, true, false, false, true, 999), 15, eim.newPoint(-413, 1740));//boss_3 - merc
            }
            if (mob.getId() == 9410206) {
                map.setObjectFlag("Boss_Clear_1", true);
                map.setObjectFlag("Boss_1", false);
                map.setObjectFlag("Boss_5", true);
                eim.broadcastMapMsg("Lana has been defeated", 5120205);
                map.spawnMonsterWithEffectBelow(eim.getKaoticMonster(9410211, 999, 50, true, false, false, true, 999), 15, eim.newPoint(242, 2560));//boss_5 - lumi
            }
            if (mob.getId() == 9410207) {
                map.setObjectFlag("Boss_Clear_2", true);
                map.setObjectFlag("Boss_2", false);
                map.setObjectFlag("Boss_6", true);
                eim.broadcastMapMsg("Jayna has been defeated", 5120205);
                map.spawnMonsterWithEffectBelow(eim.getKaoticMonster(9410213, 999, 50, true, false, false, true, 999), 15, eim.newPoint(2218, 2560));//boss_6 - DS
            }
            if (mob.getId() == 9410208) {
                map.setObjectFlag("Boss_Clear_3", true);
                map.setObjectFlag("Boss_3", false);
                map.setObjectFlag("Boss_1", true);
                eim.broadcastMapMsg("Amazoness has been defeated", 5120205);
                map.spawnMonsterWithEffectBelow(eim.getKaoticMonster(9410206, 999, 50, true, false, false, true, 999), 15, eim.newPoint(2215, 110));//boss_1 - aran
            }
            if (mob.getId() == 9410209) {
                map.setObjectFlag("Boss_Clear_4", true);
                map.setObjectFlag("Boss_4", false);
                map.setObjectFlag("Boss_2", true);
                eim.broadcastMapMsg("Ocean has been defeated", 5120205);
                map.spawnMonsterWithEffectBelow(eim.getKaoticMonster(9410207, 999, 50, true, false, false, true, 999), 15, eim.newPoint(-413, 925));//boss_2 - evan
            }
            if (mob.getId() == 9410211) {
                map.setObjectFlag("Boss_Clear_5", true);
                map.setObjectFlag("Boss_5", false);
                map.setObjectFlag("Boss_4", true);
                eim.broadcastMapMsg("Asur has been defeated", 5120205);
                map.spawnMonsterWithEffectBelow(eim.getKaoticMonster(9410209, 999, 50, true, false, false, true, 999), 15, eim.newPoint(2222, 1740));//boss_4 - phantom
            }
            if (mob.getId() == 9410213) {
                map.setObjectFlag("Boss_Clear_6", true);
                map.setObjectFlag("Boss_6", false);
                eim.setValue("boss", 1);
                map.setObjectFlag("Blake", false);
                map.spawnMonsterWithEffectBelow(eim.getKaoticMonster(9410210, 999, 50, true, false, true, true, 9999), 15, eim.newPoint(900, 1810));
                eim.broadcastMapMsg("Lucifer has been defeated, Blake has been awakened", 5120205);
            }
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

