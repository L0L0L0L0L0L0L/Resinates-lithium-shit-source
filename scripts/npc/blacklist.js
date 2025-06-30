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
            equiplist = null;
            var text = "Which Blacklist Storage options would you like to use?\r\n";
            text += "#L1##gCheck Blacklist Items#k#l\r\n";
            text += "#L2##bAdd Blacklist Items#k#l\r\n";
            text += "#L3##rRemove Blacklist Items#k#l\r\n";
            text += "#L4##rClear Blacklist Items#k#l\r\n";
            cm.sendSimpleS(text, 2);
        } else if (status == 2) {
            option = selection;
            if (option == 2) {
                equiplist = cm.getPlayer().getItemsByOverflowIds();
            }

            if (option == 1 || option == 3) {
                equiplist = cm.getPlayer().getBlackList();
            }
            if (option == 4) {
                cm.getPlayer().clearBlackList();
                cm.sendOk("All items in blacklist is now cleared.");
                return;
            }
            selection = 0;
            if (!equiplist.isEmpty()) {

                var selStr = "";

                if (option == 1) {
                    if (option == 1) {
                        for (var i = 0; i < equiplist.size(); i++) {
                            selStr += "#i" + equiplist.get(i) + "#";
                        }
                        cm.sendOk("Current Black Listed Items:\r\n" + selStr);
                        return;
                    }
                } else {
                    for (var i = 0; i < equiplist.size(); i++) {
                        var curEquip = equiplist.get(i);
                        selStr += "#L" + i + "##i" + curEquip + "# ";

                    }
                }
                if (selStr != "") {
                    if (option == 3) {
                        cm.sendSimpleS("Which item would you like to remove from blacklist?\r\n\ " + selStr + " ", 2);
                    } else {
                        cm.sendSimpleS("You currently have #b" + cm.getPlayer().getTotalTypes() + "#k Types of items.\r\n\You Currently have of #b" + cm.convertNumber(cm.getPlayer().getTotalItems()) + "#k items in your storage.\r\n\Which item would you like to withdrawl?\r\n\ " + selStr + " ", 2);
                    }
                } else {
                    cm.sendOk("No items found");
                }
            } else {
                if (option == 3) {
                    cm.sendOk("You currently do not have any blacklisted items to view.");
                } else {
                    cm.sendOk("You currently do not have any items to view.");
                }
            }

        } else if (status == 3) {
            
            if (option == 3) {
                var equip = equiplist.get(selection)
                cm.getPlayer().removeBlackList(equip);
                var blacklist = cm.getPlayer().getBlackList();
                if (!blacklist.isEmpty()) {
                    var blist = "";
                    for (var i = 0; i < blacklist.size(); i++) {
                        blist += "#i" + blacklist.get(i) + "#";
                    }
                    cm.sendNext("You have successfully removed #i" + equip + "# from the black list.#k\r\n\r\nCurrent Black Listed Items:\r\n" + blist);
                } else {
                    cm.sendNext("You have successfully removed #i" + equip + "# from the black list.#k");
                }
            } else if (option == 2) {
                equip = equiplist.get(selection);
                cm.getPlayer().addBlackList(equip);
                var blacklist = cm.getPlayer().getBlackList();
                var blist = "";
                for (var i = 0; i < blacklist.size(); i++) {
                    blist += "#i" + blacklist.get(i) + "#";
                }
                cm.sendNext("You have successfully blackisted #i" + equip + "# from going into overflow storage with @storeEtc or @storeUse command.\r\n\r\nCurrent Black Listed Items:\r\n" + blist);
            } else {
                cm.sendGetText("How many #i" + equip + "# do you want to withdrawl?\r\n\You currently have " + cm.convertNumber(cm.getPlayer().getOverflowAmount(equip)) + ".");
            }
        } else if (status == 4) {
            if (option == 2 || option == 3) {
                status = 0;
                action(1, 0, 0);
                return;
            }
        }
    }
}

