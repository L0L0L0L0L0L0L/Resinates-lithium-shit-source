var status = 0;
var level = 250;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function start() {
    if (cm.getPlayer().achievementFinished(180)) {
        cm.sendYesNo("Need a ride over to hidden training ground?");
    } else {
        cm.sendOk("Sorry i can only offer a ride to those who slayed Darknell.");
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
        cm.warp(450012001);
    }
}