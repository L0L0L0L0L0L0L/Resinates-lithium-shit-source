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
                        text += "#L4# #rReq. #k#bLvl. 500 Kaotic Tier: 15#k (Keys: 10)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 750) {
                        text += "#L5# #rReq. #k#bLvl. 750 Kaotic Tier: 16#k (Keys: 25)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 1000) {
                        text += "#L6# #rReq. #k#bLvl. 1000 Kaotic Tier: 17#k (Keys: 50)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 1500) {
                        text += "#L7# #rReq. #k#bLvl. 1500 Kaotic Tier: 18#k (Keys: 75)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 2000) {
                        text += "#L8# #rReq. #k#bLvl. 2000 Kaotic Tier: 20#k (Keys: 100)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 2500) {
                        text += "#L9# #rReq. #k#bLvl. 2500 Kaotic Tier: 25#k (Keys: 250)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 3000) {
                        text += "#L10# #rReq. #k#bLvl. 3000 Kaotic Tier: 30#k (Keys: 500)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 3500) {
                        text += "#L11# #rReq. #k#bLvl. 3500 Kaotic Tier: 35#k (Keys: 750)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 4000) {
                        text += "#L12# #rReq. #k#bLvl. 4000 Kaotic Tier: 40#k (Keys: 1000)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 5000) {
                        text += "#L13# #rReq. #k#bLvl. 5000 Kaotic Tier: 50#k (Keys: 2500)#l\r\n";
                    }
                    cm.sendSimple("Which Kaotic Vellum would you like to take on?\r\nEach entry costs #i4033611# and 25 Stamina.\r\n" + text);
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
        em = cm.getEventManager("TimeVellumKaotic");
        var scale = 0;
        if (selection == 4) {
            cost = 10;
            level = 500;
            scale = 15;
        } else if (selection == 5) {
            cost = 25;
            level = 750;
            scale = 16;
        } else if (selection == 6) {
            cost = 50;
            level = 1000;
            scale = 17;
        } else if (selection == 7) {
            cost = 75;
            level = 1500;
            scale = 18;
        } else if (selection == 8) {
            cost = 100;
            level = 2000;
            scale = 20;
        } else if (selection == 9) {
            cost = 250;
            level = 2500;
            scale = 25;
        } else if (selection == 10) {
            cost = 500;
            level = 3000;
            scale = 30;
        } else if (selection == 11) {
            cost = 750;
            level = 3500;
            scale = 35;
        } else if (selection == 12) {
            cost = 1000;
            level = 4000;
            scale = 40;
        } else if (selection == 13) {
            cost = 2500;
            level = 5000;
            scale = 50;
        }
        if (cm.haveItem(4033611, cost)) {
            if (em != null) {
                if (em.getEligibleParty(cm.getPlayer(), level)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level, scale)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.getPlayer().removeStamina(25);
                        cm.gainItem(4033611, -cost);
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