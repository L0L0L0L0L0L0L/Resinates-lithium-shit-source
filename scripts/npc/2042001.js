var ticketId = 4001760;
var status = -1;
var password = 0;
var stam = 10;
var option = 0;
var star = "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";


function getAch(player, id) {
    if (player.getAchievement(id)) {
        return " (#gCompleted#k)";
    }
    return " (#rIn Progress#k)";
}
function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 0) {
        if (cm.getPlayer().getAchievement(226)) {
            if (!cm.getPlayer().isGroup()) {
                var selStr = "Welcome to #rFarm Zones#k\r\nEach Wave is randomized map of that zone.\r\n#rEach Zone here has 50 Waves with no mini or sub bosses\r\nFinal Boss will spawn at the end.#k\r\n#bEach run requires (#r" + cm.getItemName(ticketId) + "#k)#k\r\nSelect a Zone to start in:\r\n";
                selStr += "#L100# #bGolem Temple#k#l\r\n";
                selStr += "#L101# #bKerning Square#k#l\r\n";
                selStr += "#L102# #bSnowy Mountain#k#l\r\n";
                selStr += "#L103# #bKelp Forest#k#l\r\n";
                selStr += "#L104# #bCave of Darkness#k#l\r\n";
                selStr += "#L105# #bBlack Mountain#k#l\r\n";
                selStr += "#L106# #bHidden Field#k#l\r\n";
                selStr += "#L107# #bSecret Lab#k#l\r\n";
                selStr += "#L108# #bMossy Forest#k#l\r\n";
                selStr += "#L109# #bMu Lung#k#l\r\n";
                selStr += "#L110# #bLost Time#k#l\r\n";
                selStr += "#L111# #bNeo City#k#l\r\n";
                selStr += "#L112# #bEl Nath#k#l\r\n";
                selStr += "#L113# #bLion King Castle#k#l\r\n";
                selStr += "#L114# #bDragon Forest#k#l\r\n";
                selStr += "#L115# #bTemple of Time#k#l\r\n";
                selStr += "#L116# #bStronghold#k#l\r\n";
                cm.sendSimple(selStr);
            } else {
                cm.sendOk("This Monster Park Portal is Solo Only.");
            }
        } else {
            cm.sendOk("Need to clear all of tutorial zone missions to unlock.");
        }
    }

    if (status == 1) {
        if (!cm.getPlayer().isGroup()) {
            var em = cm.getEventManager("MP_Endless_Farm");
            if (em != null) {
                if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getTotalLevel(), 1, selection)) {
                    cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                } else {
                    cm.dispose();
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        } else {
            cm.sendOk("This Monster Park Portal is Solo Only.");
        }
    }
}
