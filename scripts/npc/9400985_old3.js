var status = 0;
var groupsize = 0;
var item = 4310502;
var ach = 0;
var option = 0;
var em;

function start() {
    if (!cm.getPlayer().isGroup()) {
        var selStr = "Which reward would you like to buy?\r\n";
        selStr += ("#L6#Grand Rewards - #b100 DP#k#l\r\n");
        selStr += ("#L7#Extreme Rewards - #b1000 DP#k#l\r\n");
        cm.sendSimple(selStr);
    } else {
        cm.sendOk("Event is Solo Only.");
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
        option = selection;
        if (option == 1) {
            cost = 5;
        }
        if (option == 2) {
            cost = 10;
        }
        if (option == 3) {
            cost = 15;
        }
        if (option == 4) {
            cost = 20;
        }
        if (option == 5) {
            cost = 25;
        }
        if (option == 6) {
            cost = 100;
        }
        if (option == 7) {
            cost = 1000;
        }
        if (cm.haveItem(item, cost)) {
            cm.sendYesNo("Are you sure you want to cash in #i" + item + "# (#bx" + cost + "#k) for this reward?");
        } else {
            cm.sendOk("Event is requires at least " + cost + "x #i" + item + "#.");
        }

    } else if (status == 2) {
        if (!cm.getPlayer().isGroup()) {
            if (cm.haveItem(item, cost)) {
                var em = cm.getEventManager("reward_dp");
                if (em != null) {
                    if (!em.startPlayerInstance(cm.getPlayer(), 1, option)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(item, -cost);
                    }
                } else {
                    cm.sendOk("Event has already started, Please wait.");
                }
            } else {
                cm.sendOk("Event is requires at least 1 of #i" + item + "#.");
            }
        } else {
            cm.sendOk("Event is Solo Only.");
        }
    }
}