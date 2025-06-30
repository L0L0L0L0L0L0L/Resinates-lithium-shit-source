function enter(pi) {
    if (pi.getPlayer().achievementFinished(2001)) {
        pi.warp(211080000,"west00");
    } else {
        pi.playerMessage(5, "Speak with Ifia.");
    }
    
}