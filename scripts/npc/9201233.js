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
            if (!cm.getPlayer().getAchievement(226)) {
                cm.sendOkS("You must clear #rTutorial Zone#k to unlock Wanted Quests.", 2);
                return;
            }
            if (cm.getPlayer().getTotalLevel() < 100) {
                cm.sendOkS("You must be at least level 100 or higher to start questing.", 2);
                return;
            }
            level = 100;
            if (cm.getPlayer().getVar("wanted") < 1) {
                cm.getPlayer().setVar("wanted", 0);
            }
            if (cm.getPlayer().getVar("wanted_item") < 1) {
                cm.generateQuestItem();
                cm.getPlayer().setVar("wanted_item", cm.getPlayer().getMonsterHunt());
                cm.getPlayer().setVar("wanted_base", 1);
                cm.getPlayer().setVar("wanted_item_amount", 999);
            }
            wanted_base = cm.getPlayer().getVar("wanted_base");
            wanted_item = cm.getPlayer().getVar("wanted_item");
            wanted_item_amount = cm.getPlayer().getVar("wanted_item_amount");
            //wanted_item_amount = 1;
            wanted = cm.getPlayer().getVar("wanted");

            rewards = "\r\n#fUI/UIWindow.img/Quest/reward#\r\n\ ";
            rewards += "#i4310018# " + cm.getItemName(4310018) + " (x#b250#k)\r\n\ ";
            rewards += "#i2430131# " + cm.getItemName(2430131) + " (x#b10#k)\r\n\ ";
            if (wanted > 0) {
                rewards += "#fUI/UIWindow2.img/Quest/icon/icon0# #rCompleted Quests: " + wanted + "#k\r\n\ ";
            }
            if (cm.haveItem(wanted_item, wanted_item_amount)) {
                cm.sendYesNo("I see you have collected " + wanted_item_amount + " #i" + wanted_item + "#.\r\n" + rewards);
            } else {
                var itz = "#r" + cm.convertNumber(cm.getPlayer().countAllItem(wanted_item)) + "#k";
                cm.sendOk("Bring me " + itz + "/#b" + wanted_item_amount + "#k #i" + wanted_item + "# " + cm.getItemName(wanted_item) + "\r\nThis item can be found around #r" + cm.getPlayer().getTownFromItem(wanted_item) + "#k.\r\n#bBase: " + wanted_base + "#k\r\n" + rewards);
            }
        } else if (status == 1) {
            var text = "";
            if (cm.haveItem(wanted_item, wanted_item_amount)) {
                cm.gainItem(wanted_item, -wanted_item_amount);
                text += "You have complete your task. Here is your rewards:\r\n";
                text += "#b#i4310018# " + cm.getItemName(4310018) + "#k (x#b250#k) (#bOverflow#k)\r\n";
                cm.gainItem(4310018, 250);
                text += "#b#i2430131# " + cm.getItemName(2430131) + "#k (x#b10#k) (#bOverflow#k)\r\n";
                cm.gainItem(2430131, 10);
                text += "\r\n";
                cm.getPlayer().addVar("wanted", 1);
                wanted = cm.getPlayer().getVar("wanted");
                if (wanted >= 1) {
                    cm.getPlayer().finishAchievement(2100);
                }
                if (wanted >= 5) {
                    cm.getPlayer().finishAchievement(2101);
                }
                if (wanted >= 10) {
                    cm.getPlayer().finishAchievement(2102);
                }
                if (wanted >= 15) {
                    cm.getPlayer().finishAchievement(2103);
                }
                if (wanted >= 25) {
                    cm.getPlayer().finishAchievement(2104);
                }
                if (wanted >= 50) {
                    cm.getPlayer().finishAchievement(2105);
                }
                if (wanted >= 100) {
                    cm.getPlayer().finishAchievement(2106);
                }
                if (wanted >= 250) {
                    cm.getPlayer().finishAchievement(2107);
                }
                cm.getPlayer().setVar("wanted_item", 0);
                cm.getPlayer().gainGP(25000);
                text += "Gained Guild Points: +25000\r\n";
                cm.sendYesNo(text + "#bDo you want to do another special quest?#k");
            } else {
                cm.sendOk(text + "Bring me " + cost + " #i" + cm.getPlayer().getMonsterHunt() + "# " + cm.getItemName(cm.getPlayer().getMonsterHunt()) + "\r\nThis item can be found around #r" + cm.getPlayer().getTownFromItem(cm.getPlayer().getMonsterHunt()) + "#k.\r\n" + rewards);
            }
        } else if (status == 2) {
            status = 1;
            action(0, 0, 0);
        }
    }
}