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
importPackage(Packages.client);
importPackage(Packages.tools);
importPackage(Packages.server.life);
importPackage(Packages.server);


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
var boss = new Array(9305300, 9305301, 9305302, 9305303, 9305304, 9305305, 9305306, 9305307, 9305308, 9305309, 9305310, 9305311, 9305312, 9305313, 9305314, 9305315, 9305316, 9305317, 9305318, 9305319, 9305320, 9305321, 9305322, 9305323, 9305324, 9305325, 9305326, 9305327, 9305328, 9305329, 9305330, 9305331, 9305332, 9305333, 9305334, 9305335, 9305336, 9305337, 9305338, 9305339, 9833380, 9833381, 9833382, 9305672, 9305674, 9305675, 9305676, 9305677, 8644011, 8642016, 2600800, 8220011, 8220012, 8645009, 8240099, 8800400, 8840000, 8850011, 8860000, 8880000, 8880101, 8880150, 8880302, 8880405, 8930100, 9420620, 9601068, 9421583, 9480235, 9480236, 9480237, 9480238, 9480239, 9390812, 9390822, 9390915);

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
    return eligible;
}

//setup --------------------------------------------------------------------

function setLobbyRange() {
    return lobbyRange;
}

function init() {
}

function setup(player, level) {
    var eim = em.newInstance(player, "Dojo_Unlimited_" + player.getName(), true);
    eim.setValue("scale", 1);
    eim.setValue("level", player.getTotalLevel());
    eim.setValue("finished", 0);
    eim.setValue("wave", 0);
    eim.setValue("deadly", 1);
    eim.setScale(false);
    eim.schedule("start", 10 * 1000);
    eim.createEventTimer(10 * 1000);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(eim.getMapInstance(eventMapId), 0);
    player.dropMessage(6, "[Dojo] Event will begin in 10 seconds.");
    player.dropMessage(6, "[Dojo] Each Boss defeated increases the Tier of next boss.");
    player.dropMessage(6, "[Dojo] Endless Mode Battle Level: " + eim.getValue("level"));
    player.dropMessage(6, "[Dojo] Watch out for deadly Bombs that appear.");
}

//event --------------------------------------------------------------------

function start(eim) {
    eim.startEventTimer(60 * 60000);//1hr
    eim.changeMusic("BgmFF7/Weapon_Raid");
    var map = eim.getMapInstance(eventMapId);
    eim.dropMessage(6, "[Dojo] Battle has begun. Good Luck!!!");
    eim.setValue("wave", 1);
    var randomBoss = boss[eim.getRandom(0, 75)];
    var mob = eim.getKaoticMonster(randomBoss, eim.getValue("level"), eim.getValue("wave") + 4, true, false, false, true, 5);
    eim.dropMessage(6, "[Dojo] Round: " + eim.getValue("wave") + " Monster Tier: " + (eim.getValue("wave") + 4) + " - Monster: " + mob.getStats().getName());
    eim.getMapInstance(eventMapId).spawnMonsterWithEffectBelow(mob, 15, eim.newPoint(eim.getRandom(-250, 250), 0));
}

function finish(eim) {
    eim.exitParty(exitMap);
}

//timer ending --------------------------------------------------------------------
function scheduledTimeout(eim) {
    if (eim.getOwner() != null && eim.getOwner().compareVar("Dojo", eim.getValue("wave"))) {
        eim.getOwner().setSavedVar("Dojo", eim.getValue("wave"));
        eim.getOwner().dropMessage(6, "[Dojo] New High Score Saved! Fininshed: " + eim.getValue("wave") + " Waves.");
    }
    eim.exitParty(exitMap);
}

//monsters--------------------------------------------------------------------

function monsterKilled(mob, eim) {
    if (eim.isDojoBoss(mob.getId())) {
	eim.levelUpMax(250, 5000);
        eim.gainDojoMobExp(mob.getStats().getScale() * mob.getStats().getScale() * eim.getValue("level"), mob);
        eim.gainPartyEtc(4310054, eim.getValue("wave") * mob.getStats().getScale());
        eim.setValue("wave", eim.getValue("wave") + 1);
        var randomBoss = boss[eim.getRandom(0, 75)];
        var mob = eim.getKaoticMonster(randomBoss, eim.getValue("level"), eim.getValue("wave") + 4, true, false, false, true, eim.getValue("wave") * 5);
        eim.dropMessage(6, "[Dojo] Round: " + eim.getValue("wave") + " Monster Tier: " + (eim.getValue("wave") + 4) + " - Monster: " + mob.getStats().getName());
        eim.getMapInstance(eventMapId).spawnMonsterWithEffectBelow(mob, 15, eim.newPoint(eim.getRandom(-250, 250), 0));
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
    if (player.compareVar("Dojo", eim.getValue("wave"))) {
        player.setSavedVar("Dojo", eim.getValue("wave"));
        player.dropMessage(6, "[Dojo] New High Score Saved! Fininshed: " + eim.getValue("wave") + " Waves.");
    }
    eim.exitPlayer(player, exitMap);
}

function clear(eim) {
}

function changedMap(eim, player, mapid) {
    if (mapid != eventMapId) {
        //eim.unregisterPlayer(player);
        if (player.compareVar("Dojo", eim.getValue("wave"))) {
            player.setSavedVar("Dojo", eim.getValue("wave"));
            player.dropMessage(6, "[Dojo] New High Score Saved! Fininshed: " + eim.getValue("wave") + " Waves.");
        }
        eim.exitPlayer(player, exitMap);
    }
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    if (player.compareVar("Dojo", eim.getValue("wave"))) {
        player.setSavedVar("Dojo", eim.getValue("wave"));
        player.dropMessage(6, "[Dojo] New High Score Saved! Fininshed: " + eim.getValue("wave") + " Waves.");
    }
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

