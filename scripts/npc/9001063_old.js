var status = 0;
var groupsize = 0;
var item = 4036519;
var ach = 0;
var cost = 0;
var tier = 0;
var level = 0;

function start() {

    if (cm.getPlayer().isGroup()) {
        if (cm.getPlayer().isLeader()) {
            var text = "";
            text += "#rThis Party quest requires completing ALL Zone Dungeons to fight the final boss.#k\r\n";
            text += "#bCost of entry uses#k #i4036519#\r\n";
            text += "#L10# #bTier: 10#k - #rCost: 100#k#l\r\n";
            text += "#L20# #bTier: 20#k - #rCost: 250#k#l\r\n";
            text += "#L30# #bTier: 30#k - #rCost: 500#k#l\r\n";
            text += "#L40# #bTier: 40#k - #rCost: 1,000#k#l\r\n";
            text += "#L50# #bTier: 50#k - #rCost: 5,000#k#l\r\n";
            text += "#L60# #bTier: 60#k - #rCost: 10,000#k#l\r\n";
            text += "#L70# #bTier: 70#k - #rCost: 50,000#k#l\r\n";
            text += "#L80# #bTier: 80#k - #rCost: 100,000#k#l\r\n";
            text += "#L90# #bTier: 90#k - #rCost: 500,000#k#l\r\n";
            text += "#L99# #bTier: 99#k - #rCost: 1,000,000#k#l\r\n ";
            cm.sendSimple("Which Master Monster Park your partys wants to tackle#k?\r\n" + text);
        } else {
            cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
        }
    } else {
        cm.sendOk("Event is Party Mode Only.");
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
        tier = selection;
        if (selection == 10) {
            cost = 100;
        }
        if (selection == 20) {
            cost = 250;
        }
        if (selection == 30) {
            cost = 500;
        }
        if (selection == 40) {
            cost = 1000;
        }
        if (selection == 50) {
            cost = 5000;
        }
        if (selection == 60) {
            cost = 10000;
        }
        if (selection == 70) {
            cost = 50000;
        }
        if (selection == 80) {
            cost = 100000;
        }
        if (selection == 90) {
            cost = 500000;
        }
        if (selection == 99) {
            cost = 1000000;
        }
        if (cm.haveItem(item, cost)) {
            var em = cm.getEventManager("MP_Master");
            if (em != null) {
                if (em.getEligiblePartyAch(cm.getPlayer(), 10, 267, 1, 6)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), 10, tier)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        if (cost > 0) {
                            cm.gainItem(item, -cost);
                            cm.dispose();
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