var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function start() {
    cm.sendYesNo("Would you like to travel back to Kerning City?");
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
        cm.warp(103000000);
                    
    }
}