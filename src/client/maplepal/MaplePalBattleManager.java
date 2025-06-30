package client.maplepal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import client.MapleCharacter;
import server.Randomizer;
import tools.packet.CField;

public class MaplePalBattleManager {

    private static final Map<Integer, MaplePalBattle> battles = new ConcurrentHashMap<>();

    public static void battlePlayer(MapleCharacter self, MapleCharacter foe) {
        self.battle = true;
        boolean simple = self.getAccVara("simple_battle") > 0;
        int backgroundId = Randomizer.random(1, 40);
        MaplePalBattle battle = new MaplePalBattle(self.getId(), foe.getId(), true, backgroundId);
        battle.pvp = true;
        battle.player = self;
        battle.foe = foe;
        battle.setSelfPals(self.getPalStorage().getActivePals());
        battle.setFoePals(foe.getPalStorage().getActivePals());
        battle.setFoeName(foe.getName());
        battle.doBattle(self);
        registerBattle(battle);
        self.getClient().announce(CField.musicChange("BgmFF8/Force_your_way"));
        self.getClient().announce(MaplePalPacket.sendBattle(battle));
    }

    public static void getBattleNpc(int id) {

    }

    public static void battleNpc(MapleCharacter self, int npcId) {
        self.battle = true;
        boolean simple = self.getAccVara("simple_battle") > 0;
        TrainerTemplate.Trainer npc = TrainerTemplate.loadNpc(npcId);
        List<MaplePal> selfPals = new ArrayList<>();
        List<MaplePal> foePals = new ArrayList<>();
        int palCount = Randomizer.random(npc.min_pal(), npc.max_pal());
        for (int i = 0; i < palCount; i++) {
            int e = Randomizer.random(1, 4);
            int m = PalTemplateProvider.getPalsbyEvoLock(e).get(Randomizer.nextInt(PalTemplateProvider.getPalsbyEvoLock(e).size()));
            MaplePal foe = MaplePalTrainer.createTempPal(m, Randomizer.random(npc.min_level(), npc.max_level()), npc.tier(), npc.rank(), 50); //TODO:generate random pals
            foe.setupHP();
            foePals.add(foe);
        }
        int backgroundId = npc.bg();
        MaplePalBattle battle = new MaplePalBattle(self.getId(), npcId, false, backgroundId);

        for (MaplePal pal : self.getPalStorage().getActivePals()) {
            pal.setupHP();
            selfPals.add(pal);
        }

        battle.setSelfPals(selfPals);
        battle.setFoePals(foePals);
        battle.doBattle(self);
        registerBattle(battle);
        //self.getClient().announce(CField.UIPacket.IntroLock(true));
        //self.getClient().announce(CField.UIPacket.IntroDisableUI(true));
        self.getClient().announce(MaplePalPacket.sendBattle(battle));
        int e = Randomizer.random(1, 5);
        if (e == 1) {
            self.getClient().announce(CField.musicChange("BgmFF7/Fighting"));
        }
        if (e == 2) {
            self.getClient().announce(CField.musicChange("BgmFF8/Dont_be_Afraid"));
        }
        if (e == 3) {
            self.getClient().announce(CField.musicChange("BgmFF8/Force_your_way"));
        }
        if (e == 4) {
            self.getClient().announce(CField.musicChange("BgmFF7/Weapon_Raid"));
        }
        if (e == 5) {
            self.getClient().announce(CField.musicChange("BgmFF7/Jenova"));
        }
    }

    public static void battleNpc(MapleCharacter self, int npcId, int bg, int level, int min_level, int max_level, int min_pal, int max_pal, int iv, boolean rewards) {
        self.battle = true;
        boolean simple = self.getAccVara("simple_battle") > 0;
        List<MaplePal> selfPals = new ArrayList<>();
        List<MaplePal> foePals = new ArrayList<>();
        int palCount = Randomizer.random(min_pal, max_pal);
        for (int i = 0; i < palCount; i++) {
            int e = Randomizer.random(1, 4);
            int m = PalTemplateProvider.getPalsbyEvoLock(e).get(Randomizer.nextInt(PalTemplateProvider.getPalsbyEvoLock(e).size()));
            MaplePal foe = MaplePalTrainer.createTempPal(m, Randomizer.random(min_level, max_level), 1, 1, iv); //TODO:generate random pals
            foe.setupHP();
            foePals.add(foe);
        }
        MaplePalBattle battle = new MaplePalBattle(self.getId(), npcId, false, bg);

        for (MaplePal pal : self.getPalStorage().getActivePals()) {
            pal.setupHP();
            selfPals.add(pal);
        }
        battle.rewards = rewards;
        battle.level = level;
        battle.setSelfPals(selfPals);
        battle.setFoePals(foePals);
        battle.doBattle(self);
        registerBattle(battle);
        //self.getClient().announce(CField.UIPacket.IntroLock(true));
        //self.getClient().announce(CField.UIPacket.IntroDisableUI(true));
        self.getClient().announce(MaplePalPacket.sendBattle(battle));
        int e = Randomizer.random(1, 4);
        if (e == 1) {
            self.getClient().announce(CField.musicChange("BgmFF7/Fighting"));
        }
        if (e == 2) {
            self.getClient().announce(CField.musicChange("BgmFF8/Dont_be_Afraid"));
        }
        if (e == 3) {
            self.getClient().announce(CField.musicChange("BgmFF8/Force_your_way"));
        }
        if (e == 4) {
            self.getClient().announce(CField.musicChange("BgmFF7/Weapon_Raid"));
        }
        if (e == 5) {
            self.getClient().announce(CField.musicChange("BgmFF7/Jenova"));
        }
    }

