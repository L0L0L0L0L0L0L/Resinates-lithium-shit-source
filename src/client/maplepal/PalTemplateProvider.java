package client.maplepal;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import server.Randomizer;
import tools.FileoutputUtil;

public class PalTemplateProvider {

    //static {
    //load(); //TODO:you can either leave this here for automatic loading, or move it to server init
    //}
    public static record PalTemplate(int templateId, String name, int element, int hatchDuration, int evo) {

    } //TODO:you'll probably want to add other static per-template stats

    public static record PalSkillTemplate(int skillId, String name, int element, int power, boolean magic) {

    } //TODO:you'll probably want to add other static per-template stats

    private static Map<Integer, PalTemplate> templates;
    private static Map<Integer, PalSkillTemplate> skills;
    private static Map<Integer, MaplePalEquip> equips;
    private static Map<Integer, MaplePalAbility> abilities;
    private static List<Integer> aPals = new ArrayList<>();

    public static Map<Integer, PalTemplate> getAllMaplePal() {
        return templates;
    }
    
    public static PalTemplate getTemplate(int templateId) {
        return templates.get(templateId);
    }

    public static PalSkillTemplate getSkill(int templateId) {
        return skills.get(templateId);
    }

    public static MaplePalEquip getEquip(int id) {
        return equips.get(id);
    }

    public static MaplePalAbility getAbility(int id) {
        return abilities.get(id);
    }

    public static int getSize() {
        return templates.size();
    }

    public static void setPalCache() {
        for (int pal : templates.keySet()) {
            aPals.add(pal);
        }
    }

    public static List<Integer> getPals() {
        return aPals;
    }

    public static int getRandomPalId() {
        return aPals.get(Randomizer.nextInt(aPals.size()));
    }

    public static List<Integer> getPalsbyType(int type) {
        List<Integer> pals = new ArrayList<>();
        for (int pal : templates.keySet()) {
            if (templates.get(pal).element() == type) {
                pals.add(pal);
            }
        }
        return pals;
    }

    public static List<Integer> getPalsbyEvoLock(int evol) {
        List<Integer> pals = new ArrayList<>();
        for (int pal : templates.keySet()) {
            if (templates.get(pal).evo() == evol) {
                pals.add(pal);
            }
        }
        return !pals.isEmpty() ? pals : getPalsbyEvo(evol);
    }

    public static List<Integer> getPalsbyEvoLock(int type, int evol) {
        List<Integer> pals = new ArrayList<>();
        for (int pal : templates.keySet()) {
            if (templates.get(pal).evo() == evol && templates.get(pal).element() == type) {
                pals.add(pal);
            }
        }
        return !pals.isEmpty() ? pals : getPalsbyEvo(evol);
    }

    public static List<Integer> getPalsbyEvoList(int type, int evol) {
        List<Integer> pals = new ArrayList<>();
        for (int pal : templates.keySet()) {
            if (templates.get(pal).evo() <= evol && templates.get(pal).element() == type) {
                pals.add(pal);
            }
        }
        return !pals.isEmpty() ? pals : getPalsbyEvo(evol);
    }

    public static List<Integer> getPalsbyEvo(int evol) {
        List<Integer> pals = new ArrayList<>();
        for (int pal : templates.keySet()) {
            if (templates.get(pal).evo() <= evol) {
                pals.add(pal);
            }
        }
        return pals;
    }

    public static List<Integer> getAbilitiesbyTier(int tier) {
        List<Integer> pals = new ArrayList<>();
        for (int a : abilities.keySet()) {
            if (abilities.get(a).getTier() == tier) {
                pals.add(a);
            }
        }
        return pals;
    }

    public static void load() {
        //pals
        templates = new HashMap<>();
        try (Connection con = DatabaseConnection.getWorldConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM pal_template ORDER BY id"); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                templates.put(rs.getInt("id"), new PalTemplate(rs.getInt("id"), rs.getString("name"), rs.getByte("element"), rs.getInt("time") * rs.getInt("evo") * 1000, rs.getInt("evo")));
            }
        } catch (SQLException ess) {
            ess.printStackTrace();
            System.out.println("Failed to load maple pals");
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ess);
        }
        System.out.println(templates.size() + " MaplePals Loaded");
        setPalCache();
        System.out.println(aPals.size() + " MaplePals Cached Loaded");
        //pal skills
        skills = new HashMap<>();
        try (Connection con = DatabaseConnection.getWorldConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM pal_skills ORDER BY id"); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                skills.put(rs.getInt("id"), new PalSkillTemplate(rs.getInt("id"), rs.getString("name"), rs.getByte("element"), rs.getInt("power"), (rs.getInt("magic") == 1)));
            }
        } catch (SQLException ess) {
            ess.printStackTrace();
            System.out.println("Failed to load maple pals");
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ess);
        }
        System.out.println(skills.size() + " MaplePals Skills Loaded");
        //equips
        equips = new HashMap<>();
        try (Connection con = DatabaseConnection.getWorldConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM pal_template_equip ORDER BY id"); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                int id = rs.getInt("id");
                equips.put(id, new MaplePalEquip(id, rs.getString("name")));
                MaplePalEquip equip = equips.get(id);
                equip.setHp(rs.getInt("hp"));
                equip.setStr(rs.getInt("str"));
                equip.setDex(rs.getInt("dex"));
                equip.setInt(rs.getInt("int"));
                equip.setLuk(rs.getInt("luk"));
                equip.setAtk(rs.getInt("atk"));
                equip.setMatk(rs.getInt("matk"));
                equip.setDef(rs.getInt("def"));
                equip.setMdef(rs.getInt("mdef"));
                equip.setSpeed(rs.getInt("speed"));
                equip.setDamage(rs.getInt("damage"));
                equip.setDefense(rs.getInt("damage_reduction"));
            }
        } catch (SQLException ess) {
            ess.printStackTrace();
            System.out.println("Failed to load maple pals");
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ess);
        }
        System.out.println(equips.size() + " MaplePals Equips Loaded");

        //abilities
        //equips
        abilities = new HashMap<>();
        try (Connection con = DatabaseConnection.getWorldConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM pal_template_ability ORDER BY id"); ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
                int id = rs.getInt("id");
                abilities.put(id, new MaplePalAbility(id, rs.getString("name")));
                MaplePalAbility a = abilities.get(id);
                a.setTier(rs.getInt("tier"));
                a.setHp(rs.getInt("hp"));
                a.setStr(rs.getInt("str"));
                a.setDex(rs.getInt("dex"));
                a.setInt(rs.getInt("int"));
                a.setLuk(rs.getInt("luk"));
                a.setAtk(rs.getInt("atk"));
                a.setMatk(rs.getInt("matk"));
                a.setDef(rs.getInt("def"));
                a.setMdef(rs.getInt("mdef"));
                a.setSpeed(rs.getInt("speed"));
                a.setDamage(rs.getInt("damage"));
                a.setDefense(rs.getInt("damage_reduction"));
            }
        } catch (SQLException ess) {
            ess.printStackTrace();
            System.out.println("Failed to load maple pals");
            FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, ess);
        }
        System.out.println(abilities.size() + " MaplePals Abilities Loaded");
        //saveAch();
        //TODO: Load from XML or DB
    }
}
