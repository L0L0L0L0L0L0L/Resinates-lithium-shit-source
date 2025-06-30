var status = 0;
var level = 1000;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var etc = 4310100;
var key = 4034031;
var price = 0;
var cost = 250000;
var items = new Array(4310100, 4033335);
var amount = new Array(10000, 1000);

function start() {
    if (cm.getPlayer().isGM() || cm.getPlayer().getVar("kobold") == 7) {
        var selStr = "\r\n\r\n";
        for (var i = 0; i < items.length; i++) {
            selStr += "#i" + items[i] + "#  " + cm.getItemName(items[i]) + " (x#b" + amount[i] + "#k)\r\n\ ";
        }
        cm.sendYesNo("If you want #i" + key + "#, I want following items: " + selStr + ".");
    } else {
        cm.sendOk("Go away peasent");
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
        cm.sendGetText("How many times do you want me to do it?\r\n\r\n");
    } else if (status == 2) {
        price = cm.getNumber();
        if (price > 0 && price <= 10000) {
            cm.sendYesNoS("Are you sure you want to craft #i" + key + "# x(" + price + ").", 2);
        } else {
            cm.sendOk("enter a number greater than 0 and less than 1000.");
        }
    } else if (status == 3) {
        var count = 0;
        for (var i = 0; i < items.length; i++) {
            if (cm.haveItem(items[i], amount[i] * price)) {
                count++;
            }
        }
        if (count >= items.length) {
            for (var i = 0; i < items.length; i++) {
                cm.gainItem(items[i], -amount[i] * price);
            }
            cm.gainItem(key, price);
            cm.sendOk("Use this Key Card to access the bosses here.");
        } else {
            cm.sendOk("You currently do not have enough materials craft the #bKey Card#k");
        }
    } else {
        cm.sendOk("enter a number greater than 0 and less than 1000.");
    }
}