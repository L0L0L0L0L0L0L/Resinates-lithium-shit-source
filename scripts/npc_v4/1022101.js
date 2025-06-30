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
var ticketId = 4036572;
var reward = 4310015;
var rewamount = 250;
var items = new Array(4000010, 4000026, 4000031, 4000013, 4000041);
var amount = new Array(1000, 1000, 1000, 1000, 1000);
var exp = 250000;
var questid = 10;
var questtime = 1800;//30 min
var level = 0;
var option = 0;

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
        if (cm.getPlayer().getMapId() == 101000000) {
            cm.sendYesNo("Want to goto place full of white stuff?");
        } else {
            if (cm.getPlayer().getQuestLock(questid) > 0) {
                cm.sendOk("Come see me after, to claim your rewards.\r\n#b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k\r\n");
            } else {
                if (cm.getPlayer().getTotalLevel() >= 200) {
                    if (cm.getPlayer().getQuestStatus() == 0) {
                        if (!cm.generateQuest()) {
                            cm.sendOk("#rError with generating Items#k.");
                            return;
                        }
                    }
                    level = cm.getPlayer().getQuestlevel();
                    var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n\#fUI/UIWindow.img/Quest/basic#\r\n";
                    rewards += " #i4310018# " + cm.getItemName(4310018) + " (x#b" + Math.floor(level * 2.5) + "#k)\r\n";
                    rewards += "#i2000012# " + cm.getItemName(2000012) + " (x#b5#k)\r\n";
                    rewards += "#i4420015# " + cm.getItemName(4420015) + " (#b" + Math.floor(cm.getPlayer().getTotalLevel() / 100.0) + "x#k)\r\n";
                    var chance = 100 / ((1.0 * (5000.0 / cm.getPlayer().getTotalLevel())) * 100);
                    rewards += "#i4420007# " + cm.getItemName(4420007) + " (#b" + Number(chance).toFixed(2) + "%#k)\r\n";
                    wanted = cm.getPlayer().getVar("rooney");
                    if (wanted > 0) {
                        rewards += "#fUI/UIWindow2.img/Quest/icon/icon0# #rCompleted Rooney Quests: " + wanted + "#k\r\n\ ";
                    }
                    if (cm.getPlayer().isGM()) {
                        option = 1;
                        cm.sendYesNo("I see you have collected the following items:\r\n\r\n" + cm.getQuestPool() + "\r\n\r\n" + rewards);
                    } else {
                        if (cm.hasQuestItems()) {
                            option = 1;
                            cm.sendYesNo("I see you have collected the following items:\r\n\r\n" + cm.getQuestPool() + "\r\n\r\n" + rewards);
                        } else {
                            cm.sendOk("Bring me the following items and I will reward you.\r\n\#rThis quest can be repeated as many times as you want.#k\r\n#bQuest Level#k: " + level + "\r\n\r\n" + cm.getQuestPool() + "\r\n\r\n" + rewards);
                        }
                    }
                } else {
                    cm.sendOk("Must be at least level 200 in order to take on wanted monsters.");
                }
            }
        }
    } else if (status == 1) {
        if (cm.getPlayer().getMapId() == 101000000) {
            cm.warp(209000000);
        } else {
            if (option == 1) {
                if (cm.getPlayer().isGM() || cm.completeQuest()) {
                    var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n\#fUI/UIWindow.img/Quest/basic#\r\n";
                    rewards += "#i4310018# " + cm.getItemName(4310018) + " (x#b" + Math.floor(level * 2.5) + "#k)\r\n";
                    cm.gainItem(4310018, Math.floor(level * 2.5));
                    rewards += "#i4420015# " + cm.getItemName(4420015) + " (#b" + Math.floor(cm.getPlayer().getTotalLevel() / 200) + "x#k)\r\n";
                    cm.gainItem(4420015, Math.floor(cm.getPlayer().getTotalLevel() / 100.0));

                    rewards += "#i2000012# " + cm.getItemName(2000012) + " (x#b5#k)\r\n";
                    cm.gainItem(2000012, 5);
                    var chance = parseInt((5000.0 / cm.getPlayer().getTotalLevel()) * 100.0);
                    if (cm.random(1, chance) == 1) {
                        rewards += "#i4420007# " + cm.getItemName(4420007) + "\r\n";
                        cm.gainItem(4420007, 1);
                    }
                    cm.getPlayer().gainStamina(100);
                    if (level >= 250) {
                        cm.getPlayer().addVar("rooney", 1);
                        wanted = cm.getPlayer().getVar("rooney");
                        if (wanted >= 1) {
                            cm.getPlayer().finishAchievement(2110);
                        }
                        if (wanted >= 5) {
                            cm.getPlayer().finishAchievement(2111);
                        }
                        if (wanted >= 10) {
                            cm.getPlayer().finishAchievement(2112);
                        }
                        if (wanted >= 15) {
                            cm.getPlayer().finishAchievement(2113);
                        }
                        if (wanted >= 25) {
                            cm.getPlayer().finishAchievement(2114);
                        }
                        if (wanted >= 50) {
                            cm.getPlayer().finishAchievement(2115);
                        }
                        if (wanted >= 75) {
                            cm.getPlayer().finishAchievement(2116);
                        }
                        if (wanted >= 100) {
                            cm.getPlayer().finishAchievement(2117);
                        }
                    }
                    cm.getPlayer().gainStat(1, 10);
                    cm.getPlayer().setQuestLock(questid, questtime);
                    cm.sendOk(rewards + "\r\n\r\nYou can exchange these emblems in Free Market at Inkwell. Come back and see me for another quest.");
                } else {
                    cm.sendOk("You currently missing some quest items.");
                }
            }
            if (option == 2) {
                cm.gainItem(4034705, -1);
                cm.resetQuest();
                if (cm.generateQuest()) {
                    level = cm.getPlayer().getQuestlevel();
                    var rewards = "#fUI/UIWindow.img/Quest/reward#\r\n\#fUI/UIWindow.img/Quest/basic#\r\n";
                    rewards += " #i4310015# " + cm.getItemName(4310015) + " (x#b" + (level * 5) + "#k)\r\n";
                    rewards += "#i4310018# " + cm.getItemName(4310018) + " (x#b" + level + "#k)\r\n";
                    rewards += "#i2000012# " + cm.getItemName(2000012) + " (x#b5#k)\r\n";
                    rewards += "#i4420015# " + cm.getItemName(4420015) + " (#b" + Math.floor(cm.getPlayer().getTotalLevel() / 200) + "x#k)\r\n";
                    var chance = 100 / ((1.0 * (5000.0 / cm.getPlayer().getTotalLevel())) * 100);
                    rewards += "#i4420007# " + cm.getItemName(4420007) + " (#b" + Number(chance).toFixed(2) + "%#k)\r\n";
                    wanted = cm.getPlayer().getVar("rooney");
                    if (wanted > 0) {
                        rewards += "#fUI/UIWindow2.img/Quest/icon/icon0# #rCompleted Rooney Quests: " + wanted + "#k\r\n\ ";
                    }
                    cm.sendOk("Bring me the following items and I will reward you.\r\n\#rThis quest can be repeated as many times as you want.#k\r\n#bQuest Level#k: " + level + "\r\n\r\n" + cm.getQuestPool() + "\r\n\r\n" + rewards);
                }
            }
        }
        cm.dispose();
    } else {
        cm.dispose();
    }
}


