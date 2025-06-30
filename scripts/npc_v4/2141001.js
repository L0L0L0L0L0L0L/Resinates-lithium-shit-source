var status = 0;
var groupsize = 0;
var item = 0;

function start() {
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
            cm.sendSimple("Which Pink Bean Mode would you like to take on?\r\n#rEach mode consumes Marbles#k\r\n\#L1#L:200 Normal (#bPrice 1 Marble#k)#l\r\n\#L2#L:250 Hard (#bPrice 5 Marbles#k)#l\r\n\#L3#L:500 Hell (#bPrice 25 Marbles#k)#l\r\n\#L4#L:2500 Kaotic (#bPrice 100 Marbles#k)#l\r\n");
        } else {
            cm.sendOk("The leader of the party must be the to talk to me about joining the event.");

        }
    } else {
        cm.sendOk("Event is Party Mode Only.");

    }
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
        var cost = 0;
        var scale = 0;
        var level = 0;
        var ach = 0;
        item = 4032002;
        var em = null;
        if (selection == 1) {
            em = cm.getEventManager("PinkBean");
            level = 200;
            cost = 1;
        } else if (selection == 2) {
            em = cm.getEventManager("PinkBeanHard");
            level = 250;
            cost = 5;
            ach = 17;
        } else if (selection == 3) {
            em = cm.getEventManager("BlackBean");
            level = 500;
            item = 4032002;
            cost = 25;
            ach = 17;
        } else if (selection == 4) {
            em = cm.getEventManager("BlackBeanKaotic");
            level = 2500;
            item = 4032002;
            cost = 100;
            ach = 400;
        }
        if (em != null) {
            if (cm.haveItem(item, cost)) {
                if (em.getEligiblePartyAch(cm.getPlayer(), level, ach)) {
                    if (!em.startPlayerInstance(cm.getPlayer())) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(item, -cost);
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
                }
            } else {
                cm.sendOk("This event requires marble of chaos.");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }

    }
}