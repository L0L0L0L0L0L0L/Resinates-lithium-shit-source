function enter(pi) {
    var eim = pi.getPlayer().getEventInstance();
    if (eim != null) {
        if (pi.getPlayer().getMap().allMonsterDead()) {
            var map = pi.getPlayer().getMap().getId();
            if (map == 410007750) {
                if (eim.getValue("limbo_clear") > 0) {
                    pi.warp(pi.PortalMap(), "enter00");
                    return true;
                } else {
                    pi.playerMessage(5, "Theres Limbo Bosses still alive.");
                }
            } else {
                pi.warp(pi.PortalMap(), "enter00");
                return true;
            }
        } else {
            pi.playerMessage(5, "Clear all the monster on the map to proceed.");
        }
    } else {
        pi.playerMessage(5, "Portal has error.");
    }
    return false;
}