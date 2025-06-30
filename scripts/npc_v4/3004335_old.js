var status = 0;
var groupsize = 0;
var item = 4310502;
var ach = 0;
var cost = 0;
var tier = 0;
var level = 0;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";

function start() {
    if (cm.getPlayer().getTotalLevel() >= 250) {
        if (!cm.getPlayer().isGroup()) {
            level = cm.getPlayer().getTotalLevel();
            var text = "";
            text += "#bElite Monsters award 500% Base Exp to Killers#k\r\n";
            text += "#bElite Monsters award Elwin Exp Per kill#k\r\n";
            text += "#rElite Monster Level: " + parseInt(level) + "\r\n";
            text += "#L1# #bTier: 5#k - #rCost: 5 #i4310502##k - #bEP-Exp: 1x#k#l\r\n";
            text += "#L2# #bTier: 10#k - #rCost: 10 #i4310502##k - #bEP-Exp: 4x#k#l\r\n";
            text += "#L3# #bTier: 15#k - #rCost: 15 #i4310502##k - #bEP-Exp: 9x#k#l\r\n";
            text += "#L4# #bTier: 20#k - #rCost: 20 #i4310502##k - #bEP-Exp: 16x#k#l\r\n";
            text += "#L5# #bTier: 25#k - #rCost: 25 #i4310502##k - #bEP-Exp: 25x#k#l\r\n";
            text += "#L6# #bTier: 30#k - #rCost: 30 #i4310502##k - #bEP-Exp: 36x#k#l\r\n";
            text += "#L8# #bTier: 40#k - #rCost: 40 #i4310502##k - #bEP-Exp: 64x#k#l\r\n";
            text += "#L10# #bTier: 50#k - #rCost: 50 #i4310502##k - #bEP-Exp: 100x#k#l\r\n";
            text += "#L11# #bTier: 60#k - #rCost: 100 #i4310502##k - #bEP-Exp: 250x#k#l\r\n";
            text += "#L12# #bTier: 70#k - #rCost: 250 #i4310502##k - #bEP-Exp: 500x#k#l\r\n ";
            text += "#L13# #bTier: 80#k - #rCost: 500 #i4310502##k - #bEP-Exp: 750x#k#l\r\n ";
            text += "#L14# #bTier: 90#k - #rCost: 750 #i4310502##k - #bEP-Exp: 1000x#k#l\r\n ";
            text += "#L15# #bTier: 99#k - #rCost: 1000 #i4310502##k - #bEP-Exp: 2500x#k#l\r\n ";
            cm.sendSimple("Would you like to train in kaotic zone#k?\r\n\Each event Lasts for #b60 Mins#k and is #rSolo Only#k.\r\n\Monsters are powerful with NO drops but give insane SKIN EXP.\r\n" + text);
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
        tier = (selection * 5);
        cost = tier;
        var multi = selection * selection;
        if (selection == 11) {
            tier = 60;
            cost = 100;
            multi = 250;
        }
        if (selection == 12) {
            tier = 70;
            cost = 250;
            multi = 500;
        }
        if (selection == 13) {
            tier = 80;
            cost = 500;
            multi = 750;
        }
        if (selection == 14) {
            tier = 90;
            cost = 750;
            multi = 1000;
        }
        if (selection == 15) {
            tier = 99;
            cost = 1000;
            multi = 2500;
        }
        if (cm.haveItem(item, cost)) {
            var em = cm.getEventManager("DP_Event_Super");
            if (em != null) {
                cm.getPlayer().setVar("multi", multi);
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