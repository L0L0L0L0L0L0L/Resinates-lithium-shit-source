/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package server.life;

import database.DatabaseConnection;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.StringUtil;

public class Rank {

    private static final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Mob.wz"));
    //private static final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Mob.wz"));

    public static int getMobRank(int id) {
        MapleData monsterData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(id) + ".img", '0', 11));
        MapleData monsterInfoData = monsterData.getChildByPath("info");
        switch (id) {
            case 8880500://Holy Knight
            case 8880501://Dark Knight
            case 8880502://Black Mage
                return 8;
            case 8850011://Empress - end game
            case 8880000://Magnus - end game
            case 9300534://Baby lotus - end game
            case 8840000://Von Leon - lion form
            case 9400632://Pink Bean final form
            case 9400300://the boss - showa
            case 8870000://Hilla
            case 8644011://Dark Spirit
            case 9833101://Boss dragon
            case 9833102://Boss dragon
            case 9833103://Boss dragon
            case 9833104://Boss dragon
            case 9833105://Boss dragon
            case 8880410://evil hilla
            case 8820001://PB
            case 9420620://black bean
                return 7;
            case 9420543://targa
            case 9420544://targa
            case 9420548://scarlion
            case 9420549://scarlion
                return 6;
            case 9800003://MP - metal golem
            case 9800008://MP - spirit of rock
            case 9800009://MP - crisom of rock
            case 9800016://MP - snow witch
            case 9800022://MP - 
            case 9800023://MP - 
            case 9800024://MP - 
            case 9800025://MP - 
            case 9800031://MP - 
            case 9800037://MP - 
            case 9800038://MP - 
            case 9800044://MP - 
            case 9800048://MP - 
            case 9800050://MP - 
            case 9800056://MP - 
            case 9800057://MP - 
            case 9800058://MP - 
            case 9800060://MP - 
            case 9800063://MP - 
            case 9800066://MP - 
            case 9800072://MP - 
            case 9800075://MP - 
            case 9800076://MP - 
            case 9800077://MP - 
            case 9800082://MP - 
            case 9800083://MP - 
            case 9800084://MP - 
            case 9800090://MP - 
            case 9800091://MP - 
            case 9800099://MP - 
            case 9800105://MP - 
            case 9800108://MP - 
            case 9800109://MP - 
            case 9800113://MP - 
            case 9800119://MP - 
            case 9800120://MP - 
            case 9800122://MP - 
            case 9800123://MP - 
            case 9800124://MP -
                return 3;
            case 9400748://MV
            case 8300006://Dragonoir
            case 8300007://Dragonoir rider
            case 9400590://Margana - NLC PQ
            case 9400591://Red Nirg - NLC PQ
            case 9400592://Rellik - NLC PQ
            case 9400593://Hsalf - NLC PQ
            case 9400594://Master Guardian - NLC PQ
            case 9400112://bodyguard A
            case 9400113://bodyguard B
            case 8850010://shinsuu - empress dragon
            case 8220004://dodo - tot
            case 8220005://Lilynouch - tot
            case 8220006://Lyka - tot
            case 9300529://White Serpant
            case 9300530://White Serpant
            case 8860002://Ark's Serpant
            case 9500319://final stage snowman
            case 9500532://hard snowman
            case 8210010://anni
            case 8210011://anni
            case 8210012://anni
            case 8210013://anni
            case 8510000://pianus
            case 8520000://pianus
                return 4;
            case 9500317://stage 1 snowman
            case 9500318://stage 2 snowman
            case 8141100://Gigantic Spirit Viking
            case 8143000://Grim Phantom Watch
            case 7160000://Dual Ghost Pirate
            case 7130300://Master Death Teddy
                return 2;
            case 8510100://bloodly bloom
            case 8510200://bloodly bloom
            case 8510300://bloodly bloom
            case 8500003://Papu mine
            case 8500004://Papu mine
            case 9400549:// HH
            case 8160000://gate
            case 8170000://thanos
                return 1;
        }
        if (id >= 8820200 && id <= 8820212) { //Chaos Pink Beans
            return 7;
        }
        if (id >= 9302021 && id <= 9302038) { //golden pigs
            return 2;
        }
        if (id >= 9302040 && id <= 9302043) { //skele troopers
            return 3;
        }
        if (id >= 8610005 && id <= 8610014) { //empress knights
            return 2;
        }
        if (id >= 8850000 && id <= 8850004) { //empress boss knights
            return 4;
        }
        if (id >= 8800000 && id <= 8800002) { //zakum parts
            return 7;
        }
        if (id >= 8800003 && id <= 8800010) { //zakum arms
            return 5;
        }
        if (id >= 8810002 && id <= 8810009) { //horntail parts
            return 8;
        }
        if (id >= 8820002 && id <= 8820007) { //Pink Bean statues
            return 6;
        }
        if (id >= 9500337 && id <= 9500361) { //easy boss rush
            return 2;
        }
        if (id >= 9305100 && id <= 9305139) { //normal boss rush
            return 3;
        }
        if (id >= 9305200 && id <= 9305239) { //mega boss rush
            return 4;
        }
        if (id >= 9305300 && id <= 9305339) { //ultimate boss rush
            return 5;
        }
        if (id >= 9800000 && id <= 9800141) { //monster park
            return 2;
        }
        if (id >= 9840000 && id <= 9840025) { //Golden Drakes
            return 2;
        }
        if (MapleDataTool.getIntConvert(("boss"), monsterInfoData, 0) > 0) {
            return 2;
        }

        return 1; //normal mob
    }

    public static int getMobRank(int mid, int ch) {
        return getMobRank(mid);
    }

    public static int getMobLevel(int mid) {
        MapleData monsterData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(mid) + ".img", '0', 11));
        MapleData monsterInfoData = monsterData.getChildByPath("info");
        int level = MapleDataTool.getIntConvert("level", monsterInfoData);
        if (mid >= 8800000 && mid <= 8800002) { //zakum parts
            return 200;
        }
        if (mid >= 8800003 && mid <= 8800010) { //zakum arms
            return 180;
        }
        if (mid >= 8820200 && mid <= 8820212) { //Chaos Pink Beans
            return (((mid - 8820200) * 5) + 200);
        }
        if (mid >= 9800000 && mid <= 9800124) {
            return ((mid - 9800000) + 120);
        }
        if (mid >= 9305200 && mid <= 9305239) { //mega boss rush
            return (level + 50);
        }
        if (mid >= 9305300 && mid <= 9305339) { //ultimate boss rush
            return (level + 100);
        }
        if (mid == 9500532) {
            return 100;
        }
        return level;
    }

    public static int loadMob(MapleMonsterStats stats, int mid) {
        try ( Connection con = DatabaseConnection.getWorldConnection()) {
            try ( PreparedStatement ps = con.prepareStatement("SELECT * FROM mobdata WHERE mobid = ?")) {
                ps.setInt(1, mid);
                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        stats.setLevel((short) rs.getInt("level"));
                        stats.setScale(rs.getInt("rank"));
                        stats.setAchievement(rs.getInt("achivement"));
                        stats.setRaidBoss(rs.getInt("raid") > 0);
                        if (!stats.isExplosiveReward()) {
                            stats.setExplosiveReward(rs.getInt("droptype") > 0);
                        }
                        if (rs.getInt("stat") > 0) {
                            stats.bonusStats = rs.getInt("stat");
                            stats.bonusAmount = rs.getInt("amount");
                        }
                        if (rs.getInt("skin") > 0) {
                            stats.skin = rs.getInt("skin");
                        }
                        stats.setMaxCount(rs.getInt("maxcount"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
