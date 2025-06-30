var status = 0;
var level = 1000;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var etc = 4032060;
var key = 4032058;
var amount = 0;
var cost = 10;

function start() {
    cm.sendYesNo("Would you like to exchange #b" + cost + "#k #i" + etc + "# for 1 #i" + key + "#.");
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
        cm.sendGetText("How many times do you want me to do it?\r\n\r\n");
    } else if (status == 2) {
        amount = cm.getNumber();
        if (amount > 0 && amount <= 999999) {
            cm.sendYesNo("Are you sure you want to exchange " + (cost * amount) + " #i" + etc + "# for #i" + key + "# x(" + amount + ").");
        } else {
            cm.sendOk("enter a number greater than 0 and less than 999999.");
        }
    } else if (status == 3) {
        if (amount > 0 && amount <= 999999) {
            if (cm.haveItem(etc, cost * amount)) {
                if (cm.canHold(key)) {
                    cm.gainItem(etc, -(cost * amount));
                    cm.gainItem(key, amount);
                    cm.sendOk("Take these to #bPopoh#k.");
                } else {
                    cm.sendOk("Please make room in ETC Inventory.");
                }
            } else {
                cm.sendOk("Please bring me " + (cost * amount) + " #i" + etc + "# to craft the ancient key.");
            }
        } else {
            cm.sendOk("enter a number greater than 0 and less than 999999.");
        }
    }
}