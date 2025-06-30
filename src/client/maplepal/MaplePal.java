package client.maplepal;

import client.MapleCharacter;
import java.util.function.Consumer;

import database.Jdbi;
import java.util.function.Predicate;
import server.Randomizer;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packet.CField;

public class MaplePal {

    protected long id;
    public short templateId;
    public byte tier;
    public byte rank;
    public byte element;
    public byte gender;
    public short level;
    public int hp, maxhp; //This is only used to store current value during battles, do not save
    public int acc_1 = 0, acc_2 = 0, acc_3 = 0, acc_4 = 0, upgrades = 100, speed = 100, damage = 100, defense = 100, levels = 0, battle = 0, skill = 0, attacks = 1;
    public int hp_cap, stat_cap, damage_cap, level_cap;
    public int[] stats = new int[9];
    public short[] IVs = new short[9];
    public double[] bonus_stats = new double[9];
    public short[] abilities = new short[4];
    public String name = "";
    protected long hatchingStartTime;
    protected long lastBreedTime;
    protected int hatchingDuration;
    public boolean active = false, levelup = false;
    public MaplePalEquip slot_1 = null, slot_2 = null, slot_3 = null, slot_4 = null;
    public MaplePalAbility a_1 = null, a_2 = null, a_3 = null, a_4 = null;
    public long born, exp;

    public void encode(MaplePacketLittleEndianWriter mplew) {
        encodeBasic(mplew);
        if (!isEgg()) {
            for (int stat : getStats()) {
                mplew.writeInt(stat);
            }
            //mplew.write(IVs);
            for (short iv : IVs) {
                mplew.write((byte) Randomizer.Max(iv, 250));
            }
            for (short ability : abilities) {
                mplew.writeShort(ability);
            }
            mplew.writeString(templateId <= 0 ? "Pal Egg" : name, 16);
            mplew.writeInt(acc_1);
            mplew.writeInt(acc_2);
            mplew.writeInt(acc_3);
            mplew.writeInt(acc_4);
        } else {
            mplew.writeZeroBytes(69); //Don't send egg data to client
            mplew.writeZeroBytes(16); //Don't send egg data to client
        }
    }

    public void encodeBasic(MaplePacketLittleEndianWriter mplew) {
        mplew.writeLong(id);
        mplew.writeShort(isEgg() ? 0 : templateId); //Don't give client real egg template ID
        if (!isEgg()) {
            int t = PalTemplateProvider.getTemplate(templateId).evo();
            mplew.write(tier);
            mplew.write(t);
            mplew.write(rank);
            mplew.write(element);
            mplew.write(gender);
            mplew.writeShort(level);
        } else {
            mplew.write(0);
            mplew.write(0);
            mplew.write(0);
            mplew.write(element);
            mplew.write(0);
            mplew.writeShort(0);
        }
    }

    public void encodeHatchingInfo(MaplePacketLittleEndianWriter mplew) {
        //mplew.writeInt(0);
        //mplew.writeInt(0);
        mplew.writeInt(hatchingDuration);
        long timeLeft = Randomizer.LongMin((born + hatchingDuration) - System.currentTimeMillis(), 0);
        mplew.writeInt((int) timeLeft);
    }

    public boolean isEgg() {
        return templateId < 0;
    }

    public boolean isNotEgg() {
        return templateId >= 0;
    }

    public String getName() {
        return name;
    }

    public int getModel() {
        return templateId;
    }
    
    public void setModel(short id) {
        templateId = id;
    }
    
    public void setElement(byte type) {
        element = type;
    }
    
    public long getId() {
        return id;
    }

    public boolean canHatch() {
        return born + hatchingDuration < System.currentTimeMillis();
    }

    public boolean canBreed() {
        if (isMega()) {
            return false;
        }
        if (isLegendary()) {
            return lastBreedTime + (1000 * 60 * 60 * 4) < System.currentTimeMillis();
        }
        return lastBreedTime + (1000 * 60 * 60) < System.currentTimeMillis();
    }

    public void setBreedTime() {
        lastBreedTime = System.currentTimeMillis();
    }

    public void hatch() {
        templateId = (short) -templateId;
    }

    public int getIV(int type) {
        return IVs[type];
    }

    public void setStat(int type, int amount) {
        if (type == 6 || type == 7) {
            if (type == 6) {
                stats[7] += amount;
            }
            if (type == 7) {
                stats[6] += amount;
            }
        } else {
            stats[type] += amount;
        }
    }

    public int getStat(int type) {
        return stats[type];
    }

