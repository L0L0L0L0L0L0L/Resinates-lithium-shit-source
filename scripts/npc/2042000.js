var ticketId = 4001760;
var status = -1;
var password = 0;
var stam = 10;
var option = 0;
var star = "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
var star1 = "#fUI/Custom.img/Star/1#";
var star2 = "#fUI/Custom.img/Star/2#";
var star3 = "#fUI/Custom.img/Star/3#";
var star4 = "#fUI/Custom.img/Star/4#";
var star5 = "#fUI/Custom.img/Star/5#";



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
        if (cm.getPlayer().getMapId() == 870000010 && cm.getPlayer().getVar("starter") <= 0) {
            cm.getPlayer().setVar("starter", 1);
        }
        if (cm.getPlayer().getVar("starter") > 0) {
            if (!cm.getPlayer().isGroup()) {
                var selStr = "Welcome to #rTutorial Zone#k\r\n";
                selStr += "#L100# #bGolem Temple#k " + getAch(cm.getPlayer(), 200) + "#l\r\n";
                selStr += "#L101# #bKerning Square#k " + getAch(cm.getPlayer(), 201) + "#l\r\n";
                selStr += "#L102# #bSnowy Mountain#k " + getAch(cm.getPlayer(), 202) + "#l\r\n";
                selStr += "#L103# #bKelp Forest#k " + getAch(cm.getPlayer(), 203) + "#l\r\n";
                selStr += "#L104# #bCave of Darkness#k " + getAch(cm.getPlayer(), 204) + "#l\r\n";
                if (cm.getPlayer().getMapId() != 5000) {
                    if (cm.getPlayer().getAchievement(205)) {
                        selStr += "#L200# #bBlack Mountain#k " + getAch(cm.getPlayer(), 210) + "#l\r\n";
                        selStr += "#L201# #bHidden Field#k " + getAch(cm.getPlayer(), 211) + "#l\r\n";
                        selStr += "#L202# #bSecret Lab#k " + getAch(cm.getPlayer(), 212) + "#l\r\n";
                        selStr += "#L203# #bMossy Forest#k " + getAch(cm.getPlayer(), 213) + "#l\r\n";
                        selStr += "#L204# #bMu Lung#k " + getAch(cm.getPlayer(), 214) + "#l\r\n";
                        selStr += "#L205# #bLost Time#k " + getAch(cm.getPlayer(), 215) + "#l\r\n";
                    }
                    if (cm.getPlayer().getAchievement(216)) {
                        selStr += "#L300# #bNeo City#k  " + getAch(cm.getPlayer(), 220) + "#l\r\n";
                        selStr += "#L301# #bEl Nath#k " + getAch(cm.getPlayer(), 221) + "#l\r\n";
                        selStr += "#L302# #bLion King Castle#k " + getAch(cm.getPlayer(), 222) + "#l\r\n";
                        selStr += "#L303# #bDragon Forest#k " + getAch(cm.getPlayer(), 223) + "#l\r\n";
                        selStr += "#L304# #bTemple of Time#k " + getAch(cm.getPlayer(), 224) + "#l\r\n";
                        selStr += "#L305# #bStronghold#k " + getAch(cm.getPlayer(), 225) + "#l\r\n";
                    }
                }
                cm.sendSimple(selStr);
            } else {
                cm.sendOk("This Monster Park Portal is Solo Only.");
            }
        } else {
            cm.sendOk("Speak to #bShanks#k to obtain your starting gear.");
        }
    }
    if (status == 1) {
        option = Math.floor(selection * 0.01);
        if (option == 1) {
            var em = cm.getEventManager("MP_Easy");
            var room = selection - 100;
        }
        if (option == 2) {
            var em = cm.getEventManager("MP_Normal");
            var room = selection - 200;
        }
        if (option == 3) {
            var em = cm.getEventManager("MP_Hard");
            var room = selection - 300;
        }
        if (em != null) {
            if (!cm.getPlayer().isGroup()) {
                if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getTotalLevel(), room)) {
                    cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                } else {
                    cm.dispose();
                }
            } else {
                cm.sendOk("This Monster Park Portal is Solo Only.");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }
    }
}
