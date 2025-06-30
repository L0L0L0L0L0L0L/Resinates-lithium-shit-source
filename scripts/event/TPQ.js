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
var entryMap = 4501;
var exitMap = 4500;

var minMapId = 4501;
var maxMapId = 4509;

var eventTime = 30;     // 45 minutes

var lobbyRange = [0, 0];

var x = new Array(-720, -500, -280, 20, 400, 800, 585, 340, -200, -400, -900, -900, -700, -500, -100, 140, 550);
var y = new Array(220, 220, 220, 220, 220, 220, -93, -93, -93, -93, -235, -538, -538, -538, -390, -390, -535);

function init() {

}

function setLobbyRange() {
    return lobbyRange;
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
}

function setup(player, level, scale) {
    var eim = em.newInstance(player, "TPQ_" + player.getName(), true);
    eim.setValue("scale", scale);
    eim.setValue("level", level);
    eim.setValue("finished", 0);
    eim.setValue("portal", 0);

    var map1 = eim.getMapInstance(4503);//elite
    var randomsp = eim.getRandom(0, 16);
    map1.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9833430, 50, 1, true, false, true, true, scale * 100), eim.newPoint(x[randomsp] + eim.getRandom(-50, 50), y[randomsp]));

    var map2 = eim.getMapInstance(4504);//trash mobs
    eim.setValue("kills", 0);//map 4504
    eim.setSpawnCap(4504, 50);
    for (var i = 0; i < 50; i++) {
        var random = eim.getRandom(0, 16);
        map2.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9833440, 10, 1, true, false, true, true, scale * 10), eim.newPoint(x[random] + eim.getRandom(-50, 50), y[random]));
    }

    eim.setValue("jump", 0);//map 4505
    eim.setValue("orbs", 25);//map 4506

    var map3 = eim.getMapInstance(4508);
    map3.setObjectInt("floor_0_", eim.getRandom(0, 2));
    map3.setObjectInt("floor_1_", eim.getRandom(0, 4));
    map3.setObjectInt("floor_2_", eim.getRandom(0, 4));
    map3.setObjectInt("floor_3_", eim.getRandom(0, 5));
    map3.setObjectInt("floor_4_", eim.getRandom(0, 7));
    map3.setObjectInt("floor_5_", eim.getRandom(0, 5));
    map3.setObjectInt("floor_6_", eim.getRandom(0, 5));
    map3.setVac(false);
    //map.setSummons(false);
    //eventTime = 60 * eim.getValue("scale");

    var map4 = eim.getMapInstance(4509);//elite
    eim.setSpawnCap(4509, 50);
    var mobid = 9833432;
    if (scale == 2) {
        mobid = 9833434;
    }
    if (scale == 3) {
        mobid = 9833436;
    }
    if (scale == 4) {
        mobid = 9833438;
    }
    map4.spawnMonsterOnGroundBelow(eim.getKaoticMonster(mobid, 100 + (scale * 25), scale, true, false, true, true, scale * 500), eim.newPoint(650, 220));
    eim.startEventTimer(30 * 60000);
    return eim;
}

function bombs(eim) {
    if (eim.getValue("finished") == 0) {
        eim.getMapInstance(4509).spawnMonsterOnGround(eim.getMonsterNoAll(9601278, 1000, 20, false), eim.newPoint(eim.getRandom(-500, 1100), -400));
        eim.schedule("bombs", 2500);
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
    eim.exitParty(exitMap);
}

function playerLeft(eim, player) {
    eim.exitParty(exitMap);
}

function changedMap(eim, player, mapid) {
    if (mapid < minMapId || mapid > maxMapId) {
        eim.exitParty(exitMap);
    }
}

function afterChangedMap(eim, player, mapid) {
    if (mapid == 4501) {
        eim.playerItemMsg(player, "Scale the Mountain to progress, Everyone must complete this stage.", 5120205);
    }
    if (mapid == 4502) {
        eim.playerItemMsg(player, "Each player must pick a portal to challenge, All 4 Portals must be completed to unlock.", 5120205);
    }
    if (mapid == 4503) {
        eim.playerItemMsg(player, "Defeat the Boss to progress.", 5120205);
    }
    if (mapid == 4504) {
        eim.playerItemMsg(player, "Defeat all the monsters to progress.", 5120205);
    }
    if (mapid == 4505) {
        eim.playerItemMsg(player, "Jump the final platform to progress.", 5120205);
    }
    if (mapid == 4506) {
        eim.playerItemMsg(player, "Destory all the orbs to progress.", 5120205);
    }
    if (mapid == 4507) {
        eim.playerItemMsg(player, "Everyone must be on this map in order to progress.", 5120205);
    }
    if (mapid == 4508) {
        eim.playerItemMsg(player, "Solve portal maze to progress.", 5120205);
    }
    if (mapid == 4509) {
        eim.playerItemMsg(player, "Defeat the final boss. Watch out for killer bombs!", 5120205);
        if (eim.getValue("bombs") < 1) {
            eim.setValue("bombs", 1);
            eim.schedule("bombs", 5000);
        }
    }
}

function changedLeader(eim, leader) {
    eim.changeEventLeader(leader);
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitParty(exitMap);
}

function playerDisconnected(eim, player) {
    eim.exitParty(exitMap);
}

function leftParty(eim, player) {
    eim.exitParty(exitMap);
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
    if (mob.getId() == 9833440) {
        eim.setValue("kills", eim.getValue("kills") + 1);
        if (eim.getValue("kills") >= 50) {
            eim.getMapInstance(4504).showClear();
        } else {
            eim.broadcastMapMsg((50 - eim.getValue("kills")) + " Monsters remaining", 5120205);
        }
    }
    if (mob.getId() == 9833430) {
        eim.getMapInstance(4503).showClear();
    }
    if (mob.getId() == 9833432 || mob.getId() == 9833434 || mob.getId() == 9833436 || mob.getId() == 9833438) {
        eim.setValue("finished", 1);
        eim.gainPartyItem(2586005, eim.getPlayerCount() * eim.getValue("scale"));
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