    public void checkExtra() {
        if (acc_1 != 0) {
            slot_1 = PalTemplateProvider.getEquip(acc_1);
        }
        if (acc_2 != 0) {
            slot_2 = PalTemplateProvider.getEquip(acc_2);
        }
        if (acc_3 != 0) {
            slot_3 = PalTemplateProvider.getEquip(acc_3);
        }
        if (acc_4 != 0) {
            slot_4 = PalTemplateProvider.getEquip(acc_4);
        }

        if (abilities[0] != 0) {
            a_1 = PalTemplateProvider.getAbility(abilities[0]);
        }
        if (abilities[1] != 0) {
            a_2 = PalTemplateProvider.getAbility(abilities[1]);
        }
        if (abilities[2] != 0) {
            a_3 = PalTemplateProvider.getAbility(abilities[2]);
        }
        if (abilities[3] != 0) {
            a_4 = PalTemplateProvider.getAbility(abilities[3]);
        }
    }

    public int[] getStats() {
        checkExtra();
        int[] baseStats = new int[9];
        int[] totalStats = new int[9];
        baseStats[0] = (int) (stats[0] * (1 + (IVs[0] * 0.01)) * (100 + (slot_1 != null ? slot_1.getHp() : 0) + (slot_2 != null ? slot_2.getHp() : 0) + (slot_3 != null ? slot_3.getHp() : 0) + (slot_4 != null ? slot_4.getHp() : 0)) * 0.01);
        baseStats[1] = (int) (stats[1] * (1 + (IVs[1] * 0.01)) * (100 + (slot_1 != null ? slot_1.getStr() : 0) + (slot_2 != null ? slot_2.getStr() : 0) + (slot_3 != null ? slot_3.getStr() : 0) + (slot_4 != null ? slot_4.getStr() : 0)) * 0.01);
        baseStats[2] = (int) (stats[2] * (1 + (IVs[2] * 0.01)) * (100 + (slot_1 != null ? slot_1.getDex() : 0) + (slot_2 != null ? slot_2.getDex() : 0) + (slot_3 != null ? slot_3.getDex() : 0) + (slot_4 != null ? slot_4.getDex() : 0)) * 0.01);
        baseStats[3] = (int) (stats[3] * (1 + (IVs[3] * 0.01)) * (100 + (slot_1 != null ? slot_1.getInt() : 0) + (slot_2 != null ? slot_2.getInt() : 0) + (slot_3 != null ? slot_3.getInt() : 0) + (slot_4 != null ? slot_4.getInt() : 0)) * 0.01);
        baseStats[4] = (int) (stats[4] * (1 + (IVs[4] * 0.01)) * (100 + (slot_1 != null ? slot_1.getLuk() : 0) + (slot_2 != null ? slot_2.getLuk() : 0) + (slot_3 != null ? slot_3.getLuk() : 0) + (slot_4 != null ? slot_4.getLuk() : 0)) * 0.01);
        baseStats[5] = (int) (stats[5] * (1 + (IVs[5] * 0.01)) * (100 + (slot_1 != null ? slot_1.getAtk() : 0) + (slot_2 != null ? slot_2.getAtk() : 0) + (slot_3 != null ? slot_3.getAtk() : 0) + (slot_4 != null ? slot_4.getAtk() : 0)) * 0.01);
        baseStats[6] = (int) (stats[6] * (1 + (IVs[6] * 0.01)) * (100 + (slot_1 != null ? slot_1.getMatk() : 0) + (slot_2 != null ? slot_2.getMatk() : 0) + (slot_3 != null ? slot_3.getMatk() : 0) + (slot_4 != null ? slot_4.getMatk() : 0)) * 0.01);
        baseStats[7] = (int) (stats[7] * (1 + (IVs[7] * 0.01)) * (100 + (slot_1 != null ? slot_1.getDef() : 0) + (slot_2 != null ? slot_2.getDef() : 0) + (slot_3 != null ? slot_3.getDef() : 0) + (slot_4 != null ? slot_4.getDef() : 0)) * 0.01);
        baseStats[8] = (int) (stats[8] * (1 + (IVs[8] * 0.01)) * (100 + (slot_1 != null ? slot_1.getMdef() : 0) + (slot_2 != null ? slot_2.getMdef() : 0) + (slot_3 != null ? slot_3.getMdef() : 0) + (slot_4 != null ? slot_4.getMdef() : 0)) * 0.01);

        totalStats[0] = (int) (baseStats[0] * ((100.0 + (a_1 != null ? a_1.getHp() : 0) + (a_2 != null ? a_2.getHp() : 0) + (a_3 != null ? a_3.getHp() : 0) + (a_4 != null ? a_4.getHp() : 0)) * 0.01));
        totalStats[1] = (int) (baseStats[1] * ((100.0 + (a_1 != null ? a_1.getStr() : 0) + (a_2 != null ? a_2.getStr() : 0) + (a_3 != null ? a_3.getStr() : 0) + (a_4 != null ? a_4.getStr() : 0)) * 0.01));
        totalStats[2] = (int) (baseStats[2] * ((100.0 + (a_1 != null ? a_1.getDex() : 0) + (a_2 != null ? a_2.getDex() : 0) + (a_3 != null ? a_3.getDex() : 0) + (a_4 != null ? a_4.getDex() : 0)) * 0.01));
        totalStats[3] = (int) (baseStats[3] * ((100.0 + (a_1 != null ? a_1.getInt() : 0) + (a_2 != null ? a_2.getInt() : 0) + (a_3 != null ? a_3.getInt() : 0) + (a_4 != null ? a_4.getInt() : 0)) * 0.01));
        totalStats[4] = (int) (baseStats[4] * ((100.0 + (a_1 != null ? a_1.getLuk() : 0) + (a_2 != null ? a_2.getLuk() : 0) + (a_3 != null ? a_3.getLuk() : 0) + (a_4 != null ? a_4.getLuk() : 0)) * 0.01));
        totalStats[5] = (int) (baseStats[5] * ((100.0 + (a_1 != null ? a_1.getAtk() : 0) + (a_2 != null ? a_2.getAtk() : 0) + (a_3 != null ? a_3.getAtk() : 0) + (a_4 != null ? a_4.getAtk() : 0)) * 0.01));
        totalStats[6] = (int) (baseStats[6] * ((100.0 + (a_1 != null ? a_1.getMatk() : 0) + (a_2 != null ? a_2.getMatk() : 0) + (a_3 != null ? a_3.getMatk() : 0) + (a_4 != null ? a_4.getMatk() : 0)) * 0.01));
        totalStats[7] = (int) (baseStats[7] * ((100.0 + (a_1 != null ? a_1.getDef() : 0) + (a_2 != null ? a_2.getDef() : 0) + (a_3 != null ? a_3.getDef() : 0) + (a_4 != null ? a_4.getDef() : 0)) * 0.01));
        totalStats[8] = (int) (baseStats[8] * ((100.0 + (a_1 != null ? a_1.getMdef() : 0) + (a_2 != null ? a_2.getMdef() : 0) + (a_3 != null ? a_3.getMdef() : 0) + (a_4 != null ? a_4.getMdef() : 0)) * 0.01));
        for (int j = 0; j <= 8; j++) {
            if (j == 0) {
                totalStats[j] = Randomizer.Max(totalStats[j], level > 100 ? 999999 : 99999);
            } else {
                totalStats[j] = Randomizer.Max(totalStats[j], level > 100 ? 99999 : 9999);
            }
        }
        speed = 100 + (slot_1 != null ? slot_1.getSpeed() : 0) + (slot_2 != null ? slot_2.getSpeed() : 0) + (slot_3 != null ? slot_3.getSpeed() : 0) + (slot_4 != null ? slot_4.getSpeed() : 0);
        damage = 100 + (slot_1 != null ? slot_1.getDamage() : 0) + (slot_2 != null ? slot_2.getDamage() : 0) + (slot_3 != null ? slot_3.getDamage() : 0) + (slot_4 != null ? slot_4.getDamage() : 0);
        defense = 100 + (slot_1 != null ? slot_1.getDefense() : 0) + (slot_2 != null ? slot_2.getDefense() : 0) + (slot_3 != null ? slot_3.getDefense() : 0) + (slot_4 != null ? slot_4.getDefense() : 0);

        speed += (a_1 != null ? a_1.getSpeed() : 0) + (a_2 != null ? a_2.getSpeed() : 0) + (a_3 != null ? a_3.getSpeed() : 0) + (a_4 != null ? a_4.getSpeed() : 0);
        damage += (a_1 != null ? a_1.getDamage() : 0) + (a_2 != null ? a_2.getDamage() : 0) + (a_3 != null ? a_3.getDamage() : 0) + (a_4 != null ? a_4.getDamage() : 0);
        defense += (a_1 != null ? a_1.getDefense() : 0) + (a_2 != null ? a_2.getDefense() : 0) + (a_3 != null ? a_3.getDefense() : 0) + (a_4 != null ? a_4.getDefense() : 0);
        return totalStats;
    }

