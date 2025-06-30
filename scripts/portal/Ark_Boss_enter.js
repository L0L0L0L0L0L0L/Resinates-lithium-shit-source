/* @author RonanLana */
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var level = 180;

function enter(pi) {
    var em = pi.getEventManager("Ark_Battle");
    if (em != null) {
        if (pi.getPlayer().isGroup()) {
            if (pi.getPlayer().getStamina() >= 5) {
                if (pi.getPlayer().isLeader()) {
                    if (em.getEligiblePartyAch(pi.getPlayer(), level, 17)) {
                        if (!em.startPlayerInstance(pi.getPlayer())) {
                            pi.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                        } else {
                            pi.playPortalSE();
                            pi.getPlayer().removeStamina(5);
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
            pi.playerMessage(1, "Event is party/raid only.");
        }
    } else {
        pi.playerMessage(5, "Event has already started, Please wait.");
    }
    return false;
}
