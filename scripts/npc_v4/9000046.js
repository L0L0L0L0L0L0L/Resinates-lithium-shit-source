/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public              as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 .
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public              for more details.
 
 You should have received a copy of the GNU Affero General Public             
 along with this program.  If not, see <http://www.gnu.org/            s/>.
 */
var status = 0;
var ticketId = 5062002;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var cost = 1;
var slotcount = 0;
var cube;
var count = 0;
var min = 0;
var max = 0;
var chance = 0;
var safe = 0;
var pscroll = 0;

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
        var text = "";
        text += "#L1# #i2049300# #bStats: (5-10) #k- #rSucces: 100%#k#l\r\n";
        text += "#L2# #i2049301# #bStats: (10-15) #k- #rSucces: 75%#k#l\r\n";
        text += "#L3# #i2049302# #bStats: (15-25) #k- #rSucces: 50%#k#l\r\n";
        text += "#L4# #i2049303# #bStats: (25-50) #k- #rSucces: 25%#k#l\r\n";
        text += "#L5# #i2049304# #bStats: (50-100) #k- #rSucces: 10%#k#l\r\n";
        text += "#L6# #i2049305# #bStats: (100-250) #k- #rSucces: 5%#k#l\r\n\ ";
        text += "#L7# #i2049306# #bStats: (250-500) #k- #rSucces: 2%#k#l\r\n\ ";
        text += "#L8# #i2049307# #bStats: (500-2500) #k- #rSucces: 1%#k#l\r\n\ ";
        text += "#L9# #i2049308# #bStats: (2500-25000) #k- #rSucces: 1%#k#l\r\n\ ";
        cm.sendSimple("Welcome to the Master Enhance System. I can speed up the process of enhancing equips, but I charge a steep price. #rAndroids cannot be enchanced#k.\r\nMax amount of enhancements is 25.\r\n\#rFailures Do not blow up equips.#k\r\n\Here are my Prices Per Star -> Power:\r\n" + text);
    } else if (status == 1) {
        chance = 0;
        if (selection == 1) {
            cube = 2049300;
            min = 5;
            max = 10;
            chance = 100;
        } else if (selection == 2) {
            cube = 2049301;
            min = 10;
            max = 15;
            chance = 75;
        } else if (selection == 3) {
            cube = 2049302;
            min = 15;
            max = 25;
            chance = 50;
        } else if (selection == 4) {
            cube = 2049303;
            min = 25;
            max = 50;
            chance = 25;
        } else if (selection == 5) {
            cube = 2049304;
            min = 50;
            max = 100;
            chance = 10;
        } else if (selection == 6) {
            cube = 2049305;
            min = 100;
            max = 250;
            chance = 5;
        } else if (selection == 7) {
            cube = 2049306;
            min = 250;
            max = 500;
            chance = 2;
        } else if (selection == 8) {
            cube = 2049307;
            min = 500;
            max = 2500;
            chance = 1;
        } else if (selection == 9) {
            cube = 2049308;
            min = 2500;
            max = 25000;
            chance = 1;
        }
        if (cm.haveItem(cube, 1)) {
            equiplist = cm.getPlayer().getEquipItems();
            if (!equiplist.isEmpty()) {
                var selStr = "";
                for (var i = 0; i < equiplist.size(); i++) {
                    var curEquip = equiplist.get(i);
                    var limit = curEquip.getPower() > 75 ? 75 : curEquip.getPower();
                    if (limit < 10) {
                        limit = 10;
                    }
                    if (curEquip != null && curEquip.getEnhance() < limit) {
                        count += 1;
                        selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + "#l\r\n\ ";
                    }
                }
                if (count > 0) {
                    cm.sendSimple("Which equip would you like to Enhance?\r\n\I only accept #i" + cube + "#\r\n\ " + selStr);
                } else {
                    cm.sendOk("You currently do not have any Equips to enhance.");
                }
            } else {
                cm.sendOk("You currently do not have any Equips to enhance.");
            }
        } else {
            cm.sendOk("You currently do not have enough Scrolls. Requires " + cost + " #i" + cube + "#s");
        }
    } else if (status == 2) {
        if (equip == null) {
            equip = equiplist.get(selection);
        }
        cm.sendGetTextS("How many #i" + cube + "# " + cm.getItemName(cube) + " would like to apply?\r\nYou currently have #b" + cm.convertNumber(cm.getPlayer().countAllItem(cube)) + "#k Scrolls", 16);

    } else if (status == 3) {
        cost = cm.getNumber();
        if (cost > 0) {
            if (cm.haveItem(cube, cost)) {
                cm.sendYesNoS("ARe you sure you wish to apply " + cost + " #i" + cube + "# to \r\n#i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + "?", 16);
            } else {
                cm.sendOkS("You currently do not have enough #i" + cube + "#", 16);
            }
        } else {
            cm.sendOk("hey retard....");
        }
    } else if (status == 4) {
        if (cm.haveItem(cube, cost)) {
            if (equip != null) {
                var oldstr = equip.getTStr();
                var olddex = equip.getTDex();
                var oldint = equip.getTInt();
                var oldluk = equip.getTLuk();
                var oldatk = equip.getTAtk();
                var oldmatk = equip.getTMatk();
                var olddef = equip.getTDef();
                var oldmdef = equip.getTMdef();
                var value = cm.getPlayer().useEEScrolls(equip, chance, min, max, cost);
                var stats = "#b" + cm.getItemName(equip.getItemId()) + "#k\r\n";
                if (equip.getEnhance() > 0) {
                    stats += "Stars: #b" + equip.getEnhance() + "#k\r\n";
                }
                if (equip.getTStr() > 0) {
                    stats += "Str: #b" + cm.getFullUnitNumber(equip.getTStr()) + "  #g+" + (equip.getTStr() - oldstr) + "#k\r\n";
                }
                if (equip.getTDex() > 0) {
                    stats += "Dex: #b" + cm.getFullUnitNumber(equip.getTDex()) + "  #g+" + (equip.getTDex() - olddex) + "#k\r\n";
                }
                if (equip.getTInt() > 0) {
                    stats += "Int: #b" + cm.getFullUnitNumber(equip.getTInt()) + "  #g+" + (equip.getTInt() - oldint) + "#k\r\n";
                }
                if (equip.getTLuk() > 0) {
                    stats += "Luk: #b" + cm.getFullUnitNumber(equip.getTLuk()) + "  #g+" + (equip.getTLuk() - oldluk) + "#k\r\n";
                }
                if (equip.getTAtk() > 0) {
                    stats += "Weapon Attack: #b" + cm.getFullUnitNumber(equip.getTAtk()) + "  #g+" + (equip.getTAtk() - oldatk) + "#k\r\n";
                }
                if (equip.getTMatk() > 0) {
                    stats += "Magic Attack: #b" + cm.getFullUnitNumber(equip.getTMatk()) + "  #g+" + (equip.getTMatk() - oldmatk) + "#k\r\n";
                }
                if (equip.getTDef() > 0) {
                    stats += "Weapon Defense: #b" + cm.getFullUnitNumber(equip.getTDef()) + "  #g+" + (equip.getTDef() - olddef) + "#k\r\n";
                }
                if (equip.getTMdef() > 0) {
                    stats += "Magic Defense: #b" + cm.getFullUnitNumber(equip.getTMdef()) + "  #g+" + (equip.getTMdef() - oldmdef) + "#k\r\n";
                }
                cm.gainItem(cube, -value);
                cm.sendOk("#r#i" + cube + "# " + cm.getItemName(cube) + " (" + value + "x)#k was consumed.#k\r\n " + stats);
            } else {
                cm.sendOk("invalid equip error pls report..");
            }
        } else {
            cm.sendOk("You currently do not have enough Scrolls. Requires " + cost + " #i" + cube + "#s");

        }
    }
}


