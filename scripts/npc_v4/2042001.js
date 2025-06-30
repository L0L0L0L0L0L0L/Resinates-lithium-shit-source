var ticketId = 4001516;
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
            var selStr = "Which mode would you like to take on? Each zone rewards 3% of chosen stat from list, +10% total exp. Each zone requires previous zone to be completed.\r\n\Zone Tier: 2 - Zone Level: " + cm.getPlayer().getTotalLevel() + " \r\n";
            selStr += "#L0# #bBlack Mountain#k " + getAch(cm.getPlayer(), 210) + "#l\r\n";
            selStr += "#L1# #bHidden Field#k " + getAch(cm.getPlayer(), 211) + "#l\r\n";
            selStr += "#L2# #bSecret Lab#k " + getAch(cm.getPlayer(), 212) + "#l\r\n";
            selStr += "#L3# #bMossy Forest#k " + getAch(cm.getPlayer(), 213) + "#l\r\n";
            selStr += "#L4# #bMu Lung#k " + getAch(cm.getPlayer(), 214) + "#l\r\n";
            selStr += "#L5# #bLost Time#k " + getAch(cm.getPlayer(), 215) + "#l\r\n";
            cm.sendSimple(selStr);
        } else {
            cm.sendOk("This Monster Park Portal is Solo Only.");
        }
    } else if (status == 1) {
        var em = cm.getEventManager("MP_Normal");
        if (em != null) {
            if (!cm.getPlayer().isGroup()) {
                var ach = 204;
                if (selection > 0) {
                    var ach = (210 + selection) - 1;
                }
                if (cm.getPlayer().getAchievement(ach)) {
                    var level = cm.getPlayer().getTotalLevel() + (cm.getPlayer().getTotalLevel() * 0.05);
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