    public static void createEgg(MapleCharacter chr) {
        createEgg(chr, 1000);
    }

    public boolean isLegendary() {
        return PalTemplateProvider.getTemplate(getModel()).evo() == 4;
    }

    public boolean isMega() {
        return PalTemplateProvider.getTemplate(getModel()).evo() == 5;
    }

    public boolean isGiga() {
        return PalTemplateProvider.getTemplate(getModel()).evo() == 6;
    }

    public void setBattle(int bid) {
        battle = bid;
        Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET battle = ? where id = ?").bind(0, battle).bind(1, id).execute());
    }

    public void setSkill(int sid) {
        skill = sid;
        Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET skill = ? where id = ?").bind(0, skill).bind(1, id).execute());
    }

    public void setCapHP(int sid) {
        hp_cap = sid;
        Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET hp_cap = ? where id = ?").bind(0, skill).bind(1, id).execute());
    }

    public void setCapStats(int sid) {
        stat_cap = sid;
        Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET stat_cap = ? where id = ?").bind(0, skill).bind(1, id).execute());
    }

    public void setCapDamage(int sid) {
        damage_cap = sid;
        Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET damage_cap = ? where id = ?").bind(0, skill).bind(1, id).execute());
    }

    public void setCapLevel(int sid) {
        level_cap = sid;
        Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET level_cap = ? where id = ?").bind(0, skill).bind(1, id).execute());
    }

