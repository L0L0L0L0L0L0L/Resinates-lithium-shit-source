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
var status = -1;
var leaf = 4310066;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var scroll = 0;
var scrollslot = 0;
var amount = 0;
var slotcount = 0;
var power = 0;
var cash = 0;
var equip;
var equips;
var equiplist;
var options = 0;
var itemcount = 0;
var choice = 0;
var stars = 0;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {// \r\n\#L8#Overflow Storage#
        cm.sendSimple("Which system would you like to access?\r\n\#L7#Inventory#l\r\n\#L10#Unleashed Shop#l");
    } else if (status == 1) {
        if (selection == 7) {
            choice = 0;
            var selStr = "";
            selStr += "\r\n\#L100##eRecycle Top 24 Equips?#n #l\r\n\ ";
            selStr += "\r\n\#L101##eRecycle Top 48 Equips?#n #l\r\n\ ";
            selStr += "\r\n\#L102##eRecycle Top 72 Equips?#n #l\r\n\r\n\ ";
            selStr += "\r\n\#L103##eRecycle All 96 Slots?#n #l\r\n\ ";
            cm.sendSimple("Which slots would you like to recycle? \r\n\Tier 15 and above equip will yield Fragments of Destiny.\r\n\ " + selStr);
        }
        if (selection == 10) {
            choice = 2;
            cm.openShop(10700);
        }
        if (selection == 20) {
            var amount = cm.getPlayer().countItem(leaf);
            if (cm.removeItem(leaf, amount)) {
                cm.getPlayer().gainGP(amount);
                cm.sendOk("Your guild has gained " + amount + " Guild Points.");
            } else {
                cm.sendOk("You do not have enough Unleashed Coins.");
            }
        }
    } else if (status == 2) {
        if (choice == 0) {
            slot = selection;
            if (slot == 100 || slot == 101 || slot == 102) {
                if (slot == 100) {
                    slotcount = 24;
                }
                if (slot == 101) {
                    slotcount = 48;
                }
                if (slot == 102) {
                    slotcount = 72;
                }
                cm.sendYesNo("Are you 100% sure you want to recycle top " + slotcount + " slots in your inventory? This action CANNOT BE UNDONE!!!");
            } else if (slot == 103) {
                slotcount = 96;
                cm.sendYesNo("Are you 100% sure you want to recycle ALL EQUIPS in your inventory? This action CANNOT BE UNDONE!!!");
            } else {
                if (cm.getEquipbySlot(slot) != null) {
                    power = (cm.getEquipbySlot(slot).getEnhance() * 5) + 1;
                    cm.sendYesNo("This equip is worth " + power + " Legendary Coins. Would you like to recycle this equip?");
                } else {
                    cm.sendOk("Come back another day.");

                }
            }

        }
    } else if (status == 3) {
        if (choice == 0) {
            var FoD = 0;
            for (var i = 1; i <= slotcount; i++) {
                if (cm.getEquipbySlot(i) != null && !cm.isEquipLock(cm.getEquipbySlot(i))) {
                    var power = cm.getEquipbySlot(i).getPower();
                    if (cm.isCash(cm.getEquipbySlot(i).getItemId())) {
                        cash += 1;
                    }
                    if (power >= 10) {
                        FoD += (power * 5);
                    }
                    stars += (cm.getEquipbySlot(i).getEnhance() * 5) + 1;
                    cm.removeEquipFromItemSlot(i);
                    itemcount += 1;
                }
            }

            if (itemcount > 0) {
                var selStr = "";
                if (stars > 0) {
                    cm.getPlayer().gainItem(leaf, stars);
                    selStr += "#i" + leaf + "# " + cm.getItemName(leaf) + " x" + stars + "\r\n\ ";
                }
                if (cash > 0) {
                    cm.getPlayer().gainItem(4310501, cash);
                    selStr += "#i4310501# " + cm.getItemName(4310501) + " x" + cash + "\r\n\ ";
                }
                if (FoD > 0) {
                    cm.getPlayer().gainItem(4001895, FoD);
                    selStr += "#i4001895# " + cm.getItemName(4001895) + " x" + FoD + "\r\n\ ";
                }
                cm.sendOk("Thank You. Your equips are recycled.\r\n\You have Gained the following items:\r\n\r\n" + selStr);
            } else {
                cm.sendOk("Nothing to recycle");
            }
        }
    }
}

