function enter(pi) {
    if (pi.getPlayer().isGM()) {
        pi.warp(pi.PortalMap(), pi.getPortal().getTarget());
    } else {
        if (pi.getPlayer().getMapId() == 5000) {//intro
            pi.gainEquip(1112920, 5);
            pi.gainEquip(1112920, 5);
            pi.gainEquip(1112920, 5);
            pi.gainEquip(1112920, 5);
            pi.gainItem(2005107, 1);
            pi.getPlayer().dropMessage(1, "You gained some starter items.");

            pi.warp(pi.PortalMap(), pi.getPortal().getTarget());
            pi.getPlayer().dropMessage(1, "Kill monsters until you reach level 10 then proceed to next map to pick your job.");
            return;
        }

        if (pi.getPlayer().getMapId() == 5003) {//kill 2
            if (pi.getPlayer().getTotalLevel() >= 10) {
                pi.warp(pi.PortalMap(), pi.getPortal().getTarget());
                return;
            } else {
                pi.getPlayer().dropMessage(1, "You must reach level 10 before moving on.");
            }
        }
    }
}