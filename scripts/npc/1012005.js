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
            cm.sendYesNo("Do you wish to learn Multi Pet Skill (#rFollow the Leader#k)?");
        } else if (status == 1) {
            cm.getPlayer().changeSingleSkillLevel(8, 1);
            cm.getPlayer().changeSingleSkillLevel(10000018, 1);
            cm.getPlayer().changeSingleSkillLevel(20000024, 1);
            cm.getPlayer().changeSingleSkillLevel(20011024, 1);
            cm.getPlayer().changeSingleSkillLevel(20021024, 1);
            cm.getPlayer().changeSingleSkillLevel(30001024, 1);
            cm.getPlayer().changeSingleSkillLevel(30011024, 1);
            cm.sendOk("Multi Pet Skill (#rFollow the Leader#k) has been learned.");
            cm.dispose();
        }
    }
}