var status = 0;
var groupsize = 0;
var item = 4310001;
var ach = 0;
var cost = 0;
var tier = 0;
var level = 0;

function start() {
    if (cm.getPlayer().getParty() == null) {
        level = parseInt(cm.getPlayer().getTotalLevel() * 1.25);
        var selStr = "";
        selStr += "#rMonster Level: " + Number(level) + "#k\r\n";
        selStr += "#L1##rStamina Cost: 50#k#l\r\n";

        cm.sendSimple("Would you like to train in power zone?\r\n\Each event Lasts for 10 mins.\r\n\Monsters are peaceful with NO drops.\r\n" + selStr);
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
        tier = selection;
        if (tier == 1) {
            cost = 50;
        }
        if (tier == 2) {
            cost = 30;
        }
        if (tier == 3) {
            cost = 40;
        }
        if (tier == 4) {
            cost = 50;
        }
        if (cm.getPlayer().getStamina() >= cost) {
            var em = cm.getEventManager("DP_Event");
            if (em != null) {
                if (!em.startPlayerInstance(cm.getPlayer(), level, tier)) {
                    cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                } else {
                    cm.getPlayer().removeStamina(cost);
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        } else {
            cm.sendOk("Event costs " + cost + " stamina to enter.");
        }
    }
}