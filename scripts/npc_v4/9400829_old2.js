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
var potid = 0;

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
            if (cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.EQUIP).getNumFreeSlot() >= 1) {
                if (cm.haveItem(ticketId, 5)) {
                    cm.sendSimple("Welcome to the Ring of Choice Donation Shop. Pick a Ring you would like to purchase from, they all come with maxium power potentials?\r\n\I only accept 5 #i" + ticketId + "# Donation Points.\r\n\r\n#L0#Ring of All Stats (100%)?#l\r\n#L1#Ring of Total Damage (150%)?#l\r\n#L2#Ring of Boss Damage (250%)?#l\r\n#L3#Ring of Overpower (250%)?#l\r\n#L4#Ring of Item Drop (125%)?#l\r\n#L5#Ring of Exp (125%)?#l\r\n#L6#Ring of Meso (250%)?#l\r\n#L7#Ring of IED (250%)?#l\r\n ");
                } else {
                    cm.sendOk("You currently do not have 5 or more Donation Points.");
                                
                }
            } else {
                cm.sendOk("You currently do not have 1 or more Equip slots free.");
                            
            }
        } else if (status == 1) {
            if (selection == 0) {
                potid = 60002;
            } else if (selection == 1) {
                potid = 50029;
            } else if (selection == 2) {
                potid = 50149;
            } else if (selection == 3) {
                potid = 50549;
            } else if (selection == 4) {
                potid = 50224;
            } else if (selection == 5) {
                potid = 50324;
            } else if (selection == 6) {
                potid = 50449;
            } else if (selection == 7) {
                potid = 50749;
            }
            cm.gainItem(ticketId, -5);
            cm.gainEquip(1113077, cm.getPlayer().getTotalLevel(), 8, potid, 0);
            cm.sendOk("You have successfully exchanged 5 Donation Points for #i1113077#.\r\n\ If weapon appears invisible do not drop or trade it, save it and report the item and place item in very last slot of inventory.");
                        
        } else {
                        
        }
    }
}


