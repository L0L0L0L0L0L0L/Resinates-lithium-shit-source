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
var items = new Array(4001087, 4001088, 4001089, 4001090, 4001091);
var ivs = new Array("Hp", "Str", "Dex", "Int", "Luk", "Attack", "Defense", "Magic Attack", "Magic Defense");
var park = new Array("Meadow");
var dung = new Array("Forest");
var deep_dung = new Array("Forest");
var amount = 99;
var reward = 4001063;
var rewamount = 10;
var exp = 250000;
var questid = 6152;
var questtime = 1800;//60 = 1 min
var job = "thieves";
var option = 0;
var option2 = 0;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var bigstar = " #fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
var pals;
var pal;
var acc = 0;
var energy = 0;
var eAmount = 0;
var upgrade = 0;
var uAmount = 0;
var exp = 0;
var IV = 0;
var scroll = 0;
var em;
var e;

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
    if (status == 0) {//-------------------------------------------------------------------------------------------
        e = "Pal_Ball_2_" + cm.getPlayer().getMapId();
        if (cm.getPlayer().timeClear(e) > 0) {
            cm.sendOk("I have to wait #b" + cm.secondsToString(cm.getPlayer().timeClear(e)) + "#k \r\n\ to repeat my battle here again.");
        } else {
            cm.sendYesNo("I have found a special Ball Battle, Do I accept this Challenge?");
        }
    } else if (status == 1) {//-------------------------------------------------------------------------------------------
        em = cm.getEventManager("Pal");
        if (em != null) {
            if (em.getEligibleParty(cm.getPlayer(), 10, 1, 1)) {
                var element = cm.random(0, 8);
                var evo = 1;
                if (!em.startPlayerInstance(cm.getPlayer(), element, evo)) {
                    cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                } else {
                    cm.getPlayer().setVar(e, cm.getPlayer().getCurrentTime() + (questtime * 1000));
                }
            } else {
                cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map.");
            }
        } else {
            cm.sendOk("Event is bugged contact support. " + h);
        }
    } else if (status == 2) {//-------------------------------------------------------------------------------------------

    } else if (status == 3) {//-------------------------------------------------------------------------------------------

        //cm.sendOk("Event Option: " + option);
    } else {
        cm.dispose();
    }
}


