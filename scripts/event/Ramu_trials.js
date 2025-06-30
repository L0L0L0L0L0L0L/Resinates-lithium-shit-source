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

var eventTime = 480;     // 10 minutes

var lobbyRange = [0, 0];
var weak = new Array(8880000, 8880101, 8240099, 8880150, 8880302, 8880403, 8880404, 8880405, 8645009, 2600800);




function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "Ramu_trials_" + player.getName(), true);
    eim.getMapInstance(75200).setInstanced(true);//boss
    eim.setValue("scale", scale);
    eim.setValue("level", level);
    eim.setValue("boss", 0);
    eim.setValue("finished", 0);
    eim.schedule("start", 10 * 1000);
    return eim;
}

function start(eim) {
    eim.setValue("hits", 50 * eim.getPlayerCount());
    eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(weak[eim.getValue("boss")], 2500, 70, true, false, false, true, eim.getValue("hits")), eim.newPoint(eim.getRandom(-500, 500), 75));
    eim.startEventTimer(60 * 60000);
    eim.schedule("bombs", 10);
}

function bombs(eim) {
    if (eim.getValue("finished") == 0) {
        for (var i = 0; i < 8; i++) {
            eim.getMapInstance(75200).spawnMonsterOnGround(eim.getMonsterNoAll(9601278, 1000, 20, false), eim.newPoint(eim.getRandom(-950, 950), eim.getRandom(-500, 50)));
        }
        eim.schedule("bombs", 5000);
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
    if (eim.getValue("finished") == 0) {
        if (mob.getId() == 8880000 || mob.getId() == 8880101 || mob.getId() == 8240099 || mob.getId() == 8880150 || mob.getId() == 8880302 || mob.getId() == 8880403 || mob.getId() == 8880404 || mob.getId() == 8880405 || mob.getId() == 8645009) {
            var h = eim.getValue("hits");
            eim.setValue("hits", h + (5 * eim.getPlayerCount()));
            eim.setValue("boss", eim.getValue("boss") + 1);
            eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(weak[eim.getValue("boss")], 2500, 70, true, false, false, true, eim.getValue("hits")), eim.newPoint(eim.getRandom(-500, 500), 75));
            eim.dropMessage(6, "[Ramu] Bring on next servant!");
        }
    }
    if (mob.getId() == 2600800) {
        mob.getKiller().addPocket();
        eim.dropMessage(6, "[Ramu] You have comepleted my Trials and have unlocked new pocket slot!");
        eim.setValue("finished", 1);
        eim.gainPartyItem(4032906, 25 * mob.getBonus());
        eim.victory(exitMap);
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
