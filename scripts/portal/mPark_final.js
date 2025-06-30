function enter(pi) {
    var eim = pi.getPlayer().getEventInstance();
    if (eim != null) {
        var map = pi.getEventMap();
        var mapid = map.getId();
        if (eim.getValue("park") > 0) {
            if (pi.getPlayer().getMap().allMonsterDead()) {
                var currentMap = eim.getMapInstance(pi.getMap().getId());
                var baseMap = eim.getMapInstance(4700);
                if (currentMap.getVar("cleared") == 0) {
                    currentMap.setVar("cleared", 1);
                    eim.setValue("clear", eim.getValue("clear") + 1);
                    baseMap.setObjectFlag("door_" + currentMap.getVar("section"), true);
                    baseMap.setObjectFlag("portal_" + currentMap.getVar("section"), false);
                    eim.setValue("room_" + currentMap.getVar("section"), 1);
                    eim.dropMessage(6, "[MasterMP] Zone " + currentMap.getVar("section") + " has been cleared!");
                    if (eim.getValue("clear") >= 17) {
                        baseMap.setObjectFlag("final_portal", true);
                        eim.dropMessage(6, "[MasterMP] All Zones have been cleared! Final Boss is now Open!");
                    }
                    eim.gainPartyItem(4310020, eim.getValue("scale") * 10);
                }
                pi.warp(4700);
            }
        } else {
            if (eim.getValue("start") > 0) {
                if (pi.getPlayer().getMap().allMonsterDead()) {
                    pi.playPortalSE();
                    if (map.isEndless()) {
                        if (pi.getPlayer() == eim.getOwner()) {
                            var stage = eim.getValue("stage");
                            var mid = eim.getValue("mapid");
                            if (eim.getValue("clear") >= 1) {
                                if (eim.getValue("clear") == 1) {
                                    if (!eim.processNextMap(map)) {
                                        pi.playerMessage(5, eim.getCoolDown());
                                    }
                                } else {
                                    eim.exitPlayer(pi.getPlayer(), eim.getValue("exit"));
                                }
                                return true;
                            } else {
                                pi.playerMessage(5, "Event Mapid: " + mid);
                                pi.playerMessage(5, "Event Current Stage: " + stage);
                                pi.playerMessage(5, "Current Portal Map: " + mapid);
                                pi.playerMessage(5, "Map is not cleared of all objectives.");
                            }
                        } else {
                            if (eim.getValue("clear") > 1) {
                                eim.exitPlayer(pi.getPlayer(), eim.getValue("exit"));
                            } else {
                                pi.playerMessage(5, "Only Event Owner can go through the portal.");
                            }
                        }
                    } else {
                        eim.exitPlayer(pi.getPlayer(), eim.getValue("exit"));
                    }
                } else {
                    pi.playerMessage(5, "There are monsters still on Map.");
                }
            } else {
                pi.playerMessage(5, "Portal has error.");
            }
        }
    } else {
        pi.playerMessage(5, "Portal has error.");
    }
    return false;
}