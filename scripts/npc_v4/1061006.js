var status = -1;
var option = 0;

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else
        status--;

    if (status == 0) {
        var selStr = "Which Jump Quest would you like to take on?\r\n";
        selStr += "#L1#Easy#l\r\n";
        selStr += "#L2#Normal#l\r\n";
        selStr += "#L3#Hard#l\r\n";
        cm.sendSimple(selStr + "\r\n ");
    } else if (status == 1) {
        if (selection == 1) {
            cm.warp(910530000, 0);
        } else if (selection == 2) {
            cm.warp(910530100, 0);
        } else if (selection == 3) {
            cm.warp(910530200, 0);
        } else if (selection == 4) {
            cm.warp(101000100, 0);
        } else if (selection == 5) {
            cm.warp(105040310, 0);
        }
    }
}


