var status = 0;
var level = 1000;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var key = 2041200;
var etc = 4001094;

function start() {
    if (!cm.haveItem(key)) {
        if (cm.haveItem(etc)) {
            cm.sendYesNo("Did you bring back my #i" + etc + "#? I will give you #i" + key + "# in return.");
        } else {
            cm.sendOk("Please bring me #i" + etc + "#.");
                        
        }
    } else {
        cm.sendOk("It seems you all ready have a #i" + key + "# on you.");
                    
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
        cm.gainItem(etc, -1);
        cm.gainItem(key, 1);
        cm.sendOk("Thank YOU!!!.");
                    
    }
}