var status = 0;
var level = 250;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function start() {
    if (!cm.getPlayer().isGroup()) {
        if (cm.getPlayer().getStamina() >= 50) {
            var text = "";
            text += "#L1# #bAscendion#k#l\r\n";
            text += "#L2# #bForeberion#k#l\r\n";
            text += "#L3# #bEmbrion#k#l\r\n";

            cm.sendSimple("Would you like to enter my secret training ground?\r\nIf so, which Monster would you like to train with\r\n#rThe cost to enter is 50 Stamina#k.\r\n" + text);
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
        var em = cm.getEventManager("End_Farm");
        if (em != null) {
            if (cm.getPlayer().achievementFinished(181)) {
                var mob;
                if (selection == 1) {
                    mob = 8645040;
                } else if (selection == 2) {
                    mob = 8645041;
                } else if (selection == 3) {
                    mob = 8645042;
                }
                if (!em.startPlayerInstance(cm.getPlayer(), mob)) {
                
                    cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                } else {
                    cm.getPlayer().removeStamina(50);
                }
            } else {
                cm.sendOk("This Event requires clearing Commander Will to enter.");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }

    }
}