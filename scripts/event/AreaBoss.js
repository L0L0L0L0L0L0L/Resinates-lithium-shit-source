var towns;
var spawns;
var x;
var y;

function init() {
    towns = new Array(0, 251010102, 260010201, 107000300, 200010300, 100040105, 100040106, 261030000, 110040000, 240040401, 104000400, 222010310, 230020100, 105090310, 101030404, 250010304, 220050100, 220050000, 220050200, 221040301);
    spawns = new Array(0, 5220004, 3220001, 6220000, 8220000, 5220002, 5220002, 8220002, 5220001, 8220003, 2220000, 7220001, 4220001, 8220008, 3220000, 7220000, 5220003, 5220003, 5220003, 6220001);
    x = new Array(0, 560, 645, 90, 208, 456, 474, -300, 200, 0, 400, 0, 0, -626, 800, -300, -300, 0, 0, -4224);
    y = new Array(0, 50, 275, 119, 83, 278, 278, 180, 140, 1125, 455, 33, 520, -604, 1280, 390, 1030, 1030, 1030, 776);
    em.schedule("start", 60000);
}

function start() {
    for (var z = 1; z <= 19; z++) {
        var mapObj = em.getChannelServer().getMapFactory().getMap(towns[z]);
        if (mapObj.countMonsterById(spawns[z]) == 0) {
            var sp = mapObj.getRandomMonsterSpawnPoint();
            if (sp != null) {
                var mobObj = em.getMonster(spawns[z], mapObj.getSpawnPointLevel(sp), 3);
                mobObj.setForceBar();
                mapObj.forceSpawnMonster(mobObj, sp.getPosition());
            } else {
                var mobObj = em.getMonsterNoLinkRank(spawns[z], 3);
                mobObj.setForceBar();
                mapObj.forceSpawnMonster(mobObj, em.newPoint(x[z], y[z]));
            }
        }
    }
    em.schedule("start", 1800000);
}

// ---------- FILLER FUNCTIONS ----------

function dispose() {
}

function setup(eim, leaderid) {
}

function monsterValue(eim, mobid) {
    return 0;
}

function disbandParty(eim, player) {
}

function playerDisconnected(eim, player) {
}

function playerEntry(eim, player) {
}

function monsterKilled(mob, eim) {
}

function scheduledTimeout(eim) {
}

function afterSetup(eim) {
}

function changedLeader(eim, leader) {
}

function playerExit(eim, player) {
}

function leftParty(eim, player) {
}

function clearPQ(eim) {
}

function allMonstersDead(eim) {
}

function playerUnregistered(eim, player) {
}