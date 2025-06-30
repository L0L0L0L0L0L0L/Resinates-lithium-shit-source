var status = 0;
var level = 250;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function start() {
    if (cm.getPlayer().achievementFinished(152)) {
        cm.sendYesNo("Need a ride over to hidden training ground?");
    } else {
        cm.sendOk("Sorry i can only offer a ride to those who slayed Dark Slime Boss.");
    }
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
        if (cm.getPlayer().getMapId() == 450009100) {//intro
            cm.sendOk("Sorry there is no hidden map on this floor.");
        }
        if (cm.getPlayer().getMapId() == 450009200) {//intro
            cm.warp(450009202);
        }
        if (cm.getPlayer().getMapId() == 450009300) {//intro
            cm.warp(450009302);
        }

    }
}