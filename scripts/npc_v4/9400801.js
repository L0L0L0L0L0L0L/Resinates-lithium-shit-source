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
 */

var status;
var ticketId = 4310502;
var mapName = ["Henesys", "Ellinia", "Perion", "Kerning City", "Sleepywood", "Mushroom Shrine", "Showa Spa (M)", "Showa Spa (F)", "Ludibrium", "New Leaf City", "El Nath", "Nautilus"];
var curMapName = "";
var option = 0;
var power = 0;
var pets;
var spet;

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
    if (status == 0 && mode == 1) {

        var text = "Welcome to the Pet Gachapon. How may I help you?\r\n\r\n";
        //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
        text += "#L4#What are the Pets in this Gachapon?#l\r\n";
        //text += "#L5##rBuy Random Pet for 5 Reward Points?#k#l\r\n";
        text += "#L6##bBuy Pet of Choise for 3 Infinity Points#k#l\r\n";
        text += "#L7##bRename Pet for 1 Infinity Point#k#l\r\n";
        //text += "#L8645340# #bAssailant#k #l\r\n";
        cm.sendSimple(text);


        cm.sendSimple("");
    }
    if (status == 1) {
        option = selection;
        if (option == 4) {
            var rewards = cm.getPets();
            if (!rewards.isEmpty()) {
                var text = "The following items can be obtained from this Gachapon :\r\n\r\n";
                var iter = rewards.iterator();
                while (iter.hasNext()) {
                    var i = iter.next();
                    text += "#i" + i + "#";
                }
            }
            cm.sendOk(text);
        }
        if (option == 5) {
            power = cm.getPlayer().getTotalLevel();
            if (cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.CASH).getNumFreeSlot() >= 1) {
                if (cm.haveItem(ticketId, 5)) {
                    option = 1;
                    cm.sendYesNo("Hello#b #h ##k, Would you like to exchange 5 Donation Points for a random Pet?\r\n#kEach Pet is Cosmetic only and last forever.");
                } else {
                    option = 2;
                    cm.sendOk("You don't have enough DP. You can find these by Donating.");

                }
            } else {
                cm.sendOk("You don't have enough inventory space inside CASH tab to collect the pet.");

            }
        }
        if (option == 6) {
            var rewards = cm.getPets();
            if (!rewards.isEmpty()) {
                var text = "Select a pet you wish to buy\r\n\r\n";
                var iter = rewards.iterator();
                while (iter.hasNext()) {
                    var i = iter.next();
                    text += "#L" + i + "##i" + i + "##l";
                }
            }
            cm.sendSimple(text);
        }
        if (option == 7) {
            pets = cm.getPlayer().getPets();
            if (!pets.isEmpty()) {
                var text = "Select which pet to name\r\n\r\n";
                for (var i = 0; i < pets.size(); i++) {
                    var pet = pets.get(i);
                    text += "#L" + i + "# #b" + pet.getName() + "#k #l\r\n";
                }
                cm.sendSimple(text);
            } else {
                cm.sendOk("You dont have any active pets.");
            }

        }
    }
    if (status == 2) {
        if (option == 5) {
            if (cm.haveItem(ticketId, 5)) {
                cm.gainItem(ticketId, -5);
                var id = cm.getRandomPet();
                cm.gainItem(id, 1);
                cm.sendOk("Congrats, you have won #i" + id + "# pet");
            } else {
                cm.sendOk("You don't have enough Donation Points.");
            }
            return;
        }
        if (option == 6) {
            if (cm.haveItem(4310505, 3)) {
                cm.gainItem(4310505, -3);
                cm.gainItem(selection, 1);
                cm.sendOk("Congrats, you have won #i" + selection + "# pet");
            } else {
                cm.sendOk("You don't have enough Infinity Points.");
            }
            return;
        }
        if (option == 7) {
            spet = pets.get(selection);
            cm.sendGetText("Enter a name you like to use. Current #b" + spet.getName() + "#k\r\n#rMax Name Length is 16 characters#k\r\n\r\n");
            return;
        }

    }
    if (status == 3) {
        if (option == 7) {
            if (cm.haveItem(4310505, 1)) {
                var name = cm.getText();
                if (cm.getTextSize() > 16) {
                    cm.sendOk("Name you entered is too long.");
                    return;
                }
                cm.gainItem(4310505, -1);
                var oName = spet.getName();
                spet.setName(name);
                spet.saveToDb();
                cm.getPlayer().fakeRelog();
                cm.sendOk("You have renamed #r" + oName + "#k to #b" + name + "#k. ");
            } else {
                cm.sendOk("You don't have enough Infinity Points.");
            }
        }
    }
}