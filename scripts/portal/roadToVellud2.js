function enter(pi) {
    if (pi.getPlayer().isGM() || (pi.getPlayer().achievementFinished(157) && pi.getPlayer().achievementFinished(158))) {
        pi.warp(863010000,"out00");
    } else {
        pi.playerMessage(5, "I need to clear the Mini-Bosses in the grove before I can access this portal.");
    }
}