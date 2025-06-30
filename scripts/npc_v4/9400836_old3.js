var status = 0;
var level = 120;
var minparty = 1;
var maxparty = 6;
var minraid = 1;
var maxraid = 40;

function start() {
    var text = "#i1672005# " + cm.getItemName(1672005) + " has Following Stats:\r\n";
    text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b 50,000 Str-Dex-Int-Luk#k\r\n";
    text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b 5,000 Atk-Matk#k\r\n";
    text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b 50,000 Def-Mdef#k\r\n";
    text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b Tier: 10#k\r\n";
    text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b Scroll Slots: 50#k\r\n";
    text += "    #fUI/UIWindow2.img/CTF/MinimapIcon/ItemRespawn##b 500% IED-Crit-All Stats#k\r\n";
    text += "#rWarning if you blow up your heart, it will not be refunded\r\nBE VERY VERY CAREFUL with it.#k\r\n";

    if (cm.haveItem(4310502, 2500)) {
        if (cm.canHold(1672005, 1)) {
            cm.sendYesNo("Would you like to purchase NX #i1672005# for #r2,500 Donation Points#k." + text);
        } else {
            cm.sendOk("Event requires 1 free slot in equips.");
        }
    } else {
        cm.sendOk("#i1672005# costs #r2,500 Donation Points#k." + text);
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
        if (cm.haveItem(4310502, 2500)) {
            if (cm.gainHeart()) {
                cm.gainItem(4310502, -2500);
                cm.sendOk("Thank you for purchasing #i1672005#.");
            } else {
                cm.sendOk("You current do not have enough room for #i1672005#.");
            }
        } else {
            cm.sendOk("#i1672005# costs #r2,500 Donation Points#k." + text);

        }

    }
}