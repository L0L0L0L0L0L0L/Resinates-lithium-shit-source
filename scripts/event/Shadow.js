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


var recruitMap = 450009050;
var entryMap = 450009050;
var exitMap = 450009050;

var eventMapId = 4306;

var startMobId = 9305300;
var endMobId = 9305338;

var minPlayers = 3, maxPlayers = 6;
var minLevel = 140, maxLevel = 255;

var eventTime = 60; //30 minutes
var lobbyRange = [0, 8];
var x = new Array(936, 160, -420, 153, 1534, 2550, 3764, 5487, 7247, 6898, 6055, 5017, 4341, 3110, 2416, 1412, 663, 1551, 2352, 3326, 4206, 5228, 5942, 6910);
var y = new Array(476, 129, 129, 819, 819, 819, 820, 816, 818, 456, 456, 466, 466, 469, 469, 476, 129, 142, 142, 141, 141, 145, 145, 148);

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
    var eim = em.newInstance(player, "Shadow_" + player.getName(), true);
    eim.setValue("exit", player.getMapId());
    var map = eim.getMapInstance(eventMapId);
    map.setReturnMapId(eim.getValue("exit"));
    map.removeReactors();
    map.setSpawnCap(999);
    eim.setValue("scale", diff);
    eim.setValue("level", level);
    eim.setValue("stage", 1);
    eim.setValue("multi", diff);
    eim.schedule("start", 10 * 1000);
    eim.createEventTimer(10 * 1000);
    eim.setValue("finished", 0);
    eim.setValue("wave", 1);
    eim.setValue("omen", 5);
    eim.setValue("master_omen", 10);
    eim.setScale(false);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    player.dropMessage(6, "[Boss PQ] Event will begin in 10 seconds.");
}

//event --------------------------------------------------------------------

function start(eim) {
    eim.changeMusic("BgmFF8/Premotion");
    var map = eim.getMapInstance(eventMapId);
    var level = eim.getValue("level");
    var scale = eim.getValue("scale");
    var mob = eim.getKaoticMonster(8230012, eim.getValue("level") + eim.getValue("master_omen"), eim.getValue("scale"), true, false, false, true);
    eim.broadcastPlayerMsg(5, "You have 4 hours to kill as many shadow monsters as you can. Monsters - Kaotic Tier " + scale + " - Starting Level: " + level);
    eim.broadcastPlayerMsg(5, "Each shadow monster killed yields shadow power into you!");
    eim.broadcastPlayerMsg(5, "More shadow mobs you kill stronger they become!");
    mob.setForceBar();
    var random = eim.getRandom(0, 23);
    eim.getMapInstance(eventMapId).spawnMonsterWithEffectBelow(mob, 15, eim.newPoint(x[random], y[random]));
    eim.startEventTimer(eventTime * 240000);
    eim.schedule("bombs", 10);
}

function bombs(eim) {
    if (eim.getValue("finished") == 0) {
        var count = 200 - eim.getMapInstance(eventMapId).getSpawnedMonstersOnMap();
        if (count > 0) {
            while (count > 0) {
                var random = eim.getRandom(0, 23);
                eim.getMapInstance(eventMapId).spawnMonsterOnGround(eim.getKaoticMonster(8230061 + eim.getValue("stage"), eim.getValue("level") + eim.getValue("omen"), eim.getValue("scale"), false, false, false, true, eim.getValue("wave")), eim.newPoint(x[random] + eim.getRandom(-100, 100), y[random]));
                count--;
            }
        }
        eim.schedule("bombs", 2000);
    }
}

function finish(eim) {
    eim.exitParty(eim.getValue("exit"));
}

//timer ending --------------------------------------------------------------------
function scheduledTimeout(eim) {
    eim.exitParty(eim.getValue("exit"));
}

//monsters--------------------------------------------------------------------

function monsterKilled(mob, eim) {
    var map = eim.getMapInstance(eventMapId);
    if (mob.getId() == 8230012) {
        eim.setValue("wave", eim.getValue("wave") + 1);
        eim.setValue("omen", eim.getValue("wave"));
        eim.setValue("master_omen", eim.getValue("multi") * eim.getValue("wave"));
        if (eim.getValue("wave") <= 50) {
            if (eim.getValue("wave") >= 5 && eim.getValue("stage") == 1) {
                eim.setValue("stage", eim.getValue("stage") + 1);
                eim.broadcastPlayerMsg(5, "Omen shadow power grows stronger!!!!");
            }
            if (eim.getValue("wave") >= 10 && eim.getValue("stage") == 2) {
                eim.setValue("stage", eim.getValue("stage") + 1);
                eim.broadcastPlayerMsg(5, "Omen shadow power grows stronger!!!!");
            }
            if (eim.getValue("wave") >= 20 && eim.getValue("stage") == 3) {
                eim.setValue("stage", eim.getValue("stage") + 1);
                eim.broadcastPlayerMsg(5, "Omen shadow power grows stronger!!!!");
            }
            if (eim.getValue("wave") >= 30 && eim.getValue("stage") == 4) {
                eim.setValue("stage", eim.getValue("stage") + 1);
                eim.broadcastPlayerMsg(5, "Omen shadow power grows stronger!!!!");
            }
            if (eim.getValue("wave") >= 40 && eim.getValue("stage") == 5) {
                eim.setValue("stage", eim.getValue("stage") + 1);
                eim.broadcastPlayerMsg(5, "Omen shadow power grows stronger!!!!");
            }
            if (eim.getValue("wave") >= 50 && eim.getValue("stage") == 6) {
                eim.setValue("stage", eim.getValue("stage") + 1);
                eim.broadcastPlayerMsg(5, "Omen shadow power grows stronger!!!!");
            }
        }
        eim.broadcastMapMsg("Wave: " + eim.getValue("wave") + " - Master Omen Power: +" + eim.getValue("master_omen") + " - Omen Power: +" + eim.getValue("omen"), 5120187);
        var mob = eim.getKaoticMonster(8230012, eim.getValue("level") + eim.getValue("master_omen"), eim.getValue("scale") + eim.getValue("stage"), true, false, false, true, eim.getValue("wave"));
        mob.setForceBar();
        var random = eim.getRandom(0, 23);
        eim.getMapInstance(eventMapId).spawnMonsterWithEffectBelow(mob, 15, eim.newPoint(x[random], y[random]));
        eim.gainPowerExp(103, eim.getValue("master_omen"));
    }
    if (mob.getId() >= 8230062 && mob.getId() <= 8230068) {
        eim.gainPowerExp(103, eim.getValue("omen"));
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
    eim.exitPlayer(player, eim.getValue("exit"));
}

function playerDisconnected(eim, player) {
    eim.exitPlayer(player, eim.getValue("exit"));
}

function clear(eim) {
}

function changedMap(eim, player, mapid) {
    if (mapid != eventMapId) {
        eim.exitPlayer(player, eim.getValue("exit"));
    }
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, eim.getValue("exit"));
}

function cancelSchedule() {
}

function dispose() {
}

function end(eim) {
    eim.exitParty(eim.getValue("exit"));
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

