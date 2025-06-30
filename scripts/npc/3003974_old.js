var status = 0;
var groupsize = 0;
var item = 4031034;
var ach = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        if (cm.getPlayer().getStamina() >= 25) {
            if (cm.getPlayer().getParty() != null) {
                if (cm.getPlayer().isLeader()) {
                    var text = "";
                    text += "#L1# #bKaotic Tier: 15#k - (#i" + 4031034 + "# x5) (Reward: #r50 Gallents#k)#l\r\n";
                    text += "#L2# #bKaotic Tier: 20#k - (#i" + 4031034 + "# x25) (Reward: #r75 Gallents#k)#l\r\n";
                    text += "#L3# #bKaotic Tier: 25#k - (#i" + 4031034 + "# x100) (Reward: #r100 Gallents#k)#l\r\n";
                    text += "#L4# #bKaotic Tier: 30#k - (#i" + 4031034 + "# x500) (Reward: #r150 Gallents#k)#l\r\n";
                    text += "#L5# #bKaotic Tier: 40#k - (#i" + 4031034 + "# x1000) (Reward: #r250 Gallents#k)#l\r\n";
                    text += "#L6# #bKaotic Tier: 50#k - (#i" + 4031034 + "# x2500) (Reward: #r1000 Gallents#k)#l\r\n";
                    cm.sendSimple("Which Kaotic Elite Tier would you like to take on?\r\nEach Battle consumes #r25 Stamina#k.\r\n" + text);
                } else {
                    cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
                }
            } else {
                cm.sendOk("Event is Party Mode Only.");
            }
        } else {
            cm.sendOk("Kaotic Elite event needs a mimimum of 25 Stamina.");
        }
    } else if (status == 1) {
        if (cm.getPlayer().getParty() != null) {
            var cost = 0;
            var scale = 0;
            var level = 1000;
            var stam = 25;
            if (selection == 1) {
                cost = 5;
                scale = 15;
            } else if (selection == 2) {
                cost = 25;
                scale = 20;
            } else if (selection == 3) {
                cost = 100;
                scale = 25;
            } else if (selection == 4) {
                cost = 500;
                scale = 30;
            } else if (selection == 5) {
                cost = 1000;
                scale = 40;
            } else if (selection == 6) {
                cost = 2500;
                scale = 50;
            }
            if (cost > 0 && cm.haveItem(item, cost)) {
                var em = cm.getEventManager("EliteKaotic");
                if (em != null) {
                    if (em.getEligibleParty(cm.getPlayer(), cost, 4)) {
                        if (!em.startPlayerInstance(cm.getPlayer(), level, scale)) {
                            cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                        } else {
                            cm.gainItem(item, -cost);
                            cm.getPlayer().removeStamina(25);
                        }
                    } else {
                        cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
                    }
                } else {
                    cm.sendOk("Event has already started, Please wait.");
                }
            } else {
                cm.sendOk("Your missing " + cost + " life scrolls to enter.");
            }
        } else {
            cm.sendOk("Event is party only, Please wait.");
        }

    }
}