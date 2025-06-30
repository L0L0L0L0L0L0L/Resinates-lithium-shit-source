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
var maps = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);

var eventTime = 120;     // 10 minutes

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
    var eim = em.newInstance(player, "DP_Endless_" + scale + "_" + player.getName(), true);
    eim.setValue("exit", player.getMapId());
    eim.setValue("level", level > 9999 ? 9999 : level);
    eim.setValue("scale", 0);
    eim.setValue("endless", 1);
    eim.setValue("generate", 0);
    eim.setValue("mode", 0);

    eim.setValue("minMapId", 46000);
    eim.setValue("maxMapId", 46999);
    eim.setValue("entryMap", 46100 + eim.getRandom(1, 15));

    eim.startEventTimer(eventTime * 60000);
    eim.setValue("start", 1);
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(eim.getValue("entryMap"));
    map.setEndless(true);
    player.changeMap(map, map.getPortal(0));
    eim.countPots(player);
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
            if (map != null) {
                map.setEndless(true);
                map.setExpRate(2);
                map.setSpawnCap(999);
                map.setSkillExp(2.0);
                map.setPot(true);
                map.killAllMonsters(false);
                map.parkFelSpawn(lvl, scale, 99);
            }
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
    if (mob.getId() == 50018) {
        eim.gainPowerExp(100, Math.pow(scale, 3));
        eim.gainPartySkinLevel(mob);
    } else if (mob.getId() == 50017) {
        eim.gainPowerExp(100, Math.pow(scale, 2));
        eim.gainPartySkinLevel(mob);
    } else {
        eim.gainPowerExp(100, Math.pow(scale, 1));
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
