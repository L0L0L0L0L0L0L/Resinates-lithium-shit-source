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
        text += "   #L10#" + star + " Cash in #bHyper Shards#k#l\r\n";
        text += "   #L12#" + star + " Cash in Multiple #bHyper Shards#k#l\r\n";
        text += "   #L11#" + star + " View my #rHyper Stats#k#l\r\n";
        cm.sendSimple("" + text);
    } else if (status == 1) {
        if (selection == 10) {
            option = 1;
            cm.sendYesNo("Do you wish to exchange shards?\r\nShards are consumed one at a time with #brandom choice#k of #r4#k Stats to apply the shard to.\r\n#rHyper Shards are consumed before choices are shown. Lost Shards will NOT be refunded.#k");
        }
        if (selection == 11) {
            var text = "\r\n";
            for (var i = 1; i < jobName.length; i++) {
                text += "" + getIcon(i) + " #b" + jobName[i] + "#k (#r" + cm.getPlayer().getBonusStat(i) + "#k)\r\n";
            }
            cm.sendOk(text);
        }
        if (selection == 12) {
            option = 2;
            cm.sendGetText("How many #rHyper Shards#k do you wish to cash for\r\n#bRandomized Hyper-Stats#k?\r\nCurrent Hyper Shards #b" + cm.convertNumber(cm.getPlayer().countAllItem(item)) + "#k");
        }
    } else if (status == 2) {
        if (option == 1) {
            if (cm.haveItem(item, 1)) {
                cm.gainItem(item, -1);
                var text = "";
                var randomStat = cm.getPlayer().randomStats();
                for (var i = 1; i <= 4; i++) {
                    var c = randomStat.get(i);
                    text += "#L" + c + "#" + getIcon(c) + " #b" + jobName[c] + "#k (#r" + cm.getPlayer().getBonusStat(c) + "#k) (+#b" + jobAmount[c] + "#k)#l\r\n";
                }
                cm.sendSimpleS("Welcome to Hyper Stat System.\r\n#bSelect a Hyper Stat#k:\r\n" + text, 1);
            } else {
                cm.sendOk("You dont have enough shards to apply.");
            }
        }
        if (option == 2) {
            amount = cm.getNumber();
            if (amount > 0 && cm.haveItem(item, amount)) {
                cm.sendYesNo("Are you sure you want to cash in #bx" + amount + " Hyper shards#k?");
            } else {
                cm.sendOk("You dont have enough shards to apply.");
            }
        }
    } else if (status == 3) {
        if (option == 1) {
            stat = selection;
            cm.getPlayer().setBonusStat(stat, jobAmount[stat]);
            if (cm.haveItem(item, 1)) {
                cm.sendYesNo("Your Hyper Stat #r" + jobName[stat] + "#k has been increased by #b" + (jobAmount[stat]) + "#k.\r\n#rDo you want to use another shard?#k");
            } else {
                cm.sendOk("Your Hyper Stat #r" + jobName[stat] + "#k has been increased by #b" + (jobAmount[stat]) + "#k.");
            }
        }
        if (option == 2) {
            if (cm.haveItem(item, amount)) {
                cm.gainItem(item, -amount);
                for (var i = 1; i <= 14; i++) {
                    HSamount[i] = cm.getPlayer().getBonusStat(i);
                }
                cm.getPlayer().setRnadomBonusStat(amount);
                var text = "Gained Following Stats:\r\n";
                var randomStat = cm.getPlayer().randomStatsOrder();
                for (var i = 1; i <= 14; i++) {
                    var hsa = cm.getPlayer().getBonusStat(i) - HSamount[i];
                    if (hsa > 0) {
                        var c = randomStat.get(i);
                        text += "" + getIcon(c) + " #r" + jobName[i] + "#k (#b" + HSamount[i] + "#k) (#g+" + hsa + "#k)\r\n";
                    }
                }
                cm.sendOk("#b" + amount + "#k Hyper Shards have been randomly applied.\r\n" + text);
            } else {
                cm.sendOk("You dont have enough shards to apply.");
            }
        }
    } else if (status == 4) {
        if (option == 1) {
            status = 3;
            action(0, 0, 0);
        }
    }
}