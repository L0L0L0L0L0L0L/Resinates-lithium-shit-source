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
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import client.MapleCharacter;
import client.MapleDisease;
import client.status.MonsterStatus;
import java.util.EnumMap;
import server.Randomizer;
import server.Timer.MapTimer;
import server.TimerManager;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;

public class MobSkill {

    private int skillId, skillLevel, spawnEffect, hp, x, y;
    private long duration, cooltime;
    private float prop;
//    private short effect_delay;
    private short limit;
    private List<Integer> toSummon = new ArrayList<Integer>();
    private Point lt, rb;
    private boolean summonOnce;

    public MobSkill(int skillId, int level) {
        this.skillId = skillId;
        this.skillLevel = level;
    }

    public void setOnce(boolean o) {
        this.summonOnce = o;
    }

    public boolean onlyOnce() {
        return summonOnce;
    }

    public void addSummons(List<Integer> toSummon) {
        this.toSummon = toSummon;
    }

    /*   public void setEffectDelay(short effect_delay) {
     this.effect_delay = effect_delay;
     }*/
    public void setSpawnEffect(int spawnEffect) {
        this.spawnEffect = spawnEffect;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setCoolTime(long cooltime) {
        this.cooltime = cooltime;
    }

    public void setProp(float prop) {
        this.prop = prop;
    }

    public void setLtRb(Point lt, Point rb) {
        this.lt = lt;
        this.rb = rb;
    }

    public void setLimit(short limit) {
        this.limit = limit;
    }

    //teleports
    public class MobTeleportType {

        public static final int None = 0;
        public static final int AggroTop = 1;
        public static final int Controller = 2;
        public static final int StaticPoint = 3;
        public static final int OffsetX = 4;
        public static final int RandomUser = 5;
        public static final int NearestSP = 6;
        public static final int NotController = 7;
        public static final int Anywhere = 8;
        public static final int SummonIllusion = 9;
        public static final int OffsetX2 = 10;
        public static final int OffsetX3 = 11;
    }

    public boolean checkCurrentBuff(MapleCharacter player, MapleMonster monster) {
        if (monster == null || !monster.isAlive()) {
            return false;
        }
        boolean stop = false;
        switch (skillId) {
            case 100, 110, 150 ->
                stop = monster.isBuffed(MonsterStatus.WEAPON_ATTACK_UP);
            case 101, 111, 151 ->
                stop = monster.isBuffed(MonsterStatus.MAGIC_ATTACK_UP);
            case 102, 112, 152 ->
                stop = monster.isBuffed(MonsterStatus.WEAPON_DEFENSE_UP);
            case 103, 113, 153 ->
                stop = monster.isBuffed(MonsterStatus.MAGIC_DEFENSE_UP);
            case 140, 141, 142, 143, 144, 145 ->
                stop = monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY) || monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY) || monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY);
            case 200, 201 ->
                stop = player.getMap().getAllSummonMonsters().size() >= 200;

