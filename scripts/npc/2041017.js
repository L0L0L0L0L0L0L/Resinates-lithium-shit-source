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
var item = 4034705;
var wanted = 0;
var skin = 0;
var chain = 0;
var wanted_item = 0;
var wanted_item_amount = 0;
var wanted_base = 1;
var questid = 6800;
var questtime = 300;
var option = 0;

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
            var text = "Select a Option\r\n";
            text += "#L1##bReset my Wanted Poster Item#k#l\r\n";
            text += "#L2##bReset my Rooney Quest Items#k#l\r\n";
            cm.sendSimpleS("I can help you reset your quest item requirements?\r\n" + text + " ", 16);
        }
        if (status == 1) {
            option = selection;
            if (option == 1) {
                if (cm.haveItem(item, 1)) {
                    if (cm.getPlayer().getVar("wanted_item") > 0) {
                        var olditem = cm.getPlayer().getVar("wanted_item");
                        var otext = " #i" + olditem + "# " + cm.getItemName(olditem);
                        cm.sendYesNo("I see you have a #b#i" + item + "# " + cm.getItemName(item) + "#k\r\n#bWould you like to use it to change your Quest item#k?\r\nCurrent Quest Item: #r" + otext);
                    } else {
                        cm.sendOk("You haven't accepted any wanted poster quests.");
                    }
                } else {
                    cm.sendOk("You dont have any #b#i" + item + "# " + cm.getItemName(item) + "#k");
                }
                return;
            }
            if (option == 2) {
                if (cm.haveItem(item, 1)) {
                    if (cm.getPlayer().getQuestStatus() > 0) {
                        cm.sendYesNo("I see you have a #b#i" + item + "# " + cm.getItemName(item) + "#k\r\n#bWould you like to use it to change your Quest item#k?\r\nCurrent Quest Items:\r\n" + cm.getQuestPool() + "");
                    } else {
                        cm.sendOk("You haven't accepted any of Rooney's quests.");
                    }
                } else {
                    cm.sendOk("You dont have any #b#i" + item + "# " + cm.getItemName(item) + "#k");
                }
                return;
            }
            cm.sendOk("Nothing to see here...");
        }
        if (status == 2) {
            if (option == 1) {
                if (cm.getPlayer().getVar("wanted_item") > 0) {
                    if (cm.getPlayer().getQuestLock(6524) > 0) {
                        cm.sendOk("Come back later.\r\n#b" + cm.secondsToString(cm.getPlayer().getQuestLock(6524)) + "#k.");
                        return;
                    }
                    if (cm.haveItem(item, 1)) {
                        cm.gainItem(item, -1);
                        var olditem = cm.getPlayer().getVar("wanted_item");
                        var otext = " #i" + olditem + "# " + cm.getItemName(olditem);
                        cm.generateQuestItem();
                        cm.getPlayer().setVar("wanted_item", cm.getPlayer().getMonsterHunt());
                        var newitem = cm.getPlayer().getVar("wanted_item");
                        var ntext = " #i" + newitem + "# " + cm.getItemName(newitem) + " (" + cm.convertNumber(cm.getPlayer().countAllItem(newitem)) + ")";
                        cm.getPlayer().setQuestLock(6524, 600);
                        cm.sendOk("You have abolished \r\n#r" + otext + "#k\r\n\Your new quest item to obtain is now:\r\n\r\n\#b" + ntext + "#k");
                    }
                } else {
                    cm.sendOk("You haven't accept any wanted poster quests.");
                }
            }
            if (option == 2) {
                if (cm.haveItem(item, 1)) {
                    cm.gainItem(item, -1);
                    cm.resetQuest();
                    cm.generateQuest();
                    cm.sendOk("#rYou Rooney Quest Items have been changed.#k\r\nCurrent Quest Items:\r\n" + cm.getQuestPool() + "");
                }
            }
        } else if (status == 3) {
        }
    }
}