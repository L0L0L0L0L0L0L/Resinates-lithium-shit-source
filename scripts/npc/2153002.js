var status;

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
            if (cm.getPlayer().getTotalLevel() >= 250) {
                cm.sendYesNo("Goto Deep Mineshaft?");
            } else {
                cm.sendOk("You need to be a miniuim of level 250 to enter.");
                            
            }
        } else if (status == 1) {
            cm.warp(310050000);
        }
}