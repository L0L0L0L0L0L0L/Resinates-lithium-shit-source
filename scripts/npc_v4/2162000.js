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
var reward = 4033320;
var rewamount = 1;
var items = new Array(4009169, 4009170, 4009171, 4030034);
var amount = new Array(25000, 25000, 25000, 1000);

var bitems = new Array(4034180, 4034181, 4034182, 4034183);
var bamount = new Array(2500, 2500, 2500, 2500);
var option = 0;

var boss = "Khan";
var ach = 404;
var instance = "BGC_Khan";

var Cantidad;
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            option = 1;
            var selStr = "";
            for (var i = 0; i < items.length; i++) {
                selStr += "#i" + items[i] + "#  " + cm.getItemName(items[i]) + " (x#b" + amount[i] + "#k)\r\n\ ";
            }
            cm.sendYesNo("Bring me the following materials so I can Craft #i4030035#: \r\n\r\n\ " + selStr);
        } else if (status == 1) {
            if (option == 1) {
                var count = 0;
                for (var i = 0; i < items.length; i++) {
                    if (cm.haveItem(items[i], amount[i])) {
                        count++;
                    }
                }
                if (count >= items.length) {
                    for (var i = 0; i < items.length; i++) {
                        cm.gainItem(items[i], -amount[i]);
                    }
                    cm.gainItem(4030035, 1);
                    cm.sendOk("Use this crystal for Castle Golem Boss!");
                } else {
                    cm.sendOk("You currently do not have enough materials for the #i4030035#");
                }
            }
        } else {
            cm.dispose();
        }
    }
}


