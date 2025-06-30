function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
        eim.exitPlayer(pi.getPlayer(), 450009300);
    } else {
        pi.warp(450009300);
    }
    return true;
}