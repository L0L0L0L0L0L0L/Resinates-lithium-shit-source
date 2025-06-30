
var status;
var sel = 0;
var summon = 4006001;
var amount = 0;
var item = 4310066;
var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        if (cm.getPlayer().getRemainingAp() > 0) {
            var text = "";

            text += "#L1#Str (#b" + cm.getPlayer().getStat().getStr() + "#k)#l\r\n";
            text += "#L2#Dex (#b" + cm.getPlayer().getStat().getDex() + "#k)#l\r\n";
            text += "#L3#Int (#b" + cm.getPlayer().getStat().getInt() + "#k)#l\r\n";
            text += "#L4#Luk (#b" + cm.getPlayer().getStat().getLuk() + "#k)#l\r\n";
            cm.sendSimpleS("You have some AP available to use?\r\n\You currently have #b" + cm.convertNumber(cm.getPlayer().getRemainingAp()) + "#k Unused AP to assign. \r\n\Which Stat would you like apply AP to? \r\n" + text, 2);
        } else {
            cm.sendOkS("You currently do not have any AP to spend.", 2);
        }
    } else if (status == 1) {
        sel = selection;
        cm.sendGetText("You currently have #b" + cm.convertNumber(cm.getPlayer().getRemainingAp()) + "#k AP to spend.\r\nHow much AP do you wish to apply to this stat ?\r\n#rMax stat is 2 Billion.#k\r\n");
    } else if (status == 2) {
        amount = cm.getNumber();
        if (sel == 1) {
            cm.sendYesNoS("Do you confirm that you want to spend " + amount + " Ap on Str?", 2);
        } else if (sel == 2) {
            cm.sendYesNoS("Do you confirm that you want to spend " + amount + " Ap on Dex?", 2);
        } else if (sel == 3) {
            cm.sendYesNoS("Do you confirm that you want to spend " + amount + " Ap on Int?", 2);
        } else if (sel == 4) {
            cm.sendYesNoS("Do you confirm that you want to spend " + amount + " Ap on Luk?", 2);
        } else if (sel == 5) {
            cm.sendYesNoS("Do you confirm that you want to spend " + amount + " Ap on Max HP?", 2);
        } else if (sel == 6) {
            cm.sendYesNoS("Do you confirm that you want to spend " + amount + " Ap on Max MP?", 2);
        }
    } else if (status == 3) {
        if (amount > 0) {
            if (cm.getPlayer().getRemainingAp() >= amount) {
                if (sel == 1) {
                    if (cm.getPlayer().getStat().getStr() + amount <= 2000000000) {
                        cm.getPlayer().setStat(amount, sel);
                        cm.sendOkS("You have successfully applied " + amount + " AP into Str. Total Base Str is now " + cm.getPlayer().getStat().getStr() + ".", 2);
                    } else {
                        cm.sendOkS("You currently have maxxed out STR at 2 Billion.", 2);
                    }
                } else if (sel == 2) {
                    if (cm.getPlayer().getStat().getDex() + amount <= 2000000000) {
                        cm.getPlayer().setStat(amount, sel);
                        cm.sendOkS("You have successfully applied " + amount + " AP into Dex. Total Base Dex is now " + cm.getPlayer().getStat().getDex() + ".", 2);
                    } else {
                        cm.sendOkS("You currently have maxxed out Dex at 2 Billion.", 2);
                    }
                } else if (sel == 3) {
                    if (cm.getPlayer().getStat().getInt() + amount <= 2000000000) {
                        cm.getPlayer().setStat(amount, sel);
                        cm.sendOkS("You have successfully applied " + amount + " AP into Int. Total Base Int is now " + cm.getPlayer().getStat().getInt() + ".", 2);
                    } else {
                        cm.sendOkS("You currently have maxxed out Int at 2 Billion.", 2);
                    }
                } else if (sel == 4) {
                    if (cm.getPlayer().getStat().getLuk() + amount <= 2000000000) {
                        cm.getPlayer().setStat(amount, sel);
                        cm.sendOkS("You have successfully applied " + amount + " AP into Luk. Total Base Luk is now " + cm.getPlayer().getStat().getLuk() + ".", 2);
                    } else {
                        cm.sendOkS("You currently have maxxed out Luk at 2 Billion.", 2);
                    }
                } else if (sel == 5) {
                    if (cm.getPlayer().getStat().getMaxHp() + amount <= 99999) {
                        cm.getPlayer().setStat(amount, sel);
                        cm.sendOkS("You have successfully applied " + amount + " AP into Max HP. Total Base HP is now " + cm.getPlayer().getStat().getMaxHp() + ".", 2);
                    } else {
                        cm.sendOkS("You currently have maxxed out Max HP.", 2);
                    }
                } else if (sel == 6) {
                    if (cm.getPlayer().getStat().getMaxMp() + amount <= 99999) {
                        cm.getPlayer().setStat(amount, sel);
                        cm.sendOkS("You have successfully applied " + amount + " AP into Luk. Total Base MP is now " + cm.getPlayer().getStat().getMaxMp() + ".", 2);
                    } else {
                        cm.sendOkS("You currently have maxxed out Max MP.", 2);
                    }
                }
            } else {
                cm.sendOkS("You do not have enough AP.", 2);
            }
        } else {
            cm.sendOkS("Nice try loser. GTFO.", 2);
        }
    }
}













