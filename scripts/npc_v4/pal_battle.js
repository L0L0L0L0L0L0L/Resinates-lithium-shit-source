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
var questtime = 43200;//30 min
//3600 = 1 hour
var job = "thieves";
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var t;
var battle = 0;
var level = 0;
var mlvl = 0;

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
        if (cm.getPlayer().getQuestLock(cm.getNpc()) > 0) {
            cm.sendOk("My Pals need to recharge. Come back later.\r\n#b" + cm.secondsToString(cm.getPlayer().getQuestLock(cm.getNpc())) + "#k.");
        } else {
            if (cm.getPlayer().battleLimit()) {
                cm.sendOk("Exceeded current battle limits. Please relog to reset counter.");
                return;
            }
            if (!cm.getPlayer().battle) {
                if (cm.getPlayer().getPalStorage().getActivePals().size() > 0) {
                    t = cm.getTrainer();
                    level = cm.getPlayer().getAvgPalLevel();
                    mlvl = cm.getPlayer().getAccVara("Pal_Level");
                    if (mlvl > 0 && level > mlvl) {
                        level = mlvl;
                    }
                    if (cm.getPlayer().getBattleMode() || level >= 100) {
                        battle = 1;
                        cm.sendNext("Battle Lvl: [#r" + level + "#k]\r\nYou have activated my trap card. Prepare for Battle!!!");
                    } else {
                        var data = "#rMy Battle Stats:#k\r\n";
                        data += "   " + star + "Trainer Level: #b" + t.level() + "#k\r\n";
                        data += "   " + star + "Pal Level: (#b" + t.min_level() + "#k-#r" + t.max_level() + "#k)\r\n";
                        data += "   " + star + "Pal Count: (#b" + t.min_pal() + "#k-#r" + t.max_pal() + "#k)\r\n";
                        data += "   " + star + "Pal IV: #b" + t.iv() + "#k\r\n";
                        data += "Current Battle Count: #r" + cm.getPlayer().getBattleLimit() + "#k\r\n";
                        cm.sendYesNo("Do you want to battle me?\r\n" + data);
                    }
                } else {
                    cm.sendOk("Where is your pals at?");
                }
            } else {
                cm.sendOk("Seems you are still into battle?");
            }
        }
    } else if (status == 1) {
        if (battle == 0) {
            cm.getPlayer().setQuestLock(cm.getNpc(), 3600 + (t.level() * 108));
            cm.startBattle();
        } else {
            cm.getPlayer().setQuestLock(cm.getNpc(), 14400);
            var minP = 1;
            var maxP = 2;
            var iv = 50;
            if (level >= 100) {
                minP = 2;
                maxP = 4;
                iv = 100;
            }
            if (level >= 250) {
                minP = 4;
                maxP = 6;
                iv = 150;
            }
            if (level >= 500) {
                minP = 6;
                maxP = 8;
                iv = 250;
            }
            if (level >= 750) {
                minP = 6;
                maxP = 10;
                iv = 250;
            }
            if (level >= 999) {
                minP = 8;
                maxP = 12;
                iv = 250;
            }
            cm.startSuperBattle(cm.random(1, 36), level, level * 0.9, level * 1.1, minP, maxP, iv, 1.0, true);
        }
        cm.dispose();

    } else if (status == 2) {

    } else {
        cm.dispose();
    }
}


