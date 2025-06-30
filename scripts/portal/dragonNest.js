function enter(pi) {
    if (pi.haveItem(4001094)) {
	pi.playPortalSE();
		pi.warp(240040611, "sp");
    } else {
	pi.playerMessage(5, "In order to enter the premise, you'll need to have the Nine Spirit's Egg in possession.");
    }
}