var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var list;

var gstar = 4420008;
var gstarCost = 0;
var etc = 4310018;
var etcCost = 0;
var key = 4420010;
var keyCost = 0;
var amount = 0;
var ticket = 0;
var tier = new Array("Common", "Un-Common", "Rare", "Epic", "Legendary", "Supreme");
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";
var star2 = " #fEffect/Misc.img/orbs/0# ";
var ticketid = 0;

var dskinId = 0;
var option = 0;
var cost = 0;


function start() {
    cm.sendSimpleS("#L2##bExchange Reward Tickets for Crusader Coins#k#l\r\n", 16);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        option = selection;
        if (option == 2) {
            var selStr = "Which Reward Ticket do you wish to recycle:\r\n\r\n";
            selStr += ("   #L10##b#i4420011# - (" + cm.getItemName(etc) + "s: #i" + etc + "# (x1))#k#l\r\n");
            selStr += ("   #L11##b#i4420012# - (" + cm.getItemName(etc) + "s: #i" + etc + "# (x2))#k#l\r\n");
            selStr += ("   #L12##b#i4420013# - (" + cm.getItemName(etc) + "s: #i" + etc + "# (x5))#k#l\r\n");
            selStr += ("   #L13##b#i4420014# - (" + cm.getItemName(etc) + "s: #i" + etc + "# (x10))#k#l\r\n");
            selStr += ("   #L14##b#i4420015# - (" + cm.getItemName(etc) + "s: #i" + etc + "# (x25))#k#l\r\n");
            cm.sendSimpleS(selStr, 16);
        }
    } else if (status == 2) {
        if (option == 2) {
            if (selection == 10) {
                ticket = 4420011;
                etcCost = 1;
            }
            if (selection == 11) {
                ticket = 4420012;
                etcCost = 2;
            }
            if (selection == 12) {
                ticket = 4420013;
                etcCost = 5;
            }
            if (selection == 13) {
                ticket = 4420014;
                etcCost = 10;
            }
            if (selection == 14) {
                ticket = 4420015;
                etcCost = 25;
            }
            cm.sendGetTextS("How many #i" + ticket + "# do you want to recyle?\r\nEach Ticket is worth #b" + etcCost + "#k " + cm.getItemName(etc) + "s\r\nCurrent Morale-ETC Amount: (#b" + cm.convertNumber(cm.getPlayer().countAllItem(ticket)) + "#k)\r\n\r\n", 16);

        }
    } else if (status == 3) {
        if (option == 2) {
            amount = cm.getNumber();
            if (amount > 0 && amount <= 100000) {
                cm.sendYesNoS("Are you sure you want to exchange\r\n\r\n#i" + ticket + "# (#b" + amount + "x#k) for #i" + etc + "# (#b" + (amount * etcCost) + "x#k)?", 16);
            } else {
                cm.sendOkS("enter a number greater than 0 and less than 100000.", 16);
            }

        }
    } else if (status == 4) {
        if (option == 2) {
            if (cm.haveItem(ticket, amount)) {
                if (cm.canHold(etc, amount * etcCost)) {
                    cm.gainItem(ticket, -amount);
                    cm.gainItem(etc, amount * etcCost);
                    cm.sendOkS("You have successfully exchanged your tickets for #b" + amount * etcCost + "#k " + cm.getItemName(etc) + "s.", 16);
                } else {
                    cm.sendOkS("You do not have the space to make this exchange.", 16);
                }
            } else {
                cm.sendOkS("You do not have the required items to make this exchange.", 16);
            }
        }
    } else if (status == 5) {
        cm.sendOkS("You do not have the required items to make this exchange.", 16);
    }
}