            case 170 -> {
                stop = false;
            }

        }
        //154-157, don't stop it
        stop |= monster.isBuffed(MonsterStatus.MAGIC_CRASH);
        return stop;
    }

    public void applyEffect(MapleCharacter player, MapleMonster monster, boolean skill) {
        if (monster == null) {
            return;
        }
        if (monster.isDead()) {
            return;
        }
        int chance = Randomizer.random(1, 10);
        if (chance > 1) {
            return;
        }
        //System.out.println(monster.getStats().getName() + " use Skill: " + skillId + " - Skill level: " + skillLevel + " - Skill Duration:" + duration);
        MapleDisease disease = MapleDisease.getBySkill(skillId);
        Map<MonsterStatus, Integer> stats = new EnumMap<MonsterStatus, Integer>(MonsterStatus.class);
        List<Integer> reflection = new LinkedList<Integer>();
        switch (skillId) {
            case 199:
                int random = Randomizer.random(0, 10);
                switch (random) {
                    case 0 -> {
                        player.dropMessage("You have been sealed");
                        disease = MapleDisease.SEAL;
                    }
                    case 1 -> {
                        player.dropMessage("You have been darkend");
                        disease = MapleDisease.DARKNESS;
                    }
                    case 2 -> {
                        player.dropMessage("You have been weakened");
                        disease = MapleDisease.WEAKEN;
                    }
                    case 3 -> {
                        player.dropMessage("You have been stunned");
                        disease = MapleDisease.STUN;
                    }
                    case 4 -> {
                        player.dropMessage("You have been Cursed");
                        disease = MapleDisease.CURSE;
                    }
                    case 5 -> {
                        player.dropMessage("You have been poisoned");
                        disease = MapleDisease.POISON;
                    }
                    case 6 -> {
                        player.dropMessage("You have been slowed");
                        disease = MapleDisease.SLOW;
                    }
                    case 7 -> {
                        player.dropMessage("You have been seduced");
                        disease = MapleDisease.SEDUCE;
                    }
                    case 8 -> {
                        player.dropMessage("You have been confused");
                        disease = MapleDisease.REVERSE_DIRECTION;
                    }
                    case 9 -> {
                        player.dropMessage("You have been zombied");
                        disease = MapleDisease.ZOMBIFY;
                    }
                    case 10 ->
                        player.dropMessage("You evaded his after effect");
                }
                break;

            case 100:
            case 110:
            case 150:
                stats.put(MonsterStatus.WEAPON_ATTACK_UP, Integer.valueOf(x));
                break;
            case 101:
            case 111:
            case 151:
                stats.put(MonsterStatus.MAGIC_ATTACK_UP, Integer.valueOf(x));
                break;
            case 102:
            case 112:
            case 152:
                stats.put(MonsterStatus.WEAPON_DEFENSE_UP, Integer.valueOf(x));
                break;
            case 103:
            case 113:
            case 153:
                stats.put(MonsterStatus.MAGIC_DEFENSE_UP, Integer.valueOf(x));
                break;
            case 154:
                stats.put(MonsterStatus.ACC, Integer.valueOf(x));
                break;
            case 155:
                stats.put(MonsterStatus.AVOID, Integer.valueOf(x));
                break;
            case 115:
            case 156:
                stats.put(MonsterStatus.SPEED, Integer.valueOf(x));
                break;
            case 120:
            case 157:
                disease = MapleDisease.SEAL;
                break;
            case 121:
                disease = MapleDisease.DARKNESS;
                break;
            case 122:
                disease = MapleDisease.WEAKEN;
                break;
            case 123:
            case 134: // zombify
            case 135: // Seduce + stun
                disease = MapleDisease.STUN;
                break;
            case 124:
                disease = MapleDisease.CURSE;
                break;
            case 125:
                disease = MapleDisease.POISON;
                break;
            case 126: // Slow
                disease = MapleDisease.SLOW;
                break;
            case 132:
            case 136:
                disease = MapleDisease.REVERSE_DIRECTION;
                break;
            case 128: // Seduce
            case 129: // banished replaced with seduce
                disease = MapleDisease.SEDUCE;
                break;
            case 133: // zombify
                disease = MapleDisease.ZOMBIFY;
                break;
            case 175://deathmark
                if (skill && monster.isAlive()) {
                    for (MapleCharacter character : monster.getMap().getAllPlayers()) {
                        //character.dispel();
                        if (character != null && character.isAlive() && character.isAlive() && character.canDamage()) {
                            double dist = character.getPosition().distance(player.getPosition());
                            if (dist <= 2500) {
                                int hpdamage = (int) (Randomizer.Min(Randomizer.random(character.getStat().getHp()), (int) (character.getStat().getHp() - 1)) * (1 - character.getStat().getResist()));
                                int mpdamage = (int) (Randomizer.Min(Randomizer.random(character.getStat().getMp()), (int) (character.getStat().getMp() - 1)) * (1 - character.getStat().getResist()));
                                character.addMPHP(-hpdamage, -mpdamage);
                                character.getClient().announce(CWvsContext.damagePlayer(character, hpdamage));
                                character.getMap().broadcastMessage(character, CField.damagePlayer(character.getId(), hpdamage, true), false);
                                player.setDamage(System.currentTimeMillis() + 2000);
                            }
                        }
                    }
                }
                break;
            case 176://demi damage
                if (skill && monster.isAlive()) {
                    for (MapleCharacter character : monster.getMap().getAllPlayers()) {
                        if (character != null && character.isAlive() && character.isAlive() && character.canDamage()) {
                            double dist = character.getPosition().distance(player.getPosition());
                            if (dist <= 1000) {
                                int damage;
                                if (Randomizer.nextBoolean()) {
                                    damage = Randomizer.random(1, character.getStat().getHp());
                                    character.addMPHP(-damage, 0);
                                } else {
                                    damage = Randomizer.random(1, character.getStat().getMp());
                                    character.addMPHP(0, -damage);
                                }
                                character.getClient().announce(CWvsContext.damagePlayer(character, damage));
                                character.getMap().broadcastMessage(character, CField.damagePlayer(character.getId(), damage, true), false);
                                player.setDamage(System.currentTimeMillis() + 2000);
                            }
                        }
                    }
                }
                break;
            case 114:
            case 105: //consume..
                if (lt != null && rb != null && skill) {
                    for (MapleMonster mons : monster.getMap().getAllMonsters()) {
                        long limithp = (long) (monster.getStats().getHp() * 0.75);
                        if (mons.getStats().getHp() <= limithp) {
                            long heal = (long) (mons.getStats().getHp() * 0.05);
                            mons.heal(heal, Integer.MAX_VALUE, true);
                        }
                    }
                } else {
                    long limithp = (long) (monster.getStats().getHp() * 0.75);
                    if (monster.getStats().getHp() <= limithp) {
                        long heal = (long) (monster.getStats().getHp() * 0.05);
                        monster.heal(heal, Integer.MAX_VALUE, true);
                    }
                }
                break;
            case 127:
                for (MapleCharacter character : monster.getMap().getAllPlayers()) {
                    if (character.canCombo) {
                        double dist = character.getPosition().distance(monster.getPosition());
                        if (dist <= 5000) {
                            character.setCombo(0);
                            character.getClient().announce(CField.testCombo(0));
                            character.dropTopMessage("Your Combo has been reset!");
                            character.updateStats();
                        }
                    }
                }
            case 138:
                if (skill && monster.isAlive() && monster.getStats().getTrueBoss()) {
                    for (MapleCharacter character : monster.getMap().getAllPlayers()) {
                        double dist = character.getPosition().distance(monster.getPosition());
                        if (dist <= 4000) {

                            if (character != null && character.isAlive() && character.isAlive() && character.canDamage() && !character.isInvincible()) {
                                int damage;
                                if (Randomizer.nextBoolean()) {
                                    damage = Randomizer.random(1, character.getStat().getHp());
                                    character.addMPHP(-damage, 0);
                                } else {
                                    damage = Randomizer.random(1, character.getStat().getMp());
                                    character.addMPHP(0, -damage);
                                }
                                character.getClient().announce(CWvsContext.damagePlayer(character, damage));
                                character.getMap().broadcastMessage(character, CField.damagePlayer(character.getId(), damage, true), false);
                                character.setDamage(System.currentTimeMillis() + 2000);
                            }
                        }
                    }
                }
                break;
            case 131: // Mist
                int r = Randomizer.random(1, 3);
                if (r == 1) {
                    monster.getMap().spawnMist(new MapleMist(calculateBoundingBox(monster.getPosition(), true), monster, this), x * 1000, false);
                }
                break;
            case 140:
                //skillLevel = 5;
                if (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY) && !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
                    monster.getMap().broadcastMapMsg(monster.getStats().getName() + " is now immune to melee damage!", 5120205);
                    stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(x));
                }
                break;
            case 141:

                //skillLevel = 4;
                if (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY) && !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
                    monster.getMap().broadcastMapMsg(monster.getStats().getName() + " is now immune to magic damage!", 5120205);
                    stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(x));
                }
                break;
            case 142: // Weapon / Magic Immunity
                //skillLevel = 1;

                if (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY) && !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
                    monster.getMap().broadcastMapMsg(monster.getStats().getName() + " is now immune to all damage!", 5120205);
                    stats.put(MonsterStatus.DAMAGE_IMMUNITY, Integer.valueOf(x));
                }
                break;
            case 143: // Weapon Reflect
                //skillLevel = 1;

                if (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY) && !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
                    monster.getMap().broadcastMapMsg(monster.getStats().getName() + " is now reflecting melee damage!", 5120205);
                    stats.put(MonsterStatus.WEAPON_DAMAGE_REFLECT, Integer.valueOf(x));
                    stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(x));
                    reflection.add(x);
                }
                break;
            case 144: // Magic Reflect
                //skillLevel = 1;

                if (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY) && !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
                    monster.getMap().broadcastMapMsg(monster.getStats().getName() + " is now reflecting magic damage!", 5120205);
                    stats.put(MonsterStatus.MAGIC_DAMAGE_REFLECT, Integer.valueOf(x));
                    stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(x));
                    reflection.add(x);
                }
                break;
            case 145: // Weapon / Magic reflect
                //skillLevel = 1;
                if (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY) && !monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
                    monster.getMap().broadcastMapMsg(monster.getStats().getName() + " is now reflecting all damage!", 5120205);
                    //skillLevel = 1;
                    stats.put(MonsterStatus.WEAPON_DAMAGE_REFLECT, Integer.valueOf(x));
                    stats.put(MonsterStatus.MAGIC_DAMAGE_REFLECT, Integer.valueOf(x));
                    stats.put(MonsterStatus.WEAPON_DAMAGE_REFLECT, Integer.valueOf(x));
                    stats.put(MonsterStatus.MAGIC_DAMAGE_REFLECT, Integer.valueOf(x));
                    reflection.add(x);
                    reflection.add(x);
                }
                break;
            case 170://to be worked on
                if (skill && monster.isAlive() && monster.getStats().getTrueBoss()) {
                    TimerManager.getInstance().schedule(() -> {
                        if (!monster.isDead()) {
                            if (Randomizer.nextBoolean()) {
                                monster.randomTeleport();
                            } else {
                                if (!monster.getMap().getAllPlayers().isEmpty()) {
                                    Point pos = monster.getMap().getRandomPlayer().getPosition();
                                    pos.y--;
                                    monster.teleportMob(monster.getMap().checkMapEdge(pos));
                                } else {
                                    monster.randomTeleport();
                                }
                            }
                        }
                    }, 1000);

                }
                break;

            /*
                 if (monster.getMap() != null) {
                 final Point pos = monster.getPosition();
                 final Point oldPos = monster.getMap().getRandomPlayer().getPosition();
                 if (x == 4) {
                 oldPos.x += Randomizer.random(-y, y);
                 }
                 if (oldPos != null) {
                 monster.setPosition(oldPos);
                 monster.getMap().broadcastMessage(player, MobPacket.spawnMonster(monster, -1, 0), true);
                 monster.getMap().broadcastMessage(player, MobPacket.getMonsterTeleport(monster.getObjectId(), x, oldPos, pos), true);
                 monster.getMap().moveMonster(monster, monster.getTruePosition());
                 }
                 }
             */
            case 200:
            case 201:
                MapleMap map = monster.getMap();
                if (map.getAllSummonMonsters().size() < 200) {
                    int summoncap = 4 * getSummons().size();
                    int summonrank = Randomizer.Min(monster.getStats().getScale() - 1, 1);
                    for (Integer mobId : getSummons()) {
                        if (monster.summons.size() < summoncap) {
                            MapleMonster toSpawn;
                            if (monster.getStats().getKaotic()) {
                                toSpawn = MapleLifeFactory.getSummonKaoticMonster(mobId, monster.getStats().getLevel(), summonrank);
                            } else {
                                toSpawn = MapleLifeFactory.getSummonMonster(mobId, monster.getStats().getLevel(), summonrank);
                            }
                            //System.out.println("spawned: " + toSpawn.getId());
                            if (toSpawn != null) {
                                toSpawn.getStats().getRevives().clear();
                                toSpawn.setPosition(monster.getPosition());
                                int ypos, xpos;
                                xpos = (int) monster.getPosition().getX();
                                ypos = (int) monster.getPosition().getY();

                                switch (toSpawn.getId()) {
                                    case 8500003: // Pap bomb high
                                        toSpawn.setFh((int) Math.ceil(Math.random() * 19.0));
                                        ypos = -590;
                                        break;
                                    case 8500004: // Pap bomb
                                        //Spawn between -500 and 500 from the monsters X position
                                        xpos = (int) (monster.getTruePosition().getX() + Math.ceil(Math.random() * 1000.0) - 500);
                                        ypos = (int) monster.getTruePosition().getY();
                                        break;
                                    case 8510100: //Pianus bomb
                                        if (Math.ceil(Math.random() * 5) == 1) {
                                            ypos = 78;
                                            xpos = (int) (0 + Math.ceil(Math.random() * 5)) + ((Math.ceil(Math.random() * 2) == 1) ? 180 : 0);
                                        } else {
                                            xpos = (int) (monster.getTruePosition().getX() + Math.ceil(Math.random() * 1000.0) - 500);
                                        }
                                        break;
                                    case 8820007: //mini bean
                                        continue;
                                }
                                // Get spawn coordinates (This fixes monster lock)
                                // TODO get map left and right wall.
                                switch (map.getId()) {
                                    case 220080001: //Pap map
                                        if (xpos < -890) {
                                            xpos = (int) (-890 + Math.ceil(Math.random() * 150));
                                        } else if (xpos > 230) {
                                            xpos = (int) (230 - Math.ceil(Math.random() * 150));
                                        }
                                        break;
                                    case 230040420: // Pianus map
                                        if (xpos < -239) {
                                            xpos = (int) (-239 + Math.ceil(Math.random() * 150));
                                        } else if (xpos > 371) {
                                            xpos = (int) (371 - Math.ceil(Math.random() * 150));
                                        }
                                        break;
                                }
                                toSpawn.setPosition(new Point(xpos, ypos));
                                monster.summons.add(toSpawn);
                                map.forceSpawnMonster(toSpawn, toSpawn.getPosition());
                            } else {
                                System.out.println("Parent: " + monster.getId() + " tried to spawn null mob from skillid: " + skillId + " - skill level: " + skillLevel);
                            }
                        } else {
                            break;
                        }
                    }
                }

                break;
            default:
                long limithp2 = (long) (monster.getStats().getHp() * 0.75);
                if (monster.getStats().getHp() <= limithp2) {
                    long heal = (long) (monster.getStats().getHp() * 0.05);
                    monster.heal(heal, Integer.MAX_VALUE, true);
                }
                break;
        }
        if (getDuration() > 0) {
            if (!stats.isEmpty() && monster.isAlive()) {
                if (lt != null && rb != null && skill) {
                    for (MapleMonster mons : getMonstersInRange(monster)) {
                        mons.applyMonsterBuff(stats, getSkillId(), getDuration(), this, reflection);
                    }
                } else {
                    monster.applyMonsterBuff(stats, getSkillId(), getDuration(), this, reflection);
                }
            }
            if (disease != null && player != null) {
                if (lt != null && rb != null && skill && monster.isAlive()) {
                    for (MapleCharacter character : getPlayersInRange(monster)) {
                        if (character != null && character.isAlive()) {
                            character.giveDebuff(disease, this);
                        }
                    }
                } else {
                    if (player.isAlive()) {
                        player.giveDebuff(disease, this);
                    }
                }
            }
        }
    }

    private List<MapleCharacter> getPlayersInRange(MapleMonster monster) {
        return monster.getMap().getPlayersInRange(calculateBoundingBox(monster.getPosition()));
    }

    private List<MapleMonster> getMonstersInRange(MapleMonster monster) {
        return monster.getMap().getMonstersInRange(calculateBoundingBox(monster.getPosition()));
    }

    public int getSkillId() {
        return skillId;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public List<Integer> getSummons() {
        return Collections.unmodifiableList(toSummon);
    }

    /*    public short getEffectDelay() {
     return effect_delay;
     }*/
    public int getSpawnEffect() {
        return spawnEffect;
    }

    public int getHP() {
        return hp;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public long getDuration() {
        return duration;
    }

    public long getCoolTime() {
        return cooltime;
    }

    public Point getLt() {
        return lt;
    }

    public Point getRb() {
        return rb;
    }

    public int getLimit() {
        return limit;
    }

    public boolean makeChanceResult() {
        return prop >= 1.0 || Math.random() < prop;
    }

    private Rectangle calculateBoundingBox(Point posFrom) {
        Point mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
        Point myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);

        Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        Point mylt, myrb;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(lt.x * -1 + posFrom.x, rb.y + posFrom.y);
            mylt = new Point(rb.x * -1 + posFrom.x, lt.y + posFrom.y);
        }
        final Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }

    private List<MapleCharacter> getPlayersInRange(MapleMonster monster, MapleCharacter player) {
        final Rectangle bounds = calculateBoundingBox(monster.getTruePosition(), monster.isFacingLeft());
        List<MapleCharacter> players = new ArrayList<MapleCharacter>();
        players.add(player);
        return monster.getMap().getPlayersInRectAndInList(bounds, players);
    }

    private List<MapleMapObject> getObjectsInRange(MapleMonster monster, MapleMapObjectType objectType) {
        final Rectangle bounds = calculateBoundingBox(monster.getTruePosition(), monster.isFacingLeft());
        List<MapleMapObjectType> objectTypes = new ArrayList<MapleMapObjectType>();
        objectTypes.add(objectType);
        return monster.getMap().getMapObjectsInRect(bounds, objectTypes);
    }

    public void applyDelayedEffect(final MapleCharacter player, final MapleMonster monster, final boolean skill, int animationTime) {
        Runnable toRun = () -> {
            if (monster.isAlive()) {
                applyEffect(player, monster, skill);
            }
        };
    }
}
