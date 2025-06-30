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
var tier = new Array("Common", "Un-Common", "Rare", "Epic", "Legendary", "Supreme", "Ultimate", "Infinity");
var stat = new Array("Exp Rate", "Drop Rate", "Crit Damage", "All Stats", "Overpower", "Meso Rate", "Mob Damage", "Boss Damage", "Ignore Defense");

var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var star2 = " #fEffect/Misc.img/orbs/0# ";

var dskinId = 0;
var option = 0;


function start() {
    var selStr = "My Current Damage skin set is:\r\n\r\n";
    var skini = cm.getPlayer().getDamageSkin();
    var skind = cm.getDamageSkin(skini);
    if (skini < 9000) {
        selStr += star + "Skin ID: #b" + skini + "#k\r\n";
        selStr += star + "Tier: #b" + tier[skind.getTier()] + "#k\r\n";

        if (cm.getPlayer().getSkinLevel(skind.getId()) < cm.getPlayer().getMaxSkinLevel(skini)) {
            selStr += star + "Current Level: #b" + cm.getPlayer().getSkinLevel(skind.getId()) + "#k\r\n";
            selStr += star + "Current Exp: #b" + cm.getUnitNumber(cm.getPlayer().getSkinExp()) + " / " + cm.getUnitNumber(cm.getPlayer().getSkinNeededExp(cm.getPlayer().getSkinLevel(skind.getId()))) + " (" + cm.getPlayer().getSkinExpPercent() + "%)#k\r\n";
        } else {
            selStr += star + "Current Level: #b" + cm.getPlayer().getSkinLevel(skind.getId()) + "#k (#rMAX#k)\r\n";
        }

        if (skind.getAmount() > 0) {
            selStr += star + "Current Bonus Stats: #b+" + (skind.getAmount() * cm.getPlayer().getSkinLevel(skind.getId())) + "% " + stat[skind.getStat()] + "#k\r\n";
        }
        if (skind.getAmount2() > 0) {
            selStr += star + "Current Bonus Stats: #b+" + (skind.getAmount2() * cm.getPlayer().getSkinLevel(skind.getId())) + "% " + stat[skind.getStat2()] + "#k\r\n";
        }
        if (skind.getAmount3() > 0) {
            selStr += star + "Current Bonus Stats: #b+" + (skind.getAmount3() * cm.getPlayer().getSkinLevel(skind.getId())) + "% " + stat[skind.getStat3()] + "#k\r\n";
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
        selStr += ("\r\n");
    }
    if (skini >= 5000 && skini < 9000) {
        selStr += "#fEffect/DamageSkin.img/" + skini + "/NoCri1/icon#";
        selStr += "#fEffect/DamageSkin.img/" + skini + "/NoCustom/NoCri0/1#";
        selStr += "#fEffect/DamageSkin.img/" + skini + "/NoCustom/NoCri0/2#";
        selStr += "#fEffect/DamageSkin.img/" + skini + "/NoCustom/NoCri0/3#";
        selStr += "#fEffect/DamageSkin.img/" + skini + "/NoCri1/effect#";
    } else {
        selStr += "#fEffect/DamageSkin.img/" + skini + "/NoCri0/0#";
        selStr += "#fEffect/DamageSkin.img/" + skini + "/NoCri0/1#";
        selStr += "#fEffect/DamageSkin.img/" + skini + "/NoCri0/2#";
        selStr += "#fEffect/DamageSkin.img/" + skini + "/NoCri0/3#";
        selStr += "#fEffect/DamageSkin.img/" + skini + "/NoCri0/4#";
    }
    selStr += ("\r\n");
    selStr += ("   #L0##bChange Damage Skins#k#l\r\n");
    selStr += ("   #L2##bChange Damage Skins#k (Detailed)#l\r\n");
    selStr += ("   #L1##bReset to Default Damage Skin#k#l\r\n");
    selStr += ("   #L4##rCheck Skin-Link Stats#k#l\r\n ");
    cm.sendSimpleS(selStr, 16);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        if (selection == 0 || selection == 2) {
            option = 0;
            if (selection == 2) {

                option = 2;
            }
            list = cm.getPlayer().getDamageSkins();

            if (!list.isEmpty()) {
                var count_0 = 0;
                var count_1 = 0;
                var count_2 = 0;
                var count_3 = 0;
                var count_4 = 0;
                var count_5 = 0;
                var count_6 = 0;
                var count_7 = 0;

                for (var i = 0; i < list.size(); i++) {
                    var skinz = cm.getDamageSkin(list.get(i));
                    if (skinz != null) {
                        if (skinz.getTier() == 0) {
                            count_0++;
                        }
                        if (skinz.getTier() == 1) {
                            count_1++;
                        }
                        if (skinz.getTier() == 2) {
                            count_2++;
                        }
                        if (skinz.getTier() == 3) {
                            count_3++;
                        }
                        if (skinz.getTier() == 4) {
                            count_4++;
                        }
                        if (skinz.getTier() == 5) {
                            count_5++;
                        }
                        if (skinz.getTier() == 6) {
                            count_6++;
                        }
                        if (skinz.getTier() == 7) {
                            count_7++;
                        }
                    }
                }
                var selStr = "Select a which Tier of Damage Skins you to apply:\r\n";
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
                if (count_6 > 0) {
                    selStr += ("   #L6##bUltimate Damage Skins#k #l\r\n");
                }
                if (count_7 > 0) {
                    selStr += ("   #L7##bInfinity Damage Skins#k #l\r\n");
                }
                cm.sendSimpleS(selStr, 16);
            } else {
                cm.sendOkS("I currently do not own any skins.", 16);
            }
        } else if (selection == 4) {
            cm.getPlayer().getSkinStat(true);
            selStr = "Current Bonus Skin Link Stats:\r\n\r\n";

            selStr += star + "Bonus EXP: #b+" + cm.getPlayer().DSXP + "%#k\r\n";
            selStr += star + "Bonus DROP: #b+" + cm.getPlayer().DSDR + "%#k\r\n";
            selStr += star + "Bonus MESO: #b+" + cm.getPlayer().DSMR + "%#k\r\n";
            selStr += star + "Bonus ALL STATS: #b+" + cm.getPlayer().DSAS + "%#k\r\n";
            selStr += star + "Bonus MOB DAMAGE: #b+" + cm.getPlayer().DSTD + "%#k\r\n";
            selStr += star + "Bonus BOSS DAMAGE: #b+" + cm.getPlayer().DSBD + "%#k\r\n";
            selStr += star + "Bonus CRIT DAMAGE: #b+" + cm.getPlayer().DSCD + "%#k\r\n";
            selStr += star + "Bonus OVERPOWER: #b+" + cm.getPlayer().DSOP + "%#k\r\n";
            selStr += star + "Bonus IGNORE DEFENSE: #b+" + cm.getPlayer().DSIED + "%#k\r\n";



            cm.sendOkS(selStr, 16);
        } else {
            cm.getPlayer().setDefaultSkin();
            cm.sendOkS("Damage Skin has been set back to default.", 16);
        }
    } else if (status == 2) {
        var selStr = "Select a Skin:\r\n";
        for (var i = 0; i < list.size(); i++) {
            var skinId = list.get(i);
            var skinz = cm.getDamageSkin(skinId);
            if (skinz.getTier() == selection) {
                if (skinId >= 5000 && skinId < 9000) {
                    selStr += "#L" + skinId + "##fEffect/DamageSkin.img/" + skinId + "/NoCri1/icon##l ";
                } else {
                    selStr += "#L" + skinId + "##fEffect/DamageSkin.img/" + skinId + "/NoCri0/7##l ";
                }
                selStr += "ID: #b" + skinId + "#k - ";
                if (option == 2) {
                    var lvl = cm.getPlayer().getSkinLevel(skinz.getId());
                    var exp = cm.getPlayer().getSkinExp(skinz.getId());
                    if (lvl < cm.getPlayer().getMaxSkinLevel(skinId)) {
                        selStr += "Level: #b" + cm.getPlayer().getSkinLevel(skinz.getId()) + "#k\r\n\r\n";
                        selStr += "            " + star + "Current Exp: #b" + cm.getUnitNumber(exp) + " / " + cm.getUnitNumber(cm.getPlayer().getSkinNeededExp(skinz.getId(), lvl)) + " (" + cm.getPlayer().getSkinExpPercent(skinz.getId()) + "%)#k\r\n";
                    } else {
                        selStr += "Level: #b" + cm.getPlayer().getSkinLevel(skinz.getId()) + "#k (#rMax#k)\r\n\r\n";
                    }
                    if (skinz.getAmount() > 0) {
                        selStr += "            " + star + "Current Bonus Stats: #b+" + (skinz.getAmount() * cm.getPlayer().getSkinLevel(skinz.getId())) + "% " + stat[skinz.getStat()] + "#k\r\n";
                    }
                    if (skinz.getAmount2() > 0) {
                        selStr += "            " + star + "Current Bonus Stats: #b+" + (skinz.getAmount2() * cm.getPlayer().getSkinLevel(skinz.getId())) + "% " + stat[skinz.getStat2()] + "#k\r\n";
                    }
                    if (skinz.getAmount3() > 0) {
                        selStr += "            " + star + "Current Bonus Stats: #b+" + (skinz.getAmount3() * cm.getPlayer().getSkinLevel(skinz.getId())) + "% " + stat[skinz.getStat3()] + "#k\r\n";
                    }
                }
            }
        }
        cm.sendSimpleS(selStr, 16);
    } else if (status == 3) {
        dskinId = selection;
        var selStr = "Do you want to apply this damage skin?\r\n";
        var skin = cm.getDamageSkin(dskinId);
        selStr += star + "Skin ID: #b" + selection + "#k\r\n";
        selStr += star + "Tier: #b" + tier[skin.getTier()] + "#k\r\n";
        selStr += star + "Current Level: #b" + cm.getPlayer().getSkinLevel(skin.getId()) + "#k - Max Level: #b"+cm.getPlayer().getMaxSkinLevel(skin.getId())+"#k\r\n";
        if (skin.getAmount() > 0) {
            selStr += star + "Current Bonus Stats: #b+" + (skin.getAmount() * cm.getPlayer().getSkinLevel(skin.getId())) + "% " + stat[skin.getStat()] + "#k\r\n";
        }
        if (skin.getAmount2() > 0) {
            selStr += star + "Current Bonus Stats: #b+" + (skin.getAmount2() * cm.getPlayer().getSkinLevel(skin.getId())) + "% " + stat[skin.getStat2()] + "#k\r\n";
        }
        if (skin.getAmount3() > 0) {
            selStr += star + "Current Bonus Stats: #b+" + (skin.getAmount3() * cm.getPlayer().getSkinLevel(skin.getId())) + "% " + stat[skin.getStat3()] + "#k\r\n";
        }
        /*
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
         selStr += star + "Element: #fEffect/Misc.img/orbs/" + skin.getElement() + "# (" + element[skin.getElement()] + ") (+#b50% Damage#k)\r\n";
         selStr += star + "Resistance: #fEffect/Misc.img/orbs/" + skin.getElement() + "# (" + element[skin.getElement()] + ") (#r100% Damage#k)\r\n";
         }
         
         */
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
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCustom/NoCri0/1#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCustom/NoCri0/2#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCustom/NoCri0/3#";
        selStr += "#fEffect/DamageSkin.img/" + selection + "/NoCri1/effect#";
        cm.sendYesNoS(selStr, 16);
    } else if (status == 4) {
        cm.getPlayer().changeSkin(dskinId);
        cm.sendOkS("Damage successfully changed to #fEffect/DamageSkin.img/" + dskinId + "/NoCri0/7#.", 16);
    } else if (status == 5) {

    }
}