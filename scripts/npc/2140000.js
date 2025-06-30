/*
 Keroben - Leafre Cave of life - Entrance
 */

var morph;
var status = -1;
var rewards = new Array(4000446, 4000451, 4000456, 4000460, 4000461, 4000462);
var amount = new Array(99, 99, 99, 1, 1, 1);

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
            if (!cm.getPlayer().getAchievement(270)) {
                cm.sendOk("You must clear #rEasy Party Mode#k in #rDungeon Room#k");
                return;
            }
            var selStr = "";
            for (var i = 0; i < rewards.length; i++) {
                selStr += "#i" + rewards[i] + "# #b" + cm.getItemName(rewards[i]) + "#k (x" + amount[i] + ") (#b" + cm.convertNumber(cm.getPlayer().countAllItem(rewards[i])) + "#k)\r\n";
            }
            cm.sendYesNo("Would you like to craft #i4032002#? I require the following items: \r\n\r\n\ " + selStr);

        } else if (status == 1) {
            if (cm.canHold(4032002)) {
                if (cm.haveItem(4000446, 99) && cm.haveItem(4000451, 99) && cm.haveItem(4000456, 99) && cm.haveItem(4000460, 1) && cm.haveItem(4000461, 1) && cm.haveItem(4000462, 1)) {
                    cm.gainItem(4000446, -99);
                    cm.gainItem(4000451, -99);
                    cm.gainItem(4000456, -99);
                    cm.gainItem(4000460, -1);
                    cm.gainItem(4000461, -1);
                    cm.gainItem(4000462, -1);
                    cm.gainItem(4032002, 1);
                    cm.sendOk("This marble is used to access Pink Bean instances. Black Bean instance WILL consume this marble.");

                } else {
                    cm.sendOk("Sorry, you dont have enough materials.");
                }
            } else {
                cm.sendOk("Please make room in ETC Inventory.");

            }
        }
    }
}