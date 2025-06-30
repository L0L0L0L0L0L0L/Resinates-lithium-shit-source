var status = 0;
var option = 0;
var item = 0;
var amount = 0;
var star = 4310335;
var starAmount = 0;
var gas = 1;
var total = 0;
//4310502 - RP
//4310504 - DP
//4310505 - IP

function start() {

    var text = "Selection an Option:\r\n";
    text += "#L1# Convert Various Coins to #i4310335##l\r\n";
    text += "#L2# Convert #i4310335# to #i4310502# (#bx5000#k)#l\r\n";
    text += "#L3# Convert #i4310335# to #i4310504# (#bx1#k)#l\r\n";
    cm.sendSimple("Welcome to my Currency Exchange.\r\n" + text);

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
    if (status == 1) {//-------------------------------------------------
        option = selection;
        if (option == 1) {
            selection = 0;
            var text = "Selection a Currency:\r\n";
            text += "#L10# Convert #i4310502# (#bx1000#k) -> #i4310335# (#bx1#k)#l\r\n";
            text += "#L11# Convert #i4310504# (#bx1#k) -> #i4310335# (#bx5#k)#l\r\n";
            text += "#L12# Convert #i4310505# (#bx1#k) -> #i4310335# (#bx1000#k)#l\r\n";
            cm.sendSimple(text);
        }
        if (option == 2) {
            item = 4310502;
            amount = 1000;
        }
        if (option == 3) {
            item = 4310504;
            amount = 5;
        }
    }
    if (status == 2) {//-------------------------------------------------
        if (option == 1) {
            option = selection;
            if (option == 10) {
                item = 4310502;
                amount = 1000;
                starAmount = 1;
            }
            if (option == 11) {
                item = 4310502;
                amount = 1;
                starAmount = 5;
            }
            if (option == 12) {
                item = 4310502;
                amount = 1;
                starAmount = 1000;
            }
        }
        if (option == 2 || option == 3) {
            cm.sendGetText("How many stars do wish to convert?\r\nEach Star will give #i" + item + "# (#bx" + amount + "#k)");
        }
    }
    if (status == 3) {//-------------------------------------------------
        if (option == 1) {
            if (option == 10 || option == 11 || option == 12) {
                cm.sendGetText("How many #i" + item + "# do wish to convert to stars?\r\nEach #i" + item + "# (#rx" + amount + "#k) will give 4310335 (#bx" + starAmount + "#k)");
            }
        }
        if (option == 2 || option == 3) {
            total = cm.getNumber();
            cm.sendYesNo("Do you wish to convert "++" "++" "++" "++"  ");
        }
    }
    if (status == 4) {//-------------------------------------------------
        if (option == 1) {
            total = cm.getNumber();
            cm.sendYesNo(text);
        }
        if (option == 2 || option == 3) {

        }

    }
    if (status == 5) {//-------------------------------------------------
        if (option == 1) {

        }
        if (option == 2 || option == 3) {

        }

    }
}