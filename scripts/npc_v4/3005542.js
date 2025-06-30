var status = 0;
var groupsize = 0;
var item = 4310500;
var ach = 0;
var cost = 0;
var mob = 0;
var tier = 0;
var password = 0;

function start() {
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
        var amount = cm.getNumber();
        if (amount == password) {

            if (cm.getPlayer().getTotalLevel() >= 8000) {
                var text = "Im in charge of Shangai Battle Simulation.\r\n#rMonsters here are powerful but do not drop any drops.#k\r\nEach Simulation lasts 30 mins.\r\n#bSelect a Monster:#k\r\n";
                //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
                text += "#L8645366# #bSpring Researcher#k #l\r\n";
                text += "#L8645368# #bSummer Researcher#k #l\r\n";
                text += "#L8645370# #bAutumn Researcher#k #l\r\n";
                text += "#L8645372# #bWinter Researcher#k #l\r\n";
                //text += "#L8645375# #bLost Researcher#k #l\r\n";
                //text += "#L8645340# #bAssailant#k #l\r\n";
                cm.sendSimple(text);
            } else {
                cm.sendOk("Requires lvl 8000+.");
            }
        } else {
            cm.sendOk("Wrong password.");
        }
    }
    if (status == 2) {
        mob = selection;
        rb = cm.getPlayer().getReborns();
        var text = "Select a Tier of the monsters to battle in.\r\nBattle level is set to 25% higher than your current up to max of 9999.\r\n";
        //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
        text += "#L100# Tier: 100#k - Price: #b1000 #t4036518##k#l\r\n";
        if (rb >= 5) {
            text += "#L125# Tier: 125#k - Price: #b2500 #t4036518##k#l\r\n";
        }
        if (rb >= 10) {
            text += "#L150# Tier: 150#k - Price: #b5000 #t4036518##k#l\r\n";
        }
        if (rb >= 25) {
            text += "#L200# Tier: 200#k - Price: #b10000 #t4036518##k#l\r\n";
        }
        if (rb >= 50) {
            text += "#L250# Tier: 250#k - Price: #b25000 #t4036518##k#l\r\n";
        }
        if (rb >= 75) {
            text += "#L300# Tier: 300#k - Price: #b50000 #t4036518##k#l\r\n";
        }
        if (rb >= 100) {
            text += "#L400# Tier: 400#k - Price: #b100000 #t4036518##k#l\r\n";
        }
        if (rb >= 150) {
            text += "#L500# Tier: 500#k - Price: #b250000 #t4036518##k#l\r\n";
        }
        cm.sendSimple(text);
    }
    if (status == 3) {
        var em = cm.getEventManager("adv_dung");
        if (em != null) {
            var cookies = 1000;
            if (selection == 125) {
                cookies = 2500;
            }
            if (selection == 150) {
                cookies = 5000;
            }
            if (selection == 200) {
                cookies = 10000;
            }
            if (selection == 250) {
                cookies = 25000;
            }
            if (selection == 300) {
                cookies = 50000;
            }
            if (selection == 400) {
                cookies = 100000;
            }
            if (selection == 500) {
                cookies = 250000;
            }
            if (cm.haveItem(4036518, cookies)) {
                if (!em.startPlayerInstance(cm.getPlayer(), mob, selection)) {
                    cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                } else {
                    cm.gainItem(4036518, -cookies);
                }
            } else {
                cm.sendOk("Where is my #r" + cookies + "#k #b#i4036518# " + cm.getItemName(4036518) + "s#k????");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }

    }
}