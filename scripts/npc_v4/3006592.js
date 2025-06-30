/* 
 NPC Name: 		Shanks
 Map(s): 		Maple Road : Southperry (60000)
 Description: 		Brings you to Victoria Island
 */
var status = -1;
var option = 0;
var item = 4037167;
var cost = 0;
var mast = 0;
var ipot = 0;

function action(mode, type, selection) {
    if (status >= 0 && mode == 0) {

        return;
    }
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        ipot = cm.getPlayer().getAccVara("I_POT") * 100;
        mast = cm.getPlayer().getTotalBaseMastery() * 10;
        var spots = cm.getPlayer().getAccVara("SPIRT");
        var text = "";
        text += "How many #i" + item + "# #b" + cm.getItemName(item) + "#k do you want to exchange for Spirit Power?\r\n#rEach Fragment is worth 5% Spirit Power.#k\r\nCurrent Fragments: (#b" + cm.convertNumber(cm.getPlayer().countAllItem(item)) + "#k)\r\n";
        text += "Current Spirit Power: #b" + spots + "#k/#r999999#k%\r\n";
        text += "Mastery Power: #b" + (mast) + "%#k\r\n";
        text += "I-Pot Power: #b" + (ipot) + "%#k\r\n";
        var bpot = Math.floor((spots + mast + ipot) * 0.01);
        text += "Total Power: #b" + (spots + mast + ipot) + "%#k (#rBpot Power: " + bpot + "#k)\r\n";
        cm.sendGetText(text);
    } else if (status == 1) {
        cost = cm.getNumber();
        if (cost > 0 && cost <= 9999) {
            if (cm.haveItem(item, cost)) {
                cm.sendYesNoS("ARe you sure you wish to spend " + cost + " #i" + item + "# for more Spirit Power?", 16);
            } else {
                cm.sendOkS("You currently do not have enough #i" + item + "#", 16);
            }
        } else {
            cm.sendOkS("Max amount that be bought at a time is 9999.", 16);
        }
    } else if (status == 2) {
        if (cm.haveItem(item, cost)) {
            cm.gainItem(item, -cost);
            cm.getPlayer().setAccVar("SPIRT", cm.setMaxValue(cm.getPlayer().getAccVara("SPIRT") + (cost * 5), 999999));
            var spots = cm.getPlayer().getAccVara("SPIRT");
            var text = "";
            text += "Current Spirit Power: #b" + spots + "#k/#r999999#k%\r\n";
            text += "Mastery Power: #b" + (mast) + "%#k\r\n";
            text += "I-Pot Power: #b" + (ipot) + "%#k\r\n";
            var bpot = Math.floor((spots + mast + ipot) * 0.01);
            text += "Total Power: #b" + (spots + mast + ipot) + "%#k (#rBpot Power: " + bpot + "#k)\r\n";
            cm.sendOkS("You have exchanged #b" + cost + "#k #i" + item + "# #b" + cm.getItemName(item) + "#k\r\nfor +#r" + (cost * 5) + "% Spirit Power#k\r\n" + text, 16);
        } else {
            cm.sendOkS("You currently do not have enough #i" + item + "#", 16);
        }
    }
}