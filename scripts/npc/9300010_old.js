var status = 0;
var option = 0;
var item = 0;
var amount = 0;
var star = 4310335;
var starAmount = 0;
var gas = 1;
var total = 0;
//4310502 - RP
//4310504 - DP
//4310505 - IP

function start() {
    cm.sendSimple("GMB shop soon...");

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
}