    public void resetAbilities() {
        int abilityCount = Randomizer.Max(PalTemplateProvider.getTemplate(getModel()).evo(), 4);
        for (int j = 0; j < abilityCount; j++) {
            var x = PalTemplateProvider.getAbilitiesbyTier(Randomizer.random(1, 5));
            if (PalTemplateProvider.getTemplate(getModel()).evo() == 4) {
                if (j == 0) {
                    x = PalTemplateProvider.getAbilitiesbyTier(6);
                } else {
                    x = PalTemplateProvider.getAbilitiesbyTier(Randomizer.random(3, 5));
                }
            }
            if (PalTemplateProvider.getTemplate(getModel()).evo() == 5) {
                if (j == 0) {
                    x = PalTemplateProvider.getAbilitiesbyTier(6);
                } else {
                    x = PalTemplateProvider.getAbilitiesbyTier(Randomizer.random(4, 6));
                }
            }
            abilities[j] = x.get(Randomizer.nextInt(x.size())).shortValue();
        }
        saveLevel();
    }

    public void createAbilities(MaplePal egg, int id) {
        int abilityCount = Randomizer.Max(PalTemplateProvider.getTemplate(id).evo(), 4);
        for (int j = 0; j < abilityCount; j++) {
            var x = PalTemplateProvider.getAbilitiesbyTier(Randomizer.random(1, 5));
            if (PalTemplateProvider.getTemplate(id).evo() == 4) {
                if (j == 0) {
                    x = PalTemplateProvider.getAbilitiesbyTier(6);
                } else {
                    x = PalTemplateProvider.getAbilitiesbyTier(Randomizer.random(3, 5));
                }
            }
            if (PalTemplateProvider.getTemplate(id).evo() == 5) {
                if (j == 0) {
                    x = PalTemplateProvider.getAbilitiesbyTier(6);
                } else {
                    x = PalTemplateProvider.getAbilitiesbyTier(Randomizer.random(4, 6));
                }
            }
            egg.abilities[j] = x.get(Randomizer.nextInt(x.size())).shortValue();
        }
    }

    public static int getRandomStat(int e) {
        double evo = Randomizer.random(e * 5, e * 10);
        if (e == 4) {
            evo = Randomizer.random(25, 50);
        }
        if (e == 5) {
            evo = Randomizer.random(50, 100);
        }
        return (int) evo;
    }

    public static int getRandomStatLevel(int e, int level) {
        double evo = Randomizer.random(e * 5, e * 10);
        if (e == 4) {
            evo = Randomizer.random(25, 50);
        }
        if (e == 5) {
            evo = Randomizer.random(50, 100);
        }
        return (int) evo;
    }

    public static void resetStats(MapleCharacter chr, MaplePal egg) {
        egg.level = (short) 1;
        egg.exp = 0;
        for (int j = 0; j < egg.IVs.length; j++) {
            egg.IVs[j] = (short) getRandomStat(egg.getEvo());
        }
        egg.createStats(egg);
        egg.upgrades = 100;
        egg.save();
    }

    public static void createStats(MaplePal egg) {
        int e = PalTemplateProvider.getTemplate(egg.templateId).evo();
        egg.stats[0] = 100 + getRandomStat(e);//hp
        egg.stats[1] = 10 + getRandomStat(e);//str
        egg.stats[2] = 10 + getRandomStat(e);//dex
        egg.stats[3] = 10 + getRandomStat(e);//int
        egg.stats[4] = 10 + getRandomStat(e);//luk
        egg.stats[5] = 25 + getRandomStat(e);//atk
        egg.stats[6] = 25 + getRandomStat(e);//matk
        egg.stats[7] = 15 + getRandomStat(e);//def
        egg.stats[8] = 15 + getRandomStat(e);//mdef
    }

