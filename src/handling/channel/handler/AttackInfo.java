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

import java.util.List;
import java.awt.Point;

import client.Skill;
import constants.GameConstants;
import client.MapleCharacter;
import client.SkillFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import server.MapleStatEffect;
import server.AutobanManager;
import server.life.MapleMonster;
import tools.AttackPair;
import tools.Pair;

public class AttackInfo {

    public int skill, charge, lastAttackTickCount, level;
    public List<AttackPair> allDamage;
    public List<AttackPair> allBigDamage;
    public Point position;
    public int display;
    public int direction, delay, attack_speed, attack_delay, atks, lines;
    public byte hits, targets, tbyte, speed, csstar, AOE, slot, unk, attacks;
    public boolean real = true;
    public Skill baseSkill, linkSkill;
    public List<MapleMonster> allmobs;
    public HashMap<Integer, Integer> info = new LinkedHashMap<>();

    public final MapleStatEffect getAttackEffect(final MapleCharacter chr, int skillLevel, final Skill skill_) {
        if (GameConstants.isMulungSkill(skill) || GameConstants.isPyramidSkill(skill) || GameConstants.isInflationSkill(skill)) {
            skillLevel = 1;
        } else if (skillLevel <= 0) {
            return null;
        }
        return skill_.getEffect(skillLevel);
    }
}
