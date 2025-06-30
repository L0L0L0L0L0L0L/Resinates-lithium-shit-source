
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
package server.maps;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import java.lang.ref.WeakReference;
import server.MaplePortal;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.PartyPacket;

public class MapleDoor extends MapleMapObject {

    private WeakReference<MapleCharacter> owner;
    private MapleMap town;
    private MaplePortal townPortal;
    private MapleMap target;
    private int skillId, ownerId;
    private Point targetPosition;

    public MapleDoor(final MapleCharacter owner, final Point targetPosition, final int skillId) {
        super();
        this.owner = new WeakReference<>(owner);
        this.ownerId = owner.getId();
        this.target = owner.getMap();
        this.targetPosition = targetPosition;
        setPosition(this.targetPosition);
        this.town = this.target.getReturnMap();
        this.townPortal = getFreePortal();
        this.skillId = skillId;
    }

    public MapleDoor(final MapleDoor origDoor) {
        super();
        this.owner = new WeakReference<>(origDoor.owner.get());
        this.town = origDoor.town;
        this.townPortal = origDoor.townPortal;
        this.target = origDoor.target;
        this.targetPosition = new Point(origDoor.targetPosition);
        this.skillId = origDoor.skillId;
        this.ownerId = origDoor.ownerId;
        setPosition(townPortal.getPosition());
    }

    public final int getSkill() {
        return skillId;
    }

    public final int getOwnerId() {
        return ownerId;
    }

    public List<MaplePortal> getTownPortals() {
        final List<MaplePortal> townPortals = new ArrayList<MaplePortal>(town.getPortals());
        return townPortals;
    }

    public List<MaplePortal> getFreePortals() {
        final List<MaplePortal> freePortals = new ArrayList<MaplePortal>();

        for (final MaplePortal port : getTownPortals()) {
            if (port.getType() == 6) {
                freePortals.add(port);
            }
        }
        for (final MapleDoor door : town.getAllDoors()) {
            /// hmm
            if (door.getOwner() != null && door.getOwner().getRaid() != null && getOwner() != null && getOwner().getRaid() != null && getOwner().getRaid().getId() == door.getOwner().getRaid().getId()) {
                return null; //one per
            }
            if (door.getOwner() != null && door.getOwner().getParty() != null && getOwner() != null && getOwner().getParty() != null && getOwner().getParty().getId() == door.getOwner().getParty().getId()) {
                return null; //one per
            }
            freePortals.remove(door.getTownPortal());
        }
        return freePortals;
    }

