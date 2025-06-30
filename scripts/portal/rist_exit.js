function enter(pi) {
    if (pi.getPlayer().achievementFinished(410)) {
        pi.warp(410000402,"HP00");
        return true;
    } else {
        pi.playerMessage(5, "Zoolen requires the Spire Boss to be defeated.");
    }
    return false;
}