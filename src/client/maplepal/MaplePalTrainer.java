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
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import server.Randomizer;

/**
 *
 * @author Evil0
 */
public class MaplePalTrainer {

    private static final Map<Long, MaplePal> battlers = new HashMap<>();

    public static synchronized MaplePal getBattlePal(int id) {
        return battlers.get(id);
    }

    public static synchronized Collection<MaplePal> getBattlePals() {
        return Collections.unmodifiableCollection(battlers.values());
    }

    public static synchronized void addBattler(MaplePal pal) {
        battlers.put(pal.id, pal);
    }

    public static synchronized void removeBattler(MaplePal pal) {
        battlers.remove(pal.id);
    }

    public static void generateAcc(MaplePal egg) {
        for (int i = 1; i <= egg.getEvo(); i++) {
            egg.setAcc(i, Randomizer.random(4202017, 4202019));
        }
    }

    public static void generateAcc(MaplePal egg, int slots) {
        for (int i = 1; i <= slots; i++) {
            egg.setAcc(i, Randomizer.random(4202017, 4202019));
        }
    }

    public static int getRandomStat(int e) {
        double evo = Randomizer.random(e * 5, e * 10);
        if (e == 4) {
            evo = Randomizer.random(25, 50);
        }
        if (e == 5) {
            evo = Randomizer.random(50, 100);
        }
        return (int) evo;
    }

    public static MaplePal createTempPal(int model, int level, int tier, int rank, int iv) {
        MaplePal egg = new MaplePal().createNewTempPal();

        egg.templateId = (short) model;
        egg.level = (short) Randomizer.MinMax(level, 1, 999);
        egg.gender = (byte) Randomizer.nextInt(2);
        egg.element = (byte) PalTemplateProvider.getTemplate(model).element();
        egg.rank = (byte) tier;
        egg.tier = (byte) rank;
        int evo = PalTemplateProvider.getTemplate(model).evo();
        for (int j = 0; j < egg.IVs.length; j++) {
            egg.IVs[j] = (short) getRandomStat(evo);
        }
        egg.createStats(egg, model, egg.level);
        egg.createAbilities(egg, model);
        egg.name = PalTemplateProvider.getTemplate(model).name();
        egg.skill = Randomizer.random(0, 51);
        generateAcc(egg);
        egg.checkExtra();
        egg.setupHP();
        //System.out.println("Name: " + egg.getName());
        //for (int j = 0; j < egg.getStats().length; j++) {
        //    System.out.println("Stat: " + j + " - " + egg.getStats()[j]);
        //}
        return egg;
    }

    public static MaplePal createSuperTempPal(int model, int level, int tier, int rank, int iv, double multi) {
        MaplePal egg = new MaplePal().createNewTempPal();

        egg.templateId = (short) model;
        egg.level = (short) Randomizer.MinMax(level, 1, 999);;
        egg.gender = (byte) Randomizer.nextInt(2);
        egg.element = (byte) PalTemplateProvider.getTemplate(model).element();
        egg.rank = (byte) tier;
        egg.tier = (byte) rank;
        int evo = PalTemplateProvider.getTemplate(model).evo();
        for (int j = 0; j < egg.IVs.length; j++) {
            egg.IVs[j] = (short) getRandomStat(evo);
        }
        egg.createSuperStats(egg, model, egg.level, multi);
        egg.createAbilities(egg, model);
        egg.name = PalTemplateProvider.getTemplate(model).name();
        egg.skill = Randomizer.random(0, 51);
        generateAcc(egg, 4);
        egg.checkExtra();
        egg.setupHP();
        //System.out.println("Name: " + egg.getName());
        //for (int j = 0; j < egg.getStats().length; j++) {
        //    System.out.println("Stat: " + j + " - " + egg.getStats()[j]);
        //}
        return egg;
    }
}
