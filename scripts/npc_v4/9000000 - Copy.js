importPackage(Packages.net.server.world);
importPackage(Packages.net.server);
importPackage(Packages.net.handling.world);
var status = 0;
var sel = 0;
var chars;
var count = 0;
/*
 #L0 = raid - member - list;
 L1 = leave - cm.sendYesNo;
 L2 = disband - cm.sendYesNo;
 L3 = invite - cm.sendGetText("");
 L4 = kick - cm.sendGetText("");
 L6 = close 
 */


function start() {
    if (cm.getPlayer().getParty() != null) {
        cm.sendOk("You must leave or disband your party before creating a raid.");

    } else {
        if (cm.getPlayer().getRaid() == null) {
            cm.sendYesNo("Would you like to create a raid?");
        } else {
            if (cm.getPlayer().isRaidLeader()) {
                cm.sendSimple("Raid Menu#l\r\n\#L0#Raid Members#l\r\n\#L3#Invite Member#l\r\n\#L4#Kick Memeber#l\r\n\#L2#Disband Raid#l\r\n\#L8#Change Leader#l\r\n\#L7#Close");
            } else {
                cm.sendSimple("Raid Menu#l\r\n\#L0#Raid Members#l\r\n\#L1#Leave Raid#l\r\n\#L7#Close");
            }
        }
    }
}



function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        sel = selection;
        if (selection == -1) {
            if (cm.getPlayer().getRaid() == null) {
                cm.getPlayer().createRaid();
                cm.sendOk("You have successfully created a raid.");
            } else {
                cm.sendOk("Sorry you cannot create a raid right now.");
            }

        } else if (selection == 0) {
            if (cm.getPlayer().getRaid() != null) {
                var selStr = "";
                chars = cm.getPlayer().getRaid().getMembers();
                for (var i = 0; i < chars.size(); i++) {
                    var chr = chars.get(i);
                    if (chr.isRaidLeader()) {
                        selStr += "#r[Leader] [L: " + chr.getTotalLevel() + "] " + chr.getName() + "#k\r\n";
                    } else {
                        selStr += "#g [L: " + chr.getTotalLevel() + "] " + chr.getName() + "#k\r\n";
                    }
                }
                cm.sendOk("Current raid members?\r\n\ " + selStr);
            } else {
                cm.sendOk("Error with showing raid members.");
            }

        } else if (selection == 1) {
            cm.sendYesNo("Would you like to leave the raid?");
        } else if (selection == 2) {
            cm.sendYesNo("Would you like to disband the raid?");
        } else if (selection == 3) {
            var selStr = "";
            chars = cm.getPlayer().getFreeMembers();
            if (!chars.isEmpty()) {
                for (var i = 0; i < chars.size(); i++) {
                    var chr = chars.get(i);
                    if (chr.getParty() == null && chr.getRaid() == null) {
                        selStr += "#L" + i + "##b [L: " + chr.getTotalLevel() + "] " + chr.getName() + "#k#l\r\n";
                    }
                }
                cm.sendSimple("Which raid member do you wish to invite?\r\n\ " + selStr);
            } else {
                cm.sendOk("Currently no players to invite.");

            }
        } else if (selection == 4) {
            var selStr = "";
            chars = cm.getPlayer().getRaidMembers();
            for (var i = 0; i < chars.size(); i++) {
                var chr = chars.get(i);
                if (!chr.isRaidLeader()) {
                    count += 1;
                    selStr += "#L" + i + "##g [L: " + chr.getTotalLevel() + "] " + chr.getName() + "#k#l\r\n";
                }
            }
            if (count > 0) {
                cm.sendSimple("Which raid member do you wish to expell?\r\n\ " + selStr);
            } else {
                cm.sendOk("Currently no memebrs in the raid.");

            }
        } else if (selection == 5) {
            cm.sendYesNo("Would you like to accept the invite from raid?");
        } else if (selection == 6) {
            cm.sendYesNo("Would you like to reject the invite from raid?");
        } else if (selection == 7) {
            cm.sendOk("Hmm... I guess you still have things to do here?");

        } else if (selection == 8) {
            var selStr = "";
            chars = cm.getPlayer().getRaidMembers();
            for (var i = 0; i < chars.size(); i++) {
                var chr = chars.get(i);
                if (!chr.isRaidLeader()) {
                    count += 1;
                    selStr += "#L" + i + "##g [L: " + chr.getTotalLevel() + "] " + chr.getName() + "#k#l\r\n";
                }
            }
            if (count > 0) {
                cm.sendSimple("Which raid member do you wish to become new leader?\r\n\ " + selStr);
            } else {
                cm.sendOk("Currently no memebrs in the raid to select from.");

            }
        }

    } else if (status == 2) {
        if (sel == 1) {
            cm.getPlayer().getRaid().leaveRaid(cm.getPlayer());
            cm.sendOk("You have left the raid.");

        }
        if (sel == 2) {
            cm.getPlayer().getRaid().disbandRaidByLeader(cm.getPlayer());
            cm.sendOk("Raid has been disbanded.");

        }
        if (sel == 3) {
            var member = chars.get(selection);
            if (member != null) {
                cm.getPlayer().getRaid().invite(cm.getPlayer(), member);
                cm.sendOk("Player: " + member.getName() + " has been invited to join the raid.");

            } else {
                cm.sendOk("Player: " + cm.getText() + " is not online or does not exist.");

            }
        }
        if (sel == 4) {
            var member = chars.get(selection);
            if (member != null) {
                cm.getPlayer().getRaid().removeMember(cm.getPlayer(), member);
                cm.sendOk("Player: " + member.getName() + " has been expelled.");

            } else {
                cm.sendOk("Player: " + cm.getText() + " is not online or does not exist.");

            }
        }
        if (sel == 8) {
            var member = chars.get(selection);
            if (member != null) {
                cm.getPlayer().getRaid().setLeader(member);
                cm.sendOk("Player: " + member.getName() + " is now the new leader.");

            } else {
                cm.sendOk("Player: " + cm.getText() + " is not online or does not exist.");

            }
        }
    }
}