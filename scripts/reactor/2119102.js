/* @Author Lerk
 * 
 * 9208000.js: Guild Quest - Gatekeeper Puzzle Reactor
 * 
 */

function act() {
    if (rm.getReactor().getState() == 1) {
        var eim = rm.getEventInstance();
        if (eim != null) {
            eim.setValue("kids", eim.getValue("kids") - 1);
            var map = eim.getMapInstance(4404);
            if (eim.getValue("kids") <= 0 && !rm.getMap().getClear()) {
                map.broadcastMapMsg("All the dead children's souls are now free. The Secret door code is " + (eim.getValue("door") + 1), 5120182);
                map.showClear();
            } else {
                map.broadcastMapMsg("Child's Soul has been set free!", 5120182);
            }
        }
    }
//rm.mapMessage(6,""+rm.getReactor().getObjectId());
}