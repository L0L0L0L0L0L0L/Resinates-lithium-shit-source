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
            if (cm.getPlayer().getQuestLock(6523) > 0) {
                cm.sendOk("Come back later.\r\n#b" + cm.secondsToString(cm.getPlayer().getQuestLock(6523)) + "#k.");
                return;
            }
            wanted_item = 4033144;
            wanted_item_amount = 100;
            reward = wanted_base * 100;
            var total = cm.convertNumber(cm.getServerVar("coco"));
            rewards = "Server has returned #r" + total + "#k #bChocolates#k\r\n";
            rewards += "\r\n#fUI/UIWindow.img/Quest/reward#\r\n\ ";
            rewards += "#i4310018# " + cm.getItemName(4310018) + " (x#b" + reward + "#k)\r\n\ ";
            rewards += "#i4310502# " + cm.getItemName(4310502) + " (x#b" + Math.floor(wanted_base * 5) + "#k)\r\n\ ";
            rewards += "#i2430131# " + cm.getItemName(2430131) + " (x#b1#k)\r\n\ ";

            if (wanted > 0) {
                rewards += "#fUI/UIWindow2.img/Quest/icon/icon0# #rCompleted Quests: " + wanted + "#k\r\n\ ";
            }
            if (cm.haveItem(wanted_item, wanted_item_amount)) {
                cm.sendYesNo("I see you have collected " + wanted_item_amount + " #i" + wanted_item + "#.\r\n" + rewards);
            } else {
                cm.sendOk("Bring me #r100#k #i" + wanted_item + "# " + cm.getItemName(wanted_item) + "\r\nThis item can be found around #rZone 1-2-3#k.\r\n" + rewards);
            }
        } else if (status == 1) {
            var text = "";
            var complete = false;
            if (cm.haveItem(wanted_item, wanted_item_amount)) {
                cm.gainItem(wanted_item, -wanted_item_amount);
                text += "You have complete your task. Here is your rewards:\r\n";
                text += "#b#i4310018# " + cm.getItemName(4310018) + "#k (x#b" + reward + "#k)\r\n";
                cm.gainItem(4310018, reward);
                text += "#b#i4310502# " + cm.getItemName(4310502) + "#k (x#b" + Math.floor(wanted_base * 5) + "#k)\r\n";
                cm.gainItem(4310502, Math.floor(wanted_base * 5));
                text += "#b#i2430131# " + cm.getItemName(2430131) + "#k (x#b1#k)\r\n";
                cm.gainItem(2430131, 1);
                cm.addServerVar("coco", cm.getServerVar("coco") + 100);
                cm.getPlayer().gainGP(10000);
                text += "Gained Guild Points: +10000\r\n";
                complete = true;
            }
            if (complete) {
                cm.getPlayer().setQuestLock(6523, 300);
                cm.sendOk(text + "#bCome back in 5 mins for more.#k");
            } else {
                cm.sendOk("Bring me #r100#k #i" + wanted_item + "# " + cm.getItemName(wanted_item) + " for some rewards!");
            }
        } else if (status == 2) {
            status = 1;
            action(0, 0, 0);
        }
    }
}