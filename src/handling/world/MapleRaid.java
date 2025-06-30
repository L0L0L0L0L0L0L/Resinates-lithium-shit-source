package handling.world;

import client.MapleCharacter;
import constants.GameConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import server.Randomizer;
import server.maps.MapleDoor;
import server.quest.MapleQuest;
import tools.Pair;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class MapleRaid {

    private int id;
    private boolean invite;
    private MapleCharacter leader;
    private final List<MapleCharacter> members = new LinkedList<>();
    private List<MapleCharacter> raidmembers = null;
    private static AtomicInteger runningraidId = new AtomicInteger(1000000000);
    private Map<Integer, Integer> histMembers = new HashMap<>();
    private int nextEntry = 1000000;
    private Map<Integer, MapleDoor> doors = new HashMap<>();
    public boolean door = false;

    public MapleRaid(MapleCharacter leader, int id) {
        this.leader = leader;
        this.id = id;
    }

    public boolean containsMembers(MapleCharacter member) {
        return members.contains(member);
    }

    public static int newId() {
        return runningraidId.getAndIncrement();
    }

    public static void createRaid(MapleCharacter leader) {
        if (leader.getRaid() == null || leader.getParty() == null) {
            if (!leader.getRaidStatus()) {
                int raidid = MapleRaid.newId();
                leader.setRaid(new MapleRaid(leader, raidid));
                leader.getRaid().members.add(leader);
                leader.getRaid().histMembers.put(leader.getId(), leader.getRaid().nextEntry);
                leader.getRaid().nextEntry++;
                leader.setRaidLeader(true);
                leader.getRaid().raidMessage("[Raid] " + leader.getName() + " has created a raid");
                World.Raid.setRaid(raidid, leader.getRaid());
            }
        }

    }

    public void invite(MapleCharacter leader, MapleCharacter member) {
        if (member != null) {
            if (member.getParty() == null && member.getRaid() == null) {
                if (leader.getRaid().getLeader() == leader) {
                    leader.dropMessage(5, "Invited " + member.getName() + " to join raid." + leader.getRaid().getLeader().getId());
                    //member.getClient().announce(CWvsContext.followRequest(leader.getObjectId()));
                    member.getClient().announce(CWvsContext.PartyPacket.raidInvite(leader));
                } else {
                    leader.dropMessage(5, "Only Raid Leader can invite other players.");
                }
            } else {
                leader.dropMessage(5, member.getName() + " is already in a party or raid.");
            }
        }
    }

    public void addMember(MapleCharacter member) {
        if (!containsMembers(member) || member.getRaid() == null) {
            member.setRaid(getLeader().getRaid());
            histMembers.put(member.getId(), nextEntry);
            nextEntry++;
            members.add(member);
            raidMessage("[Raid] " + member.getName() + " has joined the raid");
            member.cancelMagicDoor();
        }
    }

    public void removeMember(final MapleCharacter pleader, final MapleCharacter member) {
        if ((containsMembers(member) && member != leader) && pleader == leader) {
            if (pleader != member) {
                member.dropMessage(5, "You have been expelled from the raid.");
                member.setRaid(null);
                if (member.getEventInstance() != null) {
                    member.getEventInstance().exitPlayer(member);
                }
                histMembers.remove(member.getId());
                members.remove(member);
                raidMessage("[Raid] " + member.getName() + " has been expelled from the raid");
                member.cancelMagicDoor();
            }
        }
    }

    public void leaveRaid(MapleCharacter member) {
        member.setRaid(null);
        histMembers.remove(member.getId());
        members.remove(member);
        member.dropMessage(5, "[Raid] you have left the raid");
        raidMessage("[Raid] " + member.getName() + " has left the raid");
        member.cancelMagicDoor();
    }

    public void setLeader(MapleCharacter member) {
        if (containsMembers(member) && member != leader) {
            leader = member;
            raidMessage("[Raid] " + member.getName() + " is now the leader of the raid");
        }
    }

    public void changeLeader(MapleCharacter pleader, MapleCharacter member) {
        if ((containsMembers(member) && member != leader) && pleader == leader) {
            if (pleader != member) {
                leader = member;
                raidMessage("[Raid] " + member.getName() + " is now the leader of the raid");
            }
        }
    }

    public MapleCharacter getLeader() {
        return leader;
    }

    public boolean getLeader(MapleCharacter chr) {
        return chr == leader;
    }

    public List<MapleCharacter> getMembers() {
        return members;
    }

    public void disbandRaid() {
        for (MapleCharacter chr : members) {
            chr.setRaid(null);
            chr.dropMessage(5, "[Raid] Raid has been disbanded");
            chr.cancelMagicDoor();
        }
        histMembers.clear();
        members.clear();
    }

    public void disbandRaidByLeader(MapleCharacter member) {
        if (member == leader) {
            for (MapleCharacter chr : members) {
                chr.setRaid(null);
                chr.dropMessage(5, "[Raid] Raid has been disbanded");
                chr.cancelMagicDoor();
            }
            histMembers.clear();
            members.clear();
            World.Raid.removeRaid(id);
        }
    }

    public void raidMessage(String msg) {
        for (MapleCharacter chr : members) {
            chr.dropMessage(5, msg);
        }
    }

    public Collection<MapleCharacter> getEligibleRaidMembers() {
        return Collections.unmodifiableList(raidmembers);
    }

    public void setEligibleMembers(List<MapleCharacter> eliParty) {
        raidmembers = eliParty;
    }

    public MapleCharacter getRandomPlayer() {
        List<MapleCharacter> players = new ArrayList<>(members);
        Collections.shuffle(players);
        MapleCharacter mc = players.get(Randomizer.nextInt());
        return mc;
    }

    public MapleCharacter getRandomPlayer(MapleCharacter chr) {
        List<MapleCharacter> players = new ArrayList<>(members);
        raidMessage("[Raid] " + chr.getName() + " has logged off or disconnected.");
        players.remove(chr);
        Collections.shuffle(players);
        MapleCharacter mc = players.get(Randomizer.nextInt());
        raidMessage("[Raid] " + mc.getName() + " is now the leader of the raid");
        return mc;
    }

    public void assignNewLeader() {
        MapleCharacter newLeadr = null;
        for (MapleCharacter mpc : members) {
            if (mpc != leader) {
                newLeadr = mpc;
                setLeader(mpc);
                return;
            }
        }
        if (newLeadr == null) {
            histMembers.clear();
            members.clear();
        }
    }

    public List<Integer> getMembersSortedByHistory() {
        List<Map.Entry<Integer, Integer>> histList;

        histList = new LinkedList<>(histMembers.entrySet());

        Collections.sort(histList, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        List<Integer> histSort = new LinkedList<>();
        for (Map.Entry<Integer, Integer> e : histList) {
            histSort.add(e.getKey());
        }

        return histSort;
    }

    public byte getRaidDoor(int cid) {
        List<Integer> histList = getMembersSortedByHistory();
        byte slot = 0;
        for (Integer e : histList) {
            if (e == cid) {
                break;
            }
            slot++;
        }

        return slot;
    }

    public void addDoor(Integer owner, MapleDoor door) {
        doors.put(owner, door);
    }

    public void removeDoor(Integer owner) {
        doors.remove(owner);
    }

    public Map<Integer, MapleDoor> getDoors() {
        return Collections.unmodifiableMap(doors);
    }

    public int getId() {
        return id;
    }

    public MapleCharacter getMemberById(int id) {
        for (MapleCharacter chr : members) {
            if (chr.getId() == id) {
                return chr;
            }
        }
        return null;
    }

    public void updateMember(MapleCharacter member) {
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getId() == member.getId()) {
                members.set(i, member);
            }
        }
    }
}
