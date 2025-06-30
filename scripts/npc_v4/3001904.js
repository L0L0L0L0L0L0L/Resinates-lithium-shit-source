var status = 0;
var groupsize = 0;
var item = 4310500;
var ach = 0;
var cost = 0;

function start() {
    if (cm.getPlayer().achievementFinished(409)) {
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {

                var text = "You think you can tear my ass up?? lets go, and you better have brought my fucking #b#i4036659# " + cm.getItemName(4036659) + "s#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4036659)) + "#k)\r\n";
                var group = cm.getPlayer().getGroupSize();
                //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
                text += "#L1# #rLevel: 5000 Tier: 90#k - Price: #b1000 #i4036659##k#l\r\n";
                text += "#L2# #rLevel: 5000 Tier: 95#k - Price: #b5000 #i4036659##k#l\r\n";
                text += "#L3# #rLevel: 5000 Tier: 100#k - Price: #b25000 #i4036659##k#l\r\n";
                cm.sendSimple(text);
            } else {
                cm.sendOkS("Have the party leader talk to me.", 2);
            }
        } else {
            cm.sendOk("Event is Party mode Only!?.");
        }
    } else {
        cm.sendOk("Head back to town and go east to defeat the #bFrozen NightHost#k");
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
        var em = cm.getEventManager("boss_fire");
        if (em != null) {
            var level = 5000;
            var tier = 90;
            var cookies = 1000;
            if (selection == 2) {
                tier = 95;
                cookies = 5000;
            }
            if (selection == 3) {
                tier = 100;
                cookies = 25000;
            }
            if (selection == 4) {
                level = 8000;
                tier = 125;
                cookies = 250000;
            }
            if (cm.haveItem(4036659, cookies)) {
                if (em.getEligiblePartyAch(cm.getPlayer(), level, 409, 1, 6)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level, tier)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(4036659, -cookies);
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
                }
            } else {
                cm.sendOk("Where is my #r" + cookies + "#k #b#i4036659# " + cm.getItemName(4036659) + "s#k????");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }

    }
}