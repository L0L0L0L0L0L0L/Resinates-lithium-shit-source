function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
        eim.exitPlayer(pi.getPlayer(), 866000150);
    } else {
         pi.warp(866000150,0);
    }
   
}