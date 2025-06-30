/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package server;

import java.awt.Point;

import client.MapleClient;
import handling.channel.ChannelServer;
import scripting.NPCScriptManager;
import scripting.PortalScriptManager;
import server.maps.MapleMap;
import tools.packet.CWvsContext;

public class MaplePortal {

    public static final int MAP_PORTAL = 2;
    public static final int DOOR_PORTAL = 6;

    private String name, target, scriptName;
    private Point position;
    private int targetmap, type, id;
    private boolean portalState = true;

    public MaplePortal(final int type) {
        this.type = type;
    }

    public final int getId() {
        return id;
    }

    public final void setId(int id) {
        this.id = id;
    }

    public final String getPortalName() {
        return name;
    }

    public final Point getPosition() {
        return position;
    }

    public final String getTarget() {
        return target;
    }

    public final int getTargetMapId() {
        return targetmap;
    }

    public final int getType() {
        return type;
    }

    public final String getScriptName() {
        return scriptName;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    public final void setPosition(final Point position) {
        this.position = position;
    }

    public final void setTarget(final String target) {
        this.target = target;
    }

    public final void setTargetMapId(final int targetmapid) {
        this.targetmap = targetmapid;
    }

    public final void setScriptName(final String scriptName) {
        this.scriptName = scriptName;
    }

    public final void enterPortal(final MapleClient c) {
        if (c == null) {
            return;
        }
        c.getPlayer().getShop();
        boolean changed = false;
        if (!c.getPlayer().hasBlockedInventory() && getPortalState()) {
            if (getScriptName() != null) {
                c.getPlayer().checkFollow();
                changed = PortalScriptManager.getInstance().executePortalScript(this, c);
                //System.out.println("test " + changed);
                if (!changed) {
                    if (c.getPlayer().getEventInstance() != null) {
                        if (getTargetMapId() != 999999999) {
                            c.getPlayer().getEventInstance().changedMap(c.getPlayer(), c.getPlayer().getEventInstance().getMapInstance(getTargetMapId()).getId());
                        } else {
                            c.getPlayer().getEventInstance().exitPlayer(c.getPlayer());
                        }
                        changed = true;
                    }
                }
            } else if (getTargetMapId() != 999999999) {
                int mapid = getTargetMapId();
                if (c.getPlayer().getEventInstance() != null) {
                    if (!c.getPlayer().getMap().getPQlock()) {
                        MapleMap to = c.getChannelServer().getMapFactory().getMap(c.getPlayer().getEventInstance().getMapInstance(getTargetMapId()).getId());
                        if (c.getPlayer().getEventInstance() != null) {
                            c.getPlayer().getEventInstance().changedMap(c.getPlayer(), to.getId());
                        }
                        MaplePortal pto = to.getPortal(getTarget());
                        if (pto == null) {// fallback for missing portals - no real life case anymore - interesting for not implemented areas
                            
                            pto = to.getPortal(0);
                        }
                        c.getPlayer().changeMap(to, pto); //late resolving makes this harder but prevents us from loading the whole world at once

                        changed = true;
                    }
                } else {
                    MapleMap to = c.getChannelServer().getMapFactory().getMap(mapid);
                    MaplePortal pto = to.getPortal(getTarget());
                    if (pto == null) {// fallback for missing portals - no real life case anymore - interesting for not implemented areas
                        pto = to.getPortal(0);
                    }
                    c.getPlayer().changeMap(to, pto); //late resolving makes this harder but prevents us from loading the whole world at once
                    changed = true;
                }
            }
        }
        if (!changed) {
            c.announce(CWvsContext.enableActions());
        }
    }

    public boolean getPortalState() {
        return portalState;
    }

    public void setPortalState(boolean ps) {
        this.portalState = ps;
    }
}
