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


var recruitMap = 925020001;
var entryMap = 925020001;
var exitMap = 925020001;

var eventMapId = 925020002;

var startMobId = 9305300;
var endMobId = 9305339;

var minPlayers = 3, maxPlayers = 6;
var minLevel = 140, maxLevel = 255;

var eventTime = 60; //30 minutes
var lobbyRange = [0, 8];

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
    return eligible;
}

//setup --------------------------------------------------------------------

function setLobbyRange() {
    return lobbyRange;
}

function init() {
}

function setup(player, diff) {
    var eim = em.newInstance(player, "Kaotic_Dojo_" + player.getName(), true);

    eim.setIntProperty("reward", player.getTotalLevel());
    if (diff == 1) {
        eim.setIntProperty("level", 2000);
        eim.setIntProperty("scale", 7);
    } else if (diff == 2) {
        eim.setIntProperty("level", 4000);
        eim.setIntProperty("scale", 8);
    } else if (diff == 3) {
        eim.setIntProperty("level", 6000);
        eim.setIntProperty("scale", 9);
    } else if (diff == 4) {
        eim.setIntProperty("level", 8000);
        eim.setIntProperty("scale", 10);
    }
    eim.schedule("start", 10 * 1000);
    eim.createEventTimer(10 * 1000);
    eim.setIntProperty("finished", 0);
    eim.setIntProperty("wave", 0);
    eim.setScale(false);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    player.dropMessage(6, "[Kaotic Dojo] Event will begin in 10 seconds.");
}

//event --------------------------------------------------------------------

function start(eim) {
    eim.changeMusic("BgmFF8/Legendary_Beast");
    var map = eim.getMapInstance(eventMapId);
    var mob = eim.getMonsterNoDropsLink(startMobId, eim.getIntProperty("level"), eim.getIntProperty("scale"));
    mob.setForceBar();
    map.spawnMonsterWithEffectBelow(mob, 15, eim.newPoint(eim.getRandom(-250, 250), 0));
    eim.startEventTimer(eventTime * 60000);
    eim.dropMessage(6, "[Kaotic Dojo] Battle has begun. Good Luck!!!");
    eim.setIntProperty("wave", 1);
    eim.dropMessage(6, "[Kaotic Dojo] Round: " + eim.getIntProperty("wave") + " - Monster level: " + eim.getIntProperty("level"));
}

function finish(eim) {
    eim.exitParty(exitMap);
}

//timer ending --------------------------------------------------------------------
function scheduledTimeout(eim) {
    eim.exitParty(exitMap);
}

//monsters--------------------------------------------------------------------

function monsterKilled(mob, eim) {
    var map = eim.getMapInstance(eventMapId);
    if (mob.getId() >= startMobId && mob.getId() < endMobId) {
        var level = eim.getIntProperty("level") + eim.getIntProperty("scale");
        eim.setIntProperty("level", level);
        if (eim.getIntProperty("scale") == 7) {
            eim.gainPartyItem(2049300, 5);
        } else if (eim.getIntProperty("scale") == 8) {
            eim.gainPartyItem(2049300, 10);
        } else if (eim.getIntProperty("scale") == 9) {
            eim.gainPartyItem(2049300, 25);
        } else if (eim.getIntProperty("scale") == 10) {
            eim.gainPartyItem(2049300, 50);
        }
        eim.setIntProperty("wave", eim.getIntProperty("wave") + 1);
        eim.dropMessage(6, "[Kaotic Dojo] Round: " + eim.getIntProperty("wave") + " - Monster level: " + eim.getIntProperty("level"));

        var mob = eim.getMonsterNoDropsLink(mob.getId() + 1, eim.getIntProperty("level"), eim.getIntProperty("scale"));
        mob.setForceBar();
        eim.getMapInstance(eventMapId).spawnMonsterWithEffectBelow(mob, 15, eim.newPoint(eim.getRandom(-250, 250), 0));
    }
    if (mob.getId() == endMobId) {
        eim.setIntProperty("finished", 1);
        if (eim.getIntProperty("scale") == 7) {
            eim.gainAchievement(182);
            eim.gainPartyItem(2049300, 100);
        } else if (eim.getIntProperty("scale") == 8) {
            eim.gainAchievement(183);
            eim.gainPartyItem(2049300, 250);
        } else if (eim.getIntProperty("scale") == 9) {
            eim.gainAchievement(184);
            eim.gainPartyItem(2049300, 500);
        } else if (eim.getIntProperty("scale") == 10) {
            eim.gainAchievement(185);
            eim.gainPartyItem(2049300, 1000);
        }
        eim.gainPartyEquip(1082392, eim.getIntProperty("scale"), 50149, false);
        eim.gainPartyEquip(1142672, eim.getIntProperty("scale"), false);

        eim.victory(exitMap);
    }
}

function monsterValue(eim, mobId) {
    return 1;
}

function allMonstersDead(eim) {
}

//player leave --------------------------------------------------------------------

function playerUnregistered(eim, player) {
}

function playerExit(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function playerDisconnected(eim, player) {
    eim.exitPlayer(player, exitMap);
}

function clear(eim) {
}

function changedMap(eim, player, mapid) {
    if (mapid != eventMapId) {
        //eim.unregisterPlayer(player);
        eim.exitPlayer(player, exitMap);
    }
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    eim.exitPlayer(player, exitMap);
}

function cancelSchedule() {
}

function dispose() {
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

