var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var option;
var cost = 0;
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
        cm.sendSimple("Welcome to the Master Dojo. How may I help you?\r\n\r\n#L1# Challenge Normal Dojo?#l\r\n#L2# Challenge Kaotic Dojo?#l\r\n#L3# Dojo Shop?#l");
    } else if (status == 1) {
        option = selection;
        if (selection == 1) {
            cm.sendSimple("Which Dojo Challenge do want to compete in? Each Challenge has an entry free.\r\n\r\n #L1# #v3994115##l #L2# #v3994116##l #L3# #v3994117##l #L4# #v3994118##l \r\n       (1,000,000)      (5,000,000)     (25,000,000)   (100,000,000)");
        } else if (selection == 2) {
            cm.sendSimple("Which Dojo Challenge do want to compete in? Each Challenge has an entry free.\r\n\r\n #L5# #v3994115##l #L6# #v3994116##l #L7# #v3994117##l #L8# #v3994118##l \r\n  (250,000,000)  (500,000,000) (1,000,000,000) (2,000,000,000)");
        } else {
            cm.openShopNPC(20000);//dojo shop
        }
    } else if (status == 2) {
        if (selection == 1) {
            cost = 1000000;
        } else if (selection == 2) {
            cost = 5000000;
            ach = 92;
        } else if (selection == 3) {
            cost = 25000000;
            ach = 93;
        } else if (selection == 4) {
            cost = 100000000;
            ach = 94;
        }
        if (cm.getPlayer().getMeso() >= cost) {
            if (!cm.getPlayer().isGroup()) {
                if (selection == 1 || selection == 2 || selection == 3 || selection == 4) {
                    if (ach == 0 || cm.getPlayer().getAchievement(ach)) {
                        var em = cm.getEventManager("Dojo");
                        if (em != null) {
                            if (!em.startPlayerInstance(cm.getPlayer(), selection)) {
                                cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                            } else {
                                cm.getPlayer().gainMeso(-cost);
                                cm.playerMessage(5, cm.convertNumber(cost) + " Mesos have been removed from your wallet.");
                            }
                        } else {
                            cm.sendOk("Event has already started, Please wait.");
                        }
                    } else {
                        cm.sendOk("Requires following Achievement to enter: " + cm.getAchievementName(ach) + ".");
                    }
                }
            } else {
                cm.sendOk("Dojo is Solo Mode Only.");
            }
        } else {
            cm.sendOk("You do not have enough meso for entry fee. Entry Free cost is " + cost + " Mesos.");
        }
    }
}