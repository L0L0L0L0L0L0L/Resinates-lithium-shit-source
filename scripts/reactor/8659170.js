/* @Author Lerk
 * 
 * AC Red reactor
 * 
 */

function act() {
    var eim = rm.getEventInstance();
    var star = 5120205;
    if (eim != null) {
        star = eim.getValue("star");
    }
    var red = rm.getReactor().getMap().getObjectInt("R") - 1;
    if (rm.getReactor().getState() == red) {
        rm.getReactor().getMap().setObjectFlag("Rclear", true);
    } else {
        rm.getReactor().getMap().setObjectFlag("Rclear", false);
    }
    if (rm.getReactor().getMap().getObjectFlag("Rclear") && rm.getReactor().getMap().getObjectFlag("Yclear") && rm.getReactor().getMap().getObjectFlag("Bclear")) {
        rm.getReactor().getMap().broadcastMapMsg("Portal is now open!", star);
        rm.getReactor().getMap().showClear();
        rm.getReactor().getMap().lockReactors(true);
    }
//rm.mapMessage(6,""+rm.getReactor().getObjectId());
}