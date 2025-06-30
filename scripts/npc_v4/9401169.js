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
var questtime = 28800;
var key = 4037081;
var rb = 0;
var cost = 0;
var mcost = 0;
var ccost = 0;
var dcost = 0;
var ecost = 0;
var gcost = 0;
var items = new Array();

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
            rb = cm.getPlayer().getReborns();
            var time = cm.getPlayer().getVaraLock("RebornTime");
            if (time > 0) {
                cm.sendOk("Come back later.\r\n#b" + cm.secondsToString(time) + "#k.");
                return;
            }
            if (cm.getPlayer().getAchievement(440) && cm.getPlayer().getTotalLevel() >= 9000 && rb < 99) {
                cost = rb;
                if (cost < 10) {
                    cost = 10;
                }
                cm.sendYesNo("Do you wish to spend " + cost + " #i4310505# " + cm.getItemName(4310505) + " to reborn?\r\n#rThere is a 1 hour cooldown on this option.#k");
            } else {
                cm.sendOkS("You not strong enough to reborn.\r\nGo defeat #rLimbo#k to prove your worth.\r\nGo become level 9000+.", 16);
            }
        } else if (status == 1) {
            var check = true;
            if (cm.getPlayer().getTotalLevel() < level) {
                check = false;
            }
            if (!cm.haveItem(4310505, cost)) {
                check = false;
            }
            if (check) {
                cm.gainItem(4310505, -cost);
                cm.getPlayer().setVaraLock("RebornTime", 3600);
                cm.getPlayer().handleReborn();
                if (rb >= 50) {
                    cm.getPlayer().setVar("rb_item", cm.generateRandomQuestItem());
                    cm.getPlayer().setVar("rb_item_amount", Math.pow(rb + 1, 2.0) * 1000);

                }
            } else {
                cm.sendOk("You seem to be missing parts of the quest or lack of levels to complete the reborn.");
            }
        } else if (status == 3) {

        }
    }
}