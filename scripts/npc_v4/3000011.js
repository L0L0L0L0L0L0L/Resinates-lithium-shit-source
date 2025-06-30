var status = 0;
var groupsize = 0;
var item = 4310058;
var ach = 0;
var cost = 250;
var tier = 0;
var level = 0;

function start() {
    if (cm.getPlayer().getTotalLevel() >= 999) {
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                var price = 250 * cm.getPlayer().getGroupSize();
                cm.sendYesNo("Ready to challenge Great Pantheon Raid?\r\n#rGroup Size Min: 1 - Max:20#k\r\nPrice: #r" + price + "#k #b#i" + item + "# " + cm.getItemName(item) + "#k");
            } else {
                cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
            }
        } else {
            cm.sendOk("Event is Party/Raid Mode Only.");
        }
    } else {
        cm.sendOk("My services are only open to those at or above level 250.");
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
        cost = 250 * cm.getPlayer().getGroupSize();
        if (cm.haveItem(item, cost)) {
            var em = cm.getEventManager("Blake");
            if (em != null) {
                if (em.getEligiblePartyAch(cm.getPlayer(), 999, 66, 1, 20)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level, tier)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(item, -cost);
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        } else {
            cm.sendOk("Seem you dont have any #b#i" + item + "# " + cm.getItemName(item) + "#k");
        }

    }
}