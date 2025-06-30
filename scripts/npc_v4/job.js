/* 
 NPC Name: 		Shanks
 Map(s): 		Maple Road : Southperry (60000)
 Description: 		Brings you to Victoria Island
 */
var status = -1;
var option = 0;

function action(mode, type, selection) {
    if (status >= 0 && mode == 0) {
                    
        return;
    }
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        if (cm.getPlayer().getTotalLevel() == 100 && cm.getPlayer().getJobBranch() == 1) {
            option = 1;
            if (cm.getJob() == 100) {
                cm.sendSimple("Which Warrior Job would you like to advance to?\r\n\#L1# Fighter #l\r\n\#L2# Page #l\r\n\#L3# Spearman #l");
            } else if (cm.getJob() == 200) {
                cm.sendSimple("Which Mage Job would you like to advance to?\r\n\#L1# Fire #l\r\n\#L2# Ice #l\r\n\#L3# Holy #l");
            } else if (cm.getJob() == 300) {
                cm.sendSimple("Which Bowman Job would you like to advance to?\r\n\#L1# Bowman #l\r\n\#L2# X-Bowman #l");
            } else if (cm.getJob() == 400) {
                cm.sendSimple("Which Theif Job would you like to advance to?\r\n\#L1# Assassin #l\r\n\#L2# Bandit #l");
            } else if (cm.getJob() == 500) {
                cm.sendSimple("Which Pirate Job would you like to advance to?\r\n\#L1# Brawler #l\r\n\#L2# Gunslinger #l");
            }
        } else {
            cm.sendOk("Hmm... I guess you still have things to do here?");
                        
        }
    } else if (status == 1) {
        if (option == 1) {
            cm.getPlayer().changeJob(cm.getJob() + (10 * selection));
        }
        if (option == 2) {
            cm.getPlayer().changeJob(cm.getJob() + 1);
        }
        if (option == 3) {
            cm.getPlayer().changeJob(430);
        }
        cm.sendOk("You can now continue on your journey");
                    
    }
}