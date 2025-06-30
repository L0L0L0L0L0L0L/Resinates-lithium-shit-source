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
            level = cm.getPlayer().getTotalLevel();
            if (cm.getPlayer().getTotalLevel() >= 100) {
                if (cm.getPlayer().getVar("wanted") < 1) {
                    cm.getPlayer().setVar("wanted", 0);
                }
                if (cm.getPlayer().getVar("wanted_item") < 1) {
                    cm.generateQuestItem();
                    cm.getPlayer().setVar("wanted_item", cm.getPlayer().getMonsterHunt());
                    var base = cm.random(1, cm.getPlayer().getTotalLevel());
                    cm.getPlayer().setVar("wanted_base", base);
                    cm.getPlayer().setVar("wanted_item_amount", base * 500);
                }
                wanted_base = cm.getPlayer().getVar("wanted_base");
                wanted_item = cm.getPlayer().getVar("wanted_item");
                wanted_item_amount = cm.getPlayer().getVar("wanted_item_amount");
                //wanted_item_amount = 1;
                wanted = cm.getPlayer().getVar("wanted");
                reward = wanted_base * 25;

                rewards = "\r\n#fUI/UIWindow.img/Quest/reward#\r\n\ ";
                rewards += "#i4310500# " + cm.getItemName(4310500) + " (x#b" + Math.floor(wanted_base * 2.5) + "#k)\r\n\ ";
                rewards += "#i4310018# " + cm.getItemName(4310018) + " (x#b" + reward + "#k)\r\n\ ";

                if (wanted == 1 || wanted == 3) {
                    skin = 4420002;
                }
                if (wanted == 2 || wanted == 4) {
                    skin = 4420003;
                }
                if (wanted == 5 || wanted == 7 || wanted == 9) {
                    skin = 4420004;
                }
                if (wanted == 10 || wanted == 15 || wanted == 20 || wanted == 30 || wanted == 35 || wanted == 40) {
                    skin = 4420005;
                }
                if (wanted == 25 || wanted == 50 || wanted == 75 || wanted == 100 || wanted == 150 || wanted == 200 || wanted == 250) {
                    skin = 4420006;
                }
                if (skin != 0) {
                    rewards += "#i" + skin + "# " + cm.getItemName(skin) + " (x1)\r\n\ ";
                }
                rewards += "#rFull Stamina recovery.#k\r\n\ ";
                var erate = Math.floor(1 + (cm.getPlayer().getVar("wanted_base") / 100.0));
                rewards += "#r+" + erate + "% Etc Rate.#k\r\n\ ";
                if (wanted > 0) {
                    rewards += "#fUI/UIWindow2.img/Quest/icon/icon0# #rCompleted Quests: " + wanted + "#k\r\n\ ";
                }
                if (cm.haveItem(wanted_item, wanted_item_amount)) {
                    cm.sendYesNo("I see you have collected " + wanted_item_amount + " #i" + wanted_item + "#.\r\n" + rewards);
                } else {
                    cm.sendOk("Bring me #b" + wanted_item_amount + "#k #i" + wanted_item + "# " + cm.getItemName(wanted_item) + "\r\nThis item can be found around #r" + cm.getPlayer().getTownFromItem(wanted_item) + "#k.\r\n#bBase: " + wanted_base + "#k\r\n" + rewards);
                }
            } else {
                cm.sendOk("Must be at least level 100+ in order to take on wanted monsters.");
            }
        } else if (status == 1) {
            var text = "";
            var complete = false;
            if (cm.haveItem(wanted_item, wanted_item_amount)) {
                cm.gainItem(wanted_item, -wanted_item_amount);
                text += "You have complete your task. Here is your rewards:\r\n";

                text += "#b#i4310500# " + cm.getItemName(4310500) + "#k (x#b" + Math.floor(wanted_base * 2.5) + "#k) (#bOverflow#k)\r\n";
                cm.getPlayer().addOverflow(4310500, Math.floor(wanted_base * 2.5), "From Wanted Poster Quest!");

                text += "#b#i4310018# " + cm.getItemName(4310018) + "#k (x#b" + reward + "#k) (#bOverflow#k)\r\n";
                cm.getPlayer().addOverflow(4310018, reward, "From Wanted Poster Quest!");

                if (skin != 0) {
                    text += "#b#i" + skin + "# " + cm.getItemName(skin) + "#k (x#b1#k)\r\n";
                    cm.gainItem(skin, 1);
                }

                var erate = Math.floor(1 + (cm.getPlayer().getVar("wanted_base") / 100.0));
                cm.getPlayer().gainStat(erate, 10);

                text += "\r\nNow ";
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
                complete = true;
            }
            if (complete) {
                cm.getPlayer().setVar("wanted_item", 0);
                cm.sendYesNo("Do you want to do another special quest?");
            } else {
                cm.sendOk(text + "Bring me " + cost + " #i" + cm.getPlayer().getMonsterHunt() + "# " + cm.getItemName(cm.getPlayer().getMonsterHunt()) + "\r\nThis item can be found around #r" + cm.getPlayer().getTownFromItem(cm.getPlayer().getMonsterHunt()) + "#k.\r\n" + rewards);
            }
        } else if (status == 2) {
            status = 1;
            action(0, 0, 0);
        }
    }
}