var status = 0;
var groupsize = 0;
var item = 4310500;
var itemName = "#i4310500#";
var ach = 408;
var cost = 0;
var multi = 1;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/BlueTeam# ";
var option = 0;
var maps;
var dmap = 0;
var m = 0;

function start() {
    var text = "";
    m = cm.getPlayer().getVarZero("MAPS");
    text += "Current Max Maps: #b" + m + "#k\r\n";
    text += "Current Maps Saved: #b" + cm.getPlayer().getMaps().size() + "#k\r\n";
    text += "#L1# Current Saved Maps#l\r\n";
    text += "#L2# #bSave current map#k#l\r\n";
    text += "#L3# #rRemove Map from list#k#l\r\n";
    text += "#L4# #rUpgrade Map Storage#k#l\r\n";
    text += "#L6# #rClear Map Storage#k#l\r\n";
    cm.sendSimpleS(text, 16);
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
        option = selection;
        if (option == 1) {
            maps = cm.getPlayer().getMaps();
            if (!maps.isEmpty()) {
                var selStr = "";
                var count = 0;
                for (var i = 0; i < maps.size(); i++) {
                    var curMap = maps.get(i);
                    count += 1;
                    selStr += "#L" + curMap + "#" + star + "#b" + cm.getPlayer().getMapName(curMap) + "#k#l\r\n";
                }
                if (count > 0) {
                    cm.sendSimpleS("Which map would you like to warp to?\r\n" + selStr, 16);
                } else {
                    cm.sendOkS("You currently do not have any Cash Equips to upgrade.", 16);
                }
            } else {
                cm.sendOkS("You currently do not have any Maps saved.", 16);
            }
        }
        if (option == 2) {
            var mapid = cm.getPlayer().getMapId();
            if (cm.getPlayer().canAddMap(mapid)) {
                cm.getPlayer().addMap(mapid);
                cm.getPlayer().saveMaps();
                cm.sendOkS("#b" + cm.getPlayer().getMapName(mapid) + "#k is now saved to your list.", 16);
            } else {
                cm.sendOkS("You list if full or you already have this map saved.", 16);
            }

        }
        if (option == 3) {
            maps = cm.getPlayer().getMaps();
            if (!maps.isEmpty()) {
                var selStr = "";
                var count = 0;
                for (var i = 0; i < maps.size(); i++) {
                    var curMap = maps.get(i);
                    count += 1;
                    selStr += "#L" + curMap + "#" + star + "#b" + cm.getPlayer().getMapName(curMap) + "#k#l\r\n";
                }
                if (count > 0) {
                    cm.sendSimpleS("Which map would you like to warp to?\r\n" + selStr, 16);
                } else {
                    cm.sendOkS("You currently do not have any Cash Equips to upgrade.", 16);
                }
            } else {
                cm.sendOkS("You currently do not have any Maps saved.", 16);
            }
        }
        if (option == 4) {
            cost = m * 10;
            cm.sendYesNoS("Do you want to upgrade your map storage to\r\n#b" + (m + 1) + "#k slots for price of #r" + cost + "#k " + itemName + "?", 16);
        }
        if (option == 5) {
            cm.sendOkS("test", 16);
            //cm.openNpc(9400845);
        }
        if (option == 6) {
            cm.getPlayer().clearMaps();
            cm.sendOkS("All you maps have been cleared.", 16);
        }

    }
    if (status == 2) {
        if (option == 1) {
            cm.warp(selection);
        }
        if (option == 3) {
            cm.getPlayer().removeMap(selection);
            cm.getPlayer().saveMaps();
            cm.sendOkS("#b" + cm.getPlayer().getMapName(selection) + "#k has been removed.", 16);
        }
        if (option == 4) {
            if (cm.haveItem(item, cost)) {
                cm.gainItem(item, -cost);
                cm.getPlayer().addVar("MAPS", 1);
                cm.sendOkS("You have successfully expanded your map storage", 16);
            } else {
                cm.sendOkS("You currently do not have enough " + itemName + ".", 16);
            }
        }
    }
}