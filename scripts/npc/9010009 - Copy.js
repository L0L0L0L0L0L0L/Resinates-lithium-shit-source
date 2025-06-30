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

var bonus = 1;

var codeinfo;

var reward = 4033320;
var rewamount = 1;
var box = 0;
var amount = 0;
var Cantidad;

function start() {
    var text = "";
    text += "#L4#What is Kaotic Market Store#l\r\n";
    text += "#L1##bVisit Kaotic Market Store#k#l\r\n";
    if (bonus > 1) {
        text += "#L2##rCash in Product Code#k (#b" + bonus + "x SALE!!!#k)#l\r\n";
    } else {
        text += "#L2##rCash in Product Code#k#l\r\n";
    }

    text += "#L3##rExchange Packages for Rewards#b#l\r\n";
    cm.sendSimple("Welcome to the Kaotic Market.\r\nHow may I help you?\r\n" + text);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }


    if (status == 1) {
        option = selection;
        if (option == 1) {
            cm.openLink("https://kaoticgaming.mysellix.io/");
            cm.dispose();
        }
        if (option == 2) {
            var text = "";
            if (bonus > 1) {
                text = "#rClaim Sale is Active\r\nAll Claim Rewards are " + bonus + "x\r\n";
            }
            cm.sendGetText("Welcome to Sellix Redeemer Shop.\r\nPlease enter your #bSellix Code#k\r\n" + text);
        }
        if (option == 3) {
            var boxes = 0;
            var text = "";
            if (cm.haveItem(2400000)) {
                text += "#L0##i2400000##l  ";
                boxes++;
            }
            if (cm.haveItem(2400001)) {
                text += "#L1##i2400001##l  ";
                boxes++;
            }
            if (cm.haveItem(2400002)) {
                text += "#L2##i2400002##l  ";
                boxes++;
            }
            if (cm.haveItem(2400003)) {
                text += "#L3##i2400003##l  ";
                boxes++;
            }
            if (cm.haveItem(2400004)) {
                text += "#L4##i2400004##l  ";
                boxes++;
            }
            if (cm.haveItem(2400005)) {
                text += "#L5##i2400005##l  ";
                boxes++;
            }
            if (cm.haveItem(2400006)) {
                text += "#L6##i2400006##l  ";
                boxes++;
            }
            if (cm.haveItem(2400007)) {
                text += "#L7##i2400007##l  ";
                boxes++;
            }
            if (cm.haveItem(2400008)) {
                text += "#L8##i2400008##l  ";
                boxes++;
            }
            if (cm.haveItem(2400009)) {
                text += "#L9##i2400009##l  ";
                boxes++;
            }
            if (boxes > 0) {
                cm.sendSimple("Welcome to the Package Exchange.\r\nHow may I help you?\r\n\r\n" + text);
            } else {
                cm.sendOk("You dont seem to have any reward boxes.");
            }
        }
        if (option == 4) {
            cm.sendOkS("Kaotic Market Store is our donation store system. Donations are used to help cover the costs of bills and employess of Kaotic Maple. In the store you place your order to buy any package you want, then when order is fulfilled you will get a #rProduct Code#k in your email. Use the code here to redeemable your product and packages.", 16);
        }
    } else if (status == 2) {
        if (option == 2) {
            codeinfo = cm.checkSellixCode(cm.getText());
            if (codeinfo != null) {
                cm.sendYesNo("Do you want to redeem sellix code:\r\n\"#r" + cm.getText() + "#k\"\r\n#bReward#k: #bx" + codeinfo.amount() + " " + cm.getItemName(codeinfo.itemId()) + "s#k ");
            } else {
                cm.sendOkS("#rThis Code does not exist#k.", 16);
            }
        }
        if (option == 3) {
            box = 2400000 + selection;
            if (cm.haveItem(box)) {
                cm.sendGetText("How many #i" + box + "#'s do you want to exchange for rewards?");
            } else {
                cm.sendOk("You dont seem to have any #i" + box + "#s.");
            }
        }
    } else if (status == 3) {
        if (option == 2) {
            if (codeinfo != null) {
                if (cm.canHold(codeinfo.itemId(), codeinfo.amount())) {
                    if (cm.tryRedeemCode(codeinfo)) {
                        cm.gainItem(codeinfo.itemId(), codeinfo.amount() * bonus);
                        cm.sendOkS("#bYou have successfuly redeemed\r\n\#k#r" + codeinfo.code() + "#k\r\n\#bRecieved: " + codeinfo.amount() + " " + cm.getItemName(codeinfo.itemId()) + "s#k.", 16);
                    } else {
                        cm.sendOkS("#rSellix Code has already been redeemed#k.", 16);
                    }
                } else {
                    cm.sendOkS("#rYou do not have enough room to carry this item.#k.", 16);
                }
            }
        }
        if (option == 3) {
            amount = cm.getNumber();
            if (cm.haveItem(box, amount)) {
                cm.sendYesNo("Do you want to confirm that you wish to cash in " + amount + " #i" + box + "# ");
            } else {
                cm.sendOk("You dont seem to have enough #i" + box + "#s.");
            }
        }
    } else if (status == 4) {
        if (option == 3) {
            if (cm.haveItem(box, amount)) {

                var text = "Rewards gained from " + amount + " #i" + box + "#:\r\n\r\n";
                var atotal = amount;
                if (box == 2400000) {//starter
                    if (cm.getPlayer().canHold(4310502, 100 * atotal) && cm.getPlayer().canHold(2430131, 25 * atotal) && cm.getPlayer().canHold(5220020, 10 * atotal) && cm.getPlayer().canHold(2005000, atotal) && cm.getPlayer().canHold(4420021, atotal)) {
                        var item = 4310502, count = 100;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        item = 2430131, count = 25;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        item = 5220020, count = 10;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        item = 2005000, count = 1;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        var item = 4420021, count = 1;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        cm.gainItem(box, -amount);
                    } else {
                        text = "#rYou do not have enough room to hold all the items, Make space.";
                    }
                }
                if (box == 2400001) {//DP
                    if (cm.getPlayer().canHold(4310502, 100 * atotal)) {
                        var item = 4310502, count = 100;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";
                        cm.gainItem(box, -amount);
                    } else {
                        text = "#rYou do not have enough room to hold all the items, Make space.";
                    }
                }
                if (box == 2400002) {//STam
                    if (cm.getPlayer().canHold(2000012, 250 * atotal)) {
                        var item = 2000012, count = 250;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";
                        cm.gainItem(box, -amount);
                    } else {
                        text = "#rYou do not have enough room to hold all the items, Make space.";
                    }
                }
                if (box == 2400003) {//damage skin
                    if (cm.getPlayer().canHold(4420005, 10 * atotal) && cm.getPlayer().canHold(4420005, 5 * atotal) && cm.getPlayer().canHold(4420021, atotal)) {
                        var item = 4420005, count = 10;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        var item = 4420006, count = 5;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        var item = 4420021, count = 1;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";
                        cm.gainItem(box, -amount);
                    } else {
                        text = "#rYou do not have enough room to hold all the items, Make space.";
                    }
                }
                if (box == 2400006) {//booster
                    if (cm.getPlayer().canHold(2430131, 25 * atotal) && cm.getPlayer().canHold(2430130, 5 * atotal)) {
                        var item = 2430131, count = 25;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        var item = 2430130, count = 5;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";
                        cm.gainItem(box, -amount);
                    } else {
                        text = "#rYou do not have enough room to hold all the items, Make space.";
                    }
                }
                if (box == 2400008) {//casino
                    if (cm.getPlayer().canHold(4310101, 5000 * atotal)) {
                        var item = 4310101, count = 5000;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";
                        cm.gainItem(box, -amount);
                    } else {
                        text = "#rYou do not have enough room to hold all the items, Make space.";
                    }
                }
                if (box == 2400009) {//supreme
                    if (cm.getPlayer().canHold(4420007, atotal)) {
                        var item = 4420007, count = 1;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";
                        cm.gainItem(box, -amount);
                    } else {
                        text = "#rYou do not have enough room to hold all the items, Make space.";
                    }
                }
                cm.sendOk(text);
            } else {
                cm.sendOk("You dont seem to have enough #i" + box + "#s.");
            }
        }
    } else if (status == 5) {

    }
}