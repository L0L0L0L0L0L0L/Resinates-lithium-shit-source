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
var ticketId = 4310501;
var item1;
var item2;
var item3;
var rewards = new Array(2000000, 2000001, 2000002, 2000003, 2000006, 2000004, 2000005, 2340000, 2473001, 2049300, 2049185, 2049186, 4310500, 4310501, 2049187, 2049188,2473000, 4001514,4001516,4001522,4001760);
var amount = new Array(100, 250, 500, 100, 250, 1000, 2500, 10, 100, 50, 250, 100, 1, 100, 50, 25, 1000, 10, 10, 10, 5);

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
            if (cm.haveItem(ticketId, 1)) {
                var selStr = "";
                for (var i = 0; i < rewards.length; i++) {
                    selStr += "#i" + rewards[i] + "#  " + cm.getItemName(rewards[i]) + " (x" + amount[i] + ")\r\n\ ";
                }
                cm.sendYesNo("Would you like to spend 1 MP for a chance to win random rewards? All 3 item Icons have to match in order to win the reward. Make sure you enough room in your inventory for rewards. Here is a list of rewards obtainable: \r\n\r\n\ " + selStr);
            } else {
                cm.sendOk("Sorry, you dont have any maple points.");
                            
            }
        } else if (status == 1) {
            if (cm.haveItem(ticketId, 1)) {
                cm.gainItem(ticketId, -1);
                var count = cm.random(0, rewards.length - 1);
                item1 = rewards[count];
                item2 = rewards[cm.random(0, rewards.length - 1)];
                item3 = rewards[cm.random(0, rewards.length - 1)];
                var selStr = "#i" + item1 + "#   #i" + item2 + "#    #i" + item3 + "# \r\n\r\n\ ";
                if (item2 == item1 && item3 == item2 && item3 == item1) {
                    if (cm.canHold(item1, amount[count])) {
                        cm.gainItem(item1, amount[count]);
                        cm.sendYesNo(selStr + " \r\n\WINNER!!\r\n\ Do you want to play again?");
                    } else {
                        cm.sendOk("Sorry, you dont have any maple points.");
                                    
                    }
                } else {
                    cm.sendYesNo(selStr + " \r\n\Sorry, No matching symbols.\r\n\ Do you want to play again?");
                }
            } else {
                cm.sendOk("Sorry, you dont have any donation points.");
                            
            }
        } else if (status == 2) {
            status = 2;
            action(0, 0, 0);
        } else {
                        
        }
    }

}

