/* 
 NPC Name: 		Shanks
 Map(s): 		Maple Road : Southperry (60000)
 Description: 		Brings you to Victoria Island
 */
var status = -1;
var option = 0;

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
        var text = "";
        if (cm.haveItem(4430001, 1)) {
            text += "#L4430001# #i4430001# #b" + cm.getItemName(4430001) + "#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4430001)) + "#k)#l\r\n";
        }
        if (cm.haveItem(4430002, 1)) {
            text += "#L4430002# #i4430002# #b" + cm.getItemName(4430002) + "#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4430002)) + "#k)#l\r\n";
        }
        if (cm.haveItem(4430003, 1)) {
            text += "#L4430003# #i4430003# #b" + cm.getItemName(4430003) + "#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4430003)) + "#k)#l\r\n";
        }
        if (cm.haveItem(4430004, 1)) {
            text += "#L4430004# #i4430004# #b" + cm.getItemName(4430004) + "#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4430004)) + "#k)#l\r\n";
        }
        if (cm.haveItem(4430005, 1)) {
            text += "#L4430005# #i4430005# #b" + cm.getItemName(4430005) + "#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4430005)) + "#k)#l\r\n";
        }
        if (cm.haveItem(4430006, 1)) {
            text += "#L4430006# #i4430006# #b" + cm.getItemName(4430006) + "#k (#b" + cm.convertNumber(cm.getPlayer().countAllItem(4430006)) + "#k)#l\r\n";
        }
        cm.sendSimpleS("Which bait would you like to apply to your fishing line\r\n" + text, 16);
    } else if (status == 1) {
        cm.getPlayer().setVar("BAIT", selection);
        var bait = cm.getPlayer().getVar("BAIT");
        cm.sendOkS("#i" + bait + "# #b" + cm.getItemName(bait) + "#k now applied.", 16);

    }
}