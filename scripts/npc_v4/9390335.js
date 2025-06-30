var status = 0;
var groupsize = 0;
var item = 4036084;
var ach = 0;
var cost = 0;
var tier = 0;
var level = 0;

function start() {
    if (cm.getPlayer().getTotalLevel() >= 5000) {
        if (!cm.getPlayer().isGroup()) {
            level = cm.getPlayer().getTotalLevel();
            var text = ":\r\n";
            text += "#L1# #bBoss T: 50#k - #rCost: 1k Shimmering Stars#k#l\r\n";
            text += "#L2# #bBoss T: 55#k - #rCost: 5k Shimmering Stars#k#l\r\n";
            text += "#L3# #bBoss T: 60#k - #rCost: 10k Shimmering Stars#k#l\r\n";
            text += "#L4# #bBoss T: 65#k - #rCost: 25k Shimmering Stars#k#l\r\n";
            text += "#L5# #bBoss T: 70#k - #rCost: 50k Shimmering Stars#k#l\r\n";
            text += "#L6# #bBoss T: 75#k - #rCost: 100k Shimmering Stars#k#l\r\n";
            text += "#L7# #bBoss T: 80#k - #rCost: 250k Shimmering Stars#k#l\r\n";
            cm.sendSimple("Would you like to train in mega kaotic zone#k?\r\n\Each event Lasts for 30 mins and is #rSOLO Only#k.\r\n\Monsters are powerful with NO drops but\r\nrewards insane SKIN EXP and Shadow Power.\r\n\#rElite Monster Level: " + parseInt(level) + "" + text);
        } else {
            cm.sendOk("Event is Solo Mode Only.");
        }
    } else {
        cm.sendOk("My services are only open to those at or above level 5000.");
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
        if (selection == 1) {
            tier = 50;
            cost = 1000;
        }
        if (selection == 2) {
            tier = 55;
            cost = 5000;
        }
        if (selection == 3) {
            tier = 60;
            cost = 10000;
        }
        if (selection == 4) {
            tier = 65;
            cost = 25000;
        }
        if (selection == 5) {
            tier = 70;
            cost = 50000;
        }
        if (selection == 6) {
            tier = 75;
            cost = 100000;
        }
        if (selection == 7) {
            tier = 80;
            cost = 250000;
        }
        if (!cm.getPlayer().isGroup()) {
            if (cm.haveItem(item, cost)) {
                var em = cm.getEventManager("DP_Event_Kaotic");
                if (em != null) {
                    cm.getPlayer().setVar("multi", selection * selection);
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
        } else {
            cm.sendOk("Event is Solo Mode Only.");
        }
    }
}