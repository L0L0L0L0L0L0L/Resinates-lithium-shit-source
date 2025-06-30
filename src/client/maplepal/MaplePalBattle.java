package client.maplepal;

import client.MapleCharacter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import client.maplepal.PalTemplateProvider.PalSkillTemplate;
import constants.GameConstants;
import server.MapleItemInformationProvider;
import server.Randomizer;
import tools.StringUtil;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packet.CField;

public class MaplePalBattle {

    protected final int selfCharId, foeId;
    protected final boolean isFoePlayer;
    protected final int backgroundId;
    protected List<MaplePal> selfPals = new ArrayList<>();
    protected List<MaplePal> foePals = new ArrayList<>();
    protected boolean isWin, pvp = false;
    protected BattleData bd = new BattleData();
    protected long startTime, points;
    protected String foeName;
    protected Map<Integer, Integer> loot = new HashMap<Integer, Integer>();
    protected boolean rewards = true;
    public boolean random = false, caught = false, kaotic = false;
    public int randomPalId = 0, level = 1, credits = 0, ach = 0;
    protected Set<Integer> usedSkills = new HashSet<>();
    public MapleCharacter foe = null, player = null;
    public double power = 1.0;

    //Battle rewards
    protected long rewardEXP = 0;
    protected List<MaplePal> livePals = new ArrayList<>();
    protected MaplePal activePal;

    public MaplePalBattle(int charId, int foeId, boolean isFoePlayer, int backgroundId) {
        this.selfCharId = charId;
        this.foeId = foeId;
        this.isFoePlayer = isFoePlayer;
        this.backgroundId = backgroundId;
    }

    public MaplePalBattle setSelfPals(List<MaplePal> pals) {
        selfPals.clear();
        selfPals.addAll(pals);
        //TODO: sort by priority
        return this;
    }

    public MaplePalBattle setFoePals(List<MaplePal> pals) {
        foePals.clear();
        foePals.addAll(pals);
        //TODO: sort by priority
        return this;
    }

    public boolean isWin() {
        return isWin;
    }

    public boolean isPvE() {
        return isFoePlayer;
    }

    public void setFoeName(String name) {
        foeName = name;
    }

    public String getFoeName() {
        return foeName;
    }

    public double getWeakness(int e, int e2) {
        //weakness
        if ((e == 0 && e2 == 1) || (e == 1 && e2 == 0)) {
            return 2.0;
        }
        if ((e == 2 && e2 == 3) || (e == 3 && e2 == 2)) {
            return 2.0;
        }
        if ((e == 4 && e2 == 5) || (e == 5 && e2 == 4)) {
            return 2.0;
        }
        if ((e == 6 && e2 == 7) || (e == 7 && e2 == 6)) {
            return 2.0;
        }
        if ((e == 8 && e2 == 9) || (e == 9 && e2 == 8)) {
            return 2.0;
        }
        //strongaa
        if (e == e2) {
            return 0.5;
        }
        //defualt
        return 1.0;
    }

    public int damagePal(MaplePal from, MaplePal to, boolean m) {
        int damage = 1, complete = 0;
        int dex = (int) (Randomizer.DoubleMax(to.getStats()[2] * 0.001, 100));
        if (Randomizer.random(1, 250) >= dex) {
            int luk = (int) (from.getStats()[4] * 0.001);
            boolean crit = Randomizer.random(1, 250) <= luk;
            if (!m) {
                double atk = from.getDamage((int) (from.getStats()[5] + (from.getStats()[1] * 0.25)));
                double melee = from.getDamage((int) (from.getStats()[5] + (from.getStats()[1])));
                double defense = to.getDefense((int) (to.getStats()[7] + to.getStats()[1]));
                double g = Randomizer.DoubleMax(melee / (defense * 2), 1.0);
                damage = (int) (atk * g * 0.5);
                //System.out.println(from.getName() + " damage: " + melee + " - def: " + defense + " Damage: " + damage + " g: " + g);
            } else {
                double atk = from.getDamage((int) (from.getStats()[6] + (from.getStats()[3] * 0.25)));
                double magic = from.getDamage((int) (from.getStats()[6] + (from.getStats()[3])));
                double m_defense = to.getDefense((int) (to.getStats()[8] + to.getStats()[3]));
                double g = Randomizer.DoubleMax(magic / (m_defense * 2), 1.0);
                damage = (int) (atk * g * 0.5);
                //System.out.println(from.getName() + " M - damage: " + magic + " - def: " + m_defense + " Damage: " + damage + " g: " + g);
            }
            double diff = Randomizer.DoubleMinMax(Math.pow((double) from.getLevel() / (double) to.getLevel(), 2), 0.5, 2.0);
            double eb = 1.0;
            if (from.skill != 0 && PalTemplateProvider.getSkill(from.skill).element() == from.element) {
                eb = 1.5;
            }
            double sp = (PalTemplateProvider.getSkill(from.skill).power() * 0.01) * eb;
            double weak = getWeakness(PalTemplateProvider.getSkill(from.skill).element(), to.element);
            double fdam = (damage * weak * diff * (crit ? 2 : 1) * sp);
            double base = Randomizer.Min((int) Randomizer.randomDouble(fdam, fdam * 1.05), 1);
            complete = Randomizer.MinMax((int) base, 1, from.getLevel() > 999 ? 999999 : from.getLevel() > 99 ? 99999 : 9999);
        }
        return complete;
    }

