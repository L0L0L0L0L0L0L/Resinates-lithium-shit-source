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
var amount;
var slotcount = 0;
var stake;
var option;

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
        stake = cm.getPlayer().getBaseStake();
        if (stake != null) {
            option = 0;
            var earned = cm.getPlayer().getFinalStake().getLeft() - cm.getPlayer().getBaseStake().getLeft();
            if (earned < 5000) {
                var interval = (1000 * 60 * 60 * 12);
                var timeLeft = cm.getPlayer().getFinalStake().getRight();
                timeLeft = interval - (Date.now() - timeLeft) % interval;
                cm.sendYesNo("You currently have " + cm.getPlayer().getBaseStake().getLeft() + " #i" + ticketId + " staked.\r\n\r\n\You have earned #g+" + earned + "#k Donation Points\r\n\Time Remaining until Interest Reward \r\n\#b" + cm.durationToString(timeLeft) + "#k\r\n\Would you like to withdrawl all of your Staked and Reward Donation Points?\r\n");
            } else {
                cm.sendYesNo("Your Stake Pool is currently full with " + cm.getPlayer().getBaseStake().getLeft() + " Donation Points staked. Would you like to withdrawl your Staked Donation Points?\r\n");
            }
        } else {
            if (cm.haveItem(ticketId, 1)) {
                option = 1;
                cm.sendGetText("You currently do not have any Donation Points Staked.\r\n\How many Donation Points would you like to stake?\r\n\r\n\#rEvery 12 Hours from starting stake time will reward 1% of Staked DP.\r\n#rMax staking amount is 5000 DP.#k\r\n ");
            } else {
                cm.sendOk("You currently do not have Donation Points to Stake.");
            }
        }

    } else if (status == 1) {
        if (option == 0) {
            var finalAmount = cm.getPlayer().getFinalStake().getLeft();
            cm.getPlayer().withdrawDP();
            cm.sendOk("You have withdrew a total of " + finalAmount + " Donation Points.");
        } else {
            amount = cm.getNumber();
            if (amount >= 1 && amount <= 5000 && cm.haveItem(4310502, amount)) {
                cm.sendYesNo("You are sure that you want to stake " + amount + " Donation Points?");
            } else {
                if (amount > 5000) {
                    cm.sendOk("#rMaximum Staking amount is 5000 #i" + ticketId + "##k.");
                } else {
                    if (amount < 1) {
                        cm.sendOk("#rMinimum Staking amount is 1 #i" + ticketId + "##k.");
                    } else {
                        cm.sendOk("You currently do not have " + amount + " Donation Points to Stake.");
                    }
                }
            }
        }
    } else if (status == 2) {
        if (amount > 0 && cm.haveItem(ticketId, amount)) {
            cm.getPlayer().stakeDP(amount);
            cm.gainItem(4310502, -amount);//gain dp
            var interval = (1000 * 60 * 60 * 12);
            var timeLeft = cm.getPlayer().getFinalStake().getRight();
            timeLeft = interval - (Date.now() - timeLeft) % interval;
            cm.sendOk("You have staked " + finalAmount + " Donation Points. \r\n Time Remaining until Interest Reward \r\n\#b" + cm.durationToString(timeLeft) + "#k.");
        } else {
            cm.sendOk("You currently do not have " + amount + " Donation Points to Stake.");
        }
    }
}


//cm.convertNumber(cm.getPlayer().getRemainingAp())