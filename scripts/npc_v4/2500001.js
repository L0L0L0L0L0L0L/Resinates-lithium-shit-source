var status = 0;
var groupsize = 0;
var item = 4000524;
var itemName = "#i4000524#";
var ach = 441;
var cost = 0;
var multi = 1;
var password = 0;
var cookies = 1;
var level = 9000;

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
                    cm.sendYesNo("Do you want to confirm that you wish to redeem #i4000524#");
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
        var em = cm.getEventManager("boss_ape");
        if (em != null) {
            if (cm.haveItem(item, cookies)) {
                if (em.getEligiblePartyAchReborn(cm.getPlayer(), 9000, ach, 1, 4, 90)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), 9999, 900)) {
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