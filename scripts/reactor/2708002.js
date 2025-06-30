/* @Author Lerk
 * 
 * 9208000.js: Guild Quest - Gatekeeper Puzzle Reactor
 * 
 */

function act() {
    if (rm.getReactor().getState() == 5) {
        var eim = rm.getEventInstance();
        if (eim != null) {
            eim.setValue("orbs", eim.getValue("orbs") - 1);
            if (eim.getValue("orbs") <= 0 && !rm.getMap().getClear()) {
                rm.getMap().broadcastMapMsg("All the Demon Orbs has been destroyed. Map is Clear to progress.", 5120182);
                eim.getMapInstance(rm.getMap().getId()).showClear();
            } else {
                rm.getMap().broadcastMapMsg("Demon Orb has been destroyed. " + eim.getValue("orbs") + " Remaining", 5120182);
            }
        }
    }
//rm.mapMessage(6,""+rm.getReactor().getObjectId());
}