    public static void createStats(MaplePal egg, int id) {
        int e = PalTemplateProvider.getTemplate(id).evo();
        egg.stats[0] = 100 + getRandomStat(e);//hp
        egg.stats[1] = 10 + getRandomStat(e);//str
        egg.stats[2] = 10 + getRandomStat(e);//dex
        egg.stats[3] = 10 + getRandomStat(e);//int
        egg.stats[4] = 10 + getRandomStat(e);//luk
        egg.stats[5] = 25 + getRandomStat(e);//atk
        egg.stats[6] = 25 + getRandomStat(e);//matk
        egg.stats[7] = 15 + getRandomStat(e);//def
        egg.stats[8] = 15 + getRandomStat(e);//mdef
    }

    public static void createStats(MaplePal egg, int id, int level) {
        int e = PalTemplateProvider.getTemplate(id).evo();
        egg.stats[0] = 100 + getRandomStat(e) + (Randomizer.random(level * (e), level * (e * 2)));//hp
        egg.stats[1] = 10 + getRandomStat(e) + (Randomizer.random(level, level * e));//str
        egg.stats[2] = 10 + getRandomStat(e) + (Randomizer.random(level, level * e));//dex
        egg.stats[3] = 10 + getRandomStat(e) + (Randomizer.random(level, level * e));//int
        egg.stats[4] = 10 + getRandomStat(e) + (Randomizer.random(level, level * e));//luk
        egg.stats[5] = 25 + getRandomStat(e) + (Randomizer.random(level, level * e));//atk
        egg.stats[6] = 25 + getRandomStat(e) + (Randomizer.random(level, level * e));//matk
        egg.stats[7] = 15 + getRandomStat(e) + (Randomizer.random(level, level * e));//def
        egg.stats[8] = 15 + getRandomStat(e) + (Randomizer.random(level, level * e));//mdef
    }

    public static void createSuperStats(MaplePal egg, int id, int level, double multi) {
        int e = PalTemplateProvider.getTemplate(id).evo();
        egg.stats[0] = (int) ((100 + getRandomStat(e) + (Randomizer.random(level * (2 * e), level * (4 * e)))));//hp
        egg.stats[1] = (int) (10 + getRandomStat(e) + (Randomizer.random(level, level * (2 * e))) * (multi));//str
        egg.stats[2] = (int) (10 + getRandomStat(e) + (Randomizer.random(level, level * (2 * e))) * (multi));//dex
        egg.stats[3] = (int) (10 + getRandomStat(e) + (Randomizer.random(level, level * (2 * e))) * (multi));//int
        egg.stats[4] = (int) (10 + getRandomStat(e) + (Randomizer.random(level, level * (2 * e))) * (multi));//luk
        egg.stats[5] = (int) (25 + getRandomStat(e) + (Randomizer.random(level, level * (2 * e))) * (multi));//atk
        egg.stats[6] = (int) (25 + getRandomStat(e) + (Randomizer.random(level, level * (2 * e))) * (multi));//matk
        egg.stats[7] = (int) (15 + getRandomStat(e) + (Randomizer.random(level, level * (2 * e))) * (multi));//def
        egg.stats[8] = (int) (15 + getRandomStat(e) + (Randomizer.random(level, level * (2 * e))) * (multi));//mdef
    }

    public static void createEgg(MapleCharacter chr, int level) {
        chr.getPalStorage().createNewEgg(egg -> {
            //TODO:set stats in this lambda function, template ID MUST be negative
            var p = PalTemplateProvider.getPals();
            short tid = p.get(Randomizer.nextInt(p.size())).shortValue();
            if (tid != 0) {
                egg.templateId = (short) -tid;
                egg.level = (short) 1;
                egg.gender = (byte) Randomizer.nextInt(2);
                egg.element = (byte) PalTemplateProvider.getTemplate(tid).element();
                egg.rank = (byte) 1;
                egg.tier = (byte) 0;
                int evo = PalTemplateProvider.getTemplate(tid).evo();
                for (int j = 0; j < egg.IVs.length; j++) {
                    egg.IVs[j] = (short) getRandomStat(evo);
                    //egg.IVs[j] = (short) 196;
                }
                egg.createStats(egg, tid);
                egg.createAbilities(egg, tid);
                egg.name = PalTemplateProvider.getTemplate(tid).name();
                egg.checkExtra();
                egg.born = 0;
                egg.upgrades = 100;
                return true;
            }
            return false;
        });
    }

