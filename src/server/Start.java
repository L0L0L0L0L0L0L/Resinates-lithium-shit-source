package server;

import client.MapleCharacter;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryIdentifier;
import client.maplepal.PalTemplateProvider;
import constants.GameConstants;
import constants.ServerConstants;
import handling.MapleServerHandler;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.login.LoginServer;
import handling.login.LoginInformationProvider;
import handling.world.World;
import java.sql.SQLException;
import database.DatabaseConnection;
import database.Jdbi;
import handling.world.family.MapleFamily;
import handling.world.guild.MapleGuild;
import java.awt.Point;
import java.security.Security;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import server.Timer.*;
import server.events.MapleOxQuizFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.MobSkillFactory;
import server.life.PlayerNPC;
import server.quest.MapleQuest;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import tools.AutoJCE;

public class Start {

    private static Start instance = null;

    public static final boolean JAVA_8 = true;

    public static Start getInstance() {
        if (instance == null) {
            instance = new Start();
        }
        return instance;
    }
    public static long startTime = System.currentTimeMillis();
    //public static final Start instance = new Start();
    public static AtomicInteger CompletedLoadingThreads = new AtomicInteger(0);
    private final AtomicLong currentTime = new AtomicLong(0);
    private long serverCurrentTime = 0;
    public static long uptime = System.currentTimeMillis();
    private ScheduledFuture<?> schedulerTask = null;

    public static void main(String args[]) {
        System.setProperty("wzpath", "wz");
        Security.setProperty("crypto.policy", "unlimited");
        if (!JAVA_8) {
            AutoJCE.removeCryptographyRestrictions();
        }
        Start.getInstance().init();
    }

    public int getCurrentTimestamp() {
        return (int) (Start.getInstance().getCurrentTime() - Start.uptime);
    }

    public long getCurrentTime() {
        long nTime = System.currentTimeMillis();
        return nTime - nTime % 100;
    }

    public void updateCurrentTime() {
        serverCurrentTime = currentTime.addAndGet(ServerConstants.UPDATE_INTERVAL);
    }

    public long forceUpdateCurrentTime() {
        long timeNow = System.currentTimeMillis();
        serverCurrentTime = timeNow;
        currentTime.set(timeNow);

        return timeNow;
    }

    public void init() {
        //new DatabaseConnection();
        System.out.println("[" + ServerProperties.getProperty("net.sf.odinms.login.serverName") + "]");
        World.init();
        Jdbi.j = DatabaseConnection.createJdbi();
        TimerManager tMan = TimerManager.getInstance();
        tMan.start();
        tMan.register(tMan.purge(), ServerConstants.PURGING_INTERVAL);//Purging ftw...
        ThreadManager.getInstance().start();
        WorldTimer.getInstance().start();
        TimerManager.getInstance().start();
        EtcTimer.getInstance().start();
        MapTimer.getInstance().start();
        CloneTimer.getInstance().start();
        EventTimer.getInstance().start();
        BuffTimer.getInstance().start();
        PingTimer.getInstance().start();
        System.out.println("Timers Loaded...");
        MapleGuildRanking.getInstance().load();
        MapleGuild.loadAll(); //(this); 
        System.out.println("Guilds Loaded...");
        MapleFamily.loadAll(); //(this); 
        System.out.println("Families Loaded...");
        MapleLifeFactory.loadQuestCounts();
        System.out.println("Quest Counts Loaded...");
        MapleQuest.initQuests();
        System.out.println("Quests Loaded...");
        MapleItemInformationProvider.getInstance().runEtc();
        MapleItemInformationProvider.getInstance().runItems();
        MapleItemInformationProvider.getInstance().loadCosmetics();
        MapleItemInformationProvider.getInstance().loadGachFromDB();
        MapleItemInformationProvider.getInstance().loadNamesFromDB();
        System.out.println("Items Loaded...");
        SkillFactory.load();
        System.out.println("Skills Loaded...");
        MapleMonsterInformationProvider.getInstance().load();
        MapleMonsterInformationProvider.getInstance().loadItemMobs();
        //BattleConstants.init(); 
        System.out.println("Monsters Loaded...");
        LoginInformationProvider.getInstance();
        MapleOxQuizFactory.getInstance();
        MapleCarnivalFactory.getInstance();
        MobSkillFactory.getInstance();
        SpeedRunner.loadSpeedRuns();
        MTSStorage.load();
        MapleInventoryIdentifier.getInstance();
        CashItemFactory.getInstance().initialize();
        MapleServerHandler.initiate();
        System.out.println("Misc Loaded...");
        System.out.println("[Loading Login]");
        LoginServer.run_startup_configurations();
        System.out.println("[Login Initialized]");

        System.out.println("[Loading Channel]");
        ChannelServer.startChannel_Main();
        System.out.println("[Channel Initialized]");

        //System.out.println("[Loading CS]");
        //CashShopServer.run_startup_configurations();
        //System.out.println("[CS Initialized]");
        //CheatTimer.getInstance().register(AutobanManager.getInstance(), 60000);
        //Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));
        //World.registerRespawn();
        //ChannelServer.getInstance(1).getMapFactory().getMap(910000000).spawnRandDrop(); //start it off
        //ShutdownServer.registerMBean();
        //ServerConstants.registerMBean();
        PlayerNPC.loadAll();// touch - so we see database problems early...
        //MapleMonsterInformationProvider.getInstance().addExtra();
        runCleaner();
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = 0, templogin = 0")) {
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("[EXCEPTION] Please check if the SQL server is active.");
        }
        tMan.register(new BuffWorker(), 1000);
        tMan.register(new EventWorker(), 60000);

