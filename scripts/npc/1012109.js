/* 
 NPC Name: 		Shanks
 Map(s): 		Maple Road : Southperry (60000)
 Description: 		Brings you to Victoria Island
 */
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

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
        if (cm.getPlayer().getJob() >= 3300 && cm.getPlayer().getJob() <= 3312) {
            cm.getPlayer().setJag();
            cm.sendOk("Your Jaguar has been updated.");
        } else {
            cm.sendYesNo("Would you like to travel to candy factory?");
        }
                    
    } else if (status == 1) {
        cm.warp(980040000, 0);
    }
}