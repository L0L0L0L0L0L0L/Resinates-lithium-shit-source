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
var use;
var equiplist;
var item = 0;
var price = 0;
var days = 0;


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
        var count = 0;
        equiplist = cm.getPlayer().getItemsByType(2);
        if (!equiplist.isEmpty()) {
            var selStr = "";
            for (var i = 0; i < equiplist.size(); i++) {
                var curEquip = equiplist.get(i);
                if (curEquip != null && curEquip.getExpiration() > 0) {
                    if (curEquip.getItemId() == 2450024 || curEquip.getItemId() == 2450025 || curEquip.getItemId() == 2450050 || curEquip.getItemId() == 2450051 || curEquip.getItemId() == 2450052 || curEquip.getItemId() == 2450053) {
                        count += 1;
                        var cost = 5;
                        selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " (Price: #r" + cost + "#k)\r\n";
                        var time = cm.getPlayer().getRemainingTime(curEquip.getExpiration());
                        if (time <= 0) {
                            selStr += "                Time: #rExpired#k#l\r\n";
                        } else {
                            selStr += "                Time: #b" + cm.secondsLongToString(time) + "#k#l\r\n";
                        }
                    }
                }
            }
            if (count > 0) {
                cm.sendSimpleS("Select Which buff you wish to extend:\r\n#rPrice is Per Day added to buff using#k #b" + cm.getItemName(4310505) + "s#k\r\n" + selStr, 16);
            } else {
                cm.sendOkS("You currently do not have any Buffs to expand.", 16);
            }
        } else {
            cm.sendOkS("You currently do not have any Buffs.", 16);
        }
    } else if (status == 1) {
        item = equiplist.get(selection);
        price = 1;
        if (item.getItemId() == 2450025 || item.getItemId() == 2450050 || item.getItemId() == 2450052) {
            price = 5;
        }
        if (item.getItemId() == 2450051 || item.getItemId() == 2450053) {
            price = 5;
        }
        var selStr = "";
        var time = cm.getPlayer().getRemainingTime(item.getExpiration());
        if (time <= 0) {
            selStr += "Current Time Remainging: #rExpired#k#l\r\n";
        } else {
            selStr += "Current Time Remainging: #b" + cm.secondsLongToString(time) + "#k#l\r\n";
        }
        cm.sendGetText("How many days do u wish to purchase?\r\nCurrent Price for #b" + cm.getItemName(item.getItemId()) + "#k is #r" + price + "#k per day.\r\n" + selStr);
    } else if (status == 2) {
        days = cm.getNumber();
        if (days > 0 && days < 999) {
            if (cm.getPlayer().haveItem(4310505, price * days)) {
                cm.sendYesNo("Are you sure you want to spend #b" + (price * days) + "#k #r" + cm.getItemName(4310505) + "#k to extended #r" + cm.getItemName(item.getItemId()) + "#k by #b" + days + "#k days?");
            } else {
                cm.sendOk("You do not have enough #b" + cm.getItemName(4310505) + "#k to extend this buff.");
            }
        } else {
            cm.sendOk("Take a hike.");
        }

    } else if (status == 3) {
        if (cm.getPlayer().haveItem(4310505, price * days)) {
            cm.gainItem(4310505, -(price * days));
            cm.getPlayer().addTime(item, days);
            cm.sendOk("You have successfully extended the time on \r\n#b" + cm.getItemName(item.getItemId()) + "#k.\r\nNew Time:#b" + cm.secondsLongToString(cm.getPlayer().getRemainingTime(item.getExpiration())) + "#k");

        } else {
            cm.sendOk("You do not have enough #b" + cm.getItemName(4310505) + "#k to extend this buff.");
        }
    } else if (status == 4) {
        status = 2;
        action(0, 0, 0);
    }
}


