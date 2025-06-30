/*
 Credits go to Travis of DeanMS ( xKillsAlotx on RaGEZONE)
 Item Exchanger for scrolls
 
 Modified by SharpAceX (Alan) for MapleSolaxia
 */

importPackage(Packages.tools);

var status = 0;
var mapid = 90000;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.getPlayer().getLevel() >= 200) {
                cm.sendSimple("Which floor would you like to go to?\r\n#k#L1# Floor 5 - Level 50+#l\r\n\#L2# Floor 10 - Level 100+#l\r\n\#L3# Floor 15 - Level 150+#l\r\n\#L4# Floor 20 - Level 200+#l");
            } else if (cm.getPlayer().getLevel() >= 150) {
                cm.sendSimple("Which floor would you like to go to?\r\n#k#L1# Floor 5 - Level 50+#l\r\n\#L2# Floor 10 - Level 100+#l\r\n\#L3# Floor 15 - Level 150+#l");
            } else if (cm.getPlayer().getLevel() >= 100) {
                cm.sendSimple("Which floor would you like to go to?\r\n#k#L1# Floor 5 - Level 50+#l\r\n\#L2# Floor 10 - Level 100+#l");
            } else if (cm.getPlayer().getLevel() >= 50) {
                cm.sendSimple("Which floor would you like to go to?\r\n#k#L1# Floor 5 - Level 50+#l");
            } else {
                cm.sendOk("You must be level 50 or greater to use this feature.");
                cm.dispose();
            }
        } else if (status == 1) {
            if (selection == 1) {
                cm.warp(90004, 0);
            } else if (selection == 2) {
                cm.warp(90009, 0);
            } else if (selection == 3) {
                cm.warp(90014, 0);
            } else if (selection == 4) {
                cm.warp(90019, 0);
            }
            cm.dispose();
        }
    }
}