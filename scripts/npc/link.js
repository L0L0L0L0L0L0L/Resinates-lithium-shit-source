
var status;
var sel = 0;
var summon = 4006001;
var amount = 0;
var item = 4310066;
var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        cm.sendOkS("Current Link Stats:\r\n" + cm.getPlayer().getLinkText(), 2);
    }
}













