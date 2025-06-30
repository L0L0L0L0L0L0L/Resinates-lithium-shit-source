var status = 0;
var level = 1000;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var key = 4001869;
var etc = 4001868;
var amount = 0;
function start() {
    cm.sendYesNo("Do you wish to leave this nightmare?");
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status == 0) {

            return;
        }
        status--;
    }
    if (status == 1) {
        if (cm.getPlayer().getEventInstance() != null) {
            cm.getPlayer().getEventInstance().exitPlayer(cm.getPlayer(), 4100);
        } else {
            cm.warp(4100);
        }
    }
}