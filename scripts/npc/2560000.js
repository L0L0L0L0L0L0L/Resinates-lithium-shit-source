/* 
 NPC Name: 		Shanks
 Map(s): 		Maple Road : Southperry (60000)
 Description: 		Brings you to Victoria Island
 */
var status = -1;
var option = 0;
var item = 4037167;
var cost = 0;

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
        var mob = cm.getPlayer().getVarZero("abyss_mob");
        var kills = cm.getPlayer().getVarZero("abyss_mob_count");
        if (mob == 0) {
            mob = 9601961 + cm.random(0, 4);
            cm.getPlayer().setVar("abyss_mob", mob);
        }
        if (kills < 9999) {
            var text = "";
            if (mob == 9601961) {
                text = "#bAbyss Master Birk#k";
            }
            if (mob == 9601962) {
                text = "#bAbyss Dark Master Birk#k";
            }
            if (mob == 9601963) {
                text = "#bAbyss Master Harp#k";
            }
            if (mob == 9601964) {
                text = "#bAbyss Dark Master Harp#k";
            }
            if (mob == 9601965) {
                text = "#bAbyss Master Hoblin#k";
            }
            cm.sendOk("Go defeat 9999 " + text + "\r\nin order for me to craft a single #i4034152#.");
        } else {
            cm.getPlayer().setVar("abyss_mob", 0);
            cm.getPlayer().setVar("abyss_mob_count", 0);
            cm.gainItem(4034152, 1);
            cm.sendOk("You have killed enough Abyss's to allow me to craft a single #i4034152#.\r\nCome back to me if you wish to craft more.");
        }
        
    } else if (status == 1) {

    } else if (status == 2) {

    }
}