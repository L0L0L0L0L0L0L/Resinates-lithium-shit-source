/* guild emblem npc */
var status = 0;
var sel;

function start() {
    status = -1;
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

    if (status == 0) {
        if (cm.getPlayerStat("GID") > 0 && cm.getPlayer().getGuildRank() == 1) {
            cm.sendSimple("What would you like to do?\r\n#b#L0#Create/Change your Guild Emblem#l#k");
        } else {
            cm.sendOk("Only leader of guild can talk to me.");
        }
    } else if (status == 1) {
        sel = selection;
        if (selection == 0) {
            if (cm.getPlayerStat("GRANK") == 1)
                cm.sendYesNo("Creating or changing Guild Emblem costs #b1,500,000 mesos#k, are you sure you want to continue?");
            else
                cm.sendOk("You must be the Guild Leader to change the Emblem.  Please tell your leader to speak with me.");
        }
    } else if (status == 2) {
        if (sel == 0) {
            cm.genericGuildMessage(18);

        }
    }
}
