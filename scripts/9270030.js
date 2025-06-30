
var status = 0;
var summon = 4006001;
var towns = new Array(230040410, 551030100, 801040004, 211042400, 270050000, 98006, 82100, 271040000, 86043, 240093200, 87300, 701220600, 401060000, 350060300, 450001230, 450014240, 450002010, 450003600,
        450005400, 450006440, 450007240, 450009300, 450011580, 450011990, 450012200, 450012300, 450012500, 610052100, 865020500, 865030500, 866000150, 410000352, 450016270, 410000414, 410000690, 410000680, 410000680,
        410000820, 410004003, 410004001, 410004001, 410004000, 410004010, 410003030, 410003000, 410005005, 410007011, 410007018, 410007018, 410007018, 410007025, 410007025, 410007025, 410007025, 410007025, 410007025,
        410007534, 410007545, 410007545, 410007610, 307000340, 410007601);
var ach = new Array(0, 120, 140, 150, 160, 200, 250, 300, 400, 500, 600, 750, 800, 900, 950, 1000, 1100, 1200, 1300, 1400, 1500,
        1600, 1700, 1900, 2100, 2300, 2500, 3000, 4000, 4200, 4500, 5000, 5000, 5000, 5100, 5300, 5500, 5700, 5900, 6000, 6200, 6300, 6400,
        6600, 6800, 7000, 7200, 7400, 7600, 7800, 8000, 8100, 8200, 8300, 8400, 8400, 8500, 8600, 8700, 8800, 8900, 9000, 9000);
var ach2 = new Array(0, 0, 0, 0, 0, 17, 75, 51, 39, 61, 52, 70, 127, 66, 65, 74, 54, 53, 64, 68, 68, 50, 152, 69, 69, 180, 181, 400, 405, 406, 407, 408, 411,
        412, 413, 414, 415, 416, 417, 418, 419, 420, 421, 422, 423, 424, 425, 426, 427, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 439, 439, 440);
var bosses = new Array("[T:5] Pianus", "[T:5] Targa", "[T:6] Big Boss", "[T:7] Zakum", "[T:10] Pinkbean", "[T:12] Arkarium", "[T:15] Von Leon", "[T:20] Empress", "[T:25] Vellum", "[T:27] Tarantulus", "[T:30] Damien", "[T:32] Temple Chief",
        "[T:35] Magnus", "[T:40] Lotus", "[T:41] Arma", "[T:42] Singularity", "[T:43] Slurpy", "[T:45] Lucid", "[T:47] Harmony", "[T:48] Arkarium V2", "[T:50] Will", "[T:55] Black Slime", "[T:58] Necro Bosses", "[T:60] Evil Hilla", "[T:65] Darknell",
        "[T:70] Commander Will", "[T:72] Maru", "[T:75] Khan", "[T:70] Riverson", "[T:70] Capo", "[T:80] Kobold King");
var cost = new Array(100, 100, 250, 250, 250, 250, 500, 500, 500, 750, 750, 750, 1000, 1000, 1250, 1250, 1250,
        2500, 2500, 2500, 2500, 3500, 5000, 5000, 5000, 5000, 5000, 15000, 25000, 50000, 100000);


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
            if (cm.getPlayer().isGM() || cm.getPlayer().getTotalLevel() >= ach[i]) {
                if (cm.getPlayer().isGM() || ach2[i] == 0 || cm.getPlayer().getAchievement(ach2[i])) {
                    count++;
                    selStr += "#b#L" + i + "##fUI/UIWindow2.img/Quest/icon/icon2/0#  " + bosses[i] + " - (L: " + ach[i] + ")#k - " + ach2[i] + "#l\r\n";
                } else {
                    selStr += "#r#L" + (100 + i) + "##fUI/UIWindow2.img/Quest/icon/icon6/0#  " + bosses[i] + " - (L: " + ach[i] + ")#k - " + ach2[i] + "#l\r\n";
                }
            } else {
                selStr += "#r#L" + (100 + i) + "##fUI/UIWindow2.img/Quest/icon/icon6/0#  " + bosses[i] + " - (L: " + ach[i] + ")#k - " + ach2[i] + "#l\r\n";
            }
        }
        cm.sendSimple("Which boss would you like to quick warp to?\r\n\r\n" + selStr + "  ");
    } else if (status == 1) {
        if (selection >= 100) {
            if (cm.getPlayer().getAchievementInfo(ach2[(selection - 100)]) != null) {
                cm.sendOk("Seems you dont have this warp unlocked, Requires:\r\n#r " + cm.getPlayer().getAchievementInfo(ach2[(selection - 100)]).getName() + " #k");
            } else {
                cm.sendOk("Seems you dont have enough pc super power to control the world, please tell daddy.");
            }
        } else {
            cm.warp(towns[selection]);
        }
    }
}
