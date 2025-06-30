/* @Author Lerk
 * 
 * 9208000.js: Guild Quest - Gatekeeper Puzzle Reactor
 * 
 */

function act() {
    if (rm.getReactor().getState() == 4) {
        var eim = rm.getEventInstance();
        if (eim != null) {
            eim.setValue("orbs", eim.getValue("orbs") - 1);
            var map = eim.getMapInstance(rm.getMap().getId());
            if (eim.getValue("orbs") <= 0 && !rm.getMap().getClear()) {
                map.broadcastMapMsg("All the evil pumpkins have been destoryed.", 5120182);
                map.showClear();
                map.killAllMonsters(true);
            } else {
                map.broadcastMapMsg("Pumpkin destroyed. " + eim.getValue("orbs") + " Remaining", 5120182);
                map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9500195, eim.getValue("level"), eim.getValue("scale"), false, false, false, true, 100), rm.getReactor().getPosition());
                map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9500195, eim.getValue("level"), eim.getValue("scale"), false, false, false, true, 100), rm.getReactor().getPosition());
                map.spawnMonsterOnGroundBelow(eim.getKaoticMonster(9500195, eim.getValue("level"), eim.getValue("scale"), false, false, false, true, 100), rm.getReactor().getPosition());
            }
        }
    }
//rm.mapMessage(6,""+rm.getReactor().getObjectId());
}