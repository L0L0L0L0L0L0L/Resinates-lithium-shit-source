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
var ticketId = 5062002;
var slots = new Array(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -50, -112, -113, -115, -116);
var slot = 0;
var equip;
var equiplist;
var scrollslot = 0;
var cost = 1;
var slotcount = 0;
var cube;

var min = 0;
var max = 0;
var chance = 0;
var safe = 0;
var pscroll = 0;

var bscroll = 0;
var scroll = 0;
var amount = 0;
var count = 0;

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
        var text = "";
        //text += "#L90# #bUpgrade #i2049180##i2049181##i2049182##i2049183##i2049184##k#l\r\n";
        text += "#L91# #b#i2049185##i2049186##i2049187##i2049188##i2049189##i2049175##i2049176##k#l\r\n";
        text += "#L92# #b#i2049300##i2049301##i2049302##i2049303##i2049304##i2049305##i2049306##i2049307##k#l\r\n";
        text += "#L93# #b#i2583000##i2583001##i2583007##i2583005##i2583002##k#l\r\n";
        text += "#L94# #b#i2585000##i2585001##i2585002##i2585003##i2585004##i2585005##i2585006##i2585007##k#l\r\n";
        text += "#L95# #b#i2586000##i2586001##i2586002##i2586003##i2586004##k#l\r\n";
        text += "#L96# #b#i2340000##i2587000##i2587001##k#l\r\n";
        text += "#L960# #b#i4430001##i4430002##i4430003##i4430004##i4430005##k#l\r\n";
        text += "#L97# #b#i4420011##i4420012##i4420013##i4420014##i4420015##k#l\r\n";
        text += "#L98# #b#i4420001##i4420002##i4420003##i4420004##i4420005##i4420006##k#l\r\n";
        text += "#L99# #b#i2430130##k#l\r\n";
        //text += "#L6# #i2049301# (Price: 100 #i2049300#)#l\r\n";
        cm.sendSimple("Welcome to the #bScroll Upgrade System#k. I can upgrade old scrolls into new stronger scrolls.\r\nWhich option would you like?\r\n\r\n" + text);
    } else if (status == 1) {
        var text = "";
        if (selection == 91) {//power
            text += "#L11# #bConvert 10 #i2049185# to #i2049186##k#l\r\n";
            text += "#L12# #bConvert 20 #i2049186# to #i2049187##k#l\r\n";
            text += "#L13# #bConvert 30 #i2049187# to #i2049188##k#l\r\n";
            text += "#L14# #bConvert 40 #i2049188# to #i2049189##k#l\r\n";
            text += "#L15# #bConvert 50 #i2049189# to #i2049175##k#l\r\n";
            text += "#L16# #bConvert 100 #i2049175# to #i2049176##k#l\r\n";
        }
        if (selection == 92) {//ee
            text += "#L21# #bConvert 10 #i2049300# to #i2049301##k#l\r\n";
            text += "#L22# #bConvert 10 #i2049301# to #i2049302##k#l\r\n";
            text += "#L23# #bConvert 20 #i2049302# to #i2049303##k#l\r\n";
            text += "#L24# #bConvert 30 #i2049303# to #i2049304##k#l\r\n";
            text += "#L25# #bConvert 40 #i2049304# to #i2049305##k#l\r\n";
            text += "#L26# #bConvert 50 #i2049305# to #i2049306##k#l\r\n";
            text += "#L27# #bConvert 100 #i2049306# to #i2049307##k#l\r\n";
        }
        if (selection == 93) {//cube
            text += "#L31# #bConvert 10 #i2583000# to #i2583001##k#l\r\n";
            text += "#L32# #bConvert 25 #i2583001# to #i2583007##k#l\r\n";
            text += "#L33# #bConvert 50 #i2583007# to #i2583005##k#l\r\n";
            text += "#L34# #bConvert 100 #i2583005# to #i2583002##k#l\r\n";
        }
        if (selection == 94) {//shard
            text += "#L41# #bConvert 10 #i2585000# to #i2585001##k#l\r\n";
            text += "#L42# #bConvert 20 #i2585001# to #i2585002##k#l\r\n";
            text += "#L43# #bConvert 30 #i2585002# to #i2585003##k#l\r\n";
            text += "#L44# #bConvert 40 #i2585003# to #i2585004##k#l\r\n";
            text += "#L45# #bConvert 50 #i2585004# to #i2585005##k#l\r\n";
            text += "#L46# #bConvert 75 #i2585005# to #i2585006##k#l\r\n";
            text += "#L47# #bConvert 100 #i2585006# to #i2585007##k#l\r\n";
        }
        if (selection == 95) {//gems
            text += "#L51# #bConvert 10 #i2586000# to #i2586001##k#l\r\n";
            text += "#L52# #bConvert 25 #i2586001# to #i2586002##k#l\r\n";
            text += "#L53# #bConvert 50 #i2586002# to #i2586003##k#l\r\n";
            text += "#L54# #bConvert 100 #i2586003# to #i2586004##k#l\r\n";
        }
        if (selection == 96) {//p scrolls
            text += "#L61# #bConvert 10 #i2340000# to #i2587000##k#l\r\n";
            text += "#L62# #bConvert 100 #i2587000# to #i2587001##k#l\r\n";
        }
        if (selection == 960) {//bait
            text += "#L65# #bConvert 10 #i4430001# to #i4430002##k#l\r\n";
            text += "#L66# #bConvert 25 #i4430002# to #i4430003##k#l\r\n";
            text += "#L67# #bConvert 50 #i4430003# to #i4430004##k#l\r\n";
            text += "#L68# #bConvert 100 #i4430004# to #i4430005##k#l\r\n";
        }
        if (selection == 97) {//rewards
            text += "#L71# #bConvert 10 #i4420011# to #i4420012##k#l\r\n";
            text += "#L72# #bConvert 25 #i4420012# to #i4420013##k#l\r\n";
            text += "#L73# #bConvert 50 #i4420013# to #i4420014##k#l\r\n";
            text += "#L74# #bConvert 100 #i4420014# to #i4420015##k#l\r\n";
        }
        if (selection == 98) {//skin tickets
            text += "#L81# #bConvert 10 #i4420001# to #i4420002##k#l\r\n";
            text += "#L82# #bConvert 15 #i4420002# to #i4420003##k#l\r\n";
            text += "#L83# #bConvert 20 #i4420003# to #i4420004##k#l\r\n";
            text += "#L84# #bConvert 25 #i4420004# to #i4420005##k#l\r\n";
            text += "#L85# #bConvert 100 #i4420005# to #i4420006##k#l\r\n";
        }
        if (selection == 99) {//boosters
            text += "#L89# #bConvert 100 #i2430131# to #i2430130##k#l\r\n";
        }
        //text += "#L6# #i2049301# (Price: 100 #i2049300#)#l\r\n";
        cm.sendSimple("Which scroll would you like to upgrade?\r\n\r\n" + text);
    } else if (status == 2) {
        //PS
        if (selection == 11) {
            bscroll = 2049185;
            scroll = 2049186;
            amount = 10;
        }
        if (selection == 12) {
            bscroll = 2049186;
            scroll = 2049187;
            amount = 20;
        }
        if (selection == 13) {
            bscroll = 2049187;
            scroll = 2049188;
            amount = 30;
        }
        if (selection == 14) {
            bscroll = 2049188;
            scroll = 2049189;
            amount = 40;
        }
        if (selection == 15) {
            bscroll = 2049189;
            scroll = 2049175;
            amount = 50;
        }
        if (selection == 16) {
            bscroll = 2049175;
            scroll = 2049176;
            amount = 100;
        }
        //EE
        if (selection == 21) {
            bscroll = 2049300;
            scroll = 2049301;
            amount = 10;
        }
        if (selection == 22) {
            bscroll = 2049301;
            scroll = 2049302;
            amount = 10;
        }
        if (selection == 23) {
            bscroll = 2049302;
            scroll = 2049303;
            amount = 20;
        }
        if (selection == 24) {
            bscroll = 2049303;
            scroll = 2049304;
            amount = 30;
        }
        if (selection == 25) {
            bscroll = 2049304;
            scroll = 2049305;
            amount = 40;
        }
        if (selection == 26) {
            bscroll = 2049305;
            scroll = 2049306;
            amount = 50;
        }
        if (selection == 27) {
            bscroll = 2049306;
            scroll = 2049307;
            amount = 100;
        }
        //cubes
        if (selection == 31) {
            bscroll = 2583000;
            scroll = 2583001;
            amount = 10;
        }
        if (selection == 32) {
            bscroll = 2583001;
            scroll = 2583007;
            amount = 25;
        }
        if (selection == 33) {
            bscroll = 2583007;
            scroll = 2583005;
            amount = 50;
        }
        if (selection == 34) {
            bscroll = 2583005;
            scroll = 2583002;
            amount = 100;
        }
        //shards
        if (selection == 41) {
            bscroll = 2585000;
            scroll = 2585001;
            amount = 10;
        }
        if (selection == 42) {
            bscroll = 2585001;
            scroll = 2585002;
            amount = 20;
        }
        if (selection == 43) {
            bscroll = 2585002;
            scroll = 2585003;
            amount = 30;
        }
        if (selection == 44) {
            bscroll = 2585003;
            scroll = 2585004;
            amount = 40;
        }
        if (selection == 45) {
            bscroll = 2585004;
            scroll = 2585005;
            amount = 50;
        }
        if (selection == 46) {
            bscroll = 2585005;
            scroll = 2585006;
            amount = 75;
        }
        if (selection == 47) {
            bscroll = 2585006;
            scroll = 2585007;
            amount = 100;
        }
        //gems
        if (selection == 51) {
            bscroll = 2586000;
            scroll = 2586001;
            amount = 10;
        }
        if (selection == 52) {
            bscroll = 2586001;
            scroll = 2586002;
            amount = 25;
        }
        if (selection == 53) {
            bscroll = 2586002;
            scroll = 2586003;
            amount = 50;
        }
        if (selection == 54) {
            bscroll = 2586003;
            scroll = 2586004;
            amount = 100;
        }
        //p-scrolls
        if (selection == 61) {
            bscroll = 2340000;
            scroll = 2587000;
            amount = 10;
        }
        if (selection == 62) {
            bscroll = 2587000;
            scroll = 2587001;
            amount = 100;
        }
        //bait
        if (selection == 65) {
            bscroll = 4430001;
            scroll = 4430002;
            amount = 10;
        }
        if (selection == 66) {
            bscroll = 4430002;
            scroll = 4430003;
            amount = 25;
        }
        if (selection == 67) {
            bscroll = 4430003;
            scroll = 4430004;
            amount = 50;
        }
        if (selection == 68) {
            bscroll = 4430004;
            scroll = 4430005;
            amount = 100;
        }
        //rewaards
        if (selection == 71) {
            bscroll = 4420011;
            scroll = 4420012;
            amount = 10;
        }
        if (selection == 72) {
            bscroll = 4420012;
            scroll = 4420013;
            amount = 25;
        }
        if (selection == 73) {
            bscroll = 4420013;
            scroll = 4420014;
            amount = 50;
        }
        if (selection == 74) {
            bscroll = 4420014;
            scroll = 4420015;
            amount = 100;
        }
        //skins
        if (selection == 81) {
            bscroll = 4420001;
            scroll = 4420002;
            amount = 10;
        }
        if (selection == 82) {
            bscroll = 4420002;
            scroll = 4420003;
            amount = 15;
        }
        if (selection == 83) {
            bscroll = 4420003;
            scroll = 4420004;
            amount = 20;
        }
        if (selection == 84) {
            bscroll = 4420004;
            scroll = 4420005;
            amount = 25;
        }
        if (selection == 85) {
            bscroll = 4420005;
            scroll = 4420006;
            amount = 50;
        }
        if (selection == 86) {
            bscroll = 4420006;
            scroll = 4420007;
            amount = 100;
        }
        if (selection == 89) {
            bscroll = 2430131;
            scroll = 2430130;
            amount = 100;
        }
        cm.sendGetText("How many #i" + scroll + "# do you wish to craft?\r\n\r\n");
    } else if (status == 3) {
        count = cm.getNumber();
        var total = count * amount;
        if (total > 0) {
            if (count <= 9999) {
                if (cm.haveItem(bscroll, total)) {
                    if (cm.canHold(scroll, count)) {
                        cm.gainItem(bscroll, -total);
                        cm.gainItem(scroll, count);
                        cm.sendOk("You successfully crafted " + count + "x #i" + scroll + "#'s");
                    } else {
                        cm.sendOk("You currently do not have enough room.");
                    }
                } else {
                    cm.sendOk("You currently do not have enough Scrolls.\r\n#rRequires " + total + "x - #i" + bscroll + "#s#k");
                }
            } else {
                cm.sendOk("Max number to exchange is 9999.");
            }
        } else {
            cm.sendOk("You currently do not have enough Scrolls.");
        }
    }
}


