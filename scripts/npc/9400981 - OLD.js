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
var ticketId = 4310502;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var amount = 1000000;
var slotcount = 0;

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
            if (cm.haveItem(ticketId, 10)) {
                equiplist = cm.getPlayer().getEquipItems();
                if (!equiplist.isEmpty()) {
                    var selStr = "";
                    for (var i = 0; i < equiplist.size(); i++) {
                        var curEquip = equiplist.get(i);
                        if (curEquip != null && curEquip.getEnhance() >= 25) {
                            selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + "#l\r\n\ ";
                        } else {
                            continue;
                        }
                    }
                    cm.sendSimple("Which equip would you like to remove 25 Enchancement Stars from.\r\n\I only accept #i" + ticketId + "# (x10).\r\n\ " + selStr);
                } else {
                    cm.sendOk("You currently do not have any Equips with 25 stars or more.");
                                
                }
            } else {
                cm.sendOk("You currently do not have 10 Donation Points for star remover.");
                            
            }
		} else if (status == 1) {
			equip = equiplist.get(selection);
				cm.sendYesNo("Do you confirm that you want to spend 10 Donation Points removing 25 Stars from " + cm.getItemName(equip.getItemId()) + ".");
        } else if (status == 2) {
            if (cm.haveItem(ticketId)) {
                
                cm.gainItem(ticketId, -10);
                cm.setEnchance(equip, -25);
                cm.sendOk("25 Stars has been removed from " + cm.getItemName(equip.getItemId()) + ".");
                            
            } else {
                cm.sendOk("You currently do not have any Donation Points.");
                            
            }
        } else {
                        
        }
    }
}


