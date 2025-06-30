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
var ticketId = 2049032;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var shard = 0;
var shardslot = 0;
var cost = 1;
var slotcount = 0;
var cube = 2049032;
var white = 2340000;
var count = 0;
var min = 0;
var max = 0;
var chance = 0;
var safe = 0;
var pshard = 0;
var tier = 0;
var success = 0;
var power = 0;
var tp = 0;

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
        equiplist = cm.getPlayer().getEquipCash();
        if (!equiplist.isEmpty()) {
            var selStr = "";
            for (var i = 0; i < equiplist.size(); i++) {
                var curEquip = equiplist.get(i);
                if (curEquip != null && curEquip.getUpgradeSlots() > 0) {
                    count += 1;
                    selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " (#rTier: " + curEquip.getPower() + "#k) (#bSlots: " + curEquip.getUpgradeSlots() + "#k) #l\r\n";
                }
            }
            if (count > 0) {
                cm.sendSimpleS("Which equip would you like to auto shard?\r\n#rStats are focused around Shard and Tier of equip#k\r\n" + selStr, 16);
            } else {
                cm.sendOkS("You currently do not have any Cash Equips to upgrade.", 16);
            }
        } else {
            cm.sendOkS("You currently do not have any Cash Equips to upgrade.", 16);
        }
    } else if (status == 1) {
        if (equip == null) {
            equip = equiplist.get(selection);
        }
        var text = "#bSelect a Shard to apply:#k\r\n";
        text += "#L2585005##i2585005# #b" + cm.getItemName(2585005) + "#k#l\r\n";
        text += "#L2585006##i2585006# #b" + cm.getItemName(2585006) + "#k#l\r\n";
        text += "#L2585007##i2585007# #b" + cm.getItemName(2585007) + "#k#l\r\n";
        text += "#L2585008##i2585008# #b" + cm.getItemName(2585008) + "#k#l\r\n";
        cm.sendSimple("Which Power Shard would you like to use?\r\n" + text);
    } else if (status == 2) {
        cube = selection;
        var base = 1;
        chance = 100;
        if (cube == 2585005) {
            base = 25;
            chance = 50;
        }
        if (cube == 2585006) {
            base = 50;
            chance = 25;
        }
        if (cube == 2585007) {
            base = 100;
            chance = 10;
        }
        if (cube == 2585008) {
            base = 250;
            chance = 5;
        }
        power = base;
        cm.sendGetTextS("How many #i" + cube + "# would you like me to apply to this Equip?\r\nYou currently have #b" + cm.convertNumber(cm.getPlayer().countAllItem(cube)) + "#k Shards\r\n#rAverage power from shards is#k #b" + power + "#k\r\n#rMax Scrolls that can be used is 999,999#k", 16);
    } else if (status == 3) {
        cost = cm.getNumber();
        if (cost > 0 && cost <= 999999) {
            if (cm.haveItem(cube, cost)) {
                cm.sendYesNoS("ARe you sure you wish to apply " + cost + " #i" + cube + "# to \r\n#i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + "?\r\n#rSuccess Rate: "+chance+"% Per Scroll#k", 16);
            } else {
                cm.sendOkS("You currently do not have enough #i" + cube + "#", 16);
            }
        } else {
            cm.sendOkS("You cannot use this many shards", 16);
        }
    } else if (status == 4) {
        if (cm.haveItem(cube, cost)) {
            if (equip != null) {
                var oldslots = equip.getUpgradeSlots();
                var oldstr = equip.getTStr();
                var olddex = equip.getTDex();
                var oldint = equip.getTInt();
                var oldluk = equip.getTLuk();
                var oldatk = equip.getTAtk();
                var oldmatk = equip.getTMatk();
                var olddef = equip.getTDef();
                var oldmdef = equip.getTMdef();
                var value = cm.getPlayer().useBulkShardScroll(equip, chance, power, cost, cube);
                var stats = "#b" + cm.getItemName(equip.getItemId()) + "#k\r\n";
                stats += "Slots Remainging: #b" + equip.getUpgradeSlots() + "#k (#r" + (equip.getUpgradeSlots() - oldslots) + " Consumed#k)\r\n";
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
            cm.sendOk("You currently do not have enough Shards. Requires " + cost + " #i" + cube + "#s");

        }
    } else if (status == 5) {
    }
}


