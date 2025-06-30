var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var cost = 0;
var reward = 0;
var reward2 = 0;
var rewards;
var exp = 0;
var count = 1;
var multi = 0;
var item = 4420011;
var wanted = 0;
var skin = 0;
var chain = 0;
var wanted_item = 0;
var wanted_item_amount = 0;
var wanted_base = 1;
var questid = 6800;
var questtime = 300;
var price = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {

    } else {
        if (mode == 0 && type > 0) {

            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.getPlayer().getTotalLevel() >= 250 && cm.getPlayer().getVar("wanted_item") > 0) {
                cm.sendYesNo("Do you want to do change #bbase power#k of your wanted quest?\r\nPrice to change quest power is in #b#i4310502# " + cm.getItemName(4310502) + "#k");
            } else {
                cm.sendOk("You have no quest to altar!");
            }
        } else if (status == 1) {
            cm.sendGetText("Put in the the amount you want your wanted quest power to be.\r\n#rRange can be set from 100-100,000#k.");
        } else if (status == 2) {
            cost = cm.getNumber();
            if (cost >= 100 && cost <= 100000) {
                price = 10;
                if (cost > 10000) {
                    price = Math.floor(cost * 0.001);
                }
                cm.sendYesNo("Do you confirm you want to change your quest power to #b" + cost + "#k for price of #b" + price + "#k #i4310502#?");
            } else {
                cm.sendOk("What are you thinking!");
            }
        } else if (status == 3) {
            if (cm.haveItem(4310502, price)) {
                cm.gainItem(4310502, -price);
                var base = cost;
                cm.getPlayer().setVar("wanted_base", base);
                cm.getPlayer().setVar("wanted_item_amount", base * 500);

                wanted_base = cm.getPlayer().getVar("wanted_base");
                wanted_item = cm.getPlayer().getVar("wanted_item");
                wanted_item_amount = cm.getPlayer().getVar("wanted_item_amount");
                //wanted_item_amount = 1;
                reward = wanted_base * 25;

                rewards = "\r\n#fUI/UIWindow.img/Quest/reward#\r\n\ ";
                rewards += "#i4310500# " + cm.getItemName(4310500) + " (x#b" + Math.floor(wanted_base * 2.5) + "#k)\r\n\ ";
                rewards += "#i4310018# " + cm.getItemName(4310018) + " (x#b" + reward + "#k)\r\n\ ";

                rewards += "#rFull Stamina recovery.#k\r\n\ ";
                var erate = Math.floor(1 + (cm.getPlayer().getVar("wanted_base") * 0.01));
                rewards += "#r+" + erate + "% Etc Rate.#k\r\n\ ";
                cm.sendOk("Collect #b" + wanted_item_amount + "#k #i" + wanted_item + "# " + cm.getItemName(wanted_item) + "\r\nThis item can be found around #r" + cm.getPlayer().getTownFromItem(wanted_item) + "#k.\r\n#bBase#k (#rUpdated#k): #b" + wanted_base + "#k\r\n" + rewards);
            } else {
                cm.sendOk("What are you thinking of trying here!");
            }
        }
    }
}