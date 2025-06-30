var status = -1;
var option = 0;
var monster;
var monsters;
var star = "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
var option = 0;
var jobName = new Array("", "Str%", "Dex%", "Int%", "Luk%", "Atk%", "M-Atk%", "Def%", "M-Def%", "Hp", "Mp", "Mob%", "Boss%", "IED", "CD%");
var jobAmount = new Array(0, 1, 1, 1, 1, 2, 2, 5, 5, 250, 250, 1, 1, 5, 1);
var HSamount = new Array();
var stat = 0;
var cost = 0;
var amount = 0;
var item = 4310505;


function getIcon(value) {
    return "#fUI/Custom.img/stat/" + value + "#";
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        var text = "Select an Option\r\n";
        //text += "#L1#" + star + " Equips#l\r\n";
        text += "   #L81#" + star + " Buy #bHyper Stats#k #l\r\n";
        text += "   #L82#" + star + " View my #rHyper Stats#k#l\r\n";
        cm.sendSimple("" + text);
    } else if (status == 1) {
        if (selection == 81) {
            option = 1;
            var shards = cm.getPlayer().countAllItem(item);
            cm.sendGetText("How many #b#z" + item + "##k do you wish to cash in?\r\n#bPrice: 1 #z" + item + "# for 1 Stat of choice#k?\r\nCurrent #z" + item + "# #b" + cm.convertNumber(shards) + "#k\r\nCurrent Hyper Stats to buy #b" + cm.convertNumber(Math.floor(shards)) + "#k");
        }
        if (selection == 82) {
            var text = "\r\n";
            for (var i = 1; i < jobName.length; i++) {
                text += "" + getIcon(i) + " #b" + jobName[i] + "#k (#r" + cm.getPlayer().getBonusStat(i) + "#k)\r\n";
            }
            cm.sendOk(text);
        }
    } else if (status == 2) {
        if (option == 1) {
            amount = cm.getNumber();
            cost = amount;
            if (amount > 0 && cm.haveItem(item, cost)) {
                var text = "";
                for (var i = 1; i < jobName.length; i++) {
                    if (i == 9 || i == 10) {
                        text += "#L" + i + "#" + getIcon(i) + " #b" + jobName[i] + "#k (#r" + cm.getPlayer().getBonusStat(i) + "#k) (+#b" + (jobAmount[i] * amount) + "#k) (#rMax: 9,999,999#k)#l\r\n";
                    } else {
                        text += "#L" + i + "#" + getIcon(i) + " #b" + jobName[i] + "#k (#r" + cm.getPlayer().getBonusStat(i) + "#k) (+#b" + (jobAmount[i] * amount) + "#k)#l\r\n";
                    }
                }
                cm.sendSimple("Welcome to Hyper Stat System.\r\n#bSelect a Hyper Stat#k:\r\n" + text);
            } else {
                cm.sendOk("You dont have enough #z" + item + "# to apply.");
            }
        }
    } else if (status == 3) {
        if (option == 1) {
            stat = selection;
            cm.sendYesNo("Are you sure you wish to spend:\r\n#r" + cost + " #z" + item + "##k on (+#b" + (jobAmount[stat] * amount) + "#k) #b" + jobName[stat] + "#k?");
        }
    } else if (status == 4) {
        if (option == 1) {
            if (cm.haveItem(item, cost)) {
                cm.gainItem(item, -cost);
                cm.getPlayer().setBonusStat(stat, jobAmount[stat] * amount);
                cm.sendOk("Your Hyper Stat #r" + jobName[stat] + "#k has been increased by #b" + (jobAmount[stat] * amount) + "#k.");
            } else {
                cm.sendOk("You dont have enough #z" + item + "# to apply.");
            }
        }
    }
}