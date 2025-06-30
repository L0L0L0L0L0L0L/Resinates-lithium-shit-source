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
var tier = new Array("Common", "Un-Common", "Rare", "Epic", "Legendary", "Supreme");
var stat = new Array("Exp Rate", "Drop Rate", "Drop Rate", "All Stats", "Overpower", "Meso Rate", "Total Damage", "Boss Damage", "Ignore Defense");

var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var star2 = " #fEffect/Misc.img/orbs/0# ";
var option = 0;
var gach = 4420002;

function start() {
    var selStr = "Welcome to the #i" + gach + "# (#bCommon#k) Damage Skins Banner:\r\n";
    selStr += ("   #L0##bWhats in this banner?#k#l\r\n");
    selStr += ("   #L1##bCash in Damage Tickets!#k#l\r\n");
    cm.sendSimpleS(selStr, 16);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        option = selection;
        if (option == 0) {
            list = cm.fetchDamageSkins(gach);
            var count_0 = 0;
            var count_1 = 0;
            var count_2 = 0;
            var count_3 = 0;
            var count_4 = 0;
            var count_5 = 0;

            for (var i = 0; i < list.size(); i++) {
                if (list.get(i).getTier() == 0) {
                    count_0++;
                }
                if (list.get(i).getTier() == 1) {
                    count_1++;
                }
                if (list.get(i).getTier() == 2) {
                    count_2++;
                }
                if (list.get(i).getTier() == 3) {
                    count_3++;
                }
                if (list.get(i).getTier() == 4) {
                    count_4++;
                }
                if (list.get(i).getTier() == 5) {
                    count_5++;
                }
            }
            var selStr = "Select a Tier from #bCommon#k Damage Skins Banner:\r\n";
            if (count_0 > 0) {
                selStr += ("   #L0##bCommon Damage Skins#k #l\r\n");
            }
            if (count_1 > 0) {
                selStr += ("   #L1##bUn-Common Damage Skins#k #l\r\n");
            }
            if (count_2 > 0) {
                selStr += ("   #L2##bRare Damage Skins#k #l\r\n");
            }
            if (count_3 > 0) {
                selStr += ("   #L3##bEpic Damage Skins#k #l\r\n");
            }
            if (count_4 > 0) {
                selStr += ("   #L4##bLegendary Damage Skins#k #l\r\n");
            }
            if (count_5 > 0) {
                selStr += ("   #L5##bSupreme Damage Skins#k #l\r\n");
            }
            cm.sendSimpleS(selStr, 16);
        }
        if (option == 1) {
            if (cm.haveItem(gach)) {
                cm.sendYesNoS("Do you want to cash in #i" + gach + "# for random Damage Skin?", 16);
            } else {
                cm.sendOkS("You do not have any #i" + gach + " to cash in.", 16);
            }
        }
    } else if (status == 2) {
        if (option == 0) {
            if (!list.isEmpty()) {
                var count = 0;
                var selStr = "Select a Skin from " + tier[selection] + " Tier:\r\n";
                for (var i = 0; i < list.size(); i++) {
                    if (list.get(i).getTier() == selection) {
                        var skinId = list.get(i).getId();
                        selStr += "#L" + skinId + "##fEffect/DamageSkin.img/" + skinId + "/NoCri0/7##l";
                        count++;
                    }
                }
                if (count > 0) {
                    cm.sendSimpleS(selStr, 16);
                } else {
                    cm.sendOkS("This banner does not have any " + tier[selection] + " Skins.", 16);
                }
            } else {
                cm.sendOkS("List is empty", 16);
            }
        }
        if (option == 1) {
            var sid = cm.fetchDamageSkin(gach);
            var skin = cm.getDamageSkin(sid);
            if (cm.haveItem(gach)) {
                cm.gainItem(gach, -1);
                if (cm.getPlayer().gainDamageSkin(sid)) {
                    var selStr = "#bYou have successfully pulled#k:\r\n";
                    //selStr += star + "Skin ID: " + skin.getId() + "\r\n";
                    selStr += star + "Tier: #b" + tier[skin.getTier()] + "#k\r\n";
                    selStr += star + "Current Level: #b" + cm.getPlayer().getSkinLevel(skin.getId()) + "#k - Max Level: #b" + skin.getMaxLevel() + "#k\r\n";

                    if (skin.getAmount() > 0) {
                        selStr += star + "Bonus Stats Per Level: #b+" + skin.getAmount() + "% " + stat[skin.getStat()] + "#k\r\n";
                    }
                    if (skin.getAmount2() > 0) {
                        selStr += star + "Bonus Stats Per Level: #b+" + skin.getAmount2() + "% " + stat[skin.getStat2()] + "#k\r\n";
                    }
                    if (skin.getAmount3() > 0) {
                        selStr += star + "Bonus Stats Per Level: #b+" + skin.getAmount3() + "% " + stat[skin.getStat3()] + "#k\r\n";
                    }
                    /*
                     if (skind.getElement() < 8) {
                     if (skind.getElement() == 7) {
                     selStr += star + "Element: #fEffect/Misc.img/orbs/" + skind.getElement() + "# (" + element[skind.getElement()] + ") (75% Damage)\r\n";
                     selStr += star + "Weakness: #fEffect/Misc.img/orbs/" + weak[skind.getElement()] + "# (" + welement[skind.getElement()] + ") (+#b150% Damage#k)\r\n";
                     } else {
                     selStr += star + "Element: #fEffect/Misc.img/orbs/" + skind.getElement() + "# (" + element[skind.getElement()] + ") (100% Damage)\r\n";
                     selStr += star + "Weakness: #fEffect/Misc.img/orbs/" + weak[skind.getElement()] + "# (" + welement[skind.getElement()] + ") (+#b100% Damage#k)\r\n";
                     }
                     selStr += star + "Resistance: #fEffect/Misc.img/orbs/" + skind.getElement() + "# (" + element[skind.getElement()] + ") (-#r50% Damage#k)\r\n";
                     } else {
                     selStr += star + "Element: #fEffect/Misc.img/orbs/" + skind.getElement() + "# (" + element[skind.getElement()] + ") (+#b50% Damage#k)\r\n";
                     selStr += star + "Resistance: #fEffect/Misc.img/orbs/" + skind.getElement() + "# (" + element[skind.getElement()] + ") (#r100% Damage#k)\r\n";
                     }
                     */
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
                    if (cm.haveItem(gach)) {
                        cm.sendYesNoS("#rWould you like to pull more skins?#k\r\n" + selStr, 16);
                    } else {
                        cm.sendOkS(selStr, 16);
                    }
                } else {
                    var selStr = "This damage skin has already been obtained, however you have gained +" + cm.getPlayer().getOrbs(skin.getId()) + " #i4420009# Destiny Orbs.\r\n";
                    if (cm.haveItem(gach)) {
                        cm.sendYesNoS("#rWould you like to pull more skins?#k\r\n" + selStr, 16);
                    } else {
                        cm.sendOkS(selStr, 16);
                    }
                }
            } else {
                cm.sendOkS("You do not have any #i" + gach + " to cash in.", 16);
            }
        }
    } else if (status == 3) {
        if (option == 0) {
            var skin = cm.getDamageSkin(selection);
            var selStr = "Stats of the Damage Skin:\r\n";
            //selStr += star + "Skin ID: " + skin.getId() + "\r\n";
            selStr += star + "#rDrop Chance#k: #b" + cm.fetchDamageSkinChance(gach, selection) + "%#k\r\n";
            selStr += star + "Tier: #b" + tier[skin.getTier()] + "#k\r\n";
            selStr += star + "Max Level: #b" + skin.getMaxLevel() + "#k\r\n";

            if (skin.getAmount() > 0) {
                selStr += star + "Bonus Stats Per Level: #b+" + skin.getAmount() + "% " + stat[skin.getStat()] + "#k\r\n";
            }
            if (skin.getAmount2() > 0) {
                selStr += star + "Bonus Stats Per Level: #b+" + skin.getAmount2() + "% " + stat[skin.getStat2()] + "#k\r\n";
            }
            if (skin.getAmount3() > 0) {
                selStr += star + "Bonus Stats Per Level: #b+" + skin.getAmount3() + "% " + stat[skin.getStat3()] + "#k\r\n";
            }
            /*
             if (skind.getElement() < 8) {
             if (skind.getElement() == 7) {
             selStr += star + "Element: #fEffect/Misc.img/orbs/" + skind.getElement() + "# (" + element[skind.getElement()] + ") (75% Damage)\r\n";
             selStr += star + "Weakness: #fEffect/Misc.img/orbs/" + weak[skind.getElement()] + "# (" + welement[skind.getElement()] + ") (+#b150% Damage#k)\r\n";
             } else {
             selStr += star + "Element: #fEffect/Misc.img/orbs/" + skind.getElement() + "# (" + element[skind.getElement()] + ") (100% Damage)\r\n";
             selStr += star + "Weakness: #fEffect/Misc.img/orbs/" + weak[skind.getElement()] + "# (" + welement[skind.getElement()] + ") (+#b100% Damage#k)\r\n";
             }
             selStr += star + "Resistance: #fEffect/Misc.img/orbs/" + skind.getElement() + "# (" + element[skind.getElement()] + ") (-#r50% Damage#k)\r\n";
             } else {
             selStr += star + "Element: #fEffect/Misc.img/orbs/" + skind.getElement() + "# (" + element[skind.getElement()] + ") (+#b50% Damage#k)\r\n";
             selStr += star + "Resistance: #fEffect/Misc.img/orbs/" + skind.getElement() + "# (" + element[skind.getElement()] + ") (#r100% Damage#k)\r\n";
             }
             */

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
        if (option == 1) {
            status = 3;
            action(0, 0, 0);
        }
    } else if (status == 4) {

    } else if (status == 5) {

    }
}