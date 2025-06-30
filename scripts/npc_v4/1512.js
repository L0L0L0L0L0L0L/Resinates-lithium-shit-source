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
var items = new Array(4001087, 4001088, 4001089, 4001090, 4001091);
var ivs = new Array("Hp", "Str", "Dex", "Int", "Luk", "Attack", "Magic Attack", "Defense", "Magic Defense");
var stats = new Array("Hp", "Str", "Dex", "Int", "Luk", "Attack", "Defense", "Magic Attack", "Magic Defense");
var park = new Array("Meadow");
var dung = new Array("Forest");
var deep_dung = new Array("Forest");
var amount = 99;
var reward = 4001063;
var rewamount = 10;
var exp = 250000;
var questid = 6152;
var questtime = 1800;//60 = 1 min
var job = "thieves";
var option = 0;
var option2 = 0;
var star = "#fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var bigstar = "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
var pals;
var pals_storage;
var players;
var pal;
var acc = 0;
var energy = 0;
var eAmount = 0;
var upgrade = 0;
var uAmount = 0;
var exp = 0;
var IV = 0;
var scroll = 0;
var recv;
var male;
var female;
var berry;
var em;

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
    if (status == 0) {//-------------------------------------------------------------------------------------------
        //cm.sendOk("Seems you cannot hold any more eggs.");
        var text = "Hello there I am Professor Berry, how can I help you?\r\n";
        text += "#rSelect an Option#k:\r\n";
        text += "#L0#" + star + "#rWhat is Maple Pals Breeding?#k#l\r\n";
        text += "#L1#" + star + "#bCheck on my Maple Pal Storage#k#l\r\n";
        text += "#L2#" + star + "#bHatch my Babies#k (#rEGGS#k)#l\r\n";
        text += "#L3#" + star + "#rBreed my Pals#k#l\r\n";

        //text += "#L99#DEBUG PALS#l\r\n";
        cm.sendSimple(text);
    } else if (status == 1) {//-------------------------------------------------------------------------------------------
        option = selection;
        if (option == 0) {
            var text = "Hello there I am Professor Beery, and I am in charge of entire Pal Breeding System.\r\n";
            text += bigstar + "#rWhat is Maple Pals Breeding?#k\r\n";
            text += star + "Breeding system is method of mating 2 Pals Male-Female to create perfect babies.\r\n\r\n";
            text += bigstar + "#rWhat kind of the Babies can be obtained from breeding?#k\r\n";
            text += star + "Babies will either spawn from male or female parent.\r\n\r\n";
            text += bigstar + "#rWhat do the Babies gain from breeding?#k\r\n";
            text += star + "Babies gain IV's and Passives of parents at random. Empty passives will generate new passives.\r\n\r\n";
            text += bigstar + "#rWhat do I need to breed my Pals?#k\r\n";
            text += star + "You will need #bMale#k and #rFemale#k pals, and some fancy Berries.\r\n\r\n";
            cm.sendOk(text);
        }
        if (option == 1) {
            cm.OpenPalWindow(cm.getPlayer());
            cm.dispose();
        }
        if (option == 2) {
            cm.OpenHatchWindow(cm.getPlayer());
            cm.dispose();
        }
        if (option == 3) {
            pals = cm.getPlayer().getPalStorage().getPals(true);
            if (pals.size() > 0) {
                var selStr = "";
                for (var i = 0; i < pals.size(); i++) {
                    var cpal = pals.get(i);
                    if (cpal != null) {
                        selStr += "#L" + i + "##fUI/Custom.img/shared/gender/" + cpal.gender + "##fUI/Custom.img/shared/element/" + cpal.getElement() + "# #b" + cpal.getName() + "#k Level: #b" + cpal.getLevel() + "#k#l\r\n\ ";
                    }
                }
                cm.sendSimple("Which Male Pal would you like to breed?\r\n\ " + selStr);
            } else {
                cm.sendOk("You dont have any male pals");
            }
        }
    } else if (status == 2) {//-------------------------------------------------------------------------------------------
        if (option == 3) {
            male = pals.get(selection);
            pals = cm.getPlayer().getPalStorage().getPals(false);
            if (pals.size() > 0) {
                var selStr = "";
                for (var i = 0; i < pals.size(); i++) {
                    var cpal = pals.get(i);
                    if (cpal != null) {
                        selStr += "#L" + i + "##fUI/Custom.img/shared/gender/" + cpal.gender + "##fUI/Custom.img/shared/element/" + cpal.getElement() + "# #b" + cpal.getName() + "#k Level: #b" + cpal.getLevel() + "#k#l\r\n\ ";
                    }
                }
                cm.sendSimple("Which Female Pal would you like to breed?\r\n\ " + selStr);
            } else {
                cm.sendOk("You dont have any male pals");
            }
        }
    } else if (status == 3) {//-------------------------------------------------------------------------------------------
        if (option == 3) {
            female = pals.get(selection);
            var selStr = "Do you wish to Breed the following pals?\r\n\r\n";
            selStr += "#fUI/Custom.img/shared/gender/" + male.gender + "##fUI/Custom.img/shared/element/" + male.getElement() + "# #b" + male.getName() + "#k with ";
            selStr += "#fUI/Custom.img/shared/gender/" + female.gender + "##fUI/Custom.img/shared/element/" + female.getElement() + "# #b" + female.getName() + "#k\r\n";
            var evo = male.getEvo();
            if (female.getEvo() > male.getEvo()) {
                evo = female.getEvo();
            }
            berry = 4200500 + evo;
            selStr += "\r\n";
            selStr += "This Breeding pair requires #i" + berry + "# #t" + berry + "#\r\n";
            var berries = cm.getPlayer().countAllItem(berry);
            selStr += "You currently have #b" + cm.convertNumber(berries) + "#k berries.";
            if (berries > 0) {
                cm.sendYesNo(selStr);
            } else {
                selStr += "\r\n#rYou currently do not have enough berries#k.";
                cm.sendOk(selStr);
            }
        }
    } else if (status == 4) {//-------------------------------------------------------------------------------------------
        if (cm.haveItem(berry, 1)) {
            if (cm.getPlayer().createEggBaby(male, female)) {
                cm.gainItem(berry, -1);
                cm.sendOk("A new egg has been created and placed in the hatchery.");
            } else {
                cm.sendOk("Not enough room in hatchery for more eggs.");
            }
        } else {
            cm.sendOk("You got no berries.");
        }

    } else if (status == 5) {
    } else if (status == 6) {
    } else if (status == 7) {
    } else if (status == 8) {
    } else if (status == 9) {
    } else if (status == 10) {
    } else {
        cm.dispose();
    }
}


