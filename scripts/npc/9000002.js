function start() {
	var map = cm.getSavedLocation("EVENT");
	if (map > -1 && map != cm.getMapId()) {
		cm.warp(map, 0);
	} else {
    		cm.warp(910000000, 0);
	}
                
}

function action(mode, type, selection) {
}
