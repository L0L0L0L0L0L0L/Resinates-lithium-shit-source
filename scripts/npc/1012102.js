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
            cm.sendYesNo("Do you wish to learn double jump?");
        } else if (status == 1) {
            cm.getPlayer().changeSingleSkillLevel(3101003, 1);
            cm.sendOk("Double jump has been learned.");
            cm.dispose();
        }
    }
}