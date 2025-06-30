/* @Author Lerk
 * 
 * 9208000.js: Guild Quest - Gatekeeper Puzzle Reactor
 * 
 */

function act() {
    var eim = rm.getEventInstance();
    var star = 5120205;
    if (eim != null) {
        star = eim.getValue("star");
    }
    var blue = rm.getReactor().getMap().getObjectInt("B") - 1;
    if (rm.getReactor().getState() == blue) {
        rm.getReactor().getMap().setObjectFlag("Bclear", true);
    } else {
        rm.getReactor().getMap().setObjectFlag("Bclear", false);
    }
    if (rm.getReactor().getMap().getObjectFlag("Rclear") && rm.getReactor().getMap().getObjectFlag("Yclear") && rm.getReactor().getMap().getObjectFlag("Bclear")) {
        rm.getReactor().getMap().broadcastMapMsg("Portal is now open!", star);
        rm.getReactor().getMap().showClear();
        rm.getReactor().getMap().lockReactors(true);
    }
//rm.mapMessage(6,""+rm.getReactor().getObjectId());
}