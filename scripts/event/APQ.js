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
var entryMap = 4002;
var exitMap = 4001;

var minMapId = 4002;
var maxMapId = 4007;

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
    var eim = em.newInstance(player, "APQ_" + player.getName(), true);
    eim.setValue("scale", scale);
    var cap = 100;
    var hits = 1;
    if (scale == 1) {
        cap = 100;
        eim.setValue("torch", 15);
    }
    if (scale == 2) {
        cap = 250;
        eim.setValue("torch", 10);
    }
    if (scale == 3) {
        cap = 500;
        eim.setValue("torch", 5);
    }
    if (scale == 4) {
        cap = 1000;
        eim.setValue("torch", 0);
    }
    if (scale == 5) {
        cap = 1500;
        eim.setValue("torch", 0);
    }
    eim.setValue("star", 5120205);
    for (var i = 4002; i <= 4007; i++) {
        var map = eim.getMapInstance(i);
        map.closeDoor();
        if (i == 4002) {
            map.setFlagsAQ_1();
            map.setVac(false);
        }
        if (i == 4003) {
            map.setFlagsAQ_2();
        }
        if (i == 4004) {
            map.setFlagsAQ_3();
        }
        if (i == 4005) {
            map.setFlagsAQ_4();
        }
        if (i == 4006) {
            map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9601174, level, scale, true, false, true, true, cap), eim.newPoint(947, 70));
        }
        if (i == 4007) {
            if (scale == 1 || scale == 2) {
                map.spawnMonsterOnGroundBelow(eim.getMonster(9601206, 1, 1), eim.newPoint(850, 70));
            }
            if (scale == 3) {
                map.spawnMonsterOnGroundBelow(eim.getMonster(9601205, 1, 1), eim.newPoint(850, 70));
            }
            if (scale == 4 || scale == 5) {
                map.spawnMonsterOnGroundBelow(eim.getMonster(9601204, 1, 1), eim.newPoint(850, 70));
            }
        }
    }
    //map.setSummons(false);
    eventTime = 30 - (eim.getValue("scale") * 4);
    eim.startEventTimer(eventTime * 60000);

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
    if (mapid == 4002) {
        eim.playerItemMsg(player, "Complete the portal maze by entering the portals", 5120205);
    }
    if (mapid == 4003) {
        eim.playerItemMsg(player, "Count the Stars and set the Stones to matching numbers", 5120205);
    }
    if (mapid == 4004) {
        eim.playerItemMsg(player, "Turn all Red Stars into Blue/Yellow stars to unlock hidden path", 5120205);
    }
    if (mapid == 4005) {
        eim.playerItemMsg(player, "Light up all torches by killing flames near the torches", 5120205);
    }
    if (mapid == 4006) {
        eim.playerItemMsg(player, "Defeat the Master Guardian", 5120205);
    }
    if (mapid == 4007) {
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
    if (mob.getId() == 9601174) {
        eim.getMapInstance(4006).showClear();
    }
    if (mob.getId() == 9601204 || mob.getId() == 9601205 || mob.getId() == 9601206) {
        var scale = eim.getValue("scale");
        eim.gainPartyEtc(2049308, eim.getPlayerCount() * scale);
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


