var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;
var item = 4036519;
var reward = 1182004;
var tier = 0;
var cost = 0;
var items = new Array(4036519, 4310500);
var amount = new Array(1000, 1);

function start() {
    var selStr = "";
    selStr += "#L1##i" + reward + "# " + cm.getItemName(1182004) + " #bCraft Tier: 1?#k #l\r\n";
    selStr += "#L5##i" + reward + "# " + cm.getItemName(1182004) + " #bCraft Tier: 5?#k #l\r\n";
    selStr += "#L10##i" + reward + "# " + cm.getItemName(1182004) + " #bCraft Tier: 10?#k #l\r\n";
    selStr += "#L25##i" + reward + "# " + cm.getItemName(1182004) + " #bCraft Tier: 25?#k #l\r\n";
    selStr += "#L50##i" + reward + "# " + cm.getItemName(1182004) + " #bCraft Tier: 50?#k #l\r\n";

    cm.sendSimple(selStr);
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
        if (cm.canHold(1182004, 1)) {
            tier = selection;
            var text = "#i" + reward + "# " + cm.getItemName(1182004) + " Stats:\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b Tier: " + (tier) + "#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (5000 * tier) + ": Str-Dex-Int-Luk#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (500 * tier) + ": Atk-Matk#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (5000 * tier) + ": Def-Mdef#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (10 * tier) + "%: Total Damage#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (10 * tier) + "%: Boss Damage#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (20 * tier) + "%: Overpower Damage#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b " + (20 * tier) + "%: Critcal Damage#k\r\n";
            text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b Hidden Slots: " + (5 * tier) + "#k\r\n";
            text += "#rWarning if you blow up this medal, it will not be refunded\r\nBE VERY VERY CAREFUL with it.#k\r\n";
            cm.sendNext("Would you like to purchase this medal#k?\r\n" + text);
        } else {
            cm.sendOk("Event requires 1 free slot in equips.");
        }
    }
    if (status == 2) {
        var selStr = "To Craft #bTier: " + tier + " #i" + reward + "# " + cm.getItemName(1182004) + "#k I will need following Materials:\r\n\ ";
        for (var i = 0; i < items.length; i++) {
            selStr += "   #i" + items[i] + "# " + cm.getItemName(items[i]) + " (x" + (amount[i] * tier * tier) + ")\r\n\ ";
        }
        selStr += "\r\n\#rWarning if you blow up this medal, it will not be refunded\r\nBE VERY VERY CAREFUL with it.#k\r\n";
        cm.sendYesNo(selStr);
    }
    if (status == 3) {
        var total = tier * tier;
        if (cm.haveItem(items[0], amount[0] * total) && cm.haveItem(items[1], amount[1] * total)) {
            cm.gainItem(items[0], -(amount[0] * total));
            cm.gainItem(items[1], -(amount[1] * total));
            cm.gainGodEquipTier(reward, tier, 10, 10, 20, 0, 20, 0);
            cm.sendOk("Thank you for purchasing #i" + reward + "#.");
        } else {
            cm.sendOk("You Do not have enough materials to craft #i" + reward + "# " + cm.getItemName(1182004));

        }

    }
}