    public static void createEggFromElement(MapleCharacter chr, int base, int level) {
        chr.getPalStorage().createNewEgg(egg -> {
            //TODO:set stats in this lambda function, template ID MUST be negative

            var p = PalTemplateProvider.getPalsbyType(base);
            short tid = p.get(Randomizer.nextInt(p.size())).shortValue();
            if (tid != 0) {
                egg.templateId = (short) -tid;
                egg.level = (short) 1;
                egg.gender = (byte) Randomizer.nextInt(2);
                egg.element = (byte) PalTemplateProvider.getTemplate(tid).element();
                egg.rank = (byte) 1;
                egg.tier = (byte) 0;
                int evo = PalTemplateProvider.getTemplate(tid).evo();
                for (int j = 0; j < egg.IVs.length; j++) {
                    egg.IVs[j] = (short) getRandomStat(evo);
                }
                egg.createStats(egg, tid);
                egg.createAbilities(egg, tid);
                egg.name = PalTemplateProvider.getTemplate(tid).name();
                egg.checkExtra();
                egg.born = 0;
                egg.upgrades = 100;
                return true;
            }
            return false;
        });
    }

    public static void createEggFromEvo(MapleCharacter chr, int base, int level) {
        chr.getPalStorage().createNewEgg(egg -> {
            //TODO:set stats in this lambda function, template ID MUST be negative
            var p = PalTemplateProvider.getPalsbyEvo(base);
            short tid = p.get(Randomizer.nextInt(p.size())).shortValue();
            if (tid != 0) {
                egg.templateId = (short) -tid;
                egg.level = (short) 1;
                egg.gender = (byte) Randomizer.nextInt(2);
                egg.element = (byte) PalTemplateProvider.getTemplate(tid).element();
                egg.rank = (byte) 1;
                egg.tier = (byte) 0;
                int evo = PalTemplateProvider.getTemplate(tid).evo();
                for (int j = 0; j < egg.IVs.length; j++) {
                    egg.IVs[j] = (short) getRandomStat(evo);
                }
                egg.createStats(egg, tid);
                egg.createAbilities(egg, tid);
                egg.name = PalTemplateProvider.getTemplate(tid).name();
                egg.checkExtra();
                egg.born = 0;
                egg.upgrades = 100;
                return true;
            }
            return false;
        });
    }

    public static void createEggFromParents(MapleCharacter chr, MaplePal male, MaplePal female) {
        chr.getPalStorage().createNewEgg(egg -> {
            //TODO:set stats in this lambda function, template ID MUST be negative
            short tid = (short) (Randomizer.nextBoolean() ? male.getModel() : female.getModel());
            if (tid != 0) {
                egg.templateId = (short) -tid;
                egg.level = (short) 1;
                egg.gender = (byte) (Randomizer.nextBoolean() ? 0 : 1);
                egg.element = (byte) PalTemplateProvider.getTemplate(tid).element();
                egg.rank = (byte) 1;
                egg.tier = (byte) 0;
                for (int j = 0; j < egg.IVs.length; j++) {
                    egg.IVs[j] = (short) (Randomizer.random(Math.min(male.IVs[j], female.IVs[j]), Math.max(male.IVs[j], female.IVs[j])));
                }
                egg.createStats(egg, tid);
                int abilityCount = Randomizer.Max(PalTemplateProvider.getTemplate(tid).evo(), 4);
                for (int j = 0; j < abilityCount; j++) {
                    short x = (Randomizer.nextBoolean() ? male.abilities[j] : female.abilities[j]);
                    if (x == 0) {
                        var ab = PalTemplateProvider.getAbilitiesbyTier(Randomizer.random(1, 5));
                        x = (short) ab.get(Randomizer.nextInt(ab.size())).shortValue();
                    }
                    egg.abilities[j] = x;
                }
                egg.name = PalTemplateProvider.getTemplate(tid).name();
                egg.checkExtra();
                egg.born = 0;
                egg.upgrades = 100;
                return true;
            }
            return false;
        });
    }

    public static void createEgg(MapleCharacter chr, int model, int level) {
        chr.getPalStorage().createNewEgg(egg -> {
            //TODO:set stats in this lambda function, template ID MUST be negative
            short tid = (short) (Randomizer.Min(model, 1));
            if (tid != 0) {
                egg.templateId = (short) -tid;
                egg.level = (short) 1;
                egg.gender = (byte) Randomizer.nextInt(2);
                egg.element = (byte) PalTemplateProvider.getTemplate(tid).element();
                egg.rank = (byte) 1;
                egg.tier = (byte) 0;
                int evo = PalTemplateProvider.getTemplate(tid).evo();
                for (int j = 0; j < egg.IVs.length; j++) {
                    egg.IVs[j] = (short) getRandomStat(evo);
                }
                egg.createStats(egg, tid);
                egg.createAbilities(egg, tid);
                egg.name = PalTemplateProvider.getTemplate(tid).name();
                egg.checkExtra();
                egg.born = 0;
                egg.upgrades = 100;
                return true;
            }
            return false;
        });
    }

