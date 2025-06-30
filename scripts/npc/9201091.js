var status = 0;
var level = 1000;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var key = 4310506;
var etc = 4031137;
var amount = 0;
function start() {
    var total = cm.convertNumber(cm.getServerVar("exmas"));
    var rewards = "Server has stolen #r" + total + "#k #bPresents#k (#bEvent!#k)\r\n";
    rewards += "You have stolen #r" + cm.getPlayer().getVarZero("exmas") + "#k #bPresents#k (#bEvent!#k)\r\n\r\n";
    cm.sendYesNo(rewards + "Would you like to exchange 25 #i" + etc + "# for 10 #i" + key + "#.");
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
        cm.sendGetText("How many times do you want me to do it?\r\n\r\n");
    } else if (status == 2) {
        amount = cm.getNumber();
        if (amount > 0 && amount <= 100000) {
            cm.sendYesNoS("Are you sure you want to exchange " + (25 * amount) + " #i" + etc + "# for #i" + key + "# x(" + (amount * 10) + ").", 2);
        } else {
            cm.sendOk("enter a number greater than 0 and less than 100000.");
        }
    } else if (status == 3) {
        if (amount > 0 && amount <= 100000) {
            if (cm.haveItem(etc, 25 * amount)) {
                if (cm.canHold(key)) {
                    var tot = 25 * amount;
                    cm.gainItem(etc, -tot);
                    cm.gainItem(key, amount * 10);
                    cm.addServerVar("exmas", tot);
                    cm.getPlayer().addVar("exmas", tot);
                    cm.getPlayer().gainGP(1000 * tot);
                    cm.sendOk("Enjoy your shards!");
                } else {
                    cm.sendOk("Please make room in ETC Inventory.");

                }
            } else {
                cm.sendOk("Please bring me " + (25 * amount) + " #i" + etc + "# to craft the ancient key.");
            }
        } else {
            cm.sendOk("enter a number greater than 0 and less than 100000.");
        }
    }
}