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
		if (cm.haveItem(4000244, 2500)) {
			cm.sendYesNo("Exchange 2500 dragon spirits for access beyond dragons nest?");
		} else {
			cm.sendOk("Sorry, you dont have enough dragon spirits. I require at least 2500 spirits.");
                        
		}
    } else if (status == 1) {
		cm.gainItem(4000244, -2500);
		cm.gainItem(4001402, 1);
		cm.sendOk("Take this Essence to Moira at cave of life.");
		            
    }
	}
}