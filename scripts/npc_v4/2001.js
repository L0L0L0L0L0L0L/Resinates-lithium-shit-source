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
/* 9000021 - Gaga
 BossRushPQ recruiter
 @author Ronan
 */

var status;
var level = 250;
var dp = 4310502;
var amount = 25;
var letter = 4009180;

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
        if (!cm.getPlayer().achievementFinished(904)) {
            if (cm.getPlayer().countItem(4000019) >= 50 && cm.getPlayer().countItem(4000000) >= 25) {
                cm.sendYesNo("OooooooooooOooOOOOooo, You brought me sooo many snail shells, Gimme Gimme!!??");
            } else {
                cm.sendOk("Bring me 50 #i4000019# and 25 #i4000000#.");
            }
        } else {
            if (cm.getPlayer().getStamina() >= 5) {
                cm.sendYesNo("Are you ready for your final test?");
            } else {
                cm.sendOk("Seems your too tired to challenge the boss. You need #r5 Stamina#.");
            }
        }
    } else if (status == 1) {
        if (!cm.getPlayer().achievementFinished(904)) {
            if (cm.getPlayer().countItem(4000019) >= 50 && cm.getPlayer().countItem(4000000) >= 25) {
                cm.gainItem(4000019, -50);
                cm.gainItem(4000000, -25);
                cm.gainItem(2049301, 25);
                cm.getPlayer().finishAchievement(904);
                cm.sendOk("Hehehehe Thanks sooo much, I love you Daddy!\r\nUse these scrolls to upgrade your gear to take on the final challenge.");
            } else {
                cm.sendOk("Bring me 50 #i4000019# and 24 #i4000000#.");
            }
        } else {
            if (cm.getPlayer().getStat().getStarForce() >= 50) {
                if (cm.getPlayer().countItem(4001237) > 0) {
                    cm.gainItem(4001237, -1);
                    cm.gainItem(4420002, 1);
                    cm.gainItem(2049302, 50);
                    cm.getPlayer().finishAchievement(905);
                    cm.sendOk("Ugh.. Here take this reward.");
                } else {
                    if (cm.getPlayer().getTotalLevel() < 10) {
                        cm.sendOk("You need to train to level 10 before you can challenge my boss.");
                    } else {
                        var em = cm.getEventManager("tuto");
                        if (em != null) {
                            if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getTotalLevel())) {
                                cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                            }
                        } else {
                            cm.sendOk("Event has already started, Please wait.");
                        }
                    }
                }
            } else {
                var selStr = "I see you want to take on the boss and get out of here.\r\n";
                selStr += "But, it seems that you are not strong enough to take on said boss,\r\n";
                selStr += "Next to me is an npc that can help you enhance your equips.\r\n";
                selStr += "Upgrade your equips with Enhance Scrolls that we gave you.\r\n";
                selStr += "Come talk to me once you have accumulated a total 50 stars spread across all your equips.\r\n";
                cm.sendOk(selStr);
            }
        }
    }
}