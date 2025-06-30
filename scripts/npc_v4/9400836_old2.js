var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function start() {
    if (cm.haveItem(4310502, 25)) {
        if (cm.canHold(1115158, 1)) {
            cm.sendYesNo("Would you like to purchase NX #i1115158# for 25 Donation Points.");
        } else {
            cm.sendOk("Event requires 1 free slot in equips.");
        }
    } else {
        cm.sendOk("Event costs 25 Donation Points.");

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
        if (cm.haveItem(4310502, 25)) {
            if (cm.canHold(1115158, 1)) {
                cm.gainItem(4310502, -25);
                cm.gainDonarEquip(1115158, 9999 * 5, 25, cm.getPlayer().getTotalLevel());
                cm.sendOk("Thank you for purchasing this NX ring.");
            } else {
                cm.sendOk("Event requires 1 free slot in equips.");
            }
        } else {
            cm.sendOk("Event costs 25 Donation Points.");
                        
        }

    }
}