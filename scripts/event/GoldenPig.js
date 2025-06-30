/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 
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
 -- Odin JavaScript --------------------------------------------------------------------------------
 Bamboo Warrior Spawner
 -- Edited by --------------------------------------------------------------------------------------
 Ronan (based on xQuasar's King Clang spawner)
 
 **/

var random = 0;
var towns = new Array(100000004, 100040003, 100040106, 101020004, 101020008, 101030103, 101030108, 101030405, 101040003, 102020300, 103000202, 103000105, 103030200, 104010001, 104040002, 105040305, 105050300, 105070002, 105090310, 105090700, 106000140, 106010106, 107000403, 140020200, 200010300, 200050000, 211040000, 211040900, 211041700, 220010200, 220010900, 220020200, 220030100, 220060201, 220070301, 221021200, 221023200, 221024200, 221030200, 221040300, 222010102, 230020300, 230040300, 240010600, 240030300, 240040521, 250010303, 250010700, 251010200, 251010402, 261020300, 261010103, 300010200, 300020100, 800020101, 801020000, 98002, 270010100, 270020100, 270030100, 82052, 271010200, 271030102, 271010400, 273020300, 241000223, 310020200);

function init() {
    scheduleNew();
}

function scheduleNew() {
    em.schedule("start", em.getRandom(60, 480) * 60000);    //spawns upon server start. Each 3 hours an server event checks if boss exists, if not spawns it instantly.
}

function start() {
    var random = em.getRandom(0, towns.length - 1);
    var town = towns[random];
    var mapObj = em.getChannelServer().getMapFactory().getMap(town);
    var spawnpoint = mapObj.getRandomMonsterSpawnPoint();
    var mob = em.getMonster(9302038, mapObj.getSpawnPointLevel(spawnpoint), 8);
    mob.setForceBar();
    mapObj.spawnMonsterOnGroundBelow(mob, spawnpoint.getPosition());
    em.broadcastYellowMsg("[EVENT] A Golden Pig has been spotted! Go find him for a massive reward.");
    em.schedule("start", em.getRandom(480, 960) * 60000);
}