    public static void battleSuperNpc(MapleCharacter self, int npcId, int bg, int level, int min_level, int max_level, int min_pal, int max_pal, int iv, double multi, boolean rewards) {
        self.battle = true;
        boolean simple = self.getAccVara("simple_battle") > 0;
        List<MaplePal> selfPals = new ArrayList<>();
        List<MaplePal> foePals = new ArrayList<>();
        int palCount = Randomizer.random(min_pal, max_pal);
        for (int i = 0; i < palCount; i++) {
            int m = PalTemplateProvider.getPals().get(Randomizer.nextInt(PalTemplateProvider.getPals().size()));
            MaplePal foe = MaplePalTrainer.createSuperTempPal(m, Randomizer.random(min_level, max_level), 1, 1, iv, multi); //TODO:generate random pals
            foe.setupHP();
            foePals.add(foe);
        }
        MaplePalBattle battle = new MaplePalBattle(self.getId(), npcId, false, bg);

        for (MaplePal pal : self.getPalStorage().getActivePals()) {
            pal.setupHP();
            selfPals.add(pal);
        }
        battle.rewards = rewards;
        battle.level = level;
        battle.kaotic = true;
        battle.power = multi;
        battle.setSelfPals(selfPals);
        battle.setFoePals(foePals);
        battle.doBattle(self);
        registerBattle(battle);
        self.getClient().announce(MaplePalPacket.sendBattle(battle));
        int e = Randomizer.random(1, 13);
        switch (e) {
            case 1 ->
                self.getClient().announce(CField.musicChange("BgmFF7/Fighting"));
            case 2 ->
                self.getClient().announce(CField.musicChange("BgmFF8/Dont_be_Afraid"));
            case 3 ->
                self.getClient().announce(CField.musicChange("BgmFF8/Force_your_way"));
            case 4 ->
                self.getClient().announce(CField.musicChange("BgmFF7/Weapon_Raid"));
            case 5 ->
                self.getClient().announce(CField.musicChange("BgmFF7/Jenova"));
            case 6 ->
                self.getClient().announce(CField.musicChange("BgmFF7/BirthofGod"));
            case 7 ->
                self.getClient().announce(CField.musicChange("BgmFF7/Jenova_Absolute"));
            case 8 ->
                self.getClient().announce(CField.musicChange("BgmFF7/Stillmorefighting"));
            case 9 ->
                self.getClient().announce(CField.musicChange("BgmFF7/hurryfaster"));
            case 10 ->
                self.getClient().announce(CField.musicChange("BgmFF8/Man_with_Machine_Gun"));
            case 11 ->
                self.getClient().announce(CField.musicChange("BgmFF8/Wounded"));
            case 12 ->
                self.getClient().announce(CField.musicChange("BgmFF8/Premotion"));
            case 13 ->
                self.getClient().announce(CField.musicChange("BgmFF8/Stage_is_set"));
        }
    }

