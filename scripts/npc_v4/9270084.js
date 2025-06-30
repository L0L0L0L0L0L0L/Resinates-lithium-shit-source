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
var ticketId = 2530000;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var pal;
var pals;
var scroll = 0;
var scrollslot = 0;
var cost = 1;
var slotcount = 0;
var cube = 2530000;
var white = 2530000;
var count = 0;
var min = 0;
var max = 0;
var chance = 0;
var safe = 0;
var pscroll = 0;
var tier = 0;
var success = 0;
var power = 0;
var index = 0;

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
        pals = cm.getPals();
        if (!pals.isEmpty()) {
            var selStr = "";
            for (var i = 0; i < pals.size(); i++) {
                var currPal = pals.get(i);
                var tempPal = cm.getTemplate(currPal);
                selStr += "#L" + i + "##fUI/Custom.img/shared/element/" + tempPal.element() + "# " + tempPal.name() + " (#rPrice: " + (tempPal.evo() * tempPal.evo() * 20) + " Dolls#k)#l\r\n";
                count++;
            }
            if (count > 0) {
                cm.sendSimpleS("Which pal would you like to buy?\r\nI only accept #r" + cm.getItemName(4310510) + "'s#k\r\n" + selStr, 16);
            } else {
                cm.sendOkS("You currently do not have any Equips to lucky day.", 16);
            }
        } else {
            cm.sendOkS("You currently do not have any Equips to lucky day.2 #i4110005#  ", 16);
        }
    } else if (status == 1) {
        if (pal == null) {
            index = selection;
            pal = pals.get(selection);
        }
        cm.removePal(pal);
        //cm.sendYesNoS("ARe you sure you wish to apply #i" + cube + "# to \r\n#i" + pal.getItemId() + "# " + cm.getItemName(pal.getItemId()) + "?", 16);
        cm.sendOk("list updated: " + pal + " - index: " + index);
    } else if (status == 2) {
        if (cm.haveItem(cube, 1)) {
            cm.gainItem(cube, -1);
            cm.getPlayer().addLucky(pal);
            cm.sendOk("#i" + cube + "# has been applied to #i" + pal.getItemId() + "# " + cm.getItemName(pal.getItemId()) + ".");
        } else {
            cm.sendOk("Seems you dont have enough #i" + cube + "#");
        }

    } else if (status == 3) {

    } else if (status == 4) {

    }
}


