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

var baseMap = 953000000;

var eventTime = 30;     // 10 minutes

var lobbyRange = [0, 0];
//bossid = 8641059

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "MP_Normal_" + scale + "_" + player.getName(), true);
    eim.setValue("scale", 2);
    eim.setValue("level", level);
    eim.setValue("zone", scale);
    eim.setValue("finished", 0);
    eim.setValue("reward", 1);
    eim.setValue("exp", 10);
    eim.setValue("rank", 1);
    eim.setValue("baseAch", 210);
    eim.setValue("ach", scale);
    eim.setValue("coin", 2);
    eim.setValue("ticket", 4001516);
    eim.setValue("rock", 4260003);
    eim.setValue("gach", 5220000);

    eim.setValue("minMapId", baseMap + (scale * 10000));
    eim.setValue("maxMapId", baseMap + 500 + (scale * 10000));
    eim.setValue("entryMap", baseMap + (scale * 10000));
    eim.setValue("exit", player.getMapId());

    for (var i = 0; i < 6; i++) {
        var mapid = baseMap + (scale * 10000) + (i * 100);
        var map = eim.getMapInstance(mapid);
        eim.setMapInfo(map, eim.getValue("exit"));
        map.parkSpawn(level, 2);
        map.setPark(true);
    }
    eim.startEventTimer(eventTime * 60000);
    eim.setValue("start", 1);
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(eim.getValue("entryMap"));
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
    if (mapid < eim.getValue("minMapId") || mapid > eim.getValue("maxMapId")) {
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
    eim.exitParty(eim.getValue("exit"));
}

function monsterKilled(mob, eim) {
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
