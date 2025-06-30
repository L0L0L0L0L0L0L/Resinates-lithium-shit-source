var status = 0;
var level = 1500;
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
                    if (cm.getPlayer().getTotalLevel() >= 1500) {
                        text += "#L1# #rReq. #k#bLvl. 1500 Kaotic Tier: 45#k (Keys: 1)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 2000) {
                        text += "#L2# #rReq. #k#bLvl. 2000 Kaotic Tier: 50#k (Keys: 5)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 2500) {
                        text += "#L3# #rReq. #k#bLvl. 2500 Kaotic Tier: 55#k (Keys: 10)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 3000) {
                        text += "#L4# #rReq. #k#bLvl. 3000 Kaotic Tier: 60#k (Keys: 25)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 3500) {
                        text += "#L5# #rReq. #k#bLvl. 3500 Kaotic Tier: 70#k (Keys: 50)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 4000) {
                        text += "#L6# #rReq. #k#bLvl. 4000 Kaotic Tier: 75#k (Keys: 100)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 5000) {
                        text += "#L7# #rReq. #k#bLvl. 5000 Kaotic Tier: 80#k (Keys: 250)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 7500) {
                        text += "#L8# #rReq. #k#bLvl. 7500 Kaotic Tier: 90#k (Keys: 500)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 9999) {
                        text += "#L9# #rReq. #k#bLvl. 9999 Kaotic Tier: 99#k (Keys: 1000)#l\r\n";
                    }
                    cm.sendSimple("Which Ark would you like to take on?\r\nEach entry costs #i4001869# and 25 Stamina.\r\n" + text);
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
        em = cm.getEventManager("ark2_battle");
        var scale = 0;
        if (selection == 1) {
            cost = 1;
            level = 1500;
            scale = 45;
        } else if (selection == 2) {
            cost = 5;
            level = 2000;
            scale = 50;
        } else if (selection == 3) {
            cost = 10;
            level = 2500;
            scale = 55;
        } else if (selection == 4) {
            cost = 25;
            level = 3000;
            scale = 60;
        } else if (selection == 5) {
            cost = 50;
            level = 3500;
            scale = 70;
        } else if (selection == 6) {
            cost = 100;
            level = 4000;
            scale = 75;
        } else if (selection == 7) {
            cost = 250;
            level = 5000;
            scale = 80;
        } else if (selection == 8) {
            cost = 500;
            level = 7500;
            scale = 90;
        } else if (selection == 9) {
            cost = 1000;
            level = 9999;
            scale = 99;
        }
        if (cm.haveItem(4001869, cost)) {
            if (em != null) {
                if (em.getEligibleParty(cm.getPlayer(), level)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level, scale)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.getPlayer().removeStamina(25);
                        cm.gainItem(4001869, -cost);
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