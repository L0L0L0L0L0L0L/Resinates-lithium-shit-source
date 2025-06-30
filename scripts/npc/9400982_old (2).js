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
var cube = 4310505;
var slots;
var slot = 0;
var safe = 0;
var pscroll = 0;
var usePScroll = 0;
var pot = 0;

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
                text += "#L50024##b25% Total Damage#k#l\r\n";//td
                text += "#L50149##b50% Boss Damage#k#l\r\n";//boss
                text += "#L50224##b25% Drop Rate#k#l\r\n";//drop
                text += "#L50324##b25% Exp Rate#k#l\r\n";//exp
                text += "#L50449##b50% Meso Rate#k#l\r\n";//meso
                text += "#L50549##b50% Overpower#k#l\r\n";//op
                text += "#L50624##b25% Item Drop Power#k#l\r\n";//idp
                text += "#L50749##b250% IED#k#l\r\n";//ied
                text += "#L51024##b50% All Stats#k#l\r\n";//all stat
                text += "#L50904##b5% Damage Resist#k#l\r\n";//resist
                text += "#L51139##b100% Attack Power#k#l\r\n";//resist
                text += "#L51239##b100% Magic Power#k#l\r\n";//resist
                text += "#L51339##b250% Str#k#l\r\n";//resist
                text += "#L51439##b250% Dex#k#l\r\n";//resist
                text += "#L51539##b250% Int#k#l\r\n";//resist
                text += "#L51639##b250% Luk#k#l\r\n";//resist
                cm.sendSimple("Which Potential do you wish to apply?\r\n\r\n" + text);
            } else {
                cm.sendOk("You currently do not have " + cost + "x #i" + cube + "# to single line cube your equips.");
            }
        } else if (status == 1) {
            if (cm.haveItem(cube, cost)) {
                pot = selection;
                selection = 0;
                equiplist = cm.getPlayer().getEquipItems();
                if (!equiplist.isEmpty()) {
                    var selStr = "";
                    for (var i = 0; i < equiplist.size(); i++) {
                        var curEquip = equiplist.get(i);
                        if (curEquip != null) {
                            selStr += "#L" + i + "##i" + curEquip.getItemId() + "# " + cm.getItemName(curEquip.getItemId()) + "#l\r\n\ ";
                        }
                    }
                    cm.sendSimple("Which equip would you like to Single Line Cube?\r\n\I only accept " + cost + "x #i" + cube + "#.\r\n\Rainbow Cubes contains ALL custom Potentials.\r\n\ " + selStr);
                } else {
                    cm.sendOk("You currently do not have any Equips to cube.");
                }
            } else {
                cm.sendOk("You currently do not have " + cost + "x #i" + cube + "# to single line cube your equips.");

            }
        } else if (status == 2) {
            if (cm.haveItem(cube, cost)) {
                if (equip == null) {
                    equip = equiplist.get(selection);
                }
                if (equip != null) {
                    var count = 0;
                    var selStr = "";
                    for (var i = 1; i < 6; i++) {
                        if (equip.getPotential(i) != 0) {
                            selStr += "#L" + i + "##e#dLine: " + i + " - " + cm.resolvePotentialID(equip, equip.getPotential(i)) + " #l\r\n\r\n\ ";
                            count++;
                        }
                    }
                    if (count > 0) {
                        cm.sendSimple("Which potential would you like to Change?\r\n\ " + selStr);
                    } else {
                        cm.sendOk("#r" + cm.getItemName(equip.getItemId()) + " has no lines to cube.");
                    }
                } else {
                    cm.sendOk("Error with equip.");
                }
            } else {
                cm.sendOk("You currently do not have " + cost + "x #i" + cube + "#.");

            }
        } else if (status == 3) {
            if (cm.haveItem(cube, cost)) {
                if (equip != null) {
                    if (slot == 0) {
                        slot = selection;
                    }
                    if (equip.getPotential(slot) != 0) {
                        var selStr = "";
                        cm.gainItem(cube, -cost);
                        if (selection == 1) {
                            equip.setPotential1(pot);
                        }
                        if (selection == 2) {
                            equip.setPotential2(pot);
                        }
                        if (selection == 3) {
                            equip.setPotential3(pot);
                        }
                        if (selection == 4) {
                            equip.setPotential4(pot);
                        }
                        if (selection == 5) {
                            equip.setPotential5(pot);
                        }
                        cm.getPlayer().updateEquipSlot(equip);
                        for (var i = 1; i < 6; i++) {
                            if (i == slot) {
                                if (equip.getPotential(i) != 0) {
                                    selStr += "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2#   #e#rLine: " + i + " - " + cm.resolvePotentialID(equip, equip.getPotential(i)) + " (changed)#l\r\n";
                                }
                            } else {
                                if (equip.getPotential(i) != 0) {
                                    selStr += "#fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn#   #e#bLine: " + i + " - " + cm.resolvePotentialID(equip, equip.getPotential(i)) + " #l\r\n";
                                }
                            }
                        }
                        cm.sendOk(selStr);
                    }
                } else {
                    cm.sendOk("invalid equip error pls report..");
                }
            } else {
                cm.sendOk("You currently do not have " + cost + "x #i" + cube + "#.");
            }
        } else if (status == 4) {
        } else {
        }
    }
}


