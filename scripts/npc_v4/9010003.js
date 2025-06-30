var status = 0;
var groupsize = 0;
var item = 4031034;
var ach = 0;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        if (cm.getPlayer().getStamina() >= 5) {
            if (cm.getPlayer().isGroup()) {
                if (cm.getPlayer().isLeader()) {
                    var text = "";
                    text += "#L1# #bL. 100 Tier: 5#k - (#i" + 4031034 + "# x1)#l\r\n";
                    text += "#L2# #bL. 150 Tier: 10#k - (#i" + 4031034 + "# x2)#l\r\n";
                    text += "#L3# #bL. 200 Tier: 15#k - (#i" + 4031034 + "# x5)#l\r\n";
                    text += "#L4# #bL. 250 Tier: 20#k - (#i" + 4031034 + "# x10)#l\r\n";
                    text += "#L5# #bL. 500 Tier: 25#k - (#i" + 4031034 + "# x25)#l\r\n";
                    text += "#L6# #bL. 750 Tier: 30#k - (#i" + 4031034 + "# x50)#l\r\n";
                    cm.sendSimple("Which Elite Tier would you like to take on?\r\nEach Elite costs #r5 Stamina#k.\r\n" + text);
                } else {
                    cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
                }
            } else {
                cm.sendOk("Event is Party Mode Only.");
            }
        } else {
            cm.sendOk("Elite event needs a mimimum of 5 Stamina.");
        }
    } else if (status == 2) {
        if (cm.getPlayer().isGroup()) {
            var cost = 0;
            var scale = 0;
            var level = 0;
            if (selection == 1) {
                cost = 1;
                scale = 5;
                level = 100;
            } else if (selection == 2) {
                cost = 2;
                scale = 10;
                level = 150;
            } else if (selection == 3) {
                cost = 5;
                scale = 15;
                level = 200;
            } else if (selection == 4) {
                cost = 10;
                scale = 20;
                level = 250;
            } else if (selection == 5) {
                cost = 25;
                scale = 25;
                level = 500;
            } else if (selection == 6) {
                cost = 50;
                scale = 30;
                level = 750;
            }
            if (cm.haveItem(item, cost)) {
                var em = cm.getEventManager("Elite");
                if (em != null) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level, scale)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(item, -cost);
                    }
                } else {
                    cm.sendOk("Event has already started, Please wait.");
                }
            } else {
                cm.sendOk("Your missing "+cost+" life scrolls to enter.");
            }
        } else {
            cm.sendOk("Event is party only, Please wait.");
        }

    }
}