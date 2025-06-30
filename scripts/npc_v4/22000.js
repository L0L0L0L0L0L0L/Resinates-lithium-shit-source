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
        if (cm.getPlayer().getTotalLevel() < 10) {
            cm.sendOk("What? You're telling me you wanted to go without finishing your training!! Speak to me when you have finished all your quests. Go talk to Sen.");
        } else {
            cm.sendYesNo("Are you ready to set sail and begin your journey?");
        }
    } else if (status == 1) {
        if (cm.getPlayer().getJob() == 0) {
            var base = "\r\n\#L0# Warrior #l\r\n\#L1# Mage #l\r\n\#L2# Bowman #l\r\n\#L3# Thief #l\r\n\#L4# Pirate #l\r\n\#L5# Cannoneer #l\r\n\#L6# Dual Blade #l\r\n\#L7# Super Grand Master #l";
            base += "\r\n\#L8# #rAran#k #l";
            base += "\r\n\#L9# #rJett#k (Mercedes)#l";
            cm.sendSimple("Before you can leave you need to select which job you like to advance to?\r\n\Once you arrive at your home town, make sure you go buy some starting equippment." + base);
        } else if (cm.getPlayer().getJob() == 1000) {
            var selStr = "";
            selStr += "\r\n\#L14#Kain#l\r\n\ ";
            selStr += "\r\n\#L10#Kanna#l\r\n\ ";
            selStr += "\r\n\#L11#Path Finder#l\r\n\ ";
            selStr += "\r\n\#L12#Night Walker#l\r\n\ ";
            selStr += "\r\n\#L13#Ark#l\r\n\ ";
            //cm.sendSimple("Before you can leave you need to select which job you like to advance to?\r\n\Once you arrive at your home town, make sure you go buy some starting equippment.\r\n\#L10# Battle Mage #l\r\n\#L11# Wild Hunter #l\r\n\#L12# Mechanic #l");
            cm.sendSimple("Before you can leave, you need to select which job you like to advance to.\r\n\Once you arrive at your home town, #rmake sure you go buy some starting equippment#k.\r\n" + selStr);
        } else if (cm.getPlayer().getJob() == 2001) {
            cm.sendSimple("Before you can leave you need to select which job you like to advance to?\r\n\Once you arrive at your home town, make sure you go buy some starting equippment.\r\n\#L50# Evan #l");
        } else if (cm.getPlayer().getJob() == 3000) {
            var selStr = "";
            selStr += "\r\n\#L20# Battle Mage #l";
            selStr += "\r\n\#L21# Wild Hunter #l";
            selStr += "\r\n\#L22# Burster (Mechanic) #l";
            cm.sendSimple("Before you can leave you need to select which job you like to advance to?\r\n\Once you arrive at your home town, make sure you go buy some starting equippment." + selStr);
        } else if (cm.getPlayer().getJob() == 3001) {
            //cm.sendOk("What? You're telling me you wanted to go without finishing your training!! Speak to me when Demon Slayers are unlocked.");
            cm.sendSimple("Before you can leave you need to select which job you like to advance to?\r\n\Once you arrive at your home town, make sure you go buy some starting equippment.\r\n\#L30# Demon Slayer #l");
        } else if (cm.getPlayer().getJob() == 2002) {
            cm.sendSimple("Before you can leave you need to select which job you like to advance to?\r\n\Once you arrive at your home town, make sure you go buy some starting equippment.\r\n\#L40# Jett (Mercedes) #l");
        }
    } else if (status == 2) {
        if (cm.getPlayer().getJob() == 0 || cm.getPlayer().getJob() == 1000 || cm.getPlayer().getJob() == 2001 || cm.getPlayer().getJob() == 3000 || cm.getPlayer().getJob() == 3001 || cm.getPlayer().getJob() == 2300) {
            //cm.getPlayer().superbuff();
            if (selection == 99) {
                cm.sendOk("This job is current locked. There must be secert somewhere to unlock this job.");
                return;
            }
            cm.getPlayer().resetStats(4, 4, 4, 4);
            if (selection == 0) {

                cm.getPlayer().changeJob(100);
            } else if (selection == 1) {

                cm.getPlayer().changeJob(200);
            } else if (selection == 2) {

                cm.getPlayer().changeJob(300);
            } else if (selection == 3) {

                cm.getPlayer().changeJob(400);
            } else if (selection == 4) {

                cm.getPlayer().changeJob(500);
            } else if (selection == 5) {//cannoneer

                cm.getPlayer().changeJob(501);
            } else if (selection == 6) {

                cm.getPlayer().changeJob(430);
            } else if (selection == 7) {

                cm.getPlayer().changeJob(900);
            } else if (selection == 99) {

                cm.getPlayer().changeJob(900);
            } else if (selection == 14) {

                cm.getPlayer().changeJob(1100);
            } else if (selection == 10) {

                cm.getPlayer().changeJob(1200);
            } else if (selection == 11) {

                cm.getPlayer().changeJob(1300);
            } else if (selection == 12) {

                cm.getPlayer().changeJob(1400);
            } else if (selection == 13) {

                cm.getPlayer().changeJob(1500);
            } else if (selection == 20) {

                cm.getPlayer().changeJob(3200);
            } else if (selection == 21) {

                cm.getPlayer().changeJob(3300);
                cm.getPlayer().setJag();
            } else if (selection == 22) {

                cm.getPlayer().changeJob(3500);
            } else if (selection == 30) {

                cm.getPlayer().changeJob(3100);
            } else if (selection == 40) {

                cm.getPlayer().changeJob(2300);
            } else if (selection == 50) {

                cm.getPlayer().changeJob(2200);
            } else if (selection == 8) {

                cm.getPlayer().changeJob(2100);
            } else if (selection == 9 || selection == 40) {

                cm.getPlayer().changeJob(2300);
            }
            cm.warp(5006);
            cm.gainStartEquip();
            cm.gainEquip(1662006, 10);
            cm.gainEquip(1672008, 10);
            cm.gainItem(4310500, 1);
            cm.gainItem(4420015, 10);
            cm.gainItem(2430131, 100);
            cm.gainItem(5450100, 1);
            cm.gainItem(cm.getRandomPet(), 1);
            cm.getPlayer().finishAchievement(906);
            cm.getPlayer().finishAchievement(3000);
            cm.getPlayer().gainDamageSkinNoOrb(6001);
            cm.getPlayer().makeEggEvo(4, 200);
            if (cm.getPlayer().getAccVara("POT") <= 0) {
                cm.getPlayer().addAccVar("POT", 1);
            }
            if (cm.getPlayer().getAccVara("I_POT") <= 0) {
                cm.getPlayer().addAccVar("I_POT", 1);
            }
            var selStr = "\r\n";
            selStr += star + "#bStarting Weapons#k\r\n";
            selStr += star + "#i1662006##i1672008# #bAndriod and Heart#k\r\n";
            selStr += star + "#i4310500# #bGolden Meso Bag#k (Quick Move -> Bank)\r\n";
            selStr += star + "#i4420015# #bKaotic Reward Ticket#k (Use at Perion -> Mia)\r\n";
            selStr += star + "#bRandom Starting Pet#k\r\n";
            selStr += star + "#bStarter Ultimate Skin#k (@skin to apply)\r\n";
            selStr += star + "#bRandom Starter Pal#k (Speak with Oak in FM)\r\n";
            cm.sendOkS("Here is some starting items that will help your journey.\r\n" + selStr, 2);
        }
    }
}