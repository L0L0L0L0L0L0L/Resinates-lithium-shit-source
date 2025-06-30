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

var bosses = new Array("Black Bean", "Von Leon", "Empress", "Damien", "Magnus", "Lotus", "Lucid", "Will", "Hilla", "Darknell", "Crazy Will", "Ramuramu", "Atilla", "Aragami", "Butcher", "Asura", "Khan", "Riverson", "Capo", "Kobold", "Tree", "Horseman", "Slime", "Seren", "Kalos", "Kaling", "Lotus Hard", "Limbo", "Limbo Hard", "Grand Ape");
var jobs = new Array("Any Warrior Job", "Any Mage Job", "Any Pirate Job", "Demon Slayer Job", "Mechanic Job", "Any Thief Job", "Ark (Thunder Breaker) Job", "Path Finder (Wind Archer) Job", "Kain (Dawn Warrior) Job", "Any Job");
//var ems = new Array("Von Leon", );
var tiers = new Array(10, 12, 14, 16, 18, 20, 22, 24, 27, 30, 33, 35, 37, 40, 42, 45, 47, 50, 52, 55, 58, 60, 65, 70, 75, 80, 85, 90, 95, 99);
var cost = new Array(1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 6, 6, 7, 7, 8, 9, 9, 10);
var level = new Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 17, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 99);
var ach = new Array(51, 39, 70, 66, 65, 64, 50, 69, 180, 181, 400, 401, 402, 403, 404, 405, 406, 407, 408, 157, 158);
var status;
var battle = 0;
var lap = 0;
var trials = 0;
var triallevel = 0;
var emi;
var allowed = false;
var tier = 0;
var slevel = 0;
var item = 4000435;
var status = 0;
var password = 0;
var boost = 0;
var password = 0;
var stam = 10;
var star = "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";

function start() {
    if (cm.getPlayer().getBotCheck()) {
        cm.sendOk("You alrdy cleared the check");
        return;
    }
    var number1 = cm.random(1, 9);
    var number2 = cm.random(1, 9);
    var number3 = cm.random(1, 9);
    var number4 = cm.random(1, 9);
    password = cm.getCode(number1, number2, number3, number4);
    cm.sendGetText("#rWhen you see a Gacha Code like this, it means botting inside these instances or event is not allowed.#k\r\nFailing the code does not cause any harm but to your ego.\r\n\Please enter the 4 digit #rAnti-Bot#k Code seen below:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "");

}

function action(mode, type, selection) {
    if (mode < 0) {
    } else {
        if (mode == 0 && type > 0) {
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 1) {
            amount = cm.getNumber();
            if (amount == password) {
                cm.getPlayer().setBotCheck(true);
                var eim = cm.getPlayer().getEventInstance();
                if (eim != null) {
                    eim.checkBots(cm.getPlayer().getMap());
                }
                cm.dispose();
            } else {
                cm.sendOk("Wrong password.");
            }
        }
    }
}