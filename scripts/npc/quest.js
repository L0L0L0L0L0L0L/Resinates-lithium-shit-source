var status = 0;
var groupsize = 0;
var item = 4036088;
var amount = 5;
var ach = 0;
var cost = 0;
var mob = 0;
var tier = 0;
var star = "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";

function start() {
    cm.sendNextS("Welcome to helper guide...", 2);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status == 0) {
            return;
        }
        status--;
    }
    if (status == 1) {
        var selStr = "\r\n";
        if (!cm.getPlayer().getAchievement(205)) {
            selStr += "  " + star + " Collect Rewards from Donation Box (12 hour cycle),\r\n";
            selStr += "  " + star + " Take (#r" + cm.getItemName(4310018) + "'s#k) over to #bSpecial Shop#k to left of me, and buy 50 or so #rGenesis Scrolls#k\r\n";
            selStr += "  " + star + " Use the Genesis scrolls on your #bZombie Army Rings#k\r\n";
            selStr += "  " + star + " Head over to Room 6 (Training Room) Begin Clearing out Easy mode\r\n";
            cm.sendOkS("Your first goal here is working on Completing Tutorial #bEasy#k zone,\r\n" + selStr, 2);
            return;
        }
        if (!cm.getPlayer().getAchievement(216)) {
            selStr += "  " + star + " Remember you can head over Room 4 (Equip Room) to upgrade your equips!\r\n";
            cm.sendOkS("Your next goal on here is working on Completing Tutorial #bNormal#k zone,\r\n" + selStr, 2);
            return;
        }
        if (!cm.getPlayer().getAchievement(226)) {
            selStr += "  " + star + " Remember you can head over Room 4 (Equip Room) to upgrade your equips!\r\n";
            cm.sendOkS("Your next goal on here is working on Completing Tutorial #bHard#k zone,\r\n" + selStr, 2);
            return;
        }
        if (!cm.getPlayer().getAchievement(270)) {
            selStr += "  " + star + " Run over to Party-Zone Npc to start working on clearing these modes, these modes will reward you with new equips. The Deeper you go the stronger the equips.\r\n";
            selStr += "  " + star + " Remember you can head over Room 4 (Equip Room) to upgrade your equips!\r\n";
            selStr += "  " + star + " If you run low on stamina you can always donate for #bGigalixers#k or head over to Tutorial Zone (#bHell Mode#k) to collect reward tickets and cash them into reward zone.\r\n";
            cm.sendOkS("Your next goal on here is working on Completing Party-Zone #bEasy#k Mode,\r\n" + selStr, 2);
            return;
        }
        if (!cm.getPlayer().getAchievement(271)) {
            selStr += "  " + star + " Run over to Party-Zone Npc to start working on clearing these modes, these modes will reward you with new equips. The Deeper you go the stronger the equips.\r\n";
            selStr += "  " + star + " Remember you can head over Room 4 (Equip Room) to upgrade your equips!\r\n";
            selStr += "  " + star + " If you run low on stamina you can always donate for #bGigalixers#k or head over to Tutorial Zone (#bHell Mode#k) to collect reward tickets and cash them into reward zone.\r\n";
            cm.sendOkS("Your next goal on here is working on Completing Tutorial #bNormal#k zone,\r\n" + selStr, 2);
            return;
        }
        if (!cm.getPlayer().getAchievement(272)) {
            selStr += "  " + star + " Run over to Party-Zone Npc to start working on clearing these modes, these modes will reward you with new equips. The Deeper you go the stronger the equips.\r\n";
            selStr += "  " + star + " Remember you can head over Room 4 (Equip Room) to upgrade your equips!\r\n";
            selStr += "  " + star + " If you run low on stamina you can always donate for #bGigalixers#k or head over to Tutorial Zone (#bHell Mode#k) to collect reward tickets and cash them into reward zone.\r\n";
            cm.sendOkS("Your next goal on here is working on Completing Tutorial #bNormal#k zone,\r\n" + selStr, 2);
            return;
        }
        if (!cm.getPlayer().getAchievement(273)) {
            selStr += "  " + star + " Run over to Party-Zone Npc to start working on clearing these modes, these modes will reward you with new equips. The Deeper you go the stronger the equips.\r\n";
            selStr += "  " + star + " Remember you can head over Room 4 (Equip Room) to upgrade your equips!\r\n";
            selStr += "  " + star + " If you run low on stamina you can always donate for #bGigalixers#k or head over to Tutorial Zone (#bHell Mode#k) to collect reward tickets and cash them into reward zone.\r\n";
            cm.sendOkS("Your next goal on here is working on Completing Tutorial #bNormal#k zone,\r\n" + selStr, 2);
            return;
        }
        selStr += "  " + star + " Remember if your lost or need help head over to discord server and check out everything!\r\n";
        cm.sendOkS("You have completed all the basics, keep farming and getting stronger. Remeber if you run slow on stamina you can always donate to buy #rGiglixers#k or farm #bHell Mode under tutorial zones for reward tickets and cashing them into reward zone.#k\r\n" + selStr, 2);
    }
    if (status == 2) {

    }
    if (status == 3) {

    }
}