/* @Author Lerk
 * 
 * 9208000.js: Guild Quest - Gatekeeper Puzzle Reactor
 * 
 */

function act() {
    if (rm.getPlayer().getReactorDelay() < 1) {
        if (rm.haveItem(4030036, 100)) {
            rm.gainItem(4030036, -100);
            rm.gainItem(4030034, 12);
            rm.getPlayer().setReactorDelay(1);
        } else {
            rm.getPlayer().dropMessage("Not enough seeds to plant..");
        }
    } else {
        rm.getPlayer().dropMessage("This object is not ready yet...");
    }
}