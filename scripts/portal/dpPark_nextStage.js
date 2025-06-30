function enter(pi) {
    var eim = pi.getPlayer().getEventInstance();
    if (eim != null) {
        var map = pi.getEventMap();
        var mapid = map.getId();
        if (eim.getValue("park") > 0) {
            if (pi.getPlayer().getMap().allMonsterDead()) {
                var nextMapId = mapid + 100;
                var NextMap = eim.getMapInstance(nextMapId);
                eim.setMapInfo(NextMap, eim.getValue("exit"));
                NextMap.setFlag("master", true);
                if (!NextMap.getFlag("spawned")) {
                    NextMap.killAllMonsters(true);
                    NextMap.setFlag("spawned", true);
                    NextMap.setEndless(true);
                    NextMap.setPark(true);
                    NextMap.setExpRate(eim.getPlayerCount());
                    NextMap.parkKaoticSpawn(eim.getValue("level") * 1.25, eim.getValue("scale"));
                }
                pi.warp(nextMapId);
                pi.getPlayer().dropMessage(6, "Defeat all the monsters.");
                return true;
            }
        } else {
            if (eim.getValue("start") > 0) {
                if (pi.getPlayer().getMap().allMonsterDead()) {
                    pi.playPortalSE();
                    if (map.isEndless()) {
                        if (pi.getPlayer() == eim.getOwner()) {
                            var stage = eim.getValue("stage");
                            var emid = eim.getValue("mapid");
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
                                pi.playerMessage(5, "Event Mapid: " + emid);
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
                        eim.warpEventTeam(mapid + 100);
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