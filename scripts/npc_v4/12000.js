var status = 0;
var groupsize = 0;
var item = 4420015;
var ach = 0;
var option = 0;
var em;
var cost = 1;

function start() {
    var number1 = cm.random(1, 9);
    var number2 = cm.random(1, 9);
    var number3 = cm.random(1, 9);
    var number4 = cm.random(1, 9);
    password = cm.getCode(number1, number2, number3, number4);
    cm.sendGetText("#rWhen you see a Gacha Code like this, it means botting inside these instances or event is not allowed.#k\r\nFailing the code does not cause any harm but to your ego.\r\n\Please enter the 4 digit #rAnti-Bot#k Code seen below:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "");
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
        cm.gainItem(4420015, 1);
        var selStr = "There is a custom reward map that is very simple, bring me #i4420015#\r\n(I provided you 1 Free Ticket)\r\nThen once you enter the #bReward Map#k break the chest to get tons of free rewards.\r\n#rYou can speak with Nana in perion to use bulk amounts.#k\r\n#bYou like to enter a Trail run here for free rewards?#k\r\nYou can always come back here for quick boost.";
        cm.sendYesNo(selStr);

    } else if (status == 2) {
        if (!cm.getPlayer().isGroup()) {
            if (cm.haveItem(item, cost)) {
                var em = cm.getEventManager("reward_start");
                if (em != null) {
                    if (!em.startPlayerInstance(cm.getPlayer(), 1, 1)) {
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