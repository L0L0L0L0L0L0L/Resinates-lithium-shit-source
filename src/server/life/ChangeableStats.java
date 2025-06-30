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
package server.life;

import constants.GameConstants;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Calendar;
import server.Randomizer;

public class ChangeableStats extends OverrideMonsterStats {

    public int watk, matk, acc, eva, PDRate, MDRate, pushed, level;

    public ChangeableStats(MapleMonsterStats stats, OverrideMonsterStats ostats) {
        hp = ostats.getHp();
        exp = ostats.getExp();
        mp = ostats.getMp();
        watk = stats.getPhysicalAttack();
        matk = stats.getMagicAttack();
        acc = stats.getAcc();
        eva = stats.getEva();
        PDRate = stats.getPDRate();
        MDRate = stats.getMDRate();
        pushed = stats.getPushed();
        level = stats.getLevel();
    }

    public ChangeableStats(MapleMonsterStats stats, int newLevel, int scale, boolean aggro) {

        newLevel = Randomizer.MinMax(newLevel, 1, 9999);
        scale = Randomizer.MinMax(scale, 1, 999);
        int fakeLevel = Randomizer.MinMax(newLevel, 1, 9999);
        double kd = 1.0;
        //stats.kdboss = true;
        if (stats.superKaotic() || stats.getKaotic() || stats.isExplosiveReward()) {
            stats.dropTier = scale;
            double multi = 100.0;
            if (stats.mega) {
                kd = 1.5;
                int chance = Randomizer.random(1, 10);
                if (chance == 1) {
                    chance = Randomizer.random(1, 10);
                    stats.omega = true;
                    kd = 2.0;
                    if (chance == 1) {
                        stats.ultimate = true;
                        kd = 3.0;
                    }
                }
            } else {
                int chance = Randomizer.random(1, 100);
                if (chance == 1) {
                    chance = Randomizer.random(1, 10);
                    stats.omega = true;
                    kd = 1.25;
                    if (chance == 1) {
                        stats.ultimate = true;
                        kd = 2.0;
                    }
                }
            }
            stats.kdRate = kd;
            double base = 1.7;
            if (stats.mega) {
                base = 1.8;
            }
            if (stats.omega) {
                base = 1.9;
            }
            if (stats.ultimate) {
                base = 2.0;
            }
            double zScale = 2.0 + (Math.pow(scale * 0.025, base));
            double yscale = 2.0;
            BigDecimal bigScale = BigDecimal.valueOf(Math.pow(scale, zScale)).multiply(BigDecimal.valueOf(Math.pow(fakeLevel, yscale))).multiply(BigDecimal.valueOf(multi));
            stats.setTotalHP(bigScale.toBigInteger());
            stats.setScale(scale);
            stats.setTier(scale);
            if (stats.getRaidBoss() || stats.isExplosiveReward()) {
                stats.setTrueBoss(true);
                stats.setBar(true);
                stats.setCapped(true);
            }
            stats.damageCap = BigInteger.ZERO;
        } else {
            int setScale = (int) (Randomizer.DoubleMin(Math.floor(newLevel / 100.0), scale));
            double zScale = 2.0 + (scale * 0.01);
            double yscale = 2.0;
            BigDecimal bigScale = BigDecimal.valueOf(Math.pow(scale, zScale)).multiply(BigDecimal.valueOf(Math.pow(fakeLevel, yscale)));
            stats.setTotalHP(bigScale.toBigInteger());
            stats.setScale(setScale);
            stats.setTier(scale);
            stats.setTrueBoss(false);
            stats.setBar(false);
            stats.damageCap = BigInteger.ZERO;
            stats.dropTier = Randomizer.DoubleMax(scale, 100.0);
            stats.kdRate = 1.0;
        }
        if (stats.getCapped()) {
            if (stats.hits > 0) {
                stats.damageCap = new BigDecimal(stats.TotalHP).divide(BigDecimal.valueOf(stats.hits), 2, RoundingMode.HALF_UP).toBigInteger();
            }
        }
        if (!GameConstants.getCapped()) {
            stats.setCapped(false);
            stats.hits = 0;
            if (stats.getFixedDamage() < 0) {
                stats.damageCap = BigInteger.ZERO;
            }
        }
        int baseTier = stats.getScale();
        stats.setMp(1000000000);//unlimited attacks
        /*
         if (scale > 5) {
         double value = (scale - 5) * 0.025;
         stats.resist = Randomizer.DoubleMax(value, 0.95);
         }
         */
        long baseatk = (long) (Math.pow(fakeLevel * scale, stats.getTrueBoss() ? 1.2 : 1.1));
        stats.setPhysicalAttack(Randomizer.Max((int) (baseatk * 2.5), 99999999));
        stats.setMagicAttack(Randomizer.Max((int) (baseatk * 1.0), 99999999));

        //stats.setPhysicalAttack(Randomizer.Max((int) (newLevel * Math.pow(scale, 3.1)), 99999999));
        //stats.setMagicAttack(Randomizer.Max((int) (newLevel * Math.pow(scale, 3)), 99999999));
        stats.setAcc(Integer.MAX_VALUE);//always max acc
        stats.setEva(0);
        double boost = (double) newLevel * 0.000025 * stats.kdRate;
        long tempExp = (long) (Math.pow(newLevel, (stats.isExplosiveReward() ? 2.5 : 2.1) + boost));
        stats.setExp(Randomizer.LongMax(tempExp, Long.MAX_VALUE));
        long def = 1;
        if (stats.getTrueBoss()) {
            def = (long) (Math.pow(fakeLevel * scale, 1.5));
        } else {
            def = (long) (Math.pow(fakeLevel * scale, 1.2));
        }
        stats.setDef(Randomizer.MaxLong(def, 999999999999L));
        stats.setPDRate(0);
        stats.setMDRate(0);
        if (baseTier >= 5) {
            stats.setPushed(Integer.MAX_VALUE);
        } else {
            stats.setPushed(stats.getPushed());
        }
        //stats.setLevel((short) (newLevel));
        stats.setLevel((short) (newLevel));
        stats.setPower((int) (newLevel));
        stats.setFirstAttack(true);
        hp = stats.getHp();
        exp = stats.getExp();
        mp = stats.getMp();
        watk = 0;
        matk = 0;
        acc = stats.getAcc();
        eva = stats.getEva();
        PDRate = 0;
        MDRate = 0;
        pushed = stats.getPushed();
        level = stats.getLevel();
    }

