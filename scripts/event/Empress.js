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
 * @Author Ronan
 * 3rd Job Event - Magician
 **/


var monsters = new Array(8610005, 8610006, 8610007, 8610008, 8610009);
var elites = new Array(8610010, 8610011, 8610012, 8610013, 8610014);
var bosses = new Array(8850003, 8850001, 8850004, 8850002, 8850000);

var recruitMap = 271040000;
var entryMap = 271040000;
var exitMap = 271040000;

var eventMapId = 81600;

var eventTime = 480; //60 minutes

var lobbyRange = [0, 8];

function setLobbyRange() {
    return lobbyRange;
}

function init() {
}

function setup(player, diff) {
    var eim = em.newInstance(player, "Empress_" + player.getName(), true);
    var map = eim.getMapInstance(eventMapId);
    map.setInstanced(true);
    eim.setIntProperty("finish", 0);
    eim.schedule("start", 10000);

    return eim;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event

}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(eventMapId);
    player.changeMap(map, map.getPortal(0));
}

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function scheduledTimeout(eim) {
    eim.exitParty(exitMap);
}

function playerDisconnected(eim, player) {
    eim.unregisterPlayer(player);
}

function clear(eim) {
    eim.exitParty(exitMap);
}

function changedMap(eim, player, mapid) {
    if (mapid != eventMapId) {
        eim.exitPlayer(player, exitMap);
    }
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, exitMap);
}

function monsterKilled(mob, eim) {
    if (mob.getId() == 8850011) {
        eim.gainPartyStat(5, 10);
        eim.setIntProperty("finish", 1);
        eim.victory(exitMap);
    }
}

function monsterValue(eim, mobId) {
    return 1;
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function dispose() {
}

function start(eim) {
    eim.startEventTimer(eventTime * 60000);
    var random = eim.getRandom(0, 4);

    //phase 1
    var map = eim.getMapInstance(81600);
    var monster = monsters[random];
    for (var i = 0; i < 20; i++) {
        map.spawnMonsterWithEffect(eim.getMonsterNoAll(monster), 15, eim.newPoint(eim.getRandom(20, 1280), -25));
    }
    //phase 2
    var elite = elites[random];
    for (var i = 0; i < 10; i++) {
        map.spawnMonsterWithEffect(eim.getMonsterNoAll(elite), 15, eim.newPoint(eim.getRandom(20, 1280), -25));
    }
    //phase 3
    //knights
    map.spawnMonsterWithEffect(eim.getMonsterNoAll(8850000), 15, eim.newPoint(eim.getRandom(20, 1280), -25));
    map.spawnMonsterWithEffect(eim.getMonsterNoAll(8850001), 15, eim.newPoint(eim.getRandom(20, 1280), -25));
    map.spawnMonsterWithEffect(eim.getMonsterNoAll(8850002), 15, eim.newPoint(eim.getRandom(20, 1280), -25));
    map.spawnMonsterWithEffect(eim.getMonsterNoAll(8850003), 15, eim.newPoint(eim.getRandom(20, 1280), -25));
    map.spawnMonsterWithEffect(eim.getMonsterNoAll(8850004), 15, eim.newPoint(eim.getRandom(20, 1280), -25));
    //empress
    map.spawnMonsterWithEffect(eim.getMonsterNoLink(8850011), 15, eim.newPoint(440, -65));


    eim.schedule("boss", eim.getRandom(60, 120) * 1000);
    eim.schedule("mob", eim.getRandom(10, 30) * 1000);



}

function boss(eim) {
    if (eim.getIntProperty("finish") == 0) {
        var map = eim.getMapInstance(eventMapId);
        var random = eim.getRandom(0, 4);
        var monster = bosses[random];
        for (var i = 1; i <= eim.getRandom(2, 4); i++) {
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(monster), eim.newPoint(eim.getRandom(20, 1280), -25));
        }
        eim.schedule("boss", eim.getRandom(120, 300) * 1000);
        eim.dropMessage(6, "[Empress] Come forth my greatest knights!!!");
    }
}

function mob(eim) {
    if (eim.getIntProperty("finish") == 0) {
        var map = eim.getMapInstance(eventMapId);
        var random = eim.getRandom(0, 4);
        var monster = elites[random];
        for (var i = 1; i <= eim.getRandom(10, 20); i++) {
            map.spawnMonsterOnGroundBelow(eim.getMonsterNoAll(monster), eim.newPoint(eim.getRandom(20, 1280), -25));
        }
        eim.schedule("mob", eim.getRandom(30, 120) * 1000);
        eim.dropMessage(6, "[Empress] Come forth my knights!!!");
    }
}



function end(eim) {
    eim.exitParty(exitMap);
}
// ---------- FILLER FUNCTIONS ----------

function disbandParty(eim, player) {
}

function afterSetup(eim) {
}

function changedLeader(eim, leader) {
}

function leftParty(eim, player) {
}

function clearPQ(eim) {
}

