/*
 NPC Name: 		The Forgotten Temple Manager
 Map(s): 		Deep in the Shrine - Twilight of the gods
 Description: 		Pink Bean
 */

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        cm.sendGetTextS("How many #bGuild Points#k do you wish to buy with\r\n\#i" + 4310150 + "# " + cm.getItemName(4310150) + ".\r\nEach Reward Coin is 100 Guild Points.", 16);
    } else if (status == 1) {
        amount = cm.getNumber();
        if (amount > 0 && amount <= 9999999) {
            if (cm.haveItem(4310150, amount)) {
                cm.gainItem(4310150, -amount);
                cm.getPlayer().gainGP(amount * 100);
                cm.sendOk("Your guild has gained #b" + (amount * 100) + " Points#k.");
            }
        }
    }
}