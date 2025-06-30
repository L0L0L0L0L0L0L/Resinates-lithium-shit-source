package client.maplepal;

import client.MapleCharacter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdbi.v3.core.statement.PreparedBatch;

import client.maplepal.PalTemplateProvider.PalTemplate;
import database.Jdbi;
import java.util.function.Predicate;

public class MaplePalStorage {

    private int chrId;
    private final Map<Long, MaplePal> storedPals = new HashMap<>();
    private List<MaplePal> activePals = new ArrayList<>();
    private final Map<Long, MaplePal> hatchingEggs = new HashMap<>();
    private final Map<Long, MaplePal> battlers = new HashMap<>();

    public MaplePalStorage(int charId) {
        this.chrId = charId;
    }

    public synchronized Collection<MaplePal> getStoredPals() {
        return Collections.unmodifiableCollection(storedPals.values());
    }

    public synchronized List<MaplePal> getStoragePals() {
        List<MaplePal> pals = new ArrayList<>();
        for (MaplePal p : getStoredPals()) {
            pals.add(p);
        }
        return Collections.unmodifiableList(pals);
    }

    public synchronized int getPalCount() {
        return storedPals.size() + activePals.size() + hatchingEggs.size();
    }

    public synchronized List<MaplePal> getActivePals() {
        return Collections.unmodifiableList(activePals);
    }

    public List<MaplePal> getPals(Boolean gender) {
        List<MaplePal> eggs = new ArrayList<>();
        int s = gender ? 1 : 0;
        for (MaplePal egg : getStoredPals()) {
            if (egg.templateId > 0 && egg.gender == s) {
                eggs.add(egg);
            }
        }
        return Collections.unmodifiableList(eggs);
    }

    public synchronized Collection<MaplePal> getHatchingEggs() {
        return Collections.unmodifiableCollection(hatchingEggs.values());
    }

    public synchronized Collection<MaplePal> getEggs() {
        Map<Long, MaplePal> eggs = new HashMap<>();
        for (MaplePal egg : getStoredPals()) {
            if (egg.templateId < 0) {
                eggs.put(egg.id, egg);
            }
        }
        return Collections.unmodifiableCollection(eggs.values());
    }

    public synchronized MaplePal getPal(long id) {
        MaplePal ret = storedPals.get(id);
        if (ret == null) {
            ret = getActivePals().stream().filter(p -> p.id == id).findAny().orElse(null);
        }
        if (ret == null) {
            ret = hatchingEggs.get(id);
        }
        return ret;
    }

    public int getAvgLevel() {
        int level = 0;
        if (!getActivePals().isEmpty()) {
            int temp = 0;
            for (MaplePal pal : getActivePals()) {
                temp += pal.getLevel();
            }
            level = (int) Math.floor(temp / getActivePals().size());
        }
        return level;
    }

    public int getHighLevel() {
        int level = 0;
        if (!getActivePals().isEmpty()) {
            int temp = 0;
            for (MaplePal pal : getActivePals()) {
                if (pal.getLevel() > level) {
                    level = pal.getLevel();
                }
            }
        }
        return level;
    }

