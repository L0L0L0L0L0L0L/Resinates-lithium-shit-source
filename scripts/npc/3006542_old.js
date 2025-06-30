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
            if (rb < 50) {
                var time = cm.getPlayer().getVaraLock("RebornTime");
                if (time > 0) {
                    cm.sendOk("Come back later.\r\n#b" + cm.secondsToString(time) + "#k.");
                    return;
                }
            }
            if (cm.getPlayer().getAchievement(440) && cm.getPlayer().getTotalLevel() >= 9000 && rb < 150) {
                cost = (rb + 1);
                mcost = (rb + 1) * 10000;
                ccost = (rb + 1) * 100;
                dcost = (rb + 1) * 25;
                gcost = (rb + 1) * 10;
                var text = "Welcome to Reborn Centeral. #bCurrent Reborns: " + rb + "#k\r\nI handle the procress of letting you reset your level back to #r1000#k.\r\nEach Reborn will increase Item Drop Power limits and reduce Exp gained.\r\nEach Reborn has following to collect to reborn:\r\n";
                //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
                text += "#b #i4310500# " + cm.getItemName(4310500) + "s#k (#rx" + cm.convertNumber(mcost) + "#k) (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4310500)) + "#k)\r\n";
                text += "#b #i4037081# " + cm.getItemName(4037081) + "s#k (#rx" + cm.convertNumber(cost) + "#k) (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4037081)) + "#k)\r\n";
                if (rb >= 50) {
                    level = 9000;
                    ecost = (rb - 49) * 25;
                    if (cm.getPlayer().getVar("rb_item") < 1) {
                        cm.getPlayer().setVar("rb_item", cm.generateRandomQuestItem());
                        cm.getPlayer().setVar("rb_item_amount", Math.pow(rb, 2.0) * 1000);
                    }
                    text += "#b #i4036624# " + cm.getItemName(4036624) + "s#k (#rx" + cm.convertNumber(ecost) + "#k) (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4036624)) + "#k)\r\n";
                    var rbitem = cm.getPlayer().getVar("rb_item");
                    var rbamount = cm.getPlayer().getVar("rb_item_amount");
                    text += "#b #i" + rbitem + "# " + cm.getItemName(rbitem) + "s#k (#rx" + cm.convertNumber(rbamount) + "#k) (#b" + cm.convertNumber(cm.getPlayer().countAllItem(rbitem)) + "#k)\r\n";
                    if (rb >= 100) {
                        text += "#b #i4310266# " + cm.getItemName(4310266) + "s#k (#rx" + cm.convertNumber(gcost) + "#k) (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4310266)) + "#k)\r\n";
                    }
                    text += "#b #i2022452# Requried Level: 9000\r\n";
                } else {
                    level = 9000;
                    if (rb > 10) {
                        text += "#b #i4036018# " + cm.getItemName(4036018) + "s#k (#rx" + cm.convertNumber(ccost) + "#k) (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4036018)) + "#k)\r\n";
                    }
                    if (rb > 25) {
                        text += "#b #i4031311# " + cm.getItemName(4031311) + "s#k (#rx" + cm.convertNumber(dcost) + "#k) (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4031311)) + "#k)\r\n";
                    }
                    text += "#b #i2022452# Requried Level: 9000\r\n";

                }
                cm.sendYesNo(text);
            } else {
                cm.sendOkS("You not strong enough for this fight.\r\nGo defeat #rLimbo#k to prove your worth.", 16);
            }
        } else if (status == 1) {
            var check = true;
            if (cm.getPlayer().getTotalLevel() < level) {
                check = false;
            }
            if (!cm.haveItem(4037081, cost)) {
                check = false;
            }
            if (!cm.haveItem(4310500, mcost)) {
                check = false;
            }
            if (rb >= 50) {
                if (!cm.haveItem(4036624, ecost)) {
                    check = false;
                }
                if (rb > 50 && !cm.haveItem(cm.getPlayer().getVar("rb_item"), cm.getPlayer().getVar("rb_item_amount"))) {
                    check = false;
                }
                if (rb >= 100 && !cm.haveItem(4310266, gcost)) {
                    check = false;
                }
            } else {
                if (rb > 10 && !cm.haveItem(4036018, ccost)) {
                    check = false;
                }
                if (rb > 25 && !cm.haveItem(4031311, dcost)) {
                    check = false;
                }

            }
            if (check) {
                cm.gainItem(4037081, -cost);
                cm.gainItem(4310500, -mcost);
                if (rb >= 50) {
                    cm.gainItem(4036624, -ecost);
                    if (rb >= 100) {
                        cm.gainItem(4310266, -gcost);
                    }
                    cm.gainItem(cm.getPlayer().getVar("rb_item"), -cm.getPlayer().getVar("rb_item_amount"));
                    cm.getPlayer().setVar("rb_item", cm.generateRandomQuestItem());
                    cm.getPlayer().setVar("rb_item_amount", Math.pow(rb + 1, 2.0) * 1000);
                    cm.getPlayer().setVaraLock("RebornTime", 3600);
                } else {
                    if (rb < 10) {
                        cm.getPlayer().setVaraLock("RebornTime", 14400);
                    } else {
                        if (rb >= 10) {
                            cm.gainItem(4036018, -ccost);
                            cm.getPlayer().setVaraLock("RebornTime", 7200);
                        }
                        if (rb >= 25) {
                            cm.gainItem(4031311, -dcost);
                            cm.getPlayer().setVaraLock("RebornTime", 3600);
                        }
                    }
                }
                cm.getPlayer().handleReborn();
            } else {
                cm.sendOk("You seem to be missing parts of the quest or lack of levels to complete the reborn.");
            }
        } else if (status == 3) {

        }
    }
}