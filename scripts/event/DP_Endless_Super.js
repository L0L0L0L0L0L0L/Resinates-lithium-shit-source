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

var exitMap = 46000;
var baseMap = 46001;
var maps = new Array(46004, 46010, 46011, 46015, 46019, 46020);

var eventTime = 60;     // 10 minutes

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
    var eim = em.newInstance(player, "DP_Endless_Super_" + scale + "_" + player.getName(), true);
    eim.setValue("exit", player.getMapId());
    eim.setValue("level", level > 9999 ? 9999 : level);
    eim.setValue("scale", 0);
    eim.setValue("endless", 1);
    eim.setValue("generate", 0);
    eim.setValue("mode", 3);

    eim.setValue("minMapId", 46000);
    eim.setValue("maxMapId", 46999);
    var random = eim.getRandom(0, 5);
    eim.setValue("entryMap", maps[random]);

    eim.startEventTimer(eventTime * 60000);
    eim.setValue("start", 1);
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
    } else {
        eim.warpEventTeam(mapid);
    }
}

function afterChangedMap(eim, player, mapid) {
    if (mapid != eim.getValue("exit")) {
        if (eim.getValue("generate") == 0) {
            eim.setValue("generate", 1);
            eim.setValue("scale", eim.getValue("scale") + 1);
            var scale = eim.getValue("scale");
            var lvl = eim.eimLevel();
            var map = eim.getMapInstance(mapid);
            map.killAllMonsters(true);
            map.setEndless(true);
            map.setExpRate(3);
            map.setSpawnCap(999);
            map.parkFelSpawn(lvl, scale, 999);
        }
    }
    eim.playerItemMsg(player, "Stage: " + eim.getValue("scale") + " Defeat all the Monsters", 5120205);
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
    var scale = eim.getValue("scale");
    if (mob.getId() == 50008) {
        eim.gainPowerExp(105, Math.pow(scale, 5));
    } else if (mob.getId() == 50007) {
        eim.gainPowerExp(105, Math.pow(scale, 4));
    } else {
        eim.gainPowerExp(105, Math.pow(scale, 3));
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
