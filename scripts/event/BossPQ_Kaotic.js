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
var boss = new Array(9305300, 9305301, 9305302, 9305303, 9305304, 9305305, 9305306, 9305307, 9305308, 9305309, 9305310, 9305311, 9305312, 9305313, 9305314, 9305315, 9305316, 9305317, 9305318, 9305319, 9305320, 9305321, 9305322, 9305323, 9305324, 9305325, 9305326, 9305327, 9305328, 9305329, 9305330, 9305331, 9305332, 9305333, 9305334, 9305335, 9305336, 9305337, 9305338, 9305339, 9833380, 9833381, 9833382, 9305672, 9305674, 9305675, 9305676, 9305677, 8644011, 8642016, 2600800, 8220011, 8220012, 8645009, 8240099, 8800400, 8840000, 8850011, 8860000, 8880000, 8880101, 8880150, 8880302, 8880405, 8930100, 9420620, 9601068, 9421583, 9480235, 9480236, 9480237, 9480238, 9480239, 9390812, 9390822, 9390915);

var minPlayers = 3, maxPlayers = 6;
var minLevel = 140, maxLevel = 255;

var eventTime = 240; //30 minutes
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
    var eim = em.newInstance(player, "Kaotic_BossPQ_" + player.getName(), true);
    eim.setExitMap(exitMap);
    var map = eim.getMapInstance(eventMapId);
    map.setReturnMapId(exitMap);
    map.removeReactors();
    eim.setValue("scale", diff);
    eim.setValue("level", level);
    eim.setValue("ticket", diff);
    eim.setValue("reward", diff * 25);
    eim.setValue("maxlevel", 9999);
    eim.setValue("end", diff > 50 ? 50 : diff);
    eim.schedule("start", 10 * 1000);
    eim.createEventTimer(10 * 1000);
    eim.setValue("finished", 0);
    eim.setValue("wave", 1);
    eim.setValue("cap", diff);
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
    var mobid = eim.getRandom(startMobId, endMobId);
    if (eim.getValue("scale") > 25) {
        mobid = boss[eim.getRandom(0, 75)];
    }
    var mob = eim.getKaoticMonster(mobid, level, eim.getValue("scale"), true, false, false, true, eim.getValue("cap"));
    var wave = eim.getValue("wave");
    eim.dropMessage(6, "[Boss PQ] Boss Battles has begun. Good Luck!!!");
    eim.broadcastMapMsg("[Round: " + wave + "] " + mob.getStats().getName() + " (Power: " + level + ")", 5120187);
    mob.setForceBar();
    var random = eim.getRandom(0, 23);
    eim.getMapInstance(eventMapId).spawnMonsterWithEffectBelow(mob, 15, eim.newPoint(x[random], y[random]));
    eim.startEventTimer(eventTime * 60000);
    eim.schedule("bombs", 10);
}

function bombs(eim) {
    if (eim.getValue("finished") == 0) {
        for (var i = 0; i < 16; i++) {
            var random = eim.getRandom(0, 23);
            eim.getMapInstance(eventMapId).spawnMonsterOnGround(eim.getMonsterNoAll(9601278, 1000, 20, false), eim.newPoint(x[random], y[random]));
        }
        if (eim.getMapInstance(eventMapId).getSpawnedMonstersOnMap() < 40) {
            var level = eim.getValue("level");
            var scale = eim.getValue("scale");
            for (var i = 0; i < 16; i++) {
                var random = eim.getRandom(0, 23);
                eim.getMapInstance(eventMapId).spawnMonsterOnGround(eim.getKaoticMonster(9833385, level, scale, false, false, false, false, eim.getValue("cap")), eim.newPoint(x[random] + eim.getRandom(-50, 50), y[random]));
            }
        }
        eim.schedule("bombs", 5000);
    }
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
    if (eim.isDojoBoss(mob.getId())) {
        if (eim.getValue("wave") >= eim.getValue("end")) {
            if (eim.getValue("finished") == 0) {
                eim.setValue("finished", 1);
                var scale = eim.getValue("scale");
                if (scale == 10) {
                    eim.gainAchievement(240);
                } else if (scale == 15) {
                    eim.gainAchievement(241);
                } else if (scale == 20) {
                    eim.gainAchievement(242);
                } else if (scale == 25) {
                    eim.gainAchievement(243);
                } else if (scale == 30) {
                    eim.gainAchievement(244);
                } else if (scale == 40) {
                    eim.gainAchievement(245);
                }
                eim.gainPartyItem(4420015, (eim.getValue("ticket")));
                eim.gainPartyItem(4310028, (eim.getValue("reward")));
                eim.victory(exitMap);
            }
        } else {
            if (mob.getId() != 9833385) {
                var wave = eim.getValue("wave") + 1;
                eim.setValue("wave", wave);
                var scale = eim.getValue("scale") + eim.getValue("wave");
                var level = eim.getValue("level");
                var mobid = eim.getRandom(startMobId, endMobId);
                eim.setValue("cap", eim.getValue("cap") + 1);
                if (eim.getValue("scale") > 25) {
                    mobid = boss[eim.getRandom(0, 75)];
                }
                var mob = eim.getKaoticMonster(mobid, level, scale, true, false, false, true, wave);
                eim.broadcastMapMsg("[Round: " + wave + "] " + mob.getStats().getName() + " (Power: " + level + ")", 5120187);
                mob.setForceBar();
                var random = eim.getRandom(0, 23);
                eim.getMapInstance(eventMapId).spawnMonsterWithEffectBelow(mob, 15, eim.newPoint(x[random], y[random]));
                eim.gainPowerExp(103, scale * wave * 10);
            }
        }
    }
    if (eim.getValue("finished") == 0) {
        if (mob.getId() == 9833385) {
            eim.setValue("reward", eim.getValue("reward") + 1);
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

