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


var recruitMap = 925020001;
var entryMap = 925020001;
var exitMap = 925020001;

var minMapId = 45000;
var maxMapId = 45150;

var eventMapId = 45000;

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
    var eim = em.newInstance(player, "Dojo_" + player.getName(), true);
    eim.setExitMap(exitMap);
    eim.setValue("scale", diff);
    eim.setValue("cleared", 0);
    eim.setValue("reward", 250 * diff);
    var max = 0;
    if (diff == 5) {
        eim.setValue("interval", 1);
        eim.setValue("exp", 10);
        max = 500;
    } else if (diff == 10) {
        eim.setValue("interval", 2);
        eim.setValue("exp", 25);
        max = 1000;
    } else if (diff == 15) {
        eim.setValue("interval", 3);
        eim.setValue("exp", 50);
        max = 1500;
    } else if (diff == 20) {
        eim.setValue("interval", 4);
        eim.setValue("exp", 100);
        max = 2500;
    } else if (diff == 25) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 250);
        max = 5000;
    } else if (diff == 30) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 1000);
        max = 5000;
    } else if (diff == 35) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 5000);
        max = 5000;
    } else if (diff == 40) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 10000);
        max = 5000;
    } else if (diff == 45) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 25000);
        max = 5000;
    } else if (diff == 50) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 50000);
        max = 5000;
    } else if (diff == 55) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 100000);
        max = 5000;
    } else if (diff == 60) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 250000);
        max = 5000;
    } else if (diff == 65) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 500000);
        max = 5000;
    } else if (diff >= 70) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 1000000);
        max = 5000;
    }
    eim.setValue("level", level > max ? max : level);
    for (var i = 0; i <= 38; i++) {
        eim.getMapInstance(45000).setObjectFlag("star_" + i, false);
        var map = eim.getMapInstance(45101 + i);
        var lvl = level + (i * eim.getValue("interval"));
        if (lvl > max) {
            lvl = max;
        }
        map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9305300 + i, lvl, diff, true, false, false, true, 10), eim.newPoint(0, -80));
    }
    eim.getMapInstance(45000).setObjectFlag("fire", false);
    eim.setValue("hidden", 0);
    eim.setValue("boss", 9833380 + eim.getRandom(0, 2));
    eim.getMapInstance(45001).spawnMonsterOnGroundBelow(eim.getKaoticMonster(eim.getValue("boss"), level, diff + 5, true, false, true, true, 9999), eim.newPoint(400, -20));
    eim.startEventTimer(30 * 60000);
    eim.setValue("finished", 0);
    eim.setValue("wave", 0);
    eim.setScale(false);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    player.dropMessage(6, "[Dojo] Clear all the rooms to unlock Master Door.");
    player.dropMessage(6, "[Dojo] Reward Tickets Enabled.");
}

//event --------------------------------------------------------------------

function start(eim) {

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
    var mapid = mob.getMap().getId() - 45101;
    if (mob.getId() >= startMobId && mob.getId() <= endMobId) {
        eim.setValue("cleared", eim.getValue("cleared") + 1);
        eim.getMapInstance(45000).setObjectFlag("star_" + mapid, true);
        eim.dropMessage(6, "[Dojo] Room: " + (mapid + 1) + " Cleared");
        if (mob.getKiller() != null) {
            eim.gainDojoMobExp((eim.getValue("exp") * eim.getValue("scale")) + eim.getValue("cleared"), mob);
        }
        mob.getMap().showClear();
        if (eim.getValue("cleared") >= 39) {
            eim.dropMessage(6, "[Dojo] All Rooms have been cleared! Final Boss is now Open!");
            if (eim.getValue("hidden") < 1) {
                if (eim.getRandom(0, 9) == 0) {
                    eim.dropMessage(6, "[Dojo] Hidden Room has been unlocked!");
                    eim.getMapInstance(45140).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9300269, eim.getValue("level") + (40 * eim.getValue("interval")), eim.getValue("scale") + 5, true, false, false, true), eim.newPoint(0, -25));
                    eim.getMapInstance(45140).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9305679, eim.getValue("level") + (40 * eim.getValue("interval")), eim.getValue("scale") + 3, false, false, false, true), eim.newPoint(eim.getRandom(-400, 400), -25));
                    eim.getMapInstance(45140).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9305679, eim.getValue("level") + (40 * eim.getValue("interval")), eim.getValue("scale") + 3, false, false, false, true), eim.newPoint(eim.getRandom(-400, 400), -25));
                    eim.getMapInstance(45140).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9305679, eim.getValue("level") + (40 * eim.getValue("interval")), eim.getValue("scale") + 3, false, false, false, true), eim.newPoint(eim.getRandom(-400, 400), -25));
                    eim.getMapInstance(45140).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9305679, eim.getValue("level") + (40 * eim.getValue("interval")), eim.getValue("scale") + 3, false, false, false, true), eim.newPoint(eim.getRandom(-400, 400), -25));
                    eim.getMapInstance(45140).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9305679, eim.getValue("level") + (40 * eim.getValue("interval")), eim.getValue("scale") + 3, false, false, false, true), eim.newPoint(eim.getRandom(-400, 400), -25));
                    eim.getMapInstance(45000).setObjectFlag("fire", true);
                    eim.setValue("hidden", 1);
                }
            }
        }
    }

    if (mob.getId() == 9305679) {
        if (mob.getKiller() != null) {
            mob.getKiller().gainDojoExp(((eim.getValue("exp") * eim.getValue("scale")) + eim.getValue("cleared")) * 5);
        }
    }
    if (mob.getId() == 9300269) {
        if (mob.getKiller() != null) {
            mob.getKiller().gainDojoExp(((eim.getValue("exp") * eim.getValue("scale")) + eim.getValue("cleared")) * 10);
        }
    }
    if (mob.getId() == eim.getValue("boss")) {
        eim.setValue("finished", 1);
        var tier = eim.getValue("scale");
        if (tier == 5) {
            eim.gainAchievement(330);
        } else if (tier == 10) {
            eim.gainAchievement(331);
        } else if (tier == 15) {
            eim.gainAchievement(332);
        } else if (tier == 20) {
            eim.gainAchievement(333);
        } else if (tier == 25) {
            eim.gainAchievement(334);
        } else if (tier == 30) {
            eim.gainAchievement(335);
        } else if (tier == 35) {
            eim.gainAchievement(336);
        } else if (tier == 40) {
            eim.gainAchievement(337);
        } else if (tier == 45) {
            eim.gainAchievement(338);
        } else if (tier == 50) {
            eim.gainAchievement(339);
        } else if (tier == 55) {
            eim.gainAchievement(340);
        } else if (tier == 60) {
            eim.gainAchievement(341);
        } else if (tier == 65) {
            eim.gainAchievement(342);
        } else if (tier == 70) {
            eim.gainAchievement(343);
        }
        eim.gainPartyEtc(4310054, eim.getValue("reward") * eim.getPlayerCount());
        eim.gainDojoExp((eim.getValue("exp") + eim.getValue("cleared")) * eim.getValue("interval") * eim.getValue("scale") * eim.getPlayerCount());
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
    if (mapid < minMapId || mapid > maxMapId) {
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

