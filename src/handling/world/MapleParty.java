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
package handling.world;

import client.MapleCharacter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MapleParty {

    private static final long serialVersionUID = 9179541993413738569L;
    //private MapleCharacter player;
    private List<MaplePartyCharacter> members = new LinkedList<MaplePartyCharacter>();
    private List<MaplePartyCharacter> pqMembers = null;
    private int id, expeditionLink = -1;
    private boolean disbanded = false;
    private int leaderId;

    public MapleParty(int id, MaplePartyCharacter chrfor) {
        this.leaderId = chrfor.getId();
        this.members.add(chrfor);
        this.id = id;
    }

    public MapleParty(int id, MaplePartyCharacter chrfor, int expeditionLink) {
        this.leaderId = chrfor.getId();
        this.members.add(chrfor);
        this.id = id;
        this.expeditionLink = expeditionLink;
    }

    public boolean containsMembers(MaplePartyCharacter member) {
        return members.contains(member);
    }

    public void addMember(MaplePartyCharacter member) {
        members.add(member);
    }

    public void removeMember(MaplePartyCharacter member) {
        members.remove(member);
    }

    public void updateMember(MaplePartyCharacter member) {
        for (int i = 0; i < members.size(); i++) {
            MaplePartyCharacter chr = members.get(i);
            if (chr.equals(member)) {
                members.set(i, member);
            }
        }
    }

    public MaplePartyCharacter getMemberById(int id) {
        for (MaplePartyCharacter chr : members) {
            if (chr.getId() == id) {
                return chr;
            }
        }
        return null;
    }

    public MaplePartyCharacter getMemberByIndex(int index) {
        return members.get(index);
    }

    public Collection<MaplePartyCharacter> getMembers() {
        return new LinkedList<MaplePartyCharacter>(members);
    }

    public Collection<MapleCharacter> getPlayerMembers() {
        Collection<MapleCharacter> players = new LinkedList<MapleCharacter>();
        for (MaplePartyCharacter chr : members) {
            players.add(chr.getPlayer());
        }
        return players;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MaplePartyCharacter getLeader() {
        for (MaplePartyCharacter mpc : members) {
            if (mpc.getId() == leaderId) {
                return mpc;
            }
        }

        return null;
    }

    public void setLeader(MaplePartyCharacter nLeader) {
        this.leaderId = nLeader.getId();
    }

    public int getLeaderId() {
        return leaderId;
    }

    public int getExpeditionId() {
        return expeditionLink;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MapleParty other = (MapleParty) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    public boolean isDisbanded() {
        return disbanded;
    }

    public void disband() {
        this.disbanded = true;
    }

    public Collection<MaplePartyCharacter> getEligibleMembers() {
        return pqMembers;
    }

    public void setEligibleMembers(List<MaplePartyCharacter> eliParty) {
        pqMembers = eliParty;
    }

    public Collection<MaplePartyCharacter> getEligibleParty(int lvlmin) {
        MapleCharacter leader = getLeader().getPlayer();
        for (MaplePartyCharacter chr : getMembers()) {
            if (chr.isOnline()) {
                MapleCharacter player = chr.getPlayer();
                if (player.isAlive()) {
                    if (player.getEventInstance() == null) {
                        if (player.getTotalLevel() >= lvlmin) {
                            pqMembers.add(chr);
                        } else {
                            leader.dropMessage("Party Member: " + player.getName() + " does not meet minimum level requirement.");
                        }
                    } else {
                        leader.dropMessage("Party Member: " + player.getName() + " is currently in an instance.");
                    }
                } else {
                    leader.dropMessage("Party Member: " + player.getName() + " is currently dead.");
                }
            } else {
                leader.dropMessage("Party Member: " + chr.getName() + " is currently not online.");
            }
        }
        return pqMembers;
    }

}
