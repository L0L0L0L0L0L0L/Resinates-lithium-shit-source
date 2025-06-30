/* @author RonanLana */
var level = 250;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function enter(pi) {
    var em = pi.getEventManager("bellDoor");
    if (em != null) {
        if (pi.getPlayer().getStamina() >= 10) {
        if (pi.getPlayer().isGroup()) {
            if (pi.getPlayer().isLeader()) {
                if (em.getEligibleParty(pi.getPlayer(), level)) {
                    if (!em.startPlayerInstance(pi.getPlayer())) {
                        pi.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        pi.playPortalSE();
                        pi.getPlayer().removeStamina(10);
                        return true;
                    }
                } else {
                    pi.playerMessage(5, "You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
                }
            } else {
                pi.playerMessage(1, "Only the leader of party can start the instance.");
            }
        } else {
            pi.playerMessage(1, "Event is Party/Raid Mode.");
            pi.playerMessage(5, "Party [minlvl=" + level + ", minplayers=" + minparty + ", maxplayers=" + maxparty + "]");
            pi.playerMessage(5, "Raid [minlvl=" + level + ", minplayers=" + minraid + ", maxplayers=" + maxraid + "]");
        }
        } else {
            pi.playerMessage(1, "You do not have enough stamina to enter the event, the cost is 10 Stamina.");
        }
    } else {
        pi.playerMessage(5, "Event has already started, Please wait.");
    }
    return false;
}
