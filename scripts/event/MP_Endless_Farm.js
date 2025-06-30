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

function setup(player, level, scale, mode) {
    var eim = em.newInstance(player, "MP_Endless_Farm_" + scale + "_" + player.getName(), true);
    eim.setValue("exit", player.getMapId());
    eim.setValue("scale", 1);
    eim.setupPark(mode);
    eim.setValue("generate", 0);
    eim.setValue("clear", 0);
    eim.startEventTimer(60000);
    eim.setValue("start", 1);
    eim.setValue("stage", 1);
    eim.setValue("mode", mode);
    var mapid = eim.getRandomMap(eim.getValue("mode"));
    var map = eim.getMapInstance(mapid);
    eim.setMapInfo(map, exitMap);
    map.setPark(true);
    map.setEndless(true);
    map.killAllMonsters(false);
    map.parkSpawn(eim.getValue("level"), scale, false);
    eim.setValue("spawn", 1);
    eim.setValue("entryMap", mapid);
    eim.setValue("boss", 0);
    eim.setValue("shop", 0);
    eim.setValue("finished", 0);
    eim.setValue("miniboss", 0);
    eim.setValue("midboss", 0);
    eim.setValue("finalboss", 1);
    return eim;
}
function start(eim) {
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(eim.getValue("entryMap"));
    player.changeMap(map, map.getPortal(0));
    player.dropMessage(6, "[Rogue] Starting Tier: " + eim.getValue("baseTier"));
    player.dropMessage(6, "[Rogue] This dungeon has no mini or sub bosses.");
    player.dropMessage(6, "[Rogue] Monsters grow stonger every 5 Waves.");
    player.dropMessage(6, "[Rogue] Floor " + eim.getValue("last") + " spawns the final boss.");
    eim.broadcastMapMsg(player, "Wave: " + eim.getValue("stage") + ": Defeat all the Monsters", 5120205);
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
    //if (mapid == eim.getValue("exit")) {
    //    eim.exitPlayer(player, eim.getValue("exit"));
    //}
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
    var boss = eim.getValue("boss");
    if (mob.getId() == boss) {
        eim.setValue("boss", 0);
        var map = eim.getMapInstance(mob.getMap().getId());
        eim.changeMusic("BgmCustom/Fanfare");
        eim.broadcastMapMsg("Reward Chest has been spawned.", 5120205);
        eim.gainPartyItem(eim.getValue("reward_item"), 25, mob);
        var monster = eim.getKaoticMonster(8950118, eim.getAvgLevel(), eim.getValue("tier"), true, false, true, true, 100, false, false, false, true);
        map.spawnMonsterOnGroundBelow(monster, map.getRandomMonsterSpawnPointPos());
        return;
    }
    if (mob.getId() == 8950118) {
        if (eim.getValue("finished") == 0) {
            eim.gainPartyItem(eim.getValue("reward_item"), 100, mob);
            eim.setValue("finished", 1);
            eim.setValue("clear", 2);
            eim.victory(exitMap);
            return;
        }
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
