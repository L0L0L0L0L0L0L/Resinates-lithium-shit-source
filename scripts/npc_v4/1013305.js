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
var ticketId = 5062001;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var cost = 1;
var slotcount = 0;
var cube = 2583002;
var slots;
var slot = 0;
var safe = 0;
var pscroll = 0;
var usePScroll = 0;
var pot = 0;
var option = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
    } else {
        if (mode == 0 && type > 0) {
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.haveItem(cube, cost)) {
                var text = "";
                text += "#L0#Check Equips#l\r\n";//td
                text += "#L1#Check Equips (#bSimple#k)#l\r\n";//td
                text += "#L2#Store Equips#l\r\n";//td
                text += "#L3#Withdrawl Equips#l\r\n";//td
                cm.sendSimple("Welcome to Resinate's Shyty Storage Service?\r\n#rWe are not responsable for lost or stolen goods.#k\r\n" + text);
            } else {
                cm.sendOk("You currently do not have " + cost + "x #i" + cube + "# to single line cube your equips.");
            }
        } else if (status == 1) {
            option = selection;
            if (option == 0) {
                equiplist = cm.getPlayer().getStorageEquips();
                if (!equiplist.isEmpty()) {
                    var selStr = "";
                    for (var i = 0; i < equiplist.size(); i++) {
                        var curEquip = equiplist.get(i);
                        if (curEquip != null) {
                            selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + " (#rT: " + curEquip.getPower() + "#k) (#bS: " + curEquip.getUpgradeSlots() + "#k)#l\r\n";
                        }
                    }
                    cm.sendSimple("Which equip would you like to examine?\r\n" + selStr);
                } else {
                    cm.sendOk("You currently do not have any Equips in storage.");
                }
            }
            if (option == 1) {
                equiplist = cm.getPlayer().getStorageEquips();
                if (!equiplist.isEmpty()) {
                    var selStr = "";
                    for (var i = 0; i < equiplist.size(); i++) {
                        var curEquip = equiplist.get(i);
                        if (curEquip != null) {
                            selStr += "#L" + i + "##i" + curEquip.getItemId() + "##l ";
                        }
                    }
                    cm.sendSimple("Which equip would you like to examine?\r\n" + selStr);
                } else {
                    cm.sendOk("You currently do not have any Equips in storage.");
                }
            }
            if (option == 2) {
                equiplist = cm.getPlayer().getEquipItems();
                if (!equiplist.isEmpty()) {
                    var selStr = "";
                    for (var i = 0; i < equiplist.size(); i++) {
                        var curEquip = equiplist.get(i);
                        if (curEquip != null) {
                            selStr += "#L" + i + "##i" + curEquip.getItemId() + "##l ";
                        }
                    }
                    cm.sendSimple("Which equip would you like to examine?\r\n" + selStr);
                } else {
                    cm.sendOk("You currently do not have any Equips in storage.");
                }
            }
        } else if (status == 2) {
            if (option == 0 || option == 1) {
                if (equip == null) {
                    equip = equiplist.get(selection);
                }
                var text = "#i" + equip.getItemId() + "# " + cm.getItemName(equip.getItemId()) + "\r\n";
                text += "Tier: #b" + equip.getPower() + "#k - Stars: #b" + equip.getEnhance() + "#k\r\n";
                text += "Str: #b" + cm.getFullUnitNumber(equip.getTStr()) + "#k - Dex: #b" + cm.getFullUnitNumber(equip.getTDex()) + "#k - Int: #b" + cm.getFullUnitNumber(equip.getTInt()) + "#k - Luk: #b" + cm.getFullUnitNumber(equip.getTLuk()) + "#k\r\n";
                text += "Atk: #b" + cm.getFullUnitNumber(equip.getTAtk()) + "#k - Matk: #b" + cm.getFullUnitNumber(equip.getTMatk()) + "#k\r\n";
                text += "Def: #b" + cm.getFullUnitNumber(equip.getTDef()) + "#k - M-Def: #b" + cm.getFullUnitNumber(equip.getTMdef()) + "#k\r\n";
                if (equip.getOverPower() > 0) {
                    text += "Overpower: #r" + cm.getFullUnitNumber(equip.getOverPower()) + "#k\r\n";
                }
                if (equip.getTotalDamage() > 0) {
                    text += "Mob-Damage: #r" + cm.getFullUnitNumber(equip.getTotalDamage()) + "#k\r\n";
                }
                if (equip.getBossDamage() > 0) {
                    text += "Boss-Damage: #r" + cm.getFullUnitNumber(equip.getBossDamage()) + "#k\r\n";
                }
                if (equip.getIED() > 0) {
                    text += "IED: #r" + cm.getFullUnitNumber(equip.getIED()) + "#k\r\n";
                }
                if (equip.getCritDamage() > 0) {
                    text += "Crit-Damage: #r" + cm.getFullUnitNumber(equip.getCritDamage()) + "#k\r\n";
                }
                if (equip.getAllStat() > 0) {
                    text += "All-Stats: #r" + cm.getFullUnitNumber(equip.getAllStat()) + "#k\r\n";
                }
                text += "Slots: #b" + equip.getUpgradeSlots() + "#k";
                cm.sendOkS(text, 2);
            }
            if (option == 2) {
                //store equip todo
            }
            if (option == 2) {
                //withdrawl equip todo
            }
        } else if (status == 3) {

        } else if (status == 5) {

        }
    }
}