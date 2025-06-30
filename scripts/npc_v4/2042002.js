var ticketId = 4001522;
var status = -1;
function getAch(player, id) {
    if (player.getAchievement(id)) {
        return " (#gCompleted#k)";
    }
    return "";
}
function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 0) {
        if (!cm.getPlayer().isGroup()) {
            var selStr = "Which mode would you like to take on? Each zone rewards 5% of chosen stat from list, +25% total exp. Each zone requires previous zone to be completed.\r\n\Zone Tier: 3 - Zone Level: " + cm.getPlayer().getTotalLevel() + " \r\n";
            selStr += "#L0# #bNeo City#k  " + getAch(cm.getPlayer(), 220) + "#l\r\n";
            selStr += "#L1# #bEl Nath#k " + getAch(cm.getPlayer(), 221) + "#l\r\n";
            selStr += "#L2# #bLion King Castle#k " + getAch(cm.getPlayer(), 222) + "#l\r\n";
            selStr += "#L3# #bDragon Forest#k " + getAch(cm.getPlayer(), 223) + "#l\r\n";
            selStr += "#L4# #bTemple of Time#k " + getAch(cm.getPlayer(), 224) + "#l\r\n";
            selStr += "#L5# #bStronghold#k " + getAch(cm.getPlayer(), 225) + "#l\r\n";
            cm.sendSimple(selStr);
        } else {
            cm.sendOk("This Monster Park Portal is Solo Only.");
        }
    } else if (status == 1) {
        var em = cm.getEventManager("MP_Hard");
        if (em != null) {
            if (!cm.getPlayer().isGroup()) {
                var ach = 215;
                if (selection > 0) {
                    var ach = (220 + selection) - 1;
                }
                if (cm.getPlayer().getAchievement(ach)) {
                    var level = cm.getPlayer().getTotalLevel() + (cm.getPlayer().getTotalLevel() * 0.1);
                    if (!em.startPlayerInstance(cm.getPlayer(), level, selection)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(ticketId, -1);
                    }
                } else {
                    cm.sendOk("Requires following Achievement to enter: " + cm.getAchievementName(ach) + ".");
                }
            } else {
                cm.sendOk("This Monster Park Portal is Solo Only.");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }
    }
}
