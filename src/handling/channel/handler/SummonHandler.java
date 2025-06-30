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
package handling.channel.handler;

import constants.GameConstants;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

import client.Skill;
import client.MapleBuffStat;
import client.MapleClient;
import client.MapleCharacter;
import client.MapleDisease;
import client.SkillFactory;
import client.SummonSkillEntry;
import client.status.MonsterStatusEffect;
import client.status.MonsterStatus;
import static handling.channel.handler.DamageParse.getDamage;
import java.awt.Rectangle;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Map;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.Randomizer;
import server.Timer.CloneTimer;
import server.movement.LifeMovementFragment;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.maps.MapleDragon;
import server.maps.MapleMap;
import server.maps.MapleSummon;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.SummonMovementType;
import tools.AttackPair;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CField.EffectPacket;
import tools.packet.CField;
import tools.packet.CField.SummonPacket;

public class SummonHandler {

    public static final void MoveDragon(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        slea.skip(8); //POS
        final List<LifeMovementFragment> res = MovementParse.parseMovement(chr, slea, 5);
        if (chr != null && chr.getDragon() != null && res.size() > 0) {
            final Point pos = chr.getDragon().getPosition();
            MovementParse.updatePosition(res, chr.getDragon(), 0);
            if (!chr.isHidden()) {
                chr.getMap().broadcastMessage(chr, CField.moveDragon(chr.getDragon(), pos, res), chr.getTruePosition());
            }

            WeakReference<MapleCharacter>[] clones = chr.getClones();
            for (int i = 0; i < clones.length; i++) {
                if (clones[i].get() != null) {
                    final MapleMap map = chr.getMap();
                    final MapleCharacter clone = clones[i].get();
                    CloneTimer.getInstance().schedule(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                if (clone.getMap() == map && clone.getDragon() != null) {
                                    final Point startPos = clone.getDragon().getPosition();
                                    MovementParse.updatePosition(res, clone.getDragon(), 0);
                                    if (!clone.isHidden()) {
                                        map.broadcastMessage(clone, CField.moveDragon(clone.getDragon(), startPos, res), clone.getTruePosition());
                                    }

                                }
                            } catch (Exception e) {
                                //very rarely swallowed
                            }
                        }
                    }, 500 * i + 500);
                }
            }
        }
    }

    public static final void MoveSummon(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        final MapleMapObject obj = chr.getMap().getSummonByOid(slea.readInt());
        if ((MapleSummon) obj == null) {
            return;
        }
        if (obj instanceof MapleDragon) {
            MoveDragon(slea, chr);
            return;
        }
        final MapleSummon sum = (MapleSummon) obj;
        if (sum.getOwnerId() != chr.getId() || sum.getSkillLevel() <= 0 || sum.getMovementType() == SummonMovementType.STATIONARY) {
            return;
        }
        slea.skip(8); //startPOS
        final List<LifeMovementFragment> res = MovementParse.parseMovement(chr, slea, 4);

        final Point pos = sum.getPosition();
        MovementParse.updatePosition(res, sum, 0);
        if (res.size() > 0) {
            chr.getMap().broadcastMessage(chr, SummonPacket.moveSummon(chr.getId(), sum.getObjectId(), pos, res), sum.getTruePosition());
        }
    }

    public static final void DamageSummon(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        final int unkByte = slea.readByte();
        final int damage = slea.readInt();
        final int monsterIdFrom = slea.readInt();
        //       slea.readByte(); // stance

        boolean remove = false;
        for (MapleSummon summon : chr.getSummonsValues()) {
            if (summon.isPuppet() && summon.getOwnerId() == chr.getId() && damage > 0) { //We can only have one puppet(AFAIK O.O) so this check is safe.
                chr.getMap().broadcastMessage(chr, SummonPacket.damageSummon(chr.getId(), summon.getSkill(), damage, unkByte, monsterIdFrom), summon.getTruePosition());
                break;
            }
        }
        if (remove) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        }
    }

    public static BigDecimal getBaseDamage(MapleCharacter chr) {
        BigDecimal base = BigDecimal.valueOf(chr.getOPStat()).multiply(BigDecimal.valueOf(chr.totalMasteryBonus()));
        if (chr.isBoosted() && chr.getStamina() > 0) {
            base = base.multiply(BigDecimal.valueOf(1 + (chr.getStamina() * 0.01)));
            chr.gainStamina(-1, false);
        }
        return base;
    }

    public static BigInteger getDamage(MapleCharacter chr, MapleMonster mob, BigDecimal base) {
        double armor = 1 - (mob.getStats().getArmor() * 0.01);

        double mobdef = mob.getStats().getDef();
        double tdiff = (Randomizer.DoubleMax((double) chr.getStat().getPDR() / (double) (mobdef), 2.5));
        double atkdiff = Math.pow(Randomizer.DoubleMax(tdiff, 1.0), 5);

        double diff = (2 - (double) mob.getStats().getLevel() / (double) chr.getTotalLevel());
        double lvldiff = Math.pow(Randomizer.MinMaxDouble(diff, 0.0, 1.0), 6);

        double total = armor * atkdiff * lvldiff;
        BigDecimal boost = base.multiply(BigDecimal.valueOf(total));

        if (mob.getStats().isBoss()) {
            boost = boost.multiply(BigDecimal.valueOf(chr.getStat().getBossDmgR() + chr.getStat().getOverpower()));
        } else {
            boost = boost.multiply(BigDecimal.valueOf(chr.getStat().getDmgR() + chr.getStat().getOverpower()));
        }

        return boost.toBigInteger();
    }

    public static void SummonAttack(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null) {
            return;
        }
        final MapleMap map = chr.getMap();
        final MapleSummon summon = map.getSummonByOid(slea.readInt());
        if (summon == null || !(summon instanceof MapleSummon)) {
            //System.out.println("OID: " + slea.readInt());
            chr.dropMessage(5, "The summon has disappeared.");
            return;
        }
        if (summon.getOwnerId() != chr.getId()) {
            chr.dropMessage(5, "Error.");
            return;
        }
        if (summon.getSkillLevel() <= 0) {
            chr.dropMessage(5, "Error. Summon Level: " + summon.getSkillLevel());
            return;
        }
        final SummonSkillEntry sse = SkillFactory.getSummonData(summon.getSkill());
        if (summon.getSkill() / 1000000 != 35 && summon.getSkill() != 33101008 && sse == null) {
            chr.dropMessage(5, "Error in processing attack.");
            return;
        }
        if (!GameConstants.GMS) {
            slea.skip(8);
        }
        int tick = slea.readInt();
        if (sse != null && sse.delay > 0) {
            chr.updateTick(tick);
            summon.CheckSummonAttackFrequency(chr, tick);
            //chr.getCheatTracker().checkSummonAttack();
        }
        if (!GameConstants.GMS) {
            slea.skip(8);
        }
        final byte animation = slea.readByte();
        if (!GameConstants.GMS) {
            slea.skip(8);
        }
        final byte numAttacked = slea.readByte();

        if (sse != null && numAttacked > sse.mobCount) {
            chr.dropMessage(5, "Warning: Attacking more monster than summon can do");
            //chr.getCheatTracker().registerOffense(CheatingOffense.SUMMON_HACK_MOBS);
            //AutobanManager.getInstance().autoban(c, "Attacking more monster that summon can do (Skillid : "+summon.getSkill()+" Count : " + numAttacked + ", allowed : " + sse.mobCount + ")");
            return;
        }
        slea.skip(summon.getSkill() == 35111002 ? 24 : 12); //some pos stuff

        final Skill summonSkill = SkillFactory.getSkill(summon.getSkill());

        final MapleStatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());
        if (summonEffect == null) {
            chr.dropMessage(5, "Error in attack.");
            return;
        }
        double skilldam = (double) 1 + (Randomizer.DoubleMin(summonEffect.getDamage() * 0.01, 0.01));
        long finaldamage = 0;
        double pdr = chr.getStat().getPDR();

        //}
        BigDecimal boost = DamageParse.getBaseDamage(chr).multiply(BigDecimal.valueOf(skilldam));
        double mastery = 10;
        if (chr.getStat().getMastery() > 0) {
            mastery = 10 / (10 * (chr.getStat().getMastery() / 100.0));
        }

        List<AttackPair> allDamage = new ArrayList<AttackPair>();
        List<Pair<Integer, Long>> allDamageNumbers = new ArrayList<Pair<Integer, Long>>();

        List<AttackPair> allBigDamage = new ArrayList<AttackPair>();
        List<Pair<Integer, BigInteger>> allBigDamageNumbers = new ArrayList<Pair<Integer, BigInteger>>();

        for (int i = 0; i < numAttacked; i++) {
            int oid = slea.readInt();
            MapleMonster mob = chr.getMap().getMonsterByOid(oid);
            slea.skip(18); // who knows
            slea.skip(4); // damage
            long damge = 1;
            BigInteger bigRand = BigInteger.ONE;
            List<Pair<Long, Boolean>> allDamageNumbers2 = new ArrayList<Pair<Long, Boolean>>();
            List<Pair<BigInteger, Boolean>> allBigDamageNumbers2 = new ArrayList<Pair<BigInteger, Boolean>>();
            boolean crit = Randomizer.random(1, 100) <= chr.getStat().passive_sharpeye_rate();
            if (mob != null) {
                if (!chr.tagged) {
                    if (!mob.isBuffed(MonsterStatus.MAGIC_IMMUNITY) && !mob.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT) && !mob.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) {
                        BigInteger bigDam = getDamage(chr, mob, boost);
                        double master = 1 + (Randomizer.randomDouble(-mastery, 5) * 0.01);
                        bigRand = new BigDecimal(bigDam).multiply(BigDecimal.valueOf(master)).toBigInteger().max(BigInteger.ONE);
                        if (mob.getStats().getCapped()) {
                            bigRand = bigRand.min(new BigDecimal(mob.getStats().damageCap).toBigInteger());
                        }
                    }
                }
            }
            allDamageNumbers.add(new Pair<Integer, Long>(oid, damge));
            allDamageNumbers2.add(new Pair<Long, Boolean>(damge, crit));
            allDamage.add(new AttackPair(oid, allDamageNumbers2));

            allBigDamageNumbers.add(new Pair<Integer, BigInteger>(oid, bigRand));
            allBigDamageNumbers2.add(new Pair<BigInteger, Boolean>(bigRand, crit));
            allBigDamage.add(new AttackPair(oid, allBigDamageNumbers2, false));
        }
        //if (!summon.isChangedMap()) {
        if (!allBigDamage.isEmpty()) {
            map.broadcastSkill(chr, SummonPacket.summonAttack(summon.getOwnerId(), summon.getObjectId(), animation, allDamageNumbers, chr.getLevel(), false), false);
            map.broadcastSkill(chr, CField.customShowSummonDamage((byte) allBigDamageNumbers.size(), allBigDamage, animation, chr.getSkinMask() != 9999 ? chr.getSkinMask() : chr.getDamageSkin()), true);
        }
        long totaldamage = 0;
        int lines = 0;
        int pdamage = 0;
        long total = 0;
        boolean fixed = false;
        for (Pair<Integer, BigInteger> attackEntry : allBigDamageNumbers) {
            final MapleMonster mob = map.getMonsterByOid(attackEntry.left);
            if (mob == null) {
                continue;
            }
            MapleMonsterStats monsterstats = mob.getStats();
            long fixeddmg = monsterstats.getFixedDamage();
            BigInteger toDamage = attackEntry.right;
            if (mob.isBuffed(MonsterStatus.MAGIC_IMMUNITY) || mob.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT) || mob.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) {
                if (mob.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT)) { //test
                    lines += 1;
                }
                toDamage = BigInteger.ONE;;
            } else {
                if (monsterstats.getOnlyNoramlAttack()) {
                    toDamage = BigInteger.ZERO;
                } else {
                    if (mob.getId() == 9990007) {
                        toDamage = BigInteger.ZERO;
                    }
                }
                total += 1;

                if (toDamage.compareTo(BigInteger.ZERO) > 0 && !summonEffect.getMonsterStati().isEmpty()) {
                    if (summonEffect.makeChanceResult()) {
                        for (Map.Entry<MonsterStatus, Integer> z : summonEffect.getMonsterStati().entrySet()) {
                            mob.applyStatus(chr, new MonsterStatusEffect(z.getKey(), z.getValue(), summonSkill.getId(), null, false), summonEffect.isPoison(), 4000, true, summonEffect);
                        }
                    }
                }

            }
            if (toDamage.compareTo(BigInteger.ZERO) > 0 && mob.isAlive()) { //10 x dmg.. eh
                mob.superBigDamage(chr, toDamage, true, summonSkill.getId(), false);
                //mob.damage(chr, toDamage, true);
            }
        }
        if (!summon.isMultiAttack()) {
            chr.getMap().broadcastMessage(SummonPacket.removeSummon(summon, true));
            chr.getMap().removeMapObject(summon, MapleMapObjectType.SUMMON);
            chr.removeSummon(summon);
            if (summon.getSkill() != 35121011) {
                chr.cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
            }
        }
    }

    public static final void RemoveSummon(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        final MapleMapObject obj = c.getPlayer().getMap().getSummonByOid(slea.readInt());
        if ((MapleSummon) obj == null || !(obj instanceof MapleSummon)) {
            return;
        }
        final MapleSummon summon = (MapleSummon) obj;
        if (summon.getOwnerId() != c.getPlayer().getId() || summon.getSkillLevel() <= 0) {
            c.getPlayer().dropMessage(5, "Error.");
            return;
        }
        if (summon.getSkill() == 35111002 || summon.getSkill() == 35121010) { //rock n shock, amp
            return;
        }
        c.getPlayer().getMap().broadcastMessage(SummonPacket.removeSummon(summon, true));
        c.getPlayer().getMap().removeMapObject(summon, MapleMapObjectType.SUMMON);
        c.getPlayer().removeSummon(summon);
        if (summon.getSkill() != 35121011) {
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
            //TODO: Multi Summoning, must do something about hack buffstat
        }
    }

    public static final void SubSummon(final SeekableLittleEndianAccessor slea, final MapleCharacter chr) {
        final MapleMapObject obj = chr.getMap().getSummonByOid(slea.readInt());
        if ((MapleSummon) obj == null || !(obj instanceof MapleSummon)) {
            return;
        }
        final MapleSummon sum = (MapleSummon) obj;
        if (sum.getOwnerId() != chr.getId() || sum.getSkillLevel() <= 0 || !chr.isAlive()) {
            return;
        }
        switch (sum.getSkill()) {
            case 35121009:
                if (!chr.canSummon(2000)) {
                    return;
                }
                final int skillId = slea.readInt(); // 35121009?
                if (sum.getSkill() != skillId) {
                    return;
                }
                slea.skip(1); // 0E?
                chr.updateTick(slea.readInt());
                //for (int i = 0; i < 3; i++) {
                //    final MapleSummon tosummon = new MapleSummon(chr, SkillFactory.getSkill(35121011).getEffect(sum.getSkillLevel()), new Point(sum.getTruePosition().x, sum.getTruePosition().y - 5), SummonMovementType.WALK_STATIONARY);
                //    chr.getMap().spawnSummon(tosummon);
                //    chr.addSummon(skillId, tosummon);
                //}
                break;
            case 35111011: //healing
                if (!chr.canSummon(1000)) {
                    return;
                }
                chr.addHP((int) (chr.getStat().getCurrentMaxHp() * SkillFactory.getSkill(sum.getSkill()).getEffect(sum.getSkillLevel()).getHp() / 100.0));
                chr.getClient().announce(EffectPacket.showOwnBuffEffect(sum.getSkill(), 2, chr.getLevel(), sum.getSkillLevel()));
                chr.getMap().broadcastMessage(chr, EffectPacket.showBuffeffect(chr.getId(), sum.getSkill(), 2, chr.getLevel(), sum.getSkillLevel()), false);
                break;
            case 1321007: //beholder
                Skill bHealing = SkillFactory.getSkill(slea.readInt());
                final int bHealingLvl = chr.getTotalSkillLevel(bHealing);
                if (bHealingLvl <= 0 || bHealing == null) {
                    return;
                }
                final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
                if (bHealing.getId() == 1320009) {
                    healEffect.applyTo(chr);
                } else if (bHealing.getId() == 1320008) {
                    if (!chr.canSummon(healEffect.getX() * 1000)) {
                        return;
                    }
                    chr.addHP(healEffect.getHp());
                }
                chr.getClient().announce(EffectPacket.showOwnBuffEffect(sum.getSkill(), 2, chr.getLevel(), bHealingLvl));
                chr.getMap().broadcastMessage(SummonPacket.summonSkill(chr.getId(), sum.getSkill(), bHealing.getId() == 1320008 ? 5 : (Randomizer.nextInt(3) + 6)));
                chr.getMap().broadcastMessage(chr, EffectPacket.showBuffeffect(chr.getId(), sum.getSkill(), 2, chr.getLevel(), bHealingLvl), false);
                break;
        }
        if (GameConstants.isAngel(sum.getSkill())) {
            if (sum.getSkill() % 10000 == 1087) {
                MapleItemInformationProvider.getInstance().getItemEffect(2022747).applyTo(chr);
            } else if (sum.getSkill() % 10000 == 1179) {
                MapleItemInformationProvider.getInstance().getItemEffect(2022823).applyTo(chr);
            } else {
                MapleItemInformationProvider.getInstance().getItemEffect(2022746).applyTo(chr);
            }
            chr.getClient().announce(EffectPacket.showOwnBuffEffect(sum.getSkill(), 2, 2, 1));
            chr.getMap().broadcastMessage(chr, EffectPacket.showBuffeffect(chr.getId(), sum.getSkill(), 2, 2, 1), false);
        }
    }

    public static final void SummonPVP(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        final MapleCharacter chr = c.getPlayer();
        if (chr == null || chr.isHidden() || !chr.isAlive() || chr.hasBlockedInventory() || chr.getMap() == null || !chr.inPVP() || !chr.getEventInstance().getProperty("started").equals("1")) {
            return;
        }
        final MapleMap map = chr.getMap();
        final MapleMapObject obj = map.getSummonByOid(slea.readInt());
        if ((MapleSummon) obj == null || !(obj instanceof MapleSummon)) {
            chr.dropMessage(5, "The summon has disappeared.");
            return;
        }
        int tick = -1;
        if (slea.available() == 27) {
            slea.skip(23);
            tick = slea.readInt();
        }
        final MapleSummon summon = (MapleSummon) obj;
        if (summon.getOwnerId() != chr.getId() || summon.getSkillLevel() <= 0) {
            chr.dropMessage(5, "Error.");
            return;
        }
        final Skill skil = SkillFactory.getSkill(summon.getSkill());
        final MapleStatEffect effect = skil.getEffect(summon.getSkillLevel());
        final int lvl = Integer.parseInt(chr.getEventInstance().getProperty("lvl"));
        final int type = Integer.parseInt(chr.getEventInstance().getProperty("type"));
        final int ourScore = Integer.parseInt(chr.getEventInstance().getProperty(String.valueOf(chr.getId())));
        int addedScore = 0;
        final boolean magic = skil.isMagic();
        boolean killed = false, didAttack = false;
        double maxdamage = lvl == 3 ? chr.getStat().getCurrentMaxBasePVPDamageL() : chr.getStat().getCurrentMaxBasePVPDamage();
        maxdamage *= (effect.getDamage() + chr.getStat().getDamageIncrease(summon.getSkill())) / 100.0;
        int mobCount = 1, attackCount = 1;
        long ignoreDEF = chr.getStat().getPDR();

        final SummonSkillEntry sse = SkillFactory.getSummonData(summon.getSkill());
        if (summon.getSkill() / 1000000 != 35 && summon.getSkill() != 33101008 && sse == null) {
            chr.dropMessage(5, "Error in processing attack.");
            return;
        }
        Point lt, rb;
        if (sse != null) {
            if (sse.delay > 0) {
                if (tick != -1) {
                    summon.CheckSummonAttackFrequency(chr, tick);
                    chr.updateTick(tick);
                } else {
                    summon.CheckPVPSummonAttackFrequency(chr);
                }
                //chr.getCheatTracker().checkSummonAttack();
            }
            mobCount = sse.mobCount;
            attackCount = sse.attackCount;
            lt = sse.lt;
            rb = sse.rb;
        } else {
            lt = new Point(-100, -100);
            rb = new Point(100, 100);
        }
        final Rectangle box = MapleStatEffect.calculateBoundingBox(chr.getTruePosition(), chr.isFacingLeft(), lt, rb, 0);
        List<AttackPair> ourAttacks = new ArrayList<AttackPair>();
        List<Pair<Long, Boolean>> attacks;
        maxdamage *= chr.getStat().dam_r / 100.0;
        for (MapleMapObject mo : chr.getMap().getCharactersIntersect(box)) {
            final MapleCharacter attacked = (MapleCharacter) mo;
            if (attacked.getId() != chr.getId() && attacked.isAlive() && !attacked.isHidden() && (type == 0 || attacked.getTeam() != chr.getTeam())) {
                double rawDamage = maxdamage / Math.max(0, (Math.max(1.0, 100.0 - ignoreDEF) / 100.0) * (type == 3 ? 0.1 : 0.25));
                if (attacked.getBuffedValue(MapleBuffStat.INVINCIBILITY) != null || PlayersHandler.inArea(attacked)) {
                    rawDamage = 0;
                }
                rawDamage += (rawDamage * chr.getDamageIncrease(attacked.getId()) / 100.0);
                rawDamage *= attacked.getStat().mesoGuard / 100.0;
                rawDamage = attacked.modifyDamageTaken(rawDamage, attacked).left;
                final double min = (rawDamage * chr.getStat().trueMastery / 100);
                attacks = new ArrayList<Pair<Long, Boolean>>(attackCount);
                int totalMPLoss = 0, totalHPLoss = 0;
                for (int i = 0; i < attackCount; i++) {
                    int mploss = 0;
                    double ourDamage = Randomizer.nextInt((int) Math.abs(Math.round(rawDamage - min)) + 1) + min;
                    if (attacked.getStat().dodgeChance > 0 && Randomizer.nextInt(100) < attacked.getStat().dodgeChance) {
                        ourDamage = 0;
                        //i dont think level actually matters or it'd be too op
                        //} else if (attacked.getLevel() > chr.getLevel() && Randomizer.nextInt(100) < (attacked.getLevel() - chr.getLevel())) {
                        //	ourDamage = 0;
                    }
                    if (attacked.getBuffedValue(MapleBuffStat.MAGIC_GUARD) != null) {
                        mploss = (int) Math.min(attacked.getStat().getMp(), (ourDamage * attacked.getBuffedValue(MapleBuffStat.MAGIC_GUARD).doubleValue() / 100.0));
                    }
                    ourDamage -= mploss;
                    if (attacked.getBuffedValue(MapleBuffStat.INFINITY) != null) {
                        mploss = 0;
                    }
                    attacks.add(new Pair<Long, Boolean>((long) Math.floor(ourDamage), false));

                    totalHPLoss += Math.floor(ourDamage);
                    totalMPLoss += mploss;
                }
                attacked.addMPHP(-totalHPLoss, -totalMPLoss);
                ourAttacks.add(new AttackPair(attacked.getId(), attacked.getPosition(), attacks));
                //attacked.getCheatTracker().setAttacksWithoutHit(false);
                if (totalHPLoss > 0) {
                    didAttack = true;
                }
                if (attacked.getStat().getHPPercent() <= 20) {
                    SkillFactory.getSkill(attacked.getStat().getSkillByJob(93, attacked.getJob())).getEffect(1).applyTo(attacked);
                }
                if (effect != null) {
                    if (effect.getMonsterStati().size() > 0 && effect.makeChanceResult()) {
                        for (Map.Entry<MonsterStatus, Integer> z : effect.getMonsterStati().entrySet()) {
                            MapleDisease d = MonsterStatus.getLinkedDisease(z.getKey());
                            if (d != null) {
                                attacked.giveDebuff(d, z.getValue(), effect.getDuration(), d.getDisease(), 1);
                            }
                        }
                    }
                    effect.handleExtraPVP(chr, attacked);
                }
                chr.getClient().announce(CField.getPVPHPBar(attacked.getId(), attacked.getStat().getHp(), attacked.getStat().getCurrentMaxHp()));
                addedScore += (totalHPLoss / 100) + (totalMPLoss / 100); //ive NO idea
                if (!attacked.isAlive()) {
                    killed = true;
                }

                if (ourAttacks.size() >= mobCount) {
                    break;
                }
            }
        }
        if (killed || addedScore > 0) {
            chr.getEventInstance().addPVPScore(chr, addedScore);
            chr.getClient().announce(CField.getPVPScore(ourScore + addedScore, killed));
        }
        if (didAttack) {
            chr.getMap().broadcastMessage(SummonPacket.pvpSummonAttack(chr.getId(), chr.getLevel(), summon.getObjectId(), summon.isFacingLeft() ? 4 : 0x84, summon.getTruePosition(), ourAttacks));
            if (!summon.isMultiAttack()) {
                chr.getMap().broadcastMessage(SummonPacket.removeSummon(summon, true));
                chr.getMap().removeMapObject(summon, MapleMapObjectType.SUMMON);
                chr.removeSummon(summon);
                if (summon.getSkill() != 35121011) {
                    chr.cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
                }
            }
        }
    }
}
