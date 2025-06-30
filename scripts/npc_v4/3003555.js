var status = 0;
var level = 250;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function start() {
    if (!cm.getPlayer().isGroup()) {
        if (cm.getPlayer().getStamina() >= 50) {
            cm.sendYesNo("Would you like to enter my secret training ground at the cost of 50 Stamina?");
        } else {
            cm.sendOk("You do not have enough stamina to enter the event, the cost is 50 Stamina.");
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
        var em = cm.getEventManager("Will_Farm");
        if (em != null) {
            if (cm.getPlayer().achievementFinished(50)) {
                if (!em.startPlayerInstance(cm.getPlayer(), 1500)) {
                    cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                } else {
                    cm.getPlayer().removeStamina(50);
                }
            } else {
                cm.playerMessage(5, "This Event requires clearing Normal Will to enter.");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }
    }
}