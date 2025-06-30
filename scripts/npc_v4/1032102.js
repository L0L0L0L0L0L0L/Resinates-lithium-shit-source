var status = 0;
var pet = null;
var theitems = Array();

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {

    } else {
        if (mode == 0) {
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendYesNo("#bDo you want to pay 250,000 mesos#k and be transported to #bElin Forest#k? You must be level 150+ to travel there.");
        } else if (status == 1) {
            if (cm.getPlayer().getTotalLevel() >= 150) {
                if (cm.getMeso() >= 250000) {
                    cm.gainMeso(-250000);
                    cm.warp(300000000);
                } else {
                    cm.sendOk("You don't seem to have enough mesos. I am terribly sorry, but I cannot help you unless you pay up. Bring in the mesos by hunting more and come back when you have enough.");
                }
            } else {
                cm.sendOk("You don't seem to have high enough level to travel there. You must be at least level 150 or higher.");
            }
        }
    }
}