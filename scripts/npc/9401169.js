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
/* 9000021 - Gaga
 BossRushPQ recruiter
 @author Ronan
 */

var status;
var level = 250;
var item = 4036088;
var option = -1;
var count = 0;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var ach = 0;
var limit = 16;
var page = 0;
var stat = new Array("Str", "Dex", "Int", "Luk");
var atk = new Array("Melee", "Magic");

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
        var selStr = "#bCurrent Core Stats#k:\r\n";
        var m = cm.getPlayer().getVarZero("Main");
        var s = cm.getPlayer().getVarZero("Sub");
        var a = cm.getPlayer().getVarZero("Attack");
        selStr += star + "Primary Stat: #r" + stat[m] + "#k - (#b" + cm.getFullUnitNumber(cm.getPlayer().getPrimary()) + "#k)\r\n";
        selStr += star + "Attack: #r" + atk[a] + "#k - (#b" + cm.getFullUnitNumber(cm.getPlayer().getAtk()) + "#k)\r\n";
        selStr += star + "Total Core Power: #b" + cm.getFullUnitNumber(cm.getPlayer().getPrimary() + cm.getPlayer().getAtk()) + "#k\r\n";
        cm.sendYesNo(selStr + "\r\nDo you wish to #rRandomly Re-Roll#k your #bCore Stat#k?\r\nPrice: #r" + cm.getItemName(item) + "#k");
    } else if (status == 1) {
        cm.sendYesNo("To re-roll stats i will need #i" + item + "# " + cm.getItemName(item) + "#k\r\n#rAre you sure you want to proceed?\r\nThere is no refunds or rollbacks.#k\r\nThe Stats you gain are #rRandomized#k.");
    } else if (status == 2) {
        if (cm.haveItem(item, 1)) {
            cm.gainItem(item, -1);
            cm.getPlayer().resetCore();
            var selStr = "#bUpdated Core Stats#k:\r\n";
            var m = cm.getPlayer().getVarZero("Main");
            var s = cm.getPlayer().getVarZero("Sub");
            var a = cm.getPlayer().getVarZero("Attack");
            selStr += star + "Primary Stat: #r" + stat[m] + "#k - (#b" + cm.getFullUnitNumber(cm.getPlayer().getPrimary()) + "#k)\r\n";
            selStr += star + "Attack: #r" + atk[a] + "#k - (#b" + cm.getFullUnitNumber(cm.getPlayer().getAtk()) + "#k)\r\n";
            selStr += star + "Total Core Power: #b" + cm.getFullUnitNumber(cm.getPlayer().getPrimary() + cm.getPlayer().getAtk()) + "#k\r\n";
            cm.getPlayer().getStat().recalcLocalStats(cm.getPlayer());
            cm.sendOk(selStr);
        } else {
            cm.sendOk("Where is my #b#i" + item + "# " + cm.getItemName(item) + "#k????");
        }
    }
}