        //tMan.register(new BotWorker(), Randomizer.random(60, 120) * 60 * 1000);
        //tMan.register(new VoteWorker(), Randomizer.random(5, 10) * 60 * 1000);
        MapleAchievements.loadAchievements();
        PalTemplateProvider.load();
        MapleDamageSkins.loadDamageSkins();
        MapleKQuests.loadQuests();
        GameConstants.loadNpcs();
        MapleLifeFactory.forceLoad();
        //MapleShopFactory.getInstance().getShop(9300004).changeNXShop();
        //MapleShopFactory.getInstance().getShop(2050012).changeQuestShop();
        //MapleShopFactory.getInstance().getShop(9400829).changeRandomDPShop();
        MapleMapFactory.loadMapNames();
        System.out.println("Map Names stored.");
        Parks.loadParks();
        System.out.println("Dungeons Pre-Loaded.");
        LoginServer.setOn(); //now or later
        MapleShopFactory.getInstance().executeShops();
        System.out.println("Shops Loaded...");
        System.out.println("[Fully Initialized in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds]");
        //dropEvent();
        loadServerData();
        serverVar();

        GameConstants.setStatRate(1);
        GameConstants.setDonationRate(60);
        //GameConstants.shufflePals();

        //System.out.println("[Pre-Loading All maps]");
        //for (ChannelServer cserv : ChannelServer.getAllInstances()) {
        //    cserv.getMapFactory().loadAllMaps();
        //}
        //System.out.println("[All maps loaded into memory]");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook starting....");
            shutdown();
        }));
    }

    public void shutdown() {
        try {
            System.out.println("System shutdown starting....");
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter victim : cserv.getPlayerStorage().getAllCharacters()) {
                    victim.dropMessage(-6, "Server is rebooting for updates in 30 Seconds.");
                    victim.dropMessage(-6, "Server is rebooting for updates in 30 Seconds.");
                    victim.dropMessage(-6, "Server is rebooting for updates in 30 Seconds.");
                }
                LoginServer.setOff();
                GameConstants.setLockSlot(true);
                GameConstants.setLock(true);
                GameConstants.shutdown = true;
                TimerManager.getInstance().schedule(() -> {
                    for (MapleCharacter player : cserv.getPlayerStorage().getAllCharacters()) {
                        if (player != null) {
                            player.shoptime = 0;
                            if (player.getGMLevel() < 4) {
                                System.out.println("Unlooading Player: " + player.getName() + "");
                                if (player.getEventInstance() != null) {
                                    player.getEventInstance().exitPlayer(player);
                                }
                                player.kick();
                            }
                        }
                    }
                    System.out.println("All Players have been saved and removed.");
                    cserv.disposeEvents();
                    System.out.println("All events have been closed.");
                    World.Guild.save();
                    World.Alliance.save();
                    World.Family.save();
                    cserv.saveServerVar();
                    System.out.println("World Saved.");
                    System.out.println("Shutdown completed.");
                }, 30000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadServerData() {
        try (Connection con = DatabaseConnection.getWorldConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `server_variables`");
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    GameConstants.setServerVar(rs.getString("var"), rs.getLong("amount"));
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void serverVar() {
        TimerManager.getInstance().schedule(() -> {
            if (!LoginServer.isShutdown()) {
                if (GameConstants.getServerVar("KP_Rate") > 0) {
                    GameConstants.setServerVar("KP_Rate", GameConstants.getServerVar("KP_Rate") - 1);
                }
                serverVar();
            }
        }, 60000);
    }

    public void dropEvent() {
        long time = 1000 * 60 * Randomizer.random(240, 480);
        TimerManager.getInstance().schedule(() -> {
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                dropEvent(cserv);
            }
        }, time);
    }

    public void dropEvent(final ChannelServer cserv) {
        cserv.dropMessage(-6, "[FM-EVENT] Drop event starting in 10 mins in Free Market.");
        TimerManager.getInstance().schedule(() -> {
            cserv.dropMessage(-6, "[FM-EVENT] Drop event starting in 5 mins in Free Market.");
        }, 1000 * 60 * 5);//5
        TimerManager.getInstance().schedule(() -> {
            cserv.dropMessage(-6, "[FM-EVENT] Drop event starting in 1 min in Free Market.");
        }, 1000 * 60 * 9);//9
        TimerManager.getInstance().schedule(() -> {
            cserv.dropMessage(-6, "[FM-EVENT] Thank you guys for the support. Enjoy!");
            List<Integer> itemz = Arrays.asList(new Integer[]{4310337, 2049032, 2450020, 2587000, 2049304, 2049188, 5220020, 4031034, 4000313, 2583001, 2583002, 2583007, 2585002, 2585003, 4310501, 2340000, 4310015, 4310020, 4310211, 4310066, 2586000, 2587001, 2049189, 2586000, 2586001, 2586002, 2585004, 2585005, 4420001, 4420009, 4420002, 4420003, 4420004, 4420005, 4420006, 4420008, 4420021, 4420015, 4310502, 2000012, 4201000, 4200009, 4200010, 4202015, 4202017, 4202018, 4202019, 4202028, 4202069, 4310510, 4202040, 4202041, 4202042, 4202043, 4202044, 4202045, 4202046, 4202047, 4202024, 4202025});
            MapleMap map = cserv.getMapFactory().getMap(870000008);
            if (itemz != null && !itemz.isEmpty() && !map.getPlayers().isEmpty()) {
                for (int i = 0; i < 1000; i++) {
                    int itemid = itemz.get(Randomizer.nextInt(itemz.size()));
                    Item idrop = new Item(itemid, (byte) 0, (short) Randomizer.random(1, 4), (byte) 0);
                    Point startpos = new Point(Randomizer.random(map.getLeft(), map.getRight()), Randomizer.random(map.getTop(), map.getBottom()));
                    map.spawnAutoDrop(idrop, (byte) 3, startpos);
                }
            }
            dropEvent();
        }, 1000 * 60 * 10);//10
    }

    public void runCleaner() {
        long timeToTake = System.currentTimeMillis();
        PreparedStatement ps;
        try (Connection con = DatabaseConnection.getPlayerConnection()) {
            ps = con.prepareStatement("DELETE FROM characters WHERE accountid NOT IN (SELECT id FROM accounts)");
            ps.executeUpdate();
            ps.close();
            System.out.println("characters cleaned in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");
            ps = con.prepareStatement("DELETE FROM inventoryitems WHERE characterid NOT IN (SELECT id FROM characters)");
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM inventoryequips WHERE characterid NOT IN (SELECT id FROM characters)");
            ps.executeUpdate();
            ps.close();
            System.out.println("inventoryitems cleaned in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");

            ps = con.prepareStatement("DELETE FROM skills WHERE characterid NOT IN (SELECT id FROM characters)");
            ps.executeUpdate();
            ps.close();
            System.out.println("skills cleaned in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");

            ps = con.prepareStatement("DELETE FROM skillmacros WHERE characterid NOT IN (SELECT id FROM characters)");
            ps.executeUpdate();
            ps.close();
            System.out.println("skillmacros cleaned in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");

            ps = con.prepareStatement("DELETE FROM keymap WHERE characterid NOT IN (SELECT id FROM characters)");
            ps.executeUpdate();
            ps.close();
            System.out.println("keymap cleaned in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");

            ps = con.prepareStatement("DELETE FROM queststatus WHERE characterid NOT IN (SELECT id FROM characters)");
            ps.executeUpdate();
            ps.close();
            System.out.println("queststatus cleaned in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");

            ps = con.prepareStatement("DELETE FROM questinfo WHERE characterid NOT IN (SELECT id FROM characters)");
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM achievements WHERE accountid NOT IN (SELECT id FROM accounts)");
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM quests WHERE accountid NOT IN (SELECT id FROM accounts)");
            ps.executeUpdate();
            ps.close();

            System.out.println("questinfo cleaned in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");
            System.out.println("Database cleaned in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");
        } catch (SQLException ex) {
            throw new RuntimeException("[EXCEPTION] Please check if the SQL server is active.");
        }
    }

    public static class Shutdown implements Runnable {

        @Override
        public void run() {
            ShutdownServer.getInstance().run();
        }
    }

}
