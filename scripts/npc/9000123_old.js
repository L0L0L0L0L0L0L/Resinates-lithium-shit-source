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
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var item = 4310502;
var reward = 1672005;
var tier = 0;
var cost = 0;
var items = new Array(4001895, 4310211, 4000313, 4310500);
var amount = new Array(5000, 2500, 250, 25);

function start() {
    var selStr = "";
    selStr += "#L1##i" + reward + "# " + cm.getItemName(1142586) + " #bCraft Tier: 1?#k #l\r\n";
    selStr += "#L5##i" + reward + "# " + cm.getItemName(1142586) + " #bCraft Tier: 5?#k #l\r\n";
    selStr += "#L10##i" + reward + "# " + cm.getItemName(1142586) + " #bCraft Tier: 10?#k #l\r\n";
    //selStr += "#L25##i" + reward + "# " + cm.getItemName(1142586) + " #bCraft Tier: 25?#k #l\r\n";
    //selStr += "#L50##i" + reward + "# " + cm.getItemName(1142586) + " #bCraft Tier: 50?#k #l\r\n";
    cm.sendSimple(selStr);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status == 0) {

            return;
        }
        status--;
    }
    if (status == 1) {
        if (cm.canHold(reward)) {
            tier = selection;
            var text = "#i" + reward + "# " + cm.getItemName(1142586) + " Stats:\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b Tier: " + (tier) + "#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (5000 * tier) + ": Str-Dex-Int-Luk#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (500 * tier) + ": Atk-Matk#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (5000 * tier) + ": Def-Mdef#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (25 * tier) + "%: Ignore Defense#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (25 * tier) + "%: Critical Damage#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (25 * tier) + "%: All Stats#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b Hidden Slots: " + (5 * tier) + "#k\r\n";
            text += "#rWarning if you blow up this medal, it will not be refunded\r\nBE VERY VERY CAREFUL with it.#k\r\n";
            cm.sendNext("Would you like to purchase this medal#k?\r\n" + text);
        } else {
            cm.sendOk("Event requires 1 free slot in equips.");
        }
    }
    if (status == 2) {
        var selStr = "To Craft #bTier: "+tier+" #i" + reward + "# " + cm.getItemName(1142586) + "#k I will need following Materials:\r\n\ ";
        for (var i = 0; i < items.length; i++) {
            selStr += "   #i" + items[i] + "# " + cm.getItemName(items[i]) + " (x" + (amount[i] * tier) + ")\r\n\ ";
        }
        selStr += "\r\n\#rWarning if you blow up this medal, it will not be refunded\r\nBE VERY VERY CAREFUL with it.#k\r\n";
        cm.sendYesNo(selStr);
    }
    if (status == 3) {
        if (cm.haveItem(items[0], amount[0] * tier) && cm.haveItem(items[1], amount[1] * tier) && cm.haveItem(items[2], amount[2] * tier) && cm.haveItem(items[3], amount[3] * tier)) {
            cm.gainItem(items[0], -(amount[0] * tier));
            cm.gainItem(items[1], -(amount[1] * tier));
            cm.gainItem(items[2], -(amount[2] * tier));
            cm.gainItem(items[3], -(amount[3] * tier));
            cm.gainGodEquipTier(reward, tier, 0, 0, 0, 25, 25, 25);
            cm.sendOk("Thank you for purchasing #i" + reward + "#.");
        } else {
            cm.sendOk("You Do not have enough materials to craft #i" + reward + "# " + cm.getItemName(1142586));

        }

    }
}

