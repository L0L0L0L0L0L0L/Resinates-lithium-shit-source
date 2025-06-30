var status = 0;
var groupsize = 0;
var item = 4031312;
var itemName = "#i4031312#";
var ach = 441;
var cost = 0;
var multi = 1;
var password = 0;

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
        var amount = cm.getNumber();
        if (amount == password) {
            if (cm.getPlayer().isGroup()) {
                if (cm.getPlayer().isLeader()) {
                    var text = "You better have brought my #b" + itemName + " " + cm.getItemName(item) + "s#k\r\nCurrent Amount: (#b" + cm.convertNumber(cm.getPlayer().countAllItem(item)) + "#k)\r\n";
                    //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
                    text += "#L1# #rReborns: 5 Tier: 100#k - Price: #b" + (1) + " " + itemName + "#k#l\r\n";
                    text += "#L2# #rReborns: 10 Tier: 125#k - Price: #b" + (2) + " " + itemName + "#k#l\r\n";
                    text += "#L3# #rReborns: 15 Tier: 150#k - Price: #b" + (3) + " " + itemName + "#k#l\r\n";
                    text += "#L4# #rReborns: 20 Tier: 200#k - Price: #b" + (4) + " " + itemName + "#k#l\r\n";
                    text += "#L5# #rReborns: 25 Tier: 250#k - Price: #b" + (5) + " " + itemName + "#k#l\r\n";
                    //text += "#L6# #rLevel: 9999 Tier: 250#k - Price: #b" + (1000 * multi) + " " + itemName + "#k#l\r\n";
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
        var em = cm.getEventManager("LMPQ_Solo");
        if (em != null) {
            var level = 9000;
            var tier = 100;
            var cookies = 1, reborns = 5;
            if (selection == 2) {
                tier = 125;
                cookies = 2;
                reborns = 10;
            }
            if (selection == 3) {
                tier = 150;
                cookies = 3;
                reborns = 15;
            }
            if (selection == 4) {
                tier = 200;
                cookies = 4;
                reborns = 20;
            }
            if (selection == 5) {
                tier = 250;
                cookies = 5;
                reborns = 25;
            }
            if (cm.haveItem(item, cookies)) {
                if (em.getEligiblePartyAchReborn(cm.getPlayer(), 9000, ach, 1, 1, reborns)) {
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