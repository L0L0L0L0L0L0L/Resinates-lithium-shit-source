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
var option = 0;
var box = 0;
var amount = 0;
var job = 0;
var price = 1;

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
            cm.sendYesNoS("Would you like to change jobs for the #bsmall price#k: \r\n#i4310505# " + cm.getItemName(4310505) + " (#r" + price + "x#k)?\r\n#rThis cannot be refunded or taken back. Make sure your fully geared for job change.#k\r\n#rWarning: Skills keybinds do not carry over, skills are saved when changing back.#k", 16);
        } else if (status == 1) {
            if (cm.getPlayer().getTotalLevel() >= 1000) {
                if (cm.haveItem(4310505, price)) {
                    var text = "";
                    text += "#L112##b Hero #k#l\r\n";
                    text += "#L122##b Paladin #k#l\r\n";
                    text += "#L132##b Dark Knight #k#l\r\n";
                    text += "#L212##b Fire Mage #k#l\r\n";
                    text += "#L222##b Ice Mage #k#l\r\n";
                    text += "#L232##b Bishop #k#l\r\n";
                    text += "#L312##b Bowman #k#l\r\n";
                    text += "#L322##b X-Bowman #k#l\r\n";
                    text += "#L412##b Hermit #k#l\r\n";
                    text += "#L422##b Shadower #k#l\r\n";
                    text += "#L434##b Dual-Blade #k#l\r\n";
                    text += "#L512##b Buccaneer #k#l\r\n";
                    text += "#L522##b Corsair #k#l\r\n";
                    text += "#L532##b Cannon Master #k#l\r\n";
                    text += "#L1112##b Kain (DW) #k#l\r\n";
                    text += "#L1212##b Kanna (BW) #k#l\r\n";
                    text += "#L1312##b Path Finder (WA) #k#l\r\n";
                    text += "#L1412##b NightWalker (NW) #k#l\r\n";
                    text += "#L1512##b Ark (TB) #k#l\r\n";
                    text += "#L2218##b Evan #k#l\r\n";
                    text += "#L3212##b Battle Mage #k#l\r\n";
                    text += "#L3312##b Wild Hunter #k#l\r\n";
                    text += "#L2312##b Jett Baby #k#l\r\n";
                    text += "#L3512##b Burster#k#l\r\n";
                    text += "#L2112##b Aran#k#l\r\n";
                    cm.sendSimpleS("Select which job you would like to become!\r\n" + text, 16);
                } else {
                    cm.sendOkS("Seems like you dont have enough Crypto Points for the tranformation.!", 16);
                }
            } else {
                cm.sendOkS("You must be at least level 1000 or higher to be able to change jobs!", 16);
            }
        } else if (status == 2) {
            job = selection;
            cm.sendYesNoS("Are you sure you want to change jobs?\r\n#rAll skills set to macro and keybinds will be reset#k.", 16);
        } else if (status == 3) {
            if (cm.haveItem(4310505, price)) {
                cm.gainItem(4310505, -price);
                cm.getPlayer().switchJob(job);
                cm.sendOkS("You have successfully changed jobs!", 16);
            } else {
                cm.sendOkS("Seems like you dont have enough Meso Bags for the tranformation.!", 16);
            }
        } else {
            cm.dispose();
        }
    }
}


