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
var ticketId = 5220000;
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
        wanted = cm.getPlayer().getAccVar("Pass");
        pot = cm.getPlayer().getAccVara("POT");
        var rolls = 10 + (pot * 5);
        if (rolls > 100) {
            rolls = 100;
        }
        if (wanted > rolls) {
            cm.getPlayer().setAccVar("Pass", 0);
            cm.getPlayer().setQuestLock(questid, questtime);
        }
        if (cm.getPlayer().getQuestLock(questid) > 0) {
            cm.sendOk("#rYou have won too many rewards for now#k\r\nYou can come back to me after\r\n#b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k\r\nto claim more free rewards.");
        } else {
            amount = 0;
            var number1 = cm.random(1, 9);
            var number2 = cm.random(1, 9);
            var number3 = cm.random(1, 9);
            var number4 = cm.random(1, 9);
            password = cm.getCode(number1, number2, number3, number4);
            cm.sendGetText(" \r\n#rWelcome to Great Anti-Bot Reward System!\r\n\#kPlease enter the 4 digit #rCode#k Code seen below:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "\r\n ");
        }
    } else if (status == 1) {
        amount = Number(cm.getText());
        if (amount == password) {
            cm.getPlayer().addAccVar("Pass", 1);
            var rewards = "";
            if ((wanted - 1) > 0) {
                rewards += "You have currently have a combo of #b" + (wanted - 1) + "#k rewards.\r\n\r\n";
            }
            rewards += "#fUI/UIWindow.img/Quest/reward#\r\n";

            var combo = cm.random(0, 18);
            if (combo == 0) {
                rewards += "#rNo Luck this time... Sorry...#k\r\n";
            }
            if (combo == 1) {
                var item = 2120000;
                var count = 1;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 2) {
                var item = 2430130;
                var count = 15 + pot;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 3) {
                var item = 2049032;
                var count = 5 + pot;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 4) {
                var item = 2340000;
                var count = 250;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 5) {
                var item = 2587000;
                var count = 200;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 6) {
                var item = 2587001;
                var count = 100;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 7) {
                var item = 4420008;
                var count = 250;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 8) {
                var item = 2583002;
                var count = 500;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 9) {
                var item = 4310500;
                var count = 10 * cm.getPlayer().getTotalLevel();
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }

            if (combo == 10) {
                var item = 4000313;
                var count = cm.getPlayer().getTotalLevel();
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }

            if (combo == 11) {
                var item = 2000012;
                var count = 100 + pot;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 12) {
                var item = 4310101;
                var count = (10 + pot) * cm.getPlayer().getTotalLevel();
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 13) {
                var item = 4420009;
                var count = 2500;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 14) {
                var item = 4420010;
                var count = 1 + pot;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 15) {
                var item = 4430005;
                var count = 250;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 16) {
                var item = 2049176;
                var count = 150 + pot;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 17) {
                var item = 2049307;
                var count = 100 + pot;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            if (combo == 18) {
                var item = 2586004;
                var count = 50 + pot;
                rewards += "#i" + item + "# " + cm.getItemName(item) + " (x#b" + count + ")#k\r\n";
                cm.getPlayer().addOverflow(item, count);
            }
            rewards += "\r\n#rPlay another round?\r\n";
            cm.sendYesNo(rewards);
        } else {
            cm.sendOk("Wrong password mother fka.");
            cm.getPlayer().setAccVar("Pass", 0);
            cm.getPlayer().setQuestLock(questid, questtime);
        }
    } else if (status == 2) {
        status = 1;
        action(0, 0, 0);
    } else {
        cm.getPlayer().setAccVar("Pass", 0);
        cm.getPlayer().setQuestLock(questid, questtime);
        cm.dispose();
    }
}