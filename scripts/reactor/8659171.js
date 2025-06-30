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
    var yellow = rm.getReactor().getMap().getObjectInt("Y") - 1;
    if (rm.getReactor().getState() == yellow) {
        rm.getReactor().getMap().setObjectFlag("Yclear", true);
    } else {
        rm.getReactor().getMap().setObjectFlag("Yclear", false);
    }
    if (rm.getReactor().getMap().getObjectFlag("Rclear") && rm.getReactor().getMap().getObjectFlag("Yclear") && rm.getReactor().getMap().getObjectFlag("Bclear")) {
        rm.getReactor().getMap().broadcastMapMsg("Portal is now open!", star);
        rm.getReactor().getMap().showClear();
        rm.getReactor().getMap().lockReactors(true);
    }
//rm.mapMessage(6,""+rm.getReactor().getObjectId());
}