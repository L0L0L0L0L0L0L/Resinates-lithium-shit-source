var status = 0;
var groupsize = 0;
var item = 4420015;
var ach = 0;
var option = 0;
var em;
var cost = 1;
var level = 1;
var rank = 0;

function start() {
    var selStr = "Welcome to #rReward Zone#k\r\n";
    selStr += "#L4420055##i4420055##l ";
    selStr += "#L4420056##i4420056##l ";
    selStr += "#L4420057##i4420057##l ";
    cm.sendSimple(selStr);


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
        item = selection;
        if (cm.haveItem(item, cost)) {
            cm.sendYesNo("Are you sure you want to cash in #r" + cost + "#k #i" + item + "#?\r\n\#rEtc Bonus% does not apply to drops in this zone.#k");
        } else {
            cm.sendOk("Event is requires #i" + item + "# to enter.");
        }
    }
    if (status == 2) {
        if (!cm.getPlayer().isGroup()) {
            if (cm.haveItem(item, cost)) {
                var em = cm.getEventManager("MP_Endless_Reward");
                if (em != null) {
                    if (item == 4420055) {
                        level = 1;
                    }
                    if (item == 4420056) {
                        level = 2;
                    }
                    if (item == 4420057) {
                        level = 3;
                    }
                    if (!em.startPlayerInstance(cm.getPlayer(), level, 1, 99)) {
                        cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                    } else {
                        cm.gainItem(item, -cost);
                    }
                } else {
                    cm.sendOk("Event has already started, Please wait.");
                }
            } else {
                cm.sendOk("Event is requires at least " + cost + " of #i" + item + "#.");
            }
        } else {
            cm.sendOk("Event is Solo Only.");
        }
    }
}