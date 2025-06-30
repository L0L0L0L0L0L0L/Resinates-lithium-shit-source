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
var option = 0;
var random = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    }
        if (status == 0) {
            cm.getPlayer().setLock(true);
            cm.getPlayer().botchecker();
            random = cm.random(3994115, 3994118);
            cm.sendSimpleS("Please select correct matching color to #v" + random + "# to be rewarded with 10 MP.\r\n\You have 30 Seconds to pick correct Color. \r\n\r\n\r\n #L0# #v3994115##l #L1# #v3994116##l #L2# #v3994117##l #L3# #v3994118##l", 1);
        } else if (status == 1) {
            var amount = 3994115 + selection;
            if (amount == random) {
                cm.getPlayer().setLock(false);
                cm.gainItem(4031034, 1);
                cm.sendOkS("You have passed the bot test. You have been rewarded with 1 #i4031034#!", 1);
            } else {
                cm.getPlayer().setLock(false);
                cm.getPlayer().kick();
            }
                        
        }
    }