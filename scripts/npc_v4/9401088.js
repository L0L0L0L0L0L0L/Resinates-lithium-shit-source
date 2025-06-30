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
        if (cm.getPlayer().battleLimit()) {
            cm.sendOk("Exceeded current battle limits. Please relog to reset counter.");
            return;
        }
        if (cm.getPlayer().getPalStorage().getActivePals().size() > 0) {
            cm.sendYesNoS("Do you want to start a Pal Battle?", 2);
        } else {
            cm.sendOk("Speak with Oak on Battle Teams.");
        }
    } else if (status == 1) {
        //startSuperBattle(int bg, int level, int min_level, int max_level, int min_pal, int max_pal, int iv, double multi, boolean rewards)
        cm.startSuperBattle(40, 40, 40, 40, 20, 20, 250, 5.0, false);
        //cm.startRandomBattle(cm.getPlayer().getAvgPalLevel(), false);
        cm.dispose();

    } else if (status == 2) {

    } else {
        cm.dispose();
    }
}


