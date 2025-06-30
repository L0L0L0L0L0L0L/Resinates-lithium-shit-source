var status = 0;
var groupsize = 0;
var item = 4420015;
var ach = 0;
var option = 0;
var em;
var cost = 1;

function start() {
    if (cm.haveItem(item, cost)) {
        cm.sendYesNo("Are you sure you want to cash in x" + cost + " #i" + item + "#?");
    } else {
        cm.sendOk("Event is requires at least " + cost + " of #i" + item + "#.");
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
        if (!cm.getPlayer().isGroup()) {
            if (cm.haveItem(item, cost)) {
                var em = cm.getEventManager("reward");
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
                cm.sendOk("Event is requires at least " + cost + " of #i" + item + "#.");
            }
        } else {
            cm.sendOk("Event is Solo Only.");
        }
    }
}