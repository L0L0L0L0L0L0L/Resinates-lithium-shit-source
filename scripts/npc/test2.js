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

var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var star2 = " #fEffect/Misc.img/orbs/0# ";


function start() {
    list = cm.getDamageSkinsbyIds();
    if (!list.isEmpty()) {
        var selStr = "Select a Skin:\r\n";
        for (var i = 0; i < list.size(); i++) {
            selStr += "#L" + list.get(i) + "#(" + list.get(i) + ")#fEffect/DamageSkin.img/" + list.get(i) + "/NoCri0/7##l - ";
        }
        cm.sendSimpleS(selStr, 16);
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