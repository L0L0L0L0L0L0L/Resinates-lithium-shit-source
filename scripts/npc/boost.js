var status = 0;
var level = 1000;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var key = 4001869;
var etc = 4001868;
var cost = 0;
var price = 0;
var amount = 0;
function start() {
    var info = "Boosting Maps increases the rate of which monsters respawn from 4 seconds to 3 seconds (25% Faster).\r\nThis is applied to the #rMap#k not the player.\r\n#rBoosting does not save on server reboots or updates!\r\nBuy with caution knowing this risk?\r\nAll sales on boosts are final and non-refundable.#k\r\n";
    var selStr = "\r\n";
    selStr += "#L3#Boost with #b#i4310500# x100-(hr)#k#l\r\n";
    if (cm.getPlayer().getMap().getBoost() > 0) {
        cm.sendSimpleS(info + "#bThis map is already boosted with \r\n" + cm.getTimeSec(cm.getPlayer().getMap().getBoost()) + "#k Time left.\r\nDo you want to add more boost to this map?" + selStr, 16);
    } else {
        cm.sendSimpleS(info + "Do you want to boost this map?" + selStr, 16);
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
        if (selection == 1) {
            etc = 4310502;
            cost = 10;
        }
        if (selection == 2) {
            etc = 4310503;
            cost = 25;
        }
        if (selection == 3) {
            etc = 4310500;
            cost = 100;
        }
        cm.sendGetTextS("How many #rHours#k do you want to buy, #rEach hour costs " + cost + "x #i" + etc + "##k?\r\n ", 16);
    } else if (status == 2) {
        amount = cm.getNumber();
        if (amount > 0 && amount <= 10000) {
            price = cost * amount;
            cm.sendYesNoS("Are you sure you want to pay #r" + price + " #i" + etc + "##k for \r\n#b" + cm.getTimeSec(amount * 3600) + "#k Boost?\r\n#rBoosting does not save on server reboots/updates!\r\nBuy with caution knowing this risk?#k", 16);
        } else {
            cm.sendOkS("enter a number greater than 0 and less than 1000.", 16);
        }
    } else if (status == 3) {
        if (cm.haveItem(etc, price)) {
            cm.gainItem(etc, -price);
            cm.getPlayer().getMap().addBoost(amount * 3600);
            cm.sendOkS("you have successfully boosted this map for:\r\n#b" + cm.getTimeSec(amount * 3600) + "#k", 16);
        } else {
            cm.sendOkS("Please bring me " + price + " #i" + etc + "# to boost this map.", 16);
        }
    }
}