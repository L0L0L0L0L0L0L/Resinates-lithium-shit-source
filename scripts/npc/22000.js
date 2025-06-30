/* 
 NPC Name: 		Shanks
 Map(s): 		Maple Road : Southperry (60000)
 Description: 		Brings you to Victoria Island
 */
var status = 0;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
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
        if (cm.getPlayer().getVar("starter") <= 0) {
            cm.sendYesNo("Are you Ready to begin your journey?");
        } else {
            if (!cm.getPlayer().getAchievement(205)) {
                cm.sendOk("Go speak with #bSpiegelmann#k and Complete the Missions.\r\n#b"+star+" Make sure you scroll your gears.\r\n"+star+" Use your pets as they heal 10% HP/MP per Second.#k\r\n#r"+star+" Best Skill layout is Teleport (1), Holy Nova (10), Dual Blast (1).#k");
            } else {
                cm.sendYesNo("Are you ready to leave here begin your journey?");
            }
        }
    }
    if (status == 1) {
        if (cm.getPlayer().getVar("starter") <= 0) {
            cm.getPlayer().setLevel(10);
            cm.getPlayer().changeJob(910);
            cm.getPlayer().gainSP(25);
            cm.getPlayer().resetStats(4, 4, 4, 4);
            cm.gainStartEquip();
            cm.gainEquip(1112920, 10);
            cm.gainEquip(1112920, 10);
            cm.gainEquip(1112920, 10);
            cm.gainEquip(1112920, 10);
            cm.gainEquip(1102337, 10);
            cm.gainEquip(1662006, 4);
            cm.gainEquip(1672008, 4);
            cm.gainItem(2005107, 1);
            cm.gainItem(2049189, 250);
            cm.gainItem(2000005, 1000);
            cm.gainItem(cm.getRandomPet(), 1);
            cm.gainItem(cm.getRandomPet(), 1);
            cm.gainItem(cm.getRandomPet(), 1);
            if (!cm.getPlayer().hasSkin(7000)) {
                cm.getPlayer().gainDamageSkinNoOrb(7000);
            }
            cm.getPlayer().finishAchievement(906);
            cm.getPlayer().changeSingleSkillLevel(8, 1);
            cm.getPlayer().changeSingleSkillLevel(10000018, 1);
            cm.getPlayer().changeSingleSkillLevel(20000024, 1);
            cm.getPlayer().changeSingleSkillLevel(20011024, 1);
            cm.getPlayer().changeSingleSkillLevel(20021024, 1);
            cm.getPlayer().changeSingleSkillLevel(30001024, 1);
            cm.getPlayer().changeSingleSkillLevel(30011024, 1);
            cm.getPlayer().changeSingleSkillLevel(3101003, 1);
            var selStr = "\r\n#bStarter Items#k:\r\n";
            selStr += "#i1112920# #i1662006# #i1672008# #i4420015# #i5450100#\r\n";
            selStr += "#i2049189# #i2005107# #i1102337# #i2049305# #i2049189#\r\n";
            selStr += "#rNo speak with Spiegelmann to start the Tutorial Missions. Once you clear them all come back to me.#k\r\n";
            cm.getPlayer().resetCore();
            var m = cm.getPlayer().getVarZero("Main");
            var s = cm.getPlayer().getVarZero("Sub");
            var a = cm.getPlayer().getVarZero("Attack");
            selStr += star + "#bCore Stats: Main: " + stat[m] + " - Sub: " + stat[s] + " - Type: " + atk[a];
            cm.getPlayer().setVar("starter", 1);
            cm.sendOkS("Here is some starting items that will help your journey.\r\n" + selStr, 2);
            return;
        } else {
            if (!cm.getPlayer().getAchievement(205)) {
                cm.sendOkS("I need to speak with Spiegelmann to start the Tutorial Missions. Once I clear them all, I speak to #bShanks#k." + selStr, 2);
            } else {
                cm.gainItem(5450100, 1);
                cm.getPlayer().gainStamina(500, true);
                cm.getPlayer().makeEggEvo(4, 200);
                cm.gainItem(4420015, 5);
                cm.getPlayer().finishAchievement(3000);
                cm.warp(870000008);
                cm.sendOk("Talk to Demian the Guide, else head up to room 6 to train more.");
                return;
            }
        }
    }
}