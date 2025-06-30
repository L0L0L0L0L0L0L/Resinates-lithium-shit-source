var status = 0;
var groupsize = 0;
var item = 4310505;
var amount = 5;
var ach = 0;
var cost = 0;
var mob = 0;
var tier = 0;
var rb = 1;
var sAmount = 0;
var stat = "";

function start() {
    cm.sendYesNo("Spend #i4310505# " + cm.getItemName(4310505) + " to expand @boss stats?#k\r\n#bEach reborn adds 25% more stats alrdy calculated in prices.#k\r\n#bMastery Buff Does stack with final value given.");
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
        rb = 1 + Math.floor(cm.getPlayer().getReborns() * 0.25);
        var text = "Select a Stat you wish to buy?\r\n#bMastery Buff Does stack with this sales.\r\n#bSelect a Stat:#k\r\n";
        //text += "#L1# #rCash in #i" + orb + "#" + cm.getItemName(orb) + " for Fel Power!#k#l\r\n\r\n";
        text += "#L1##bExp#k (#r" + cm.convertNumber(rb * 50000) + "%#k)#l\r\n";
        text += "#L3##bAll Stat#k (#r" + cm.convertNumber(rb * 10000) + "%#k)#l#l\r\n";
        text += "#L4##bOverpower#k (#r" + cm.convertNumber(rb * 10000) + "%#k)#l#l\r\n";
        text += "#L6##bMob Damage#k (#r" + cm.convertNumber(rb * 10000) + "%#k)#l#l\r\n";
        text += "#L7##bBoss Damage#k (#r" + cm.convertNumber(rb * 10000) + "%#k)#l#l\r\n";
        text += "#L11##bCrit Damage#k (#r" + cm.convertNumber(rb * 10000) + "%#k)#l#l\r\n";
        //text += "#L8645340# #bAssailant#k #l\r\n";
        cm.sendSimple(text);
    }
    if (status == 2) {
        mob = selection;
        sAmount = 10000;
        if (mob == 1) {
            sAmount = 50000;
            stat = "EXP";
        }
        if (mob == 3) {
            stat = "ALL STAT";
        }
        if (mob == 4) {
            stat = "OVERPOWER";
        }
        if (mob == 6) {
            stat = "MOB DAMAGE";
        }
        if (mob == 7) {
            stat = "BOSS DAMAGE";
        }
        if (mob == 11) {
            stat = "CRITICAL DAMAGE";
        }
        cm.sendGetText("How much #b" + cm.getItemName(4310505) + "#k do you wish to spend on #b" + stat + "%#k?\r\nEach #b" + cm.getItemName(4310505) + "#k rewards #r" + cm.convertNumber(sAmount * rb) + "#k #b" + stat + "%#k#k");
    }
    if (status == 3) {
        amount = cm.getNumber();
        var total = sAmount * amount * rb;
        cm.sendYesNo("Are you sure to spend " + cm.getItemName(4310505) + " to expand\r\n#b" + stat + "#k by #r" + cm.convertNumber(total) + "%#k?");
    }
    if (status == 4) {
        if (cm.haveItem(item, amount)) {
            cm.gainItem(item, -amount);
            var total = sAmount * amount * rb;
            cm.getPlayer().gainStat(total, mob);
            cm.sendOk("You have successfully bought base #r" + cm.convertNumber(total) + "#k #b" + stat + "%#k");
        } else {
            cm.sendOk("Where is my #r" + amount + "#k #b#i" + item + "# " + cm.getItemName(item) + "s#k????");
        }

    }
}