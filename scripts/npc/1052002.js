var status = 0;
var level = 1000;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var key = 4310500;
var etc = 4280002;
var amount = 0;
var gmb = 0;
function start() {
    gmb = cm.getPlayer().getLevelData(104);
    cm.sendYesNo("Would you like to exchange #i" + etc + "# for " + gmb + " of #i" + key + "#.");
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
        if (amount > 0 && amount <= 1000) {
            cm.sendYesNoS("Are you sure you want to exchange " + amount + " #i" + etc + "?", 2);
        } else {
            cm.sendOk("enter a number greater than 0 and less than 1000.");
        }
    } else if (status == 3) {
        if (amount > 0 && amount <= 1000) {
            if (cm.haveItem(etc, amount)) {
                if (cm.canHold(key)) {
                    cm.gainItem(etc, amount * -1);
                    cm.gainItem(key, gmb * amount);
                    cm.sendOk("Enjoy your money!");

                } else {
                    cm.sendOk("Please make room in ETC Inventory.");

                }
            } else {
                cm.sendOk("Please bring me " + amount + " #i" + etc + "#");
            }
        } else {
            cm.sendOk("enter a number greater than 0 and less than 1000.");
        }
    }
}