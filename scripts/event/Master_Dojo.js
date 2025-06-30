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

var minMapId = 45010;
var maxMapId = 45020;

var eventMapId = 45010;

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
    var eim = em.newInstance(player, "Master_Dojo_" + player.getName(), true);
    eim.setExitMap(exitMap);
    eim.setValue("scale", diff);
    eim.setValue("cleared", 0);
    eim.setValue("reward", 50 * diff);
    if (diff == 5) {
        eim.setValue("interval", 1);
        eim.setValue("exp", 25);
        eim.setValue("level", 250);
        eim.setValue("levelinc", 5);
    } else if (diff == 10) {
        eim.setValue("interval", 2);
        eim.setValue("exp", 50);
        eim.setValue("level", 500);
        eim.setValue("levelinc", 10);
    } else if (diff == 15) {
        eim.setValue("interval", 3);
        eim.setValue("exp", 100);
        eim.setValue("level", 1000);
        eim.setValue("levelinc", 25);
    } else if (diff == 20) {
        eim.setValue("interval", 4);
        eim.setValue("exp", 250);
        eim.setValue("level", 1500);
        eim.setValue("levelinc", 50);
    } else if (diff == 25) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 1000);
        eim.setValue("level", 2500);
        eim.setValue("levelinc", 100);
    } else if (diff == 30) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 2500);
        eim.setValue("level", 3000);
        eim.setValue("levelinc", 100);
    } else if (diff == 35) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 5000);
        eim.setValue("level", 3500);
        eim.setValue("levelinc", 100);
    } else if (diff == 40) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 10000);
        eim.setValue("level", 4000);
        eim.setValue("levelinc", 100);
    } else if (diff == 45) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 25000);
        eim.setValue("level", 4500);
        eim.setValue("levelinc", 100);
    } else if (diff == 50) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 30000);
        eim.setValue("level", 5000);
        eim.setValue("levelinc", 125);
    } else if (diff == 55) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 40000);
        eim.setValue("level", 5500);
        eim.setValue("levelinc", 150);
    } else if (diff == 60) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 50000);
        eim.setValue("level", 6000);
        eim.setValue("levelinc", 175);
    } else if (diff == 65) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 75000);
        eim.setValue("level", 6500);
        eim.setValue("levelinc", 200);
    } else if (diff >= 70) {
        eim.setValue("interval", 5);
        eim.setValue("exp", 75000 + (diff * 2500));
        eim.setValue("level", (diff * 100));
        eim.setValue("levelinc", 250);
    }

    for (var i = 0; i <= 5; i++) {
        eim.getMapInstance(45010).setObjectFlag("star_" + i, true);
    }
    eim.getMapInstance(45010).setObjectFlag("door", false);
    eim.getMapInstance(45011).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9305672, eim.getValue("level"), diff, true, false, false, true, 999), eim.newPoint(eim.getRandom(-400, 400), -25));
    eim.getMapInstance(45012).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9305674, eim.getValue("level") + (eim.getValue("levelinc") * 2), diff + 1, true, false, false, true, 999), eim.newPoint(eim.getRandom(-400, 400), -25));
    eim.getMapInstance(45013).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9305675, eim.getValue("level") + (eim.getValue("levelinc") * 3), diff + 2, true, false, false, true, 999), eim.newPoint(eim.getRandom(-400, 400), -25));
    eim.getMapInstance(45014).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9305676, eim.getValue("level") + (eim.getValue("levelinc") * 4), diff + 3, true, false, false, true, 999), eim.newPoint(eim.getRandom(-400, 400), -25));
    eim.getMapInstance(45015).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9305677, eim.getValue("level") + (eim.getValue("levelinc") * 4), diff + 4, true, false, false, true, 999), eim.newPoint(eim.getRandom(-400, 400), -25));
    eim.setValue("boss", 9601015);
    eim.getMapInstance(45020).spawnMonsterOnGroundBelow(eim.getKaoticMonster(eim.getValue("boss"), eim.getValue("level") + (eim.getValue("levelinc") * 5), diff + 5, true, false, true, true, 9999), eim.newPoint(0, -25));

    eim.startEventTimer(60 * 60000);
    eim.setValue("finished", 0);
    eim.setValue("wave", 0);
    eim.setValue("kill", 0);
    eim.setScale(false);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    player.dropMessage(6, "[Dojo] Clear all the rooms to unlock Master Door.");
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
    var mapid = mob.getMap().getId() - 45011;
    if (mob.getId() == 9305672 || mob.getId() == 9305673 || mob.getId() == 9305674 || mob.getId() == 9305675 || mob.getId() == 9305676 || mob.getId() == 9305677) {
        eim.setValue("cleared", eim.getValue("cleared") + 1);
        eim.getMapInstance(45010).setObjectFlag("star_" + mapid, false);

        eim.dropMessage(6, "[Dojo] Room: " + (mapid + 1) + " Cleared");
        if (mob.getKiller() != null) {
            eim.gainDojoMobExp((eim.getValue("exp") * mob.getStats().getScale()), mob);
        }
        mob.getMap().showClear();
    }
    if (eim.getValue("cleared") >= 5) {
        eim.getMapInstance(45010).setObjectFlag("door", true);
    }
    if (mob.getId() == eim.getValue("boss")) {
        eim.setValue("finished", 1);
        eim.gainPartyEtc(4310054, eim.getValue("reward") * eim.getPlayerCount());
        eim.gainDojoExp((eim.getValue("exp") + eim.getValue("cleared")) * eim.getValue("interval") * mob.getStats().getScale());
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