    public void doBattle(MapleCharacter player) {
        int turns = 0;
        startTime = System.currentTimeMillis();
        BattleState self = new BattleState(true, this.selfPals);
        BattleState foe = new BattleState(false, this.foePals);

        //Begin battle
        //bd.startBattleEffect().delay(500);
        bd.delay(1000);

        //Battle until one side is out of pals
        while (!GameConstants.getLock() && self.hasRemainingPals() && foe.hasRemainingPals() && turns < 999) {
            //Determine which pal will perform an action this turn
            BattleState first;
            if (self.remainingActionTime > foe.remainingActionTime) {
                first = foe;
            } else if (self.remainingActionTime < foe.remainingActionTime) {
                first = self;
            } else {
                first = Randomizer.nextBoolean() ? self : foe; //TODO:some other tie-breaker criteria?
            }
            BattleState second = (first == self ? foe : self);

            //Calculate and set timer bars
            bd.actionBar(first.isSelf, 100, first.remainingActionTime); //Show own action bar completing
            bd.actionBar(second.isSelf, (int) (100.0 * (1.0 - ((double) (second.remainingActionTime - first.remainingActionTime) / (double) second.getActionTime()))), first.remainingActionTime); //Show foe action bar filling to next pause value
            bd.delay(first.remainingActionTime);
            bd.actionBar(first.isSelf, 0, 0); //Clear action bar
            //Set new remaining times
            second.remainingActionTime = Math.max(second.remainingActionTime - first.remainingActionTime, 0);
            first.remainingActionTime = first.getActionTime();
            //Attack animation
            int effectId = first.getActivePal().skill;
            int sDelay = (int) (bd.getEffectDuration(effectId) * 0.55);
            int totald = 0;
            int hits = first.getActivePal().attacks;
            for (int i = 0; i < hits; i++) {
                bd.delay(-sDelay);
                //bd.showToolTip(effectId);//.delay(effectDelay) - 
                bd.playSound(0);
                bd.attackEffect(!first.isSelf, effectId, false);//
                int damage = (int) Math.floor((damagePal(first.getActivePal(), second.getActivePal(), PalTemplateProvider.getSkill(effectId).magic())) / hits); //TODO:damage calc - add check for magic/melee
                bd.showDamage(second.isSelf, damage, sDelay);
                totald += damage;
            }
            second.getActivePal().hp = Math.max(second.getActivePal().hp - totald, 0); //Set new HP
            bd.setHp(second.isSelf, second.getActivePal().hp, 500).delay(250);
            if (second.getActivePal().hp <= 0) {
                bd.delay(500);
                int newIndex = second.nextPal();
                first.remainingActionTime = first.getActionTime();
                bd.deathEffect(second.isSelf);
                bd.setPal(second.isSelf, -1).actionBar(second.isSelf, 0, 0);//
                bd.delay(250);
                if (newIndex != -1) {
                    //Spawn next pal
                    bd.delay(200); //Delay
                    bd.summonEffect(second.isSelf);
                    bd.delay(-100);
                    bd.setPal(second.isSelf, newIndex).setHp(second.isSelf, second.getActivePal().hp, 0).delay(300); //Make pal appear a bit before summon animation ends (maybe add fade)
                    bd.delay(250); //Delay for next turn
                }
            }
            turns++;
        }
        if (turns >= 999) {
            bd.delay(400).finishBattle(false);
            player.addAccVar("Pal_Loss", 1);
        } else {
            //Battle over
            isWin = self.hasRemainingPals();
            bd.delay(400).finishBattle(isWin);

            if (isWin) {
                player.addAccVar("Pal_Win", 1);
                //Calculate and store rewards
                if (rewards) {
                    double etc = player.getStat().getItemKpRate();
                    double exp = player.getStat().getItemExpRate();
                    rewardEXP = 0;
                    for (MaplePal pal : foePals) {
                        double e = (1.5 + (pal.getEvo() * 0.1));
                        rewardEXP += (long) ((100 + Math.floor(Math.pow(pal.getLevel() * power, e))) * exp);
                        for (int i = 1; i <= 4; i++) {
                            if (pal.getAcc(i) != 0) {
                                int chance = Randomizer.random(1, 100);
                                if (chance == 1) {
                                    addLoot(pal.getAcc(i), (int) (etc));
                                }
                            }
                        }
                        addLoot(4201001, (int) ((int) Math.pow(pal.getEvo(), e) * etc));
                        if (kaotic && Randomizer.random(1, 4) == 1) {
                            addLoot(4202014, (int) Math.floor(etc));
                        }
                        addLoot(4310506, 1);
                    }
                }
                livePals = self.pals;
                activePal = self.getActivePal();
            } else {
                player.addAccVar("Pal_Loss", 1);
            }
        }
    }

