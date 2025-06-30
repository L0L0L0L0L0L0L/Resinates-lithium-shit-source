var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            cm.sendYesNo("Do you wish to leave?");
        } else if (status == 1) {
            if (cm.getPlayer().getEventInstance() != null) {
                cm.getPlayer().getEventInstance().exitPlayer(cm.getPlayer());
            } else {
                cm.warp(46000);
            }
            cm.dispose();
        }
    }
}