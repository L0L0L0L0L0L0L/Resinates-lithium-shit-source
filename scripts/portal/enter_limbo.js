function enter(pi) {
    if (pi.getPlayer().achievementFinished(440)) {
        pi.warp(410007612,"west00");
        return true;
    } else {
        pi.playerMessage(5, "This zone requires Limbo to be defeated.");
    }
    return false;
}