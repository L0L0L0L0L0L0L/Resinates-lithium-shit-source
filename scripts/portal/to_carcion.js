function enter(pi) {
    if (pi.getPlayer().achievementFinished(438)) {
        pi.warp(410007602,"pt_west00");
        return true;
    } else {
        pi.playerMessage(5, "This zone requires Extreme Lotus to be defeated.");
    }
    return false;
}