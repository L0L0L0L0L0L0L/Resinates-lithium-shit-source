var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var option;
var cost = 0;
var ach = 0;
var password = 0;
var amount = 0;
var solo = 0;
var dojo = 0;
var em;

function start() {
    cm.sendOk("#bCurrent Reborn Rankings:#k\r\n" + cm.getPlayer().getRebornRank());

    //password = cm.random(1000, 9999);
    //cm.sendGetText("Please enter the 4 digit Code seen below: \r\n\ " + cm.botTest(password) + "\r\n");
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
        selStr += "#L4# Reborn Rankings#l\r\n";
    } else if (status == 2) {
        if (selection == 4) {
            cm.sendOk("#bCurrent Reborn Rankings:#k\r\n" + cm.getPlayer().getDojoRank());
        }
    } else if (status == 3) {
        if (option == 5) {
            amount = cm.getNumber();
            if (amount == password) {
                if (!cm.getPlayer().isGroup()) {
                    em = cm.getEventManager("DojoUnlimited");
                    if (em != null) {
                        if (!em.startPlayerInstance(cm.getPlayer())) {
                            cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                        }
                    } else {
                        cm.sendOk("Event has already started, Please wait.");
                    }
                } else {
                    cm.sendOk("Event has already started, Please wait.");
                }
            } else {
                cm.sendOk("Wrong password.");
            }
        } else {
            dojo = 0;
            if (option == 1) {
                em = cm.getEventManager("Dojo");
            }
            if (option == 2) {
                em = cm.getEventManager("Master_Dojo");
            }
            if (selection == 1) {
                level = 250;
                scale = 5;
            } else if (selection == 2) {
                level = 500;
                ach = 330;
                dojo = 10;
                scale = 10;
            } else if (selection == 3) {
                level = 1000;
                ach = 331;
                dojo = 25;
                scale = 15;
            } else if (selection == 4) {
                level = 1500;
                ach = 332;
                dojo = 50;
                scale = 20;
            } else if (selection == 5) {
                level = 2500;
                ach = 333;
                dojo = 100;
                scale = 25;
            } else if (selection == 6) {
                level = 3000;
                ach = 334;
                dojo = 125;
                scale = 30;
            } else if (selection == 7) {
                level = 3500;
                ach = 334;
                dojo = 150;
                scale = 35;
            } else if (selection == 8) {
                level = 4000;
                ach = 334;
                dojo = 200;
                scale = 40;
            } else if (selection == 9) {
                level = 4500;
                ach = 334;
                dojo = 250;
                scale = 45;
            } else if (selection == 10) {
                level = 5000;
                ach = 334;
                dojo = 300;
                scale = 50;
            } else if (selection == 11) {
                level = 5500;
                ach = 334;
                dojo = 350;
                scale = 55;
            } else if (selection == 12) {
                level = 6000;
                ach = 334;
                dojo = 400;
                scale = 60;
            } else if (selection == 13) {
                level = 6500;
                ach = 334;
                dojo = 450;
                scale = 65;
            } else if (selection == 14) {
                level = 7000;
                ach = 334;
                dojo = 500;
                scale = 70;
            }

            var number1 = cm.random(1, 9);
            var number2 = cm.random(1, 9);
            var number3 = cm.random(1, 9);
            var number4 = cm.random(1, 9);
            password = cm.getCode(number1, number2, number3, number4);
            cm.sendGetText("#rBotting beyond this point is not allowed!\r\n\#kPlease enter the 4 digit #rAnti-Bot#k Code seen below:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "");
        }
    } else if (status == 4) {
        amount = cm.getNumber();
        if (amount == password) {
            if (cm.getPlayer().getTotalLevel() >= level) {
                if (cm.getPlayer().isGroup()) {
                    if (cm.getPlayer().getDojoLevel() >= dojo) {
                        if (em != null) {
                            if (em.getEligiblePartyAch(cm.getPlayer(), level, ach, 1, 4)) {
                                if (!em.startPlayerInstance(cm.getPlayer(), level, scale)) {
                                    cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                                }
                            } else {
                                cm.sendOk("Requires at level " + level + " or higher to join");
                            }
                        } else {
                            cm.sendOk("Event has already started, Please wait.");
                        }
                    } else {
                        cm.sendOk("Dojo requires " + dojo + " level to enter.");
                    }

                } else {
                    cm.sendOk("Dojo is Party Mode Only.");
                }
            } else {
                cm.sendOk("Requires at least " + level + " to enter.");
            }
        } else {
            cm.sendOk("Wrong password.");
        }
    }
}