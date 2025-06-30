var status = 0;
var groupsize = 0;
var item = 4036518;
var itemName = "#i4036518#";
var ach = 408;
var cost = 0;
var multi = 1;

function start() {
    if (cm.getPlayer().isGroup()) {
        if (cm.getPlayer().isLeader()) {
            var text = "You better have brought my fucking #b" + itemName + " " + cm.getItemName(item) + "s#k\r\nCurrent Amount: (#b" + cm.convertNumber(cm.getPlayer().countAllItem(item)) + "#k)\r\n";
            var group = cm.getPlayer().getGroupSize();
            //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
            text += "#L1# #rLevel: 5000 Tier: 100#k - Price: #b" + (1 * multi) + " " + itemName + "#k#l\r\n";
            text += "#L2# #rLevel: 6000 Tier: 110#k - Price: #b" + (5 * multi) + " " + itemName + "#k#l\r\n";
            text += "#L3# #rLevel: 7000 Tier: 125#k - Price: #b" + (25 * multi) + " " + itemName + "#k#l\r\n";
            text += "#L4# #rLevel: 8000 Tier: 150#k - Price: #b" + (100 * multi) + " " + itemName + "#k#l\r\n";
            text += "#L5# #rLevel: 9000 Tier: 200#k - Price: #b" + (250 * multi) + " " + itemName + "#k#l\r\n";
            text += "#L6# #rLevel: 9999 Tier: 250#k - Price: #b" + (1000 * multi) + " " + itemName + "#k#l\r\n";
            cm.sendSimple(text + " ");
        } else {
            cm.sendOkS("Have the party leader talk to me.", 2);
        }
    } else {
        cm.sendOk("Event is Party mode Only!?.");
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
        var em = cm.getEventManager("boss_nerota");
        if (em != null) {
            var level = 5000;
            var tier = 100;
            var cookies = multi;
            if (selection == 2) {
                level = 6000;
                tier = 110;
                cookies = 5 * multi;
            }
            if (selection == 3) {
                level = 7000;
                tier = 125;
                cookies = 25 * multi;
            }
            if (selection == 4) {
                level = 8000;
                tier = 150;
                cookies = 100 * multi;
            }
            if (selection == 5) {
                level = 9000;
                tier = 200;
                cookies = 250 * multi;
            }
            if (selection == 6) {
                level = 9999;
                tier = 250;
                cookies = 1000 * multi;
            }
            if (cm.haveItem(item, cookies)) {
                if (em.getEligiblePartyAch(cm.getPlayer(), level, ach, 1, 6)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level, tier)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(item, -cookies);
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
                }
            } else {
                cm.sendOk("Where is my #r" + cookies + "#k #b" + itemName + " " + cm.getItemName(item) + "s#k????");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }

    }
}