/**
 Jake - Victoria Road : Subway Ticketing Booth (103000100)
 **/

var meso = new Array(500, 1200, 2000);
var item = new Array(4031036, 4031037, 4031038);
var selector;
var menu = "";
var mesos = 0;
var itm = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        cm.sendOk("Subway is under construction.");
    }
}