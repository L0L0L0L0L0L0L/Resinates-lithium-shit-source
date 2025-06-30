var status = 0;
var level = 250;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function start() {
    if (cm.getPlayer().getAchievement(155)) {
        cm.sendYesNo("Psst.... You wanna do some gambling?");
    } else {
        cm.sendOk("Go see Mr. Hazard.");
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
        cm.warp(2000);
    }
}