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
var leaf = 4000313;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var scroll = 0;
var scrollslot = 0;
var amount = 0;
var slotcount = 0;
var power = 0;
var equip;
var equips;
var equiplist;
var options = 0;
var itemcount = 0;
var option;
var Buscar;
var map = 0;
function start() {
    status = 0;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
        }
        if (mode == 1) {
            status++;
        } else {
            cm.dispose();
        }
        if (status == 1) {
            //cm.sendOk("No items found");
            map = cm.getPlayer().getMapId();
            if (Buscar == null) {
                if (option != 2) {
                    var text = "Which Listing of Overflow Storage would you like to use?\r\n";
                    text += "#L0#Simple (#bAll#k)#l\r\n";
                    text += "#L3#Simple (#bUse#k)#l\r\n";
                    text += "#L4#Simple (#bEtc#k)#l\r\n";
                    text += "#L1#Detailed Items#l\r\n";
                    text += "#L2#Search in Overflow#l\r\n";
                    cm.sendSimpleS(text, 2);
                } else {
                    cm.sendGetText("What item do you want to search?\r\n\r\n");

                }
            } else {
                cm.sendSimpleS("You will search for the word #e" + Buscar.replace("'", "''") + "#n, how do you want to see it?\r\n\#L0#Simple #l\r\n\#L1#Detailed #l", 2);
            }
        } else if (status == 2) {
            if (map != cm.getPlayer().getMapId()) {
                cm.sendOk("No items found");
                return;
            }
            if (selection >= 0) {
                option = selection;
            }
            if (selection == 0 || selection == 1 || selection == 2 || selection == 5) {
                equiplist = cm.getPlayer().getOverflow();
            }
            if (selection == 3) {
                equiplist = cm.getPlayer().getOverflowUse();
            }
            if (selection == 4) {
                equiplist = cm.getPlayer().getOverflowEtc();
            }
            if (selection == 6 || selection == 8) {
                equiplist = cm.getPlayer().getBlackList();
            }
            if (selection == 7) {
                cm.getPlayer().clearBlackList();
                cm.sendOk("All items in blacklist is now cleared.");
                return;
            }
            if (!equiplist.isEmpty()) {
                if (option == 2) {
                    Buscar = cm.getText();
                    if (Buscar != null) {
                        Buscar = Buscar.replace("'", "''");
                        option = 1;
                    } else {
                        status = status - 2;
                        action(mode, type, selection);
                        return;
                    }
                }
                selection = 0;
                var selStr = "";

                if (option == 6 || option == 8) {
                    for (var i = 0; i < equiplist.size(); i++) {
                        selStr += "#i" + equiplist.get(i) + "#";
                    }
                    cm.sendOk("Current Black Listed Items:\r\n" + selStr);
                    return;
                }

                for (var i = 0; i < equiplist.size(); i++) {
                    var curEquip = equiplist.get(i);
                    var NombreItem = cm.getItemName(curEquip);
                    if (Buscar != null) {
                        if (NombreItem.toUpperCase().indexOf(Buscar.toUpperCase()) == -1) {
                            continue;
                        }
                    }

                    //selStr += "#L" + i + "# "+curEquip+" overflow amount "+equiplist.size()+"  #l\r\n\ ";
                    if (option == 0 || option == 3 || option == 4 || option == 5) {
                        selStr += "#L" + i + "##i" + curEquip + "# ";
                    } else {
                        selStr += "#L" + i + "##i" + curEquip + "# " + NombreItem + " (x#b" + cm.convertNumber(cm.getPlayer().getOverflowAmount(curEquip)) + "#k)#l\r\n";
                    }
                    //selStr += "#L" + i + "##i" + curEquip + "##l\r\n\ ";
                }
                if (selStr != "") {
                    cm.sendSimpleS("You currently have #b" + cm.getPlayer().getTotalTypes() + "#k Types of items.\r\n\You Currently have of #b" + cm.convertNumber(cm.getPlayer().getTotalItems()) + "#k items in your storage.\r\n\Which item would you like to withdrawl?\r\n\ " + selStr + " ", 2);
                } else {
                    cm.sendOk("No items found");
                }
            } else {
                if (option == 6) {
                    cm.sendOk("You currently do not have any blacklisted items to view.");
                } else {
                    cm.sendOk("You currently do not have any items to view.");
                }
            }

        } else if (status == 3) {
            if (map != cm.getPlayer().getMapId()) {
                cm.sendOk("No items found");
                return;
            }
            equip = equiplist.get(selection);
            if (option == 6) {
                cm.getPlayer().addBlackList(equip);
                var blacklist = cm.getPlayer().getBlackList();
                var blist = "";
                for (var i = 0; i < blacklist.size(); i++) {
                    blist += "#i" + blacklist.get(i) + "#";
                }
                selStr += "#i" + curEquip + "#";
                cm.sendOk("You have successfully removed #i" + equip + "# from blackisted.#k\r\n\r\nCurrent Black Listed Items:\r\n" + blist);
                return;
            }
            if (option == 5) {
                cm.getPlayer().addBlackList(equip);
                var blacklist = cm.getPlayer().getBlackList();
                var blist = "";
                for (var i = 0; i < blacklist.size(); i++) {
                    blist += "#i" + blacklist.get(i) + "#";
                }
                selStr += "#i" + curEquip + "#";
                cm.sendOk("You have successfully blackisted #i" + equip + "# from going into overflow storage with @storeEtc or @storeUse command.\r\n\#rRelogging will reset this list#k.\r\n\r\nCurrent Black Listed Items:\r\n" + blist);
            } else {
                cm.sendGetText("How many #i" + equip + "# do you want to withdrawl?\r\n\You currently have " + cm.convertNumber(cm.getPlayer().getOverflowAmount(equip)) + ".");
            }
        } else if (status == 4) {
            if (map != cm.getPlayer().getMapId()) {
                cm.sendOk("No items found");
                return;
            }
            amount = cm.getNumber();
            if (amount > 0 && amount <= 1000000 && cm.getPlayer().getOverflowAmount(equip) >= amount && cm.canHold(equip, amount)) {
                cm.sendYesNo("Do you want to confirm you want to withdrawl " + amount + " #i" + equip + "#?");
            } else {
                cm.sendOk("You do not have that many #i" + equip + "#.");

            }
        } else if (status == 5) {
            if (map != cm.getPlayer().getMapId()) {
                cm.sendOk("No items found");
            } else {
                if (cm.getPlayer().getOverflowAmount(equip) >= amount) {
                    if (cm.canHold(equip, amount)) {
                        if (cm.getPlayer().removeOverflowNpc(equip, amount, " From ETC Storage NPC")) {
                            cm.getPlayer().updateOverflow(equip);
                            cm.sendOk("You do have successfully withdrawn " + amount + " #i" + equip + "# into your inventory.");
                        } else {
                            cm.sendOk("Something wrong here please report this message to masterke in discord with screenshot of this error - overflow ETC error.");
                        }
                    } else {
                        cm.sendOk("You do not have room in your inventory to hold " + amount + " #i" + equip + "#.");
                    }
                } else {
                    cm.sendOk("Fuck off cheater!!!");
                }
            }
        }
    }
}

