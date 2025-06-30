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
var ticketId = 4310502;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scroll = 0;
var scrollslot = 0;
var amount = 1000000;
var slotcount = 0;
var ep = 0;
var tier = 0;
var power = 0;
var option = 0;
var count = 0;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var pots = 0;
var ipots = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {

    } else {
        if (mode == 0 && type > 0) {

            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            pots = cm.getPlayer().getAccVara("POT");
            ipot = cm.getPlayer().getAccVara("I_POT");
            var text = "You currently have #b" + pots + "#k Magic Pots Activated!\r\n";
            text += "You currently have #b" + ipot + "#k Infinity Pots Activated!\r\n";
            text += "#L5##bWhat are uses of Supreme Magic Pots#k#l\r\n";
            text += "#L3##bActivate Supreme Pots#k#l\r\n";
            text += "\r\n";
            text += "#L6##bWhat are uses of Infinity Magic Pots#k (#rVersion: 1#k)#l\r\n";
            text += "#L7##bActivate Infinity Pots#k#l\r\n";
            if (ipot > 0) {
                text += "#L8##bClaim Infinity Rewards#k (#rVersion: 1#k)#l\r\n";
            }
            cm.sendSimple(text);
        } else if (status == 1) {
            if (selection == 3) {
                option = 1;
                pots = cm.getPlayer().countItem(5052000);
                cm.sendYesNoS("Do you wish to activate all your Supreme Pots?\r\n#rYou currently have " + pots + " Supreme Pots not activated#k", 16);
            }
            if (selection == 9) {
                option = 9;
                cm.sendGetText("I can convert your un-activated #i5450200# into #b100#k #i5052000#");
            }
            if (selection == 4) {
                option = 2;
                cm.sendGetText("Kaotic Boosts increases the amount of Kaotic Power gained per equip dropped on bosses.\r\n#b1 Kaotic Boost = +1 KP Gained per Equip Dropped#k\r\n\r\n#bEach Boost costs 10 #rRaw Magic Pots#k.\r\nPlease enter the number of Kaotic Boosts you wish to get.");
            }
            if (selection == 5) {
                var text = "#rMain uses of Magic Pots are for personal passive supportive gains.#k\r\n\r\n";
                text += "#bPassives are based on number of pots:#k\r\n";//DP
                text += "#bPassive values are applied are multipled:#k\r\n";//DP
                text += "" + star + "Increases Player Damage\r\n";//gml
                text += "" + star + "Increases Damage Skin Exp rates.\r\n";//gml
                text += "" + star + "Increases Weapon Mastery Exp rates. (#rMax 100k#k)\r\n";//gml
                text += "" + star + "Increases Mastery Exp rates. (#rMax 100k#k)\r\n";//gml
                text += "" + star + "Increases Skill Exp gained while farming DMP.\r\n";//gml
                text += "" + star + "Increases Increases Range on Pet attacks.\r\n";//gml
                text += "" + star + "Increases IED rates at #b+25%#k Per Magic Pot.\r\n";//gml
                text += "" + star + "Increases Exp rates at #b+25%#k Per Magic Pot.\r\n";//gml
                text += "" + star + "Increases Leech Exp by #b+1%#k Per Magic Pot. (#rMax: 100%#k)\r\n";//gml
                text += "" + star + "Increases Item Drop Power by #b+10%#k Per Magic Pot.\r\n";//gml
                text += "\r\n";//gml
                cm.sendOk(text);
            }
            if (selection == 6) {
                var text = "#rMain uses of Infinity Pots are for personal active supportive gains.#k\r\n\r\n";
                text += "#bRewards are based on number of pots:#k\r\n";//DP
                text += "#bCurrent List of Rewards are claimable #rOnce a day#k Per iPot:#k\r\n";//DP
                text += "#bCurrent Day#k: " + cm.getPlayer().getCurrentDay() + " - #rClaimed Day#k: " + cm.getPlayer().getLastDay() + "\r\n";
                text += "#rx = number of iPots you have activated#k\r\n";//DP
                text += "#i4310500##i4430006##i4036518##i4310502##i4310510##i4034867##i2049032##i4310504#\r\n";
                text += "" + star + "#b250000x#k " + cm.getItemName(4310500) + "\r\n";//gml
                text += "" + star + "#b10000x#k " + cm.getItemName(4430006) + "\r\n";//gml
                text += "" + star + "#b5000x#k " + cm.getItemName(4036518) + "\r\n";//gml
                text += "" + star + "#b2500x#k " + cm.getItemName(4310502) + "\r\n";//gml
                text += "" + star + "#b500x#k " + cm.getItemName(4310510) + "\r\n";//gml
                text += "" + star + "#b100x#k " + cm.getItemName(4034867) + "\r\n";//gml
                text += "" + star + "#b50x#k " + cm.getItemName(2049032) + "\r\n";//gml
                text += "" + star + "#b20x#k " + cm.getItemName(4310504) + "\r\n";//gml
                text += "#rItems gained will be placed inside your OVERFLOW system.#k\r\n";//DP
                cm.sendOk(text);
            }
            if (selection == 7) {
                option = 3;
                pots = cm.getPlayer().countItem(5450200);
                cm.sendYesNoS("Do you wish to activate all your Infinity Pots?\r\n#rYou currently have " + pots + " Infinity Pots not activated#k", 16);
            }
            if (selection == 8) {
                option = 4;
                if (cm.getPlayer().checkDay()) {
                    cm.sendOk("Come back tomorrow to claim your rewards.");
                } else {
                    cm.sendYesNo("Do you want to claim all your #bInfinity Rewards#k?");
                }
            }
        } else if (status == 2) {
            if (option == 1) {
                if (cm.haveItem(5052000, pots)) {
                    cm.gainItem(5052000, -pots);
                    cm.getPlayer().addAccVar("POT", pots);
                    cm.sendOkS("You have activated " + pots + " Supreme Pots.", 16);
                } else {
                    cm.sendOkS("You dont have enough #i5052000#", 16);
                }
            }
            if (option == 2) {
                count = cm.getNumber();
                if (count > 0) {
                    if (cm.haveItem(4000999, (count * 5))) {
                        cm.sendYesNoS("Are you sure you want to exchange #b" + (count * 5) + "#k #i4000999# for #b" + count + "#k Elwin Power Level Ups?" + text, 16);
                    } else {
                        cm.sendOkS("You dont have enough #i4000999#", 16);
                    }
                } else {
                    cm.sendOkS("You dont have enough #i4000999#", 16);
                }
            }
            if (option == 9) {
                count = cm.getNumber();
                if (count > 0) {
                    if (cm.haveItem(5450200, count)) {
                        cm.sendYesNoS("Are you sure you want to exchange #b" + (count) + "#k #i5450200# for #b" + (count * 100) + "#k #i5052000#?\r\n#rThese Supreme will be auto-activated!#k", 16);
                    } else {
                        cm.sendOkS("You dont have enough #i5450200#", 16);
                    }
                } else {
                    cm.sendOkS("You dont have enough #i5450200#", 16);
                }
            }
            if (option == 3) {
                if (cm.haveItem(5450200, pots)) {
                    cm.gainItem(5450200, -pots);
                    cm.getPlayer().addAccVar("I_POT", pots);
                    cm.sendOkS("You have activated " + pots + " Infinity Pots.", 16);
                } else {
                    cm.sendOkS("You dont have enough #i5450200#", 16);
                }
            }
            if (option == 4) {
                cm.getPlayer().setAccVaraLock("I_Pot_Time", 14400);
                cm.getPlayer().setAccVar("DAY", cm.getPlayer().getDay());
                ipots = cm.getPlayer().getAccVara("I_POT");
                var text = "You have collected the following items:\r\n";
                text += "" + star + "#b" + (250000 * ipots) + "#k " + cm.getItemName(4310500) + "'s\r\n";//gml
                text += "" + star + "#b" + (10000 * ipots) + "#k " + cm.getItemName(4430006) + "'s\r\n";//gml
                text += "" + star + "#b" + (5000 * ipots) + "#k " + cm.getItemName(4036518) + "'s\r\n";//gml
                text += "" + star + "#b" + (2500 * ipots) + "#k " + cm.getItemName(4310502) + "'s\r\n";//gml
                text += "" + star + "#b" + (500 * ipots) + "#k " + cm.getItemName(4310510) + "'s\r\n";//gml
                text += "" + star + "#b" + (100 * ipots) + "#k " + cm.getItemName(4034867) + "'s\r\n";//gml
                text += "" + star + "#b" + (50 * ipots) + "#k " + cm.getItemName(2049032) + "'s\r\n";//gml
                text += "" + star + "#b" + (20 * ipots) + "#k " + cm.getItemName(4310504) + "'s\r\n";//gml
                text += "#rAll rewards have been sent to Overflow system (@etc-@item)#k\r\n";//gml
                text += "#bCome back tomorrow to claim more rewards.#k\r\n";//gml
                cm.getPlayer().addOverflow(4310500, 250000 * ipots, "collected from Infinity Rewards");
                cm.getPlayer().addOverflow(4430006, 10000 * ipots, "collected from Infinity Rewards");
                cm.getPlayer().addOverflow(4036518, 5000 * ipots, "collected from Infinity Rewards");
                cm.getPlayer().addOverflow(4310502, 2500 * ipots, "collected from Infinity Rewards");
                cm.getPlayer().addOverflow(4310510, 500 * ipots, "collected from Infinity Rewards");
                cm.getPlayer().addOverflow(4034867, 100 * ipots, "collected from Infinity Rewards");
                cm.getPlayer().addOverflow(2049032, 50 * ipots, "collected from Infinity Rewards");
                cm.getPlayer().addOverflow(4310504, 20 * ipots, "collected from Infinity Rewards");
                cm.systemMsg(cm.getPlayer().getName() + " Claimed Free I-Pot Rewards. (Level: " + cm.getPlayer().getTotalLevel() + ")-(Pots: " + ipots + ")-(IP: " + cm.getPlayer().getClient().getSessionIPAddress() + ")");
                cm.sendOkS(text, 16);
            }
        } else if (status == 3) {
            if (option == 9) {
                if (cm.haveItem(5450200, count)) {
                    cm.gainItem(5450200, -count);
                    cm.getPlayer().addAccVar("POT", count * 100);
                    cm.sendOkS("You have converted #b" + count + "#k Infinity Pots into #b" + count * 100 + "#k #rActivated#k Supreme Pots.", 16);
                } else {
                    cm.sendOkS("You dont have enough #i5450200#", 16);
                }
            }
            if (option == 2) {
                if ((cm.getPlayer().getLevelData(100) + count) <= 99999) {
                    if (cm.haveItem(4000999, (count * 5))) {
                        cm.getPlayer().gainLevelsData(100, count);
                        cm.gainItem(4000999, -(count * 5));
                        cm.sendOkS("You have gained " + count + " Elwin Power Levels", 16);
                    } else {
                        cm.sendOkS("You dont have enough #i4000999#", 16);
                    }
                } else {
                    cm.sendOkS("You alrdy have maxxed out this Mastery.", 16);
                }
            } else {
                if (cm.haveItem(4000999, amount)) {
                    var rewards = cm.getRewardsByPower2(9110100, cm.getPlayer(), power, tier);
                    if (rewards != null) {
                        var iter = rewards.iterator();
                        var text = "";
                        while (iter.hasNext()) {
                            var i = iter.next();
                            text += "#i" + i.getItemId() + "# #t" + i.getItemId() + "# " + i.getQuantity() + "x\r\n\r\n";
                        }
                        cm.gainItem(4000999, -amount);
                        cm.sendOkS("You have gained:\r\n" + text, 16);
                    } else {
                        cm.sendOkS("Error with DP npc, please report to discord", 16);
                    }
                } else {
                    cm.sendOkS("You dont have enough #i4000999#", 16);
                }
            }
        } else if (status == 4) {

        }
    }
}