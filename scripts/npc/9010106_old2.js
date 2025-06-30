var status = 0;
var groupsize = 0;
var item = 4310500;
var ach = 0;
var cost = 0;
var tier = 0;
var level = 0;

function start() {
    if (cm.getPlayer().getTotalLevel() >= 1000) {
        if (!cm.getPlayer().isGroup()) {
            level = cm.getPlayer().getTotalLevel();
            if (level > 5000) {
                level = 5000;
            }
            var text = ":\r\n";
            text += "#L1# #bBoss Tier: 10#k - #rCost: 10 Meso Bags#k#l\r\n";
            text += "#L2# #bBoss Tier: 15#k - #rCost: 25 Meso Bags#k#l\r\n";
            text += "#L3# #bBoss Tier: 20#k - #rCost: 50 Meso Bags#k#l\r\n";
            text += "#L4# #bBoss Tier: 25#k - #rCost: 75 Meso Bags#k#l\r\n";
            text += "#L5# #bBoss Tier: 30#k - #rCost: 100 Meso Bags#k#l\r\n";
            text += "#L6# #bBoss Tier: 40#k - #rCost: 250 Meso Bags#k#l\r\n";
            text += "#L7# #bBoss Tier: 50#k - #rCost: 500 Meso Bags#k#l\r\n";
            cm.sendSimple("Would you like to train in kaotic zone#k?\r\n\Each event Lasts for 30 mins and is #rSOLO Only#k.\r\n\Monsters are powerful with NO drops but give insane SKIN EXP and Shadow Power.\r\n\#rElite Monster Level: " + parseInt(level) + "" + text);
        } else {
            cm.sendOk("Event is Solo Mode Only.");
        }
    } else {
        cm.sendOk("My services are only open to those at or above level 1000.");
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
            tier = 10;
            cost = 10;
        }
        if (selection == 2) {
            tier = 15;
            cost = 25;
        }
        if (selection == 3) {
            tier = 20;
            cost = 50;
        }
        if (selection == 4) {
            tier = 25;
            cost = 75;
        }
        if (selection == 5) {
            tier = 30;
            cost = 100;
        }
        if (selection == 6) {
            tier = 40;
            cost = 250;
        }
        if (selection == 7) {
            tier = 50;
            cost = 500;
        }
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
    }
}