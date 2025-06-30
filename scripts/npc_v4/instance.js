var status = 0;
var groupsize = 0;
var item = 4310502;
var ach = 0;
var cost = 0;
var tier = 0;
var time = 1800;
var level = 0;
var map = 0;
var questid = 0;

function start() {
    var player = cm.getPlayer();
    if (cm.getPlayer().getTotalLevel() >= 10) {
        if (!player.isGroup()) {
            if (!player.isMapChange() && player.isAlive()) {
                if (!player.getMap().getSpawnCount().isEmpty()) {
                    if (player.getEventInstance() == null) {
                        level = cm.getPlayer().getTotalLevel();
                        var text = ":\r\n";
                        questid = cm.getPlayer().getMapId() + 1000000000;
                        if (cm.getPlayer().getQuestLock(questid) > 0) {
                            text += "#L0##fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# #e#r" + cm.secondsToString(cm.getPlayer().getQuestLock(questid)) + "#k - #rRemaining#k#n#l\r\n\r\n";
                        }
                        text += "#L15# #bNew Time: 30 Minutes#k - #rCost: FREE#k#l\r\n";
                        text += "#L1# #bNew Time: 1 Hour#k - #rCost: 10 #i4310502##k#l\r\n";
                        text += "#L2# #bNew Time: 2 Hour#k - #rCost: 20 #i4310502##k#l\r\n";
                        text += "#L4# #bNew Time: 4 Hour#k - #rCost: 40 #i4310502##k#l\r\n";
                        text += "#L8# #bNew Time: 8 Hour#k - #rCost: 80 #i4310502##k#l\r\n";
                        text += "#L12# #bNew Time: 12 Hour#k - #rCost: 120 #i4310502##k#l\r\n ";
                        cm.sendSimpleS("Would you like to train in your personal zone#k?\r\n\#rBotting is Allowed in this instance#k\r\n\#r@at or @at move is allowed#k\r\n\Each event Lasts for X hours and is #rSOLO Only#k." + text, 2);
                    } else {
                        cm.sendOkS("You are already inside an instance..", 2);
                    }
                } else {
                    cm.sendOkS("Cannot make instance on a map with no monster spawns.", 2);
                }
            } else {
                cm.sendOkS("Cannot create an instance while changing maps or dead.", 2);
            }
        } else {
            cm.sendOkS("@instance is Solo mode only.", 2);
        }
    } else {
        cm.sendOkS("@instance is for level 11 or higher.", 2);
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
        cost = selection * 10;
        time = (selection * 3600);
        if (selection == 15) {
            cost = 0;
            time = 1800;
        } else {
            if (cost == 0) {
                time = cm.getPlayer().getQuestLock(questid);
            }
        }
        if (cost == 0 || cm.haveItem(item, cost)) {
            em = cm.getEventManager("player_instance");
            if (!em.startPlayerInstance(cm.getPlayer(), cm.getPlayer().getMapId(), time * 1000)) {
                cm.sendOkS("Error with making personal instance.", 2);
            } else {
                if (cost > 0) {
                    cm.gainItem(item, -cost);
                }
                cm.getPlayer().setQuestLock(questid, time);
                cm.getPlayer().dropMessage("You are free to afk and bot in this instance.");
            }
        } else {
            cm.sendOkS("You dont have enough #i" + item + "# for this event.", 2);
        }
    }
}