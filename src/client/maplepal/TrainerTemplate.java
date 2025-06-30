/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client.maplepal;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 * @author Evil0
 */
public class TrainerTemplate {

    public static record Trainer(int id, int cooldown, int level, int min_level, int max_level, int min_pal, int max_pal, int tier, int rank, int iv, int bg) {

    } //TODO:you'll probably want to add other static per-template stats

    private static Map<Integer, Trainer> templates;

    public static Trainer getTemplate(int templateId) {
        return templates.get(templateId);
    }

    public static Trainer loadNpc(int id) {
        try (Connection con = DatabaseConnection.getWorldConnection();) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM pal_trainer where id = ?");
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    return new TrainerTemplate.Trainer(rs.getInt("id"), rs.getInt("cooldown"), rs.getInt("level"), rs.getInt("min_level"), rs.getInt("max_level"), rs.getInt("min_pal"), rs.getInt("max_pal"), rs.getInt("tier"), rs.getInt("rank"), rs.getInt("iv"), rs.getInt("bg"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