    public MaplePal createNewEgg(int charId, Predicate<MaplePal> statsSetter) {
        Jdbi.j.useTransaction(h -> {
            long newId = h.createUpdate("INSERT INTO player_pals (charid, pos) VALUES (?, 'STORAGE')").bind(0, charId).executeAndReturnGeneratedKeys().mapTo(Long.class).one();
            this.id = newId;
            if (!statsSetter.test(this)) {
                h.rollback();
            } else {
                save();
            }
        });
        return this;
    }

    public int getPalIVBoost(int value) {
        return Randomizer.MinMax(Randomizer.random(1, value), 1, 250);
    }

    public MaplePal createNewTempPal() {
        return this;
    }

    public void setName(String Name) {
        name = Name;
    }

    public long getExpLevel(int level) {
        return Randomizer.LongMax((long) (100 + Math.pow(level, 3)), Long.MAX_VALUE);
    }

    public void checkEquips() {

    }

    public void setAcc(int slot, int id) {
        if (slot == 1) {
            acc_1 = id;
            slot_1 = PalTemplateProvider.getEquip(id);
        }
        if (slot == 2) {
            acc_2 = id;
            slot_2 = PalTemplateProvider.getEquip(id);
        }
        if (slot == 3) {
            acc_3 = id;
            slot_3 = PalTemplateProvider.getEquip(id);
        }
        if (slot == 4) {
            acc_4 = id;
            slot_4 = PalTemplateProvider.getEquip(id);
        }
        saveAcc();
    }

    public int getAcc(int slot) {
        if (slot == 1) {
            return acc_1;
        }
        if (slot == 2) {
            return acc_2;
        }
        if (slot == 3) {
            return acc_3;
        }
        if (slot == 4) {
            return acc_4;
        }
        return acc_1;
    }

    public int skillSpeed() {
        return 1000 + (BattleData.getEffectDurationSize(skill) * 30);
    }

    public int getSpeed() {//convert speed into ms
        double spd = 100.0 + (slot_1 != null ? slot_1.getSpeed() : 0) + (slot_2 != null ? slot_2.getSpeed() : 0) + (slot_3 != null ? slot_3.getSpeed() : 0) + (slot_4 != null ? slot_4.getSpeed() : 0);
        int x = Randomizer.Min((int) (Math.floor((100.0 / spd) * skillSpeed())), 1000);
        return (int) x;
    }

    public double getDamage(int base) {//convert speed into ms
        double dam = 100 + (slot_1 != null ? slot_1.getDamage() : 0) + (slot_2 != null ? slot_2.getDamage() : 0) + (slot_3 != null ? slot_3.getDamage() : 0) + (slot_4 != null ? slot_4.getDamage() : 0);
        return base * (dam / 100.0);
    }

    public double getDefense(int base) {//convert speed into ms
        double def = 100.0 + (slot_1 != null ? slot_1.getDefense() : 0) + (slot_2 != null ? slot_2.getDefense() : 0) + (slot_3 != null ? slot_3.getDefense() : 0) + (slot_4 != null ? slot_4.getDefense() : 0);
        return base * (100.0 / def);
    }

    public void setUpgrades(int value) {
        upgrades = value;
    }

    public int getUpgrades() {
        return upgrades;
    }

