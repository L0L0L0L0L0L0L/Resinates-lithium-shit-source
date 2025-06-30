var status = 0;
var level = 1800;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var option = 0;

function start() {
    if (cm.getPlayer().getParty() != null) {
        if (cm.getPlayer().isLeader()) {
            if (cm.getPlayer().getTotalLevel() >= level) {
                cm.sendSimple("Welcome to the Deepest part of the maze. Pick which necro boss to challenge.\r\n\r\n#L0# Necro Lotus#l\r\n\#L1# Necro Damien#l");
            } else {
                cm.sendOk("The leader of the party must be level " + level + " or higher to complete this event.");
            }
        } else {
            cm.sendOk("The leader of the party must be the one to talk to me about joining the event.");
        }
    } else {
        cm.sendOk("Event is Party Mode Only.");
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        option = selection;
        if (option == 0) {
            var em = cm.getEventManager("Necro_Lotus");
            if (em != null) {
                if (em.getEligibleParty(cm.getPlayer(), level)) {
                    if (!em.startPlayerInstance(cm.getPlayer())) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + " and 4+ players.");
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        } else if (option == 1) {
            var em = cm.getEventManager("Necro_Damien");
            if (em != null) {
                if (em.getEligibleParty(cm.getPlayer(), level)) {
                    if (!em.startPlayerInstance(cm.getPlayer())) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + " and 4+ players.");
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        }
    }
}