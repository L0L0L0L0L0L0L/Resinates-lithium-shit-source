/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
var status = 0;
var option = 0;
var item = 0;
var ticketId = 4036572;
var reward = 4021021;
var rewamount = 1;

var items = new Array(4021022, 4033442, 4033443, 4033444, 4033445, 4033446, 4033447, 4033448, 4033449, 4033450);
var amount = new Array(10, 1, 1, 1, 1, 1, 1, 1, 1, 1);

var itemSet = new Array(4033442, 4033443, 4033444, 4033445, 4033446, 4033447, 4033448, 4033449, 4033450);
var selectedItemStone = -1;
var matSet = new Array(4033433, 4033434, 4033435, 4033436, 4033437, 4033438, 4033439, 4033440, 4033441);
var selectedItem = -1;
var selectedItemCount = -1;


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
        var options = "#L1# Craft #i" + reward + "# " + cm.getItemName(reward) + " #l\r\n";
        options += "#L2# Convert Stones#l\r\n";

        cm.sendSimple("Which Option would you like to do?\r\n" + options);
    } else if (status == 1) {
        if (option == 0) {
            option = selection;
        }
        if (option == 1) {
            var selStr = "#i" + items[0] + "# #b" + cm.getItemName(items[0]) + "#k (x#r10#k)\r\n";
            selStr += "Then Bring me each of the following Stones\r\n";
            for (var i = 1; i < items.length; i++) {
                selStr += "#i" + items[i] + "#";
            }

            cm.sendGetNumber("Craft how many #i" + reward + "# " + cm.getItemName(reward) + "s?\r\n" + selStr + "\r\n .", 1, 1, 30000);
        }
        if (option == 2) {
            var mats = "Which stone would you like to auto convert?\r\nEach stone costs #b5000 baisc stones#k to upgrade.\r\n";
            mats += "You currently have the following Stones:\r\n";
            for (var i = 0; i < matSet.length; i++) {
                if (cm.getPlayer().countAllItem(matSet[i]) > 0) {
                    mats += "#L" + i + "# #i" + matSet[i] + "# " + cm.getItemName(matSet[i]) + " - #b" + cm.convertNumber(cm.getPlayer().countAllItem(matSet[i])) + "#kx\r\n";
                }
            }
            cm.sendSimple(mats);
        }

    } else if (status == 2) {
        if (option == 1) {
            var amountb = selection;
            var craft = true;
            for (var i = 0; i < items.length; i++) {
                if (!cm.haveItem(items[i], amount[i] * amountb)) {
                    craft = false;
                    break;
                }
            }
            if (craft) {
                for (var i = 0; i < items.length; i++) {
                    cm.gainItem(items[i], (-amount[i] * amountb));
                }
                cm.gainItem(reward, rewamount * amountb);
                cm.sendOk("I have Crafted you " + amountb + "x #i" + reward + "#.");
            } else {
                cm.sendOk("You currently do not have enough materials to craft #i" + reward + "#.\r\n#rGo suck some dick please#k. Thank You.");
            }
        }
        if (option == 2) {
            var sel = selection;
            if (sel >= 0 && sel < 9) {
                selectedItemStone = itemSet[sel];
                selectedItem = matSet[sel];
                if (cm.haveItem(selectedItem, 5000)) {
                    selectedItemCount = Math.floor(cm.getPlayer().countAllItem(selectedItem) / 5000);
                    cm.sendYesNo("Do you to convert the following:\r\nFrom: #i" + selectedItem + "# " + cm.getItemName(selectedItem) + " #b" + (selectedItemCount * 5000) + "x#k\r\nTo: #i" + selectedItemStone + "# " + cm.getItemName(selectedItemStone) + " #b" + (selectedItemCount) + "x#k?\r\n");
                } else {
                    cm.sendOk("You dont have enough materials to craft #i" + selectedItemStone + "#,\r\n#rGo suck some dick please#k. Thank You.");
                }
            } else {
                cm.sendOk("Go suck some dick please#k. Thank You.");
            }
        }
    } else if (status == 3) {
        if (selectedItemCount > 0 && cm.haveItem(selectedItem, 5000 * selectedItemCount)) {
            if (cm.canHold(selectedItemStone, selectedItemCount)) {
                cm.gainItem(selectedItem, -(5000 * selectedItemCount));
                cm.gainItem(selectedItemStone, selectedItemCount);
                cm.sendYesNo("You have gained #i" + selectedItemStone + "# " + cm.getItemName(selectedItemStone) + " #b" + selectedItemCount + "x#k.\r\n#bDo you want to convert more stones?#k");
            } else {
                cm.sendOk("You dont have enough space to hold #i" + selectedItemStone + "#,\r\n#rGo suck some dick please#k. Thank You.");
            }
        } else {
            cm.sendOk("You dont have enough space to hold #i" + selectedItemStone + "#,\r\n#rGo suck some dick please#k. Thank You.");
        }
    } else if (status == 4) {
        status = 2;
        action(0, 0, 0);
    }
}


