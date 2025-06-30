var status = 0;
var level = 1500;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function start() {
    if (!cm.getPlayer().getAchievement(271)) {
        cm.sendOk("You must clear #rNormal Party Mode#k in #rDungeon Room#k");
        return;
    }
    if (cm.getPlayer().isGroup()) {
        if (cm.getPlayer().isLeader()) {
            if (cm.getPlayer().getTotalLevel() >= level) {
                cm.sendYesNo("Do you have the determination to challenge Will?");
            } else {
                cm.sendOk("The leader of the party must be level " + level + " or higher to complete this event.");
            }
        } else {
            cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
        }
    } else {
        cm.sendOk("Event is Party Mode Only.");
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
        var em = cm.getEventManager("will_boss");
        if (em != null) {
            if (em.getEligiblePartyAch(cm.getPlayer(), level, 64) && em.getEligiblePartyAch(cm.getPlayer(), level, 271)) {
                if (!em.startPlayerInstance(cm.getPlayer())) {
                    cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                }
            } else {
                cm.playerMessage(5, "You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }

    }
}