/* 
 NPC Name: 		Shanks
 Map(s): 		Maple Road : Southperry (60000)
 Description: 		Brings you to Victoria Island
 */
var status = -1;
var option = 0;
var item = 4037167;
var cost = 0;
var hasKey = false;
var amount = 0;

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
        if (cm.getPlayer().getReborns() >= 90) {
            var text = "Bring me the #i3991002# #i3991040# #i3991012# #i3991027# #i3991014# to Craft #i4000524#\r\n\r\n";
            text += "#i3991002# Slime Boss (Raid)\r\n";
            text += "#i3991040# Kalos Boss (Raid)\r\n";
            text += "#i3991012# Kaling Boss (Raid)\r\n";
            text += "#i3991027# Extreme Lotus Boss (Raid)\r\n";
            text += "#i3991014# Dragon Boss (Raid)\r\n";
            cm.sendGetText("How many Combo's do you want to exchange for?\r\n" + text);
        } else {
            cm.sendOk("Your journey is not far enough\r\nYou need to have mastered #r99#k Reborns!");
        }
    } else if (status == 1) {
        amount = cm.getNumber();
        cm.sendYesNo("Do you want to confirm that you wish to redeem " + amount + " #kCOMBO#b's");
    } else if (status == 2) {
        var key = true;
        if (!cm.haveItem(3991002, amount)) {
            key = false;
        }
        if (!cm.haveItem(3991040, amount)) {
            key = false;
        }
        if (!cm.haveItem(3991012, amount)) {
            key = false;
        }
        if (!cm.haveItem(3991027, amount)) {
            key = false;
        }
        if (!cm.haveItem(3991014, amount)) {
            key = false;
        }
        if (!cm.getPlayer().canHold(4000524, amount)) {
            key = false;
        }
        if (key) {
            cm.gainItem(3991002, -amount);
            cm.gainItem(3991040, -amount);
            cm.gainItem(3991012, -amount);
            cm.gainItem(3991027, -amount);
            cm.gainItem(3991014, -amount);
            cm.gainItem(4000524, amount);
            cm.sendOk("Take these #i4000524# to the #bSkylark Rita#k in town for you next mission!");
        } else {
            cm.sendOk("Your lacking your combo!");
        }
    }
}