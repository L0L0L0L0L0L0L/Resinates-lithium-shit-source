var status = 0;
var groupsize = 0;
var item = 4310504;
var ach = 0;
var cost = 0;
var tier = 0;
var level = 0;

function start() {
    if (cm.getPlayer().getTotalLevel() >= 100) {
        if (!cm.getPlayer().isGroup()) {
            level = Math.floor(cm.getPlayer().getTotalLevel() * 1.25);
            if (level > 9999) {
                level = 9999;
            }
            var text = ":\r\n";
            var coin = "";

            text += "#L0# #bTier: 5#k - #rCost: 1 #i4310504##k#l\r\n";
            text += "#L1# #bTier: 10#k - #rCost: 2 #i4310504##k#l\r\n";
            text += "#L2# #bTier: 15#k - #rCost: 5 #i4310504##k#l\r\n";
            text += "#L3# #bTier: 20#k - #rCost: 10 #i4310504##k#l\r\n";
            text += "#L4# #bTier: 25#k - #rCost: 25 #i4310504##k#l\r\n";
            text += "#L5# #bTier: 30#k - #rCost: 50 #i4310504##k#l\r\n";
            text += "#L6# #bTier: 40#k - #rCost: 75 #i4310504##k#l\r\n";
            text += "#L7# #bTier: 50#k - #rCost: 100 #i4310504##k#l\r\n ";
            cm.sendSimple("Would you like to train in kaotic zone#k?\r\n\#rBotting is Allowed in this instance#k\r\n\Each event Lasts for 12 hours and is #rSOLO Only#k.\r\n\Monsters are powerful with NO drops.\r\n\#rElite Monster Level: " + parseInt(level) + "" + text);
        } else {
            cm.sendOk("Event is Solo Mode Only.");
        }
    } else {
        cm.sendOk("My services are only open to those at or above level 250.");
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
        if (selection == 0) {
            tier = 5;
            cost = 1;
        }
        if (selection == 1) {
            tier = 10;
            cost = 2;
        }
        if (selection == 2) {
            tier = 15;
            cost = 5;
        }
        if (selection == 3) {
            tier = 20;
            cost = 10;
        }
        if (selection == 4) {
            tier = 25;
            cost = 25;
        }
        if (selection == 5) {
            tier = 30;
            cost = 50;
        }
        if (selection == 6) {
            tier = 40;
            cost = 75;
        }
        if (selection == 7) {
            tier = 50;
            cost = 100;
        }
        if (cm.haveItem(item, cost)) {
            var em = cm.getEventManager("DP_Event_Kaotic");
            if (em != null) {
                if (!em.startPlayerInstance(cm.getPlayer(), level, tier)) {
                    cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                } else {
                    if (cost > 0) {
                        cm.gainItem(item, -cost);
                    }
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        } else {
            cm.sendOk("You dont have enough #i" + item + "# for this event.");
        }
    }
}