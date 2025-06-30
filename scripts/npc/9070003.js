var status = -1;
var option = 0;
var monster;
var monsters;

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        if (cm.getMap().getId() == 960000000) {
            option = 1;
            cm.sendYesNo("Hello#b #h ##k, Would you like to go back to New Leafe City");
        } else {
            if (cm.getMap().getAllMonsters().size() <= 0) {
                cm.sendOk("There are no monsters in this map.");
                cm.dispose();
                return;
            }
            var selStr = "Select which monster you wish to check.\r\n\r\n#b";
            monsters = cm.getMap().getAllUniqueMonsters();

            for (var i = 0; i < monsters.size(); i++) {
                var mob = monsters.get(i);
                if (mob != null) {
                    selStr += "#L" + i + "##o" + mob.getId() + "##l\r\n";
                }
            }
            cm.sendSimple(selStr);
        }
    } else if (status == 1) {
        if (option == 1) {
            cm.warp(600000000);
        } else {
            cm.sendOk(cm.checkDrop(monsters.get(selection)));
        }
    }
}