var status = 0;
var groupsize = 0;
var item = 4310001;
var ach = 0;
var cost = 0;
var tier = 0;
var level = 0;

function start() {
    if (cm.getPlayer().getParty() == null) {
        cm.sendSimple("Would you like to train in donation zone at the of #b" + cost + " DP#k?\r\n\Each event Lasts for 10 mins.\r\n\Monsters are peaceful with NO drops.\r\n\Select a Tier you wish to train at.#r No refunds for mistakes#k\r\n#L1##bTier 1#k#l\r\n#L2##bTier 2#k#l\r\n#L3##bTier 3#k#l\r\n#L4##bTier 4#l");
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
        cm.sendGetText("Please enter the level of the mob you wish to fight (1-999): \r\n\r\n");
    }
    if (status == 2) {
        amount = Number(cm.getText());
        if (amount >= 1 && amount <= 999) {
            if (cm.haveItem(4310502, cost)) {
                var em = cm.getEventManager("DP_Event");
                if (em != null) {
                    if (!em.startPlayerInstance(cm.getPlayer(), amount, tier)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        if (cost > 0) {
                            cm.gainItem(4310502, -cost);
                        }
                    }
                } else {
                    cm.sendOk("Event has already started, Please wait.");
                }
            } else {
                cm.sendOk("You dont have enough Donation Points for this event.");
            }
        } else {
            cm.sendOk("Stop cheating....");
        }
    }
}