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

var bosses = new Array("Damien", "Magnus", "Lotus", "Lucid", "Will", "Hilla", "Darknell", "Crazy Will");
var jobs = new Array("Any Warrior Job", "Any Mage Job", "Any Pirate Job", "Demon Slayer Job", "Mechanic Job", "Any Thief Job", "Ark (Thunder Breaker) Job", "Path Finder (Wind Archer) Job", "Kain (Dawn Warrior) Job", "Any Job");
//var ems = new Array("Von Leon", );
var status;
var battle = 0;
var lap = 0;
var trials = 0;
var triallevel = 0;

var allowed = false;

var item = 4310505;
var status = 0;
var password = 0;
var boost = 0;

var emi;
var level = 0;
var rlevel = 0;
var tier = 0;
var price = 0;
var option = 0;

function start() {
    var text = "Which Achivement would you like to unlock?\r\n\#rEach Achivement Costs #rInfinite Points# to unlock.#k\r\n";
    text += "#L0# " + (!cm.getPlayer().achievementFinished(39) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(39).getName() + "#k (#rPrice: 1#k)#l\r\n";
    text += "#L1# " + (!cm.getPlayer().achievementFinished(60) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(60).getName() + "#k (#rPrice: 1#k)#l\r\n";
    text += "#L2# " + (!cm.getPlayer().achievementFinished(61) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(61).getName() + "#k (#rPrice: 1#k)#l\r\n";
    text += "#L3# " + (!cm.getPlayer().achievementFinished(52) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(52).getName() + "#k (#rPrice: 1#k)#l\r\n";
    text += "#L4# " + (!cm.getPlayer().achievementFinished(70) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(70).getName() + "#k (#rPrice: 1#k)#l\r\n";
    text += "#L5# " + (!cm.getPlayer().achievementFinished(66) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(66).getName() + "#k (#rPrice: 1#k)#l\r\n";
    text += "#L6# " + (!cm.getPlayer().achievementFinished(155) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(155).getName() + "#k (#rPrice: 1#k)#l\r\n";
    text += "#L7# " + (!cm.getPlayer().achievementFinished(65) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(65).getName() + "#k (#rPrice: 1#k)#l\r\n";
    text += "#L8# " + (!cm.getPlayer().achievementFinished(74) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(74).getName() + "#k (#rPrice: 2#k)#l\r\n";
    text += "#L9# " + (!cm.getPlayer().achievementFinished(54) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(54).getName() + "#k (#rPrice: 2#k)#l\r\n";
    text += "#L10# " + (!cm.getPlayer().achievementFinished(53) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(53).getName() + "#k (#rPrice: 2#k)#l\r\n";
    text += "#L11# " + (!cm.getPlayer().achievementFinished(64) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(64).getName() + "#k (#rPrice: 2#k)#l\r\n";
    text += "#L12# " + (!cm.getPlayer().achievementFinished(50) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(50).getName() + "#k (#rPrice: 2#k)#l\r\n";
    //iron key
    text += "#L30# " + (!cm.getPlayer().achievementFinished(170) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(170).getName() + "#k (#rPrice: 3#k)#l\r\n";

    text += "#L13# " + (!cm.getPlayer().achievementFinished(152) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(152).getName() + "#k (#rPrice: 4#k)#l\r\n";
    text += "#L14# " + (!cm.getPlayer().achievementFinished(69) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(69).getName() + "#k (#rPrice: 4#k)#l\r\n";
    text += "#L15# " + (!cm.getPlayer().achievementFinished(180) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(180).getName() + "#k (#rPrice: 4#k)#l\r\n";
    text += "#L16# " + (!cm.getPlayer().achievementFinished(181) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(181).getName() + "#k (#rPrice: 4#k)#l\r\n";
    text += "#L17# " + (!cm.getPlayer().achievementFinished(400) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(400).getName() + "#k (#rPrice: 5#k)#l\r\n";

    //bgc
    text += "#L31# " + (!cm.getPlayer().achievementFinished(185) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(185).getName() + "#k (#rPrice: 6#k)#l\r\n";
    text += "#L18# " + (!cm.getPlayer().achievementFinished(401) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(401).getName() + "#k (#rPrice: 7#k)#l\r\n";
    text += "#L19# " + (!cm.getPlayer().achievementFinished(402) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(402).getName() + "#k (#rPrice: 7#k)#l\r\n";
    text += "#L20# " + (!cm.getPlayer().achievementFinished(403) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(403).getName() + "#k (#rPrice: 7#k)#l\r\n";
    text += "#L21# " + (!cm.getPlayer().achievementFinished(404) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(404).getName() + "#k (#rPrice: 7#k)#l\r\n";
    text += "#L22# " + (!cm.getPlayer().achievementFinished(405) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(405).getName() + "#k (#rPrice: 8#k)#l\r\n";
    text += "#L32# " + (!cm.getPlayer().achievementFinished(184) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(184).getName() + "#k (#rPrice: 8#k)#l\r\n";

    text += "#L23# " + (!cm.getPlayer().achievementFinished(406) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(406).getName() + "#k (#rPrice: 10#k)#l\r\n";
    text += "#L24# " + (!cm.getPlayer().achievementFinished(407) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(407).getName() + "#k (#rPrice: 10#k)#l\r\n";

    text += "#L33# " + (!cm.getPlayer().achievementFinished(2000) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(2000).getName() + "#k (#rPrice: 15#k)#l\r\n";
    text += "#L25# " + (!cm.getPlayer().achievementFinished(408) ? "#b" : "#g") + "Unlock " + cm.getPlayer().getAchievementInfo(408).getName() + "#k (#rPrice: 25#k)#l\r\n";

    text += "\r\n#L99##rUnlock ALL Story mode achievements#k (#bPrice: 100#k)#l\r\n";
    cm.sendSimple("Select which mode you which option you want.\r\n" + text + "\r\n ");
}

function action(mode, type, selection) {
    if (mode < 0) {
    } else {
        if (mode == 0 && type > 0) {
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 1) {
            option = selection;
            if (option == 99) {
                price = 100;
                cm.sendYesNo("Are you sure you want to spend " + price + " #i" + item + "# to unlock #rAll Achievements#k?");
            } else {
                if (option >= 0 && option <= 7) {
                    price = 1;
                }
                if (option >= 8 && option <= 12) {
                    price = 2;
                }
                if (option == 30) {
                    price = 3;
                }
                if (option >= 13 && option <= 16) {
                    price = 4;
                }
                if (option == 17) {
                    price = 5;
                }
                if (option == 31) {
                    price = 6;
                }
                if (option >= 18 && option <= 21) {
                    price = 7;
                }
                if (option == 22 || option == 32) {
                    price = 8;
                }
                if (option == 23 || option == 24) {
                    price = 10;
                }
                if (option == 33) {
                    price = 15;
                }
                if (option == 25) {
                    price = 25;
                }
                cm.sendYesNo("Are you sure you want to spend " + price + " #i" + item + "# on this achievement?");
            }
        } else if (status == 2) {
            if (cm.haveItem(item, price)) {
                if (option == 99) {
                    cm.gainItem(4310505, -price);
                    cm.getPlayer().finishAchievement(39);
                    cm.getPlayer().finishAchievement(60);
                    cm.getPlayer().finishAchievement(61);
                    cm.getPlayer().finishAchievement(52);
                    cm.getPlayer().finishAchievement(70);
                    cm.getPlayer().finishAchievement(66);
                    cm.getPlayer().finishAchievement(155);
                    cm.getPlayer().finishAchievement(65);
                    cm.getPlayer().finishAchievement(74);
                    cm.getPlayer().finishAchievement(54);
                    cm.getPlayer().finishAchievement(53);
                    cm.getPlayer().finishAchievement(64);
                    cm.getPlayer().finishAchievement(50);
                    cm.getPlayer().finishAchievement(152);
                    cm.getPlayer().finishAchievement(69);
                    cm.getPlayer().finishAchievement(180);
                    cm.getPlayer().finishAchievement(181);
                    cm.getPlayer().finishAchievement(400);
                    cm.getPlayer().finishAchievement(401);
                    cm.getPlayer().finishAchievement(402);
                    cm.getPlayer().finishAchievement(403);
                    cm.getPlayer().finishAchievement(404);
                    cm.getPlayer().finishAchievement(405);
                    cm.getPlayer().finishAchievement(406);
                    cm.getPlayer().finishAchievement(407);
                    cm.getPlayer().finishAchievement(408);
                    cm.getPlayer().finishAchievement(170);
                    cm.getPlayer().finishAchievement(185);
                    cm.getPlayer().finishAchievement(184);
                    cm.getPlayer().finishAchievement(2000);
                    cm.sendOk("All Story mode achievements have been unlocked.");
                } else {

                    if (option == 0 && !cm.getPlayer().achievementFinished(39)) {
                        cm.getPlayer().finishAchievement(39);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(39).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 1 && cm.getPlayer().achievementFinished(39) && !cm.getPlayer().achievementFinished(60)) {
                        cm.getPlayer().finishAchievement(60);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(60).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 2 && cm.getPlayer().achievementFinished(60) && !cm.getPlayer().achievementFinished(61)) {
                        cm.getPlayer().finishAchievement(61);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(61).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 3 && cm.getPlayer().achievementFinished(61) && !cm.getPlayer().achievementFinished(52)) {
                        cm.getPlayer().finishAchievement(52);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(52).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 4 && cm.getPlayer().achievementFinished(52) && !cm.getPlayer().achievementFinished(70)) {
                        cm.getPlayer().finishAchievement(70);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(70).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 5 && cm.getPlayer().achievementFinished(70) && !cm.getPlayer().achievementFinished(66)) {
                        cm.getPlayer().finishAchievement(66);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(66).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 6 && cm.getPlayer().achievementFinished(66) && !cm.getPlayer().achievementFinished(155)) {
                        cm.getPlayer().finishAchievement(155);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(155).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 7 && cm.getPlayer().achievementFinished(155) && !cm.getPlayer().achievementFinished(65)) {
                        cm.getPlayer().finishAchievement(65);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(65).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 8 && cm.getPlayer().achievementFinished(65) && !cm.getPlayer().achievementFinished(74)) {
                        cm.getPlayer().finishAchievement(74);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(74).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 9 && cm.getPlayer().achievementFinished(74) && !cm.getPlayer().achievementFinished(54)) {
                        cm.getPlayer().finishAchievement(54);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(54).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 10 && cm.getPlayer().achievementFinished(54) && !cm.getPlayer().achievementFinished(53)) {
                        cm.getPlayer().finishAchievement(53);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(53).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 11 && cm.getPlayer().achievementFinished(53) && !cm.getPlayer().achievementFinished(64)) {
                        cm.getPlayer().finishAchievement(64);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(64).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 12 && cm.getPlayer().achievementFinished(64) && !cm.getPlayer().achievementFinished(50)) {
                        cm.getPlayer().finishAchievement(50);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(50).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 13 && cm.getPlayer().achievementFinished(50) && !cm.getPlayer().achievementFinished(152)) {
                        cm.getPlayer().finishAchievement(152);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(152).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 14 && cm.getPlayer().achievementFinished(152) && !cm.getPlayer().achievementFinished(69)) {
                        cm.getPlayer().finishAchievement(69);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(69).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 15 && cm.getPlayer().achievementFinished(69) && !cm.getPlayer().achievementFinished(180)) {
                        cm.getPlayer().finishAchievement(180);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(180).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 16 && cm.getPlayer().achievementFinished(180) && !cm.getPlayer().achievementFinished(181)) {
                        cm.getPlayer().finishAchievement(181);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(181).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 17 && cm.getPlayer().achievementFinished(181) && !cm.getPlayer().achievementFinished(400)) {
                        cm.getPlayer().finishAchievement(400);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(400).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 18 && cm.getPlayer().achievementFinished(170) && !cm.getPlayer().achievementFinished(401)) {
                        cm.getPlayer().finishAchievement(401);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(401).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 19 && cm.getPlayer().achievementFinished(401) && !cm.getPlayer().achievementFinished(402)) {
                        cm.getPlayer().finishAchievement(402);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(402).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 20 && cm.getPlayer().achievementFinished(402) && !cm.getPlayer().achievementFinished(403)) {
                        cm.getPlayer().finishAchievement(403);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(403).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 21 && cm.getPlayer().achievementFinished(403) && !cm.getPlayer().achievementFinished(404)) {
                        cm.getPlayer().finishAchievement(404);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(404).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 22 && cm.getPlayer().achievementFinished(404) && !cm.getPlayer().achievementFinished(405)) {
                        cm.getPlayer().finishAchievement(405);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(405).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 23 && cm.getPlayer().achievementFinished(184) && !cm.getPlayer().achievementFinished(406)) {
                        cm.getPlayer().finishAchievement(406);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(406).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 24 && cm.getPlayer().achievementFinished(184) && !cm.getPlayer().achievementFinished(407)) {
                        cm.getPlayer().finishAchievement(407);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(407).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 25 && cm.getPlayer().achievementFinished(2000) && !cm.getPlayer().achievementFinished(408)) {
                        cm.getPlayer().finishAchievement(408);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(408).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 30 && cm.getPlayer().achievementFinished(50) && !cm.getPlayer().achievementFinished(170)) {
                        cm.getPlayer().finishAchievement(170);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(170).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 31 && cm.getPlayer().achievementFinished(400) && !cm.getPlayer().achievementFinished(185)) {
                        cm.getPlayer().finishAchievement(185);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(185).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 32 && cm.getPlayer().achievementFinished(405) && !cm.getPlayer().achievementFinished(184)) {
                        cm.getPlayer().finishAchievement(184);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(184).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    if (option == 33 && cm.getPlayer().achievementFinished(405) && !cm.getPlayer().achievementFinished(2000)) {
                        cm.getPlayer().finishAchievement(2000);
                        cm.sendOk("You have successfully unlocked - #g" + cm.getPlayer().getAchievementInfo(2000).getName() + "#k");
                        cm.gainItem(4310505, -price);
                        return;
                    }
                    cm.sendOk("You have already unlocked this achivement or you missing some requirements.");
                }
            } else {
                cm.sendOk("Seems you dont have enough CP for this achivement!");
            }
        } else if (status == 2) {

        }
    }
}