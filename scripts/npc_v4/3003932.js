var status = 0;
var level = 2800;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var cost = 0;
var scale = 0;
var boost = 1;
var em;

function start() {
    if (cm.getPlayer().getStamina() >= 25) {
        if (cm.getPlayer().isGroup()) {
            if (cm.getPlayer().isLeader()) {
                if (cm.getPlayer().getTotalLevel() >= level) {
                    var text = "";
                    if (cm.getPlayer().getTotalLevel() >= 3000) {
                        text += "#L10# #rReq. #k#bLvl. 3000 Kaotic Tier: 70#k (Keys: 1)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 4000) {
                        text += "#L11# #rReq. #k#bLvl. 4000 Kaotic Tier: 75#k (Keys: 10)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 5000) {
                        text += "#L12# #rReq. #k#bLvl. 5000 Kaotic Tier: 80#k (Keys: 100)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 7500) {
                        text += "#L13# #rReq. #k#bLvl. 7500 Kaotic Tier: 90#k (Keys: 1000)#l\r\n";
                    }
                    if (cm.getPlayer().getTotalLevel() >= 9999) {
                        text += "#L14# #rReq. #k#bLvl. 9999 Kaotic Tier: 99#k (Keys: 10000)#l\r\n";
                    }
                    cm.sendSimpleS("Which Ramu would you like to take on?\r\nEach entry costs #i4033320#s and 25 Stamina.\r\nEach Stage has boosted value to increase USE-ETC drops.\r\n" + text, 16);
                } else {
                    cm.sendOkS("The leader of the party must be level " + level + " or higher to complete this event.", 16);
                }
            } else {
                cm.sendOkS("The leader of the party must be the to talk to me about joining the event.", 16);
            }
        } else {
            cm.sendOkS("Event is Party Mode Only.", 16);
        }
    } else {
        cm.sendOkS("Ramu event needs a mimimum of 25 Stamina.", 16);
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        if (cm.getPlayer().getStamina() >= 25) {
            em = cm.getEventManager("Ramu");
            if (selection == 10) {
                cost = 1;
                scale = 70;
                level = 3000;
            }
            if (selection == 11) {
                cost = 10;
                scale = 75;
                level = 4000;
            }
            if (selection == 12) {
                cost = 100;
                scale = 80;
                level = 5000;
            }
            if (selection == 13) {
                cost = 1000;
                scale = 90;
                level = 7500;
            }
            if (selection == 14) {
                cost = 10000;
                scale = 99;
                level = 9999;
            }
            if (cm.haveItem(4033320, cost)) {
                if (em != null) {
                    if (em.getEligiblePartyAch(cm.getPlayer(), level, 181)) {
                        if (!em.startPlayerInstance(cm.getPlayer(), level, scale)) {
                            cm.sendOkS("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.", 16);
                        } else {
                            cm.getPlayer().removeStamina(25);
                            cm.gainItem(4033320, -cost);
                        }
                    } else {
                        cm.sendOkS("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level 2800+, 1+ Raid members.", 16);
                    }
                } else {
                    cm.sendOkS("Event has already started, Please wait.", 16);
                }
            } else {
                cm.sendOkS("Sorry, your team does not have enough keys to enter.", 16);
            }
        } else {
            cm.sendOkS("Ramu event needs a mimimum of 25 Stamina.", 16);
        }
    }
}