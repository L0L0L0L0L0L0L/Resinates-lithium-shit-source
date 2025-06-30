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
        if (cm.getPlayer().getStamina() >= 10) {
            if (cm.getPlayer().getParty() != null) {
                if (cm.getPlayer().isLeader()) {
                    var text = "";
                    text += "#L1# #bL. 1000 Tier: 40#k - (#i" + 4031034 + "# x75) (Reward: #r75 Gallents#k)#l\r\n";
                    text += "#L2# #bL. 1250 Tier: 45#k - (#i" + 4031034 + "# x100) (Reward: #r100 Gallents#k)#l\r\n";
                    text += "#L3# #bL. 1500 Tier: 50#k - (#i" + 4031034 + "# x250) (Reward: #r150 Gallents#k)#l\r\n";
                    cm.sendSimple("Which Elite Tier would you like to take on?\r\nEach Elite costs #r10 Stamina#k.\r\n" + text);
                } else {
                    cm.sendOk("The leader of the party must be the to talk to me about joining the event.");
                }
            } else {
                cm.sendOk("Event is Party Mode Only.");
            }
        } else {
            cm.sendOk("Elite event needs a mimimum of 10 Stamina.");
        }
    } else if (status == 2) {
        if (cm.getPlayer().getParty() != null) {
            var cost = 0;
            var scale = 0;
            var level = 0;
            if (selection == 1) {
                cost = 75;
                scale = 40;
                level = 1000;
            } else if (selection == 2) {
                cost = 100;
                scale = 45;
                level = 1250;
            } else if (selection == 3) {
                cost = 250;
                scale = 50;
                level = 1500;
            } else if (selection == 4) {
                cost = 100;
                scale = 16;
                level = 1000;
            }
            if (cost > 0 && cm.haveItem(item, cost)) {
                var em = cm.getEventManager("Elite2");
                if (em != null) {
                    if (!em.startPlayerInstance(cm.getPlayer(), level, scale)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(item, -cost);
                        cm.getPlayer().removeStamina(10);
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