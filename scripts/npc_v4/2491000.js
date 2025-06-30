/* 
 NPC Name: 		Shanks
 Map(s): 		Maple Road : Southperry (60000)
 Description: 		Brings you to Victoria Island
 */
var status = 0;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";

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
        if (cm.getPlayer().getMapId() == 5006) {
            cm.sendOkS("You should have recieved #i5450100#, You use it to #bsave this map#k, so that you can come back here when you need anything.\r\n\r\nYou should have also recieved #b100#k #i2430131#, Open up your inventory and double them to use them.\r\n#bNote: It is advised to place AP in HP before any other stats.\r\nEquips will cover out base stats for damage.#k\r\n\r\n#rNow head over speak with old man on how reward systems work.#k", 2);
        }
        if (cm.getPlayer().getMapId() == 5007) {
            cm.sendOkS("This map is about learning what MaplePals are.\r\nSpeak with Prof Oak about the system.\r\nClick on #bPal Balls#k to collect new eggs to hatch.", 2);
        }
        if (cm.getPlayer().getMapId() == 5008) {
            cm.sendOkS("This map is all about learning how to upgrade your equips to get stronger.\r\nYou can head back to the first map to collect more resources for upgrading here.\r\n#bYou always be upgrading and scrolling equips and replacing them over time, never be afraid to upgrade trash gear to progress.#k", 2);
        }
        if (cm.getPlayer().getMapId() == 5009) {
            cm.sendOkS("This map is all about learning how to use Damage Skins.\r\nSpeak with npcs about what skins you can collect and upgrade.\r\n#bYou can apply damage skins using @skin#k\r\nYou can head back to the first map to collect more resources for upgrading skins here.", 2);
        }
        if (cm.getPlayer().getMapId() == 5010) {
            cm.sendOkS("This map is learning about how train on maps and using\r\n#b@instance#k to create your own personal instance.\r\nYou will also find that random Pal Balls will spawn on natural monster maps, they provide useful crafting items for pal system.\r\n\r\n#bJob Advancements are automatic at level 100-250-500\r\nAdventure jobs need to use #b@job#k at level 100 to pick sub job#k\r\n\r\nYou can check #b@item#k or #b@etc#k for looted USE and ETC items.\r\nUse #b@recycle#k to recycle equips or use #b@loot#k to auto-recycle.\r\nAll Use and ETC items automaticly go into overflow storage system.\r\n\r\n#rUse @mobinfo to see item drops of monsters on current map.#k\r\n#bRARE#k item tag means that Drop Rate and ETC% does not effect the drop.", 2);
        }
        cm.sendOkS("Something you need?", 2);
    } else if (status == 1) {
    }
}