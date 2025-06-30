function enter(pi) {
    if (pi.getPlayer().isGM() || (pi.getPlayer().achievementFinished(408) && pi.getPlayer().getTotalLevel() >= 7000)) {
        pi.warp(863000100,"ra10");
    } else {
        pi.playerMessage(5, "I need to clear Kobold and Reach level 7000+ before I can access this portal.");
    }
}