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
        var simp = cm.getPlayer().getAccVara("simple_battle") > 0;
        var wins = cm.getPlayer().getAccVara("Pal_Win");
        var loss = cm.getPlayer().getAccVara("Pal_Loss");
        var cred = cm.getPlayer().getAccVara("Pal_Credits");
        var mlvl = cm.getPlayer().getAccVara("Pal_Level");
        var bMode = cm.getPlayer().getBattleMode();
        var text = "Hello there I am Professor Oak, how can I help you?\r\n#rAny Mistakes you make with my services will not be refunded.#k\r\nWins: #b" + wins + "#k - Losses: #r" + loss + "#k - Credits: #b" + cred + "#k\r\n";
        text += "#rSelect an Option#k:\r\n";
        text += "#L0#" + star + "#rWhat is Maple Pals?#k#l\r\n";
        text += "#L1#" + star + "#bCheck on my Maple Pal Storage#k#l\r\n";
        text += "#L10#" + star + "#bActive Pals Extra Options#k#l\r\n";
        text += "#L2#" + star + "#bHatch my Babies#k (#rEGGS#k)#l\r\n";
        text += "#L3#" + star + "#bGive me an egg#k (#rTime Limited#k)#l\r\n";
        text += "#L4#" + star + "#bUnlock more#k #rPal Slots#k#l\r\n";
        text += "#L5#" + star + "#rSend a Pal to Player#k#l\r\n";
        text += "#L96#" + star + "#rToggle Battle Mode#k (" + (bMode ? "#gON#k" : "#rOFF#k") + ")#l\r\n";
        text += "#L95#" + star + "#rSet Battle Lvl Limit#k (#r" + mlvl + "#k)#l\r\n";
        //text += "#L97#" + star + "#rToggle Simple Battles#k (" + (simp ? "#gON#k" : "#rOFF#k") + ")#l\r\n";
        text += "#L98#" + star + "#rBattle Simulation Room#k#l\r\n";
        text += "#L94#" + star + "#rSave Pal's Active Positions#k#l\r\n";

        //text += "#L99#DEBUG PALS#l\r\n";
        cm.sendSimple(text);
    } else if (status == 1) {//-------------------------------------------------------------------------------------------
        option = selection;
        if (option == 0) {
            if (cm.getPlayer().achievementFinished(3000)) {
                cm.getPlayer().finishAchievement(3001);
            }
            var text = "Hello there I am Professor Oak, and I am in charge of entire Pal World.\r\n";
            text += bigstar + "#rWhat are Maple Pals?#k\r\n";
            text += star + "Maple Pal are creatures that I have created using DNA technogoly with Maple Monsters.\r\n\r\n";
            text += bigstar + "#rWhat can I do with Maple Pals?#k\r\n";
            text += star + "Maple Pals can be captured then hatched with my hatching system. If you come across having too many pals, you can recycle them inside my Pal Storage System using #bShift+Right Click#k. The Energies you obtain from them can then be applied to pal you want to make stronger.\r\n\r\n";
            text += bigstar + "#rHow can I obtain Maple Pals?#k\r\n";
            text += star + "Maple Pal Egg can be found in the wild or by speaking with Prof Elm for his pal dungeons. Simply attack the eggs to capture them. Bring them to me to Hatch them for you.\r\n\r\n";
            text += bigstar + "#rHow can I Battle my Maple Pals?#k\r\n";
            text += star + "You can challenge various #rNPCs#k all over the world, you can speak to me to force MOST npcs to battle you.\r\n\r\n";
            text += bigstar + "#rCan I battle other Players?#k\r\n";
            text += star + "Yes you can challenge other players. However there are rules with this. First, If you win you can steal #b5%#k of that players #bPal Credits#k, However if you lose, they steal #b1%#k of your credits. There is a 1 hour cooldown for dueling the same person.\r\n*Note Pal Credits have no direct use in this world.\r\n\r\n";
            cm.sendOk(text);
        }
        if (option == 94) {
            cm.getPlayer().saveActivePals();
            cm.sendOk("Pal Settings saved");
            return;
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
            if (cm.getPlayer().getQuestLock(questid) > 0) {
                cm.sendOk("Come see me after #b" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k \r\n\      to repeat my quest again.\r\n\ Quest ID: " + questid);
            } else {
                if (cm.getPlayer().canHoldEgg()) {
                    cm.getPlayer().makeEggEvo(cm.random(1, 4), cm.random(1, cm.getPlayer().getTotalLevel()));
                    cm.getPlayer().setQuestLock(questid, questtime);
                    cm.sendOk("Here is an egg. Come back in little bit for more eggs.");
                } else {
                    cm.sendOk("Seems you cannot hold any more eggs.");
                }
            }
        }
        if (option == 4) {
            if (cm.haveItem(4202015, 1) || cm.haveItem(4202016, 1)) {
                var text = "";
                if (cm.haveItem(4202015, 1)) {
                    text += "#L4202015##i4202015##bExpand 9 Pal Storage Slots#k#l\r\n";
                }
                if (cm.haveItem(4202016, 1)) {
                    text += "#L4202016##i4202016##bExpand 7 Incubation Slots#k#l\r\n";
                }
                cm.sendSimple(text);
            } else {
                cm.sendOk("Bring me either #i4202016# or #i4202015# to upgrade your hatching slots.");
            }
        }
        if (option == 99) {
            cm.getPlayer().makeEggEvo(3, cm.random(1, cm.getPlayer().getTotalLevel()));
            cm.getPlayer().makeEggEvo(3, cm.random(1, cm.getPlayer().getTotalLevel()));
            cm.getPlayer().makeEggEvo(3, cm.random(1, cm.getPlayer().getTotalLevel()));
            cm.getPlayer().makeEggEvo(3, cm.random(1, cm.getPlayer().getTotalLevel()));
            cm.getPlayer().makeEggEvo(3, cm.random(1, cm.getPlayer().getTotalLevel()));
            cm.getPlayer().makeEggEvo(3, cm.random(1, cm.getPlayer().getTotalLevel()));
            cm.sendOk("pals created");
        }
        if (option == 98) {
            cm.warp(1000);
            //cm.startRandomBattle(50, false);
            cm.dispose();
            return;
        }
        if (option == 10) {
            pals = cm.getPlayer().getPalStorage().getActivePals();
            if (pals.size() > 0) {
                var selStr = "";
                for (var i = 0; i < pals.size(); i++) {
                    var cpal = pals.get(i);
                    if (cpal != null) {
                        if (cm.getPlayer().isActivePal(cpal)) {
                            selStr += "#L" + i + "# #fUI/Custom.img/shared/element/" + cpal.getElement() + "# #b" + cpal.getName() + "#k (#rMain Active#k)#l\r\n\ ";
                        } else {
                            selStr += "#L" + i + "# #fUI/Custom.img/shared/element/" + cpal.getElement() + "# #b" + cpal.getName() + "#k #l\r\n\ ";
                        }
                    }
                }
                cm.sendSimple("Which Pal would you like to access?\r\n\ " + selStr);
            } else {
                cm.sendOk("You dont have any active pals");
            }
        }
        if (option == 20) {
            cm.openShopNPC(1500);//pokedoll shop
        }
        if (option == 5) {
            /*
             
             */
            pals_storage = cm.getPlayer().getPalStorage().getStoragePals();
            if (pals_storage.size() > 0) {
                var selStr = "";
                for (var i = 0; i < pals_storage.size(); i++) {
                    var cpal = pals_storage.get(i);
                    if (cpal != null) {
                        selStr += "#L" + i + "# #fUI/Custom.img/shared/element/" + cpal.getElement() + "# #b" + cpal.getName() + " (Lv. " + cpal.getLevel() + ")#k#l\r\n\ ";
                    }
                }
                cm.sendSimple("Which Pal would you like to send?\r\n\ " + selStr);
            } else {
                cm.sendOk("You dont have any active pals");
            }
        }
        if (option == 97) {
            var simp = cm.getPlayer().getAccVara("simple_battle") > 0;
            cm.getPlayer().setAccVar("simple_battle", simp ? 0 : 1);
            simp = cm.getPlayer().getAccVara("simple_battle") > 0;
            cm.sendOk("Simple Battles: " + (simp ? "#gON#k" : "#rOFF#k"));
        }
        if (option == 96) {
            var simp = cm.getPlayer().getAccVara("battle_mode") > 0;
            cm.getPlayer().setAccVar("battle_mode", simp ? 0 : 1);
            simp = cm.getPlayer().getAccVara("battle_mode") > 0;
            var text = "";
            if (simp) {
                text = "#bAll Npcs are now flagged as trainers#k";
            } else {
                text = "#bAll Npcs are restored to normal function#k";
            }
            cm.sendOk("Battle Mode: " + (simp ? "#gON#k" : "#rOFF#k") + "\r\n" + text);
        }
        if (option == 95) {
            cm.sendGetText("What battle level limit would you like to set\r\n#rMinimum Level: 10#k\r\n#rMaximum Level: 999#k\r\n");
        }
    } else if (status == 2) {//-------------------------------------------------------------------------------------------
        if (option == 4) {
            upgrade = selection;
            if (cm.haveItem(upgrade, 1)) {
                if (upgrade == 4202015) {
                    if (cm.getPlayer().getPalSlots() < 800) {
                        cm.sendYesNo("Are you sure you want to use #i" + upgrade + "# to expand more pal storage slots? ");
                    } else {
                        cm.sendOkS("You have reached your #r800 MAXIMUM#k Slots", 16);
                    }
                } else {
                    if (cm.getPlayer().getHatchSlots() < 99) {
                        cm.sendYesNo("Are you sure you want to use #i" + upgrade + "# to expand more incubation slots? ");
                    } else {
                        cm.sendOkS("You have reached your #r99 MAXIMUM#k Slots", 16);
                    }
                }
            } else {
                cm.sendOkS("You dont have enough #i" + upgrade + "#", 16);
            }
        }
        if (option == 95) {
            var lvl = cm.getNumber();
            if (lvl >= 10 && lvl <= 999) {
                cm.getPlayer().setAccVar("Pal_Level", lvl);
                cm.sendOk("Battle Level has been set to " + lvl);
            } else {
                cm.sendOk("Please use a valid number...");
            }

            return;
        }
        if (option == 5) {
            pal = pals_storage.get(selection);
            var txt = "#rWhich Player would you like to send #fUI/Custom.img/shared/element/" + pal.getElement() + "# #b" + pal.getName() + "#k:\r\n";
            players = cm.getOtherPlayers(cm.getPlayer());
            if (players.size() > 0) {
                for (var i = 0; i < players.size(); i++) {
                    var chr = players.get(i);
                    if (chr != null) {
                        txt += "#L" + i + "# #fUI/Custom.img/shared/element/" + chr.getElement() + "# #b" + chr.getName() + "#k #l\r\n\ ";
                    }
                }
                //text += "#L99#Goto Secret Map#l\r\n";
                cm.sendSimple(txt);
            } else {
                cm.sendOk("Seems like you are all alone in this world.\r\n#rGo touch some grass you kite.#k");
            }
        }
        if (option == 10) {
            pal = pals.get(selection);
            var txt = "Pal Stats:\r\n";
            var cexp = cm.getFullUnitNumber(pal.getExp());
            var mexp = cm.getFullUnitNumber(pal.getExpLevel(pal.getLevel()))
            var perc = parseInt(Math.floor((pal.getExp() / pal.getExpLevel(pal.getLevel())) * 100.0));
            txt += "Level: #b" + pal.getLevel() + "#k - Exp #b" + cexp + "#k / #r" + mexp + "#k (#b" + perc + "%#k)\r\n";
            var skill = cm.getPalSkill(pal.skill);
            txt += "Skill: #fUI/Custom.img/shared/element/" + skill.element() + "# #r" + skill.name() + "#k - (Pwr: #b" + skill.power() + "#k Spd: #b" + pal.getSpeed() + "ms#k) (#r" + (skill.magic() ? "M" : "P") + "#k)\r\n";
            txt += "Upgrades Remaining: #r" + pal.getUpgrades() + "#k\r\n";
            txt += "#rWhat would you like to do to " + pal.getName() + "?#k:\r\n";
            txt += "#L50#" + star + "#bChange Pal Name#k#l\r\n";
            txt += "#L51##fUI/Custom.img/shared/icon/2#  #bEquip Accessory: Slot 1#k (" + (pal.getAcc(1) != 0 ? "#r" + cm.getItemName(pal.getAcc(1)) + "#k" : "Empty") + ")#l\r\n";
            txt += "#L52##fUI/Custom.img/shared/icon/2#  #bEquip Accessory: Slot 2#k (" + (pal.getAcc(2) != 0 ? "#r" + cm.getItemName(pal.getAcc(2)) + "#k" : "Empty") + ")#l\r\n";
            txt += "#L57##fUI/Custom.img/shared/icon/2#  #bEquip Accessory: Slot 3#k (" + (pal.getAcc(3) != 0 ? "#r" + cm.getItemName(pal.getAcc(3)) + "#k" : "Empty") + ")#l\r\n";
            txt += "#L58##fUI/Custom.img/shared/icon/3#  #bEquip Accessory: Slot 4#k (" + (pal.getAcc(4) != 0 ? "#r" + cm.getItemName(pal.getAcc(4)) + "#k" : "Empty") + ")#l\r\n";
            txt += "#L53##fUI/Custom.img/shared/icon/0# #bFeed my Pal Energy#k#l\r\n";
            txt += "#L54##fUI/Custom.img/shared/icon/8# #bUpgrade my Pal's IVs#k#l\r\n";
            txt += "#L55##fUI/Custom.img/shared/icon/6# #bUpgrade my Pal's Stats#k#l\r\n";
            txt += "#L63##fUI/Custom.img/shared/icon/6# #bUpgrade my #rAll#b Pal's Stats#k#l\r\n";
            txt += "#L56##fUI/Custom.img/shared/icon/4# #bReset me Pal's Abilities#k#l\r\n";
            txt += "#L60##fUI/Custom.img/shared/icon/5# #bReset me Pal's Staties#k#l\r\n";
            txt += "#L61##fUI/Custom.img/shared/icon/7# #bChange me Pal's Skill#k#l\r\n";
            txt += "#L62##fUI/Custom.img/shared/icon/1# #bFeed my Pal Souls#k#l\r\n";
            txt += "#L59#" + star + "#bI want to fuck it#k#l\r\n ";
            //text += "#L99#Goto Secret Map#l\r\n";
            cm.sendSimple(txt);
        }
    } else if (status == 3) {//-------------------------------------------------------------------------------------------
        if (option == 4) {
            if (cm.haveItem(upgrade, 1)) {
                cm.gainItem(upgrade, -1);
                if (upgrade == 4202015) {
                    cm.getPlayer().setPalSlots(9);
                    cm.sendOk("You have consumed #i" + upgrade + "# for an 9 additional pal storage slots.");
                }
                if (upgrade == 4202016) {
                    cm.getPlayer().setHatchSlots(7);
                    cm.sendOk("You have consumed #i" + upgrade + "# for an 7 additional incubation slots.");
                }
            } else {
                cm.sendOk("Bring me either #i4202016# or #i4202015# to upgrade your hatching slots.");
            }
            return;
        }
        if (option == 5) {
            recv = players.get(selection);
            if (recv != null) {
                if (recv.canHoldPal()) {
                    cm.sendYesNo("Are you sure you want to send #b" + pal.getName() + "#k to #r" + recv.getName() + "#k?");
                } else {
                    cm.sendOk("This player does not have enough room to hold this pal.");
                }
            } else {
                cm.sendOk("This player does not exist.");
            }


            return;
        }
        option = selection;
        if (option == 50) {
            cm.sendGetText("Enter a name you like to use\r\n#rMax Name Length is 16 characters#k\r\n\r\n");
        }
        if (option == 51 || option == 52 || option == 57 || option == 58) {
            var txt = "#rWhich accessory would you like to Equip?#k:\r\n\r\n";
            var count = 0;
            if (cm.haveItem(4202017, 1)) {
                txt += "#L4202017# #i4202017##l ";
                count++;
            }
            if (cm.haveItem(4202018, 1)) {
                txt += "#L4202018# #i4202018##l ";
                count++;
            }
            if (cm.haveItem(4202019, 1)) {
                txt += "#L4202019# #i4202019##l ";
                count++;
            }
            for (var i = 4202500; i < 4202550; i++) {
                if (cm.haveItem(i, 1)) {
                    txt += "#L" + i + "# #i" + i + "##l ";
                    count++;
                }
            }
            if (count > 0) {
                cm.sendSimple(txt);
            } else {
                cm.sendOk("You dont have any accessories to equip.");
            }
        }
        if (option == 53) {
            var e = 4200000 + pal.getElement();
            var txt = "#rWhich energy would you like to feed your Pal?#k:\r\n";
            if (pal.getElement() <= 8) {
                txt += "#L" + e + "##i" + e + "##l   ";
            }
            txt += "#L4200009##i4200009##l   ";
            txt += "#L4200010##i4200010##l";
            //text += "#L99#Goto Secret Map#l\r\n";
            cm.sendSimple(txt);
        }
        if (option == 54) {
            upgrade = 4201000;
            cm.sendGetText("How many #i" + upgrade + "# do you want to apply to #r" + pal.getName() + "#k.\r\nUpgrades are used to increase an IV stat of choice by 1.\r\nMax IV stat obtained it 250.\r\n\Current Upgrades Remainging: #b" + pal.getUpgrades() + "#k.\r\nYou currently have #b" + cm.convertNumber(cm.getPlayer().countAllItem(upgrade)) + "#k\r\n#rMax number of upgrades a pal can consume is 100#k\r\n#rUpgrades are not refunded once used up!#k\r\n ");
        }
        if (option == 55) {
            var txt = "#rWhich Stat Booster would you like to apply to your " + pal.getName() + "?#k:\r\n";
            txt += "#L4202040##i4202040##l";
            txt += "#L4202041##i4202041##l";
            txt += "#L4202042##i4202042##l";
            txt += "#L4202043##i4202043##l";
            txt += "\r\n";
            txt += "#L4202044##i4202044##l";
            txt += "#L4202045##i4202045##l";
            txt += "#L4202046##i4202046##l";
            txt += "#L4202047##i4202047##l";
            txt += "\r\n ";
            //text += "#L99#Goto Secret Map#l\r\n";
            cm.sendSimple(txt);
        }
        if (option == 63) {
            cm.sendYesNo("Are you sure you want to use All?\r\n#i4202040##i4202041##i4202042##i4202043##i4202044##i4202045##i4202046##i4202047#");
        }
        if (option == 56) {
            cm.sendYesNo("Are you sure you want to use #i4202026# to reset your Pal's #bPassive Abilities#k? ");
        }
        if (option == 60) {
            cm.sendYesNo("Are you sure you want to use #i4202027#\r\nto reset your Pal's #bBase Stats and Level#k?\r\n#rThis feature cannot be undone#k\r\n#rAny Energies applied will not be refunded!#k");
        }
        if (option == 61) {
            var skill = cm.getPalSkill(pal.skill);
            var txt = "#rCurrent#k Skill: #fUI/Custom.img/shared/element/" + skill.element() + "# #r" + skill.name() + "#k - (Pwr: #b" + skill.power() + "#k Spd: #b" + pal.skillSpeed() + "ms#k) (#r" + (skill.magic() ? "M" : "P") + "#k)\r\n";
            cm.sendYesNo("Are you sure you want to use #i4202069#\r\nto change your current Pal's Attack?\r\n#rThis feature cannot be undone#k\r\n" + txt);
        }
        if (option == 59) {
            if (pal.canBreed()) {
                var c = cm.random(1, 10);
                pal.setBreedTime();
                if (cm.getPlayer().canHoldEgg() && c == 1) {
                    var lv = cm.getPlayer().getTotalLevel();
                    cm.getPlayer().makeEgg(pal.getModel(), lv * 2.5);
                    cm.sendOk("I dont know how its possible but an #rabomination#k was born.");
                } else {
                    cm.getPlayer().kill();
                    cm.sendOk("Ummm the pal was soo massive, you was smashed to death.");
                }
            } else {
                cm.sendOk("Lay off the poor thing you sick monster. It needs time to rest.");
            }
        }
        if (option == 62) {
            upgrade = 4201001;
            cm.sendGetText("How many #i" + upgrade + "# do you want to apply to #r" + pal.getName() + "#k.\r\nThese souls are used to increase a Pals Upgrade Count.\r\n\Current Upgrades Remainging: #b" + pal.getUpgrades() + "#k.\r\nYou currently have #b" + cm.convertNumber(cm.getPlayer().countAllItem(upgrade)) + "#k\r\n#rEach Upgrade costs 1000 souls, How many upgrades you want?#k");
        }
        //cm.sendOk("Event Option: " + option);
    } else if (status == 4) {//-------------------------------------------------------------------------------------------
        if (option == 5) {
            if (recv != null) {
                if (recv.canHoldPal() && cm.getPlayer().getMap() == recv.getMap()) {
                    if (cm.haveItem(4202068, 1)) {
                        cm.gainItem(4202068, -1);
                        cm.getPlayer().getPalStorage().sendPal(cm.getPlayer(), recv, pal);
                        recv.dropMessage(1, cm.getPlayer().getName() + " has sent you their " + pal.getName());
                        cm.sendOk("Your #b" + pal.getName() + "#k has been sent to #r" + recv.getName() + "#k");
                    } else {
                        cm.sendOk("You do not have the #4202068# to send this pal.");
                    }
                } else {
                    cm.sendOk("#rUnable to send pal to player due to player not on same map.#k");
                }

            } else {
                cm.sendOk("#rPlayer does not exist.#k");
            }
        }
        if (option == 63) {
            var txt = "#b" + pal.getName() + " Current Stats#k:\r\n";
            var count = 0;
            for (var i = 1; i <= 8; i++) {
                var j = 4202039 + i;
                var x = cm.getPlayer().countAllItem(j);
                if (x > 0) {
                    count++;
                    cm.gainItem(j, -x);
                    var st = pal.getStat(i);
                    pal.setStat(i, x);
                    if (i == 6) {
                        st = pal.getStat(7);
                    } else if (i == 7) {
                        st = pal.getStat(6);
                    }
                    txt += "#bBase " + stats[i] + " #k stat has increased to #r" + st + "#k\r\n";
                }
            }
            if (count > 0) {
                pal.save();
                cm.getPlayer().updateStats();
                cm.sendOk("You pal has consumed alot of pills and is now alot stronger.\r\n" + txt);
            } else {
                cm.sendOk("No pills were consumed.");
            }
            return;
        }
        if (option == 50) {
            var name = cm.getText();
            if (cm.getTextSize() > 16) {
                cm.sendOk("Name you entered is too long.");
                return;
            }
            var oName = pal.getName();
            pal.setName(name);
            pal.save();
            cm.sendOk("You have renamed #r" + oName + "#k to #b" + name + "#k. ");
        }
        if (option == 51 || option == 52 || option == 57 || option == 58) {
            acc = selection;
            if (pal != null && cm.haveItem(acc, 1)) {
                if (pal.getAcc(1) != 0 || pal.getAcc(2) != 0 || pal.getAcc(3) != 0 || pal.getAcc(4) != 0) {
                    cm.sendYesNo("Are you sure you want to equip #i" + acc + "# to this pal?\r\n#rOnce this item is equipped, it cannot be returned!#k\r\n#bThis will delete the currently equipped accessory!!#k");
                } else {
                    cm.sendYesNo("Are you sure you want to equip #i" + acc + "# to this pal?\r\n#rOnce this item is equipped, it cannot be returned!#k");
                }
            } else {
                cm.sendOk("Umm you missing something here.");
            }
        }
        if (option == 53) {
            energy = selection;
            exp = 100;
            if (energy == 4200009) {
                exp = 10000;
            }
            if (energy == 4200010) {
                exp = 1000000;
            }
            cm.sendGetText("How many #i" + energy + "# do you want to feed #r" + pal.getName() + "#k.\r\nYou current have #b" + cm.convertNumber(cm.getPlayer().countAllItem(energy)) + "#k Energies.\r\n\#bEach energy is valued at " + exp + " Exp#k.");
        }
        if (option == 54) {
            uAmount = cm.getNumber();
            if (uAmount > 0 && uAmount <= pal.getUpgrades()) {
                var txt = "Which #bIV Stat#k do you wish to increase by #b" + uAmount + "#k?\r\n";
                for (var i = 0; i < 9; i++) {
                    if (pal.getIV(i) < 250) {
                        txt += "#L" + (60 + i) + "# " + i + " " + star + " #b" + ivs[i] + "#k (Current: #r" + pal.getIV(i) + "#k)#l\r\n";
                    }
                }
                cm.sendSimple(txt);
            } else {
                cm.sendOk("Umm you missing something here.");
            }
        }
        if (option == 55) {
            upgrade = selection;
            cm.sendGetText("How many #i" + upgrade + "# do you want to apply to #r" + pal.getName() + "#k.\r\nBoosters are used to increase a Base stat of choice by 1.\r\n#rStat Boosters are not refunded once used up!#k\r\nYou currently have #b" + cm.convertNumber(cm.getPlayer().countAllItem(upgrade)) + "#k\r\n");
        }
        if (option == 56) {
            if (cm.haveItem(4202026, 1)) {
                cm.gainItem(4202026, -1);
                pal.resetAbilities();
                cm.sendOk("Your Pal's Abilities have been reset.");
            } else {
                cm.sendOk("You do not have enough #i4202026#.");
            }
        }
        if (option == 60) {
            if (cm.haveItem(4202027, 1)) {
                cm.gainItem(4202027, -1);
                pal.resetStats(cm.getPlayer(), pal);
                cm.sendOk("Your Pal's Base Stats and Level have been reset.");
            } else {
                cm.sendOk("You do not have enough #i4202027#.");
            }
        }
        if (option == 61) {
            if (cm.haveItem(4202069, 1)) {
                cm.gainItem(4202069, -1);
                pal.setSkill(cm.random(1, 51));
                var skill = cm.getPalSkill(pal.skill);
                var txt = "#r(NEW)#k Skill: #fUI/Custom.img/shared/element/" + skill.element() + "# #r" + skill.name() + "#k - (Pwr: #b" + skill.power() + "#k Spd: #b" + pal.skillSpeed() + "ms#k) (#r" + (skill.magic() ? "M" : "P") + "#k)\r\n";
                cm.sendOk("Your Pal's Attack has been changed.\r\n" + txt);
            } else {
                cm.sendOk("You do not have enough #i4202069#.");
            }
        }

        if (option == 62) {
            uAmount = cm.getNumber();
            if (cm.haveItem(upgrade, uAmount * 1000)) {
                cm.sendYesNo("Are you sure you want to apply #b" + (uAmount * 1000) + "#k #i" + upgrade + "# to increase your pal's upgrades.#k");
            } else {
                cm.sendOk("You cannot apply this many upgrades to this pal.");
            }
        }

    } else if (status == 5) {
        if (option == 51 || option == 52 || option == 57 || option == 58) {
            if (pal != null && acc != 0 && cm.haveItem(acc, 1)) {
                cm.gainItem(acc, -1);
                if (option == 51) {
                    pal.setAcc(1, acc);
                    cm.sendOk("#i" + acc + "# has been equipped to slot 1.");
                }
                if (option == 52) {
                    pal.setAcc(2, acc);
                    cm.sendOk("#i" + acc + "# has been equipped to slot 2.");
                }
                if (option == 57) {
                    pal.setAcc(3, acc);
                    cm.sendOk("#i" + acc + "# has been equipped to slot 3.");
                }
                if (option == 58) {
                    pal.setAcc(4, acc);
                    cm.sendOk("#i" + acc + "# has been equipped to slot 4.");
                }
            } else {
                cm.sendOk("Umm you missing something here.");
            }
        }
        if (option == 53) {
            eAmount = cm.getNumber();
            cm.sendYesNo("Are you sure you want to consume #b" + eAmount + "#k #i" + energy + "#\r\n#bThe total amount of Exp Gained: " + (eAmount * exp) + "#k");
        }
        if (option == 54) {
            IV = selection - 60;
            if (pal != null) {
                var tiv = 250 - pal.getIV(IV);
                if (uAmount > tiv) {
                    uAmount = tiv;
                }
                if (cm.haveItem(upgrade, uAmount)) {
                    cm.sendYesNo("Are you sure you want to apply #b" + uAmount + "#k #i" + upgrade + "# to increase #b" + ivs[IV] + " IV Stat (" + IV + ")#k");
                } else {
                    cm.sendOk("You cannot apply this many upgrades to this pal.");
                }
            } else {
                cm.sendOk("where is the pal?.");
            }

        }
        if (option == 55) {
            uAmount = cm.getNumber();
            IV = upgrade - 4202039;
            if (cm.haveItem(upgrade, uAmount)) {
                cm.sendYesNo("Are you sure you want to apply #b" + uAmount + "#k #i" + upgrade + "# to increase \r\n#bBase " + ivs[IV] + " Stat?#k");
            } else {
                cm.sendOk("Umm you missing something here.");
            }
        }
        if (option == 62) {
            if (cm.haveItem(upgrade, uAmount * 1000)) {
                cm.gainItem(upgrade, -(uAmount * 1000));
                pal.setUpgrades(pal.getUpgrades() + uAmount);
                pal.save();
                cm.sendOk(pal.getName() + " has gained #b" + uAmount + "#k Upgrades.\r\nUpgrades Remainging: #r" + pal.getUpgrades() + "#k");
            } else {
                cm.sendOk("You cannot apply this many upgrades to this pal.");
            }
        }
    } else if (status == 6) {
        if (option == 53) {
            if (cm.haveItem(energy, eAmount)) {
                cm.gainItem(energy, -eAmount);
                pal.gainLevelData(cm.getPlayer(), exp * eAmount);
                cm.sendOk(pal.getName() + " has fed on the energy of fallen pals and has gained #b" + (exp * eAmount) + "#k.");
            } else {
                cm.sendOk("Umm you missing something here.");
            }
        }
        if (option == 54) {
            if (IV >= 0 && uAmount > 0) {
                if (cm.haveItem(upgrade, uAmount)) {
                    cm.gainItem(upgrade, -uAmount);
                    pal.useUpgrade(IV, uAmount);
                    pal.save();
                    cm.getPlayer().updateStats();
                    cm.sendOk(pal.getName() + " #b" + ivs[IV] + " #k IV stat has increased to " + pal.getIV(IV) + ".");
                } else {
                    cm.sendOk("Umm you missing something here.");
                }
            } else {
                cm.sendOk("Umm you missing something here.");
            }
        }
        if (option == 55) {
            if (IV >= 0 && uAmount > 0) {
                if (cm.haveItem(upgrade, uAmount)) {
                    cm.gainItem(upgrade, -uAmount);
                    pal.setStat(IV, uAmount);
                    pal.save();
                    cm.getPlayer().updateStats();
                    cm.sendOk(pal.getName() + " #bBase " + stats[IV] + " #k stat has increased to " + pal.getStat(IV) + ".");
                } else {
                    cm.sendOk("Umm you missing something here.");
                }
            } else {
                cm.sendOk("Umm you missing something here.");
            }
        }
    } else if (status == 7) {
    } else if (status == 8) {
    } else if (status == 9) {
    } else if (status == 10) {
    } else {
        cm.dispose();
    }
}


