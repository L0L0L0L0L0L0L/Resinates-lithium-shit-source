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

var monsters = new Array(8610005, 8610006, 8610007, 8610008, 8610009);
var elites = new Array(8610010, 8610011, 8610012, 8610013, 8610014);
var bosses = new Array(8850003, 8850001, 8850004, 8850002, 8850000);
var isPq = true;
var minPlayers = 2, maxPlayers = 6;
var minLevel = 150, maxLevel = 255;
var entryMap = 410007609;
var exitMap = 870000010;
var eventMapId = 410007609;
var minMapId = 410007609;
var maxMapId = 410007609;

var boss = 8850011;

var eventTime = 60;     // 10 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "BMT_Empress_" + player.getName(), true);
    eim.getMapInstance(eventMapId).setInstanced(true);//boss
    eim.getMapInstance(eventMapId).setBoosted(player.getVar("boost"));//boss
	eim.setMapInfo(eim.getMapInstance(eventMapId), exitMap);
    eim.setValue("scale", scale);
    eim.setValue("level", level);
    eim.schedule("start", 10 * 1000);
    eim.setValue("stage", 0);
    eim.setValue("finished", 0);
    return eim;
}

function start(eim) {
    var random = eim.getRandom(0, 4);
    var map = eim.getMapInstance(eventMapId);
    var monster = monsters[random];
    var level = eim.getValue("level");
    var scale = eim.getValue("scale");
    for (var i = 0; i < 20; i++) {
        map.spawnMonsterWithEffect(eim.getMonsterNoAll(monster, level, scale - 3), 15, eim.newPoint(eim.getRandom(-450, 950), 750));
    }
    //phase 2
    var elite = elites[random];
    for (var i = 0; i < 10; i++) {
        map.spawnMonsterWithEffect(eim.getMonsterNoAll(elite, level, scale - 2), 15, eim.newPoint(eim.getRandom(-450, 950), 750));
    }
    //phase 3
    //knights
    map.spawnMonsterWithEffect(eim.getMonsterNoAll(8850000, level, scale - 1), 15, eim.newPoint(eim.getRandom(-450, 950), 750));
    map.spawnMonsterWithEffect(eim.getMonsterNoAll(8850001, level, scale - 1), 15, eim.newPoint(eim.getRandom(-450, 950), 750));
    map.spawnMonsterWithEffect(eim.getMonsterNoAll(8850002, level, scale - 1), 15, eim.newPoint(eim.getRandom(-450, 950), 750));
    map.spawnMonsterWithEffect(eim.getMonsterNoAll(8850003, level, scale - 1), 15, eim.newPoint(eim.getRandom(-450, 950), 750));
    map.spawnMonsterWithEffect(eim.getMonsterNoAll(8850004, level, scale - 1), 15, eim.newPoint(eim.getRandom(-450, 950), 750));
    //empress
    var mob = eim.getKaoticMonster(boss, eim.getValue("level"), eim.getValue("scale"), true, false, true, true, 999, true, true, false, false);
    eim.getMapInstance(eventMapId).spawnMonsterOnGroundBelow(mob, eim.newPoint(330, 750));
    eim.startEventTimer(30 * 60000);
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
    if (mob.getId() == 8850011) {
        eim.gainPartyItem(4310296, eim.getValue("scale"));
        eim.setValue("finished", 1);
        eim.victory(exitMap);
    }
    if (eim.getValue("finished") == 0) {
        if (mob.getId() >= 8850000 && mob.getId() <= 8850004) {
            var map = eim.getMapInstance(eventMapId);
            var level = eim.getValue("level");
            var scale = eim.getValue("scale");
            map.spawnMonsterWithEffect(eim.getMonsterNoAll(mob.getId(), level, scale - 1), 15, eim.newPoint(eim.getRandom(-450, 950), 750));
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
