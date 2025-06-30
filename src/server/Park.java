/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
import static server.MapleKQuests.addQuest;
import static server.Randomizer.nextInt;
import tools.FileoutputUtil;

/**
 *
 * @author Evil0
 */
public final class Park {

    public Map<Integer, Park_Mob> bosses = new LinkedHashMap<>();
    public Map<Integer, Park_Mob> mini_bosses = new LinkedHashMap<>();
    public Map<Integer, Park_Mob> final_bosses = new LinkedHashMap<>();
    public List<Integer> maps = new ArrayList<>();

    public int mode = 0;
    public int base_tier = 0;
    public int map_group = 0;
    public int tier_mini_boss = 0;
    public int mini_group = 0;
    public int tier_boss = 0;
    public int boss_group = 0;
    public int waves = 0;
    public int required_ach = 0;
    public int reward_ach = 0;
    public int cap_mini = 0;
    public int cap_boss = 0;
    public int cap_final = 0;
    public int rank = 0;
    public int reward = 0;
    public int reward_amount = 0;
    public int level = 1;

    public Park(int mode, int tier, int m_tier, int b_tier, int waves, int maps_id, int mini_ids, int boss_id, int req_ach, int rew_ach, int cap, int boss_cap, int final_cap) {
        this.mode = mode;
        this.base_tier = tier;
        this.tier_mini_boss = m_tier;
        this.tier_boss = b_tier;
        this.waves = waves;
        this.required_ach = req_ach;
        this.reward_ach = rew_ach;
        this.map_group = maps_id;
        this.mini_group = mini_ids;
        this.boss_group = boss_id;
        this.cap_mini = cap;
        this.cap_boss = boss_cap;
        this.cap_final = final_cap;
    }

    public int getMode() {
        return mode;
    }

    public int getWaves() {
        return waves;
    }

    public int getReqAch() {
        return required_ach;
    }

    public int getRewAch() {
        return reward_ach;
    }

    public int getBaseTier() {
        return base_tier;
    }

    public int getMiniBossTier() {
        return tier_mini_boss;
    }

    public int getFinalBossTier() {
        return tier_boss;
    }
    
    public int getMiniCap() {
        return cap_mini;
    }
    
    public int getBossCap() {
        return cap_boss;
    }
    
    public int getRank() {
        return rank;
    }
    
    public int getFinalCap() {
        return cap_final;
    }
    
    public void setRank(int value) {
        rank = value;
    }

    public Park_Mob getMiniBoss(int id) {
        return mini_bosses.get(id);
    }

    public Park_Mob getBoss(int id) {
        return bosses.get(id);
    }

    public Park_Mob getFinalBoss(int id) {
        return final_bosses.get(id);
    }

    public List<Integer> getMaps() {
        return maps;
    }
    
    public void setReward(int value) {
        reward = value;
    }
    
    public void setRewardAmount(int value) {
        reward_amount = value;
    }
    
    public int getReward() {
        return reward;
    }
    
    public int getRewardAmount() {
        return reward_amount;
    }
    
    public void setLevel(int value) {
        level = value;
    }
    
    public int getLevel() {
        return level;
    }

    public int getRandomMiniBoss() {
        List<Integer> keys = new ArrayList<>(mini_bosses.keySet());
        return mini_bosses.get(keys.get(Randomizer.nextInt(keys.size()))).getId();
    }

    public int getRandomBoss() {
        List<Integer> keys = new ArrayList<>(bosses.keySet());
        return bosses.get(keys.get(Randomizer.nextInt(keys.size()))).getId();
    }

    public int getRandomFinalBoss() {
        List<Integer> keys = new ArrayList<>(final_bosses.keySet());
        return final_bosses.get(keys.get(Randomizer.nextInt(keys.size()))).getId();
    }

    public void loadPark(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `park_maps` WHERE mode = ?")) {
            ps.setInt(1, map_group);
            ResultSet rs;
            rs = ps.executeQuery();
            while (rs.next()) {
                maps.add(rs.getInt("mapid"));
            }
            rs.close();
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `park_mini_bosses` WHERE mode = ?")) {
            ps.setInt(1, mini_group);
            ResultSet rs;
            rs = ps.executeQuery();
            while (rs.next()) {
                mini_bosses.put(rs.getInt("mobid"), new Park_Mob(rs.getInt("mode"), rs.getInt("mobid"), rs.getInt("power"), rs.getInt("mega") == 1, rs.getInt("kaotic") == 1, rs.getInt("ultimate") == 1, cap_mini));
            }
            rs.close();
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `park_bosses` WHERE mode = ?")) {
            ps.setInt(1, boss_group);
            ResultSet rs;
            rs = ps.executeQuery();
            while (rs.next()) {
                bosses.put(rs.getInt("mobid"), new Park_Mob(rs.getInt("mode"), rs.getInt("mobid"), rs.getInt("power"), rs.getInt("mega") == 1, rs.getInt("kaotic") == 1, rs.getInt("ultimate") == 1, cap_boss));
                final_bosses.put(rs.getInt("mobid"), new Park_Mob(rs.getInt("mode"), rs.getInt("mobid"), rs.getInt("power"), rs.getInt("mega") == 1, rs.getInt("kaotic") == 1, rs.getInt("ultimate") == 1, cap_final));
            }
            rs.close();
        }
    }

}
