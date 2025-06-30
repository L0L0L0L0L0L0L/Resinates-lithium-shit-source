function enter(pi) {
    var mapid = pi.getPlayer().getMap().getId();
    var eim = pi.getPlayer().getEventInstance();
    if (eim != null) {
        if (pi.getPlayer().getMap().getClear()) {
            pi.warp(pi.PortalMap(), pi.TargetPortal());
            return true;
        } else {
            if (pi.getMap().getPlayerCount() == eim.getPlayerCount()) {
                pi.getPlayer().getMap().showClear();
                pi.warp(pi.PortalMap(), pi.TargetPortal());
                if (eim.getValue("boss") == 0) {
                    eim.setValue("boss", 1);
                }
                return true;
            } else {
                pi.playerMessage(5, "All players must be here to trigger the clear flag");
            }
        }
    } else {
        pi.playerMessage(5, "Portal has error.");
    }
    return false;
}