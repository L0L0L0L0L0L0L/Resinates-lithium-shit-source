/*
 Keroben - Leafre Cave of life - Entrance
 */

var morph;
var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
        if (status == 0) {
            morph = cm.getMorphState();
            if (morph == 2210003) {
                cm.sendNextS("Oh, my Brother! Don't worry about human's invasion. I'll protect you all. Come in.", 1);
            } else {
                cm.sendNextS("That's far enough, human! No one is allowed beyond this point. Get away from here!", 1);
            }
        } else if (status == 1) {
            if (morph == 2210003) {
                cm.cancelItem(2210003);
                cm.warp(240050000);
            } else {
                cm.warp(240040600, "st00");
            }

        }
    }