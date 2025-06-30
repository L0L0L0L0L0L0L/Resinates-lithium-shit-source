var status = 0;
var groupsize = 0;
var item = 4310001;
var ach = 0;

function start() {
    password = cm.random(1000, 9999);
    cm.sendGetText("Please enter the 4 digit Code seen below: \r\n\ " + cm.botTest(password) + "\r\n");
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
        if (cm.getPlayer().getParty() != null) {
            if (cm.getPlayer().isLeader()) {
                cm.sendSimple("Which mode would you like to take on?\r\n\#L1# Easy (Power 180 - #bReward: 5 Coins#k)#l\r\n\#L2# Normal (Power 250 - #bReward: 10 Coins#k)#l\r\n\#L3# Hard (Power 500 - #bReward: 25 Coins#k)#l\r\n\#L4# Ultimate (Power 750 - #bReward: 50 Coins#k)#l\r\n\#L5# Extreme (Power 1000 - #bReward: 100 Coins#k)#l\r\n");
            } else {
                cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
            }
        } else {
            cm.sendOk("Event is Party Mode Only.");
        }
    } else if (status == 2) {
        var cost = 0;
        var scale = 0;
        var level = 0;
        var moblvl = 0;
        var em = cm.getEventManager("BossPQ");
        if (selection == 1) {
            level = 180;
            moblvl = 180;
            scale = 5;
        } else if (selection == 2) {
            level = 250;
            moblvl = 250;
            scale = 6;
            ach = 88;
        } else if (selection == 3) {
            level = 250;
            moblvl = 500;
            scale = 7;
            ach = 89;
        } else if (selection == 4) {
            level = 250;
            moblvl = 750;
            scale = 8;
            ach = 90;
        } else if (selection == 5) {
            level = 250;
            moblvl = 1000;
            scale = 10;
            ach = 91;
        }
        if (em != null) {
            if (em.getEligiblePartyAch(cm.getPlayer(), level, ach, 1, 6)) {
                if (!em.startPlayerInstance(cm.getPlayer(), moblvl, scale)) {
                    cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                }
            } else {
                cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }

    }
}