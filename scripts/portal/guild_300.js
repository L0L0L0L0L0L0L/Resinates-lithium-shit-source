function enter(pi) {
    if (pi.getPlayer().achievementFinished(39)) {
        pi.warp(870001200,"east00");
    } else {
        pi.playerMessage(5, "I need to clear Cygnues EMpress before I can access this portal.");
    }
    return false;
}