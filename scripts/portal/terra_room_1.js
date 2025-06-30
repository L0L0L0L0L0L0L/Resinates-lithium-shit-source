function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
        pi.getMap().setObjectFlag("portal_1", false);
        pi.warp(4503, 0);
    } else {
        pi.warp(4500, 0);
    }
}