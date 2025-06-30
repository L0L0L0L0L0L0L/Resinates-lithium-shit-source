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
/* Author: Xterminator
 NPC Name: 		Shanks
 Map(s): 		Maple Road : Southperry (60000)
 Description: 		Brings you to Victoria Island
 */
var status = 0;
var option;
var amount;
var stat;

function start() {
    if (cm.haveItem(4310502)) {
        var selStr = "Each Donation Point adds +5% (UNCAPPED) Perma stat. \r\n Which Stat would you like to purchase?#b";
        selStr += "\r\n#L0# Exp#l";
        selStr += "\r\n#L1# Drop#l";
        selStr += "\r\n#L3# All Stat#l";
        selStr += "\r\n#L4# Overpower#l";
        selStr += "\r\n#L5# Meso#l";
        selStr += "\r\n#L6# Total Damage#l";
        selStr += "\r\n#L7# Boss Damage#l";
        selStr += "\r\n#L8# IED#l";
        cm.sendSimple(selStr);
    } else {
        cm.sendOk("Sorry, you dont have any donation points.");

    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        option = selection;
        if (option == 0) {
            stat = "Exp";
        } else if (option == 1) {
            stat = "Drop";
        } else if (option == 3) {
            stat = "All Stat";
        } else if (option == 4) {
            stat = "Overpower";
        } else if (option == 5) {
            stat = "Meso";
        } else if (option == 6) {
            stat = "Total Damage";
        } else if (option == 7) {
            stat = "Boss Damage";
        } else if (option == 8) {
            stat = "IED";
        }
        cm.sendGetText("How much Donation Points do you want to apply to " + stat + "?\r\n");
    } else if (status == 2) {
        amount = cm.getNumber();
        if (amount > 0 && cm.haveItem(4310502, amount)) {
            if (amount + cm.getPlayer().getStat(option) <= 99999) {
                cm.gainItem(4310502, -amount);
                cm.getPlayer().addStat(amount * 5, option, true);
                cm.sendOk("You have purchased " + (amount * 5) + "% " + stat + " for " + amount + " Donation Points.");
            } else {
                cm.sendOk("Sorry, you have already maxxed out that perma stat.");
            }
        } else {
            cm.sendOk("Sorry, you dont have any donation points.");
        }
    }
}


