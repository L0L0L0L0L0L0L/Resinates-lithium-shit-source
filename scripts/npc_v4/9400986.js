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
            if (cm.haveItem(ticketId, 5)) {
                if (cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.SETUP).getNumFreeSlot() >= 1) {
                                equiplist = cm.getChairs();
            var selStr = "";
                for (var i = 0; i < equiplist.size(); i++) {
                    var curEquip = equiplist.get(i);
                    if (curEquip != null) {
                        selStr += "#L" + i + "##i" + curEquip + "##l";
                    } else {
                        continue;
                    }
                }
                cm.sendSimple("Which Chair would you like to buy for 5 Reward Points?\r\n\ " + selStr);
                } else {
                    cm.sendOk("You currently do not have 1 or more Equip slots free.");
                                
                }
            } else {
				cm.sendOk("You currently do not have 5 or more Reward Points for Chair of choice.");
                            
            }
        } else if (status == 1) {
            equip = equiplist.get(selection);
            cm.gainItem(ticketId, -5);
            cm.gainItem(equip, 1);
            cm.sendOk("You have successfully exchanged 5 Donation Points for #i" + equip + "#.\r\n\ If chair appears invisible do not drop or trade it, save it and report the item to resinate.");
                        
        } else {
                        
        }
    }
}


