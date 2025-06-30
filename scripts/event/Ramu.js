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
var entryMap = 75200;
var exitMap = 450012500;
var eventMapId = 75200;

var minMapId = 75200;
var maxMapId = 75200;
var boss = 8840000;

var eventTime = 30;     // 10 minutes

var lobbyRange = [0, 0];
var weak = new Array(8880403, 8880404, 8880405, 9420620, 2600800, 8645009, 8880150, 8880302, 8880101);

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "Ramu_" + player.getName(), true);
    eim.getMapInstance(75200).setInstanced(true);//boss
    eim.getMapInstance(75200).setBoosted(player.getVar("boost"));//boss
    eim.setValue("scale", scale);
    eim.setValue("level", level);
    eim.setValue("boss", 0);
    eim.setValue("finished", 0);
    eim.schedule("start", 10 * 1000);
    return eim;
}

function start(eim) {
    var nMob3 = eim.getKaoticMonster(9421583, eim.getValue("level"), eim.getValue("scale"), true, false, true, true, 999, true, true, false, true);
    eim.spawnMonsterOnGroundBelow(eventMapId, nMob3, eim.newPoint(0, 75));
    eim.startEventTimer(30 * 60000);
    eim.schedule("bombs", 10);
    eim.schedule("randomboss", 30000);
}

function bombs(eim) {
    if (eim.getValue("finished") == 0) {
        for (var i = 0; i < 8; i++) {
            eim.getMapInstance(75200).spawnMonsterOnGround(eim.getMonsterNoAll(9601278, 1000, 20, false), eim.newPoint(eim.getRandom(-950, 950), eim.getRandom(-500, 50)));
        }
        eim.schedule("bombs", 5000);
    }
}

function randomboss(eim) {
    if (eim.getValue("finished") == 0) {
        if (eim.getValue("boss") < 4) {
            eim.setValue("boss", eim.getValue("boss") + 1);
            eim.dropMessage(6, "[Ramu] Come forth my servant and slay thee pests!");
            var s = eim.getValue("scale") - eim.getRandom(1, 5);
            eim.getMapInstance(75200).spawnMonsterOnGroundBelow(eim.getKaoticMonster(weak[eim.getRandom(0, 8)], eim.getValue("level") - eim.getRandom(0, 500), s, false, false, false, true, s), eim.newPoint(eim.getRandom(-500, 500), 75));
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
    if (mob.getId() == 9421583) {
        eim.dropMessage(6, "[Ramu] You will never find the Black Mage!");
        eim.setValue("finished", 1);
        eim.gainPartyItem(4001238, 250);
        eim.victory(exitMap);
    }
    if (eim.getValue("finished") == 0) {
        if (mob.getId() == 8880403 || mob.getId() == 8880404 || mob.getId() == 8880405 || mob.getId() == 9420620 || mob.getId() == 2600800 || mob.getId() == 8645009 || mob.getId() == 8880150 || mob.getId() == 8880302 || mob.getId() == 8880101) {
            eim.dropMessage(6, "[Ramu] How Dare you! I will get you for this!");
            eim.setValue("boss", eim.getValue("boss") - 1);
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
