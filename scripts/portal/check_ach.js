function enter(pi) {
    if (pi.getPlayer().isGM()) {
        pi.warp(pi.PortalMap(), pi.getPortal().getTarget());
    } else {
        if (pi.getPlayer().getMapId() == 450012500) {//intro
            if (pi.getPlayer().achievementFinished(400)) {
                pi.warp(pi.PortalMap(), pi.getPortal().getTarget());
                return;
            } else {
                pi.getPlayer().dropMessage(1, "You defeat Mori Ranamru to progress.");
            }
        }

        if (pi.getPlayer().getMapId() == 610050000) {//jump
            if (pi.getPlayer().achievementFinished(185)) {
                pi.warp(pi.PortalMap(), pi.getPortal().getTarget());
                return;
            } else {
                pi.getPlayer().dropMessage(1, "You must complete this map's quest from Angel to progress.");
            }
        }
    }
}