    public static void battleSuperNpc(MapleCharacter self, int npcId, int bg, int level, int min_level, int max_level, int min_pal, int max_pal, int iv, double multi, boolean rewards, int ach) {
        self.battle = true;
        boolean simple = self.getAccVara("simple_battle") > 0;
        List<MaplePal> selfPals = new ArrayList<>();
        List<MaplePal> foePals = new ArrayList<>();
        int palCount = Randomizer.random(min_pal, max_pal);
        for (int i = 0; i < palCount; i++) {
            int m = PalTemplateProvider.getPals().get(Randomizer.nextInt(PalTemplateProvider.getPals().size()));
            MaplePal foe = MaplePalTrainer.createSuperTempPal(m, Randomizer.random(min_level, max_level), 1, 1, iv, multi); //TODO:generate random pals
            foe.setupHP();
            foePals.add(foe);
        }
        MaplePalBattle battle = new MaplePalBattle(self.getId(), npcId, false, bg);

        for (MaplePal pal : self.getPalStorage().getActivePals()) {
            pal.setupHP();
            selfPals.add(pal);
        }
        battle.rewards = rewards;
        battle.level = level;
        battle.kaotic = true;
        battle.power = multi;
        battle.ach = ach;
        battle.setSelfPals(selfPals);
        battle.setFoePals(foePals);
        battle.doBattle(self);
        registerBattle(battle);
        self.getClient().announce(MaplePalPacket.sendBattle(battle));
        int e = Randomizer.random(1, 13);
        switch (e) {
            case 1 ->
                self.getClient().announce(CField.musicChange("BgmFF7/Fighting"));
            case 2 ->
                self.getClient().announce(CField.musicChange("BgmFF8/Dont_be_Afraid"));
            case 3 ->
                self.getClient().announce(CField.musicChange("BgmFF8/Force_your_way"));
            case 4 ->
                self.getClient().announce(CField.musicChange("BgmFF7/Weapon_Raid"));
            case 5 ->
                self.getClient().announce(CField.musicChange("BgmFF7/Jenova"));
            case 6 ->
                self.getClient().announce(CField.musicChange("BgmFF7/BirthofGod"));
            case 7 ->
                self.getClient().announce(CField.musicChange("BgmFF7/Jenova_Absolute"));
            case 8 ->
                self.getClient().announce(CField.musicChange("BgmFF7/Stillmorefighting"));
            case 9 ->
                self.getClient().announce(CField.musicChange("BgmFF7/hurryfaster"));
            case 10 ->
                self.getClient().announce(CField.musicChange("BgmFF8/Man_with_Machine_Gun"));
            case 11 ->
                self.getClient().announce(CField.musicChange("BgmFF8/Wounded"));
            case 12 ->
                self.getClient().announce(CField.musicChange("BgmFF8/Premotion"));
            case 13 ->
                self.getClient().announce(CField.musicChange("BgmFF8/Stage_is_set"));
        }
    }

    public static void randomBattle(MapleCharacter self, int bg, int level, int min_level, int max_level, int min_pal, int max_pal, int iv, boolean rewards) {
        self.battle = true;
        boolean simple = self.getAccVara("simple_battle") > 0;
        List<MaplePal> selfPals = new ArrayList<>();
        List<MaplePal> foePals = new ArrayList<>();
        int palCount = Randomizer.random(min_pal, max_pal);
        int e = 1;
        if (level >= 50) {
            e = 2;
        }
        if (level >= 80) {
            e = 3;
        }
        int m = PalTemplateProvider.getPalsbyEvoLock(e).get(Randomizer.nextInt(PalTemplateProvider.getPalsbyEvoLock(e).size()));
        MaplePal foe = MaplePalTrainer.createTempPal(m, Randomizer.random(min_level, max_level), 1, 1, iv); //TODO:generate random pals
        foe.setupHP();
        foePals.add(foe);
        MaplePalBattle battle = new MaplePalBattle(self.getId(), Randomizer.random(100000000, 200000000), false, bg);

        for (MaplePal pal : self.getPalStorage().getActivePals()) {
            pal.setupHP();
            selfPals.add(pal);
        }
        battle.randomPalId = foe.templateId;
        battle.random = true;
        battle.rewards = rewards;
        battle.setSelfPals(selfPals);
        battle.setFoePals(foePals);
        battle.doBattle(self);
        registerBattle(battle);
        //self.getClient().announce(CField.UIPacket.IntroLock(true));
        //self.getClient().announce(CField.UIPacket.IntroDisableUI(true));
        self.getClient().announce(MaplePalPacket.sendBattle(battle));
        if (e == 1) {
            self.getClient().announce(CField.musicChange("BgmFF7/Fighting"));
        }
        if (e == 2) {
            self.getClient().announce(CField.musicChange("BgmFF8/Dont_be_Afraid"));
        }
        if (e == 3) {
            self.getClient().announce(CField.musicChange("BgmFF8/Force_your_way"));
        }
        if (e == 4) {
            self.getClient().announce(CField.musicChange("BgmFF7/Weapon_Raid"));
        }
        if (e == 5) {
            self.getClient().announce(CField.musicChange("BgmFF7/Jenova"));
        }
    }

    public static void registerBattle(MaplePalBattle battle) {
        battles.put(battle.selfCharId, battle);
    }

    public static MaplePalBattle tryFinish(int charId) {
        MaplePalBattle ret = battles.remove(charId);
        if (ret.startTime == 0 || ret.startTime + (int) (ret.bd.getTotalTime() * 0.8) > System.currentTimeMillis()) { //Make sure battle is started and lasts long enough
            //log *potential* packet edit?
            return null;
        }
        return ret;
    }
}
