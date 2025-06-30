var status = 0;
var groupsize = 0;
var item = 4310500;
var ach = 0;
var cost = 0;

function start() {
    if (cm.getPlayer().isGroup()) {
        var number1 = cm.random(1, 9);
        var number2 = cm.random(1, 9);
        var number3 = cm.random(1, 9);
        var number4 = cm.random(1, 9);
        password = cm.getCode(number1, number2, number3, number4);
        cm.sendGetText("#rBotting beyond this point is not allowed!\r\n\#kPlease enter the 4 digit #rAnti-Bot#k Code seen below to start the dojo:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "");
    } else {
        cm.sendOkS("I must take on this Challenge alone.", 2);
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
        amount = cm.getNumber();
        if (amount == password) {
            if (cm.getPlayer().isGroup()) {
                if (cm.getPlayer().isLeader()) {
                    var text = "You think you can tear my ass up?? lets go, and you better have brought my fucking #b#i4036514# " + cm.getItemName(4036514) + "s#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4036514)) + "#k)\r\n";
                    //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
                    text += "#L1# #rLevel: 2500 Tier: 50#k - Price: #b1000 #i4036514##k#l\r\n";
                    text += "#L2# #rLevel: 5000 Tier: 100#k - Price: #b10000 #i4036514##k#l\r\n";
                    text += "#L3# #rLevel: 7000 Tier: 150#k - Price: #b100000 #i4036514##k#l\r\n";
                    text += "#L4# #rLevel: 8000 Tier: 200#k - Price: #b1000000 #i4036514##k#l\r\n";
                    text += "#L5# #rLevel: 9000 Tier: 250#k - Price: #b10000000 #i4036514##k#l\r\n";
                    cm.sendSimple(text);
                } else {
                    cm.sendOkS("Have the party leader talk to me.", 2);
                }
            } else {
                cm.sendOk("Event is Party mode Only!?.");
            }
        } else {
            cm.sendOk("Wrong password.");
        }
    }
    if (status == 2) {
        var em = cm.getEventManager("boss_sellas_mega");
        if (em != null) {
            var level = 2500;
            var tier = 50;
            var cookies = 1000;
            if (selection == 2) {
                level = 5000;
                tier = 100;
                cookies = 10000;
            }
            if (selection == 3) {
                level = 7000;
                tier = 150;
                cookies = 100000;
            }
            if (selection == 4) {
                level = 8000;
                tier = 200;
                cookies = 1000000;
            }
            if (selection == 5) {
                level = 9000;
                tier = 250;
                cookies = 10000000;
            }
            if (cm.haveItem(4036514, cookies)) {
                if (em.getEligiblePartyAch(cm.getPlayer(), level, 50, 1, 20)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level, tier)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(4036514, -cookies);
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
                }
            } else {
                cm.sendOk("Where is my #r" + cookies + "#k #b#i4036514# " + cm.getItemName(4036514) + "s#k????");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }

    }
}