function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
        if (pi.getPlayer().getMapId() == 450010500) {
            eim.exitPlayer(pi.getPlayer(), 450011580);
        } else {
            eim.exitPlayer(pi.getPlayer(), 450011990);
        }
    } else {
        if (pi.getPlayer().getMapId() == 450010500) {
            pi.warp(450011580);//necro bosses
        } else {
            pi.warp(450011990);//hilla
        }
    }
    return true;
}