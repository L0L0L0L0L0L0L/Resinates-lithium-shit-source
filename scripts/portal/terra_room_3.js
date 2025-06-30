function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
        pi.getMap().setObjectFlag("portal_3", false);
        pi.warp(4505, 0);
    } else {
        pi.warp(4500, 0);
    }
}