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
 * @event: Vs Papulatus
 */

var isPq = true;
var entryMap = 52000;
var exit = 870000010;

var minMapId = 3001;
var maxMapId = 3001;

var eventTime = 10;     // 45 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "Reward_" + player.getName(), true);
    eim.setValue("exit", player.getMapId());
    eim.setValue("scale", scale);
    var map = eim.getMapInstance(entryMap);
    map.setEverlast(true);
    eim.setMapInfo(map, eim.getValue("exit"));
    map.spawnMonsterOnGroundBelow(eim.getMonster(9601204, 1, 1), eim.newPoint(800, -80));
    eim.startEventTimer(eventTime * 60000);
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
    eim.exitParty(eim.getValue("exit"));
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
    if (mapid != entryMap) {
        eim.exitPlayer(player, eim.getValue("exit"));
    }
}

function afterChangedMap(eim, player, mapid) {
    if (mapid == 4407) {
        eim.playerItemMsg(player, "Crack open the treasure chest to get your rewards", 5120205);
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
    eim.unregisterPlayer(player);
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
    eim.exitParty(eim.getValue("exit"));
}

function monsterKilled(mob, eim) {
    if (mob.getId() == 9601204 || mob.getId() == 9601205 || mob.getId() == 9601206) {
        eim.setIntProperty("finished", 1);
        eim.gainPartyItem(4310502, 5);
        eim.getMapInstance(entryMap).spawnReward(4, eim.getRandom(100, 500));
        eim.victory(eim.getValue("exit"));
    }
}

function finish(eim) {
    eim.exitParty(eim.getValue("exit"));
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function dispose(eim) {
}

function afterSetup(eim) {
}


