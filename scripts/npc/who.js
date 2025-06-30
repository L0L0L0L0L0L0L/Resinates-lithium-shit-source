var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var list;
var element = new Array("Non-Elemental", "Fire", "Water", "Wind", "Earth", "Shadow", "Light", "Support", "Supreme");
var weak = new Array(7, 2, 1, 4, 3, 6, 5, 0, 8);
var welement = new Array("Support", "Water", "Fire", "Earth", "Wind", "Light", "Shadow", "Non-Elemental", "Supreme");

var stat = new Array("Exp Rate", "Drop Rate", "Drop Rate", "All Stats", "Overpower", "Meso Rate", "Total Damage", "Boss Damage", "Ignore Defense");

var star = " #fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
var star2 = " #fEffect/Misc.img/orbs/0# ";


function start() {
    list = cm.getPlayers();
    if (!list.isEmpty()) {
        var count = 0;
        var selStr = "Cuurent players online: " + cm.getPlayers().size() + "\r\n\r\n";
        for (var i = 0; i < list.size(); i++) {
            var player = list.get(i);
            if (player != null) {
                //if (!player.isGM()) {
                var check = true;
                if (player.getMapId() == 910000000 || player.getMapId() == 2000 || player.getMapId() == 4000 || player.getMapId() == 910000001 || player.getMapId() == 910000023) {
                    check = false;
                }
                if (player.getEventInstance() != null) {
                    check = true;
                }
                if (check) {
                    selStr += star + " Acc: #r" + player.getClient().getAccountName() + "#k Player: #b" + player.getName() + "#k IP: #b" + player.getClient().getSessionIPAddress() + "#k\r\n";
                    selStr += "       Map: #r" + player.getMapId() + "#k - Name: #b" + player.getMap().getMapName() + "#k\r\n";
                    if (player.getEventInstance() != null) {
                        selStr += "       Instance: #r" + player.getEventInstance().getName() + "#k\r\n";
                    }
                    count++;
                }
            }
        }
        if (count > 0) {
            cm.sendOkS(selStr, 16);
        } else {
            cm.sendOkS("Cuurent players online: " + cm.getPlayers().size(), 16);
        }

    } else {
        cm.sendOkS("List is empty", 16);
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        var selStr = "Stats of the Damage Skin:\r\n";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCri0/0#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCri0/1#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCri0/2#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCri0/3#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCri0/4#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCri0/5#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCri0/6#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCri0/7#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCri0/8#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCri0/9#";
        //selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCustom/NoCri0/1#";
        //selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCustom/NoCri0/2#";
        //selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCustom/NoCri0/3#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCri1/effect#";
        cm.getPlayer().setDamageSkin(selection);
        cm.sendOkS(selStr, 16);
    } else if (status == 2) {

    }
}