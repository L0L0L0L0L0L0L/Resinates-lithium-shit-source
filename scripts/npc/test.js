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

    var selStr = "Select a Tier of Damage Skins:\r\n";
    selStr += ("   #L0##bCommon Damage Skins#k #l\r\n");
    selStr += ("   #L1##bUn-Common Damage Skins#k #l\r\n");
    selStr += ("   #L2##bRare Damage Skins#k #l\r\n");
    selStr += ("   #L3##bEpic Damage Skins#k #l\r\n");
    selStr += ("   #L4##bLegendary Damage Skins#k #l\r\n");
    selStr += ("   #L5##bSupreme Damage Skins#k #l\r\n");
    cm.sendSimpleS(selStr, 16);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        list = cm.getDamageSkinsTier(selection);
        if (!list.isEmpty()) {
            var selStr = "Select a Skin:\r\n";
            for (var i = 0; i < list.size(); i++) {
                selStr += "#L" + i + "##fEffect/DamageSkin.img/" + list.get(i).getId() + "/NoCri0/7##l";
            }
            cm.sendSimpleS(selStr, 16);
        } else {
            cm.sendOkS("List is empty", 16);
        }
    } else if (status == 2) {
        var skin = list.get(selection);
        var selStr = "Stats of the Damage Skin:\r\n";
        selStr += star + "Skin ID: " + skin.getId() + "\r\n";
        selStr += star + "Max Level: #b" + skin.getMaxLevel() + "#k\r\n";

        selStr += star + "Bonus Stats Per Level: #b+" + skin.getAmount() + "% " + stat[skin.getStat()] + "#k\r\n";
        if (skin.getElement() < 8) {
            if (skin.getElement() == 7) {
                selStr += star + "Element: #fEffect/Misc.img/orbs/" + skin.getElement() + "# (" + element[skin.getElement()] + ") (75% Damage)\r\n";
                selStr += star + "Weakness: #fEffect/Misc.img/orbs/" + weak[skin.getElement()] + "# (" + welement[skin.getElement()] + ") (+#b150% Damage#k)\r\n";
            } else {
                selStr += star + "Element: #fEffect/Misc.img/orbs/" + skin.getElement() + "# (" + element[skin.getElement()] + ") (100% Damage)\r\n";
                selStr += star + "Weakness: #fEffect/Misc.img/orbs/" + weak[skin.getElement()] + "# (" + welement[skin.getElement()] + ") (+#b100% Damage#k)\r\n";
            }
            selStr += star + "Resistance: #fEffect/Misc.img/orbs/" + skin.getElement() + "# (" + element[skin.getElement()] + ") (-#r50% Damage#k)\r\n";
        } else {
            selStr += star + "Bonus Stats Per Level: #b+" + skin.getAmount2() + "% " + stat[skin.getStat2()] + "#k\r\n";
            selStr += star + "Bonus Stats Per Level: #b+" + skin.getAmount3() + "% " + stat[skin.getStat3()] + "#k\r\n";
            selStr += star + "Element: #fEffect/Misc.img/orbs/" + skin.getElement() + "# (" + element[skin.getElement()] + ") (+#b50% Damage#k)\r\n";
            selStr += star + "Resistance: #fEffect/Misc.img/orbs/" + skin.getElement() + "# (" + element[skin.getElement()] + ") (#r100% Damage#k)\r\n";
        }

        selStr += "\r\n";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCri0/0#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCri0/1#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCri0/2#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCri0/3#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCri0/4#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCri0/5#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCri0/6#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCri0/7#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCri0/8#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCri0/9#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCustom/NoCri0/1#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCustom/NoCri0/2#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCustom/NoCri0/3#";
        selStr += "#fEffect/DamageSkin.img/" + skin.getId() + "/NoCri1/effect#";
        cm.sendOkS(selStr, 16);

    }
}