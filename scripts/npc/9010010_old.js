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
var ticketId = 4310500;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var types = new Array("", "Str", "Dex", "Int", "Luk", "W-Def", "M-Def", "W-Atk", "M-Atk", "", "Overpower", "Total Damage", "Boss Damage", "IED", "Critical Damage", "All Stats");
var tier = new Array("Common", "Un-Common", "Rare", "Epic", "Legendary", "Supreme");
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var subscroll = 0;
var stat = 0;
var cost = 1;
var price = 1;
var amount = 0;
var power = 0;
var max = 0;

//2585007-shard
//2586004-gem
//2049307-ee
//2049176-PS
//2587001-GS


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
        level = cm.getPlayer().getTotalLevel();
        var text = "";
        text += "#L1# #i4310500##i2049176##i2340000# #bAdd Str#k (#rBased on Tier#k)#l\r\n";
        text += "#L2# #i4310500##i2049176##i2340000# #bAdd Dex#k (#rBased on Tier#k)#l\r\n";
        text += "#L3# #i4310500##i2049176##i2340000# #bAdd Int#k (#rBased on Tier#k)#l\r\n";
        text += "#L4# #i4310500##i2049176##i2340000# #bAdd Luk#k (#rBased on Tier#k)#l\r\n";

        text += "#L5# #i4310500##i2049307##i2340000# #bAdd WDef#k (#rBased on Tier#k)#l\r\n";
        text += "#L6# #i4310500##i2049307##i2340000# #bAdd MDef#k (#rBased on Tier#k)#l\r\n";

        text += "#L7# #i4310500##i2585007##i2587000# #bAdd W-Atk#k (#rBased on Tier#k)#l\r\n";
        text += "#L8# #i4310500##i2585007##i2587000# #bAdd M-Atk#k (#rBased on Tier#k)#l\r\n";

        text += "#L10# #i4310500##i2586004##i2587001# #rIncrease Overpower% based on tier#k#l\r\n";
        text += "#L11# #i4310500##i2586004##i2587001# #rIncrease Total Damage% based on tier#k#l\r\n";
        text += "#L12# #i4310500##i2586004##i2587001# #rIncrease Boss Damage% based on tier#k#l\r\n";
        text += "#L13# #i4310500##i2586004##i2587001# #rIncrease IED% based on tier#k#l\r\n";
        text += "#L14# #i4310500##i2586004##i2587001# #rIncrease Critial Damage% based on tier#k#l\r\n";
        text += "#L15# #i4310500##i2586004##i2587001# #rIncrease All Stats% based on tier#k#l\r\n";
        cm.sendSimple("Welcome to the Master NX Upgrading System. I can #rUpgrade#k any NX equip you have, but I charge a steep price with Meso Bags and\r\nWith the following Materials:\r\n" + text);
    } else if (status == 1) {
        stat = selection;
        if (selection == 1 || selection == 2 || selection == 3 || selection == 4) {
            scroll = 2049176;
            subscroll = 2340000;
            price = 2;
            power = 100;
            max = 9999999;
        }
        if (selection == 5 || selection == 6) {
            scroll = 2049307;
            subscroll = 2340000;
            price = 1;
            power = 250;
            max = 9999999;
        }
        if (selection == 7 || selection == 8) {
            scroll = 2585007;
            subscroll = 2587000;
            price = 5;
            power = 50;
            max = 9999999;
        }
        if (selection == 10 || selection == 11 || selection == 12 || selection == 13 || selection == 14 || selection == 15) {
            scroll = 2586004;
            subscroll = 2587001;
            price = 10;
            power = 1;
            max = 9999;
        }

        equiplist = cm.getPlayer().getEquipItems();
        if (!equiplist.isEmpty()) {
            var selStr = "";
            var count = 0;
            for (var i = 0; i < equiplist.size(); i++) {
                var curEquip = equiplist.get(i);
                var pot = cm.getPlayer().getBonusPot(curEquip, stat);
                if (curEquip != null && curEquip.isCash(curEquip.getItemId()) && curEquip.getUpgradeSlots() > 0) {
                    count += 1;
                    selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " - Slots: " + curEquip.getUpgradeSlots() + "#l\r\n\ ";
                }
            }
            if (count > 0) {
                cm.sendSimple("Which equip would you like to Upgrade?\r\n\ " + selStr);
            } else {
                cm.sendOk("You currently do not have any NX Equips to upgrade.");
            }
        } else {
            cm.sendOk("You currently do not have any NX Equips to upgrade.");
        }
    } else if (status == 2) {
        if (equip == null) {
            equip = equiplist.get(selection);
        }
        price *= equip.getPower();
        if (stat >= 10 && !cm.getPlayer().hasBonusPot(equip, stat)) {
            cm.sendOk("NX Equip currently does not have any #b" + types[stat] + "#k to upgrade.");
        } else {
            cm.sendGetTextS("How many upgrade do you wish to apply?\r\n#rMax upgrades available: " + equip.getUpgradeSlots() + "\r\n#rI require the following items#k:\r\n\ #i" + scroll + "# " + cm.getItemName(scroll) + " \r\n\ #i" + subscroll + "# " + cm.getItemName(subscroll) + "\r\n\ #i" + ticketId + "# " + cm.getItemName(ticketId) + " (#r" + price + "x#k) \r\n\r\n", 16);
        }
    } else if (status == 3) {
        amount = cm.getNumber();
        if (amount > 0 && amount <= equip.getUpgradeSlots()) {
            var selStr = "";
            selStr += "   #i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + " Tier: #b" + equip.getPower() + "#k\r\n";
            selStr += "   #i" + scroll + "# Cost Per Upgrade: #r" + (amount * cost) + "#k\r\n";
            selStr += "   #i" + subscroll + "# Cost Per Upgrade: #r" + (amount * cost) + "#k\r\n";
            selStr += "   #i4310500# Cost Per Upgrade: #r" + (amount * price) + "#k\r\n";
            cm.sendYesNo("Do you confirm this selection?\r\n#rAny stats above cap will not be refunded.#k\r\n#rBasic Stats Cap: 99,999,999.#k\r\n#rBonus Stats Cap: 9999%.#k\r\n" + selStr);
        } else {
            cm.sendOk("You currently do not have any brains.");
        }
    } else if (status == 4) {
        if (equip != null) {
            var finalcost = amount * cost;
            var finalprice = amount * price;
            if (cm.haveItem(scroll, finalcost) && cm.haveItem(subscroll, finalcost) && cm.haveItem(4310500, finalprice)) {
                cm.gainItem(scroll, -finalcost);
                cm.gainItem(subscroll, -finalcost);
                cm.gainItem(4310500, -finalprice);
                cm.getPlayer().gainEquipStat(equip, stat, amount);
                cm.sendOk("You have successfully upgraded your \r\n #i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + ".");
            } else {
                cm.sendOk("You currently do not have enough materials to upgrade.");
            }
        } else {
            cm.sendOk("You currently do not have an equip to upgrade.");
        }
    }
}


