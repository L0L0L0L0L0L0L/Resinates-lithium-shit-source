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
var entryMap = 610052500;
var exitMap = 610050000;
var eventMapId = 610052500;

var minMapId = 610052500;
var maxMapId = 610052500;
var boss = 9480238;
var lvl = 2975;
var tier = 29;

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
    var eim = em.newInstance(player, "BGC_" + player.getName(), true);
    eim.setValue("exit", player.getMapId());
    eim.getMapInstance(75200).setInstanced(true);//boss
    eim.setValue("scale", scale);
    eim.setValue("level", level);
    eim.setValue("boss", 0);
    eim.setValue("finished", 0);
    eim.schedule("start", 10 * 1000);
    return eim;
}

function start(eim) {
    eim.getMapInstance(entryMap).spawnMonsterOnGroundBelow(eim.getKaoticMonster(boss, 3800, 73, true, false, true), eim.newPoint(400, 10));
    eim.startEventTimer(30 * 60000);
    eim.schedule("bombs", 10);
}

function bombs(eim) {
    if (eim.getValue("finished") == 0) {
        for (var i = 0; i < 8; i++) {
            eim.getMapInstance(eventMapId).spawnMonsterOnGround(eim.getMonsterNoAll(9601278, 1000, 20, false), eim.newPoint(eim.getRandom(-800, 700), eim.getRandom(-500, 0)));
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
    eim.exitPlayer(player, eim.getValue("exit"));
}

function playerLeft(eim, player) {
    eim.exitPlayer(player, eim.getValue("exit"));
}

function changedMap(eim, player, mapid) {
    if (mapid < minMapId || mapid > maxMapId) {
        eim.exitPlayer(player, eim.getValue("exit"));
    }
}

function changedLeader(eim, leader) {
    eim.changeEventLeader(leader);
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, eim.getValue("exit"));
}

function playerDisconnected(eim, player) {
    eim.exitPlayer(player, eim.getValue("exit"));
}

function leftParty(eim, player) {
    eim.exitPlayer(player, eim.getValue("exit"));
}

function disbandParty(eim) {
    eim.exitParty(eim.getValue("exit"));
}

function monsterValue(eim, mobId) {
    return 1;
}

function end(eim) {
    eim.exitParty(exitMap);
}

function monsterKilled(mob, eim) {
    if (mob.getId() == boss) {
        eim.gainPartyItem(4310150, eim.getRandom(5, 10) * eim.getPlayerCount());
        eim.setValue("finished", 1);
        eim.victory(eim.getValue("exit"));
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
