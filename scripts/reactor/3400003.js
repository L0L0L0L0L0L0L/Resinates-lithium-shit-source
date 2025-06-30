/* @Author Lerk
 * 
 * 9208000.js: Guild Quest - Gatekeeper Puzzle Reactor
 * 
 */

function act() {
    var eim = rm.getEventInstance();
    if (eim != null) {
        if (rm.getReactor().getState() == 9 && !rm.getMap().getClear()) {
            rm.getMap().broadcastMapMsg("The Great Billy has appeared.", 5120150);
            rm.getMap().changeMusic("BgmFF9/Ambush");
            var mob = eim.getKaoticMonster(1003, eim.getValue("level"), eim.getValue("level") * 0.01, true, false, true, true, 1000);
            rm.getMap().spawnMonsterOnGroundBelow(mob, rm.getReactor().getPosition());
        }
    }

//rm.mapMessage(6,""+rm.getReactor().getObjectId());
}