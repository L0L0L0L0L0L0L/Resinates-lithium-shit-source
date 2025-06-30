var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var list;

var gstar = 4310504;
var gstarCost = 0;
var etc = 4420009;
var etcCost = 0;
var key = 4420010;
var keyCost = 0;
var amount = 0;
var ticket = 0;
var tier = new Array("Common", "Un-Common", "Rare", "Epic", "Legendary", "Supreme");
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var star2 = " #fEffect/Misc.img/orbs/0# ";
var ticketid = 0;

var dskinId = 0;
var option = 0;
var cost = 0;


function start() {
    var selStr = "Psst I can help you max out your #bDamage Skins#k with that smexy #rCrypto Points#k\r\n\r\n";
    selStr += ("   #L0##bMax My Damage Skin#k#l\r\n");
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
            list = cm.getPlayer().getDamageSkins();

            if (!list.isEmpty()) {
                var count_0 = 0;
                var count_1 = 0;
                var count_2 = 0;
                var count_3 = 0;
                var count_4 = 0;
                var count_5 = 0;
                var count_6 = 0;

                for (var i = 0; i < list.size(); i++) {
                    var skinz = cm.getDamageSkin(list.get(i));
                    if (cm.getPlayer().getSkinLevel(skinz.getId()) < 999) {
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
                    }
                }
                var selStr = "Select a which Tier of Damage Skins you to apply:\r\n";
                if (count_0 > 0) {
                    selStr += ("   #L0##i" + gstar + "# #bCommon Damage Skins#k #bPrice: 1#k#l\r\n");
                }
                if (count_1 > 0) {
                    selStr += ("   #L1##i" + gstar + "# #bUn-Common Damage Skins#k #bPrice: 2#k#l\r\n");
                }
                if (count_2 > 0) {
                    selStr += ("   #L2##i" + gstar + "# #bRare Damage Skins#k #bPrice: 3#k#l\r\n");
                }
                if (count_3 > 0) {
                    selStr += ("   #L3##i" + gstar + "# #bEpic Damage Skins#k #bPrice: 4#k#l\r\n");
                }
                if (count_4 > 0) {
                    selStr += ("   #L4##i" + gstar + "# #bLegendary Damage Skins#k #bPrice: 5#k#l\r\n");
                }
                if (count_5 > 0) {
                    selStr += ("   #L5##i" + gstar + "# #bSupreme Damage Skins#k #bPrice: 10#k#l\r\n");
                }
                cm.sendSimpleS(selStr, 16);
            } else {
                cm.sendOkS("I currently do not own any skins.", 16);
            }
        }
    } else if (status == 2) {
        if (option == 0) {
            var selStr = "Select a Skin:\r\n";
            for (var i = 0; i < list.size(); i++) {
                var skinId = list.get(i);
                var skinz = cm.getDamageSkin(skinId);
                if (cm.getPlayer().getSkinLevel(skinz.getId()) < 999 && skinz.getTier() == selection) {
                    selStr += "#L" + skinId + "##fEffect/DamageSkin.img/" + skinId + "/NoCri0/7##l";
                }
            }
            cm.sendSimpleS(selStr, 16);
        }
    } else if (status == 3) {
        if (option == 0) {
            dskinId = selection;
            var skin = cm.getDamageSkin(dskinId);
            if (skin != null) {
                if (skin.getTier() == 0 || skin.getTier() == 1 || skin.getTier() == 2 || skin.getTier() == 3 || skin.getTier() == 4) {
                    gstarCost = 1 + skin.getTier();
                }
                if (skin.getTier() == 5) {
                    gstarCost = 10;
                }
                if (skin.getTier() == 6) {
                    gstarCost = 25;
                }
                var selStr = "Do you wish to max out this for price of #i" + gstar + "# (#b" + gstarCost + "#kx)?";
                cm.sendYesNoS(selStr, 16);
            } else {
                cm.sendOkS("Error with skin.", 16);
            }
        }
    } else if (status == 4) {
        if (option == 0) {
            if (cm.haveItem(gstar, gstarCost)) {
                cm.gainItem(gstar, -gstarCost);
                cm.getPlayer().maxSkin(dskinId);
                var skin = cm.getDamageSkin(dskinId);
                cm.sendOkS("Your Damage skin is now maxxed out.", 16);
            } else {
                cm.sendOkS("You do not have the required items to make this exchange.", 16);
            }
        }
    }
}