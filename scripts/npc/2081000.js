var status = 0;
var level = 1000;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var etc = 4310100;
var key = 4034031;
var amount = 0;
function start() {
    cm.sendOk("Leafre is such wonderful place to live");
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
        if (amount > 0 && amount <= 10000) {
            cm.sendYesNoS("Are you sure you want to exchange " + (1000 * amount) + " #i" + etc + "# for #i" + key + "# x(" + amount + ").", 2);
        } else {
            cm.sendOk("enter a number greater than 0 and less than 1000.");
        }
    } else if (status == 3) {
        if (amount > 0 && amount <= 10000) {
            if (cm.haveItem(4033653, 1000 * amount)) {
                if (cm.canHold(key)) {
                    cm.gainItem(etc, 1000 * amount * -1);
                    cm.gainItem(key, amount);
                    cm.sendOk("Use this Key Card to access the bosses here.");
                } else {
                    cm.sendOk("Please make room in ETC Inventory.");
                }
            } else {
                cm.sendOk("Please bring me " + (1000 * amount) + " #i" + etc + "# to craft the ancient key.");
            }
        } else {
            cm.sendOk("enter a number greater than 0 and less than 1000.");
        }
    }
}