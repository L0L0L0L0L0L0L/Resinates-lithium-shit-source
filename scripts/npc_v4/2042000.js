var ticketId = 4001514;
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
            var selStr = "Which mode would you like to take on? Each zone rewards 1% of chosen stat from list, +5% total exp. Each zone requires previous zone to be completed.\r\n\Zone Tier: 1 - Zone Level: " + cm.getPlayer().getTotalLevel() + " \r\n";
            selStr += "#L0# #bGolem Temple#k " + getAch(cm.getPlayer(), 200) + "#l\r\n";
            selStr += "#L1# #bKerning Square#k " + getAch(cm.getPlayer(), 201) + "#l\r\n";
            selStr += "#L2# #bSnowy Mountain#k " + getAch(cm.getPlayer(), 202) + "#l\r\n";
            selStr += "#L3# #bKelp Forest#k " + getAch(cm.getPlayer(), 203) + "#l\r\n";
            selStr += "#L4# #bCave of Darkness#k " + getAch(cm.getPlayer(), 204) + "#l\r\n";
            cm.sendSimple(selStr);
        } else {
            cm.sendOk("This Monster Park Portal is Solo Only.");
        }
    } else if (status == 1) {
        var em = cm.getEventManager("MP_Easy");
        if (em != null) {
            if (!cm.getPlayer().isGroup()) {
                if (selection > 0) {
                    var ach = (200 + selection) - 1;
                    if (cm.getPlayer().getAchievement(ach)) {
                        if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getTotalLevel(), selection)) {
                            cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                        } else {
                            cm.gainItem(ticketId, -1);
                        }
                    } else {
                        cm.sendOk("Requires following Achievement to enter: " + cm.getAchievementName(ach) + ".");
                    }
                } else {
                    if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getTotalLevel(), selection)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    }
                }
            } else {
                cm.sendOk("This Monster Park Portal is Solo Only.");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }
    }
}
