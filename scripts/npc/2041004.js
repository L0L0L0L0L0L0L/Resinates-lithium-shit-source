/*
 Keroben - Leafre Cave of life - Entrance
 */

var morph;
var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        if (cm.getPlayer().getMeso() >= 1000000) {
            cm.sendNext("Oh, my Brother! Slip me of that sweet meso and I will send you off to farawy land.");
        } else {
            cm.sendOk("psshhh you poor peasent, leave me at once.");
        }
    } else if (status == 1) {
        cm.getPlayer().gainMeso(-1000000, true, true);
        cm.warp(300000100);
    }
}