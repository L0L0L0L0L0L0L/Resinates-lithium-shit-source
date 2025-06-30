function enter(pi) {
    pi.playPortalSE();
    var eim = pi.getEventInstance();
    if (eim != null) {
        eim.exitPlayer(pi.getPlayer(), eim.getValue("exit"));
    } else {
        pi.warp(951000000, "sp00"); // fourth tower
    }

    return true;
}