    public ChangeableStats(MapleMonsterStats stats, int newLevel, boolean pqMob) {
        final double mod = (double) newLevel / (double) stats.getLevel();
        final double hpRatio = (double) stats.getHp() / (double) stats.getExp();
        final double pqMod = (pqMob ? 2.5 : 1.0);
        hp = (long) Math.round((!stats.isBoss() ? GameConstants.getMonsterHP(newLevel) : (stats.getHp() * mod)) * pqMod);
        exp = (int) Math.round((!stats.isBoss() ? (GameConstants.getMonsterHP(newLevel) / hpRatio) : (stats.getExp() * mod)) * mod * pqMod);
        mp = (int) Math.round(stats.getMp() * mod * pqMod);
        watk = (int) Math.round(stats.getPhysicalAttack() * mod);
        matk = (int) Math.round(stats.getMagicAttack() * mod);
        acc = (int) Math.round(stats.getAcc() + Math.max(0, newLevel - stats.getLevel()) * 2);
        eva = (int) Math.round(stats.getEva() + Math.max(0, newLevel - stats.getLevel()));
        PDRate = Math.min(stats.isBoss() ? 30 : 20, (int) Math.round(stats.getPDRate() * mod));
        MDRate = Math.min(stats.isBoss() ? 30 : 20, (int) Math.round(stats.getMDRate() * mod));
        pushed = (int) Math.round(stats.getPushed() * mod);
        level = newLevel;
    }
}