    public void useUpgrade(int type, int amount) {
        if (upgrades >= amount) {
            IVs[type] += amount;
            upgrades -= amount;
            Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET iv_hp = ?, iv_str = ?, iv_dex = ?, iv_int = ?, iv_luk = ?, iv_atk = ?, iv_matk = ?, iv_def = ?, iv_mdef = ?, upgrades = ? WHERE id = ?")
                    .bind(0, IVs[0])
                    .bind(1, IVs[1])
                    .bind(2, IVs[2])
                    .bind(3, IVs[3])
                    .bind(4, IVs[4])
                    .bind(5, IVs[5])
                    .bind(6, IVs[6])
                    .bind(7, IVs[7])
                    .bind(8, IVs[8])
                    .bind(9, upgrades)
                    .bind(10, id)
                    .execute());
        }
    }

    public int getLevel() {
        return level;
    }

    public long getExp() {
        return exp;
    }

    public int getExpPerc() {
        return (int) Math.floor((exp / getExpLevel(level)) * 100);
    }

    public int getEvo() {
        return PalTemplateProvider.getTemplate(templateId).evo();
    }

    public void gainLevelData(MapleCharacter chr, long value) {
        if (level < 999) {
            long rExp = getExpLevel(level);
            long leftover = 0;
            double sExp = (double) exp + (double) value;
            exp = (long) Randomizer.DoubleMinMax(sExp, 0, Long.MAX_VALUE);
            if (exp >= rExp) {
                int evo = PalTemplateProvider.getTemplate(templateId).evo();
                leftover = (long) (sExp - rExp);
                exp = 0;
                level++;
                for (int j = 0; j < stats.length; j++) {
                    if (j == 0) {
                        int hp = Randomizer.random(evo, evo * 2);
                        stats[j] = Randomizer.Max(stats[j] + hp, 999999);
                    } else {
                        int stat = Randomizer.random(0, evo);
                        stats[j] = Randomizer.Max(stats[j] + stat, 99999);
                    }
                }
                levelup = true;
                levels++;
            }
            if (leftover > 0) {
                gainLevelData(chr, leftover);
                return;
            }
        }
        if (levelup) {
            levelup = false;
            chr.getClient().announce(CField.EffectPacket.showForeignEffect(35));
            chr.dropTopMessage(name + " has leveled up " + levels + " times!");
            chr.updateStats();
            levels = 0;
        }
        saveLevel();
    }

    public void saveAcc() {
        Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET acc_1 = ?, acc_2 = ?,acc_3 = ?, acc_4 = ? WHERE id = ?")
                .bind(0, acc_1)
                .bind(1, acc_2)
                .bind(2, acc_3)
                .bind(3, acc_4)
                .bind(4, id)
                .execute());
    }

    public void saveLevel() {
        Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET level = ?, exp = ?, hp = ?, str = ?, dex = ?, `int` = ?, luk = ?, atk = ?, matk = ?, def = ?, mdef = ?, ability_1 = ?, ability_2 = ?, ability_3 = ?, ability_4 = ? WHERE id = ?")
                .bind(0, level)
                .bind(1, exp)
                .bind(2, stats[0])
                .bind(3, stats[1])
                .bind(4, stats[2])
                .bind(5, stats[3])
                .bind(6, stats[4])
                .bind(7, stats[5])
                .bind(8, stats[6])
                .bind(9, stats[7])
                .bind(10, stats[8])
                .bind(11, abilities[0])
                .bind(12, abilities[1])
                .bind(13, abilities[2])
                .bind(14, abilities[3])
                .bind(15, id)
                .execute());
    }

    public void save() {
        if (templateId != 0) {
            Jdbi.j.useHandle(h -> h.createUpdate("UPDATE player_pals SET template_id = ?, name = ?, `tier` = ?, `rank` = ?, gender = ?, level = ?, exp = ?"
                    + ", hp = ?, str = ?, dex = ?, `int` = ?, luk = ?, atk = ?, matk = ?, def = ?, mdef = ?, ability_1 = ?, ability_2 = ?, ability_3 = ?, ability_4 = ?"
                    + ", iv_hp = ?, iv_str = ?, iv_dex = ?, iv_int = ?, iv_luk = ?, iv_atk = ?, iv_matk = ?, iv_def = ?, iv_mdef = ?, hatch_start_time = NULL"
                    + ", acc_1 = ?, acc_2 = ?,acc_3 = ?, acc_4 = ?, upgrades = ?, speed = ?, element = ?, born = 0 WHERE id = ?")
                    .bind(0, templateId)
                    .bind(1, name)
                    .bind(2, tier)
                    .bind(3, rank)
                    .bind(4, gender)
                    .bind(5, level)
                    .bind(6, exp)
                    .bind(7, stats[0])
                    .bind(8, stats[1])
                    .bind(9, stats[2])
                    .bind(10, stats[3])
                    .bind(11, stats[4])
                    .bind(12, stats[5])
                    .bind(13, stats[6])
                    .bind(14, stats[7])
                    .bind(15, stats[8])
                    .bind(16, abilities[0])
                    .bind(17, abilities[1])
                    .bind(18, abilities[2])
                    .bind(19, abilities[3])
                    .bind(20, IVs[0])
                    .bind(21, IVs[1])
                    .bind(22, IVs[2])
                    .bind(23, IVs[3])
                    .bind(24, IVs[4])
                    .bind(25, IVs[5])
                    .bind(26, IVs[6])
                    .bind(27, IVs[7])
                    .bind(28, IVs[8])
                    .bind(29, acc_1)
                    .bind(30, acc_2)
                    .bind(31, acc_3)
                    .bind(32, acc_4)
                    .bind(33, upgrades)
                    .bind(34, speed)
                    .bind(35, element)
                    .bind(36, id)
                    .execute());
        }
    }

    public int getElement() {
        return element;
    }

    public void setupHP() {
        hp = getStats()[0];
        maxhp = hp;
        //System.out.println(name + " - hp: " + hp);
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int value) {
        hp = value;
    }

    public boolean isAlive() {
        return hp > 0;
    }
    
    public void addStat(int type, int value) {
        stats[type] += value;
    }
}
