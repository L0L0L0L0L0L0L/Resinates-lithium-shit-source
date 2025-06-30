/*
 NPC Name: 		The Forgotten Temple Manager
 Map(s): 		Deep in the Shrine - Twilight of the gods
 Description: 		Pink Bean
 */

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
        cm.sendOk("My husband went off to war to stop Magnus. We haven't seen him back sense. He may have not survived the battle. Don't tell my son....");
    } else if (status == 1) {
        
    }
}