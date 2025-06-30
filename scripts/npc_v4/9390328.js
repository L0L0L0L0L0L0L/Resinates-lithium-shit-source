var ticketId = 4001760;
var status = 0;


function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 1) {
        if (cm.getPlayer().getAchievement(293)) {
            if (cm.getPlayer().isGroup()) {
                if (cm.getPlayer().isLeader()) {
                    var text = "Do you wish to take on Strongest challenges possible?\r\n";
                    text += "#L1# Easy (#rPrice: " + (100 * cm.getPlayer().getGroupSize()) + " #i" + ticketId + "#  " + cm.getItemName(ticketId) + "#k)#l\r\n";
                    text += "#L2# Normal (#rPrice: " + (250 * cm.getPlayer().getGroupSize()) + " #i" + ticketId + "#  " + cm.getItemName(ticketId) + "#k)#l\r\n";
                    text += "#L3# Hard (#rPrice: " + (500 * cm.getPlayer().getGroupSize()) + " #i" + ticketId + "#  " + cm.getItemName(ticketId) + "#k)#l\r\n";
                    text += "#L4# Super (#rPrice: " + (1000 * cm.getPlayer().getGroupSize()) + " #i" + ticketId + "#  " + cm.getItemName(ticketId) + "#k)#l\r\n";
                    text += "#L5# Kaotic (#rPrice: " + (2500 * cm.getPlayer().getGroupSize()) + " #i" + ticketId + "#  " + cm.getItemName(ticketId) + "#k)#l\r\n";
                    cm.sendSimple(text);
                } else {
                    cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
                }
            } else {
                cm.sendOk("Event is Party Mode Only.");
            }
        } else {
            cm.sendOk("Requires completing all of previous Monster Park missions.");
        }
    } else if (status == 2) {
        var ach = 0;
        var em;
        var level = 1000;
        var cost = 0;
        var stamina = 0;
        if (selection == 1) {
            em = cm.getEventManager("MP_Mega_Easy");
            ach = 293;
            cost = 100;
            level = 5000;
        } else if (selection == 2) {
            em = cm.getEventManager("MP_Mega_Normal");
            ach = 293;
            cost = 250;
            level = 5000;
        } else if (selection == 3) {
            em = cm.getEventManager("MP_Mega_Hard");
            ach = 293;
            cost = 500;
            level = 5000;
        } else if (selection == 4) {
            em = cm.getEventManager("MP_Mega_Super");
            ach = 293;
            cost = 1000;
            level = 5000;
        } else if (selection == 5) {
            em = cm.getEventManager("MP_Mega_Kaotic");
            ach = 293;
            cost = 2500;
            level = 5000;
        }
        if (cm.getPlayer().getStamina() >= stamina) {
            if (cost > 0 && cm.haveItem(ticketId, cost * cm.getPlayer().getGroupSize())) {
                if (em != null) {
                    if (em.getEligiblePartyAch(cm.getPlayer(), level, ach, 1, 4)) {
                        var room = cm.random(0, 16);
                        if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getTotalLevel(), room)) {
                            cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                        } else {
                            cm.gainItem(ticketId, -(cost * cm.getPlayer().getGroupSize()));
                        }
                    } else {
                        cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map.");
                    }
                } else {
                    cm.sendOk("Event has already started, Please wait.");
                }
            } else {
                cm.sendOk("To enter you must have #i" + ticketId + "#.");
            }
        } else {
            cm.sendOk("Kaotic Boss PQ needs a mimimum of " + stamina + " Stamina.");
        }
    }
}
