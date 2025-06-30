var status = 0;
var groupsize = 0;
var item = 4031312;
var itemName = "#i4031312#";
var ach = 441;
var cost = 0;
var multi = 1;
var password = 0;

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
        var amount = cm.getNumber();
        if (amount == password) {
            if (cm.getPlayer().getVar("rb_item") > 0) {
                var rbitem = cm.getPlayer().getVar("rb_item");
                var rbamount = cm.getPlayer().getVar("rb_item_amount");
                var text = "#b #i" + rbitem + "# " + cm.getItemName(rbitem) + "s#k (#rx" + cm.convertNumber(rbamount) + "#k) (#b" + cm.convertNumber(cm.getPlayer().countAllItem(rbitem)) + "#k)\r\n";
                if (cm.getPlayer().getDay() == cm.getPlayer().getAccVara("RB_DAY")) {
                    cm.sendOk("#rCome back tomorrow to reset your Reborn quest ETC#k\r\n" + text);
                } else {
                    cm.sendYesNo("Do you wish to reset your current Reborn Quest Item?\r\n#rThis can only be done once per day.#k\r\n " + text);
                }
            } else {
                cm.sendOk("You have no Reborn Quest to reset!");
            }
        } else {
            cm.sendOk("Wrong password.");
        }
    }
    if (status == 2) {
        cm.getPlayer().setVar("rb_item", cm.generateRandomQuestItem());
        cm.getPlayer().setAccVar("RB_DAY", cm.getPlayer().getDay());
        var rbitem = cm.getPlayer().getVar("rb_item");
        var rbamount = cm.getPlayer().getVar("rb_item_amount");
        var text = "#b #i" + rbitem + "# " + cm.getItemName(rbitem) + "s#k (#rx" + cm.convertNumber(rbamount) + "#k) (#b" + cm.convertNumber(cm.getPlayer().countAllItem(rbitem)) + "#k)\r\n";
        cm.sendOk("#bYour Reborn Quest ETC has been Reset#k:\r\n" + text);

    }
}