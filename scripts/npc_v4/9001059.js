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
var cube = 4310272;
var price = 1;
var option = 0;
var rounds = 0;
var slot = 25;
var multi = 0;
var exp = 1;
var typez = 101;
var icon = " ";
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var status = 0;
var password = 0;
var trace = 0;
var reward = 0;
var reward_amount = 0;

var bonus = 1;

var codeinfo;

var reward = 4033320;
var rewamount = 1;
var box = 0;
var amount = 0;
var Cantidad;

function start() {
    var text = "#bCurrent Collectable Rewards#k:\r\n#i2049177# #i2585008# #i2586005# #i2049308# #i2049032#\r\n";
    text += "#L4310337# #i4310337# #rExchange Shiny Star Fragments#k#l\r\n";
    text += "#L4310338# #i4310338# #rExchange Infinity Star Fragments#k#l\r\n\r\n";
    text += "#bRewards of Choice#k:\r\n#i2049177# #i2585008# #i2586005# #i2049308#\r\n";
    text += "#L4310505# #i4310505# #rExchange Infinity Points#b#l\r\n";
    cm.sendSimple("Welcome to the Star Rewards Program.\r\nHow may I help you?\r\n" + text);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        box = selection;
        if (cm.haveItem(box)) {
            cm.sendGetText("How many #i" + box + "#'s do you want to exchange for rewards?");
        } else {
            if (box == 4310337) {//starter
                cm.sendOk("You dont seem to have any #i" + box + "#s.\r\n#bThese can be obtained from Party Quest Rewards#k\r\n(#rComing Soon....#k)");
            }
            if (box == 4310338) {//starter
                cm.sendOk("You dont seem to have any #i" + box + "#s.\r\n#bThese can be obtained from Duey, Infinity Shop#k");
            }
            if (box == 4310505) {//starter
                cm.sendOk("You dont seem to have any #i" + box + "#s.\r\n#bThese can be obtained from Duey.#k");
            }
        }
    } else if (status == 2) {
        amount = cm.getNumber();
        cm.sendYesNo("Do you want to confirm that you wish to redeem " + amount + " #i" + box + "#s");
    } else if (status == 3) {
        if (cm.haveItem(box, amount)) {
            if (box == 4310337) {//starter
                var pots = amount;
                var text = "You have collected the following items:\r\n";
                text += "#i2049177##b" + cm.convertNumber(200 * pots) + "#k " + cm.getItemName(2049177) + "'s\r\n";//gml
                text += "#i2585008##b" + cm.convertNumber(200 * pots) + "#k " + cm.getItemName(2585008) + "'s\r\n";//gml
                text += "#i2586005##b" + cm.convertNumber(50 * pots) + "#k " + cm.getItemName(2586005) + "'s\r\n";//gml
                text += "#i2049308##b" + cm.convertNumber(25 * pots) + "#k " + cm.getItemName(2049308) + "'s\r\n";//gml
                text += "#rAll rewards have been sent to Overflow system (@etc-@item)#k\r\n";//gml
                cm.getPlayer().addOverflow(2049177, 200 * pots, "collected from Star Rewards");
                cm.getPlayer().addOverflow(2585008, 200 * pots, "collected from Star Rewards");
                cm.getPlayer().addOverflow(2586005, 50 * pots, "collected from Star Rewards");
                cm.getPlayer().addOverflow(2049308, 25 * pots, "collected from Star Rewards");
                cm.gainItem(box, -pots);
                cm.sendOkS(text, 16);
            }
            if (box == 4310338) {//starter
                var pots = amount;
                var text = "You have collected the following items:\r\n";
                text += "#i2049177##b" + cm.convertNumber(10000 * pots) + "#k " + cm.getItemName(2049177) + "'s\r\n";//gml
                text += "#i2585008##b" + cm.convertNumber(10000 * pots) + "#k " + cm.getItemName(2585008) + "'s\r\n";//gml
                text += "#i2586005##b" + cm.convertNumber(5000 * pots) + "#k " + cm.getItemName(2586005) + "'s\r\n";//gml
                text += "#i2049308##b" + cm.convertNumber(2500 * pots) + "#k " + cm.getItemName(2049308) + "'s\r\n";//gml
                text += "#i2049032##b" + cm.convertNumber(500 * pots) + "#k " + cm.getItemName(2049032) + "'s (#bHidden Bonus#k)\r\n";//gml
                text += "#rAll rewards have been sent to Overflow system (@etc-@item)#k\r\n";//gml
                cm.getPlayer().addOverflow(2049177, 10000 * pots, "collected from Star Rewards");
                cm.getPlayer().addOverflow(2585008, 10000 * pots, "collected from Star Rewards");
                cm.getPlayer().addOverflow(2586005, 5000 * pots, "collected from Star Rewards");
                cm.getPlayer().addOverflow(2049308, 2500 * pots, "collected from Star Rewards");
                cm.getPlayer().addOverflow(2049032, 500 * pots, "collected from Star Rewards");
                cm.gainItem(box, -pots);
                cm.sendOkS(text, 16);
            }
            if (box == 4310505) {//starter
                var text = "Select Collectable Option:\r\n";
                text += "#L2049177# #i2049177# #b" + cm.getItemName(2049177) + "#k (#r25,000#k)#l\r\n";
                text += "#L2585008# #i2585008# #b" + cm.getItemName(2585008) + "#k (#r25,000#k)#l\r\n";
                text += "#L2586005# #i2586005# #b" + cm.getItemName(2586005) + "#k (#r10,000#k)#l\r\n";
                text += "#L2049308# #i2049308# #b" + cm.getItemName(2049308) + "#k (#r5,000#k)#l\r\n";
                cm.sendSimple("Selection which reward you want." + text);
                var pots = amount;
            }
        } else {
            cm.sendOk("You dont seem to have any #i" + box + "#s");
        }
    } else if (status == 4) {
        if (cm.haveItem(box, amount)) {
            if (box == 4310505) {//starter
                reward = selection;
                if (reward == 2049177) {
                    reward_amount = 25000 * amount;
                }
                if (reward == 2585008) {
                    reward_amount = 25000 * amount;
                }
                if (reward == 2586005) {
                    reward_amount = 10000 * amount;
                }
                if (reward == 2049308) {
                    reward_amount = 5000 * amount;
                }
                if (reward == 2049032) {
                    reward_amount = 1000 * amount;
                }
                var text = "You have collected the following items:\r\n";
                text += "#i" + reward + "##b" + cm.convertNumber(reward_amount) + "#k " + cm.getItemName(reward) + "'s\r\n";//gml
                text += "#rAll rewards have been sent to Overflow system (@etc-@item)#k\r\n";//gml
                cm.getPlayer().addOverflow(reward, reward_amount, "collected from Star Rewards");
                cm.gainItem(box, -amount);
                cm.sendOkS(text, 16);
            }
        } else {
            cm.sendOk("You dont seem to have any #i" + box + "#s");
        }
    }
}