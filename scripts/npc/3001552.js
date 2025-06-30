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
        cm.sendOk("Have you seen my daddy, hes been gone for very long time. I really miss him......");
    } else if (status == 1) {
        
    }
}