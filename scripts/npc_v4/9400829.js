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
var cube = 4310502;
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
        selStr += star + "Primary (10%): #r" + stat[m] + "#k - (#b" + cm.getFullUnitNumber(cm.getPlayer().getPrimary()) + "#k)\r\n";
        selStr += star + "Secondary (5%): #r" + stat[s] + "#k - (#b" + cm.getFullUnitNumber(cm.getPlayer().getSecondary()) + "#k)\r\n";
        selStr += star + "Attack (25%): #r" + atk[a] + "#k - (#b" + cm.getFullUnitNumber(cm.getPlayer().getAtk()) + "#k)\r\n";
        selStr += star + "Total Core Power: #b" + cm.getFullUnitNumber(cm.getPlayer().getPrimary() + cm.getPlayer().getSecondary() + cm.getPlayer().getAtk()) + "#k\r\n";
        cm.sendYesNo(selStr + "\r\nDo you wish to change your core stats?");
    } else if (status == 1) {
        var selStr = "Select your #bPrimary#k Stat:\r\n";
        selStr += "#L0#" + star + " Str#l\r\n";
        selStr += "#L1#" + star + " Dex#l\r\n";
        selStr += "#L2#" + star + " Int#l\r\n";
        selStr += "#L3#" + star + " Luk#l\r\n";
        cm.sendSimple(selStr);
    } else if (status == 2) {
        cm.getPlayer().setVar("Main", selection);
        var selStr = "Select your #bSecondary#k Stat:\r\n";
        selStr += "#L10#" + star + " Str#l\r\n";
        selStr += "#L11#" + star + " Dex#l\r\n";
        selStr += "#L12#" + star + " Int#l\r\n";
        selStr += "#L13#" + star + " Luk#l\r\n";
        cm.sendSimple(selStr);
    } else if (status == 3) {
        cm.getPlayer().setVar("Sub", selection - 10);
        var selStr = "Select your #bAttack#k Stat:\r\n";
        selStr += "#L20#" + star + " Melee#l\r\n";
        selStr += "#L21#" + star + " Magic#l\r\n";
        cm.sendSimple(selStr);
    } else if (status == 4) {
        cm.getPlayer().setVar("Attack", selection - 20);
        var selStr = "#bUpdated Core Stats#k:\r\n";
        var m = cm.getPlayer().getVarZero("Main");
        var s = cm.getPlayer().getVarZero("Sub");
        var a = cm.getPlayer().getVarZero("Attack");
        selStr += star + "Primary (10%): #r" + stat[m] + "#k - (#b" + cm.getFullUnitNumber(cm.getPlayer().getPrimary()) + "#k)\r\n";
        selStr += star + "Secondary (5%): #r" + stat[s] + "#k - (#b" + cm.getFullUnitNumber(cm.getPlayer().getSecondary()) + "#k)\r\n";
        selStr += star + "Attack (25%): #r" + atk[a] + "#k - (#b" + cm.getFullUnitNumber(cm.getPlayer().getAtk()) + "#k)\r\n";
        selStr += star + "Total Core Power: #b" + cm.getFullUnitNumber(cm.getPlayer().getPrimary() + cm.getPlayer().getSecondary() + cm.getPlayer().getAtk()) + "#k\r\n";
        cm.getPlayer().getStat().recalcLocalStats(cm.getPlayer());
        cm.sendOk(selStr);
    }
}