var ticketId = 4001760;
var status = 0;


function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 1) {
        if (cm.getPlayer().getAchievement(267)) {
            if (cm.getPlayer().getTotalLevel() >= 250) {
                if (cm.getPlayer().isGroup()) {
                    if (cm.getPlayer().isLeader()) {
                        var text = "Do you wish to take on Strongest challenges possible?\r\n#rSoul Park is party based and requires at least 2 players#k.\r\nMinimum level of each party memeber is 250+.#k\r\nCost of entry uses #i4001760# and #rStamina#k.\r\n";
                        text += "#L1# Easy T:5 L.250 - (#rC: 5 Tickets - Stam: 5#k)#l\r\n";
                        text += "#L2# Normal T:10 L.500 - (#rC: 10 Tickets - Stam: 10#k)#l\r\n";
                        text += "#L3# Hard T:15 L.750 - (#rC: 25 Tickets - Stam: 25#k)#l\r\n";
                        cm.sendSimple(text);
                    } else {
                        cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
                    }
                } else {
                    cm.sendOk("Event is Party Mode Only.");
                }
            } else {
                cm.sendOk("You need to be a miniuim of level 250 to enter.");
            }
        } else {
            cm.sendOk("Requires completing all Monster Park missions.");
        }
    } else if (status == 2) {
        var ach = 267;
        var em;
        var level = 250;
        var cost = 5;
        var stamina = 10;
        var scale = 5;
        if (selection == 1) {
            em = cm.getEventManager("MP_Soul_Easy");
            cost = 5;
            stamina = 5;
            level = 250;
            scale = 5;
        } else if (selection == 2) {
            em = cm.getEventManager("MP_Soul_Normal");
            cost = 10;
            stamina = 10;
            level = 500;
            scale = 10;
        } else if (selection == 3) {
            em = cm.getEventManager("MP_Soul_Hard");
            cost = 25;
            stamina = 25;
            level = 750;
            scale = 15;
        }
        if (cm.getPlayer().getStamina() >= stamina) {
            if (cost > 0 && cm.haveItem(ticketId, cost)) {
                if (em != null) {
                    if (em.getEligiblePartyAch(cm.getPlayer(), level, ach, 1, 4)) {
                        if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getTotalLevel(), scale)) {
                            cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                        } else {
                            cm.gainItem(ticketId, -cost);
                            cm.getPlayer().removeStamina(stamina);
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
            cm.sendOk("This PQ needs a mimimum of " + stamina + " Stamina.");
        }
    }
}
