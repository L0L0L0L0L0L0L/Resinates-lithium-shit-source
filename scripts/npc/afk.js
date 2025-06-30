var status = 0;
var groupsize = 0;
var item = 4310001;
var ach = 0;

function start() {
    if (!cm.getPlayer().isGroup()) {
        if (cm.haveItem(4310502, 10)) {
            cm.sendYesNo("Would you like to enter AFK map for 10 DP? The event lasts for 8 hours, Life Scrolls do Drop.");
        } else {
            cm.sendOk("You don't have 10 Doantion Points. You can find these by donating and help support this amazing server.");
        }
    } else {
        cm.sendOk("Event is Solo Mode Only.");
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
            var em = cm.getEventManager("player_bot");
            if (em != null) {
                if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getMapId())) {
                    cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                } else {
                    cm.gainItem(4310502, -10);
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        } else {
            cm.sendOk("Expliot detected.");
        }
    }
}