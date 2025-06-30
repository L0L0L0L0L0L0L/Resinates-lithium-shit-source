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
            if (cm.getPlayer().getTotalLevel() >= 1000) {
                if (cm.getPlayer().isGroup()) {
                    if (cm.getPlayer().isLeader()) {
                        var text = "Do you wish to take on Strongest challenges possible?\r\n#rMonster Park is party based and requires at least 2 players#k.\r\nMinimum level of each party memeber is 1,000.#k\r\nCost of entry uses #i4001760# and #rStamina#k.\r\n";
                        text += "#L1# Easy (#rPrice: 5 - Stam: 5#k) (#bRT: 5#k)#l\r\n";
                        text += "#L2# Normal (#rPrice: 10 - Stam: 10#k) (#bRT: 10#k)#l\r\n";
                        text += "#L3# Hard (#rPrice: 15 - Stam: 15#k) (#bRT: 15#k)#l\r\n";
                        text += "#L4# Hell (#rPrice: 25 - Stam: 20#k) (#bRT: 25#k)#l\r\n";
                        text += "#L5# Super (#rPrice: 50 - Stam: 25#k) (#bRT: 50#k)#l\r\n";
                        text += "#L6# Kaotic (#rPrice: 75 - Stam: 50#k) (#bRT: 75#k)#l\r\n";
                        text += "#L7# Giga (#rPrice: 100 - Stam: 100#k) (#bRT: 100#k)#l\r\n";
                        cm.sendSimple(text);
                    } else {
                        cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
                    }
                } else {
                    cm.sendOk("Event is Party Mode Only.");
                }
            } else {
                cm.sendOk("You need to be a miniuim of level 1000 to enter.");
            }
        } else {
            cm.sendOk("Requires completing all Monster Park missions.");
        }
    } else if (status == 2) {
        var ach = 0;
        var em;
        var level = 1000;
        var cost = 0;
        var stamina = 0;
        if (selection == 1) {
            em = cm.getEventManager("MP_Kaotic_Easy");
            ach = 267;
            cost = 5;
            stamina = 5;
        } else if (selection == 2) {
            em = cm.getEventManager("MP_Kaotic_Normal");
            ach = 287;
            cost = 10;
            stamina = 10;
        } else if (selection == 3) {
            em = cm.getEventManager("MP_Kaotic_Hard");
            ach = 288;
            cost = 15;
            stamina = 15;
        } else if (selection == 4) {
            em = cm.getEventManager("MP_Kaotic_Hell");
            ach = 289;
            cost = 25;
            stamina = 20;
        } else if (selection == 5) {
            em = cm.getEventManager("MP_Kaotic_Super");
            ach = 290;
            cost = 50;
            stamina = 25;
        } else if (selection == 6) {
            em = cm.getEventManager("MP_Kaotic_Kaotic");
            ach = 291;
            cost = 75;
            stamina = 50;
        } else if (selection == 7) {
            em = cm.getEventManager("MP_Kaotic_Giga");
            ach = 292;
            cost = 100;
            stamina = 100;
        }
        if (cm.getPlayer().getStamina() >= stamina) {
            if (cost > 0 && cm.haveItem(ticketId, cost)) {
                if (em != null) {
                    if (em.getEligiblePartyAch(cm.getPlayer(), level, ach, 1, 4)) {
                        var room = cm.random(0, 16);
                        if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getTotalLevel(), room)) {
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
            cm.sendOk("Kaotic Boss PQ needs a mimimum of " + stamina + " Stamina.");
        }
    }
}
