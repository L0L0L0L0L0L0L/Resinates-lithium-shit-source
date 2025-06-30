var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status == 0) {
	                
	}
	status--;
    }
    if (status == 0) {
	cm.sendYesNo("Would you like to get out?");
    } else if (status == 1) {
	cm.warp(980010000,0);
	            
    }
}