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
var leaf = 4310066;
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
var questid = 6801;
var questtime = 60;

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
            if (cm.getPlayer().getQuestLock(questid) > 0) {
                cm.sendOk("Come see me after #b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k \r\n\      to exchange ETC items.\r\n\ Quest ID: " + questid);
            } else {
                equiplist = cm.getPlayer().getOverflowEtc();
                if (!equiplist.isEmpty()) {
                    var selStr = "";
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
                        cm.sendSimpleS("You currently have #b" + cm.getPlayer().getTotalTypes() + "#k Types of items.\r\n\You Currently have of #b" + cm.convertNumber(cm.getPlayer().getTotalItems()) + "#k items in your storage.\r\n\Which item would you like to convert to #bUnleased Coins#k?\r\n\ " + selStr + " ", 2);
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
            }
        } else if (status == 2) {
            equip = equiplist.get(selection);
            if (equip != null && equip != leaf) {
                cm.sendGetText("How many #i" + equip + "# do you want to exchange?\r\n\You currently have #b" + cm.convertNumber(cm.getPlayer().getOverflowAmount(equip)) + "#k.");
            } else {
                cm.sendOk("You cannot convert this item, #rDumbAss#k.");
            }
        } else if (status == 3) {
            amount = cm.getNumber();
            if (equip != null && equip != leaf) {
                if (amount > 0 && cm.getPlayer().getOverflowAmount(equip) >= amount) {
                    cm.sendYesNo("Do you want to confirm you want to exchange:\r\n #b" + amount + " #i" + equip + "##k into #r#i" + leaf + "##k?");
                } else {
                    cm.sendOk("You do not have that many #i" + equip + "#.");

                }
            } else {
                cm.sendOk("You cannot convert this item, #rDumbAss#k.");
            }
        } else if (status == 4) {
            if (equip != null && equip != leaf) {
                if (cm.getPlayer().OverflowExchange(equip, amount, leaf)) {
                    cm.getPlayer().setQuestLock(questid, questtime);
                    cm.sendOk("You do have successfully exchanged\r\n" + amount + " #i" + equip + "# converted into #i" + leaf + "#\r\n#bUnleashed Coins have been placed into overflow!#k");
                } else {
                    cm.sendOk("Something wrong here please report this message to masterke in discord with screenshot of this error - overflow ETC error.");
                }
            } else {
                cm.sendOk("You cannot convert this item, #rDumbAss#k.");
            }
        }
    }
}

