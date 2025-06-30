var status = 0;
var groupsize = 0;
var item = 4034867;
var itemName = "#i4034867#";
var ach = 438;
var cost = 0;
var multi = 1;

function start() {
    if (cm.getPlayer().getAccVara("KP_BOOST") < 250) {
        var morale = cm.getPlayer().getAccVara("Morale");
        if (morale >= 2500) {
            var kp = cm.getPlayer().getAccVara("KP_BOOST");
            cm.sendYesNo("Do you want to exchange #b2,500#k Morale for #b1 Kaotic Point#k?\r\nYou currently have #b" + morale + "#k Morale\r\nYou currently have #b" + kp + "#k KP Rate.");
        } else {
            cm.sendOk("You must have #r2,500#k or more morale to use my services.\r\nYou currently have #b" + morale + "#k Morale");
        }
    } else {
        cm.sendOk("Seems that you have maxxed out your KP Rate.");
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
        var m = cm.getPlayer().getAccVara("Morale");
        if (m >= 2500) {
            var kp = cm.getPlayer().getAccVara("KP_BOOST");
            var amount = (m - 2500);
            cm.getPlayer().setAccVar("Morale", amount);
            cm.getPlayer().setAccVar("KP_BOOST", kp + 1);
            cm.getPlayer().updateStats();
            cm.sendOk("You have converted 2,500 Morale into #b1 Kaotic Point#k.");
        } else {
            cm.sendOk("You do not have enough morale for this action.");
        }

    }
}