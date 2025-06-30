var status = 0;
var groupsize = 0;
var item = 4036519;
var ach = 0;
var cost = 0;
var tier = 0;
var level = 0;

function start() {
    if (cm.getPlayer().getTotalLevel() >= 100) {
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                var text = ":\r\n";
                text += "#L0# #bLevel 100 - Tier: 5#k - #rCost: 10 #i4036519##k#l\r\n";
                text += "#L1# #bLevel 250 - Tier: 10#k - #rCost: 25 #i4036519##k#l\r\n";
                text += "#L2# #bLevel 500 - Tier: 15#k - #rCost: 50 #i4036519##k#l\r\n";
                text += "#L3# #bLevel 750 - Tier: 20#k - #rCost: 100 #i4036519##k#l\r\n";
                text += "#L4# #bLevel 1000 - Tier: 25#k - #rCost: 250 #i4036519##k#l\r\n";
                text += "#L5# #bLevel 1500 - Tier: 30#k - #rCost: 500 #i4036519##k#l\r\n";
                text += "#L10# #bLevel 1750 - Tier: 35#k - #rCost: 750 #i4036519##k#l\r\n";
                text += "#L6# #bLevel 2000 - Tier: 40#k - #rCost: 1000 #i4036519##k#l\r\n";
                text += "#L11# #bLevel 2250 - Tier: 45#k - #rCost: 1500 #i4036519##k#l\r\n";
                text += "#L7# #bLevel 2500 - Tier: 50#k - #rCost: 2500 #i4036519##k#l\r\n";
                text += "#L12# #bLevel 2750 - Tier: 55#k - #rCost: 3500 #i4036519##k#l\r\n";
                text += "#L8# #bLevel 3000 - Tier: 60#k - #rCost: 5000 #i4036519##k#l\r\n";
                text += "#L9# #bLevel 3500 - Tier: 70#k - #rCost: 10000 #i4036519##k#l\r\n";
                text += "#L13# #bLevel 4000 - Tier: 80#k - #rCost: 25000 #i4036519##k#l\r\n ";
                cm.sendSimple("Which Master Monster Park your partys wants to tackle#k?" + text);
            } else {
                cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
            }
        } else {
            cm.sendOk("Event is Party Mode Only.");
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
            level = 100;
            tier = 5;
            cost = 10;
        }
        if (selection == 1) {
            level = 250;
            tier = 10;
            cost = 25;
        }
        if (selection == 2) {
            level = 500;
            tier = 15;
            cost = 50;
        }
        if (selection == 3) {
            level = 750;
            tier = 20;
            cost = 100;
        }
        if (selection == 4) {
            level = 1000;
            tier = 25;
            cost = 250;
        }
        if (selection == 5) {
            level = 1500;
            tier = 30;
            cost = 500;
        }
        if (selection == 6) {
            level = 2000;
            tier = 40;
            cost = 1000;
        }
        if (selection == 7) {
            level = 2500;
            tier = 50;
            cost = 2500;
        }
        if (selection == 8) {
            level = 3000;
            tier = 60;
            cost = 5000;
        }
        if (selection == 9) {
            level = 3500;
            tier = 70;
            cost = 10000;
        }
        if (selection == 13) {
            level = 4000;
            tier = 80;
            cost = 25000;
        }
        if (selection == 10) {
            level = 1750;
            tier = 35;
            cost = 750;
        }
        if (selection == 11) {
            level = 2250;
            tier = 45;
            cost = 1500;
        }
        if (selection == 12) {
            level = 2750;
            tier = 55;
            cost = 3500;
        }
        if (cm.haveItem(item, cost)) {
            var em = cm.getEventManager("MP_Master");
            if (em != null) {
                if (em.getEligiblePartyAch(cm.getPlayer(), level, 267, 1, 6)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level, tier)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        if (cost > 0) {
                            cm.gainItem(item, -cost);
                        }
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        } else {
            cm.sendOk("You dont have enough #i" + item + "# for this event.");
        }
    }
}