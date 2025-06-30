/*
	Keroben - Leafre Cave of life - Entrance
*/

var morph;
var status = -1;

function action(mode, type, selection) {
    if (mode == -1) {
                    
    } else {
        if (mode == 0 && type > 0) {
                        
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

    if (status == 0) {
		if (cm.haveItem(4001402, 1)) {
			cm.sendYesNo("You want to get past the dragon guard to Horntail's Lair?");
		} else {
			cm.sendOk("Sorry, you dont have enough Dragon's Essence. You can obtain this back on Nest of Dead Dragon Cave in bottom right.");
                        
		}
    } else if (status == 1) {
		cm.gainItem(4001402, -1);
		cm.useItem(2210003);
		cm.sendOk("You can jump down and talk to guard to enter the Lair.");
		            
    }
	}
}