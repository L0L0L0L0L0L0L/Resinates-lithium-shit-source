var status = 0;
var option = 0;
var etc = 0;
var cost = 0;
var reward = 0;
var amount = 0;

function start() {
    var selStr = "Which Option you want use to gain Kaotic Power:\r\n#bKaotic Points are used for increasing chance of Kaotic Drops#k.\r\n#rYou KP will reset back to 0 once KD drop appears!#k\r\nYou Currently have #r" + cm.getPlayer().getVar("eDrop") + " Kaotic Points#k\r\n";
    selStr += ("#L4001895##i4001895# " + cm.getItemName(4001895) + " (#b100,000#k) -> #r1 KP#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4001895)) + "#k)#l\r\n");
    selStr += ("#L4036018##i4036018# " + cm.getItemName(4036018) + " (#b1,000#k) -> #r1 KP#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4036018)) + "#k)#l\r\n");
    selStr += ("#L4036518##i4036518# " + cm.getItemName(4036518) + " (#b250#k) -> #r1 KP#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4036518)) + "#k)#l\r\n");
    selStr += ("#L4310510##i4310510# " + cm.getItemName(4310510) + " (#b100#k) -> #r1 KP#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4310510)) + "#k)#l\r\n");
    selStr += ("#L4310502##i4310502# " + cm.getItemName(4310502) + " (#b50#k) -> #r1 KP#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4310502)) + "#k)#l\r\n");
    selStr += ("#L4310503##i4310503# " + cm.getItemName(4310503) + " (#b1#k) -> #r50 KP#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4310503)) + "#k)#l\r\n");
    selStr += ("#L4310504##i4310504# " + cm.getItemName(4310504) + " (#b1#k) -> #r250 KP#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4310504)) + "#k)#l\r\n");
    selStr += ("#L4310505##i4310505# " + cm.getItemName(4310505) + " (#b1#k) -> #r25000 KP#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4310505)) + "#k)#l\r\n");
    cm.sendSimpleS(selStr, 16);
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
        etc = selection;
        reward = 1;
        cost = 1;
        if (selection == 4001895) {
            cost = 100000;
        }
        if (selection == 4036018) {
            cost = 1000;
        }
        if (selection == 4036518) {
            cost = 250;
        }
        if (selection == 4310510) {
            cost = 100;
        }
        if (selection == 4310502) {
            cost = 50;
        }
        if (selection == 4310503) {
            cost = 1;
            reward = 50;
        }
        if (selection == 4310504) {
            cost = 1;
            reward = 250;
        }
        if (selection == 4310505) {
            cost = 1;
            reward = 25000;
        }
        cm.sendGetText("How many KP do you want to buy?\r\nCost is #r" + cost + "#k #b" + cm.getItemName(etc) + "#k per KP.\r\n#rYou currently have #b" + cm.convertNumber(cm.getPlayer().countAllItem(etc)) + " " + cm.getItemName(etc) + "#k\r\n");
    } else if (status == 2) {
        amount = cm.getNumber();
        if (amount > 0 && amount <= 99999) {
            cm.sendYesNoS("Are you sure you want to exchange\r\n#i" + etc + "# #b" + cm.getItemName(etc) + "#k (#b" + (cost * amount) + "#k) for #r" + (reward * amount) + "#k Kaotic Points.", 2);
        } else {
            cm.sendOk("enter a number greater than 0 and less than 9999.");
        }
    } else if (status == 3) {
        if (cm.haveItem(etc, cost * amount)) {
            cm.gainItem(etc, -(cost * amount));
            cm.getPlayer().addVar("eDrop", (reward * amount));
            cm.sendOk("You have gained #b+" + (reward * amount) + "#k Kaotic Points.\r\n#rYour KP will reset back to 0 once KD drop appears!#k");
        } else {
            cm.sendOk("Please bring me " + (cost * amount) + " #i" + etc + "# to create Kaotic Points.\r\n#rYour KP will reset back to 0 once KD drop appears!#k");
        }
    }
}