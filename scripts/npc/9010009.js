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
var qr = "#fUI/Custom.img/crypto/QR#"
var status = 0;
var password = 0;
var trace = 0;

var bonus = 1;

var codeinfo;

var reward = 4033320;
var rewamount = 1;
var box = 0;
var amount = 0;
var Cantidad;

function start() {
    var text = "If you wish to donate and support the server select the first option.\r\n\r\n";
    //text += "#L1##bSellix Shop#k#l\r\n";
    if (bonus > 1) {
        //text += "#L2##rCash in Special Code#k (#bSALE: " + (bonus * 100) + "%#k)#l\r\n";
    } else {
        //text += "#L2##rCash in Special Code#k#l\r\n";
    }
    //setLevel(newLvl)
    text += "#L21##bDiscord#k#l\r\n";
    text += "#L11##bCrypto Donation (ETH QR)#k#l\r\n";
    text += "#L4##bInfinity Maple Shop#k#l\r\n";
    text += "#L7##bBuy Chaos Damage/Hour#k#l\r\n";
    //text += "#L3##rExchange Packages for Rewards#b#l\r\n";
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
            cm.sendYesNo("Donating through Sellix rewards 1x $ to IP ratio.\r\n#bIf you can donate VIA crypto for 250%+ Deals, please speak with staff in discord to help you.#k \r\nIf you cannot donate crypto, Please Click #rYES#k here.");
        }
        if (option == 11) {
            cm.sendOk(qr + "\r\n\r\n\r\n#rScan QR below to donate ETH#k\r\nETH-Address\r\n#b0x90A3903447344D613348E95d0413342dB21b7311#k\r\nContact #rResinate (767761048156766209)#k in Discord\r\nonce your Crypto has sent with the transaction log or receipt.");
        }
        if (option == 2) {
            var text = "";
            if (bonus > 1) {
                text = "#rClaim Sale is Active\r\nAll Claim Rewards are " + bonus + "x\r\n";
            }
            cm.sendGetText("Welcome to Sellix Redeemer Shop.\r\nPlease enter your #bSellix Code#k\r\n" + text);
        }
        if (option == 8) {
            var text = "";
            cm.sendGetText("You can convert #b1#k #i4310504# into #r25#k #i4000999#.\r\n#rHow many DP would you like to use?#k\r\n ");
        }
        if (option == 10) {
            var eMod = cm.getPlayer().getAccVara("Etc_Mod");
            var text = "";
            cm.sendGetText("You can expand Etc% Cap with #b1#k #i4037176# into #r+25% Etc%#k .\r\nThese can be bought in DP shop for 5 DP each.\r\n\Current Etc% Cap: #r+" + eMod + "%#k\r\n#rMax Etc% Cap that can be bought is 50,000%#k\r\n#rHow many would you like to use?#k\r\n");
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
            if (cm.haveItem(2400010)) {
                text += "#L10##i2400010##l  ";
                boxes++;
            }
            if (cm.haveItem(2400011)) {
                text += "#L11##i2400011##l  ";
                boxes++;
            }
            if (cm.haveItem(2400012)) {
                text += "#L12##i2400012##l  ";
                boxes++;
            }
            if (boxes > 0) {
                cm.sendSimple("Welcome to the Package Exchange.\r\nHow may I help you?\r\n\r\n" + text);
            } else {
                cm.sendOk("You dont seem to have any reward boxes.");
            }
        }
        if (option == 4) {
            cm.openShop(10741);
        }
        if (option == 5) {
            cm.sendYesNo("Kaotic Boosts increases the amount of Kaotic Power gained per equip dropped on bosses.\r\n#b1 Kaotic Boost = +1 KP Gained per Equip Dropped#k\r\n#rThis boost is PERM and cannot be undone!#k\r\n#bDo you wish to buy Kaotic Boost?#k");
        }
        if (option == 6) {
            cm.sendGetText("How many Minutes of Kaotic Buff do you wish to buy?\r\n#rThis action cannot be undone or refunded#k");
        }
        if (option == 7) {
            cm.sendGetText("How many Rates do you want to buy?\r\n#rEach Rate costs 1 " + cm.getItemName(4310505) + "s#k\r\n#rThis action cannot be undone or refunded#k\r\n#rMax pull from Chaos Bank is +99 (Max: 100x)#k\r\nEach Point is worth 1x Damage.");
        }
        if (option == 9) {
            cm.sendGetText("How many #bLevels#k do you want to buy?\r\n#rEach Rate costs 1 " + cm.getItemName(4310504) + " per level upto level 9000#k\r\n#rThis action cannot be undone or refunded#k");
        }
        if (option == 21) {
            cm.openLink("https://discord.gg/n5tTCzFHWG");
            cm.dispose();
        }
    } else if (status == 2) {
        if (option == 1) {
            cm.openLink("https://kaoticgaming.mysellix.io/");
            cm.dispose();
        }
        if (option == 2) {
            codeinfo = cm.checkSellixCode(cm.getText());
            if (codeinfo != null) {
                cm.sendYesNo("Do you want to redeem sellix code:\r\n\"#r" + cm.getText() + "#k\"\r\n#bReward#k: #bx" + codeinfo.amount() + " " + cm.getItemName(codeinfo.itemId()) + "s#k ");
            } else {
                cm.sendOkS("#rThis Code does not exist#k.", 16);
            }
        }
        if (option == 8) {
            trace = cm.getNumber();
            if (trace > 0) {
                cm.sendYesNo("Are you sure you want to convert\r\n#b" + trace + "#k #i4310504# for #r" + (trace * 25) + "#k #i4000999#?");
            } else {
                cm.sendOkS("#rThis Code does not exist#k.", 16);
            }
        }
        if (option == 9) {
            trace = cm.getNumber();
            if (trace > 0) {
                var cap = 9000 - cm.getPlayer().getTotalLevel();
                if (trace > cap) {
                    trace = cap;
                }
                cm.sendYesNo("Are you sure you want to spend\r\n#b" + trace + "#k #i4310504# for #r" + (trace) + "#k Levels?\r\n#rWarning: There is no AP SP HP or MP gains from this system.#k");
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
        if (option == 5) {
            if (cm.haveItem(4310504, 100)) {
                cm.gainItem(4310504, -100);
                cm.getPlayer().addAccVar("KP_BOOST", 1);
                cm.sendOk("You have gained Kaotic Boost.\r\n#bKaotic Boost Rate: +" + cm.getPlayer().getAccVara("KP_BOOST") + "#k");
            } else {
                cm.sendOk("You dont seem to have enough #i4310504#s.");
            }
        }
        if (option == 6) {
            amount = cm.getNumber();
            if (cm.haveItem(4310504, amount)) {
                cm.sendYesNo("Do you want to confirm that you wish to spend " + amount + " #i4310504# on Server Kaotic Buff?");
            } else {
                cm.sendOk("You dont seem to have enough #i4310504#s.");
            }
        }
        if (option == 7) {
            amount = cm.getNumber();
            if (cm.haveItem(4310505, amount)) {
                cm.sendYesNo("Do you want to confirm that you wish to spend\r\n#r" + amount + " #i4310505# " + cm.getItemName(4310505) + "#k on #bChaos Damage/Hour Bonus#k?");
            } else {
                cm.sendOk("You dont seem to have enough #i4310505#s.");
            }
        }
        if (option == 10) {
            amount = cm.getNumber();
            if (cm.haveItem(4037176, amount)) {
                cm.sendYesNo("Do you want to confirm that you wish to spend\r\n#r" + amount + " #i4037176# " + cm.getItemName(4037176) + "#k on #bExpanding Etc% Cap#k?");
            } else {
                cm.sendOk("You dont seem to have enough #i4037176#s.");
            }
        }
    } else if (status == 3) {
        if (option == 2) {
            if (codeinfo != null) {
                if (cm.canHold(codeinfo.itemId(), codeinfo.amount())) {
                    if (cm.tryRedeemCode(codeinfo)) {
                        var tip = codeinfo.amount() * bonus;
                        var tid = codeinfo.itemId();
                        cm.gainItem(tid, tip);
                        var players = cm.getChannelServer().getPlayerStorage().getAllCharacters();
                        if (tip >= 10) {
                            var sip = Math.floor(tip * 0.1);
                            for (var i = 0; i < players.size(); i++) {
                                var player = players.get(i);
                                player.gainItem(tid, sip);
                                player.dropMessage(1, "You have gained " + sip + " Infinity Points from " + cm.getPlayer().getName() + "\r\nThese Free Points are not refundable.");
                            }
                        }
                        cm.systemMsg(cm.getPlayer().getName() + " has redeemed " + tip + " " + cm.getItemName(tid));
                        cm.sendOkS("#bYou have successfuly redeemed\r\n\#k#r" + codeinfo.code() + "#k\r\n\#bRecieved: " + codeinfo.amount() + " " + cm.getItemName(codeinfo.itemId()) + "s#k.", 16);
                    } else {
                        cm.sendOkS("#rSellix Code has already been redeemed#k.", 16);
                    }
                } else {
                    cm.sendOkS("#rYou do not have enough room to carry this item.#k.", 16);
                }
            }
        }
        if (option == 8) {
            if (cm.haveItem(4310504, trace)) {
                cm.gainItem(4310504, -trace);
                cm.getPlayer().addOverflow(4000999, trace * 25, "Converted from DP - Duey");
                cm.sendOk("You have gained " + (trace * 25) + "x #i4000999#.\r\n#bSpell Traces have been placed inside overflow#k.");
            } else {
                cm.sendOk("You dont seem to have enough #i4310504#s.");
            }
        }
        if (option == 9) {
            if (cm.haveItem(4310504, trace)) {
                cm.gainItem(4310504, -trace);
                cm.gainItem(2430131, trace * 5);
                cm.getPlayer().addLevels(trace, 9000);
                cm.sendOk("You have gained " + (trace) + " Levels and Energy Charges.");
            } else {
                cm.sendOk("You dont seem to have enough #i4310504#s.");
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
        if (option == 6) {
            if (cm.haveItem(4310504, amount)) {
                cm.gainItem(4310504, -amount);
                cm.addKaoticPowerTime(amount);
                cm.sendOk("Server has gained " + amount + " Minutes of Kaotic Buff.");
            } else {
                cm.sendOk("You dont seem to have enough #i4310504#s.");
            }
        }
        if (option == 7) {
            if (cm.getDonationRate() >= 5) {
                if (cm.haveItem(4310505, amount)) {
                    cm.gainItem(4310505, -(amount));
                    cm.addServerVar("donation_rate", cm.getServerVar("donation_rate") + amount);
                    cm.sendOk("Chaos Hour Bank has gained " + amount + " Rates.");
                } else {
                    cm.sendOk("You dont seem to have enough #i4310505#s.");
                }
            } else {
                cm.sendOk("This system is currently closed. Try again after chaos rates update.");
            }
        }
        if (option == 10) {
            if (cm.haveItem(4037176, amount)) {
                cm.gainItem(4037176, -amount);
                cm.getPlayer().setAccVar("Etc_Mod", cm.getPlayer().getAccVara("Etc_Mod") + (amount * 25));
                var eMod = cm.getPlayer().getAccVara("Etc_Mod");
                cm.sendOk("You Etc% bonus cap is now set to #r+" + eMod + "%#k");
            } else {
                cm.sendOk("You dont seem to have enough #i4037176#s.");
            }
        }
    } else if (status == 4) {
        if (option == 3) {
            if (cm.haveItem(box, amount)) {

                var text = "Rewards gained from " + amount + " #i" + box + "#:\r\n\r\n";
                var atotal = amount;
                if (box == 2400000) {//starter
                    if (cm.getPlayer().canHold(4310502, 100 * atotal) && cm.getPlayer().canHold(2005107, atotal) && cm.getPlayer().canHold(2005000, atotal) && cm.getPlayer().canHold(4420021, 5 * atotal)) {
                        var item = 4310502, count = 100;//DP
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        item = 2005107, count = 1;//apple
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        item = 2005000, count = 1;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        var item = 4420021, count = 5;
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


                if (box == 2400010) {//basic
                    if (cm.getPlayer().canHold(4310502, 250 * atotal) && cm.getPlayer().canHold(5052000, atotal) && cm.getPlayer().canHold(4420007, atotal) && cm.getPlayer().canHold(4420021, 10 * atotal)) {
                        var item = 4310502, count = 250;//DP
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        item = 5052000, count = 1;//apple
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        item = 4420007, count = 1;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        var item = 4420021, count = 5;
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        cm.gainItem(box, -amount);
                    } else {
                        text = "#rYou do not have enough room to hold all the items, Make space.";
                    }
                }
                if (box == 2400011) {//mega
                    if (cm.getPlayer().canHold(4310502, 500 * atotal) && cm.getPlayer().canHold(5052000, 5 * atotal) && cm.getPlayer().canHold(4420007, atotal)) {
                        var item = 4310502, count = 500;//DP
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        item = 5052000, count = 5;//pot
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        item = 4420007, count = 1;//supreme
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        cm.gainItem(box, -amount);
                    } else {
                        text = "#rYou do not have enough room to hold all the items, Make space.";
                    }
                }
                if (box == 2400012) {//Elite
                    if (cm.getPlayer().canHold(4310502, 1000 * atotal) && cm.getPlayer().canHold(5052000, 10 * atotal) && cm.getPlayer().canHold(4000999, 250 * atotal)) {
                        var item = 4310502, count = 1000;//DP
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        item = 5052000, count = 10;//pot
                        cm.gainItem(item, count * atotal);
                        text += "#b#i" + item + "# #t" + item + "# " + (count * atotal) + "x#k\r\n";

                        item = 4000999, count = 250;//trace
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