    public void addLoot(int id, int amount) {
        if (loot.get(id) == null) {
            loot.put(id, 0);
        }
        int total = loot.get(id) + amount;
        loot.put(id, total);
    }

    public void encode(MaplePacketLittleEndianWriter mplew) {
        //Encode battle events
        bd.encode(mplew);
        //Encode final detail text lines
        List<String> finishDetails = new ArrayList<>();
        if (isWin) {
            if (isFoePlayer) {
                finishDetails.add("You have defeated " + foeName);
            } else {
                finishDetails.add("You finished the battle.");
            }
            if (rewards) {
                int count = 0;
                for (MaplePal pal : livePals) {
                    if (pal == activePal) {
                        if (rewardEXP > 0) {
                            finishDetails.add(pal.getName() + " gained " + StringUtil.getUnitFullNumber(rewardEXP) + " XP");
                        }
                    } else {
                        count++;
                    }
                }
                if (count > 0) {
                    long leech = (long) Math.floor(rewardEXP * 0.25);
                    finishDetails.add("Side Pals gained " + StringUtil.getUnitFullNumber(leech) + " XP");
                }
                if (!loot.isEmpty()) {
                    Iterator<Integer> iter = loot.keySet().iterator();
                    while (iter.hasNext()) {
                        int i = iter.next();
                        int a = loot.get(i);
                        finishDetails.add("Gained " + a + " " + MapleItemInformationProvider.getInstance().getName(i) + (a > 1 ? "s" : ""));
                    }
                }
                if (random) {
                    int c = Randomizer.random(1, 4);
                    if (c == 1) {
                        caught = true;
                        finishDetails.add(PalTemplateProvider.getTemplate(randomPalId).name() + " egg has dropped");
                    }
                }
                /*
                credits = 0;
                if (isFoePlayer) {
                    if (player != null && foe != null) {
                        long f = foe.getAccVara("Pal_Credits");
                        long p = player.getAccVara("Pal_Credits");
                        if (isWin) {
                            int m = Randomizer.Max((int) Math.floor(f * 0.05), 100000);
                            if (m > 0) {
                                credits = m;
                                finishDetails.add("Stole +" + StringUtil.getUnitFullNumber(credits) + " Credits from " + foe.getName());
                            }
                        } else {
                            int m = Randomizer.Max((int) Math.floor(p * 0.01), 100000);
                            if (m > 0) {
                                credits = m;
                                finishDetails.add("Gained +" + credits + " Credits");
                            }
                        }
                    }

                } else {
                    for (MaplePal fpal : foePals) {
                        credits += fpal.level;
                    }
                    finishDetails.add("Gained +" + credits + " Credits");
                }
                 */
                if (rewardEXP > 0) {
                    finishDetails.add("Gained +" + StringUtil.getUnitFullNumber(rewardEXP) + " Pal-Mastery");
                }

            }
        }
        //Finish encoding battle info
        mplew.writeInt(75); //nLineDelay
        mplew.writeShort(finishDetails.size());
        for (String s : finishDetails) {
            mplew.writeMapleAsciiString(s);
        }

        mplew.writeShort(usedSkills.size());
        for (int id : usedSkills) {
            PalSkillTemplate skill = PalTemplateProvider.getSkill(id);
            mplew.writeShort(id);
            mplew.writeBool(!skill.magic());
            mplew.write(skill.element());
            mplew.writeString(skill.name());
        }
        mplew.writeBool(true);
    }

    private static class BattleState {

        final boolean isSelf;
        List<MaplePal> pals = new ArrayList<>();
        int remainingActionTime;
        int curPalIndex;

        BattleState(boolean isSelf, List<MaplePal> pals) {
            this.isSelf = isSelf;
            this.pals.addAll(pals); //TODO:sort based on priority
            pals.forEach(p -> p.setupHP()); //Refill HP
            remainingActionTime = getActionTime();
        }

        boolean hasRemainingPals() {
            return !pals.isEmpty();
        }

        MaplePal getActivePal() {
            return !pals.isEmpty() ? pals.get(0) : null;
        }

        int getActionTime() {
            return getActionTime(getActivePal());
        }

        int nextPal() {
            if (!pals.isEmpty()) {
                pals.remove(0);
            }
            if (!pals.isEmpty()) {
                remainingActionTime = getActionTime();
            }
            return !pals.isEmpty() ? ++curPalIndex : -1;
        }

        int getActionTime(MaplePal pal) {
            return pal == null ? 1000 : pal.getSpeed(); //TODO:calculate action time properly
        }
    }
}
