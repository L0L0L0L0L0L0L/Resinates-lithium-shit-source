
var status;
var sel;
var summon = 4006001;
var amount = 0;
var item = 4310066;
var status = -1;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        if (cm.getPlayer().getGuild() != null) {
            cm.sendGetText("How many Unleashed Coins do you want to exchange into Guild Points? You current have "+cm.convertNumber(cm.getPlayer().countItem(item))+" Unleashed Coins to cash in.");
        } else {
            cm.sendOk("You must be in a guild to exchange Unleashed Coins into guild points.");
                        
        }
    } else if (status == 1) {
        amount = cm.getNumber();
        if (amount > 0 && cm.haveItem(4310066, amount)) {
            cm.sendYesNo("Do you want to confirm you want to exchange " + amount + " Unleashed Coins for " + amount + " Guild Points?");
        } else {
            cm.sendOk("You do not have enough Unleashed Coins.");
                        
        }
    } else if (status == 2) {
        if (cm.removeItem(item, amount)) {
            cm.getPlayer().gainGP(amount);
            cm.sendOk("Your guild has gained "+amount+" Guild Points.");
        } else {
            cm.sendOk("You do not have enough Unleashed Coins.");
        }
                    
    }
}













