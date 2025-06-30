var status = 0;
var groupsize = 0;
var item = 4310001;
var ach = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        var number1 = cm.random(1, 9);
        var number2 = cm.random(1, 9);
        var number3 = cm.random(1, 9);
        var number4 = cm.random(1, 9);
        password = cm.getCode(number1, number2, number3, number4);
        cm.sendGetText("#rBotting beyond this point is not allowed!\r\n\#kPlease enter the 4 digit #rAnti-Bot#k Code seen below:\r\n\    " + cm.getDamageSkinNumber(number1, number2, number3, number4) + "");
    } else if (status == 1) {
        amount = cm.getNumber();
        if (amount == password) {
            if (cm.getPlayer().getStamina() >= 10) {
                if (cm.getPlayer().getParty() != null) {
                    if (cm.getPlayer().isLeader()) {
                        var text = "";
                        text += "#L1# Easy (Tier: #r10#k - #bRounds: 10#k)#l\r\n";
                        text += "#L2# Normal (Tier: #r15#k - #bRounds: 15#k)#l\r\n";
                        text += "#L3# Hard (Tier: #r20#k - #bRounds: 20#k)#l\r\n";
                        text += "#L4# Hell (Tier: #r25#k - #bRounds: 25#k)#l\r\n";
                        text += "#L5# Extreme (Tier: #r30#k - #bRounds: 30#k)#l\r\n";
                        text += "#L6# Kaotic (Tier: #r40#k - #bRounds: 40#k)#l\r\n";
                        text += "#L7# Giga (Tier: #r50#k - #bRounds: 50#k)#l\r\n";
                        text += "#L8# Ultimate (Tier: #r75#k - #bRounds: 50#k)#l\r\n";
                        if (cm.getPlayer().achievementFinished(296)) {
                            text += "#L9# Supreme (Tier: #r100#k - #bRounds: 50#k)#l\r\n";
                        }
                        if (cm.getPlayer().achievementFinished(297)) {
                            text += "#L10# Super Supreme (Tier: #r150#k - #bRounds: 50#k)#l\r\n";
                        }
                        if (cm.getPlayer().achievementFinished(298)) {
                            text += "#L11# Mega Supreme (Tier: #r200#k - #bRounds: 50#k)#l\r\n";
                        }
                        if (cm.getPlayer().achievementFinished(299)) {
                            text += "#L12# Kaotic Supreme (Tier: #r250#k - #bRounds: 50#k)#l\r\n";
                        }
                        cm.sendSimple("Which mode would you like to take on?\r\n\Each Battle costs #r10 Stamina#k\r\n" + text);
                    } else {
                        cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
                    }
                } else {
                    cm.sendOk("Event is Party Mode Only.");
                }
            } else {
                cm.sendOk("Kaotic Boss PQ needs a mimimum of 10 Stamina.");
            }
        } else {
            cm.sendOk("Wrong password.");
        }
    } else if (status == 2) {
        var cost = 10;
        var scale = 0;
        var moblvl = 1000;
        var em = cm.getEventManager("BossPQ_Kaotic");
        if (selection == 1) {
            scale = 10;
            moblvl = 1600;
        } else if (selection == 2) {
            scale = 15;
            moblvl = 1800;
        } else if (selection == 3) {
            scale = 20;
            moblvl = 2000;
        } else if (selection == 4) {
            scale = 25;
            moblvl = 2200;
        } else if (selection == 5) {
            scale = 30;
            moblvl = 2300;
        } else if (selection == 6) {
            scale = 40;
            moblvl = 2500;
        } else if (selection == 7) {
            scale = 50;
            moblvl = 5000;
        } else if (selection == 8) {
            scale = 75;
            moblvl = 7500;
        } else if (selection == 9) {
            scale = 100;
            moblvl = 9999;
        } else if (selection == 10) {
            scale = 150;
            moblvl = 9999;
        } else if (selection == 11) {
            scale = 200;
            moblvl = 9999;
        } else if (selection == 12) {
            scale = 250;
            moblvl = 9999;
        }
        if (em != null) {
            if (cm.getPlayer().getStamina() >= 10) {
                if (em.getEligiblePartyAch(cm.getPlayer(), moblvl, 91, 1, 4)) {
                    if (!em.startPlayerInstance(cm.getPlayer(), moblvl, scale)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.getPlayer().removeStamina(10);
                    }
                } else {
                    cm.sendOk("You cannot start this party quest yet, because either your party is not in the range size, some of your party members are not eligible to attempt it or they are not in this map. Minimum requirements are: Level " + moblvl + ", 1+ Raid members.");
                }
            } else {
                cm.sendOk("Kaotic Boss PQ needs a mimimum of 10 Stamina.");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }

    }
}