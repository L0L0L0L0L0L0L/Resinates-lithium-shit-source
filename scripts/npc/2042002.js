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
        if (cm.getPlayer().getAchievement(226)) {
            if (!cm.getPlayer().isGroup()) {
                var selStr = "Welcome to #rZone 4#k - #rZone Tier: Hell#k\r\n#bEach dungeon here randomly picked#k\r\n";
                selStr += "  " + star + " #bGolem Temple#k " + getAch(cm.getPlayer(), 250) + "\r\n";
                selStr += "  " + star + " #bKerning Square#k " + getAch(cm.getPlayer(), 251) + "\r\n";
                selStr += "  " + star + " #bSnowy Mountain#k " + getAch(cm.getPlayer(), 252) + "\r\n";
                selStr += "  " + star + " #bKelp Forest#k " + getAch(cm.getPlayer(), 253) + "\r\n";
                selStr += "  " + star + " #bCave of Darkness#k " + getAch(cm.getPlayer(), 254) + "\r\n";
                selStr += "  " + star + " #bBlack Mountain#k " + getAch(cm.getPlayer(), 255) + "\r\n";
                selStr += "  " + star + " #bHidden Field#k " + getAch(cm.getPlayer(), 256) + "\r\n";
                selStr += "  " + star + " #bSecret Lab#k " + getAch(cm.getPlayer(), 257) + "\r\n";
                selStr += "  " + star + " #bMossy Forest#k " + getAch(cm.getPlayer(), 258) + "\r\n";
                selStr += "  " + star + " #bMu Lung#k " + getAch(cm.getPlayer(), 259) + "\r\n";
                selStr += "  " + star + " #bLost Time#k " + getAch(cm.getPlayer(), 260) + "\r\n";
                selStr += "  " + star + " #bNeo City#k  " + getAch(cm.getPlayer(), 261) + "\r\n";
                selStr += "  " + star + " #bEl Nath#k " + getAch(cm.getPlayer(), 262) + "\r\n";
                selStr += "  " + star + " #bLion King Castle#k " + getAch(cm.getPlayer(), 263) + "\r\n";
                selStr += "  " + star + " #bDragon Forest#k " + getAch(cm.getPlayer(), 264) + "\r\n";
                selStr += "  " + star + " #bTemple of Time#k " + getAch(cm.getPlayer(), 265) + "\r\n";
                selStr += "  " + star + " #bStronghold#k " + getAch(cm.getPlayer(), 266) + "\r\n";
                cm.sendYesNo(selStr);
            } else {
                cm.sendOk("This Monster Park Portal is Solo Only.");
            }
        } else {
            cm.sendOk("You must clear Tutorial Zone.");
        }
    }

    if (status == 1) {
        if (cm.haveItem(ticketId)) {
            var em = cm.getEventManager("MP_Hell");
            if (em != null) {
                if (cm.getPlayer().getAchievement(226)) {
                    var room = cm.random(0, 16);
                    var level = cm.getPlayer().getTotalLevel() + (cm.getPlayer().getTotalLevel() * 0.10);
                    if (!em.startPlayerInstance(cm.getPlayer(), level, room)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(ticketId, -1);
                        cm.dispose();
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map.");
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        } else {
            cm.sendOk("To enter you must have #i" + ticketId + "#.");
        }
    }
}
