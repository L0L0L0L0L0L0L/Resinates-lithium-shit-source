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
var ticketId = 4310504;
var items = new Array(5052000, 2120000, 2430130, 2049032, 2340000, 2587000, 2587001, 4420008, 2583002, 4310500, 4000313, 2000012, 4310272, 4310502, 4420007, 4430005, 2049176, 2049307, 2586004, 2585007);
var mapName = ["Henesys", "Ellinia", "Perion", "Kerning City", "Sleepywood", "Mushroom Shrine", "Showa Spa (M)", "Showa Spa (F)", "Ludibrium", "New Leaf City", "El Nath", "Nautilus"];
var curMapName = "";
var option = 0;
var power = 0;
var scale = 1.0;
var questid = 170;
var questtime = 14400;//30 min
var wanted = 0;
var pot = 0;

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
        pot = 1 + cm.getPlayer().getAccVara("POT");
        var selStr = "";
        for (var i = 0; i < items.length; i++) {
            selStr += "#i" + items[i] + "#";
        }
        cm.sendYesNoS("Do you want to cash in #i" + ticketId + "# " + cm.getItemName(ticketId) + " for\r\n\#brandom lucky reward#k?\r\n#rNo refunds on what is givin here!#k\r\nHere are possible rewards:\r\n" + selStr, 16);
    } else if (status == 1) {
        if (cm.haveItem(ticketId, 1)) {
            var rewards = "";
            rewards += "#fUI/UIWindow.img/Quest/reward#\r\n";

            var combo = cm.random(0, 19);
            var item = 0;
            var count = 0;
            if (combo == 0) {
                item = 5052000;
                count = 1;
            }
            if (combo == 1) {
                item = 2120000;
                count = cm.random(1, 4);
            }
            if (combo == 2) {
                item = 2430130;
                count = cm.random(10, 25) + pot;
            }
            if (combo == 3) {
                item = 2049032;
                count = cm.random(4, 8);
            }
            if (combo == 4) {
                item = 2340000;
                count = cm.random(100, 500);
            }
            if (combo == 5) {
                item = 2587000;
                count = cm.random(100, 500);
            }
            if (combo == 6) {
                item = 2587001;
                count = cm.random(100, 500);
            }
            if (combo == 7) {
                item = 4420008;
                count = cm.random(100, 500);
            }
            if (combo == 8) {
                item = 2583002;
                count = cm.random(250, 500);
            }
            if (combo == 9) {
                item = 4310500;
                count = cm.getPlayer().getTotalLevel();
            }
            if (combo == 10) {
                item = 4000313;
                count = cm.random(250, 500);
            }
            if (combo == 11) {
                item = 2000012;
                count = cm.random(50, 250) + pot;
            }
            if (combo == 12) {
                item = 4310272;
                count = cm.random(1000, 2500) + (pot * 25);
            }
            if (combo == 13) {
                item = 4310502;
                count = cm.random(500, 1000);
            }
            if (combo == 14) {
                item = 4420007;
                count = 1;
            }
            if (combo == 15) {
                item = 4430005;
                count = 250 + (cm.random(pot, pot * 5));
            }
            if (combo == 16) {
                item = 2049176;
                count = cm.random(500, 2500) + (pot * 10);
            }
            if (combo == 17) {
                item = 2049307;
                count = cm.random(500, 2500) + (pot * 10);
            }
            if (combo == 18) {
                item = 2586004;
                count = cm.random(500, 2500) + (pot * 10);
            }
            if (combo == 19) {
                item = 2585007;
                count = cm.random(500, 2500) + (pot * 10);
            }
            if (cm.getPlayer().canHold(2050004, 1) && cm.getPlayer().canHold(4310020, 1) && cm.getPlayer().canHold(5076000, 1)) {
                if (item > 0) {
                    if (cm.getPlayer().canHold(item, count)) {
                        rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                        cm.gainItem(item, count);
                        rewards += "\r\n#rPlay another round?\r\n";
                        cm.gainItem(ticketId, -1);
                        cm.sendYesNo(rewards);
                    } else {
                        cm.sendOk("No room to hold anymore #i" + item + "# " + cm.getItemName(item) + "!\r\n#rMake sure you have a free slot open in USE-ETC-CASH#k");
                    }
                } else {
                    cm.sendOk("Error with item!");
                }
            } else {
                cm.sendOk("No room to hold anymore items\r\n#rMake sure you have a free slot open in USE-ETC-CASH#k");
            }
        } else {
            cm.sendOk("You dont have enough CP.");
        }
    } else if (status == 2) {
        status = 2;
        action(0, 0, 0);
    } else {
        cm.dispose();
    }
}