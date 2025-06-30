var status = 0;
var groupsize = 0;
var item = 4034867;
var itemName = "#i4034867#";
var ach = 435;//435
var cost = 0;
var multi = 1;

function start() {
    var number1 = cm.random(1, 9);
    var number2 = cm.random(1, 9);
    var number3 = cm.random(1, 9);
    var number4 = cm.random(1, 9);
    password = cm.getCode(number1, number2, number3, number4);
    cm.sendGetText("#rWhen you see a Gacha Code like this, it means botting inside these instances or event is not allowed.#k\r\nFailing the code does not cause any harm but to your ego.\r\n\Please enter the 4 digit #rAnti-Bot#k Code seen below:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "");
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
                    var text = "You better have brought my fucking #b" + itemName + " " + cm.getItemName(item) + "s#k\r\nCurrent Amount: (#b" + cm.convertNumber(cm.getPlayer().countAllItem(item)) + "#k)\r\n";
                    var group = cm.getPlayer().getGroupSize();
                    //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
                    text += "#L2# #rLevel: 8000 Tier: 150#k - Price: #b" + (25 * multi) + " " + itemName + "#k#l\r\n";
                    text += "#L3# #rLevel: 8500 Tier: 175#k - Price: #b" + (50 * multi) + " " + itemName + "#k#l\r\n";
                    text += "#L4# #rLevel: 9000 Tier: 200#k - Price: #b" + (100 * multi) + " " + itemName + "#k#l\r\n";
                    //text += "#L5# #rLevel: 9500 Tier: 225#k - Price: #b" + (250 * multi) + " " + itemName + "#k#l\r\n";
                    //text += "#L6# #rLevel: 9999 Tier: 250#k - Price: #b" + (1000 * multi) + " " + itemName + "#k#l\r\n";
                    cm.sendSimple(text);
                } else {
                    cm.sendOkS("Have the party leader talk to me.", 2);
                }
            } else {
                cm.sendOk("Event is Party mode Only!?.");
            }
        } else {
            cm.sendOk("Wrong password mother fka.");
        }
    }
    if (status == 2) {
        var em = cm.getEventManager("boss_lotus_art");
        if (em != null) {
            var level = 5000;
            var tier = 100;
            var cookies = multi;
            if (selection == 2) {
                level = 8000;
                tier = 150;
                cookies = 25 * multi;
            }
            if (selection == 3) {
                level = 8500;
                tier = 175;
                cookies = 50 * multi;
            }
            if (selection == 4) {
                level = 9000;
                tier = 200;
                cookies = 100 * multi;
            }
            if (selection == 5) {
                level = 9500;
                tier = 225;
                cookies = 250 * multi;
            }
            if (selection == 6) {
                level = 9999;
                tier = 250;
                cookies = 1000 * multi;
            }
            if (cm.haveItem(item, cookies)) {
                if (em.getEligiblePartyAch(cm.getPlayer(), level, ach, 1, 40)) {
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