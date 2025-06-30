var status = 0;
var groupsize = 0;
var item = 4420015;
var ach = 0;
var option = 0;
var em;
var cost = 0;

function start() {
    var number1 = cm.random(1, 9);
    var number2 = cm.random(1, 9);
    var number3 = cm.random(1, 9);
    var number4 = cm.random(1, 9);
    password = cm.getCode(number1, number2, number3, number4);
    //cm.sendGetText("#rBotting beyond this point is not allowed!\r\n\#kPlease enter the 4 digit #rAnti-Bot#k Code seen below:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "");

    cm.sendYesNo("Are you sure you want to cash in #i" + item + "#?");
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
        var selStr = "Which reward would you like to cash in?\r\n";
        selStr += ("#L1##i4420015#(#rx1#k)#l");
        selStr += ("#L2##i4420015#(#rx2#k)#l");
        selStr += ("#L3##i4420015#(#rx5#k)#l");
        selStr += ("#L4##i4420015#(#rx10#k)#l");
        cm.sendSimple(selStr);

    } else if (status == 2) {
        option = selection;
        if (selection == 1) {
            cost = 1;
        }
        if (selection == 2) {
            cost = 2;
        }
        if (selection == 3) {
            cost = 5;
        }
        if (selection == 4) {
            cost = 10;
        }
        item = 4420015;
        if (cm.haveItem(item, cost)) {
            cm.sendYesNo("Are you sure you want to cash in x" + cost + " #i" + item + "#?");
        } else {
            cm.sendOk("Event is requires at least " + cost + " of #i" + item + "#.");
        }

    } else if (status == 3) {
        if (!cm.getPlayer().isGroup()) {
            if (cm.haveItem(item, cost)) {
                var em = cm.getEventManager("reward");
                if (em != null) {
                    if (!em.startPlayerInstance(cm.getPlayer(), 1, option)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(item, -cost);
                    }
                } else {
                    cm.sendOk("Event has already started, Please wait.");
                }
            } else {
                cm.sendOk("Event is requires at least " + cost + " of #i" + item + "#.");
            }
        } else {
            cm.sendOk("Event is Solo Only.");
        }
    }
}