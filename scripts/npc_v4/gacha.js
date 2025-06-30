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
/* NPC Base
 Map Name (Map ID)
 Extra NPC info.
 
 
 cm.gainItem(4310001, amount);
 cm.getPlayer().getCashShop().gainCash(2, amount);
 
 */

importPackage(Packages.client);
importPackage(Packages.tools);
importPackage(Packages.server.life);

var status;
var option = 0;
var item = 0;
var mPoint = 0;
var amount = 0;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var power = 0;
var cost = 0;




function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    }
        if (status == 0 && mode == 1) {
            mPoint = cm.getPlayer().getCashShop().getCash(2);
            //cm.sendOk("Sorry, npc is temped closed");
            //            
            if (mPoint >= 10) {
                cm.sendYesNo("Nx gacha gives 5 Random NX Equips and 1 Random NX Weapon and Rare chance for a Legendary Chair.\r\n Do you confirm you want to spend 10 MP for this?");
            } else {
                cm.sendOk("Sorry, you dont seem to have any maple points.");
                            
            }
        } else if (status == 1) {
            if (cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.SETUP).getNumFreeSlot() >= 1 && cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.EQUIP).getNumFreeSlot() >= 6) {
                var power = (cm.getPlayer().getTotalLevel());
                var text = "You have recieved the following items:\r\n\r\n";
                text += "NX Equips:\r\n";
                for (var i = 0; i < 5; i++) {
                    var item = cm.getRandomNx();
                    cm.gainEquip(item, power);
                    text += "#i" + item + "#";
                }
                text += "\r\n\r\nNX Weapon:\r\n";
                var weapon = cm.getRandomNxWeapon();
                cm.gainEquip(weapon, power);
                text += "#i" + weapon + "#";

                cm.getPlayer().getCashShop().gainCash(2, -10);
                cm.sendOk(text);
                            
            } else {
                cm.sendOk("Not Enough space. Requires 6 free equip slots and 1 setup slot.");
                            
            }
        } else {
            cm.sendOk("Have a good day.");
                        
        }
    }