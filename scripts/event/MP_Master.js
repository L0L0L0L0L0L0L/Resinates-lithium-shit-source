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

var exitMap = 870000010;
var baseMap = 952000000;
var maps = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);

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
    var eim = em.newInstance(player, "MP_Endless_" + scale + "_" + player.getName(), true);
    eim.setValue("exit", exitMap);
    eim.setValue("level", level);
    eim.setValue("scale", scale);
    eim.setValue("endless", 1);
    eim.setValue("generate", 0);
    eim.setValue("entryMap", 4700);
    eim.setMapInfo(eim.getMapInstance(4700), exitMap);
    eim.setMapInfo(eim.getMapInstance(4701), exitMap);

    eim.startEventTimer(eventTime * 60000);
    eim.setValue("start", 1);
    eim.setValue("park", 1);
    eim.setValue("clear", 0);

    eim.getMapInstance(952000500).setVar("section", 1);
    eim.getMapInstance(952010500).setVar("section", 2);
    eim.getMapInstance(952020500).setVar("section", 3);
    eim.getMapInstance(952030500).setVar("section", 4);
    eim.getMapInstance(952040500).setVar("section", 5);
    eim.getMapInstance(953000500).setVar("section", 6);
    eim.getMapInstance(953010500).setVar("section", 7);
    eim.getMapInstance(953020500).setVar("section", 8);
    eim.getMapInstance(953030500).setVar("section", 9);
    eim.getMapInstance(953040500).setVar("section", 10);
    eim.getMapInstance(953050500).setVar("section", 11);
    eim.getMapInstance(954000500).setVar("section", 12);
    eim.getMapInstance(954010500).setVar("section", 13);
    eim.getMapInstance(954020500).setVar("section", 14);
    eim.getMapInstance(954030500).setVar("section", 15);
    eim.getMapInstance(954040500).setVar("section", 16);
    eim.getMapInstance(954050500).setVar("section", 17);

    var map = eim.getMapInstance(4700);
    for (var i = 1; i < 18; i++) {
        map.setObjectFlag("door_" + i, false);
        eim.setValue("room_" + i, 0);
    }
    map.setObjectFlag("final_portal", false);
    map.setVac(false);

    eim.getMapInstance(4701).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9800126, level, scale + 5, true, false, true, true, 2500), eim.newPoint(400, 90));
    eim.getMapInstance(4701).setExpRate(eim.getPlayerCount() * 10);

    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(eim.getValue("entryMap"));
    map.setEndless(true);
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
    if (mapid == eim.getValue("exit")) {
        eim.exitPlayer(player, eim.getValue("exit"));
    }
}

function afterChangedMap(eim, player, mapid) {
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
    if (mob.getId() == 9800126) {
        eim.victory(exitMap);
        eim.gainPowerExp(109, eim.getValue("scale") * eim.getValue("scale") * eim.getPlayerCount());
	eim.gainPartyItem(2430130, 5, mob);
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
