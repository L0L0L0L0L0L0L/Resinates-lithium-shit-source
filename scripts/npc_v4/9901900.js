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
        var selStr = "Welcome to Kaotic Maple. I am here to help you learn how this server works compared to a normal maplestory server.\r\n";
        selStr += "#L1#" + star + "" + (cm.getPlayer().achievementFinished(910) ? "#b" : "#r") + "Equips#k#l\r\n";
        selStr += "#L2#" + star + "" + (cm.getPlayer().achievementFinished(911) ? "#b" : "#r") + "Scrolling and Upgrades#k#l\r\n";
        selStr += "#L3#" + star + "" + (cm.getPlayer().achievementFinished(912) ? "#b" : "#r") + "Monsters and Tiers#k#l\r\n";
        selStr += "#L4#" + star + "" + (cm.getPlayer().achievementFinished(913) ? "#b" : "#r") + "Looting and Storage#k#l\r\n";
        selStr += "#L5#" + star + "" + (cm.getPlayer().achievementFinished(914) ? "#b" : "#r") + "Quick Move#k#l\r\n";
        selStr += "#L6#" + star + "" + (cm.getPlayer().achievementFinished(915) ? "#b" : "#r") + "Questing#k#l\r\n";
        selStr += "#L7#" + star + "" + (cm.getPlayer().achievementFinished(916) ? "#b" : "#r") + "Events#k#l\r\n";
        selStr += "#L8#" + star + "" + (cm.getPlayer().achievementFinished(917) ? "#b" : "#r") + "Androids#k#l\r\n";
        selStr += "#L9#" + star + "" + (cm.getPlayer().achievementFinished(918) ? "#b" : "#r") + "Damage Systems#k#l\r\n";
        selStr += "#L10#" + star + "" + (cm.getPlayer().achievementFinished(919) ? "#b" : "#r") + "Currency#k#l\r\n";
        selStr += "#L11#" + star + "" + (cm.getPlayer().achievementFinished(920) ? "#b" : "#r") + "Commands#k#l\r\n";
        selStr += "#L12#" + star + "" + (cm.getPlayer().achievementFinished(921) ? "#b" : "#r") + "Custom Zones#k#l\r\n";
        selStr += "#L13#" + star + "" + (cm.getPlayer().achievementFinished(922) ? "#b" : "#r") + "Custom Jobs#k#l\r\n";
        selStr += "#L14#" + star + "" + (cm.getPlayer().achievementFinished(923) ? "#b" : "#r") + "Leveling#k#l\r\n";
        selStr += "#L15#" + star + "" + (cm.getPlayer().achievementFinished(924) ? "#b" : "#r") + "Stamina#k#l\r\n";
        selStr += "#L16#" + star + "" + (cm.getPlayer().achievementFinished(925) ? "#b" : "#r") + "Fishing#k#l\r\n ";
        cm.sendSimpleS(selStr, 16);
    } else if (status == 1) {
        var selStr = "";
        if (selection < 99) {
            ach = selection;
            if (ach == 1) {
                selStr = star + "#r#eEquips:#k#n\r\n";
                selStr += "     " + star + "Equips are most vital part of this server. Everything you do revovlves around upgrading and scrolling equips. You will be constantly replacing equips over your career here.\r\n\r\n";
                selStr += star + "#r#eEquip Tier System:#k#n\r\n";
                selStr += "     " + star + "Tiers on equips determines how many upgrade slots equips can have. The tier system also determines how many stats items get from Scrolls and Enhancements.\r\n\r\n";
                selStr += "     " + star + "Each Tier of the Equips gives scrolls +10% more power.\r\n\r\n";
            }
            if (ach == 2) {
                selStr = star + "#r#eStat Scrolls #i2049180##i2049181##i2049182##i2049183##i2049184#:#k#n\r\n";
                selStr += "     " + star + "Scrolls that give #rFixed#k stats\r\nAdd stats based on Tier of Equip multiplied by stat on Scroll Description. Does not consume slots on failures.\r\n\r\n";
                selStr += star + "#r#eChaos-Power Scrolls #i2049185##i2049186##i2049187##i2049188##i2049189#:#k#n\r\n";
                selStr += "     " + star + "Scrolls that give #rRanged#k stats \r\nAdd stats based on Tier of Equip multiplied by stat range on Scroll Description. Does not consume slots on failures.\r\n\r\n";
                selStr += star + "#r#eEnhancing Scrolls #i2049300##i2049301##i2049302##i2049303##i2049304##i2049305#:#k#n\r\n";
                selStr += "     " + star + "Scrolls that give #rRanged#k stats without consuming slots\r\n. Enhancements add stats based on Tier of Equip multiplied by stat range on Scroll Description.\r\nYou must speak to #rCygnus Mentality#k to use EE.\r\n#rEquips maximum enchancements is 25 stars.#k\r\n\r\n";
                selStr += star + "#r#eSoul Gems #i2586000##i2586001##i2586002#:#k#n\r\n";
                selStr += "     " + star + "Special Gems that give #rFixed#k bonus stats.\r\nThese gems expand on bonus stats thats applied on items. These Gems are built for Mid-Late game usage and require special Protections to use.\r\nYou must speak to #rT-1337#k to use Soul Gems.\r\n\r\n";
                selStr += star + "#r#eShards #i2585000##i2585001##i2585002##i2585003##i2585004##i2585005#:#k#n\r\n";
                selStr += "     " + star + "Shards are used to reset stats on NX gear, Shard power is based on players level and tier of NX item. Shards Reset stats AND pots HOWEVER Bonus stats are untouched. They do have the power to destory NX Equips.\r\n";
                selStr += star + "#r#eProtection Scrolls #i2340000##i2587000##i2587001#:#k#n\r\n";
                selStr += "     " + star + "Special Scrolls that Protect Items from failing or Destruction.\r\nThese very Powerful scrolls are rare and hard to find. They are used to protect items and have other various uses.\r\n\r\n";
                selStr += star + "#r#eCubes #i2583000##i2583001##i2583007##i2583002#:#k#n\r\n";
                selStr += "     " + star + "Special Custom Cubes that roll Kaotic ONLY potentails.\r\nYou must Speak to #rFredrick#k to use these cubes\r\n";
                selStr += "     " + star + "Rainbow Cubes are very powerful single-line cube that has power to destory equips but offer The best possible potentials.\r\nYou must Speak to #rCassandra#k to use these cubes\r\n\r\n";
                selStr += star + "#r#eDestiny Fragment Orbs #i4001895#:#k#n\r\n";
                selStr += "     " + star + "Special orb that allows equips to upgrade more tiers.\r\nYou must Speak to #rCygnus#k located in #bOutpost#k to use these orbs\r\n";
            }
            if (ach == 3) {
                selStr = star + "#r#eMonsters:#k#n\r\n";
                selStr += "     " + star + "Monsters on this server are completly 100% customed. Monster's stats are purely based on what Tier the monster is. Higher the Tier, stronger the items are - more exp gained - more mesos gained - harder they hit - more damage they can take.\r\n\r\n";
                selStr += star + "#r#eMonsters Tiers:#k#n\r\n";
                selStr += "     " + star + "#r#eTier 1-4 (Normal Monsters):#k#n\r\n";
                selStr += "     " + star + "#r#eTier 5-15 (Normal Bosses):#k#n\r\n";
                selStr += "     " + star + "#r#eTier 15-24 (Kaotic Bosses):#k#n\r\n";
                selStr += "     " + star + "#r#eTier 25+ (Super Kaotic Bosses):#k#n\r\n\r\n";
                selStr += star + "#r#eBotting Rules:#k#n\r\n";
                selStr += "     " + star + "Botting is #rONLY#k allowed inside peronal @instance command and #r No where Else is allowed#k\r\n\r\n";
                selStr += star + "#r#eKaotic Monsters:#k#n\r\n";
                selStr += "     " + star + "Special Strong monsters that reward a lot of exp and powerful gears. They also grant more ETC and Mesos when killed.\r\n\r\n";
                selStr += star + "#r#eFarming Monsters with Instance:#k#n\r\n";
                selStr += "     " + star + "Using the command - @instance - will teleport you to your own personal map to farm and BOT.\r\n\r\n";
                selStr += star + "#r#eFarming Kaotic Monsters:#k#n\r\n";
                selStr += "     " + star + "Using the command - @kaotic # - (1-4) - will teleport you to your own personal kaotic map where you can farm for buffed monsters that give extra ETC and Mesos.\r\n\r\n";
                selStr += star + "#r#eBoss Material Events:#n#k \r\n ";
                selStr += "     " + star + "Special events used to farm materials for bosses.\r\n ";
                selStr += "     " + star + "Lucid Boss Material Event can be activated once your defeat Lucid.\r\nThis event can be found by talking to Dark Mask on Lucid Tower 3rd Floor.\r\n ";
                selStr += "     " + star + "Normal Will Boss Material Event can be activated once your defeat Will.\r\nThis event can be found by talking to Melange at Radiant Temple 1st Map.\r\n ";
                selStr += "     " + star + "Commander Will Boss Material Event can be activated once your defeat Commander Will.\r\nThis event can be found by talking to Cygnus Soilders at End of the World.\r\n\r\n ";
                selStr += star + "#r#eMonster Commands:#n#k \r\n ";
                selStr += "     " + star + "You can use #r@monster#k to view all stats of monsters on the map.\r\n ";
                selStr += "     " + star + "You can use #r@mobinfo#k to view all the drops of monsters on the map.\r\n ";
            }
            if (ach == 4) {
                selStr = star + "#r#eLooting:#k#n\r\n";
                selStr += "     " + star + "Normal Monsters drops are all auto pickup on kill. ETC items automaticly goto into your ETC storage system.\r\n";
                selStr += "     " + star + "Boss and Kaotic Boss drops on the ground for your character only, other players do not see your drops. Boss drops have auto-vac system simply walking over the items.\r\n";
                selStr += "     " + star + "Equips: If your inventory is full of equips, excess equips are automaticly recycled into #i4310066#.\r\n\r\n";
                selStr += star + "#r#eStorages:#k#n\r\n";
                selStr += "     " + star + "We have a special unlimited size ETC storage that is linked to your account and can be accessed on any char at any time. You can use #r@storeEtc#k command to quickly dump all ETC from your inventory into the storage system. You can also visit Ace of Spades in any town to put items into ETC storage. You can use #r@loot#k to disable ETC going into your ETC storage.\r\n\r\n";
            }
            if (ach == 5) {
                selStr = star + "#r#eQuick Move Starter System:#k#n\r\n";
                selStr += "     " + star + "#r#eCaster#k#n\r\n";
                selStr += "     Handles custom quest system in the server. He can also be used to know where to level based off level of quests.\r\n";
                selStr += "     " + star + "#r#eRecycler#k#n\r\n";
                selStr += "     Handles recycling equips and turning them into #i4310066#\r\n";
                selStr += "     Handles #i4310066# Unleashed Shop used to cash in Unleashed Coins for rare items.\r\n";
                selStr += "     " + star + "#r#eOverflow ETC Storage#k#n\r\n";
                selStr += "     Lets you access your ETC items inside your Storage ETC system.\r\n";
                selStr += "     " + star + "#r#eBank#k#n\r\n";
                selStr += "     Handles all your mesos when your wallet is full.\r\n";
                selStr += "     The bank also handles importing and exporting mesos into #i4310500#.\r\n";
                selStr += "     " + star + "#r#eAchievements#k#n\r\n";
                selStr += "     Shows what achievements that you have comepleted during your journey.\r\n\r\n";
                selStr += star + "#r#eQuick Move System:#k#n\r\n";
                selStr += "     " + star + "#r#eMirrior#n#k\r\n";
                selStr += "     Allows fast travel to various towns. More you progress more towns become unlocked, such as leveling or completing achievements.\r\n";
                selStr += "     " + star + "#r#eMonster Park Taxi#k#n\r\n";
                selStr += "     Allows fast travel to various event zones.\r\n";
                selStr += "     " + star + "#r#eQuick Cube#k#n\r\n";
                selStr += "     Allows instant access to cubing equips.\r\n";
            }
            if (ach == 6) {
                selStr = star + "#r#eKaotic Quests:#k#n\r\n";
                selStr += "     " + star + "Special Quests that give extra #rDrop Rate#k when completed. Quests also help lost players know where to go. Some quests are hard and some are very easy. These quests are NOT repeatable.\r\n";
                selStr += star + "#r#eWanted Poster Quests:#k#n\r\n";
                selStr += "     " + star + "Special Quest located in #rFree Market#k that can help players gain monster park coins and Fame. These quests can be repeated.\r\n";
                selStr += star + "#r#eRooney Quests:#k#n\r\n";
                selStr += "     " + star + "Special Quest that gives alot of Gallent Emblems, Fame, Mesos and level ups. Quest can be repeated.\r\n";
                selStr += star + "#r#eNPC Quests:#k#n\r\n";
                selStr += "     " + star + "Special Quests found all over maple world. they offer up some quick mini-levels.\r\n";
            }
            if (ach == 7) {
                selStr = star + "#r#eEvents:#n#k \r\n ";
                selStr += "     " + star + "All events and instances used are personal and belong to leader of group or player who started the instance-event.\r\n\r\n ";
                selStr += star + " #r#eBoss PQ:#k#n \r\n ";
                selStr += "     " + star + "Party based boss event that lets you take on various bosses for special rewards.\r\n\r\n ";
                selStr += star + "#r#eMonster Park:#n#k \r\n ";
                selStr += "     " + star + "Monster Park is used to quickly gain levels and earn monster park badge with monster park E Tickets.\r\n ";
                selStr += "     " + star + "Normal Monster Park is Solo only\r\n ";
                selStr += "     " + star + "Kaotic Monster Park is a Party Play. This event is used later in game for fast Leveling. This event is unlocked after completing all of Monster Park and is found at Arcane River Towns Spingleman.\r\n\r\n ";
                selStr += star + "#r#eElite - Life Scroll:#n#k \r\n ";
                selStr += "     " + star + "Life scrolls can be found all over the world from various monsters.\r\n ";
                selStr += "     " + star + "You can take these scrolls to #r Mia #k in #b Ellinia Forest#k.\r\n ";
                selStr += "     " + star + "You can take these scrolls to #r Kao #k in #b Nameless Town#k.\r\n ";
                selStr += "     " + star + "You can take these scrolls to #r Kyrin #k in #b Outpost#k.\r\n ";
            }
            if (ach == 8) {
                selStr = star + "#r#eAndroids:#k#n\r\n";
                selStr += "     " + star + "Androids offer vital secondary source of power and damage.\r\n";
                selStr += "     " + star + "Androids can earn exp from monsters like how players do, with level ups and stat gains. There is no Level Caps.\r\n";
                selStr += "     " + star + "Androids earn stats based on what job play and what #rTier#k droid is. Higher the Tier more stats android gains per level up.\r\n";
                selStr += "     " + star + "Androids can earn bonus stats if any bonus stats are present.\r\n";
                selStr += "     " + star + "Androids cannot be scrolled or gemmed.\r\n";
                selStr += "     " + star + "You can enable droids using #r@showdroid#k Command.\r\n";
                selStr += "     " + star + "Androids can be obtained from gachapon or boss drops.\r\n";
            }
            if (ach == 9) {
                selStr = star + "#r#ePlayer Damage System:#k#n\r\n";
                selStr += "     " + star + "All Damage dealt to monsters is calculated  on server side and visiually sent to you.\r\n";
                selStr += "     " + star + "Damage is calculated based on your base stats which creates your #rOverpower#k stat.\r\n";
                selStr += "     " + star + "Damage cap is set to 9 quint PER line.\r\n";
                selStr += "     " + star + "Monsters higher level than you will make you deal less damage.\r\n";
                selStr += "     " + star + "Monsters higher defense than your IED stat will make you deal less damage.\r\n";
                selStr += "     " + star + "You can see FULL Damage system calculation by using the command #r@op#k.\r\n\r\n";
                selStr += star + "#r#eMonster Damage Systems:#k#n\r\n";
                selStr += "     " + star + "Damage from monsters is based on monsters Attack stat.\r\n";
                selStr += "     " + star + "You can reduce damage taken with #rDamage Resist#k stat from guild skills and potenials for max reduction of 90%\r\n";
                selStr += "     " + star + "Monsters attacks can stack multiple times into single damage line.\r\n";
                selStr += "     " + star + "Monsters higher level than you will deal alot more damage to you.\r\n";
                selStr += "     " + star + "You can see FULL stats of monsters by using the command #r@monster#k\r\n\r\n";
            }
            if (ach == 10) {
                selStr = star + "#r#eCurrency Systems:#k#n\r\n";
                selStr += "     " + star + "There are tons and tons of different currencies that do many different things. They fall under ETC inventory space and can be stored inside the ETC storage system.\r\n";
                selStr += "     " + star + "All currencies can be used with #rSpecial Random Shop# located in #bFree Market#k.\r\n\r\n";
                selStr += star + "#i4310015##r#e Gallent Emblems#k#n\r\n";
                selStr += "     " + star + "Obtained from achivements and quests.\r\n";
                selStr += "     " + star + "Can be used with #rInkwell#k located in #bFree Market#k.\r\n\r\n";
                selStr += star + "#i4310018##r#e Crusader Coins#k#n\r\n";
                selStr += "     " + star + "Obtained from quests and very rare drop from random monsters.\r\n";
                selStr += "     " + star + "Can be used with #rSpecial Random Shop# located in #bFree Market#k.\r\n\r\n";
                selStr += star + "#i4310020##r#e Monster Park Coins#k#n\r\n";
                selStr += "     " + star + "Obtained from quests and common drop from all monsters.\r\n";
                selStr += "     " + star + "Can be used at #rMary#k located in #bMonster Park Map#k.\r\n\r\n";
                selStr += star + "#i4310028##r#e Legendary Boss PQ Coins#k#n\r\n";
                selStr += "     " + star + "Obtained from Boss PQ.\r\n";
                selStr += "     " + star + "Can be used with #rAgent Meow#k located in #bBoss PQ Map#k.\r\n\r\n";
                selStr += star + "#i4310064##i4310065##r#e Root Abyss Coins#k#n\r\n";
                selStr += "     " + star + "Obtained from bosses located in Root Abyss.\r\n";
                selStr += "     " + star + "Can be used with #rOko#k located in #bRoot Abyss#k.\r\n\r\n";
                selStr += star + "#i4310066##r#e Unleashed Coins#k#n\r\n";
                selStr += "     " + star + "Obtained from recycling equips.\r\n";
                selStr += "     " + star + "Can be used with #rRecycler#k located in #bQuick Move#k.\r\n\r\n";
                selStr += star + "#i4310150##r#e Reward Coins#k#n\r\n";
                selStr += "     " + star + "Obtained from Kaotic Monster Park and other End Game events.\r\n";
                selStr += "     " + star + "Can be used with #rInkwell#k located in #bFree Market#k.\r\n\r\n";
                selStr += star + "#i4310211##r#e Unity Hearts#k#n\r\n";
                selStr += "     " + star + "Obtained from monsters in Outpost.\r\n";
                selStr += "     " + star + "Can be used with various shops found around outpost area.\r\n";
                selStr += "     " + star + "Can be used to craft #rCrystal Heart#k at End of the World.\r\n\r\n";
                selStr += star + "#i4310156##r#e Absolute Coins#k#n\r\n";
                selStr += "     " + star + "Obtained from defeating Lotus and Damien.\r\n";
                selStr += "     " + star + "Can be used with #rThree Hands Robot#k located in #bBlack Heaven#k.\r\n\r\n";
                selStr += star + "#i4310218##r#e Lucid Coins#k#n\r\n";
                selStr += "     " + star + "Obtained from defeating Lucid.\r\n";
                selStr += "     " + star + "Can be used with #rKanto#k located in #bLachelein#k.\r\n\r\n";
                selStr += star + "#i4310249##r#e Will Coins#k#n\r\n";
                selStr += "     " + star + "Obtained from defeating Will.\r\n";
                selStr += "     " + star + "Can be used with #rKanto#k located in #bEsfera#k.\r\n\r\n";
                selStr += star + "#i4310260##r#e Genesis Essence#k#n\r\n";
                selStr += "     " + star + "Obtained from defeating Evil Hilla, Darknell and Commander Will.\r\n";
                selStr += "     " + star + "Can be used with #rPlastic Roy#k located in #bEnd of the World#k.\r\n\r\n";
                selStr += star + "#i4310500##r#e Golden Meso Bag#k#n\r\n";
                selStr += "     " + star + "Obtained from Quick Move - Bank.\r\n";
                selStr += "     " + star + "Each bag is worth 1 Billion Mesos.\r\n";
                selStr += "     " + star + "Used with several god item recipes in the end game\r\n";
                selStr += "     " + star + "Can be used with #rDame#k located in #bArcania#k and other various towns.\r\n\r\n";
                selStr += star + "#i4310501##r#e Maple Points#k#n\r\n";
                selStr += "     " + star + "Obtained from recycling NX equips or Meso shop or Rare Drop from monsters.\r\n";
                selStr += "     " + star + "Can be used to buy NX gears at #rWu Yuan#k located in #bFree Market#k\r\n\r\n";
                selStr += star + "#i4310502##r#e Donation Points#k#n\r\n";
                selStr += "     " + star + "Obtained from donations or fishing with Master Bait.\r\n";
                selStr += "     " + star + "Can be used to buy various goods located #bDP Lands#k located in Free Market\r\n\r\n";

            }
            if (ach == 11) {
                selStr = star + "#r#e Commands#k#n\r\n";
                selStr += "     " + star + "Commands can #ronly#k be used in general chat.\r\n";
                selStr += "     " + star + "Most commands have a global timer set to them to prevent exploits or abuse.\r\n\r\n";
                selStr += "#b#e @ghelp#k#n\r\n";
                selStr += "     " + star + "Sends a message to any gms online to help you.\r\n\r\n";
                selStr += "#b#e @say -message#k#n\r\n";
                selStr += "     " + star + "Sends a message to everyone on the server.\r\n\r\n";
                selStr += "#b#e @info #k#n\r\n";
                selStr += "     " + star + "Shows various info about your account.\r\n\r\n";
                selStr += "#b#e @guild#k#n\r\n";
                selStr += "     " + star + "Shows info about the guild your in.\r\n\r\n";
                selStr += "#b#e @fix#k#n\r\n";
                selStr += "     " + star + "If portals or NPC's dont work, Use this command to fix it.\r\n\r\n";
                selStr += "#b#e @mega - @effects#k#n\r\n";
                selStr += "     " + star + "Enables or Disables Megaphones or Effects from other players.\r\n\r\n";
                selStr += "#b#e @save#k#n\r\n";
                selStr += "     " + star + "Saves your character on the spot.\r\n\r\n";
                selStr += "#b#e @who#k#n\r\n";
                selStr += "     " + star + "Shows who is online and player count.\r\n\r\n";
                selStr += "#b#e @stats#k#n\r\n";
                selStr += "     " + star + "Shows hidden detailed stats of your character.\r\n\r\n";
                selStr += "#b#e @rates#k#n\r\n";
                selStr += "     " + star + "Shows your current rates. Doesnt display server bonus daily rates.\r\n\r\n";
                selStr += "#b#e @exit#k#n\r\n";
                selStr += "     " + star + "Quickly sends you out of any instance your in.\r\n\r\n";
                selStr += "#b#e @level#k#n\r\n";
                selStr += "     " + star + "Shows your level and exp needed to level. Shows your Android's Level and Exp needed to level.\r\n\r\n";
                selStr += "#b#e @op#k#n\r\n";
                selStr += "     " + star + "Shows your damage range in full detail.\r\n\r\n";
                selStr += "#b#e @bonus#k#n\r\n";
                selStr += "     " + star + "Shows your bonus stats gained from completed achievements.\r\n\r\n";
                selStr += "#b#e @quest#k#n\r\n";
                selStr += "     " + star + "Shows your bonus stats gained from completed quests.\r\n\r\n";
                selStr += "#b#e @monster#k#n\r\n";
                selStr += "     " + star + "Shows stats of monsters on your map.\r\n\r\n";
                selStr += "#b#e @mobinfo#k#n\r\n";
                selStr += "     " + star + "Shows the item drop lists of all mobs on your map.\r\n\r\n";
                selStr += "#b#e @map#k#n\r\n";
                selStr += "     " + star + "Shows details of the map your in. Used for debug purposes.\r\n\r\n";
                selStr += "#b#e @pos#k#n\r\n";
                selStr += "     " + star + "Shows your position on the map your currently in. Used for debug purposes.\r\n\r\n";
                selStr += "#b#e @debug#k#n\r\n";
                selStr += "     " + star + "Shows various info about certain things.\r\n\r\n";
                selStr += "#b#e @stuck#k#n\r\n";
                selStr += "     " + star + "Warps your character to random spawn point on a map if your stuck.\r\n\r\n";
                selStr += "#b#e @loot#k#n\r\n";
                selStr += "     " + star + "Enables or Disable auto ETC-Storage looting.\r\n\r\n";
                selStr += "#b#e @buff#k#n\r\n";
                selStr += "     " + star + "Shows how much time you have left with your VIP Status.\r\n\r\n";
                selStr += "#b#e @raid#k#n\r\n";
                selStr += "     " + star + "Creates and shows info about your raid.\r\n";
                selStr += "     " + star + "Sub-Command @raid add -Name : Adds player to your raid.\r\n";
                selStr += "     " + star + "Sub-Command @raid kick -Name : Kicks player from your raid.\r\n";
                selStr += "     " + star + "Sub-Command @raid leader -Name : Changes leader of the raid.\r\n";
                selStr += "     " + star + "Sub-Command @raid leave : leaves raid or disbands your raid.\r\n\r\n";
                selStr += "#b#e @totem#k#n\r\n";
                selStr += "     " + star + "Despawns your totem.\r\n\r\n";
                selStr += "#b#e @instance#k#n\r\n";
                selStr += "     " + star + "Creates an instance of map your training on. Requires monsters on the map.\r\n\r\n";
                selStr += "#b#e @kaotic -Number#k#n\r\n";
                selStr += "     " + star + "Creates a peroonal instance of map your on and changes all monsters to X-Tier. Requires monsters on the map.\r\n\r\n";
                selStr += "#b#e @ap#k#n\r\n";
                selStr += "     " + star + "Assigns AP.\r\n\r\n";
                selStr += "#b#e @etc - @recycle - @job#k#n\r\n";
                selStr += "     " + star + "Opens npc based commands.\r\n\r\n";
                selStr += "#b#e @time#k#n\r\n";
                selStr += "     " + star + "Displays Server time.\r\n\r\n";
                selStr += "#b#e @storeEtc#k#n\r\n";
                selStr += "     " + star + "Dumps all ETC items in inventory into your ETC storage system.\r\n\r\n";
                selStr += "#b#e @hidedroid#k#n\r\n";
                selStr += "     " + star + "Shows or Hides your android. Used to avoid crashes on certain maps.\r\n\r\n";
                selStr += "#b#e @test -Level -Tier#k#n\r\n";
                selStr += "     " + star + "Warps you to a test map for damage testing.\r\n\r\n";

            }
            if (ach == 12) {
                selStr = star + "#r#eCustom Zones:#k#n\r\n";
                selStr += "     " + star + "Custom zones are all found in #bTemple of Time#k\r\n";
                selStr += "     " + star + "Level: 200-500 zones are all found in #bTemple of Time#k inside #bBLUE PORTAL#k\r\n";
                selStr += "     " + star + "Level: 500+ Arcane River zones are found in #bTemple of Time#k inside #bRED PORTAL#k\r\n";
            }
            if (ach == 13) {
                selStr = star + "#r#eCustom Jobs:#k#n\r\n";
                selStr += "     " + star + "This server uses re-skins jobs as custom jobs.\r\n";
                selStr += "     " + star + "Job system on this server is built for progression reasons and #rNOT GMS LIKE#k.\r\n";
                selStr += "     " + star + "Every level up gains +1 SP, Energy Charges also give +1 SP\r\n\r\n";

                selStr += star + "#r#eAdventure Jobs:#k#n\r\n";
                selStr += "     " + star + "Auto Job Level: 2nd job: 100\r\n";
                selStr += "     " + star + "Auto Job Level: 3rd job: 250\r\n";
                selStr += "     " + star + "Auto Job Level: 4th job: 500\r\n\r\n";

                selStr += star + "#r#eEvan Jobs:#k#n\r\n";
                selStr += "     " + star + "Auto Job Level: 2nd job: 50\r\n";
                selStr += "     " + star + "Auto Job Level: 3rd job: 100\r\n";
                selStr += "     " + star + "Auto Job Level: 4th job: 150\r\n";
                selStr += "     " + star + "Auto Job Level: 5th job: 200\r\n";
                selStr += "     " + star + "Auto Job Level: 6th job: 250\r\n";
                selStr += "     " + star + "Auto Job Level: 7th job: 300\r\n";
                selStr += "     " + star + "Auto Job Level: 8th job: 350\r\n";
                selStr += "     " + star + "Auto Job Level: 9th job: 400\r\n";
                selStr += "     " + star + "Auto Job Level: 10th job: 500\r\n\r\n";

                selStr += star + "#r#eCustom Jobs:#k#n\r\n";
                selStr += "     " + star + "Auto Job Level: 2nd job: 250\r\n";
                selStr += "     " + star + "Auto Job Level: 3rd job: 500\r\n";
                selStr += "     " + star + "Custom CK jobs have no 4th job\r\n";
                selStr += star + "#b#eKain (Cygnus Warrior):#k#n\r\n";
                selStr += "     " + star + "Does not have 4th job.\r\n\r\n";
                selStr += star + "#b#eKanna (Cygnus Mage):#k#n\r\n";
                selStr += "     " + star + "Does not have 4th job.\r\n\r\n";
                selStr += star + "#b#ePath Finder (Cygnus bowman):#k#n\r\n";
                selStr += "     " + star + "Does not have 4th job.\r\n\r\n";
                selStr += star + "#b#eNight Walker (Cygnus Thief):#k#n\r\n";
                selStr += "     " + star + "Does not have 4th job.\r\n\r\n";
                selStr += star + "#b#eArk (Cygnus Pirate):#k#n\r\n";
                selStr += "     " + star + "Does not have 4th job.\r\n\r\n";
                selStr += star + "#b#eGrand Master (GM):#k#n\r\n";
                selStr += "     " + star + "Very difficult job. Does not have 2-4th job. Uses all equips. Gains Base skills every level at random. Uses All stats for damage.\r\n\r\n";
            }
            if (ach == 14) {
                selStr = star + "#r#eLeveling:#k#n\r\n";
                selStr += "     " + star + "Leveling in this server is not like anything you have seen in other servers.\r\n";
                selStr += "     " + star + "Leveling starts very fast and slows deeper you go.\r\n";
                selStr += "     " + star + "Leveling has no limits, you can level forever.\r\n";
                selStr += "     " + star + "You will auto job-advance on level up, except for explore jobs where you need to use @job to advance for 2nd job.\r\n\r\n";
            }
            if (ach == 15) {
                selStr = star + "#r#eStamina:#k#n\r\n";
                selStr += "     " + star + "Everyone has 100 Stamina limit. This limit never changes.\r\n";
                selStr += "     " + star + "Stamina is used for various events and boss fights. This is to limit and prevent botting bosses.\r\n";
                selStr += "     " + star + "You regen 1 Stamina every 30 seocnds no matter where you are.\r\n";
                selStr += "     " + star + "You can greatly speed up stamina recover by fishing with various baits.\r\n\r\n";
            }
            if (ach == 16) {
                selStr = star + "#r#eFishing:#k#n\r\n";
                selStr += "     " + star + "You can buy fishing chair from MESO SHOP - RED BUTTON at bottom of your screen.\r\n";
                selStr += "     " + star + "You can obtain various baits from killing monsters or shops.\r\n";
                selStr += "     " + star + "You must place Bait in your #rFirst ETC SLOT#k in order to catch fish.\r\n";
                selStr += "     " + star + "Bait has X-Chance every 10 seconds to catch fish. Higher Tier bait has higher chances of catch.\r\n";
                selStr += "     " + star + "Different Tiers of bait will give different and better rewards.\r\n\r\n";
            }
            cm.sendNextS(selStr, 16);
        }

    } else if (status == 2) {
        status = 1;
        action(0, 0, 0);
    }
}