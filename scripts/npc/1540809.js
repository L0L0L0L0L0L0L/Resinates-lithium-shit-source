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
var cube = 4310502;
var option = -1;
var count = 0;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var ach = 0;
var limit = 16;
var page = 0;
var stat = new Array("Str", "Dex", "Int", "Luk");
var atk = new Array("Melee", "Magic");

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
        var text = "Select which room you want to learn about\r\n";
        //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
        text += "#L0#" + star + " #rMain Lobby#k#l\r\n";
        text += "#L9#" + star + " #rRoom 0#k. #bQuest Room#k#l\r\n";
        text += "#L1#" + star + " #rRoom 1#k. #bPal Room#k#l\r\n";
        text += "#L2#" + star + " #rRoom 2#k. #bFishing Pond#k#l\r\n";
        text += "#L3#" + star + " #rRoom 3#k. #bDamage Skins#k#l\r\n";
        text += "#L4#" + star + " #rRoom 4#k. #bEquip Upgrades#k#l\r\n";
        text += "#L5#" + star + " #rRoom 5#k. #bGuild Room#k#l\r\n";
        text += "#L6#" + star + " #rRoom 6#k. #bDungeon Room#k#l\r\n";
        text += "#L7#" + star + " #rRoom 7#k. #bDonation Room#k#l\r\n";
        text += "#L8#" + star + " #rMarket Room#k#l\r\n";
        //text += "#L8645340# #bAssailant#k #l\r\n";
        cm.sendSimple(text);
    } else if (status == 1) {
        option = selection;

        if (option == 0) {
            var text = "This is Main Lobby for quick access to various npcs and systems.\r\n";
            text += "  " + star + " #rDaily Box#k -> Rewards daily items.\r\n";
            text += "  " + star + " #rKaotic Database#k -> Shows info where items are dropped.\r\n";
            text += "  " + star + " #rRandolf#k -> Zone Quest Npc for overworld.\r\n";
            text += "  " + star + " #rHyperspace#k -> Handles Syper Stat System.\r\n";
            text += "  " + star + " #rCody#k -> Handles player Cosmetics.\r\n";
            text += "  " + star + " #rWanted Poster#k -> Single Item quest for rewards.\r\n";
            text += "  " + star + " #rRooney#k -> Multi-Item quest for rewards.\r\n";
            text += "  " + star + " #rPaperBoy#k -> Handles in game refunds.\r\n";
            text += "  " + star + " #rVieren#k -> Handles job changes.\r\n";
            text += "  " + star + " #rPollo#k -> Handles Energy Charges and Skill Boosters.\r\n";
            cm.sendOk(text);
        } else {
            //text += "  " + star + " #r----#k -> \r\n";
            var text = "\r\n#rRoom " + option + "#k handles the Current Systems:.\r\n";
            if (option == 1) {
                text += "" + star + " #bMaple Pal Room, handles all maple pal needs and wants.#k\r\n";
                text += "  " + star + " #rProf Oak#k -> Handles all Pal needs and wants.\r\n";
                text += "  " + star + " #rProf Elm#k -> Handles Pal dungeons and parks for materials.\r\n";
                text += "  " + star + " #rBreeder Ray#k -> Enjoy watching pals make babies.\r\n";
                text += "  " + star + " #rPal Balls#k -> Mini-event to catch pals.\r\n";
                text += "  " + star + " #rRoom 1: Master Trainer Room#k -> Very hard pal battles!\r\n";
                text += "  " + star + " #rRoom 2: Trainer Room#k -> Simple pal battles!\r\n";
                text += "  " + star + " #rRoom 3: Trainer Room#k -> Simple pal battles!\r\n";
            }
            if (option == 2) {
                text += "" + star + " #bFishing Room, handles all Idle fishing.#k\r\n";
                text += "  " + star + " #r@bait#k -> Sets the fishing bait used.\r\n";
                text += "  " + star + " #rFishing Chair#k -> Use any chair in game to fish with.\r\n";
                text += "  " + star + " #rFishing#k -> Procs % chance of catch every 1 second.\r\n";
            }
            if (option == 3) {
                text += "" + star + " #bDamage Skins, handles all @skin in the game.#k\r\n";
                text += "  " + star + " #r@skin#k -> Sets the Damage Skin you want to use.\r\n";
                text += "  " + star + " #rBlaster#k -> Handles Orb system for damage skin tickets.\r\n";
                text += "  " + star + " #rHayato#k -> Shows all detailed info about damage skins\r\n";
                text += "  " + star + " #rTop Row Npcs#k -> Handles exchange of damage tickets for x Tier\r\n";
                text += "  " + star + " #rHooded Npc#k -> Handles Choas Damage Skin Tickets\r\n";
            }
            if (option == 4) {
                text += "" + star + " #bEquip Room: Handles All equip systems in the server.#k\r\n";
                text += " " + star + " #bRoom 1#k\r\n";
                text += "  " + star + " #rNameless Soul#k -> Handles Power scrolls for NON-NX Gears.\r\n";
                text += "  " + star + " #rAdler#k -> Handles Power Shards for NX Gears.\r\n";
                text += "  " + star + " #rFredrick#k -> Handles Cubing system.\r\n";
                text += "  " + star + " #rCygnus#k -> Handles Equip enhancement for All Gears.\r\n";
                text += " " + star + " #bRoom 2#k\r\n";
                text += "  " + star + " #rObv Soul#k -> Handles upgrade equips using Spell Traces.\r\n";
                text += "  " + star + " #rBarry#k -> Handles unlocking equips.\r\n";
                text += "  " + star + " #rHaruaki#k -> Handles NX item fusion system.\r\n";
                text += "  " + star + " #rShemp#k -> Handles Weapon Mastery.\r\n";
                text += "  " + star + " #rJason#k -> Handles Weapon Fusion\r\n";
                text += "  " + star + " #rButler#k -> Handles Medal Fusion\r\n";
                text += "  " + star + " #rOther Npcs#k -> Currently Unused\r\n";
                text += "  " + star + " #rEnneth#k -> Thee Pet Killer\r\n";
            }
            if (option == 5) {
                text += "" + star + " #bGuild Room, handles all guild systems in the server.#k\r\n";
                text += "  " + star + " #rPink Bean#k -> Cash in reward coins for guild points.\r\n";
                text += "  " + star + " #rHeracle#k -> Creates-Disbands-Upgrades guilds.\r\n";
                text += "  " + star + " #rLenario#k -> Handles Guild alliances.\r\n";
                text += "  " + star + " #rLea#k -> Handles Guild emblems.\r\n";
            }
            if (option == 6) {
                text += "" + star + " #bDungeon Room, handles all of the players progresson.#k\r\n";
                text += "  " + star + " #rTutorial Zone#k -> First step of progression, Finish these missions unlock Quick Move, Party Zone, Endless Zone, Wanted Poster, Rooney, Randolf.\r\n";
                text += "  " + star + " #rFarming Zone#k -> Used to farm various Etcs by zone.\r\n";
                text += "  " + star + " #rRandom (Hell)#k -> used to farm Golden Gach Tickets and other rewards.\r\n";
                text += "  " + star + " #rEndless Zone#k -> Used to Farm levels at very rapid pace.\r\n";
                text += "  " + star + " #rParty Zone#k -> Used to unlock various systems such as Job Changing and Job Trial Missions.\r\n";
                text += "  " + star + " #rReward Zone#k -> Used to farm various rewards.\r\n";
                text += "  " + star + " #rPremium Dungone#k -> Dungeon Mission with no damage caps and no leveling but can farm gear.\r\n";
                text += "  " + star + " #rJob Trial#k -> Complete these missions to unlock or expand random job chosen at the end.\r\n";
                text += "  " + star + " #rAdv. Job Trials#k -> Complete these missions to unlock or expand random job chosen at will.\r\n";
                text += "  " + star + " #rCore Upgrades#k -> Craft core upgrades to add bonus stats to your character.\r\n";
                text += "  " + star + " #rMagicite Shop#k -> Shop that handles buying various rewards with magicite.\r\n";
            }
            if (option == 7) {
                text += "" + star + " #bDonation Room, handles all donation systems in the server.#k\r\n";
                text += "  " + star + " #rCony#k -> Handles Label Ring Gacha\r\n";
                text += "  " + star + " #rBrownie#k -> Handles Chat Ring Gacha\r\n";
                text += "  " + star + " #rMedina#k -> Handles Medal Gacha\r\n";
                text += "  " + star + " #rDuey#k -> Handles all info needed for Donations\r\n";
                text += "  " + star + " #rAbbess#k -> Job Ticket Exchanges\r\n";
                text += "  " + star + " #rKelm#k -> Handles increasing item vac range\r\n";
                text += "  " + star + " #rEka#k -> Handles Pet Gacha\r\n";
                text += "  " + star + " #rGarnet#k -> Handles NX of choice\r\n";
                text += "  " + star + " #rModryn#k -> Handles Random Face expressions gacha\r\n";
                text += "  " + star + " #rVurian#k -> Handles Chair of choice\r\n";
                text += "  " + star + " #rVersalian#k -> Handles change android looks\r\n";
                text += "  " + star + " #rHaneet#k -> Handles Changing Core Stats with Lucky Sacks\r\n";
                text += "  " + star + " #rHaku#k -> Handles expanding Buff Coupon durations\r\n";
                text += "  " + star + " #rRiman#k -> handles upgrading Items with Infinity Points\r\n";
                text += "  " + star + " #rOther Npcs#k -> Currently Unused\r\n";
            }
            if (option == 8) {
                text += "  " + star + " #bMarket Room, handles various Npc shops and Player Shops.#k\r\n";
            }
            if (option == 9) {
                text = "\r\n#rRoom 0#k handles various quests players can take on:\r\n";
                text += "  " + star + " #rWanted Poster#k, -> Complete ETC quest for Energy Charges#k\r\n";
                text += "  " + star + " #rRooney#k, -> Complete Series of ETC quest for SKill Boosters#k\r\n";
                text += "  " + star + " #rAce of Hearts#k, -> Re-shuffle required ETC for Wanted and Rooney#k\r\n";
                text += "  " + star + " #rRandolf#k, -> Various Etc Quests in various towns.#k\r\n";
                text += "  " + star + " #rCoco#k, -> Collect Chocolates bring them to coco#k\r\n";
                text += "  " + star + " #rSanta-Pongo#k, -> Collect Presents bring to Santa or Pongo#k\r\n";
                text += "  " + star + " #rGusto#k, -> Bring various currencies to buy casino coins#k\r\n";
                text += "  " + star + " #rSlots#k, -> Use casino coins to gamble for random rewards#k\r\n";
                text += "  " + star + " #rOther Npcs#k -> Currently Unused\r\n";
            }
            cm.sendYesNo("Do you wish to go here?" + text);
        }
    } else if (status == 2) {
        if (option == 1) {
            cm.warp(870000100);
        }
        if (option == 2) {
            cm.warp(870000203);
        }
        if (option == 3) {
            cm.warp(870000102);
        }
        if (option == 4) {
            cm.warp(870000103);
        }
        if (option == 5) {
            cm.warp(870000101);
        }
        if (option == 6) {
            cm.warp(870000010);
        }
        if (option == 7) {
            cm.warp(870000000);
        }
        if (option == 8) {
            cm.warp(870000001);
        }
        if (option == 8) {
            cm.warp(870000009);
        }
        cm.dispose();
    } else if (status == 3) {

    } else if (status == 4) {

    }
}