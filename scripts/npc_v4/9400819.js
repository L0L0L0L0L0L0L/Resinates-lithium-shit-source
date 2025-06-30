var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var list;

var gstar = 4420008;
var gstarCost = 0;
var etc = 4420009;
var etcCost = 0;
var key = 4420010;
var keyCost = 0;
var amount = 0;
var ticket = 0;
var tier = new Array("Common", "Un-Common", "Rare", "Epic", "Legendary", "Supreme", "Ultimate");
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var star2 = " #fEffect/Misc.img/orbs/0# ";
var ticketid = 0;

var dskinId = 0;
var option = 0;
var cost = 0;


function start() {
    var selStr = "My Current Damage skin set is:\r\n\r\n";
    selStr += ("   #L4##bMax Damage Skin#k#l\r\n");
    selStr += ("   #L0##bUpgrade Damage Skin#k#l\r\n");
    selStr += ("   #L1##bCash in Destiny Orbs#k#l\r\n");
    selStr += ("   #L2##bExchange Tickets for Destiny Orbs#k#l\r\n ");
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
        if (option == 0 || option == 4) {
            list = cm.getPlayer().getDamageSkins();

            if (!list.isEmpty()) {
                var count_0 = 0;
                var count_1 = 0;
                var count_2 = 0;
                var count_3 = 0;
                var count_4 = 0;
                var count_5 = 0;

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
                cm.sendSimpleS(selStr, 16);
            } else {
                cm.sendOkS("I currently do not own any skins.", 16);
            }
        }
        if (option == 1) {
            var selStr = "Which Damage Skin Ticket would you like to buy?\r\n\r\n";
            selStr += ("   #L11##b#i4420002# - (Price: #i" + etc + "# (x5))#k#l\r\n");
            selStr += ("   #L12##b#i4420003# - (Price: #i" + etc + "# (x10))#k#l\r\n");
            selStr += ("   #L13##b#i4420004# - (Price: #i" + etc + "# (x100))#k#l\r\n");
            selStr += ("   #L14##b#i4420005# - (Price: #i" + etc + "# (x1000))#k#l\r\n");
            selStr += ("   #L15##b#i4420006# - (Price: #i" + etc + "# (x25000))#k#l\r\n");
            cm.sendSimpleS(selStr, 16);
        }
        if (option == 2) {
            var selStr = "Which skin ticket do you wish to recycle:\r\n\r\n";
            selStr += ("   #L10##b#i4420001# - (Orbs: #i" + etc + "# (x1))#k#l\r\n");
            selStr += ("   #L16##b#i4420020# - (Orbs: #i" + etc + "# (x2))#k#l\r\n");
            selStr += ("   #L11##b#i4420002# - (Orbs: #i" + etc + "# (x3))#k#l\r\n");
            selStr += ("   #L12##b#i4420003# - (Orbs: #i" + etc + "# (x5))#k#l\r\n");
            selStr += ("   #L13##b#i4420004# - (Orbs: #i" + etc + "# (x10))#k#l\r\n");
            selStr += ("   #L14##b#i4420005# - (Orbs: #i" + etc + "# (x100))#k#l\r\n");
            selStr += ("   #L15##b#i4420006# - (Orbs: #i" + etc + "# (x1000))#k#l\r\n");
            cm.sendSimpleS(selStr, 16);
        }
    } else if (status == 2) {
        if (option == 0 || option == 4) {
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
        if (option == 1) {
            if (selection == 11) {
                ticket = 4420002;
                etcCost = 5;
                keyCost = 0;
            }
            if (selection == 12) {
                ticket = 4420003;
                etcCost = 10;
                keyCost = 0;
            }
            if (selection == 13) {
                ticket = 4420004;
                etcCost = 100;
                keyCost = 0;
            }
            if (selection == 14) {
                ticket = 4420005;
                etcCost = 1000;
                keyCost = 0;
            }
            if (selection == 15) {
                ticket = 4420006;
                etcCost = 25000;
                keyCost = 0;
            }
            if (selection == 16) {
                ticket = 4420007;
                etcCost = 250000;
                keyCost = 1;
            }
            if (keyCost > 0) {
                cm.sendGetTextS("How many times do you want me apply this sale?\r\n\r\nEach #i" + ticket + "# costs (#i" + etc + "# #b" + etcCost + "#kx + #i" + key + "# #b" + keyCost + "#kx)\r\n\r\n", 16);
            } else {
                cm.sendGetTextS("How many times do you want me apply this sale?\r\n\r\nEach #i" + ticket + "# costs (#i" + etc + "# #b" + etcCost + "#kx)\r\n\r\n", 16);
            }
        }
        if (option == 2) {
            if (selection == 10) {
                ticket = 4420001;
                etcCost = 1;
            }
            if (selection == 11) {
                ticket = 4420002;
                etcCost = 3;
            }
            if (selection == 12) {
                ticket = 4420003;
                etcCost = 5;
            }
            if (selection == 13) {
                ticket = 4420004;
                etcCost = 10;
            }
            if (selection == 14) {
                ticket = 4420005;
                etcCost = 100;
            }
            if (selection == 15) {
                ticket = 4420006;
                etcCost = 1000;
            }
            if (selection == 16) {
                ticket = 4420020;
                etcCost = 2;
            }
            cm.sendGetTextS("How many #i" + ticket + "# do you want to recyle?\r\nEach Ticket is worth #b" + etcCost + "#k Orbs\r\n\r\n", 16);

        }
    } else if (status == 3) {
        if (option == 0 || option == 4) {
            dskinId = selection;
            var skin = cm.getDamageSkin(dskinId);
            if (skin != null) {
                if (skin.getTier() == 5) {
                    gstarCost = 10;
                } else {
                    gstarCost = (skin.getTier() + 1);
                }
                if (option == 4) {
                    cm.sendYesNoS("Are you sure want to max out thie damage skin?", 16);
                } else {
                    var selStr = "How many levels do you want to upgrade your Damage Skin?\r\n#b" + tier[skin.getTier()] + "#k Price (#i" + gstar + "# #b" + gstarCost + "#kx) Per level for #fEffect/DamageSkin.img/" + dskinId + "/NoCri0/7#.\r\n#rCurrent Damage Skin Level: " + cm.getPlayer().getSkinLevel(skin.getId()) + "#k. #bMax Level: 999#k.";
                    cm.sendGetTextS(selStr, 16);
                }

            } else {
                cm.sendOkS("Error with skin.", 16);
            }
        }
        if (option == 1) {
            amount = cm.getNumber();
            if (amount > 0 && amount <= 10000) {
                if (keyCost > 0) {
                    cm.sendYesNoS("Are you sure you want to exchange\r\n\r\n#i" + etc + "# (#b" + (etcCost * amount) + "x#k) + #i" + key + "# (#b" + (keyCost * amount) + "x#k)  =  #i" + ticket + "# (#b" + amount + "x#k)?", 16);
                } else {
                    cm.sendYesNoS("Are you sure you want to exchange\r\n\r\n#i" + etc + "# (#b" + (etcCost * amount) + "x#k)  =   #i" + ticket + "# (#b" + amount + "x#k)?", 16);
                }
            } else {
                cm.sendOkS("enter a number greater than 0 and less than 1000.", 16);
            }
        }
        if (option == 2) {
            amount = cm.getNumber();
            if (amount > 0 && amount <= 10000) {
                cm.sendYesNoS("Are you sure you want to exchange\r\n\r\n#i" + ticket + "# (#b" + amount + "x#k) for #i" + etc + "# (#b" + (amount * etcCost) + "x#k)?", 16);
            } else {
                cm.sendOkS("enter a number greater than 0 and less than 1000.", 16);
            }

        }
    } else if (status == 4) {
        if (option == 0 || option == 4) {
            var skin = cm.getDamageSkin(dskinId);
            if (option == 0) {
                amount = cm.getNumber();
            } else {
                amount = (999 - cm.getPlayer().getSkinLevel(skin.getId()));
            }
            if (amount > 0 && amount <= 999) {
                cost = gstarCost * amount;
                if (cost > 0 && cm.haveItem(gstar, cost)) {
                    var total = cm.getPlayer().getSkinLevel(skin.getId()) + amount;
                    if (total <= 999) {
                        cm.sendYesNoS("Are you sure you want to upgrade the Damage Skin\r\nFrom #bLevel " + cm.getPlayer().getSkinLevel(skin.getId()) + "#k to #rLevel " + total + "#k?\r\n\r\nPrice will cost #i" + gstar + "# (#b" + (cost) + "x#k)", 16);
                    } else {
                        cm.sendOkS("Amount you put in exceeds max level of the Damage Skin. Max Level is " + skin.getMaxLevel(), 16);
                    }
                } else {
                    cm.sendOkS("You do not have the required items to make this exchange.", 16);
                }
            } else {
                cm.sendOkS("You cannot exceed the max level range.", 16);
            }
        }
        if (option == 1) {
            if (keyCost > 0) {
                if (cm.haveItem(etc, etcCost * amount) && cm.haveItem(key, keyCost * amount)) {
                    cm.gainItem(etc, -(etcCost * amount));
                    cm.gainItem(key, -(keyCost * amount));
                    cm.gainItem(ticket, amount);
                    cm.sendOkS("You have successfully exchanged\r\n#i" + etc + "# (#b" + (etcCost * amount) + "x#k)  for   #i" + ticket + "# (#b" + amount + "x#k)", 16);
                } else {
                    cm.sendOkS("You do not have the required items to make this exchange.", 16);
                }
            } else {
                if (cm.haveItem(etc, etcCost * amount)) {
                    cm.gainItem(etc, -(etcCost * amount));
                    cm.gainItem(ticket, amount);
                    cm.sendOkS("You have successfully exchanged\r\n#i" + etc + "# (#b" + (etcCost * amount) + "x#k)  for   #i" + ticket + "# (#b" + amount + "x#k)", 16);
                } else {
                    cm.sendOkS("You do not have the required items to make this exchange.", 16);
                }
            }
        }
        if (option == 2) {
            if (cm.haveItem(ticket, amount)) {
                cm.gainItem(ticket, -amount);
                cm.gainItem(etc, amount * etcCost);
                cm.sendOkS("You have successfully exchanged your tickets for #b" + amount * etcCost + "#k Orbs.", 16);
            } else {
                cm.sendOkS("You do not have the required items to make this exchange.", 16);
            }
        }
    } else if (status == 5) {
        if (option == 0 || option == 4) {
            var skin = cm.getDamageSkin(dskinId);
            if (cm.haveItem(gstar, cost)) {
                var total = cm.getPlayer().getSkinLevel(skin.getId()) + amount;
                if (total <= 999) {
                    cm.gainItem(gstar, -(cost));
                    cm.getPlayer().changeSkinLevel(dskinId, total);
                    cm.getPlayer().getSkinStat(true);
                    cm.sendOkS("You have successfully upgraded your damage to level " + cm.getPlayer().getSkinLevel(skin.getId()) + ".", 16);
                } else {
                    cm.sendOkS("Amount you put in exceeds max level of the Damage Skin. Max Level is " + skin.getMaxLevel(), 16);
                }
            } else {
                cm.sendOkS("You do not have the required items to make this exchange.", 16);
            }
        }
    }
}