/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
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
package server;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.FileoutputUtil;

public class MapleAchievements {

    private static final Map<Integer, MapleAchievement> achievements = new LinkedHashMap<>();
    private static final MapleAchievements instance = new MapleAchievements();

    public static MapleAchievements getInstance() {
        return instance;
    }

    public MapleAchievement getById(int id) {
        return achievements.get(id);
    }

    public List<Integer> getAchievementsIds() {
        return new ArrayList<>(achievements.keySet());
    }

    public List<MapleAchievement> getAchievementsbyCag(int cag) {
        List<MapleAchievement> Achs = new ArrayList<>();
        for (MapleAchievement ach : achievements.values()) {
            if (ach.getCag() == cag) {
                Achs.add(ach);
            }
        }
        return Achs;
    }

    public List<MapleAchievement> getAchievements() {
        return new ArrayList<>(achievements.values());
    }

    private static void addAchievement(MapleAchievement ach) {
        if (!achievements.containsKey(ach.getId())) {
            achievements.put(ach.getId(), ach);
        } else {
            System.out.println("Dupe Achievement ID: " + ach.getId());
        }
    }

    public static void saveAch() {
        try (Connection con = DatabaseConnection.getWorldConnection()) {//import script
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO achievements_template (id, type, name, reward, reward_amount, stat, stat_amount) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                for (MapleAchievement ach : achievements.values()) {
                    ps.setInt(1, ach.id);
                    ps.setInt(2, ach.cag);
                    ps.setString(3, ach.name);
                    ps.setInt(4, ach.reward);
                    ps.setInt(5, ach.reward_amount);
                    ps.setInt(6, ach.stat);
                    ps.setInt(7, ach.stat_amount);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void loadAchievements() {
        try (Connection con = DatabaseConnection.getWorldConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM achievements_template ORDER BY guid"); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                addAchievement(new MapleAchievement(rs.getInt("id"), rs.getInt("type"), rs.getString("name"), rs.getInt("reward"), rs.getInt("reward_amount"), rs.getInt("stat"), rs.getInt("stat_amount")));
            }
        } catch (SQLException ess) {
            ess.printStackTrace();
            System.out.println("Failed to load quests");
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ess);
        }
        System.out.println(achievements.size() + " Achievements Loaded");
        //saveAch();
    }

}
