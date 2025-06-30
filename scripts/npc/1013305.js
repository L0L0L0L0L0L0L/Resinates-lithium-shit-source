var status = -1;
var option = 0;
var monster;
var monsters;
var star = "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
var option = 0;
var items;
var item;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        var text = "";
        //text += "#L1#" + star + " Equips#l\r\n";
        text += "#L2#" + star + " Usable Items#l\r\n";
        text += "#L4#" + star + " Etc/Quest Items#l\r\n";
        if (cm.getPlayer().getVar("wanted_item") > 0) {
            text += "#L10#" + star + " #bMy Wanted Poster Item#k#l\r\n";
        }
        if (cm.getQuestItem(1) > 0) {
            text += "#L11#" + star + " #rMy Rooney Quest Items#k#l\r\n";
        }

        cm.sendSimpleS("Welcome to Kaotic Drop Item DataBase\r\nSelect a Type of item you wish to find:\r\n" + text, 16);
    } else if (status == 1) {
        if (selection == 1 || selection == 2 || selection == 4) {
            items = cm.getDropItems(selection);
            var text = "";
            for (var i = 0; i < items.size(); i++) {
                var it = items.get(i);
                text += "#L" + it + "##i" + items.get(i) + "##l ";
            }
            cm.sendSimpleS("Welcome to Kaotic Drop Item DataBase\r\nSelect the item you wish to know whhere its found:\r\n" + text, 16);
        }
        if (selection == 10) {
            var itemid = cm.getPlayer().getVar("wanted_item");
            if (itemid > 0) {
                var mobs = cm.whoDrop(itemid);
                cm.sendOkS("#i" + itemid + "# #b#z" + itemid + "##k (#r" + itemid + "#k)\r\n            Location: #r" + cm.getPlayer().getTownFromItem(itemid) + "#k:\r\n" + mobs, 16);
            } else {
                cm.sendOkS("I have no Wanted Item.", 16);
            }

        }
        if (selection == 11) {

            var item1 = cm.getQuestItem(1);
            var item2 = cm.getQuestItem(2);
            var item3 = cm.getQuestItem(3);
            var item4 = cm.getQuestItem(4);
            var item5 = cm.getQuestItem(5);

            if (item1 > 0) {
                var text = "";
                text += "#L" + item1 + "# #i" + item1 + "# #b" + cm.getItemName(item1) + "#k#l\r\n";
                text += "#L" + item2 + "# #i" + item2 + "# #b" + cm.getItemName(item2) + "#k#l\r\n";
                text += "#L" + item3 + "# #i" + item3 + "# #b" + cm.getItemName(item3) + "#k#l\r\n";
                text += "#L" + item4 + "# #i" + item4 + "# #b" + cm.getItemName(item4) + "#k#l\r\n";
                text += "#L" + item5 + "# #i" + item5 + "# #b" + cm.getItemName(item5) + "#k#l\r\n";
                cm.sendSimpleS("Welcome to Kaotic Drop Item DataBase\r\nSelect the item you wish to know whhere its found:\r\n" + text, 16);
            } else {
                cm.sendOkS("I have no Rooney Quest.", 16);
            }
        }

    } else if (status == 2) {
        var itemid = selection;
        var mobs = cm.whoDrop(itemid);
        cm.sendOkS("#i" + itemid + "# #b#z" + itemid + "##k (#r" + itemid + "#k):\r\n" + mobs, 16);
    }
}