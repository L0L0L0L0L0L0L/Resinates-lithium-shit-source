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
var ticketId = 4310066;
var reward = 4420015;
var rewamount = 5;
var items = new Array(4310066);
var amount = new Array(25000);
var exp = 250000;
var questid = 99999;
var questtime = 28800;//30 min
var job = "thieves";
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var pals;
var playerPals;
var spal;
var evo;
var pppal;

function start() {
    status = -1;
    action(1, 0, 0);
}

function getPalIcon(id) {
    return "#fUI/Custom.img/shared/palHead/" + id + "#";
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 0) {
        playerPals = cm.getPlayer().getPalStorage().getActivePals();
        if (playerPals.size() > 0) {
            var selStr = "";
            for (var i = 0; i < playerPals.size(); i++) {
                var cpal = playerPals.get(i);
                if (cpal != null) {
                    selStr += "#L" + i + "# #fUI/Custom.img/shared/element/" + cpal.getElement() + "# #b" + cpal.getName() + "#k #l\r\n\ ";
                }
            }
            cm.sendSimpleS("Which Pal would you like to #bSplice#k?\r\n\ " + selStr, 16);
        } else {
            cm.sendOkS("You dont have any active pals", 16);
        }
    }

    if (status == 1) {
        spal = playerPals.get(selection);
        evo = spal.getEvo();
        cm.sendYesNoS("Are you sure you want to splice #b" + spal.getName() + "#k (#rTier: " + evo + "#k)?", 16);
    }

    if (status == 2) {
        var selStr = "Select a Pal Tier You wish to splice from:\r\n";
        selStr += "#L1#Tier 1 (#bCommon#k)#l\r\n";
        if (evo >= 2) {
            selStr += "#L2#Tier 2 (#bRare#k)#l\r\n";
        }
        if (evo >= 3) {
            selStr += "#L3#Tier 3 (#bEpic#k)#l\r\n";
        }
        if (evo >= 4) {
            selStr += "#L4#Tier 4 (#bLegendary#k)#l\r\n";
        }
        if (evo >= 5) {
            selStr += "#L5#Tier 5 (#bMega#k)#l\r\n";
        }
        cm.sendSimpleS(selStr, 16);
    }
    if (status == 3) {
        pals = cm.getPalsEvo(selection);
        var text = "";
        for (var i = 0; i < pals.size(); i++) {
            var p = pals.get(i);
            text += "#L" + p + "#" + getPalIcon(p) + "#l       ";
        }
        cm.sendSimpleS("Select a Maple Pal:\r\n" + text, 16);
    }
    if (status == 4) {
        ppal = selection;
        cm.sendYesNoS("Are you sure you want to splice this Pal?\r\n\r\n " + getPalIcon(spal.getModel()) + "#fUI/Custom.img/shared/rightArrow#" + getPalIcon(ppal) + "  ", 16);
    }
    if (status == 5) {
        if (cm.checkPal(ppal, evo)) {
            if (cm.haveItem(4202048, 1)) {
                cm.gainItem(4202048, -1);
                spal.setModel(ppal);
                spal.save();
                cm.sendOkS("You Pal has been successfully spliced", 16);
            } else {
                cm.sendOkS("Splicing requires #i4202048#....", 16);
            }
        } else {
            cm.systemMsg("Player: " + cm.getPlayer().getName() + " is hacking selection on npcs");
            cm.sendOkS("Wrong choice....", 16);
        }

    }
    //cm.dispose();
}


