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
var dprice = 1;
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
var finalcost = 0;

function start() {
    var selStr = "Welcome, you can use Doantion Points to boost the amount of EXP gained from big wins and jackpots.\r\n";
    selStr += "Select a Reward Boost Rate:\r\n";
    selStr += "#L26##r10x Exp rate#k - #bCost: upto 10 Reward Points#k#l\r\n";
    selStr += "#L27##r25x Exp rate#k - #bCost: upto 25 Reward Points#k#l\r\n";
    selStr += "#L28##r50x Exp rate#k - #bCost: upto 50 Reward Points#k#l\r\n";
    selStr += "#L29##r100x Exp rate#k - #bCost: upto 100 Reward Points#k#l\r\n";
    selStr += "#L30##r250x Exp rate#k - #bCost: upto 250 Reward Points#k#l\r\n";
    cm.sendSimple(selStr);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        if (!cm.getLockSlot()) {
            slot = selection;
            if (slot == 26) {
                dprice = 10;
            }
            if (slot == 27) {
                dprice = 25;
            }
            if (slot == 28) {
                dprice = 50;
            }
            if (slot == 29) {
                dprice = 100;
            }
            if (slot == 30) {
                dprice = 250;
            }
            var selStr = "Welcome to #i" + cube + "# Machine. Exp rate: #b" + cm.getSlotExp(slot) + "#k\r\n";
            selStr += "#rPrice: " + price + " tokens#k\r\n";
            selStr += "#rCoin Bonus: +" + cm.getPlayer().getLevelData(101) + "%#k\r\n";
            selStr += "#L3#Slot Information#l\r\n";
            selStr += "#L1#What items does slots give?#l\r\n";
            selStr += "#L2##bI want to spin some slots#k?#l\r\n";
            cm.sendSimple(selStr);
        } else {
            cm.sendOk("Slot machine is currently closed.");
        }
    } else if (status == 2) {
        option = selection;
        if (option == 1) {
            selStr = "Blue = Min Amount, Red = Combo Amount, = Jackpot amount:\r\n\\r\n\ ";
            var itemz = cm.getPlayer().getItemsFromSlot(slot);
            for (var i = 0; i < itemz.size(); i++) {
                selStr += "#fMap/Effect.img/miro/RR1/" + itemz.get(i).getIcon() + "/6# = #i" + itemz.get(i).getId() + "# - #b" + itemz.get(i).getAmount() + "#k  - #r" + itemz.get(i).getAmount2() + "#k - #g" + itemz.get(i).getAmount3() + "#k\r\n\ ";
            }
            cm.sendOk("Here is comeplete list of all the items:\r\n\ " + selStr);
        }
        if (option == 2) {
            var text = "Which Multiplier would you like to use for Each Spin?\r\n";
            text += "#rMultipliers increases cost and amount of rewards.#k\r\n";
            text += "#rMultipliers are unlocked every 1000 levels.#k\r\n\r\n";
            text += "#L1##i3995001##l";
            text += "#L2##i3995002##l";
            text += "#L3##i3995003##l";
            text += "#L4##i3995004##l";
            text += "#L5##i3995005##l";
            text += "\r\n ";
            cm.sendGetText(text);
        }
        if (option == 3) {
            var text = "Slot Information:\r\n";
            text += star + "#rRewards#k\r\n";
            text += "      Every single spin rewards items, match 2 symbols and win extra amount. Match 3 Symbols win a jackpot for that item.\r\nWhich option would you like to pick from?\r\n";
            text += "\r\n";
            text += star + "#rMultipliers#k\r\n";
            text += "      #bMultipliers increases cost and amount of rewards.#k\r\n";
            text += "      #rMultipliers are unlocked every 1000 levels.#k\r\n\r\n";
            text += "      #i3995001##i3995002##i3995003##i3995004##i3995005#\r\n";
            text += "\r\n";
            text += star + "#rGambling Exp#k\r\n";
            text += "      #rExp amount per pull is " + cm.getSlotExp(slot) + " on Big Wins and Jackpots#k\r\n";
            text += "      #bExp scales with multipliers#k\r\n";
            text += "      #bExp is awarded when BIG WIN or JACKPOT is won#k\r\n";
            text += "      #bExp from JACKPOTS is 10x bonus#k\r\n";
            text += "      #bGambling levels effects how strong casino equip gachapon are#k\r\n";
            text += "      #bGambling levels also effects how many casino coins are earned#k\r\n";
            text += "\r\n ";
            cm.sendOk(text);
        }
    } else if (status == 3) {
        multi = selection;
        cm.sendGetText("Do you want to play slots and win some possible rewards or #rJACKPOTS#k?\r\n\Each spin costs #r" + price * multi + "#k #i" + cube + "# " + cm.getItemName(cube) + ".\r\n\How many spins would like you like to buy.\r\n\#rMaximum spin count is 9999#k.\r\n\#rIF YOU DO NOT HAVE ENOUGH SPACE TO HOLD THE ITEMS,\r\n\THEY WILL BE LOST#k");
    } else if (status == 4) {
        rounds = cm.getNumber();
        if (rounds > 0 && rounds <= 9999) {
            if (cm.haveItem(cube, rounds * price * multi)) {
                var perc = (rounds / 9999.0);
                if (perc < 0.1) {
                    perc = 0.1;
                }
                finalcost = parseInt(dprice * perc);
                cm.sendYesNo("Are sure that you want to spend " + (rounds * price * multi) + " #i" + cube + "# and\r\n\#bBoosted: " + finalcost + " Reward Points#k on " + rounds + " slot spins?\r\n\How many spins would like you like to buy.\r\n\#rIF YOU DO NOT HAVE ENOUGH SPACE TO HOLD THE ITEMS,\r\n\THEY WILL BE LOST#k\r\n");
            } else {
                cm.sendOk("You dont have enough #i" + cube + "# to play with.");
            }
        } else {
            cm.sendOk("You dont have enough brains to cheat this.");
        }
        //cm.warp(450009050, "pt_back");
    } else if (status == 5) {
        if (rounds > 0 && rounds <= 9999) {
            if (cm.haveItem(cube, rounds * price * multi)) {
                if (cm.haveItem(4310502, finalcost)) {
                    cm.gainItem(4310502, -finalcost);
                    cm.getPlayer().setMulti(multi);
                    cm.getPlayer().runCustomSlotShow(slot, 4000, rounds, cube, price, true);
                    cm.delayRewardNPC(4000 * rounds);
                } else {
                    cm.sendOk("You dont have enough #i4310502# to play with.");
                }
            } else {
                cm.sendOk("You dont have enough #i" + cube + "# to play with.");
            }
        } else {
            cm.sendOk("You dont have enough brains to cheat this.");
        }
        //cm.warp(450009050, "pt_back");
    }
}