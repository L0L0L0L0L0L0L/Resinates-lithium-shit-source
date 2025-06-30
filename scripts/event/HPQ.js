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
var entryMap = 4402;
var exitMap = 4401;

var minMapId = 4402;
var maxMapId = 4407;

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
    var eim = em.newInstance(player, "Halloween_PQ_" + scale + "_" + player.getName(), true);
    eim.setValue("scale", scale);
    if (eim.getValue("scale") == 1) {
        eim.setValue("level", 100);
        eim.setValue("levelInc", 1);
        eim.setValue("cap", 50);
    }
    if (eim.getValue("scale") == 2) {
        eim.setValue("level", 150);
        eim.setValue("levelInc", 2);
        eim.setValue("cap", 100);
    }
    if (eim.getValue("scale") == 3) {
        eim.setValue("level", 200);
        eim.setValue("levelInc", 3);
        eim.setValue("cap", 250);
    }
    if (eim.getValue("scale") == 4) {
        eim.setValue("level", 250);
        eim.setValue("levelInc", 5);
        eim.setValue("cap", 500);
    }
    if (eim.getValue("scale") == 5) {
        eim.setValue("level", 500);
        eim.setValue("levelInc", 10);
        eim.setValue("cap", 1000);
    }
    eim.setValue("door", eim.getRandom(0, 48));
    eim.getMapInstance(4403).setFlagsAQ_2();
    eim.setValue("star", 5120182);
    eim.setValue("orbs", 44);
    eim.setValue("kids", 30);
    eim.setValue("bomb", 0);
    eim.setValue("bonus", 0);
    eim.setValue("phase", 0);
    eim.getMapInstance(4406).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9833719, eim.getValue("level"), eim.getValue("scale"), true, false, true, true, eim.getValue("cap")), eim.newPoint(666, -110));
    eim.getMapInstance(4407).spawnMonsterOnGroundBelow(eim.getMonster(9601204, 1, 1), eim.newPoint(700, 1080));
    eim.startEventTimer(eventTime * 60000);

    return eim;
}

function bomb(eim) {
    if (eim.getIntProperty("finished") < 1) {
        var map = eim.getMapInstance(4406);
        if (map.getSpawnedMonstersOnMap() < 80) {
            for (var i = 1; i <= 8; i++) {
                map.spawnMonsterOnGround(eim.getMonsterNoAll(9601278, 1000, eim.getValue("scale"), false), eim.newPoint(eim.getRandom(-600, 2000), eim.getRandom(-200, 400)));
            }
        }
        eim.schedule("bomb", 5000);
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
    if (mapid == 4402) {
        eim.playerItemMsg(player, "Find the secret door.", 5120182);
        return;
    }
    if (mapid == 4403) {
        eim.playerItemMsg(player, "Count eh stars.", 5120182);
        return;
    }
    if (mapid == 4404) {
        eim.playerItemMsg(player, "Save the children's souls by hitting them.", 5120182);
        return;
    }
    if (mapid == 4405) {
        eim.playerItemMsg(player, "Destory all the pumpkins.", 5120182);
        return;
    }
    if (mapid == 4406) {
        eim.playerItemMsg(player, "Destory the Master Pumpkin", 5120182);
        return;
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
    if (mob.getId() == 9833719) {
        eim.setValue("level", eim.getValue("level") + eim.getValue("levelInc"));
        eim.setValue("phase", eim.getValue("phase") + 1);
        eim.getMapInstance(4406).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9833720, eim.getValue("level"), eim.getValue("scale"), true, false, true, true, eim.getValue("cap") * 2), eim.newPoint(666, -110));
        eim.schedule("bomb", 1000);
    }
    if (mob.getId() == 9833720) {
        eim.setValue("level", eim.getValue("level") + eim.getValue("levelInc"));
        eim.setValue("phase", eim.getValue("phase") + 1);
        eim.getMapInstance(4406).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9833721, eim.getValue("level"), eim.getValue("scale"), true, false, true, true, eim.getValue("cap") * 3), eim.newPoint(666, -110));
    }
    if (mob.getId() == 9833721) {
        eim.setValue("level", eim.getValue("level") + eim.getValue("levelInc"));
        eim.setValue("phase", eim.getValue("phase") + 1);
        eim.getMapInstance(4406).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9833722, eim.getValue("level"), eim.getValue("scale"), true, false, true, true, eim.getValue("cap") * 4), eim.newPoint(666, -110));
    }
    if (mob.getId() == 9833722) {
        eim.setValue("level", eim.getValue("level") + eim.getValue("levelInc"));
        eim.setValue("phase", eim.getValue("phase") + 1);
        eim.getMapInstance(4406).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9833723, eim.getValue("level"), eim.getValue("scale"), true, false, true, true, eim.getValue("cap") * 5), eim.newPoint(666, -110));
    }
    if (mob.getId() == 9833723) {
        eim.setValue("level", eim.getValue("level") + eim.getValue("levelInc"));
        eim.setValue("phase", eim.getValue("phase") + 1);
        eim.getMapInstance(4406).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9833724, eim.getValue("level"), eim.getValue("scale"), true, false, true, true, eim.getValue("cap") * 7), eim.newPoint(666, -110));
    }
    if (mob.getId() == 9833724) {
        eim.setValue("level", eim.getValue("level") + eim.getValue("levelInc"));
        eim.setValue("phase", eim.getValue("phase") + 1);
        eim.getMapInstance(4406).spawnMonsterOnGroundBelow(eim.getKaoticMonster(9833725, eim.getValue("level"), eim.getValue("scale"), true, false, true, true, eim.getValue("cap") * 10), eim.newPoint(666, -110));
    }
    if (mob.getId() == 9833725) {
        eim.eimItemMsg("Master Pumpkin has been destoryed!", 5120182);
        eim.getMapInstance(4406).showClear();
    }
    if (mob.getId() == 9500195) {
        eim.setValue("bonus", eim.getValue("bonus") + 1);
    }
    if (mob.getId() == 9601204 || mob.getId() == 9601205 || mob.getId() == 9601206) {
        eim.setIntProperty("finished", 1);
        eim.gainPartyEtc(2049177, eim.getPlayerCount() * eim.getValue("scale"));
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


