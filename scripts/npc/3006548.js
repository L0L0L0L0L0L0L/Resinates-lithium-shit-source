var status = 0;
var groupsize = 0;
var item = 4031312;
var itemName = "#i4031312#";
var ach = 439;
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
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                var text = "You better have brought my #b" + itemName + " " + cm.getItemName(item) + "s#k\r\nCurrent Amount: (#b" + cm.convertNumber(cm.getPlayer().countAllItem(item)) + "#k)\r\n";
                text += "#L0# #rLevel: 9000 Tier: 100#k - Price: #b" + (1) + " " + itemName + "#k#l\r\n";
                text += "#L1# #rLevel: 9200 Tier: 200#k - Price: #b" + (1) + " " + itemName + "#k#l\r\n";
                text += "#L2# #rLevel: 9400 Tier: 300#k - Price: #b" + (2) + " " + itemName + "#k#l\r\n";
                text += "#L3# #rLevel: 9600 Tier: 500#k - Price: #b" + (5) + " " + itemName + "#k#l\r\n";
                text += "#L4# #rLevel: 9800 Tier: 750#k - Price: #b" + (10) + " " + itemName + "#k#l\r\n";
                text += "#L5# #rLevel: 9999 Tier: 999#k - Price: #b" + (25) + " " + itemName + "#k#l\r\n";
                cm.sendSimple(text);
            } else {
                cm.sendOkS("Have the party leader talk to me.", 2);
            }
        } else {
            cm.sendOk("Event is Party mode Only!?.");
        }
    }
    if (status == 2) {
        var em = cm.getEventManager("LMPQ");
        if (em != null) {
            var level = 9000;
            var tier = 200;
            var cookies = 1, reborns = 10;
            if (selection == 0) {
                level = 9000;
                tier = 100;
                cookies = 1;
                reborns = 0;
            }
            if (selection == 2) {
                level = 9200;
                tier = 200;
                cookies = 2;
                reborns = 20;
            }
            if (selection == 3) {
                level = 9400;
                tier = 300;
                cookies = 5;
                reborns = 30;
            }
            if (selection == 4) {
                level = 9600;
                tier = 500;
                cookies = 10;
                reborns = 40;
            }
            if (selection == 5) {
                level = 9800;
                tier = 750;
                cookies = 25;
                reborns = 50;
            }
            if (selection == 6) {
                level = 9999;
                tier = 999;
                cookies = 100;
                reborns = 50;
            }
            if (cm.haveItem(item, cookies)) {
                if (em.getEligiblePartyAchReborn(cm.getPlayer(), 9000, ach, 1, 4, reborns) && em.getEligiblePartyAch(cm.getPlayer(), 9000, 273)) {
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