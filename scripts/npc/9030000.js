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
        if (cm.haveItem(2583000, 1)) {
            text += "#L0##i2583000##l";
        }
        if (cm.haveItem(2583001, 1)) {
            text += "#L1##i2583001##l";
        }
        if (cm.haveItem(2583007, 1)) {
            text += "#L2##i2583007##l";
        }
        if (cm.haveItem(2583005, 1)) {
            text += "#L3##i2583005##l";
        }
        if (cm.haveItem(2583000, 1) || cm.haveItem(2583001, 1) || cm.haveItem(2583007, 1) || cm.haveItem(2583005, 1)) {
            cm.sendSimple("Welcome to the Master Cubing System. Pick a Cube you like to use?\r\n\#rCash items cannot be cubed.#k\r\n\r\n" + text);
        } else {
            cm.sendOk("You currently do not have any cubes.");
        }
    } else if (status == 1) {
        switch (selection) {
            case 0:
                cube = 2583000;
                break;
            case 1:
                cube = 2583001;
                break;
            case 2:
                cube = 2583007;
                break;
            case 3:
                cube = 2583005;
                break;
        }
        if (cm.haveItem(cube, cost)) {
            equiplist = cm.getPlayer().getEquipNoCash();
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
                cm.sendOk("You currently do not have any Non-Cash Equips to cube.");
            }
        } else {
            cm.sendOk("You currently do not have any #i" + cube + "#.");

        }
    } else if (status == 2) {
        if (cm.haveItem(cube)) {
            if (equip == null) {
                equip = equiplist.get(selection);
            }
            if (equip != null) {
                cm.gainItem(cube, -cost);
                cm.cube(equip, cube);
                if (cm.haveItem(cube, cost)) {
                    if (equip.getPotential5() > 0) {
                        cm.sendYesNo("Would you like to Cube " + cm.getItemName(equip.getItemId()) + "?\r\n\Current Potentials:\r\n\#e#d     " + cm.resolvePotentialID(equip, equip.getPotential1()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential2()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential3()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential4()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential5()));
                    } else if (equip.getPotential4() > 0) {
                        cm.sendYesNo("Would you like to Cube " + cm.getItemName(equip.getItemId()) + "?\r\n\Current Potentials:\r\n\#e#d     " + cm.resolvePotentialID(equip, equip.getPotential1()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential2()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential3()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential4()));
                    } else if (equip.getPotential3() > 0) {
                        cm.sendYesNo("Would you like to Cube " + cm.getItemName(equip.getItemId()) + "?\r\n\Current Potentials:\r\n\#e#d     " + cm.resolvePotentialID(equip, equip.getPotential1()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential2()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential3()));
                    } else if (equip.getPotential2() > 0) {
                        cm.sendYesNo("Would you like to Cube " + cm.getItemName(equip.getItemId()) + "?\r\n\Current Potentials:\r\n\#e#d     " + cm.resolvePotentialID(equip, equip.getPotential1()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential2()));
                    } else if (equip.getPotential1() > 0) {
                        cm.sendYesNo("Would you like to Cube " + cm.getItemName(equip.getItemId()) + "?\r\n\Current Potentials:\r\n\#e#d     " + cm.resolvePotentialID(equip, equip.getPotential1()));
                    }
                } else {
                    if (equip.getPotential5() > 0) {
                        cm.sendOk(cm.getItemName(equip.getItemId()) + "?\r\n\Current Potentials:\r\n\#e#d     " + cm.resolvePotentialID(equip, equip.getPotential1()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential2()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential3()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential4()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential5()));
                    } else if (equip.getPotential4() > 0) {
                        cm.sendOk(cm.getItemName(equip.getItemId()) + "?\r\n\Current Potentials:\r\n\#e#d     " + cm.resolvePotentialID(equip, equip.getPotential1()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential2()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential3()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential4()));
                    } else if (equip.getPotential3() > 0) {
                        cm.sendOk(cm.getItemName(equip.getItemId()) + "?\r\n\Current Potentials:\r\n\#e#d     " + cm.resolvePotentialID(equip, equip.getPotential1()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential2()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential3()));
                    } else if (equip.getPotential2() > 0) {
                        cm.sendOk(cm.getItemName(equip.getItemId()) + "?\r\n\Current Potentials:\r\n\#e#d     " + cm.resolvePotentialID(equip, equip.getPotential1()) + "\r\n\     " + cm.resolvePotentialID(equip, equip.getPotential2()));
                    } else if (equip.getPotential1() > 0) {
                        cm.sendOk(cm.getItemName(equip.getItemId()) + "?\r\n\Current Potentials:\r\n\#e#d     " + cm.resolvePotentialID(equip, equip.getPotential1()));
                    }
                }
            } else {
                cm.sendOk("invalid equip error pls report..");
            }
        } else {
            cm.sendOk("You currently do not have any Platium cubes.");
        }
    } else if (status == 3) {
        status = 3;
        action(0, 0, 0);
    }
}


