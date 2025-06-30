var status = 0;
var groupsize = 0;
var item = 4036088;
var amount = 5;
var ach = 0;
var cost = 0;
var mob = 0;
var tier = 0;

function start() {
    if (!cm.getPlayer().isGroup()) {
        if (cm.haveItem(item, 1)) {
            cm.sendYesNo("Do you wish take take on my special dungeon for extra stats?");
        } else {
            cm.sendOkS("I must bring #i" + item + "##b" + cm.getItemName(item) + "#k", 2);
        }

    } else {
        cm.sendOkS("I must take on this Challenge alone.", 2);
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
        var text = "Select a Stat you iwsh to farm\r\n#rMonsters here are do not have any drops.#k\r\nEach Star you defeat rewards #bx% of x Stat#k.\r\nEach Simulation lasts 30 mins.\r\n#bSelect a Monster:#k\r\n";
        //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
        text += "#L9834032# #bYellow Star#k (#r50% All Stats#k)#l\r\n";
        text += "#L9834033# #bPurple Star#k (#r50% Overpower#k)#l\r\n";
        text += "#L9834034# #bOrange Star#k (#r50% Boss Damage#k)#l\r\n";
        text += "#L9834035# #bGreen Star#k (#r100% IED#k)#l\r\n";
        text += "#L9834036# #bRainbow Star#k (#r250% Exp Rate#k)#l\r\n";
        //text += "#L8645340# #bAssailant#k #l\r\n";
        cm.sendSimple(text);
    }
    if (status == 2) {
        mob = selection;
        var text = "Select a Spawn chance\r\n#rMax number of stars on the map is 50#k\r\n";
        //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
        text += "#L1# Spawn rate: #r100%#k - Price: #b10 #t" + item + "##k#l\r\n";
        text += "#L2# Spawn rate: #r200%#k - Price: #b20 #t" + item + "##k#l\r\n";
        text += "#L3# Spawn rate: #r300%#k - Price: #b30 #t" + item + "##k#l\r\n";
        text += "#L5# Spawn rate: #r500%#k - Price: #b40 #t" + item + "##k#l\r\n";
        text += "#L10# Spawn rate: #r1000%#k - Price: #b50 #t" + item + "##k#l\r\n";
        text += "#L25# Spawn rate: #r2500%#k - Price: #b100 #t" + item + "##k#l\r\n";
        //text += "#L25# Spawn chance: #r25%#k - Price: #b250 #t" + item + "##k#l\r\n";
        //text += "#L125# Tier: 125#k - Price: #b2500 #t4036518##k#l\r\n";
        //text += "#L150# Tier: 150#k - Price: #b5000 #t4036518##k#l\r\n";
        //text += "#L200# Tier: 200#k - Price: #b10000 #t4036518##k#l\r\n";
        //text += "#L250# Tier: 250#k - Price: #b25000 #t4036518##k#l\r\n";
        cm.sendSimple(text);
    }
    if (status == 3) {
        var em = cm.getEventManager("star_dung");
        if (em != null) {
            amount = 10;
            if (selection == 2) {
                amount = 20;
            }
            if (selection == 3) {
                amount = 30;
            }
            if (selection == 5) {
                amount = 40;
            }
            if (selection == 10) {
                amount = 50;
            }
            if (selection == 25) {
                amount = 100;
            }
            if (cm.haveItem(item, amount)) {
                if (!em.startPlayerInstance(cm.getPlayer(), mob, selection)) {
                    cm.sendOk("Someone is already attempting the PQ or your instance is currently being reseted. Try again in few seconds.");
                } else {
                    cm.gainItem(item, -amount);
                }
            } else {
                cm.sendOk("Where is my #r" + amount + "#k #b#i" + item + "# " + cm.getItemName(item) + "s#k????");
            }
        } else {
            cm.sendOk("Event has already started, Please wait.");
        }

    }
}