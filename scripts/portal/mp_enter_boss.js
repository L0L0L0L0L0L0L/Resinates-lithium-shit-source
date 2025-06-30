function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
        if (eim.getValue("clear") >= 17) {
            pi.playPortalSE();
            eim.warpFullEventTeam(4701);
            return true;
        }
    }
    //pi.warp(4700); // fourth tower
    return false;

}