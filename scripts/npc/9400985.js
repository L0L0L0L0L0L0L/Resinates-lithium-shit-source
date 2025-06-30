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
var ticketId = 4310505;
var mapName = ["Henesys", "Ellinia", "Perion", "Kerning City", "Sleepywood", "Mushroom Shrine", "Showa Spa (M)", "Showa Spa (F)", "Ludibrium", "New Leaf City", "El Nath", "Nautilus"];
var curMapName = "";
var option = 0;
var power = 0;
var scale = 1.0;
var item = 0;
var rewards;

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
        cm.sendSimple("Welcome to the Expression Gachapon\r\nEach expression costs 5 #i" + ticketId + "# " + cm.getItemName(ticketId) + "\r\n#rThese Expressions are purely cosmetic#k\r\n\r\n#L4#Cash in for a random Expression?#l");
    } else if (status == 1) {
        if (selection == 4) {
            rewards = cm.getAllRewards(9900001);
            if (!rewards.isEmpty()) {
                var text = "Pick an Expression can be obtained from this Gachapon:\r\n\r\n";
                var iter = rewards.iterator();
                while (iter.hasNext()) {
                    var i = iter.next();
                    text += "#L" + i + "##i" + i + "##l";
                }
            }
            cm.sendSimple(text);
        }
    } else if (status == 2) {
        if (cm.haveItem(ticketId, 5)) {
            cm.gainItem(ticketId, -5);
            cm.gainItem(selection, 1);
            cm.sendOk("You can gained #i" + selection + "#.");
        } else {
            cm.sendOk("You don't have 5 #i4310505#.");
        }
    }
}