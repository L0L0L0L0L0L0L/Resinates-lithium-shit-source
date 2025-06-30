var status = 0;
var level = 1000;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var key = 4420090;
var etc = 4310505;
var amount = 0;
var star = " #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn# ";

function start() {
    var range = 250 + cm.getPlayer().getAccVara("Pickup");
    if (range >= 1000) {
        var rewards = "Do you want to buy some Perm-Item-Vac?\r\n";
        rewards += star + "#bCost per hour is 10 #z4310505#.#k\r\n";
        rewards += star + "#bPremium Item Vac allows for instant item gains from any trash mob loot in the game being ETC or USE items.#k\r\n";
        rewards += star + "#bPremium Item Vac do not apply to boss drops or event style item drops.#k\r\n";
        rewards += star + "#bPremium Item Vac only works when @permloot is enabled.#k\r\n";
        rewards += star + "#bPremium Item Vac will also double any ETC and USE items collected thru this system.#k\r\n";
        var ptime = cm.getPlayer().getAccVara("Perm_Vac");
        if (ptime > 0) {
            var time = cm.secondsToString(ptime * 1000);
            rewards += "You currently have #r" + time + "#k Time Banked up.\r\n";
        }
        cm.sendYesNo(rewards);
    } else {
        cm.sendOk("#rYou must have Item Vac range of 1000 before using my services#k.");
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
        cm.sendGetText("How many hours do wish to buy?\r\n\r\n");
    } else if (status == 2) {
        amount = cm.getNumber();
        if (amount > 0 && amount <= 100000) {
            cm.sendYesNoS("Are you sure you want to exchange " + (10 * amount) + " #i" + etc + "# for #b" + amount + " Hours#k of Prem Item Vac?", 2);
        } else {
            cm.sendOk("enter a number greater than 0 and less than 100000.");
        }
    } else if (status == 3) {
        if (amount > 0 && amount <= 100000) {
            var tot = 10 * amount;
            var ztime = 3600 * amount;
            if (cm.haveItem(etc, tot)) {
                cm.gainItem(etc, -tot);
                var ptime = cm.getPlayer().getAccVara("Perm_Vac");
                cm.getPlayer().setAccVar("Perm_Vac", ptime + ztime);
                ptime = cm.getPlayer().getAccVara("Perm_Vac");
                var time = cm.secondsToString(ptime * 1000);
                cm.sendOk("You now have #b" + time + "#k remaining!\r\n#rUse @permloot to activate or deactivate your Prem-Item_Vac.#k");
            } else {
                cm.sendOk("Please bring me " + (10 * amount) + " #i" + etc + "# to add more time..");
            }
        } else {
            cm.sendOk("enter a number greater than 0 and less than 100000.");
        }
    }
}