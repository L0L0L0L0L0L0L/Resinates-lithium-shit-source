function enter(pi) {
    if (pi.getPlayer().getMapId() == 2004) {
        pi.getPlayer().changeMap(pi.getPlayer().getMapId(), "tp01");
        return;
    }
    if (pi.getPlayer().getMap().getStart()) {
        pi.getPlayer().changeMap(pi.getPlayer().getMapId(), "tp01");
        if (pi.getPlayer().getMapId() != 2003) {
            pi.getPlayer().cancelAllBuffs();
        }
    } else {
        //pi.getPlayer().changeMap(pi.getPlayer().getMapId(), "tp01");
        pi.playerMessage(5, "Event has not started.");
    }
}