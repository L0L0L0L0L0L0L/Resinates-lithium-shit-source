var status = -1;

function start() {
	if (cm.getMapId() == 261010000) {
	    cm.playerMessage("Your name is on the list. You'll now be transported to the secret tunnel.");
	    cm.warp(261030000, "sp_jenu");
	} else {
	    cm.playerMessage("Your name is on the list. You'll now be transported to the secret tunnel.");
	    cm.warp(261030000, "sp_alca");
	}
	            
}

function action(mode, type, selection) {
                
}