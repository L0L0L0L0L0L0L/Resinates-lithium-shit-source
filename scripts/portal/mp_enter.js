function enter(pi) {
    pi.playPortalSE();
    var eim = pi.getEventInstance();
    if (eim != null) {
        var map = null;
        var enter = true;
        if (pi.PortalName() == "portal_01") {
            map = eim.getMapInstance(952000000);
            if (eim.getValue("room_1") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_02") {
            map = eim.getMapInstance(952010000);
            if (eim.getValue("room_2") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_03") {
            map = eim.getMapInstance(952020000);
            if (eim.getValue("room_3") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_04") {
            map = eim.getMapInstance(952030000);
            if (eim.getValue("room_4") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_05") {
            map = eim.getMapInstance(952040000);
            if (eim.getValue("room_5") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_06") {
            map = eim.getMapInstance(953000000);
            if (eim.getValue("room_6") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_07") {
            map = eim.getMapInstance(953010000);
            if (eim.getValue("room_7") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_08") {
            map = eim.getMapInstance(953020000);
            if (eim.getValue("room_8") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_09") {
            map = eim.getMapInstance(953030000);
            if (eim.getValue("room_9") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_10") {
            map = eim.getMapInstance(953040000);
            if (eim.getValue("room_10") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_11") {
            map = eim.getMapInstance(953050000);
            if (eim.getValue("room_11") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_12") {
            map = eim.getMapInstance(954000000);
            if (eim.getValue("room_12") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_13") {
            map = eim.getMapInstance(954010000);
            if (eim.getValue("room_13") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_14") {
            map = eim.getMapInstance(954020000);
            if (eim.getValue("room_14") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_15") {
            map = eim.getMapInstance(954030000);
            if (eim.getValue("room_15") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_16") {
            map = eim.getMapInstance(954040000);
            if (eim.getValue("room_16") > 0) {
                enter = false;
            }
        }
        if (pi.PortalName() == "portal_17") {
            map = eim.getMapInstance(954050000);
            if (eim.getValue("room_17") > 0) {
                enter = false;
            }
        }
        if (enter) {
            if (map != null) {
                map.setFlag("master", true);
                if (!map.getFlag("spawned")) {
                    eim.setMapInfo(map, eim.getValue("exit"));
                    map.killAllMonsters(true);
                    map.setFlag("spawned", true);
                    map.setEndless(true);
                    map.setPark(true);
                    map.setExpRate(eim.getPlayerCount());
                    map.parkKaoticSpawn(eim.getValue("level") * 1.25, eim.getValue("scale"));
                }
                pi.warp(map.getId());
                pi.getPlayer().dropMessage(6, "Defeat all the fucking monsters.");
            }
        }
    } else {
        pi.warp(951000000, "sp00");
    }
    return true;
}