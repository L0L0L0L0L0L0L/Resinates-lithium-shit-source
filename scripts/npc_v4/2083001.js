var status = 0;
var level = 200;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function start() {
    cm.sendOk("Horntail has been dead for years now.");
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status == 0) {
                        
            return;
        }
        status--;
    }
    if (status == 1) {
        var em = cm.getEventManager("HorntailBoss");
        if (em != null) {
            if (em.getEligiblePartyAch(cm.getPlayer(), level, 15)) {
                if (!em.startPlayerInstance(cm.getPlayer())) {
                    cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    cm.sendOk("Event has already started, Please wait.");
                }
            } else {
                cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }
                    
    }
}