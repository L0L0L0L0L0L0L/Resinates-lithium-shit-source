function enter(pi) {
    var mapid = pi.getPlayer().getMap().getId();
    var eim = pi.getPlayer().getEventInstance();
    if (eim != null) {
        if (pi.getPlayer().getMap().getClear()) {
            pi.warp(pi.PortalMap(), pi.TargetPortal());
            return true;
        } else {
            pi.playerMessage(5, "Must clear all monsters to preceed.");
        }
    } else {
        pi.playerMessage(5, "Portal has error.");
    }
    return false;
}