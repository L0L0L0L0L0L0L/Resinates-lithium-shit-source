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
var status = 0;
var ticketId = 5062002;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var cost = 1;
var slotcount = 0;
var cube;
var scroll;
var count = 0;
var min = 0;
var max = 0;
var chance = 0;
var safe = 0;
var pscroll = 0;
var tier = 0;
var success = 0;

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
        var text = "";
        text += "#L3# #i2586002# = #bRandom Bonus Stats: +1-1% #k#l\r\n";
        text += "#L4# #i2586003# = #bRandom Bonus Stats: +1-2% #k#l\r\n";
        text += "#L5# #i2586004# = #bRandom Bonus Stats: +1-3% #k#l\r\n";
        text += "#L6# #i2586005# = #bRandom Bonus Stats: +1-5% #k#l\r\n ";
        cm.sendSimple("Welcome traveler! If you happened to stumble on some Gems, I can upgrade you gear with them.\r\n#rEach Gem will consume an equip's upgrade slot!#k\r\n" + text);
    } else if (status == 1) {
        chance = 0;
        if (selection == 3) {
            chance = 3;
            cube = 2586002;
            tier = 1;
        } else if (selection == 4) {
            chance = 4;
            cube = 2586003;
            tier = 2;
        } else if (selection == 5) {
            chance = 5;
            cube = 2586004;
            tier = 3;
        } else if (selection == 6) {
            chance = 5;
            cube = 2586005;
            tier = 5;
        }
        equiplist = cm.getPlayer().getEquipItems();
        if (!equiplist.isEmpty()) {
            var selStr = "";
            for (var i = 0; i < equiplist.size(); i++) {
                var curEquip = equiplist.get(i);
                if (curEquip != null && curEquip.canSoul() && curEquip.getUpgradeSlots() > 0) {
                    count += 1;
                    selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + "#l\r\n\ ";
                }
            }
            if (count > 0) {
                cm.sendSimple("Which equip would you like to apply the Soul Gem to?\r\n\I only accept #i" + cube + "#\r\n\ " + selStr);
            } else {
                cm.sendOk("You currently do not have any Equips to Gem.");
            }
        } else {
            cm.sendOk("You currently do not have any Equips to Gem.");
        }
    } else if (status == 2) {
        if (equip == null) {
            equip = equiplist.get(selection);
        }
        cm.sendGetTextS("How many #i" + cube + "# would you like me to apply to this Equip?\r\n#rMax Gems that can be used is 30000#k", 16);
    } else if (status == 3) {
        cost = cm.getNumber();
        if (cost > 0 && cost <= 30000) {
            if (cm.haveItem(cube, cost)) {
                cm.sendYesNoS("ARe you sure you wish to apply " + cost + " #i" + cube + "# to \r\n#i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + "?", 16);
            } else {
                cm.sendOkS("You currently do not have enough #i" + cube + "#", 16);
            }
        } else {
            cm.sendOkS("You currently do not have enough slots on your equip for this many gems", 16);
        }
    } else if (status == 4) {
        if (cm.haveItem(cube, cost)) {
            if (equip != null) {
                var OP = 0, TD = 0, BD = 0, IED = 0, CD = 0, AS = 0;
                if (equip.getOverPower() > 0) {
                    OP = equip.getOverPower();
                }
                if (equip.getTotalDamage() > 0) {
                    TD = equip.getTotalDamage();
                }
                if (equip.getBossDamage() > 0) {
                    BD = equip.getBossDamage();
                }
                if (equip.getIED() > 0) {
                    IED = equip.getIED();
                }
                if (equip.getCritDamage() > 0) {
                    CD = equip.getCritDamage();
                }
                if (equip.getAllStat() > 0) {
                    AS = equip.getAllStat();
                }
                var used = cm.getPlayer().soulUpgrade(equip, tier, cost);
                cm.gainItem(cube, -used);
                var stats = "#b" + cm.getItemName(equip.getItemId()) + "#k\r\n";
                if (equip.getOverPower() > 0) {
                    var value = equip.getOverPower() - OP;
                    var text = value > 0 ? "   #g(+" + value + "%)" : "";
                    stats += "Overpower: #b" + equip.getOverPower() + "%" + text + "#k\r\n";
                }
                if (equip.getTotalDamage() > 0) {
                    var value = equip.getTotalDamage() - TD;
                    var text = value > 0 ? "   #g(+" + value + "%)" : "";
                    stats += "Total Damage: #b" + equip.getTotalDamage() + "%" + text + "#k\r\n";
                }
                if (equip.getBossDamage() > 0) {
                    var value = equip.getBossDamage() - BD;
                    var text = value > 0 ? "   #g(+" + value + "%)" : "";
                    stats += "Boss Damage: #b" + equip.getBossDamage() + "%" + text + "#k\r\n";
                }
                if (equip.getIED() > 0) {
                    var value = equip.getIED() - IED;
                    var text = value > 0 ? "   #g(+" + value + "%)" : "";
                    stats += "Ignore Defense: #b" + equip.getIED() + "%" + text + "#k\r\n";
                }
                if (equip.getCritDamage() > 0) {
                    var value = equip.getCritDamage() - CD;
                    var text = value > 0 ? "   #g(+" + value + "%)" : "";
                    stats += "Critical Damage: #b" + equip.getCritDamage() + "%" + text + "#k\r\n";
                }
                if (equip.getAllStat() > 0) {
                    var value = equip.getAllStat() - AS;
                    var text = value > 0 ? "   #g(+" + value + "%)" : "";
                    stats += "All Stats: #b" + equip.getAllStat() + "%" + text + "#k\r\n";
                }
                stats += "You have consumed #b" + used + " #i" + cube + "##k\r\n";
                if (equip.getUpgradeSlots() > 0) {
                    stats += "Remaining Slots: #b" + equip.getUpgradeSlots() + "#k\r\n";
                } else {
                    stats += "#rNo remaining slots left#k\r\n";
                }
                cm.sendOk(stats);
            } else {
                cm.sendOk("invalid equip error pls report..");
            }
        } else {
            cm.sendOk("You currently do not have enough Materials to Soul Gem. Requires #i" + cube + "#");
        }
    }
}


