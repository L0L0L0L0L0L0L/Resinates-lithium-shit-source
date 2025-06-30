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
var leaf = 4000313;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var scroll = 0;
var scrollslot = 0;
var amount = 0;
var slotcount = 0;
var power = 0;
var equip;
var equips;
var equiplist;
var options = 0;
var itemcount = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
                    
    } else {
        if (mode == 0 && type > 0) {
                        
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            equiplist = cm.getPlayer().getLockedEquipItems();
            if (!equiplist.isEmpty()) {
                var selStr = "";
                for (var i = 0; i < equiplist.size(); i++) {
                    var curEquip = equiplist.get(i);
                    if (curEquip != null && cm.isEquipLock(curEquip)) {
                        selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " #l\r\n\ ";
                    } else {
                        break;
                    }
                }
                cm.sendSimple("Which equip would you like to unlock?\r\n\ " + selStr);
            } else {
                cm.sendOk("You currently do not have any equips to unlock.");
                            
            }
        } else if (status == 1) {
            equip = equiplist.get(selection);
            if (equip != null) {
                cm.sendYesNo("Would you like to remove the lock on this Equip: " + cm.getItemName(equip.getItemId()) + "?");
            } else {
                cm.sendOk("You currently do not have any equips to unlock.");
                            
            }
        } else if (status == 2) {
            if (equip != null) {
                cm.unlockEquip(equip);
                cm.gainItem(5060001, -1);
                cm.sendOk("You have successfully unlocked " + cm.getItemName(equip.getItemId()) + ".");
                            
            } else {
                cm.sendOk("You currently do not have any equips to unlock.");
                            
            }
        } else {
            cm.sendOk("You currently do not have any equips to view.");
                        
        }
    }
}