    private MaplePortal getFreePortal() {
        final List<MaplePortal> freePortals = new ArrayList<MaplePortal>();

        for (final MaplePortal port : getTownPortals()) {
            if (port.getType() == 6) {
                freePortals.add(port);
            }
        }
        Collections.sort(freePortals, new Comparator<MaplePortal>() {

            @Override
            public final int compare(final MaplePortal o1, final MaplePortal o2) {
                if (o1.getId() < o2.getId()) {
                    return -1;
                } else if (o1.getId() == o2.getId()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (final MapleDoor door : town.getAllDoors()) {
            /// hmm
            if (door.getOwner() != null && door.getOwner().getRaid() != null && getOwner() != null && getOwner().getRaid() != null && getOwner().getRaid().getId() == door.getOwner().getRaid().getId()) {
                return null; //one per
            }
            if (door.getOwner() != null && door.getOwner().getParty() != null && getOwner() != null && getOwner().getParty() != null && getOwner().getParty().getId() == door.getOwner().getParty().getId()) {
                return null; //one per
            }
            freePortals.remove(door.getTownPortal());
        }
        if (freePortals.size() <= 0) {
            return null;
        }
        return freePortals.iterator().next();
    }

    @Override
    public final void sendSpawnData(final MapleClient client) {
        if (getOwner() == null || target == null || client.getPlayer() == null) {
            return;
        }
        if (getOwner() != null) {
            client.announce(CField.spawnDoor(getOwnerId(), target.getId() == client.getPlayer().getMapId() ? targetPosition : townPortal.getPosition(), true)); //spawnDoor always has same position.
            if (client.getPlayer().getParty() == null) {
                if (target.getId() == client.getPlayer().getMapId() || getOwnerId() == client.getPlayer().getId()) {
                    client.announce(CWvsContext.spawnPortal(town.getId(), target.getId(), skillId, target.getId() == client.getPlayer().getMapId() ? targetPosition : townPortal.getPosition()));
                }
            } else {
                if (getOwner().getParty() != null && client.getPlayer().getParty() != null && getOwner().getParty().getId() == client.getPlayer().getParty().getId()) {
                    //client.announce(PartyPacket.partyPortal(town.getId(), target.getId(), skillId, targetPosition, true));
                    client.announce(PartyPacket.partyPortal(town.getId(), target.getId(), skillId, townPortal.getPosition(), true));
                }
            }
            client.announce(CWvsContext.spawnPortal(town.getId(), target.getId(), skillId, target.getId() == client.getPlayer().getMapId() ? targetPosition : townPortal.getPosition()));

            /*
             if (getOwner().getParty() != null && client.getPlayer().getParty() != null && getOwner().getParty().getId() == client.getPlayer().getParty().getId()) {
            
             if (getOwner() != null && getOwner().getParty() != null && client.getPlayer().getParty() != null && (getOwnerId() == client.getPlayer().getId() || getOwner().getParty().getId() == client.getPlayer().getParty().getId())) {
             if (target.getId() == client.getPlayer().getMapId()) {
             client.announce(PartyPacket.partyPortal(town.getId(), target.getId(), skillId, targetPosition, true));
             System.out.println("Party door pos: " + targetPosition);
             } else {
             client.announce(PartyPacket.partyPortal(town.getId(), target.getId(), skillId, townPortal.getPosition(), true));
             System.out.println("Party Town door pos: " + townPortal.getPosition());
             }
             } else {
             client.announce(CWvsContext.spawnPortal(town.getId(), target.getId(), skillId, target.getId() == client.getPlayer().getMapId() ? targetPosition : townPortal.getPosition()));
             System.out.println("door pos: " + targetPosition);
             }
             }
             */
        }
    }

    @Override
    public final void sendDestroyData(final MapleClient client) {
        if (client.getPlayer() == null || getOwner() == null || target == null) {
            return;
        }
        if (target.getId() == client.getPlayer().getMapId() || getOwnerId() == client.getPlayer().getId() || (getOwner() != null && getOwner().getParty() != null && client.getPlayer().getParty() != null && getOwner().getParty().getId() == client.getPlayer().getParty().getId())) {
            client.announce(CField.removeDoor(getOwnerId(), false));
            if (getOwner() != null && getOwner().getParty() != null && client.getPlayer().getParty() != null && (getOwnerId() == client.getPlayer().getId() || getOwner().getParty().getId() == client.getPlayer().getParty().getId())) {
                //client.announce(PartyPacket.partyPortal(999999999, 999999999, 0, new Point(-1, -1), false));
                client.announce(PartyPacket.partyPortal(999999999, 999999999, 0, new Point(-1, -1), false));
            } else {
                client.announce(CWvsContext.spawnPortal(999999999, 999999999, 0, null));
            }
        }
    }

    public final void warp(final MapleCharacter chr, final boolean toTown) {
        if (getOwner() != null && getOwner().getParty() != null && chr.getParty() != null && getOwner().getParty().getId() == chr.getParty().getId()) {
            if (!toTown) {
                chr.changeMap(target, target.findClosestPortal(targetPosition));
            } else {
                chr.changeMap(town, townPortal);
            }
        } else if (getOwner() != null && getOwner().getRaid() != null && chr.getRaid() != null && getOwner().getRaid().getId() == chr.getRaid().getId()) {
            if (!toTown) {
                chr.changeMap(target, target.findClosestPortal(targetPosition));
            } else {
                chr.changeMap(town, townPortal);
            }
        } else if (chr.getId() == getOwnerId()) {
            if (!toTown) {
                chr.changeMap(target, target.findClosestPortal(targetPosition));
            } else {
                chr.changeMap(town, townPortal);
            }
        } else {
            chr.getClient().announce(CWvsContext.enableActions());
        }
    }

    public void updatePartyDoor(final MapleClient client, MapleDoor door) {
        client.announce(PartyPacket.partyPortal(door.getTown().getId(), door.getTarget().getId(), door.getSkill(), door.getTarget().getId() == client.getPlayer().getMapId() ? door.getTargetPosition() : door.getTownPortal().getPosition(), true));
    }

    public final MapleCharacter getOwner() {
        return owner.get();
    }

    public final MapleMap getTown() {
        return town;
    }

    public final MaplePortal getTownPortal() {
        return townPortal;
    }

    public final MapleMap getTarget() {
        return target;
    }

    public final Point getTargetPosition() {
        return targetPosition;
    }

    @Override
    public final MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }
}
