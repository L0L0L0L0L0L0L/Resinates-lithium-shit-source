var status = 0;
var level = 3000;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var cost = 0;
var em;

function start() {
    if (cm.getPlayer().getStamina() >= 25) {
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().getTotalLevel() >= level) {
                if (cm.getPlayer().achievementFinished(400)) {
                    var text = "#L4# #rStart my Trial!#k#l\r\n";
                    cm.sendSimpleS("Are you ready to challenge Ramu's trials to\r\nunlock Legendary #bPocket Slot#k?\r\nEach entry costs 25 Stamina.\r\n#brewards are boosted based on number of attackers.#k\r\n" + text, 16);
                } else {
                    cm.sendOkS("I must defeat ramu before I can challenge her trials.", 16);
                }
            } else {
                cm.sendOkS("The leader of the party must be level " + level + " or higher to complete this event.", 16);
            }
        } else {
            cm.sendOkS("Event is Solo Mode Only.", 16);
        }
    } else {
        cm.sendOkS("Ramu event needs a mimimum of 25 Stamina.", 16);
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        if (cm.getPlayer().getStamina() >= 25) {
            em = cm.getEventManager("Ramu_trials");
            if (em != null) {
                if (em.getEligiblePartyAch(cm.getPlayer(), level, 400, 1, 10)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), 2800, 26)) {
                        cm.sendOkS("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.", 16);
                    } else {
                        cm.getPlayer().removeStamina(25);
                    }
                } else {
                    cm.sendOkS("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level 2800+, 1+ Raid members.", 16);
                }
            } else {
                cm.sendOkS("Event has already started, Please wait.", 16);
            }
        } else {
            cm.sendOkS("Ramu event needs a mimimum of 25 Stamina.", 16);
        }
    }
}