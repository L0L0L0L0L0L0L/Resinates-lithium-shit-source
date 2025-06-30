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
var entryMap = 3000;
var exitMap = 4000;

var minMapId = 3000;
var maxMapId = 3000;

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
    eim.setValue("scale", scale);
    eim.getMapInstance(3000).setEverlast(true);
    eim.getMapInstance(3000).spawnMonsterOnGroundBelow(eim.getMonster(9601204, 1, 1), eim.newPoint(180, 70));
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
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
    eim.exitPlayer(player, exitMap);
}

function playerDisconnected(eim, player) {
    eim.unregisterPlayer(player);
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
    if (mob.getId() == 9601204 || mob.getId() == 9601205 || mob.getId() == 9601206) {
        eim.setIntProperty("finished", 1);
        var scale = eim.getValue("scale");
        var count = 1, drop = 11, maxlevel = 1000, amount = 1;
        if (scale == 2) {
            count = 2;
            drop = 12;
            maxlevel = 1500;
            amount = 500;
        }
        if (scale == 3) {
            count = 5;
            drop = 13;
            maxlevel = 2000;
            amount = 500;
        }
        if (scale == 4) {
            count = 10;
            drop = 14;
            maxlevel = 2500;
            amount = 500;
        }
        if (scale == 5) {
            count = 20;
            drop = 15;
            maxlevel = 9999;
            amount = 500;
        }
        if (scale == 6) {
            count = 50;
            drop = 16;
            maxlevel = 9999;
            amount = 500;
        }
        if (scale == 7) {
            count = 100;
            drop = 17;
            maxlevel = 9999;
            amount = 500;
        }
        eim.gainPartyItem(4310150, count * 25);
        
        eim.getMapInstance(3000).spawnReward(drop, amount);
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


