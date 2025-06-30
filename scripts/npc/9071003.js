
var status = 0;
var option = 0;

function start() {
    if (cm.getPlayer().getTotalLevel() >= 250) {
        var text = "Which Party Quest would you like to travel to?\r\n";
        text += "#L4# Ancient City PQ#l\r\n";
        text += "#L7# Halloween PQ#l\r\n";
        text += "#L10# Terra PQ#l\r\n";
        if (cm.getPlayer().getTotalLevel() >= 200) {
            //text += "#L11# Fel Monster Park#l\r\n";
        }
        if (cm.getPlayer().getTotalLevel() >= 1000 && cm.getPlayer().getAchievement(64)) {
            text += "#L6# Lucid PQ#l\r\n";
        }
        if (cm.getPlayer().getTotalLevel() >= 900 && cm.getPlayer().getAchievement(155)) {
            //text += "#L9# Casino#l\r\n";
        }
        cm.sendSimple(text);
    } else {
        cm.sendOk("You currently are not level 150 or higher to access this.");
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        if (selection == 1) {
            cm.warp(951000000);
        }
        if (selection == 2) {
            cm.warp(970030000);
        }
        if (selection == 3) {
            cm.warp(925020001);
        }
        if (selection == 4) {
            cm.warp(4008);
        }
        if (selection == 5) {
            cm.warp(960000000);
        }
        if (selection == 6) {
            cm.warp(4100);
        }
        if (selection == 7) {
            cm.warp(4400);
        }
        if (selection == 8) {
            cm.warp(925020001);
        }
        if (selection == 9) {
            cm.warp(2000);
        }
        if (selection == 10) {
            cm.warp(4500);
        }
        if (selection == 11) {
            cm.warp(46000);
        }


    }
    cm.dispose();
}