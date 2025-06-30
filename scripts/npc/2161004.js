var status = 0;
var level = 500;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var cost = 0;
var em;

function start() {
    if (cm.getPlayer().getStamina() >= 25) {
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                if (cm.getPlayer().getTotalLevel() >= level) {
                    var text = "";
                    if (cm.getPlayer().getTotalLevel() >= 500) {
                        text += "#L0# #rReq. #k#bLvl. 500 Kaotic Tier: 20#k (Roses: 1)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 750) {
                        text += "#L1# #rReq. #k#bLvl. 750 Kaotic Tier: 30#k (Roses: 2)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 1000) {
                        text += "#L2# #rReq. #k#bLvl. 1000 Kaotic Tier: 40#k (Roses: 3)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 1500) {
                        text += "#L3# #rReq. #k#bLvl. 1500 Kaotic Tier: 50#k (Roses: 5)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 2000) {
                        text += "#L4# #rReq. #k#bLvl. 2000 Kaotic Tier: 60#k (Roses: 25)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 2500) {
                        text += "#L5# #rReq. #k#bLvl. 2500 Kaotic Tier: 70#k (Roses: 100)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 5000) {
                        text += "#L6# #rReq. #k#bLvl. 5000 Kaotic Tier: 80#k (Roses: 500)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 7500) {
                        text += "#L7# #rReq. #k#bLvl. 7500 Kaotic Tier: 90#k (Roses: 1000)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 9999) {
                        text += "#L8# #rReq. #k#bLvl. 9999 Kaotic Tier: 99#k (Roses: 10000)#l\r\n";
                    }
                    cm.sendSimple("Which King Golem would you like to take on?\r\nEach entry costs #i4030035# and 25 Stamina.\r\n" + text);
                } else {
                    cm.sendOk("The leader of the party must be level " + level + " or higher to complete this event.");
                }
            } else {
                cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
            }
        } else {
            cm.sendOk("Event is Party Mode Only.");
        }
    } else {
        cm.sendOk("Kaotic Elite event needs a mimimum of 25 Stamina.");
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        em = cm.getEventManager("rose");
        var scale = 0;
        if (selection == 0) {
            cost = 1;
            level = 500;
            scale = 20;
        } else if (selection == 1) {
            cost = 2;
            level = 750;
            scale = 30;
        } else if (selection == 2) {
            cost = 3;
            level = 1000;
            scale = 40;
        } else if (selection == 3) {
            cost = 5;
            level = 1500;
            scale = 50;
        } else if (selection == 4) {
            cost = 25;
            level = 2000;
            scale = 60;
        } else if (selection == 5) {
            cost = 100;
            level = 2500;
            scale = 70;
        } else if (selection == 6) {
            cost = 500;
            level = 5000;
            scale = 80;
        } else if (selection == 7) {
            cost = 1000;
            level = 7500;
            scale = 90;
        } else if (selection == 8) {
            cost = 10000;
            level = 9999;
            scale = 99;
        }
        if (cm.haveItem(4030035, cost)) {
            if (em != null) {
                if (em.getEligibleParty(cm.getPlayer(), level)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level, scale)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.getPlayer().removeStamina(25);
                        cm.gainItem(4030035, -cost);
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + level + ", 1+ Raid members.");
                }
            } else {
                cm.sendOk("Event has already started, Please wait.");
            }
        } else {
            cm.sendOk("Sorry, your team does not have enough keys to enter.");
        }
    }
}