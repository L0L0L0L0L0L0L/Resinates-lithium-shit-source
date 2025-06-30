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
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var cost = 0;
var slotcount = 0;
var cube = 4310502;
var slots;
var slot = 0;
var pots = 0;

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
        if (cm.haveItem(cube)) {
            cm.sendSimple("Which potential would you to cube with?\r\n\I only accept #i" + cube + "#. Cost per cube is " + cost + " Mesos\r\n\ #L1# Meso Potentials#l\r\n\ #L2# Exp Potentials#l\r\n\ #L3# Drop Rate Potentials#l\r\n\ #L4# Overpower Potentials#l\r\n\ #L5# IED Potentials#l\r\n\ #L6# Boss Damage Potentials#l\r\n\ #L7# Total Damage Potentials#l\r\n\ #L8# All Stat Potentials#l\r\n\ #L0# All Potentials#l\r\n\ ");
        } else {
            cm.sendOk("You currently do not have any #i" + cube + "#.");
        }
    } else if (status == 1) {
        pots = selection;
        if (cm.haveItem(cube)) {
            equiplist = cm.getPlayer().getEquipItems();
            if (!equiplist.isEmpty()) {
                var selStr = "";
                for (var i = 0; i < equiplist.size(); i++) {
                    var curEquip = equiplist.get(i);
                    if (curEquip != null) {
                        selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + "#l\r\n\ ";
                    } else {
                        break;
                    }
                }
                cm.sendSimple("Which equip would you like to Cube?\r\n\I only accept #i" + cube + "#. Cost per cube is " + cost + " Mesos\r\n\ " + selStr);
            } else {
                cm.sendOk("You currently do not have any Equips to cube.");
            }
        } else {
            cm.sendOk("You currently do not have any #i" + cube + "#.");

        }
    } else if (status == 2) {
        if (cm.haveItem(cube)) {
            equip = equiplist.get(selection);
            if (equip != null) {
                var selStr = "";
                for (var i = 1; i < 6; i++) {
                    if (equip.getPotential(i) != 0) {
                        selStr += "#L" + i + "##e#dLine: " + i + " - " + cm.resolvePotentialID(equip, equip.getPotential(i)) + " #l\r\n\r\n\ ";
                    }
                }
                cm.sendSimple("Which potential would you like to Change?\r\n\ " + selStr);
            }
        } else {
            cm.sendOk("You currently do not have any #i" + cube + "#.");

        }
    } else if (status == 3) {
        if (cm.haveItem(cube)) {
            if (equip != null) {
                if (slot == 0) {
                    slot = selection;
                }
                if (equip.getPotential(slot) != 0) {
                    cm.gainItem(cube, -1);
                    cm.singleCube(equip, slot, pots);

                    var selStr = "";
                    for (var i = 1; i < 6; i++) {
                        if (i == slot) {
                            if (equip.getPotential(i) != 0) {
                                selStr += "   #e#rLine: " + i + " - " + cm.resolvePotentialID(equip, equip.getPotential(i)) + " #l\r\n\ ";
                            }
                        } else {
                            if (equip.getPotential(i) != 0) {
                                selStr += "   #e#dLine: " + i + " - " + cm.resolvePotentialID(equip, equip.getPotential(i)) + " #l\r\n\ ";
                            }
                        }
                    }
                    cm.sendYesNo("Do you want to change the potential again?\r\n\r\n\ " + selStr);
                }
            } else {
                cm.sendOk("invalid equip error pls report..");

            }
        } else {
            cm.sendOk("You currently do not have any #i" + cube + "#.");

        }
    } else if (status == 4) {
        status = 4;
        action(0, 0, 0);
    }
}


