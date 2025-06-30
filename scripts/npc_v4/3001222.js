var status = 0;
var groupsize = 0;
var item = 4310500;
var ach = 0;
var cost = 0;
var tier = 0;
var level = 0;

function start() {
    if (cm.getPlayer().getTotalLevel() >= 900) {
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {

                var text = "You think you can tear my ass up?? lets go pussy, and you better have brought my fucking #b#i4032061# " + cm.getItemName(4032061) + "s#k bitch!!!\r\n\r\n";
                var group = cm.getPlayer().getGroupSize();
                //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
                text += "#L1# #rLevel: 750 Tier: 30#k #b1000 #i4032061##k#l\r\n";
                text += "#L2# #rLevel: 1000 Tier: 40#k #b5000 #i4032061##k#l\r\n";
                text += "#L3# #rLevel: 2500 Tier: 50#k #b10000 #i4032061##k#l\r\n";
                text += "#L4# #rLevel: 5000 Tier: 60#k #b25000 #i4032061##k#l\r\n";
                text += "#L5# #rLevel: 7500 Tier: 80#k #b100,000 #i4032061##k#l\r\n";
                text += "#L6# #rLevel: 9999 Tier: 99#k #b1,000,000 #i4032061##k#l\r\n";
                cm.sendSimple(text);
            } else {
                cm.sendOkS("Have the party leader talk to me.", 2);
            }
        } else {
            cm.sendOk("Event is Orgy mode Only, BITCH!?.");
        }
    } else {
        cm.sendOk("My services are only open to those that aren't pussies.");
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
        var em = cm.getEventManager("Hazard");
        if (em != null) {
            var level = 750;
            var tier = 30;
            var cookies = 1000;
            if (selection == 2) {
                level = 1000;
                tier = 40;
                cookies = 5000;
            }
            if (selection == 3) {
                level = 2500;
                tier = 50;
                cookies = 10000;
            }
            if (selection == 4) {
                level = 5000;
                tier = 60;
                cookies = 25000;
            }
            if (selection == 5) {
                level = 7500;
                tier = 80;
                cookies = 100000;
            }
            if (selection == 6) {
                level = 9999;
                tier = 99;
                cookies = 1000000;
            }
            if (cm.haveItem(4032061, cookies)) {
                if (em.getEligiblePartyAch(cm.getPlayer(), 900, 66, 1, 20)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level, tier)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(4032061, -cookies);
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
                }
            } else {
                cm.sendOk("Where is my fucking #r2500#k #b#i4032061# " + cm.getItemName(4032061) + "s#k????");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }

    }
}