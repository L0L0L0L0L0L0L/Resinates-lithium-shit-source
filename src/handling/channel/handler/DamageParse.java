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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import client.Skill;
import constants.GameConstants;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Map;
import server.MapleStatEffect;
import server.Randomizer;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.maps.MapleMap;
import tools.AttackPair;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CWvsContext;

public class DamageParse {

    public static int lastAttack = 0;
    //public static long damagecap = 9000000000000000000L;

    public static boolean isFA(int id) {
        return switch (id) {
            case 2000, 22150004, 30002000, 30012000 ->
                true;
            default ->
                false;
        };
    }

    public static void applyAttack(final AttackInfo attack, final Skill theSkill, final MapleCharacter player, int attackCount, final double maxDamagePerMonster, final MapleStatEffect effect, final AttackType attack_type) {
        if (attack.skill != 0) {
            if (effect == null) {
                player.getClient().announce(CWvsContext.enableActions());
                return;
            }
            if (GameConstants.isMulungSkill(attack.skill)) {
                if (player.getMapId() / 10000 != 92502) {
                    //AutobanManager.getInstance().autoban(player.getClient(), "Using Mu Lung dojo skill out of dojo maps.");
                    return;
                } else {
                    if (player.getMulungEnergy() < 10000) {
                        return;
                    }
                    player.mulung_EnergyModify(false);
                }
            } else if (GameConstants.isPyramidSkill(attack.skill)) {
                if (player.getMapId() / 1000000 != 926) {
                    //AutobanManager.getInstance().autoban(player.getClient(), "Using Pyramid skill outside of pyramid maps.");
                    return;
                } else {
                    if (player.getPyramidSubway() == null || !player.getPyramidSubway().onSkillUse(player)) {
                        return;
                    }
                }
            } else if (GameConstants.isInflationSkill(attack.skill)) {
                if (player.getBuffedValue(MapleBuffStat.GIANT_POTION) == null) {
                    return;
                }
            } else if (attack.targets > effect.getMobCount() && attack.skill != 1211002 && attack.skill != 1220010) { // Must be done here, since NPE with normal atk
                System.out.println(player.getName() + " is using wz edits for extra mob hits!");
                player.kick();
                return;
            }
        }
        //final boolean useAttackCount = attack.skill != 4211006 && attack.skill != 23121003 && (attack.skill != 1311001 || player.getJob() != 132) && attack.skill != 3211006;
        final MapleMap map = player.getMap();
        /*
        if (attack.skill == 4211006) { // meso explosion
            for (AttackPair oned : attack.allBigDamage) {
                if (oned.attack != null) {
                    continue;
                }
                final MapleMapObject mapobject = map.getMapObject(oned.objectid);

                if (mapobject != null) {
                    final MapleMapItem mapitem = (MapleMapItem) mapobject;
                    mapitem.getLock().lock();
                    try {
                        if (mapitem.getMeso() > 0) {
                            if (mapitem.isPickedUp()) {
                                return;
                            }
                            map.removeMapObject(mapitem, MapleMapObjectType.ITEM);
                            map.broadcastMessage(CField.explodeDrop(mapitem.getObjectId()));
                            mapitem.setPickedUp(true);
                        } else {
                            //player.getCheatTracker().registerOffense(CheatingOffense.ETC_EXPLOSION);
                            return;
                        }
                    } finally {
                        mapitem.getLock().unlock();
                    }
                } else {
                    //player.getCheatTracker().registerOffense(CheatingOffense.EXPLODING_NONEXISTANT);
                    return; // etc explosion, exploding nonexistant things, etc.
                }
            }
        }
         */

        final Skill eaterSkill = SkillFactory.getSkill(GameConstants.getMPEaterForJob(player.getJob()));
        final int eaterLevel = player.getTotalSkillLevel(eaterSkill);
        long total = 0;
        int pdamage = 0;
        int lines = 0;
        boolean fixed = false;
        boolean atk = false;
        for (final AttackPair oned : attack.allBigDamage) {
            MapleMonster monster = map.getMonsterByOid(oned.objectid);
            if (monster != null && monster.getLinkCID() <= 0 && !monster.isDead() && !monster.isDead()) {
                //totDamageToOneMonster = 0;
                MapleMonsterStats monsterstats = monster.getStats();
                BigInteger eachd = BigInteger.ZERO;
                boolean guard = monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY) || monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT) || monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY);
                boolean reflect = monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT);
                BigInteger damz = BigInteger.ZERO;
                for (Pair<BigInteger, Boolean> eachde : oned.attacks) {
                    if (eachde.left.compareTo(BigInteger.ZERO) > 0) {
                        if (guard) {
                            if (reflect) { //test
                                lines += 1;
                            }
                            eachd = BigInteger.ONE;
                        } else {
                            if (monster.getId() == 9990007) {
                                eachd = BigInteger.ZERO;
                            } else {
                                eachd = eachde.left;
                            }
                            total += 1;
                            if (monsterstats.getOnlyNoramlAttack()) {
                                eachd = BigInteger.ZERO;
                            }
                        }
                        damz = damz.add(eachd);
                    }
                }
                int check = damz.compareTo(BigInteger.ZERO);
                if (check > 0) {
                    if (eaterLevel > 0) {
                        eaterSkill.getEffect(eaterLevel).applyPassive(player, monster);
                    }
                    if (player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null) {
                        handlePickPocket(player, monster);
                    }
                    if (GameConstants.isDemon(player.getJob())) {
                        player.handleForceGain(monster.getObjectId(), attack.skill);
                    }
                    if (player.getBuffedValue(MapleBuffStat.BLIND) != null) {
                        final MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.BLIND);

                        if (eff != null && eff.makeChanceResult()) {
                            final MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.ACC, eff.getX(), eff.getSourceId(), null, false);
                            monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 1000, true, eff);
                        }

                    }
                    if (player.getJob() == 121 || player.getJob() == 122) { // WHITEKNIGHT
                        final Skill skill = SkillFactory.getSkill(1211006);
                        if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, skill)) {
                            final MapleStatEffect eff = skill.getEffect(player.getTotalSkillLevel(skill));
                            final MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.FREEZE, 1, skill.getId(), null, false);
                            monster.applyStatus(player, monsterStatusEffect, false, eff.getY(), true, eff);
                        }
                    }
                    if (effect != null && !effect.getMonsterStati().isEmpty()) {
                        if (effect.makeChanceResult()) {
                            for (Map.Entry<MonsterStatus, Integer> z : effect.getMonsterStati().entrySet()) {
                                monster.applyStatus(player, new MonsterStatusEffect(z.getKey(), z.getValue(), theSkill.getId(), null, false), effect.isPoison(), effect.getDuration(), true, effect);
                            }
                        }
                    }
                    checkDamage(player, damz);
                    monster.superBigDamage(player, damz, true, attack.skill, false);
                    if (monster.isAlive()) {
                        player.checkMonsterAggro(monster);
                    }
                    atk = true;
                }
            }
        }
        if (atk) {
            player.setAtkCooldown(30);
            player.onAttack(attack);
            if (lines > 0 && !player.isInvincible()) {
                pdamage = (int) (player.getStat().getMaxHp() * (lines * 0.01));
                player.addMPHP(-pdamage, 0);
                player.getMap().broadcastMessage(player, CWvsContext.damagePlayer(player, pdamage), true);
            }
            if (total > 0 && !fixed) {
                player.afterAttack(attack.targets, attack.hits, attack.skill);
            }
            if (effect != null) {
                effect.applyTo(player, attack.position);
            }
        }
    }

    public static final void applyAttackMagic(final AttackInfo attack, final Skill theSkill, final MapleCharacter player, final MapleStatEffect effect, double maxDamagePerHit) {
        if (GameConstants.isMulungSkill(attack.skill)) {
            if (player.getMapId() / 10000 != 92502) {
                //AutobanManager.getInstance().autoban(player.getClient(), "Using Mu Lung dojo skill out of dojo maps.");
                return;
            } else {
                if (player.getMulungEnergy() < 10000) {
                    return;
                }
                player.mulung_EnergyModify(false);
            }
        } else if (GameConstants.isPyramidSkill(attack.skill)) {
            if (player.getMapId() / 1000000 != 926) {
                //AutobanManager.getInstance().autoban(player.getClient(), "Using Pyramid skill outside of pyramid maps.");
                return;
            } else {
                if (player.getPyramidSubway() == null || !player.getPyramidSubway().onSkillUse(player)) {
                    return;
                }
            }
        } else if (GameConstants.isInflationSkill(attack.skill)) {
            if (player.getBuffedValue(MapleBuffStat.GIANT_POTION) == null) {
                return;
            }
        }

        final Skill eaterSkill = SkillFactory.getSkill(GameConstants.getMPEaterForJob(player.getJob()));
        final int eaterLevel = player.getTotalSkillLevel(eaterSkill);

        final MapleMap map = player.getMap();
        boolean atk = false;
        int lines = 0;
        int pdamage = 0;
        long total = 0;
        boolean fixed = false;

        for (final AttackPair oned : attack.allBigDamage) {
            final MapleMonster monster = map.getMonsterByOid(oned.objectid);
            if (monster != null && monster.getLinkCID() <= 0 && !monster.isDead() && !monster.isDead()) {
                MapleMonsterStats monsterstats = monster.getStats();
                BigInteger eachd = BigInteger.ZERO;
                boolean guard = monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY) || monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT) || monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY);
                boolean reflect = monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT);
                BigInteger damz = BigInteger.ZERO;
                for (Pair<BigInteger, Boolean> eachde : oned.attacks) {
                    if (eachde.left.compareTo(BigInteger.ZERO) > 0) {
                        if (guard) {
                            if (reflect) { //test
                                lines += 1;
                            }
                            eachd = BigInteger.ONE;
                        } else {
                            if (monster.getId() == 9990007) {
                                eachd = BigInteger.ZERO;
                            } else {
                                eachd = eachde.left;
                            }
                            total += 1;
                            if (monsterstats.getOnlyNoramlAttack()) {
                                eachd = BigInteger.ZERO;
                            }
                        }
                        damz = damz.add(eachd);
                    }
                }
                int check = damz.compareTo(BigInteger.valueOf(0));
                if (check > 0) {
                    if (GameConstants.isDemon(player.getJob())) {
                        player.handleForceGain(monster.getObjectId(), attack.skill);
                    }
                    if (player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null) {
                        handlePickPocket(player, monster);
                    }
                    if (eaterLevel > 0) {
                        eaterSkill.getEffect(eaterLevel).applyPassive(player, monster);
                    }
                    if (player.getBuffedValue(MapleBuffStat.SLOW) != null) {
                        final MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.SLOW);

                        if (eff != null && eff.makeChanceResult() && !monster.isBuffed(MonsterStatus.SPEED)) {
                            monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.SPEED, eff.getX(), eff.getSourceId(), null, false), false, eff.getY() * 1000, true, eff);
                        }
                    }
                    // effects, reversed after bigbang
                    if (effect != null) {
                        if (!effect.getMonsterStati().isEmpty()) {
                            if (effect.makeChanceResult()) {
                                for (Map.Entry<MonsterStatus, Integer> z : effect.getMonsterStati().entrySet()) {
                                    monster.applyStatus(player, new MonsterStatusEffect(z.getKey(), z.getValue(), theSkill.getId(), null, false), effect.isPoison(), effect.getDuration(), true, effect);
                                }
                            }
                        }
                    }
                    checkDamage(player, damz);
                    monster.superBigDamage(player, damz, true, attack.skill, false);
                    if (monster.isAlive()) {
                        player.checkMonsterAggro(monster);
                    }
                    atk = true;
                }
            }
        }
        if (lines > 0 && !player.isInvincible()) {
            pdamage = (int) (player.getStat().getMaxHp() * (lines * 0.01));
            player.addMPHP(-pdamage, 0);
            player.getMap().broadcastMessage(player, CWvsContext.damagePlayer(player, pdamage), true);
        }
        if (atk) {
            player.onAttack(attack);
            player.setAtkCooldown(30);
            if (total > 0 && !fixed) {
                player.afterAttack(attack.targets, attack.hits, attack.skill);
            }
            if (effect != null && attack.skill != 2301002) {
                effect.applyTo(player);
            }

        }
    }

    public static void checkDamage(MapleCharacter player, BigInteger value) {
        if (value.compareTo(BigInteger.valueOf(10000L)) > 0) {
            player.finishAchievement(26);
        }
        if (value.compareTo(BigInteger.valueOf(50000L)) > 0) {
            player.finishAchievement(27);
        }
        if (value.compareTo(BigInteger.valueOf(100000L)) > 0) {
            player.finishAchievement(28);
        }
        if (value.compareTo(BigInteger.valueOf(500000L)) > 0) {
            player.finishAchievement(29);
        }
        if (value.compareTo(BigInteger.valueOf(1000000L)) > 0) {
            player.finishAchievement(30);
        }
        if (value.compareTo(BigInteger.valueOf(1000000000L)) > 0) {
            player.finishAchievement(76);
        }
        if (value.compareTo(BigInteger.valueOf(2000000000L)) > 0) {
            player.finishAchievement(77);
        }
        if (value.compareTo(BigInteger.valueOf(5000000000L)) > 0) {
            player.finishAchievement(303);
        }
        if (value.compareTo(BigInteger.valueOf(10000000000L)) > 0) {
            player.finishAchievement(304);
        }
        if (value.compareTo(BigInteger.valueOf(1000000000000L)) > 0) {
            player.finishAchievement(305);
        }
        if (value.compareTo(BigInteger.valueOf(1000000000000000L)) > 0) {
            player.finishAchievement(306);
        }
        if (value.compareTo(BigInteger.valueOf(1000000000000000000L)) > 0) {
            player.finishAchievement(307);
        }
    }

    private static void handlePickPocket(final MapleCharacter player, final MapleMonster mob) {
        final int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET);
        if (player.getStat().pickRate >= 100 || Randomizer.nextInt(99) < player.getStat().pickRate) {
            //player.getMap().spawnMesoDrop(Randomizer.random(maxmeso, maxmeso * 2), new Point((int) (mob.getTruePosition().getX() + Randomizer.nextInt(100) - 50), (int) (mob.getTruePosition().getY())), mob, player, false, (byte) 0);
            player.addBank(maxmeso, false);
        }
    }

    public static final AttackInfo DivideAttack(final AttackInfo attack, final int rate) {
        attack.real = false;
        if (rate <= 1) {
            return attack; //lol
        }
        for (AttackPair p : attack.allDamage) {
            if (p.attack != null) {
                for (Pair<Long, Boolean> eachd : p.attack) {
                    eachd.left /= rate; //too ex.
                }
            }
        }
        return attack;
    }

    public static final List<Pair<Long, Boolean>> Modify_AttackCrit(final List<Pair<Long, Boolean>> attack, final MapleCharacter chr, final int type, final MapleStatEffect effect, final Skill skill, final MapleMonster mob) {
        if (skill.getId() != 4211006 && skill.getId() != 3211003 && skill.getId() != 4111004) { //blizz + shadow meso + m.e no crits
            final int CriticalRate = chr.getStat().passive_sharpeye_rate() + (effect == null ? 0 : effect.getCr());
            for (Pair<Long, Boolean> eachd : attack) {
                if (!eachd.right) {
                    if (Randomizer.nextInt(100) < CriticalRate) {
                        if (eachd.left > 1) {
                            long critDam = (long) (eachd.left * (1 + (chr.getStat().passive_sharpeye_percent() / 100.0)));
                            eachd.left = Randomizer.MinMaxLong(critDam, 1, mob.getDamageCap());
                        }
                        eachd.right = true;
                    }
                }
            }
        }
        return attack;
    }

    public static final List<Pair<BigInteger, Boolean>> Modify_AttackBigCrit(final List<Pair<BigInteger, Boolean>> attack, final MapleCharacter chr, final int type, final MapleStatEffect effect, final Skill skill, final MapleMonster mob) {
        if (skill.getId() != 4211006 && skill.getId() != 3211003 && skill.getId() != 4111004) { //blizz + shadow meso + m.e no crits
            final int CriticalRate = chr.getStat().passive_sharpeye_rate() + (effect == null ? 0 : effect.getCr());
            for (Pair<BigInteger, Boolean> eachd : attack) {
                if (!eachd.right) {
                    if (Randomizer.nextInt(100) < CriticalRate) {
                        if (eachd.left.compareTo(BigInteger.ONE) > 0) {
                            double critDam = 1 + (chr.getStat().passive_sharpeye_percent() / 100.0);
                            if (mob.getStats().getCapped()) {
                                //eachd.left = new BigDecimal(eachd.left).multiply(BigDecimal.valueOf(critDam)).min(new BigDecimal(mob.getStats().damageCap).divide(BigDecimal.valueOf(attack.size()), 0, RoundingMode.UP)).toBigInteger();
                                eachd.left = new BigDecimal(eachd.left).multiply(BigDecimal.valueOf(critDam)).min(new BigDecimal(mob.getStats().damageCap)).max(BigDecimal.ONE).toBigInteger();
                            } else {
                                eachd.left = new BigDecimal(eachd.left).multiply(BigDecimal.valueOf(critDam)).toBigInteger();
                            }
                        }
                        eachd.right = true;

                    }
                }
            }
        }
        return attack;
    }

    public static BigDecimal getBaseDamage(MapleCharacter chr) {
        double opstat = chr.getOPStat() * chr.totalMasteryBonus();
        double combo = 1.0;
        double ipots = 1.0;
        if (chr.getCombo() > 0) {
            combo = Randomizer.DoubleMax(1 + (chr.getCombo() * 0.001), chr.getMaxCombo());
        }
        long ipot = chr.getAccVara("I_POT");
        if (ipot > 0) {
            ipots = (1 + (Math.log10(ipot) + (ipot * 0.01)));
        }
        double totem = chr.getMap().getTotemType(chr);
        double sLevel = chr.getSuperLevelBonus();
        return BigDecimal.valueOf(opstat * ipots * combo * totem * chr.getFullJobBonus() * sLevel * GameConstants.getStatRate());
    }

    public static BigInteger getDamage(MapleCharacter chr, MapleMonster mob, double count, BigDecimal base) {
        if (mob != null || !mob.isDead()) {
            double armor = 1 - (mob.getStats().getArmor() * 0.01);
            double tdiff = (double) chr.getStat().getPDR() / (double) (mob.getStats().getDef());
            double atkdiff = tdiff < 1.0 ? Math.pow(tdiff, 5) : Randomizer.DoubleMax(tdiff, 2);
            double total = armor * atkdiff * count;
            if (mob.getStats().isBoss()) {
                return base.multiply(BigDecimal.valueOf(total * chr.getStat().getBossDmgR())).toBigInteger();
            } else {
                return base.multiply(BigDecimal.valueOf(total * chr.getStat().getDmgR())).toBigInteger();
            }
        } else {
            return BigInteger.ZERO;
        }
    }

    public static final AttackInfo parseDmgMa(final SeekableLittleEndianAccessor lea, final MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.isChangingMaps()) {
            return null;
        }
        final AttackInfo ret = new AttackInfo();
        lea.skip(1);
        Point mobPos = null;
        Point chrPos = chr.getPosition();
        ret.tbyte = lea.readByte();
        List<Point> points = new LinkedList<>();
        //System.out.println("TBYTE: " + tbyte);
        ret.targets = (byte) ((ret.tbyte >>> 4) & 0xF);
        ret.hits = (byte) (ret.tbyte & 0xF);
        ret.skill = lea.readInt();
        //System.out.println("skill id" + ret.skill);
        if (ret.skill >= 91000000) { //guild/recipe? no
            return null;
        }
        lea.skip(GameConstants.GMS ? 9 : 17); // ORDER [1] byte on bigbang, [4] bytes on v.79, [4] bytes on v.80, [1] byte on v.82
        //ret.charge = -1;

        if (GameConstants.isMagicChargeSkill(ret.skill)) {
            ret.charge = lea.readInt();
        } else {
            ret.charge = -1;
        }

        ret.unk = lea.readByte();
        ret.display = lea.readShort();
        ret.direction = (ret.display >>> 15) & 0x1;
        lea.skip(4); //big bang
        lea.skip(1); // Weapon class
        ret.speed = lea.readByte(); // Confirmed
        int delayAtk = lea.readInt();
        lea.skip(4); //0
        ret.allDamage = new ArrayList<AttackPair>();
        ret.allBigDamage = new ArrayList<AttackPair>();

        double skilldam = 0.1;
        final Skill skillz = SkillFactory.getSkill(ret.skill);
        if (skillz != null) {
            ret.baseSkill = skillz;
            ret.linkSkill = SkillFactory.getSkill(GameConstants.getLinkedAranSkill(ret.skill));
            ret.delay = skillz.getAttackDelay();
            ret.attack_speed = skillz.getSpeed();
            ret.attack_delay = skillz.getLineDelay();
            if (!isFA(ret.skill)) {
                chr.attacks++;
                if (chr.getAttacks() > skillz.getSpeed()) {
                    chr.attacks = 0;
                    return null;
                }
            }
            ret.level = chr.getSkillLevel(ret.skill);
            MapleStatEffect info = ret.getAttackEffect(chr, ret.level, skillz);
            if (info != null) {
                skilldam = (getSkillDamage(info, skillz, chr));
            }
        }
        List<Pair<Long, Boolean>> allDamageNumbers;
        List<Pair<BigInteger, Boolean>> allBigDamageNumbers;
        //chr.getStat().getMastery()
        double mastery = 10;
        if (chr.getStat().getMastery() > 0) {
            mastery = 10 / (10 * (chr.getStat().getMastery() / 100.0));
        }
        Pair<Integer, Integer> getAttack = GameConstants.getAttack(ret.skill);
        ret.atks = 1;
        double multi = getAttack.getLeft();
        ret.lines = getAttack.getRight();

        BigDecimal OPStat = DamageParse.getBaseDamage(chr).multiply(BigDecimal.valueOf(skilldam));
        int combo = 0;
        for (int i = 0; i < ret.targets; i++) {
            int oid = lea.readInt();

            MapleMonster mob = chr.getMap().getMonsterByOid(oid);
            if (mob != null) {
                if (mob.isDead() || mob.isDead() || mob.teleport) {
                    continue;
                }
                long curr = System.currentTimeMillis();
                if (mob.spawning > curr) {
                    continue;
                }
                if (ret.skill == 2301002 && !mob.getStats().getUndead()) {
                    continue;
                }
                lea.skip(4);
                //lea.skip(12); // byte byte byte byte pos(client) pos(server)
                mobPos = lea.readPos();
                lea.skip(4);
                short delay = lea.readShort();
                BigInteger bigDam = BigInteger.ONE;

                mob.setAtkDelay(delay);
                if (!chr.tagged) {
                    bigDam = getDamage(chr, mob, multi, OPStat);
                }
                for (int j = 0; j < ret.hits; j++) {
                    lea.skip(4);
                }
                allDamageNumbers = new ArrayList<Pair<Long, Boolean>>();
                allBigDamageNumbers = new ArrayList<Pair<BigInteger, Boolean>>();
                for (int k = 0; k < 1; k++) {
                    for (int j = 0; j < ret.lines; j++) {
                        //long damage = lea.readInt();
                        double master = 1 + (Randomizer.randomDouble(-mastery, 5) * 0.01);
                        BigInteger bigRand = new BigDecimal(bigDam).multiply(BigDecimal.valueOf(master)).toBigInteger().max(BigInteger.ONE);
                        if (mob != null) {
                            if (!mob.isBuffed(MonsterStatus.MAGIC_IMMUNITY) && !mob.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT) && !mob.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) {
                                if (mob.getStats().getCapped()) {
                                    //bigRand = bigRand.min(new BigDecimal(mob.getStats().damageCap).divide(BigDecimal.valueOf(ret.lines), 0, RoundingMode.UP).toBigInteger());
                                    bigRand = bigRand.min(new BigDecimal(mob.getStats().damageCap).toBigInteger());
                                    if (bigRand.compareTo(BigInteger.ONE) < 0) {
                                        bigRand = BigInteger.ONE;
                                    }
                                }
                            } else {
                                bigRand = BigInteger.ONE;
                            }
                        }
                        allBigDamageNumbers.add(new Pair<BigInteger, Boolean>(bigRand, false)); //m.e. never crits
                    }
                }
                lea.skip(4); // CRC of monster [Wz Editing]
                if (mob != null && skillz != null) {
                    allBigDamageNumbers = Modify_AttackBigCrit(allBigDamageNumbers, chr, 1, ret.getAttackEffect(chr, ret.level, ret.baseSkill), ret.baseSkill, mob);
                }
                ret.allBigDamage.add(new AttackPair(Integer.valueOf(oid).intValue(), allBigDamageNumbers, true));
                combo++;
            }
        }
        if (chr.canCombo && combo > 0) {
            PlayerHandler.AranCombo(chr.getClient(), chr, combo);
        }
        ret.targets = (byte) ret.allBigDamage.size();
        return ret;
    }

    //melee-misc
    public static final AttackInfo parseDmgM(final SeekableLittleEndianAccessor lea, final MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.isChangingMaps()) {
            return null;
        }
        final AttackInfo ret = new AttackInfo();
        lea.skip(1);
        ret.tbyte = lea.readByte();
        ret.targets = (byte) ((ret.tbyte >>> 4) & 0xF);
        ret.hits = (byte) (ret.tbyte & 0xF);
        ret.skill = lea.readInt();
        if (ret.skill >= 91000000) { //guild/recipe? no
            return null;
        }
        if (ret.skill == 5301001) {
            return null;
        }
        Point mobPos = null;
        Point chrPos = chr.getPosition();
        lea.skip(9); // ORDER [1] byte, [4] bytes, [4] bytes
        List<Point> points = new LinkedList<>();
        double mastery = 10;
        if (chr.getStat().getMastery() > 0) {
            mastery = 10 / (10 * (chr.getStat().getMastery() / 100.0));
        }
        long damagecap = chr.getDamageCap();
        Pair<Integer, Integer> getAttack = GameConstants.getAttack(ret.skill);
        switch (ret.skill) {
            case 11101007: // Power Reflection
            case 11101006: // Dawn Warrior - Power Reflection
            case 21101003: // body pressure
            case 2111007:// tele mastery skills
            case 2211007:
            case 12111007:
            case 22161005:
            case 32111010:
            case 2311007: // bishop tele mastery
                lea.skip(1); // charge = 0
                ret.charge = 0;
                ret.display = lea.readShort();
                ret.direction = (ret.display >>> 15) & 0x1;
                lea.skip(4);// dunno
                ret.speed = (byte) lea.readShort();
                int attackTime = lea.readInt();
                chr.lastAttackDelay = attackTime - chr.lastAttack;
                chr.lastAttack = attackTime;
                ret.lastAttackTickCount = attackTime;
                lea.skip(4);// looks like zeroes
                ret.allDamage = new ArrayList();
                ret.allBigDamage = new ArrayList<AttackPair>();
                double skilldam = 0.1;
                final Skill skillz = SkillFactory.getSkill(ret.skill);
                if (skillz != null) {
                    ret.baseSkill = skillz;
                    ret.linkSkill = SkillFactory.getSkill(GameConstants.getLinkedAranSkill(ret.skill));
                    ret.delay = skillz.getAttackDelay();
                    ret.attack_speed = skillz.getSpeed();
                    ret.attack_delay = skillz.getLineDelay();
                    if (!isFA(ret.skill)) {
                        chr.attacks++;
                        if (chr.getAttacks() > skillz.getSpeed()) {
                            chr.attacks = 0;
                            return null;
                        }
                    }
                    ret.level = chr.getSkillLevel(ret.skill);
                    MapleStatEffect info = ret.getAttackEffect(chr, ret.level, skillz);
                    if (info != null) {
                        skilldam = (getSkillDamage(info, skillz, chr));
                    }
                }
                BigDecimal OPStat = DamageParse.getBaseDamage(chr).multiply(BigDecimal.valueOf(skilldam));
                List<Pair<Long, Boolean>> allDamageNumbers;
                List<Pair<BigInteger, Boolean>> allBigDamageNumbers;
                ret.atks = 1;
                double multi = getAttack.getLeft();
                ret.lines = getAttack.getRight();
                int combo = 0;
                for (int i = 0; i < ret.targets; i++) {
                    int oid = lea.readInt();
                    MapleMonster mob = chr.getMap().getMonsterByOid(oid);
                    if (mob != null) {
                        if (mob.isDead() || mob.isDead() || mob.teleport) {
                            continue;
                        }
                        long curr = System.currentTimeMillis();
                        if (mob.spawning > curr) {
                            continue;
                        }
                        lea.skip(4);
                        //lea.skip(12); // byte byte byte byte pos(client) pos(server)
                        mobPos = lea.readPos();
                        lea.skip(4);
                        short delay = lea.readShort();
                        BigInteger bigDam = BigInteger.ONE;

                        mob.setAtkDelay(delay);
                        if (!chr.tagged) {
                            bigDam = getDamage(chr, mob, multi, OPStat);
                        }

                        for (int j = 0; j < ret.hits; j++) {
                            lea.skip(4);
                        }
                        allDamageNumbers = new ArrayList<Pair<Long, Boolean>>();
                        allBigDamageNumbers = new ArrayList<Pair<BigInteger, Boolean>>();
                        ret.atks = 1;
                        for (int k = 0; k < 1; k++) {
                            for (int j = 0; j < ret.lines; j++) {
                                //long damage = lea.readInt();
                                double master = 1 + (Randomizer.randomDouble(-mastery, 5) * 0.01);
                                BigInteger bigRand = new BigDecimal(bigDam).multiply(BigDecimal.valueOf(master)).toBigInteger().max(BigInteger.ONE);
                                if (mob != null) {
                                    if (!mob.isBuffed(MonsterStatus.WEAPON_IMMUNITY) && !mob.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT) && !mob.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) {
                                        if (mob.getStats().getCapped()) {
                                            //bigRand = bigRand.min(new BigDecimal(mob.getStats().damageCap).divide(BigDecimal.valueOf(ret.lines), 0, RoundingMode.UP).toBigInteger());
                                            bigRand = bigRand.min(new BigDecimal(mob.getStats().damageCap).toBigInteger());
                                            if (bigRand.compareTo(BigInteger.ONE) < 0) {
                                                bigRand = BigInteger.ONE;
                                            }
                                        }
                                    } else {
                                        bigRand = BigInteger.ONE;
                                    }
                                }
                                allBigDamageNumbers.add(new Pair<BigInteger, Boolean>(bigRand, false)); //m.e. never crits
                            }
                        }
                        lea.skip(4); // CRC of monster [Wz Editing]
                        if (mob != null && skillz != null) {
                            allBigDamageNumbers = Modify_AttackBigCrit(allBigDamageNumbers, chr, 1, ret.getAttackEffect(chr, ret.level, ret.baseSkill), ret.baseSkill, mob);
                        }
                        ret.allBigDamage.add(new AttackPair(Integer.valueOf(oid).intValue(), allBigDamageNumbers, true));
                        combo++;
                    }
                }
                ret.targets = (byte) ret.allBigDamage.size();
                if (chr.canCombo && combo > 0) {
                    PlayerHandler.AranCombo(chr.getClient(), chr, combo);
                }
                return ret;
            case 4341002:
            case 4341003:
            case 5101004: // Corkscrew
            case 5201002: // Gernard
            case 5301001:
            case 5300007:
            case 15101003: // Cygnus corkscrew
            case 14111006: // Poison bomb
            case 31001000: // grim scythe
            case 31101000: // soul eater
            case 31111005: // carrion breath
                //case 22121000:
                //case 22151001:
                ret.charge = lea.readInt();
                //System.out.println(ret.charge);
                break;
            default:
                ret.charge = -1;
                break;
        }
        ret.unk = lea.readByte();
        ret.display = lea.readShort();
        ret.direction = (ret.display >>> 15) & 0x1;
        if (GameConstants.isDemon(ret.skill / 10000) && (ret.skill / 1000) % 10 == 0) { //passive skill
            lea.skip(1); //1, 2, 3, or 4; which hit in the combo
        }
        lea.skip(4); //big bang
        lea.skip(1); // Weapon class
        ret.speed = lea.readByte(); // Confirmed
        //int delayAtk = lea.readInt();
        //lastAttack = delayAtk; // Ticks

        int attackTime = lea.readInt();
        chr.lastAttackDelay = attackTime - chr.lastAttack;
        chr.lastAttack = attackTime;
        ret.lastAttackTickCount = attackTime;
        lastAttack = attackTime;

        lea.skip(4); //0

        ret.allDamage = new ArrayList<AttackPair>();
        ret.allBigDamage = new ArrayList<AttackPair>();

        //if (ret.skill == 4211006) { // Meso Explosion
        //    return parseMesoExplosion(lea, ret, chr);
        //}
        List<Pair<Long, Boolean>> allDamageNumbers;
        List<Pair<BigInteger, Boolean>> allBigDamageNumbers;

        double skilldam = 0.1;

        final Skill skillz = SkillFactory.getSkill(ret.skill);
        if (skillz != null) {
            ret.baseSkill = skillz;
            ret.linkSkill = SkillFactory.getSkill(GameConstants.getLinkedAranSkill(ret.skill));
            ret.delay = skillz.getAttackDelay();
            ret.attack_speed = skillz.getSpeed();
            ret.attack_delay = skillz.getLineDelay();
            if (!isFA(ret.skill)) {
                chr.attacks++;
                if (chr.getAttacks() > skillz.getSpeed()) {
                    chr.attacks = 0;
                    return null;
                }
            }
            ret.level = chr.getSkillLevel(ret.linkSkill);
            MapleStatEffect info = ret.getAttackEffect(chr, ret.level, skillz);
            if (info != null) {
                //ret.targets = info.getMobCount();
                skilldam = (getSkillDamage(info, skillz, chr));
            }
        }

        BigDecimal OPStat = DamageParse.getBaseDamage(chr).multiply(BigDecimal.valueOf(skilldam));
        ret.atks = 1;
        double multi = getAttack.getLeft();
        ret.lines = getAttack.getRight();
        int combo = 0;
        for (int i = 0; i < ret.targets; i++) {
            int oid = lea.readInt();
            MapleMonster mob = chr.getMap().getMonsterByOid(oid);
            if (mob != null) {
                if (mob.isDead() || mob.isDead() || mob.teleport) {
                    continue;
                }
                long curr = System.currentTimeMillis();
                if (mob.spawning > curr) {
                    continue;
                }
                lea.skip(4);
                //lea.skip(12); // byte byte byte byte pos(client) pos(server)
                mobPos = lea.readPos();
                lea.skip(4);
                short delay = lea.readShort();
                BigInteger bigDam = BigInteger.ONE;
                mob.setAtkDelay(delay);
                if (!chr.tagged) {
                    bigDam = getDamage(chr, mob, multi, OPStat);
                }

                for (int j = 0; j < ret.hits; j++) {
                    lea.skip(4);
                }
                //allDamageNumbers = new ArrayList<Pair<Long, Boolean>>();
                allBigDamageNumbers = new ArrayList<Pair<BigInteger, Boolean>>();
                ret.atks = 1;
                for (int k = 0; k < 1; k++) {
                    for (int j = 0; j < ret.lines; j++) {
                        //long damage = lea.readInt();
                        double master = 1 + (Randomizer.randomDouble(-mastery, 5) * 0.01);
                        BigInteger bigRand = new BigDecimal(bigDam).multiply(BigDecimal.valueOf(master)).toBigInteger().max(BigInteger.ONE);
                        if (mob != null) {
                            if (!mob.isBuffed(MonsterStatus.WEAPON_IMMUNITY) && !mob.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT) && !mob.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) {
                                if (mob.getStats().getCapped()) {
                                    //bigRand = bigRand.min(new BigDecimal(mob.getStats().damageCap).divide(BigDecimal.valueOf(ret.lines), 0, RoundingMode.UP).toBigInteger());
                                    bigRand = bigRand.min(new BigDecimal(mob.getStats().damageCap).toBigInteger());
                                    if (bigRand.compareTo(BigInteger.ONE) < 0) {
                                        bigRand = BigInteger.ONE;
                                    }
                                }
                            } else {
                                bigRand = BigInteger.ONE;
                            }
                        }
                        allBigDamageNumbers.add(new Pair<BigInteger, Boolean>(bigRand, false)); //m.e. never crits
                    }
                }
                lea.skip(4); // CRC of monster [Wz Editing]
                if (skillz != null) {
                    allBigDamageNumbers = Modify_AttackBigCrit(allBigDamageNumbers, chr, 1, ret.getAttackEffect(chr, ret.level, ret.baseSkill), ret.baseSkill, mob);
                }
                ret.allBigDamage.add(new AttackPair(Integer.valueOf(oid).intValue(), allBigDamageNumbers, true));
                combo++;
            }
        }
        if (chr.canCombo && combo > 0) {
            PlayerHandler.AranCombo(chr.getClient(), chr, combo);
        }
        ret.targets = (byte) ret.allBigDamage.size();
        return ret;
    }

    //range
    public static final AttackInfo parseDmgR(final SeekableLittleEndianAccessor lea, final MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.isChangingMaps()) {
            return null;
        }
        //System.out.println("parseDmgR.." + lea.toString());
        final AttackInfo ret = new AttackInfo();
        lea.skip(1); // portal count
        ret.tbyte = lea.readByte();
        //System.out.println("TBYTE: " + tbyte);
        ret.targets = (byte) ((ret.tbyte >>> 4) & 0xF);
        ret.hits = (byte) (ret.tbyte & 0xF);
        ret.skill = lea.readInt();
        if (ret.skill >= 91000000) { //guild/recipe? no
            return null;
        }
        lea.skip(10); // ORDER [2] byte on bigbang [4] bytes on v.79, [4] bytes on v.80, [1] byte on v.82
        Point mobPos = null;
        Point chrPos = chr.getPosition();
        switch (ret.skill) {//hold down kills
            case 3121004, 3221001, 5221004, 13111002, 33121009, 35001001, 35101009, 23121000, 5311002, 24121000 -> // Hurricane
                lea.skip(4); // extra 4 bytes
        }
        //hold down kills
        // Hurricane
        // Pierce
        // Rapidfire
        // Cygnus Hurricane
        ret.charge = -1;
        ret.unk = lea.readByte();
        ret.display = lea.readShort();
        ret.direction = (ret.display >>> 15) & 0x1;
        lea.skip(4); //big bang
        lea.skip(1); // Weapon class
        if (ret.skill == 23111001) { // Leap Tornado
            lea.skip(4); // 7D 00 00 00
            lea.skip(4); // pos A0 FC FF FF 
            // could it be a rectangle?
            lea.skip(4); // 1D 00 00 00		
        }
        ret.speed = lea.readByte(); // Confirmed

        int attackTime = lea.readInt();
        chr.lastAttackDelay = attackTime - chr.lastAttack;
        chr.lastAttack = attackTime;
        ret.lastAttackTickCount = attackTime;
        lastAttack = attackTime;

        lea.skip(4); //0
        ret.slot = (byte) lea.readShort();
        ret.csstar = (byte) lea.readShort();
        ret.AOE = lea.readByte(); // is AOE or not, TT/ Avenger = 41, Showdown = 0
        List<Pair<Long, Boolean>> allDamageNumbers;
        List<Pair<BigInteger, Boolean>> allBigDamageNumbers;

        Pair<Integer, Integer> getAttack = GameConstants.getAttack(ret.skill);
        ret.allDamage = new ArrayList<AttackPair>();
        ret.allBigDamage = new ArrayList<AttackPair>();
        double skilldam = 0.1;
        final Skill skillz = SkillFactory.getSkill(ret.skill);
        if (skillz != null) {
            ret.baseSkill = skillz;
            ret.linkSkill = SkillFactory.getSkill(GameConstants.getLinkedAranSkill(ret.skill));
            ret.delay = skillz.getAttackDelay();
            ret.attack_speed = skillz.getSpeed();
            ret.attack_delay = skillz.getLineDelay();
            if (!isFA(ret.skill)) {
                chr.attacks++;
                if (chr.getAttacks() > skillz.getSpeed()) {
                    //System.out.println(chr.getName() + " attack speed: " + chr.getAttacks());
                    chr.attacks = 0;
                    return null;
                }
            }
            ret.level = chr.getSkillLevel(ret.skill);
            MapleStatEffect info = ret.getAttackEffect(chr, ret.level, skillz);
            if (info != null) {
                skilldam = (getSkillDamage(info, skillz, chr));
            }
        }
        BigDecimal OPStat = DamageParse.getBaseDamage(chr).multiply(BigDecimal.valueOf(skilldam));
        List<Point> points = new LinkedList<>();
        double mastery = 10;
        if (chr.getStat().getMastery() > 0) {
            mastery = 10 / (10 * (chr.getStat().getMastery() / 100.0));
        }
        long damagecap = chr.getDamageCap();
        ret.atks = 1;
        double multi = getAttack.getLeft();
        ret.lines = getAttack.getRight();
        int combo = 0;
        for (int i = 0; i < ret.targets; i++) {
            int oid = lea.readInt();
            MapleMonster mob = chr.getMap().getMonsterByOid(oid);
            if (mob != null) {
                if (mob.isDead() || mob.isDead() || mob.teleport) {
                    continue;
                }
                long curr = System.currentTimeMillis();
                if (mob.spawning > curr) {
                    continue;
                }
                lea.skip(4);
                //lea.skip(12); // byte byte byte byte pos(client) pos(server)
                mobPos = lea.readPos();
                lea.skip(4);
                short delay = lea.readShort();
                BigInteger bigDam = BigInteger.ONE;
                if (chrPos.distance(mob.getPosition()) > 2500) {
                    //System.out.println("Player: " + chr.getName() + " - dist: " + chr.getPosition().distance(mob.getPosition()) + " - SKillID: " + ret.skill);
                    continue;
                }
                mob.setAtkDelay(delay);
                if (!chr.tagged) {
                    bigDam = getDamage(chr, mob, multi, OPStat);
                }

                for (int j = 0; j < ret.hits; j++) {
                    lea.skip(4);
                }
                allDamageNumbers = new ArrayList<Pair<Long, Boolean>>();
                allBigDamageNumbers = new ArrayList<Pair<BigInteger, Boolean>>();
                for (int k = 0; k < 1; k++) {
                    for (int j = 0; j < ret.lines; j++) {
                        //long damage = lea.readInt();
                        double master = 1 + (Randomizer.randomDouble(-mastery, 5) * 0.01);
                        BigInteger bigRand = new BigDecimal(bigDam).multiply(BigDecimal.valueOf(master)).toBigInteger().max(BigInteger.ONE);
                        if (mob != null) {
                            if (!mob.isBuffed(MonsterStatus.MAGIC_IMMUNITY) && !mob.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT) && !mob.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) {
                                if (mob.getStats().getCapped()) {
                                    //bigRand = bigRand.min(new BigDecimal(mob.getStats().damageCap).divide(BigDecimal.valueOf(ret.lines), 0, RoundingMode.UP).toBigInteger());
                                    bigRand = bigRand.min(new BigDecimal(mob.getStats().damageCap).toBigInteger());
                                    if (bigRand.compareTo(BigInteger.ONE) < 0) {
                                        bigRand = BigInteger.ONE;
                                    }
                                }
                            } else {
                                bigRand = BigInteger.ONE;
                            }
                        }
                        allBigDamageNumbers.add(new Pair<BigInteger, Boolean>(bigRand, false)); //m.e. never crits
                    }
                }
                lea.skip(4); // CRC of monster [Wz Editing]
                if (mob != null && skillz != null) {
                    allBigDamageNumbers = Modify_AttackBigCrit(allBigDamageNumbers, chr, 1, ret.getAttackEffect(chr, ret.level, ret.baseSkill), ret.baseSkill, mob);
                }
                ret.allBigDamage.add(new AttackPair(Integer.valueOf(oid).intValue(), allBigDamageNumbers, true));
                combo++;
            }
        }
        if (chr.canCombo && combo > 0) {
            PlayerHandler.AranCombo(chr.getClient(), chr, combo);
        }
        ret.targets = (byte) ret.allBigDamage.size();
        return ret;
    }

    public static final AttackInfo parseMesoExplosion(final SeekableLittleEndianAccessor lea, final AttackInfo ret, final MapleCharacter chr) {
        //System.out.println(lea.toString(true));
        byte bullets;
        double skilldam = 0.01;
        Skill skillz = SkillFactory.getSkill(ret.skill);
        if (skillz != null) {
            int skillLevel = chr.getSkillLevel(GameConstants.getLinkedAranSkill(skillz.getId()));
            MapleStatEffect info = ret.getAttackEffect(chr, skillLevel, skillz);
            if (info != null) {
                skilldam = (getSkillDamage(info, skillz, chr));
            }
        }
        long OPStat = (long) (chr.getOpDamage() * skilldam);

        if (ret.hits == 0) {
            lea.skip(4);
            bullets = lea.readByte();
            for (int k = 0; k < 4; k++) {
                for (int j = 0; j < bullets; j++) {
                    ret.allDamage.add(new AttackPair(Integer.valueOf(lea.readInt()), null));
                    lea.skip(1);
                }
            }
            lea.skip(2); // 8F 02
            return ret;
        }

        List<Pair<Long, Boolean>> allDamageNumbers;
        double mastery = 10;
        if (chr.getStat().getMastery() > 0) {
            mastery = 10 / (10 * (chr.getStat().getMastery() / 100.0));
        }
        MapleMonster mob = null;
        long damagecap = chr.getDamageCap();
        for (int i = 0; i < ret.targets; i++) {
            int oid = lea.readInt();
            mob = chr.getMap().getMonsterByOid(oid);
            lea.skip(12); // [1] Always 6?, [3] unk, [4] Pos1, [4] Pos2, [2] seems to change randomly for some attack
            bullets = lea.readByte();
            long boost = (long) (OPStat);
            if (mob != null) {
                double mobdef = mob.getStats().getDef();
                double atkdiff = (Randomizer.DoubleMax((double) chr.getStat().getPDR() / (double) (mobdef), 1.0));
                double lvldiff = Math.pow((Randomizer.DoubleMax((double) chr.getTotalLevel() / (double) mob.getStats().getLevel(), 1.0)), 3);
                boost = Randomizer.LongMax((long) (boost * atkdiff * lvldiff), damagecap);
                if (mob.getStats().isBoss()) {
                    boost = Randomizer.LongMax((long) (boost * chr.getStat().getBossDmgR()), damagecap);
                } else {
                    boost = Randomizer.LongMax((long) (boost * chr.getStat().getDmgR()), damagecap);
                }
            }
            allDamageNumbers = new ArrayList<Pair<Long, Boolean>>();
            for (int j = 0; j < bullets; j++) {
                lea.skip(4);
                long range = (long) ((1 + (Randomizer.randomDouble(-mastery, 5) * 0.01)) * boost);
                long damage = 1;
                long cap = Long.MAX_VALUE;
                if (mob != null) {
                    cap = mob.getDamageCap();
                    damage = Randomizer.LongMax(range, mob.getId() == 9990007 ? Long.MAX_VALUE : damagecap);
                    if (mob.isBuffed(MonsterStatus.WEAPON_IMMUNITY) || mob.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT) || mob.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) {
                        damage = 1;
                    }
                    damage *= Math.pow(Randomizer.DoubleMin(chr.getLevel() / mob.getStats().getLevel(), 1.0), 5);
                }
                allDamageNumbers.add(new Pair<Long, Boolean>(Randomizer.LongMax(damage, cap), false)); //m.e. never crits
            }
            lea.skip(4); // CRC of monster [Wz Editing]
            if (mob != null && skillz != null) {
                allDamageNumbers = DamageParse.Modify_AttackCrit(allDamageNumbers, chr, 1, ret.getAttackEffect(chr, chr.getSkillLevel(GameConstants.getLinkedAranSkill(skillz.getId())), skillz), skillz, mob);
            }
            ret.allDamage.add(new AttackPair(Integer.valueOf(oid).intValue(), allDamageNumbers));
        }
        lea.skip(4);
        bullets = lea.readByte();
        for (int j = 0; j < bullets; j++) {
            ret.allDamage.add(new AttackPair(Integer.valueOf(lea.readInt()), null));
            lea.skip(2);
        }
        short delay = lea.readShort();
        if (mob != null && mob.isAlive()) {
            mob.setAtkDelay(delay);
        }
        // 8F 02/ 63 02

        return ret;
    }

    public static double getSkillDamage(MapleStatEffect ret, Skill skill, MapleCharacter chr) {
        if (ret != null) {
            switch (ret.getSourceId()) {
                case 4321000://tornado spin
                    return ret.getV();
                default:
                    if (ret.getDamage() > 0) {
                        return (Randomizer.DoubleMin(((chr.getSkillDamageBoost(skill) * skill.getBaseDamage())) * 0.01, 0.01));
                    }
            }
        }
        return 0;
    }
    /*
     if (!points.isEmpty()) {
     for (Point point : points) {
     if (chrPos != null && point != null) {
     double distance = chrPos.distance(point);
     //System.out.println("SKILL: " + ret.skill);
     //System.out.println("DIST: " + distance);
     if (!isFA(ret.skill) && distance > 1600) {
     chr.dropErrorMessage("Damage exceeding maximum range. Range: " + distance);
     chr.kill();
     ret.allDamage.clear();
     }
     }
     }
     }
     */
}
