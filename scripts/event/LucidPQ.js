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
var entryMap = 4101;
var exitMap = 4100;

var minMapId = 4101;
var maxMapId = 4103;

var eventTime = 60;     // 45 minutes

var lobbyRange = [0, 0];

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "Lucid_PQ_" + scale + "_" + player.getName(), true);
    eim.setValue("scale", scale);
    eim.setValue("orbs", 78);
    eim.setValue("level", level);
    eim.setValue("bomb", 0);
    eim.setValue("finished", 0);
    for (var i = minMapId; i <= maxMapId; i++) {
        var map = eim.getMapInstance(i);
        if (i == minMapId + 1) {
            map.setObjectInt("floor_0_", eim.getRandom(0, 3));
            map.setObjectInt("floor_1_", eim.getRandom(0, 4));
            map.setObjectInt("floor_2_", eim.getRandom(0, 4));
            map.setObjectInt("floor_3_", eim.getRandom(0, 4));
            map.setObjectInt("floor_4_", eim.getRandom(0, 4));
            map.setObjectInt("floor_5_", eim.getRandom(0, 4));
            map.setObjectInt("floor_6_", eim.getRandom(0, 4));
            map.setObjectInt("floor_7_", eim.getRandom(0, 4));
            map.setObjectInt("floor_8_", eim.getRandom(0, 4));
            map.setVac(false);
        }
        if (i == minMapId + 2) {
            if (eim.getValue("scale") == 1) {
                map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9601281, 1000, 50, true, false, true, true, 1000), eim.newPoint(666, -135));
            }
            if (eim.getValue("scale") == 2) {
                map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9601281, 1500, 55, true, false, true, true, 2000), eim.newPoint(666, -135));
            }
            if (eim.getValue("scale") == 3) {
                map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9601281, 2000, 60, true, false, true, true, 3000), eim.newPoint(666, -135));
            }
            if (eim.getValue("scale") == 4) {
                map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9601281, 2500, 65, true, false, true, true, 5000), eim.newPoint(666, -135));
            }
            if (eim.getValue("scale") == 5) {
                map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9601281, 3000, 70, true, false, true, true, 7500), eim.newPoint(666, -135));
            }
        }
    }
    eim.startEventTimer(eventTime * 60000);

    return eim;
}

function bomb(eim) {
    if (eim.getValue("finished") < 1) {
        if (eim.getValue("bomb") > 0) {
            var map = eim.getMapInstance(4103);
            if (map.getSpawnedMonstersOnMap() < 80) {
                for (var i = 1; i <= 4; i++) {
                    map.spawnMonsterOnGround(eim.getMonsterNoAll(9601278, 1000, 10, false), eim.newPoint(eim.getRandom(-350, 1000), eim.getRandom(-800, -40)));
                }
            }
            eim.schedule("bomb", 5000);
        }
    }
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
    if (mapid == 4101) {
        eim.playerItemMsg(player, "Destory all the orbs to progress", 5120182);
    }
    if (mapid == 4102) {
        eim.playerItemMsg(player, "Complete the portal maze by entering the portals", 5120182);
    }
    if (mapid == 4103) {
        if (eim.getValue("bomb") == 0) {
            eim.setValue("bomb", 1);
            eim.schedule("bomb", 5000);
        }
        eim.playerItemMsg(player, "Destory the Demon", 5120182);
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
    if (mob.getId() == 9601281) {
        eim.setValue("finished", 1);
        eim.setValue("bomb", 0);
        eim.gainPartyItem(2585008, eim.getPlayerCount() * eim.getValue("scale"));
        eim.victory(exitMap);
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


