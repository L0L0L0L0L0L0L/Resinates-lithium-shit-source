var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var cost = 0;
var reward = 0;
var reward2 = 0;
var rewards;
var exp = 0;
var count = 1;
var multi = 0;
var item = 4420011;
var wanted = 0;
var skin = 0;
var chain = 0;
var wanted_item = 0;
var wanted_item_amount = 0;
var wanted_base = 1;
var amount = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {

    } else {
        if (mode == 0 && type > 0) {

            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.getPlayer().getGuild() != null) {
                cm.sendYesNo("Do you wish to spend #i4310505# #z4310505# to buy #b100,000#k Guild Points?");
            } else {
                cm.sendOk("You must be in a guild to use my services.");
            }

        } else if (status == 1) {
            cm.sendGetText("How many #i4310505# #z4310505# do you wish to spend?\r\nEach Point is worth #b100,000#k Guild Points.\r\n");
        } else if (status == 2) {
            amount = cm.getNumber();
            if (amount > 0 && amount <= 10000) {
                if (cm.haveItem(4310505, amount)) {
                    cm.sendYesNo("Do you confirm to spend #i4310505# #z4310505# to buy #b" + (100000 * amount) + "#k Guild Points?");
                } else {
                    cm.sendOk("You must have enough #i4310505# #z4310505# to use my services.");
                }
            } else {
                cm.sendOk("You must be within the limits of 1-10000.");
            }

        } else if (status == 3) {
            if (cm.getPlayer().getGuild() != null) {
                if (amount > 0 && cm.haveItem(4310505, amount)) {
                    cm.gainItem(4310505, -amount);
                    cm.getPlayer().gainGP(100000 * amount);
                    text += "Gained Guild Points: +50000\r\n";
                    cm.sendOk("Your guild has gained #b" + (100000 * amount) + "#k Guild Points.");
                } else {
                    cm.sendOk("You must have enough #i4310505# #z4310505# to use my services.");
                }
            } else {
                cm.sendOk("You must be in a guild to use my services.");
            }
        }
    }
}