var status = 0;
var level = 0;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var option = 0;
var items = new Array(4032868, 4032873, 4033322);
var amount = new Array(1000, 1000, 100);
var reward = 4001886;
var rewamount = 10;

function start() {
    if (cm.getPlayer().getMapId() == 450009300) {
        level = 1600;
        option = 1;
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                if (cm.getPlayer().getTotalLevel() >= level) {
                    cm.sendYesNo("Are you ready to take down evil slime boss thats causing all this trouble here?");
                } else {
                    cm.sendOk("The leader must be level " + level + " or higher to complete this event.");
                }
            } else {
                cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
            }
        } else {
            cm.sendOk("Event is Party Only.");
        }
    } else if (cm.getPlayer().getMapId() == 450011320) {
        option = 2;
        var selStr = "";
        for (var i = 0; i < items.length; i++) {
            selStr += "#i" + items[i] + "#  #b" + cm.getItemName(items[i]) + "#k (#rx" + amount[i] + "#k) (#b" + cm.convertNumber(cm.getPlayer().countAllItem(items[i])) + "#k)\r\n\ ";
        }
        cm.sendYesNo("I need the following materials to craft 10 #i" + reward + "#: \r\n\r\n\ " + selStr);
    } else if (cm.getPlayer().getMapId() == 450012300) {
        level = 2500;
        option = 3;
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                if (cm.getPlayer().getTotalLevel() >= level) {
                    cm.sendYesNo("Are you ready to take down Commander Will thats causing all this trouble here?");
                } else {
                    cm.sendOk("The leader must be level " + level + " or higher to complete this event.");
                }
            } else {
                cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
            }
        } else {
            cm.sendOk("Event is Party/Raid Only.");
        }
    } else {
        cm.sendOk("Have a good day.");
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        if (option == 1) {
            var em = cm.getEventManager("Moonbridge");
            if (em != null) {
                if (!em.startPlayerInstance(cm.getPlayer())) {
                    cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        } else if (option == 2) {
            if (cm.haveItem(items[0], amount[0]) && cm.haveItem(items[1], amount[1]) && cm.haveItem(items[2], amount[2])) {
                cm.gainItem(items[0], -amount[0]);
                cm.gainItem(items[1], -amount[1]);
                cm.gainItem(items[2], -amount[2]);
                cm.gainItem(reward, rewamount);
                cm.sendOk("You have gained 10 #i" + reward + "#. Use this Key to Access the airship in Esfera.");
            } else {
                cm.sendOk("You currently do not have enough materials to craft #i" + reward + "#.");
            }
        } else if (option == 3) {
            var em = cm.getEventManager("Comm_will");
            if (em != null) {
                if (em.getEligiblePartyAch(cm.getPlayer(), level, 180) && em.getEligiblePartyAch(cm.getPlayer(), level, 271)) {
                    if (!em.startPlayerInstance(cm.getPlayer())) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    }
                } else {
                    cm.sendOk("Someone in your party is not ready.");
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        }
    }
}