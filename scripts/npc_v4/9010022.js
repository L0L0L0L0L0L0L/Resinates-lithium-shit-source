var status;
var sel;
var summon = 4006001;
var towns = new Array(5006, 104000000, 100000000, 101000000, 102000000, 103000000, 120000000,
        105040300, 110000000, 103040000, 200000200, 800000000, 600000000, 211000000, 230000000,
        540000000, 220000000, 260000000, 261000000, 250000000, 251000000, 240000000, 270000000,
        221000000, 222000000, 300000000, 271010000, 273000000, 410000200, 241000120, 86000, 87000,
        310000000, 240090000, 701100000, 410000000, 400000000, 401040000, 310070000, 701210000, 402000000,
        402000500, 402000600, 450001000, 450014050, 450002000, 450015020, 450003000, 450005000, 450006130,
        450007040, 450016000, 450009050, 450009100, 450011120, 450012000, 450012300, 610052100, 865000000, 866000000, 410000300,
        410000401, 410000500, 410004000, 410003010, 410007000, 410007020, 410007500, 410007520, 307000100, 410007600);
var lvl = new Array(10, 10, 10, 10, 10, 10, 10, 15, 30, 30, 30, 30, 30,
        40, 40, 50, 50, 60, 70, 80, 90, 100, 120, 140, 150, 180, 200, 250,
        325, 400, 400, 450, 500, 625, 675, 700, 750, 800, 850, 900, 925, 950, 975, 1000, 1000,
        1100, 1100, 1200, 1300, 1400, 1500, 1500, 1600, 1600, 1800, 2200, 2500, 3000, 4000, 4500, 5000,
        5250, 5500, 6000, 6500, 7000, 7500, 8000, 8500, 9000, 9000);
var ach = new Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 17, 17, 17, 61, 70, 61, 52, 52, 52, 70, 66, 66, 127, 127, 65, 65,
        74, 54, 53, 53, 64, 68, 68, 50, 50, 170, 152, 69, 180, 181, 405, 407, 408, 408, 413, 417, 421, 424, 426, 431, 436, 437, 437, 439);
var bosses = new Array("Starter Zone", "Lith Harbor", "Henesys", "Ellinia Forest", "Perion", "Kerning City", "Nautilus Harbor",
        "Sleepywood", "Florina Beach", "Kenring Mall", "Orbis", "Mushroom Shrine", "New Leaf City",
        "El Nath", "Aquarium", "Singapore", "Ludibrium", "Ariant", "Magatia", "Mu Lung", "Herb Town", "Leafre", "Temple of Time", "Omega Sector",
        "Korean Folk Town", "Ellin Forest", "Future Henesys", "Future Perion", "Cheong", "Kritias", "Root Abyss", "World Tree", "Edelstein",
        "Stone Colossus", "Yu Garden", "Fox Village", "Pantheon", "Heliseum", "Black Heaven", "Shaolin Temple", "Black Market", "Sanctuary", "Verdel",
        "Nameless Town", "Reverse City", "Chu Chu", "Yum Yum", "Lachelein", "Arcana", "Morass", "Esfera", "Sellas", "Outpost", "White Spear", "Labyrinth of Suffering",
        "World Sorrow", "World's End", "Blackgate City", "Commerci", "Arboren", "Ristonia", "Toolen", "Cernium", "Nian", "Arcus", "Odium", "Shangri", "Emiros", "Arteria", "ShadowVale", "Carcion");
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var star2 = " #fEffect/Misc.img/orbs/0# ";

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        var selStr = "";
        var count = 0;
        for (var i = 0; i < bosses.length; i++) {
            if (cm.getPlayer().isGM()) {
                count++;
                selStr += "#b#L" + i + "##fUI/UIWindow2.img/Quest/icon/icon2/0#  [Lvl: " + lvl[i] + "] " + bosses[i] + "#k - " + ach[i] + "#l\r\n";
            } else {
                if (cm.getPlayer().getTotalLevel() >= lvl[i]) {
                    if (ach[i] > 0) {
                        if (cm.getPlayer().getAchievement(ach[i])) {
                            count++;
                            selStr += "#b#L" + i + "##fUI/UIWindow2.img/Quest/icon/icon2/0#  [Lvl: " + lvl[i] + "] " + bosses[i] + "#k - " + ach[i] + "#l\r\n";
                        } else {
                            selStr += "#r#L" + (100 + i) + "##fUI/UIWindow2.img/Quest/icon/icon6/0#  [Lvl: " + lvl[i] + "] [Locked] " + bosses[i] + "#k - " + ach[i] + "#l\r\n";
                        }
                    } else {
                        selStr += "#b#L" + i + "##fUI/UIWindow2.img/Quest/icon/icon2/0#  [Lvl: " + lvl[i] + "] " + bosses[i] + "#k - " + ach[i] + "#l\r\n";
                    }
                } else {
                    selStr += "#r#L" + (100 + i) + "##fUI/UIWindow2.img/Quest/icon/icon6/0#  [Lvl: " + lvl[i] + "] " + bosses[i] + "#k - " + ach[i] + "#l\r\n";
                }
            }
        }
        cm.sendSimple("Which town would you like to quick warp to?\r\n\r\n" + selStr + "  ");
    } else if (status == 1) {
        if (selection >= 100) {
            var selStr = "This Town Requires:\r\n\r\n";
            if (lvl[selection - 100] > 0) {
                selStr += "     " + star + " Level: " + lvl[selection - 100] + " \r\n";
            }
            if (ach[selection - 100] > 0) {
                selStr += "     " + star + " Achievement: #r" + cm.getPlayer().getAchievementInfo(ach[(selection - 100)]).getName() + " #k";
            }

            cm.sendOk(selStr);
        } else {
            cm.warp(towns[selection]);
        }
    }
}











