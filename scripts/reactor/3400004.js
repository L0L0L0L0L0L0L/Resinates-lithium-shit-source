/* @Author Lerk
 * 
 * 9208000.js: Guild Quest - Gatekeeper Puzzle Reactor
 * 
 */

function act() {
    if (rm.getReactor().getState() == 9) {
        var eim = rm.getEventInstance();
        if (eim != null) {
            var count = 200, drop = 1;
            if (eim.getValue("scale") == 1) {
                eim.gainPartyItem(4420015, 5);
            }
            if (eim.getValue("scale") == 2) {
                count = 300;
                eim.gainPartyItem(4420015, 10);
            }
            if (eim.getValue("scale") == 3) {
                count = 400;
                eim.gainPartyItem(4420015, 25);
            }
            if (eim.getValue("scale") == 4) {
                count = 500;
                eim.gainPartyItem(4420015, 50);
            }
            eim.gainPartyItem(4310150, count * eim.getPlayerCount());
            eim.victory(4300);
        }
    }
//rm.mapMessage(6,""+rm.getReactor().getObjectId());
}