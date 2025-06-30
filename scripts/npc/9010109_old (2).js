var status = 0;
var groupsize = 0;
var item = 4310500;
var ach = 0;
var cost = 0;
var tier = 0;
var level = 0;

function start() {
    if (cm.getPlayer().getTotalLevel() >= 250) {
        if (cm.getPlayer().isGroup()) {
            var text = "\r\n";
            text += "#L1# #bLvl: 250 - T: 10#k - #rCost: 10 Meso Bags#k#l\r\n";
            text += "#L2# #bLvl: 500 - T: 15#k - #rCost: 25 Meso Bags#k#l\r\n";
            text += "#L3# #bLvl: 750 - T: 20#k - #rCost: 50 Meso Bags#k#l\r\n";
            text += "#L4# #bLvl: 1000 - T: 25#k - #rCost: 100 Meso Bags#k#l\r\n";
            text += "#L5# #bLvl: 2500 - T: 30#k - #rCost: 250 Meso Bags#k#l\r\n";
            text += "#L6# #bLvl: 4000 - T: 40#k - #rCost: 500 Meso Bags#k#l\r\n";
            text += "#L7# #bLvl: 5000 - T: 50#k - #rCost: 750 Meso Bags#k#l\r\n";
            text += "#L8# #bLvl: 6000 - T: 60#k - #rCost: 1000 Meso Bags#k#l\r\n";
            text += "#L9# #bLvl: 7000 - T: 70#k - #rCost: 2500 Meso Bags#k#l\r\n";
            text += "#L10# #bLvl: 8000 - T: 80#k - #rCost: 10000 Meso Bags#k#l\r\n";
            text += "#L11# #bLvl: 9999 - T: 99#k - #rCost: 25000 Meso Bags#k#l\r\n";
            cm.sendSimple("Would you like to challenge the Shadow Zone#k?\r\n\Each event Lasts for 10 mins and is #rGroup Only#k.\r\n\Monsters are powerful with NO drops but give insane SKIN EXP and Shadow Power." + text);
        } else {
            cm.sendOk("Event is Group Mode Only.");
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
        if (selection == 1) {
            tier = 10;
            cost = 10;
            level = 250;
        }
        if (selection == 2) {
            tier = 15;
            cost = 25;
            level = 500;
        }
        if (selection == 3) {
            tier = 20;
            cost = 50;
            level = 750;
        }
        if (selection == 4) {
            tier = 25;
            cost = 100;
            level = 1000;
        }
        if (selection == 5) {
            tier = 30;
            cost = 250;
            level = 2500;
        }
        if (selection == 6) {
            tier = 40;
            cost = 500;
            level = 4000;
        }
        if (selection == 7) {
            tier = 50;
            cost = 750;
            level = 5000;
        }
        if (selection == 8) {
            tier = 60;
            cost = 1000;
            level = 6000;
        }
        if (selection == 9) {
            tier = 70;
            cost = 2500;
            level = 7000;
        }
        if (selection == 10) {
            tier = 80;
            cost = 10000;
            level = 8000;
        }
        if (selection == 11) {
            tier = 99;
            cost = 25000;
            level = 9999;
        }
        if (cm.haveItem(item, cost)) {
            var em = cm.getEventManager("Shadow");
            if (em != null) {
                if (em.getEligibleParty(cm.getPlayer(), level, 4)) {
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