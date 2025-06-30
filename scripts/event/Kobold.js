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
 * @author: Ronan
 * @event: Vs Bergamot
 */


var isPq = true;
var minPlayers = 2, maxPlayers = 6;
var minLevel = 150, maxLevel = 255;
var entryMap = 866033000;
var exitMap = 866000150;
var eventMapId = 866033000;

var minMapId = 866033000;
var maxMapId = 866033000;
var boss = 8840000;

var eventTime = 30;     // 10 minutes

var lobbyRange = [0, 0];
var weak = new Array(9390943, 9390940, 9390941, 9390914, 9390933, 9390942);

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "Kobold_" + player.getName(), true);
    eim.getMapInstance(entryMap).setInstanced(true);//boss
    eim.getMapInstance(entryMap).setBoosted(player.getVar("boost"));//boss
    eim.setValue("scale", scale);
    eim.setValue("level", level);
    eim.setValue("boss", 0);
    eim.setValue("finished", 0);
    eim.schedule("start", 10 * 1000);
    return eim;
}

function start(eim) {
    var nMob3 = eim.getKaoticMonster(9390915, eim.getValue("level"), eim.getValue("scale"), true, false, true, true, 999, true, true, false, true);
    eim.spawnMonsterOnGroundBelow(eventMapId, nMob3, eim.newPoint(350, 45));
    eim.startEventTimer(30 * 60000);
    eim.schedule("bombs", 10);
    eim.schedule("randomboss", 10000);
}

function bombs(eim) {
    if (eim.getValue("finished") == 0) {
        for (var i = 0; i < 8; i++) {
            eim.getMapInstance(entryMap).spawnMonsterOnGround(eim.getMonsterNoAll(9601278, 1000, 20, false), eim.newPoint(eim.getRandom(-400, 800), eim.getRandom(-500, 50)));
        }
        eim.schedule("bombs", 5000);
    }
}

function randomboss(eim) {
    if (eim.getValue("finished") == 0) {
        if (eim.getValue("boss") < 16) {
            eim.setValue("boss", eim.getValue("boss") + 4);
            eim.dropMessage(6, "[King Kobold] Come forth my servants and slay thee pests!");
            eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(weak[eim.getRandom(0, 5)], eim.getValue("level") - eim.getRandom(250, 500), eim.getValue("scale") - eim.getRandom(5, 10), false, false, false, true), eim.newPoint(eim.getRandom(-50, 750), 45));
            eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(weak[eim.getRandom(0, 5)], eim.getValue("level") - eim.getRandom(250, 500), eim.getValue("scale") - eim.getRandom(5, 10), false, false, false, true), eim.newPoint(eim.getRandom(-50, 750), 45));
            eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(weak[eim.getRandom(0, 5)], eim.getValue("level") - eim.getRandom(250, 500), eim.getValue("scale") - eim.getRandom(5, 10), false, false, false, true), eim.newPoint(eim.getRandom(-50, 750), 45));
            eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(weak[eim.getRandom(0, 5)], eim.getValue("level") - eim.getRandom(250, 500), eim.getValue("scale") - eim.getRandom(5, 10), false, false, false, true), eim.newPoint(eim.getRandom(-50, 750), 45));
        }
        eim.schedule("randomboss", 30000);
    }
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
    player.dropColorMessage(6, "Event Starting soon....");
}

function scheduledTimeout(eim) {
    eim.exitParty(exitMap);
}

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function playerLeft(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function changedMap(eim, player, mapid) {
    if (mapid < minMapId || mapid > maxMapId) {
        eim.exitPlayer(player, exitMap);
    }
}

function changedLeader(eim, leader) {
    eim.changeEventLeader(leader);
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, exitMap);
}

function playerDisconnected(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function leftParty(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function disbandParty(eim) {
    eim.exitParty(exitMap);
}

function monsterValue(eim, mobId) {
    return 1;
}

function end(eim) {
    eim.exitParty(exitMap);
}

function monsterKilled(mob, eim) {
    if (mob.getId() == 9390915) {
        eim.dropMessage(6, "[King Kobold] I will be back!!!");
        eim.setValue("finished", 1);
        eim.victory(exitMap);
    }
    if (eim.getValue("finished") == 0) {
        if (mob.getId() == 9390943 || mob.getId() == 9390940 || mob.getId() == 9390941 || mob.getId() == 9390914 || mob.getId() == 9390933 || mob.getId() == 9390942) {
            eim.dropMessage(6, "[King Kobold] How Dare you! I will get you for this!");
            eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(weak[eim.getRandom(0, 5)], eim.getValue("level") - eim.getRandom(250, 500), eim.getValue("scale") - eim.getRandom(5, 10), false, false, false, true), eim.newPoint(eim.getRandom(-50, 750), 45));
        }
    }

}

function finish(eim) {
    eim.exitParty(exitMap);
}

function allMonstersDead(eim) {

}

function cancelSchedule() {
}

function dispose(eim) {
}

function afterSetup(eim) {
}
