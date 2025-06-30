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
/* Author: Xterminator
 NPC Name: 		Shanks
 Map(s): 		Maple Road : Southperry (60000)
 Description: 		Brings you to Victoria Island
 */
var status = 0;

function start() {
    if (cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.ETC).getNumFreeSlot() >= 2) {
        cm.sendYesNo("Claim your missing things?");
    } else {
        cm.sendOk("Please make room in your ETC.");
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        var text = "Current Rewards applied:\r\n";
        var count = 0;
        if (cm.getPlayer().addDP()) {
            count++;
            text += "Donation Points #i4310502# has been transfered into your invnetory from auto refund.\r\n";
        }
        if (cm.getPlayer().addVP()) {
            count++;
            text += "Vote Points #i4310503# has been transfered into your invnetory.\r\n";
        }
        if (cm.getPlayer().getTotalLevel() >= 9999) {
            cm.getPlayer().gainDamageSkinNoOrb(6000);
        }
        
        cm.getPlayer().gainDamageSkinNoOrb(6001);
        if (count < 1) {
            text = "You have no rewards to collect at this time.";
        } else {
            cm.getPlayer().saveToDB(false, false);
        }
        cm.sendOk(text);
    }
}

