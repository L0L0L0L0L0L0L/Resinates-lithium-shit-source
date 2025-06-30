var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;


function start() {
    cm.sendOk("Event is Solo Mode Only.");  
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        var em = cm.getEventManager("EventBattleFree");
        if (em != null) {
            if (!em.startPlayerInstance(cm.getPlayer(), selection)) {
                cm.playerMessage(5, "Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
            } else {
                cm.gainItem(4031034, -5);
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }
    }
}