var ticketId = 4001760;
var status = -1;
function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 0) {
        if (cm.haveItem(ticketId)) {
            if (!cm.getPlayer().isGroup()) {
                cm.sendYesNo("Do you wish to challenge Monster Park? This Hell mode will place the party into RANDOM zone. All monsters are tier 4, Mini-Bosses are tier 6 and final boss is tier 8. Each zone requires previous zone to be completed.\r\n\Zone Tier: 4 - Zone Level: " + cm.getPlayer().getTotalLevel());
            } else {
                cm.sendOk("This Monster Park Portal is Solo Only.");
            }
        } else {
            cm.sendOk("To enter you must have #i" + ticketId + "#.");
        }
    } else if (status == 1) {
        if (!cm.getPlayer().isGroup()) {
            var em = cm.getEventManager("MP_Hell");
            if (em != null) {
                if (cm.getPlayer().getAchievement(225)) {
                    var room = cm.random(0, 16);
                    var level = cm.getPlayer().getTotalLevel() + (cm.getPlayer().getTotalLevel() * 0.10);
                    if (!em.startPlayerInstance(cm.getPlayer(), level, room)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(ticketId, -1);
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map.");
                }

            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        } else {
            cm.sendOk("This Monster Park Portal is Solo Only.");
        }
    }
}
