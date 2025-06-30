var status = -1;
var option = 0;
var monster;
var monsters;
var star = "#fUI/UIWindow2.img/ToolTip/Equip/Star/Star2# ";
var option = 0;
var jobName = new Array("", "Str%", "Dex%", "Int%", "Luk%", "Atk%", "M-Atk%", "Def%", "M-Def%", "Hp", "Mp", "Mob%", "Boss%", "IED", "CD%");
var jobAmount = new Array(0, 5, 5, 5, 5, 5, 5, 10, 10, 1000, 1000, 10, 10, 100, 10);
var HSamount = new Array();
var stat = 0;
var cost = 0;
var amount = 0;
var item = 4420090;


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
        text += "   #L81#" + star + " Cash in Multiple #bHyper Shards#k#l\r\n";
        text += "   #L82#" + star + " View my #rHyper Stats#k#l\r\n";
        cm.sendSimple("" + text);
    } else if (status == 1) {
        if (selection == 81) {
            option = 1;
            var shards = cm.getPlayer().countAllItem(item);
            cm.sendGetText("How many #rHyper Shards#k do you wish to cash in?\r\n#bPrice: 5 Hyper shards for 1 Stat of choice#k?\r\nCurrent Hyper Shards #b" + cm.convertNumber(shards) + "#k\r\nCurrent Hyper Stats to buy #b" + cm.convertNumber(Math.floor(shards * 0.2)) + "#k");
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
            cost = amount * 5;
            if (amount > 0 && cm.haveItem(item, cost)) {
                var text = "";
                for (var i = 1; i < jobName.length; i++) {
                    text += "#L" + i + "#" + getIcon(i) + " #b" + jobName[i] + "#k (#r" + cm.getPlayer().getBonusStat(i) + "#k) (+#b" + (jobAmount[i] * amount) + "#k)#l\r\n";
                }
                cm.sendSimple("Welcome to Hyper Stat System.\r\n#bSelect a Hyper Stat#k:\r\n" + text);
            } else {
                cm.sendOk("You dont have enough shards to apply.");
            }
        }
    } else if (status == 3) {
        if (option == 1) {
            stat = selection;
            cm.sendYesNo("Are you sure you wish to spend:\r\n#r" + cost + " Hyper Shards#k on (+#b" + (jobAmount[stat] * amount) + "#k) #b" + jobName[stat] + "#k?");
        }
    } else if (status == 4) {
        if (option == 1) {
            if (cm.haveItem(item, cost)) {
                cm.gainItem(item, -cost);
                cm.getPlayer().setBonusStat(stat, jobAmount[stat] * amount);
                cm.sendOk("Your Hyper Stat #r" + jobName[stat] + "#k has been increased by #b" + (jobAmount[stat] * amount) + "#k.");
            } else {
                cm.sendOk("You dont have enough shards to apply.");
            }
        }
    }
}