    public synchronized boolean moveToActive(long id) {
        if (activePals.size() >= 6) {
            return false;
        }
        MaplePal pal = getPal(id);
        if (pal == null || pal.isEgg()) {
            return false;
        }
        try {
            Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET pos = 'ACTIVE' WHERE id = ?").bind(0, id).execute());
            if (storedPals.remove(id) != null) {
                activePals.add(pal);
                saveActivePal(pal, false);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized boolean moveActiveToStorage(long id) {
        Iterator<MaplePal> iter = activePals.iterator();
        while (iter.hasNext()) {
            MaplePal cur = iter.next();
            if (cur.id == id) {
                try {
                    Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET pos = 'STORAGE' WHERE id = ?").bind(0, id).execute());
                    iter.remove();
                    storedPals.put(id, cur);
                    saveActivePal(cur, true);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        return false;
    }

    public synchronized boolean removeAll(List<Long> ids) {
        for (long id : ids) {
            if (getPal(id) == null) {
                return false;
            }
        }
        activePals.removeIf(p -> ids.contains(p.id));
        for (long id : ids) {
            storedPals.remove(id);
            hatchingEggs.remove(id);
            battlers.remove(id);
        }
        Jdbi.j.useHandle(h -> {
            PreparedBatch batch = h.prepareBatch("DELETE FROM player_pals WHERE id = ?");
            for (long id : ids) {
                batch.bind(0, id).add();
            }
            batch.execute();
        });
        return true;
    }

    public synchronized MaplePal getBattlePal(long id) {
        return battlers.get(id);
    }

    public synchronized Collection<MaplePal> getBattlePals() {
        return Collections.unmodifiableCollection(battlers.values());
    }

    public synchronized void addBattler(MaplePal pal, int cid) {
        battlers.put(pal.id, pal);
        Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET battle = ? where id = ?").bind(0, pal.battle).bind(1, pal.id).execute());
    }

    public synchronized void removeBattler(MaplePal pal) {
        battlers.remove(pal.id);
        Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET battle = 0 where id = ?").bind(0, pal.id).execute());
    }

    public void sendPal(MapleCharacter owner, MapleCharacter newOwner, MaplePal pal) {
        if (storedPals.containsKey(pal.id) && newOwner.canHoldPal()) {
            remove(pal.id);
            newOwner.getPalStorage().add(pal, newOwner.getAccountID());
            System.out.println(owner.getName() + " sent Lv. " + pal.getLevel() + " - " + pal.getName() + " to " + newOwner.getName());
        }
    }

    public synchronized void add(MaplePal pal, int id) {
        this.chrId = id;
        Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET charid = ? where id = ?").bind(0, id).bind(1, pal.id).execute());
        storedPals.put(pal.id, pal);
    }

    private synchronized boolean remove(long id) {
        return activePals.removeIf(p -> p.id == id) || storedPals.remove(id) != null;
    }

    public synchronized boolean beginHatching(MapleCharacter chr, long id) {
        if (id == 0 || hatchingEggs.containsKey(id)) {
            return false;
        }
        if (hatchingEggs.size() >= chr.getHatchSlots()) {
            chr.dropMessage(1, "All your hatching slots are currently full");
            return false;
        }
        if (!chr.canHoldPal()) {
            chr.dropMessage(1, "You must free up space in your Pal Storage to hatch more eggs");
            return false;
        }
        MaplePal egg = getPal(id);
        if (egg != null && egg.isEgg()) {
            if (remove(id)) {
                hatchingEggs.put(id, egg);
                egg.hatchingStartTime = System.currentTimeMillis();
                egg.born = System.currentTimeMillis();
                egg.hatchingDuration = PalTemplateProvider.getTemplate(-egg.templateId).hatchDuration();
                Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET pos = 'HATCHING', hatch_start_time = CURRENT_TIMESTAMP(), born = ? WHERE id = ?").bind(0, System.currentTimeMillis()).bind(1, id).execute());
                return true;
            }
        }
        return false;
    }

    public synchronized boolean cancelHatching(long id) {
        MaplePal egg = hatchingEggs.remove(id);
        if (egg != null) {
            storedPals.put(id, egg);
            egg.born = 0;
            Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET pos = 'STORAGE', hatch_start_time = NULL, born = 0 WHERE id = ?").bind(0, id).execute());
            return true;
        }
        return false;
    }

    public synchronized boolean tryHatch(long id) {
        MaplePal pal = hatchingEggs.remove(id); //Eggs can only be hatched through UI
        if (pal != null) {
            pal.hatch();
            storedPals.put(id, pal);
            Jdbi.j.useTransaction(h -> {
                h.createUpdate("UPDATE player_pals SET pos = 'STORAGE', hatch_start_time = NULL, born = 0 WHERE id = ?").bind(0, pal.id).execute();
                pal.save();
            });
            return true;
        }
        return false;
    }

    public synchronized MaplePal createNewEgg(Predicate<MaplePal> statSetter) {
        MaplePal newEgg = new MaplePal().createNewEgg(chrId, statSetter);
        storedPals.put(newEgg.id, newEgg);
        return newEgg;
    }

    public void saveActivePal(MaplePal pal, boolean remove) {
        int slot = 1;
        for (MaplePal pals : getActivePals()) {
            final int sid = slot++;
            Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET battle = ? where id = ?").bind(0, sid).bind(1, pals.getId()).execute());
        }
        if (remove) {
            Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET battle = 0 where id = ?").bind(0, pal.getId()).execute());
        }
    }

    public void saveActivePals() {
        int slot = 1;
        for (MaplePal pal : getActivePals()) {
            final int sid = slot++;
            Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET battle = ? where id = ?").bind(0, sid).bind(1, pal.getId()).execute());
        }
        for (MaplePal pal : getStoredPals()) {
            Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET battle = 0 where id = ?").bind(0, pal.getId()).execute());
        }
    }

    public void savePals() {
        int slot = 0;
        for (MaplePal pal : getActivePals()) {
            final int sid = slot++;
            Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET battle = ? where id = ?").bind(0, sid).bind(1, pal.getId()).execute());
            pal.save();
        }
        for (MaplePal pal : getStoredPals()) {
            Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET battle = 0 where id = ?").bind(0, pal.getId()).execute());
            pal.save();
        }
        for (MaplePal pal : getHatchingEggs()) {
            pal.save();
        }
    }

    public synchronized void load(Connection con) {
        List<MaplePal> tempPals = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM player_pals WHERE charid = ?")) {
            ps.setInt(1, chrId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MaplePal pal = new MaplePal();
                    pal.id = rs.getLong("id");
                    pal.templateId = rs.getShort("template_id");
                    String storage = rs.getString("pos");
                    pal.name = rs.getString("name");
                    if (pal.name == null) {
                        pal.name = "";
                    }
                    pal.tier = rs.getByte("tier");
                    pal.rank = rs.getByte("rank");
                    pal.level = (short) rs.getInt("level");
                    pal.gender = (byte) rs.getInt("gender");
                    pal.exp = rs.getLong("exp");
                    pal.stats[0] = rs.getInt("hp");
                    pal.stats[1] = rs.getInt("str");
                    pal.stats[2] = rs.getInt("dex");
                    pal.stats[3] = rs.getInt("int");
                    pal.stats[4] = rs.getInt("luk");
                    pal.stats[5] = rs.getInt("atk");
                    pal.stats[6] = rs.getInt("matk");
                    pal.stats[7] = rs.getInt("def");
                    pal.stats[8] = rs.getInt("mdef");
                    pal.IVs[0] = rs.getShort("iv_hp");
                    pal.IVs[1] = rs.getShort("iv_str");
                    pal.IVs[2] = rs.getShort("iv_dex");
                    pal.IVs[3] = rs.getShort("iv_int");
                    pal.IVs[4] = rs.getShort("iv_luk");
                    pal.IVs[5] = rs.getShort("iv_atk");
                    pal.IVs[6] = rs.getShort("iv_matk");
                    pal.IVs[7] = rs.getShort("iv_def");
                    pal.IVs[8] = rs.getShort("iv_mdef");
                    pal.abilities[0] = rs.getShort("ability_1");
                    pal.abilities[1] = rs.getShort("ability_2");
                    pal.abilities[2] = rs.getShort("ability_3");
                    pal.abilities[3] = rs.getShort("ability_4");
                    Timestamp hatchTs = rs.getTimestamp("hatch_start_time");
                    pal.hatchingStartTime = hatchTs != null ? hatchTs.getTime() : 0;
                    PalTemplate template = PalTemplateProvider.getTemplate(pal.isEgg() ? -pal.templateId : pal.templateId);
                    pal.hatchingDuration = pal.isEgg() ? template.hatchDuration() : 0;
                    byte e = (byte) rs.getInt("element");
                    if (e == -1) {
                        e = (byte) template.element();
                    }
                    pal.element = e;
                    pal.acc_1 = rs.getInt("acc_1");
                    pal.acc_2 = rs.getInt("acc_2");
                    pal.acc_3 = rs.getInt("acc_3");
                    pal.acc_4 = rs.getInt("acc_4");
                    pal.upgrades = rs.getInt("upgrades");
                    pal.speed = rs.getInt("speed");
                    pal.lastBreedTime = rs.getTimestamp("last_breed_time").getTime();
                    if (pal.acc_1 != 0) {
                        pal.slot_1 = PalTemplateProvider.getEquip(pal.acc_1);
                    }
                    if (pal.acc_2 != 0) {
                        pal.slot_2 = PalTemplateProvider.getEquip(pal.acc_2);
                    }
                    if (pal.acc_3 != 0) {
                        pal.slot_3 = PalTemplateProvider.getEquip(pal.acc_3);
                    }
                    if (pal.acc_4 != 0) {
                        pal.slot_4 = PalTemplateProvider.getEquip(pal.acc_4);
                    }

                    if (pal.abilities[0] != 0) {
                        pal.a_1 = PalTemplateProvider.getAbility(pal.abilities[0]);
                    }
                    if (pal.abilities[1] != 0) {
                        pal.a_2 = PalTemplateProvider.getAbility(pal.abilities[1]);
                    }
                    if (pal.abilities[2] != 0) {
                        pal.a_3 = PalTemplateProvider.getAbility(pal.abilities[2]);
                    }
                    if (pal.abilities[3] != 0) {
                        pal.a_4 = PalTemplateProvider.getAbility(pal.abilities[3]);
                    }
                    pal.born = rs.getLong("born");
                    pal.skill = rs.getInt("skill");
                    switch (storage) {
                        case "ACTIVE" ->
                            tempPals.add(pal);
                        case "STORAGE" ->
                            storedPals.put(pal.id, pal);
                        case "HATCHING" ->
                            hatchingEggs.put(pal.id, pal);
                        default ->
                            throw new IllegalStateException("Read invalid storage type for Pal during loading. (ID: " + pal.id + ")");
                    }
                    pal.battle = rs.getInt("battle");
                    pal.setupHP();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        savePals();
        Collections.sort(tempPals, (t, o) -> Integer.compare(t.battle, o.battle));
        activePals = tempPals;
    }

}
