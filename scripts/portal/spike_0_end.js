function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
        if (pi.getPlayer().getMap().getStart()) {
            if (pi.getPlayer().getMap().getClear()) {
                pi.warp(270000000);
            } else {
                pi.getPlayer().changeMap(pi.getPlayer().getMapId(), "start");
            }
        } else {
            pi.playerMessage(5, "Event has not started.");
        }
    } else {
        pi.playerMessage(5, "Event has not started.");
    }
}