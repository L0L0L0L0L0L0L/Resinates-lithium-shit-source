var status = -1;
var bank;
var select;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status == 0) {

        }
        status--;
    }
    if (status == 0) {
        bank = cm.getPlayer().getBank();
        cm.sendSimple("Welcome to Kaotic Bank. This Bank account is locked to your user account. How can we help you? \r\n#k Your current balance is " + cm.convertNumber(bank) + " Mesos.\r\n\r\n\#L1# Withdrawl Mesos #l\r\n\#L2# Deposit Mesos #l\r\n\#L3# Withdrawl Mesos as Gold Bags #l\r\n\#L4# Deposit Gold Bags into you Bank #l\r\n");
    } else if (status == 1) {
        select = selection;
        if (select == 1) {
            if (bank > 0) {
                cm.sendGetText("Hello#b #h ##k, How much mesos would you like to withdrawl today?\r\n#kMaximum Withdrawl limit is 2 Billion Mesos.\r\n\r\n#kYour current balance in your bank is " + cm.convertNumber(bank) + ".\r\n\r\n");
            } else {
                cm.sendOk("Sorry, your bank is currently empty.");

            }
        } else if (select == 2) {
            cm.sendGetText("Hello#b #h ##k, How much mesos would you like to deposit today?\r\n\r\n#kYour current balance is " + cm.convertNumber(bank) + ".\r\n#kYour currently have " + cm.convertNumber(cm.getPlayer().getMeso()) + " in your wallet.\r\n\r\n");
        } else if (select == 3) {
            if (bank > 0) {
                cm.sendGetText("Hello#b #h ##k, How many meso bags would you like to withdrawl today?\r\n\r\n#kEach meso bag is worth 1 billion mesos.\r\n#kYour current balance in your bank is " + cm.convertNumber(bank) + ".\r\n\r\n");
            } else {
                cm.sendOk("Sorry, your bank is currently empty.");

            }
        } else if (select == 4) {
            cm.sendGetText("Hello#b #h ##k, How many meso bags would you like to deposit today?\r\n\r\n#kEach meso bag is worth 1 million mesos.\r\n\#kYour currently have " + cm.getPlayer().getItemQuantity(4310500, false) + " meso bags you can deposite.\r\n\r\n");
        }
    } else if (status == 2) {
        amount = cm.getNumber();
        if (amount > 0) {
            if ((amount + "").indexOf(".") !== -1) {
                cm.sendOk("You must enter an integer");
            } else {
                if (select == 1) {//withdrawl
                    if (amount <= 2000000000) {
                        if (bank >= amount) {
                            if (cm.getPlayer().canHoldMeso(amount)) {
                                cm.sendYesNo("Do you confirm that you want to withdrawl " + cm.convertNumber(amount) + " Mesos from your bank?");
                            } else {
                                cm.sendOk("Sorry, you can't carry this much.");

                            }
                        } else {
                            cm.sendOk("Sorry, you do not have enough mesos in your bank.");

                        }
                    } else {
                        cm.sendOk("Sorry, you cannot withdrawl more than 2,000,000,000 at time.");

                    }
                } else if (select == 2) {//depo
                    if (amount <= 2000000000) {
                        if (cm.getPlayer().getMeso() >= amount) {
                            cm.sendYesNo("Do you confirm this trasaction?");
                        } else {
                            cm.sendOk("Sorry, you cannot deposite less than 2,000,000,000 at time.");

                        }
                    } else {
                        cm.sendOk("Sorry, no cheaty doddles here.");

                    }
                } else if (select == 3) {
                    if (bank >= (amount * 1000000)) {
                        cm.sendYesNo("Do you confirm this trasaction?");
                    } else {
                        cm.sendOk("Sorry, no cheaty doddles here.");

                    }
                } else if (select == 4) {
                    if (cm.haveItem(4310500, amount)) {
                        cm.sendYesNo("Do you confirm this trasaction?");
                    } else {
                        cm.sendOk("Sorry, no cheaty doddles here.");

                    }
                }
            }
        } else {
            cm.sendOk("Sorry, no cheaty doddles here.");

        }
    } else if (status == 3) {
        if (select == 1) {
            cm.getPlayer().gainMeso(amount);
            cm.getPlayer().updateBank(-amount);
            cm.sendOk("You have successfully withdrawn " + amount + " mesos from your bank. Remaining balance is " + cm.getPlayer().getBank() + " Mesos.");

        } else if (select == 2) {
            cm.getPlayer().gainMeso(-amount);
            cm.getPlayer().updateBank(amount);
            cm.sendOk("You have successfully deposited " + amount + " mesos into your bank. New balance is " + cm.getPlayer().getBank() + " Mesos.");

        } else if (select == 3) {
            if (cm.canHold(4310500, amount)) {
                var total = amount * 1000000;
                cm.getPlayer().updateBank(-(total));
                cm.gainItem(4310500, amount);
                cm.sendOk("You have successfully withdrawn " + amount + " meso bags from your bank. New balance is " + cm.getPlayer().getBank() + " Mesos.");
            } else {
                cm.sendOk("Inventory is full.");
            }
        } else if (select == 4) {
            var total = amount * 1000000;
            cm.getPlayer().updateBank(total);
            cm.gainItem(4310500, -amount);
            cm.sendOk("You have successfully deposited " + amount + " meso bags into your bank. New balance is " + cm.getPlayer().getBank() + " Mesos.");

        }
    } else {

    }
}