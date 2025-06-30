function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null && eim.getIntProperty("clear") > 0) {
	pi.playPortalSE();
	eim.warpEventTeam(350060160);
    } else {
	pi.playerMessage(5, "All crates have not been destoryed.");
    }
}