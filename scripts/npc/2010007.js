/* guild creation npc */
var status = -1;
var sel;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && status == 0) {

        return;
    }
    if (mode == 1)
        status++;
    else
        status--;

    if (status == 0)
        if (cm.getPlayerStat("GID") > 0) {
            if (cm.getPlayer().getGuildRank() == 1) {
                cm.sendSimple("What would you like to do?\r\n#L2#Increase your Guild's capacity +5 (limited to 250)#l#k\r\n\r\n\r\n#L1##rDisband your Guild#k#l");
            } else {
                cm.sendOk("Only leader of guild can talk to me.");
            }
        } else {
            cm.sendSimple("What would you like to do?\r\n#b#L0#Create a Guild?#l#k");
        }
    else if (status == 1) {
        sel = selection;
        if (selection == 0) {
            if (cm.getPlayerStat("GID") > 0) {
                cm.sendOk("You may not create a new Guild while you are in one.");
            } else {
                cm.sendYesNo("Creating a Guild costs #b25,000 mesos#k, are you sure you want to continue?");
            }
        } else if (selection == 1) {
            if (cm.getPlayerStat("GID") <= 0 || cm.getPlayerStat("GRANK") != 1) {
                cm.sendOk("You can only disband a Guild if you are the leader of that Guild.");

            } else
                cm.sendYesNo("Are you sure you want to disband your Guild? You will not be able to recover it afterward and all your GP will be gone.");
        } else if (selection == 2) {
            if (cm.getPlayerStat("GID") <= 0 || cm.getPlayerStat("GRANK") != 1) {
                cm.sendOk("You can only increase your Guild's capacity if you are the leader.");

            } else
                cm.sendYesNo("Increasing your Guild capacity by #b5#k costs #b5,000 mesos#k, are you sure you want to continue?");
        } else if (selection == 3) {
            if (cm.getPlayerStat("GID") <= 0 || cm.getPlayerStat("GRANK") != 1) {
                cm.sendOk("You can only increase your Guild's capacity if you are the leader.");

            } else
                cm.sendYesNo("Increasing your Guild capacity by #b5#k costs #b25,000 GP#k, are you sure you want to continue?");
        }
    } else if (status == 2) {
        if (sel == 0 && cm.getPlayerStat("GID") <= 0) {
            cm.genericGuildMessage(1);
            cm.dispose();

        } else if (sel == 1 && cm.getPlayerStat("GID") > 0 && cm.getPlayerStat("GRANK") == 1) {
            cm.disbandGuild();
            cm.dispose();

        } else if (sel == 2 && cm.getPlayerStat("GID") > 0 && cm.getPlayerStat("GRANK") == 1) {
            cm.increaseGuildCapacity(false);
            cm.dispose();

        } else if (sel == 3 && cm.getPlayerStat("GID") > 0 && cm.getPlayerStat("GRANK") == 1) {
            cm.increaseGuildCapacity(true);
            cm.dispose();

        }
    } else if (status == 3) {
        cm.dispose();
    }
}