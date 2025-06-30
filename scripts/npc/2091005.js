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
    cm.sendNext("Welcome to Dojo.");

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
        amount = cm.getNumber();
        if (amount == password) {
            var selStr = "#bDojo Level: " + cm.getPlayer().getDojoLevel() + "#k - #rExp (" + cm.getPlayer().getDojoExp() + " / " + cm.getPlayer().getNeededDojoExp() + ")#k\r\n";
            if (cm.getPlayer().getVar("Dojo") > 0) {
                selStr += "Highest Tier Cleared on Unlimited: #b" + cm.getPlayer().getVar("Dojo") + "#k\r\n";
            }
            selStr += "Welcome to the Master Dojo. How may I help you?\r\n\r\n";

            selStr += "Welcome to the Master Dojo. How may I help you?\r\n\r\n";
            selStr += "#L2# #bChallenge The Dojo#k (#rShort#k)#l\r\n";
            selStr += "#L1# #bSave The Dojo#k (#rLong#k)#l\r\n";
            selStr += "#L5# #bUnlimited Barrage#k (#rSolo#k)#l\r\n\r\n";
            selStr += "#L3# Dojo Shop#l\r\n";
            selStr += "#L4# Dojo Ranking#l\r\n";
            selStr += "#L6# Dojo Unlimited Ranking#l";
            cm.sendSimple(selStr);
            //cm.sendSimple("Welcome to the Master Dojo. How may I help you?\r\n\r\n\r\n#L3# Dojo Shop#l\r\n#L4# Dojo Ranking#l");
        } else {
            cm.sendOk("Wrong password.");
        }
    } else if (status == 2) {
        option = selection;
        if (selection == 1) {
            if (cm.getPlayer().getParty() != null) {
                if (cm.getPlayer().isLeader()) {
                    var selStr = "#bDojo Level: " + cm.getPlayer().getDojoLevel() + "#k - #rExp (" + cm.getPlayer().getDojoExp() + " / " + cm.getPlayer().getNeededDojoExp() + ")#k\r\n";
                    selStr += "#L1##bMin-Level: 250+ Tier: 5#k#l\r\n";
                    selStr += "#L2##bMin-Level: 500+ Tier: 10#k (#rDojo-Lvl: 10+#k)#l\r\n";
                    selStr += "#L3##bMin-Level: 1000+ Tier: 15#k (#rDojo-Lvl: 25+#k)#l\r\n";
                    selStr += "#L4##bMin-Level: 1500+ Tier: 20#k (#rDojo-Lvl: 50+#k)#l\r\n";
                    selStr += "#L5##bMin-Level: 2500+ Tier: 25#k (#rDojo-Lvl: 100+#k)#l\r\n";
                    selStr += "#L6##bMin-Level: 3000+ Tier: 30#k (#rDojo-Lvl: 125+#k)#l\r\n";
                    selStr += "#L7##bMin-Level: 3500+ Tier: 35#k (#rDojo-Lvl: 150+#k)#l\r\n";
                    selStr += "#L8##bMin-Level: 4000+ Tier: 40#k (#rDojo-Lvl: 200+#k)#l\r\n";
                    selStr += "#L9##bMin-Level: 4500+ Tier: 45#k (#rDojo-Lvl: 250+#k)#l\r\n";
                    selStr += "#L10##bMin-Level: 5000+ Tier: 50#k (#rDojo-Lvl: 300+#k)#l\r\n";
                    selStr += "#L11##bMin-Level: 5500+ Tier: 55#k (#rDojo-Lvl: 350+#k)#l\r\n";
                    selStr += "#L12##bMin-Level: 6000+ Tier: 60#k (#rDojo-Lvl: 400+#k)#l\r\n";
                    selStr += "#L13##bMin-Level: 6500+ Tier: 65#k (#rDojo-Lvl: 450+#k)#l\r\n";
                    selStr += "#L14##bMin-Level: 7000+ Tier: 70#k (#rDojo-Lvl: 500+#k)#l\r\n";
                    cm.sendSimple("Which Dojo Challenge do want to compete in?\r\n" + selStr);
                } else {
                    cm.sendOkS("I must take on this Challenge with a party.", 2);
                }
            } else {
                cm.sendOkS("I must take on this Challenge with a party.", 2);
            }
        } else if (selection == 2) {
            if (cm.getPlayer().getParty() != null) {
                if (cm.getPlayer().isLeader()) {
                    var selStr = "#bDojo Level: " + cm.getPlayer().getDojoLevel() + "#k - #rExp (" + cm.getPlayer().getDojoExp() + " / " + cm.getPlayer().getNeededDojoExp() + ")#k\r\n";
                    selStr += "#L1##bMin-Level: 250+ Tier: 5#k#l\r\n";
                    selStr += "#L2##bMin-Level: 500+ Tier: 10#k (#rDojo-Lvl: 10+#k)#l\r\n";
                    selStr += "#L3##bMin-Level: 1000+ Tier: 15#k (#rDojo-Lvl: 25+#k)#l\r\n";
                    selStr += "#L4##bMin-Level: 1500+ Tier: 20#k (#rDojo-Lvl: 50+#k)#l\r\n";
                    selStr += "#L5##bMin-Level: 2500+ Tier: 25#k (#rDojo-Lvl: 100+#k)#l\r\n";
                    selStr += "#L6##bMin-Level: 3000+ Tier: 30#k (#rDojo-Lvl: 125+#k)#l\r\n";
                    selStr += "#L7##bMin-Level: 3500+ Tier: 35#k (#rDojo-Lvl: 150+#k)#l\r\n";
                    selStr += "#L8##bMin-Level: 4000+ Tier: 40#k (#rDojo-Lvl: 200+#k)#l\r\n";
                    selStr += "#L9##bMin-Level: 4500+ Tier: 45#k (#rDojo-Lvl: 250+#k)#l\r\n";
                    selStr += "#L10##bMin-Level: 5000+ Tier: 50#k (#rDojo-Lvl: 300+#k)#l\r\n";
                    selStr += "#L11##bMin-Level: 5500+ Tier: 55#k (#rDojo-Lvl: 350+#k)#l\r\n";
                    selStr += "#L12##bMin-Level: 6000+ Tier: 60#k (#rDojo-Lvl: 400+#k)#l\r\n";
                    selStr += "#L13##bMin-Level: 6500+ Tier: 65#k (#rDojo-Lvl: 450+#k)#l\r\n";
                    selStr += "#L14##bMin-Level: 7000+ Tier: 70#k (#rDojo-Lvl: 500+#k)#l\r\n";
                    cm.sendSimple("Which #bMaster Dojo Challenge#k do want to compete in?\r\n" + selStr);
                } else {
                    cm.sendOkS("I must take on this Challenge with a party.", 2);
                }
            } else {
                cm.sendOkS("I must take on this Challenge with a party.", 2);
            }
        } else if (selection == 5) {
            if (!cm.getPlayer().isGroup()) {
                var number1 = cm.random(1, 9);
                var number2 = cm.random(1, 9);
                var number3 = cm.random(1, 9);
                var number4 = cm.random(1, 9);
                password = cm.getCode(number1, number2, number3, number4);
                cm.sendGetText("#rBotting beyond this point is not allowed!\r\n\#kPlease enter the 4 digit #rAnti-Bot#k Code seen below to start the dojo:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "");
            } else {
                cm.sendOkS("I must take on this Challenge alone.", 2);
            }
        } else if (selection == 3) {
            cm.openShopNPC(20000);//dojo shop
        } else if (selection == 4) {
            cm.sendOk("#bCurrent Rankings:#k\r\n" + cm.getPlayer().getDojoRank());
        } else if (selection == 6) {
            cm.sendOk("#bCurrent Highest Wave Cleared:#k\r\n" + cm.getPlayer().getRankVariable("Dojo"));
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
        var psize = option == 1 ? 6 : 4;
        amount = cm.getNumber();
        if (amount == password) {
            if (cm.getPlayer().getTotalLevel() >= level) {
                if (cm.getPlayer().isGroup()) {
                    if (cm.getPlayer().getDojoLevel() >= dojo) {
                        if (em != null) {
                            if (em.getEligiblePartyAch(cm.getPlayer(), level, ach, 1, psize)) {
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