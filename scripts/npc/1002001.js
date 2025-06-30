var status = 0;
var groupsize = 0;
var item = 4310500;
var ach = 0;
var cost = 0;
var mob = 0;
var tier = 0;
var password;

function start() {
    cm.sendYesNo("Do you wish to change your #bpassword#k?\r\n#rThis action CANNOT be undone!!#k");
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
        cm.sendGetText("Enter the password you want to set\r\n#rMax Password length is 18 chars.#k");
    }
    if (status == 2) {
        password = cm.getText();
        if (cm.getTextSize() <= 18) {
            cm.sendYesNo("Are you sure want to apply this new password?\r\n#rThis action CANNOT be undone!!#k");
        } else {
            cm.sendOk("This password is too long.");
        }
    }
    if (status == 3) {
        cm.getPlayer().changePassword(password);
        cm.sendOk("Your #bPassword#k has been updated. Please relog to test...\r\nMake sure you write down you new password!!!");
    }
}