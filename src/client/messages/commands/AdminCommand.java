package client.messages.commands;

import client.MapleCharacter;
import constants.ServerConstants.PlayerGMRank;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;

import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.World;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import scripting.ReactorScriptManager;
import server.MapleAchievements;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.Randomizer;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;

/**
 *
 * @author Emilyx3
 */
public class AdminCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.ADMIN;
    }

    public static class slots extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                c.announce(CField.showEffect("miro2/frame"));
                c.announce(CField.showEffect("miro2/RR1/" + Randomizer.random(0, 19)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    public static class setRate extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                int rate = Integer.parseInt(splitted[1]);
                GameConstants.setStatRate(rate);
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    cserv.dropMessage("Boss Stat Rate is now set to " + rate + "x!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 1;
        }
    }

    public static class setBaseRate extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                int rate = Integer.parseInt(splitted[1]);
                GameConstants.setBaseStatRate(rate);
                c.getPlayer().dropMessage("Base Rates now set to " + GameConstants.getbaseStatRate());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return 1;
        }
    }

    public static class antibot extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                c.announce(CWvsContext.sendLieDetector(null)); //give_buff with no data :D
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 1;
        }
    }

    public static class dcall extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
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
                        c.getPlayer().dropMessage("All Players have been saved and removed.");
                        cserv.disposeEvents();
                        System.out.println("All events have been closed.");
                        c.getPlayer().dropMessage("All events have been closed.");
                        World.Guild.save();
                        World.Alliance.save();
                        World.Family.save();
                        System.out.println("World Saved.");
                        c.getPlayer().dropMessage("World Saved.");

                        System.out.println("Server is safe to close.");
                        c.getPlayer().dropMessage("Server is safe to close.");
                        cserv.saveServerVar();
                    }, 30000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return 1;
        }
    }

    public static class Saveall extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "Current players online: " + ChannelServer.getAllInstances().get(0).getPlayerStorage().getAllCharacters().size());

            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter victim : cserv.getPlayerStorage().getAllCharacters()) {

                    victim.saveToDB(false, false);
                }
            }
            c.getPlayer().dropMessage(6, "All Players are saved.");
            return 1;
        }
    }

    public static class lock extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().toggleEeventLock(true);
            c.getPlayer().dropMessage(6, "All Players are saved.");
            return 1;
        }
    }

    public static class mp extends CommandExecute {

        protected static Thread t = null;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    int amount = 0;
                    try {
                        amount = Integer.parseInt(splitted[2]);
                    } catch (NumberFormatException nfe) {
                        c.getPlayer().dropMessage(6, "Invalid Number...");
                        return 0;
                    }
                    if (amount > 0) {
                        Item item = new client.inventory.Item(4310501, (byte) 0, (short) amount, (byte) 0);
                        if (!MapleInventoryManipulator.addFromDrop(victim.getClient(), item)) {
                            victim.dropMessage(6, "Please make space in your ETC for maple points");
                            c.getPlayer().dropMessage(6, victim.getName() + " does not have room for maplepoints");
                            return 1;
                        }
                        victim.dropMessage(6, "you have recieved " + amount + " Maple Points. Check your ETC.");
                        c.getPlayer().dropMessage(6, victim.getName() + " has recieved " + amount + " Maple Points.");
                    }
                } else {
                    c.getPlayer().dropMessage(6, "Player is null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    public static class dp extends CommandExecute {

        protected static Thread t = null;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null) {
                    int amount = 0;
                    try {
                        amount = Integer.parseInt(splitted[2]);
                    } catch (NumberFormatException nfe) {
                        c.getPlayer().dropMessage(6, "Invalid Number...");
                        return 0;
                    }
                    if (amount > 0) {
                        //victim.modifyCSPoints(2, amount, true);
                        if (victim.canHold(4310505, amount)) {
                            victim.gainItem(4310505, amount, " admin sending Infinity Points");
                            victim.dropMessage(6, "you have recieved " + amount + " Infinity Points. Check your ETC.");
                            victim.updateMP(amount);
                            victim.saveToDB(false, false);
                            Date date = new Date(System.currentTimeMillis());
                            SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss z");
                            c.getPlayer().dropMessage(6, victim.getName() + " has recieved " + amount + " Infinity Points - IP [" + victim.getClient().getSessionIPAddress() + "] on " + formatter.format(date));
                        } else {
                            c.getPlayer().dropMessage(1, victim.getName() + " does not have room to hold this.");
                        }
                    }
                } else {
                    c.getPlayer().dropMessage(6, "Player is null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    public static class Killall extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            //c.getPlayer().getMap().killAllMonsters(true);
            c.getPlayer().getMap().killMonsters(c.getPlayer());
            return 1;
        }
    }

    public static class Killnpc extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().destoryNPC();
            return 1;
        }
    }

    public static class Respawnnpc extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().resetNPC();
            return 1;
        }
    }

    public static class reloadall extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMonsterInformationProvider.getInstance().clearDrops();
            MapleShopFactory.getInstance().clear();
            c.getPlayer().dropMessage(6, "Drop Data and Shops reloaded.");
            return 1;
        }
    }

    public static class ReloadDrops extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMonsterInformationProvider.getInstance().clearDrops();
            ReactorScriptManager.getInstance().clearDrops();
            c.getPlayer().dropMessage(6, "Drop Data reloaded.");
            return 1;
        }
    }

    public static class ReloadShops extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleShopFactory.getInstance().clear();
            MapleShopFactory.getInstance().executeShops();
            c.getPlayer().dropMessage(6, "All Shops reloaded.");
            return 1;
        }
    }

    public static class ReloadShop extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int id = Integer.parseInt(splitted[1]);
            boolean npc = Integer.parseInt(splitted[2]) > 0;
            MapleShopFactory.getInstance().removeShop(id);
            MapleShopFactory.getInstance().reloadShop(id, npc);
            c.getPlayer().dropMessage(6, "Shop: " + id + " has been reset.");
            return 1;
        }
    }

    public static class shop extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleShopFactory.getInstance().getShop(9999).sendShop(c);
            return 1;
        }
    }

    public static class getAch extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (int i : MapleAchievements.getInstance().getAchievementsIds()) {
                System.out.println("Achs ID: " + i + " A-ID: " + MapleAchievements.getInstance().getById(i).getId());
            }
            return 1;
        }
    }

    /*
    public static class Monitor extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (target != null) {
                if (target.getClient().isMonitored()) {
                    target.getClient().setMonitored(false);
                    c.getPlayer().dropMessage(5, "Not monitoring " + target.getName() + " anymore.");
                } else {
                    target.getClient().setMonitored(true);
                    c.getPlayer().dropMessage(5, "Monitoring " + target.getName() + ".");
                }
            } else {
                c.getPlayer().dropMessage(5, "Target not found on channel.");
                return 0;
            }
            return 1;
        }
    }
     */
    public static class damagecap extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                GameConstants.setCapped(!GameConstants.getCapped());
                cserv.dropMessage("Server Caps currently: " + GameConstants.getCapped());
            }
            return 1;
        }
    }

    public static class item extends CommandExecute {

        protected static Thread t = null;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                int id = Integer.parseInt(splitted[1]);
                int amount = Integer.parseInt(splitted[2]);
                MapleCharacter chr = c.getPlayer();
                if (GameConstants.isPet(id)) {
                    if (c.getPlayer().canHold(id)) {
                        System.out.println("pet Created by: " + c.getPlayer().getName());
                        c.getPlayer().gainItem(id, 1, "pet Created by: " + c.getPlayer().getName());
                        return 1;
                    }
                } else {
                    System.out.println("item Created by: " + c.getPlayer().getName());
                    while (amount > 0) {
                        Item item = null;
                        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        if (GameConstants.getInventoryType(id) == MapleInventoryType.EQUIP) {
                            item = ii.randomizeStats(chr, (Equip) ii.getEquipById(id), 1);
                        } else {
                            item = new client.inventory.Item(id, (byte) 0, (short) (amount > 30000 ? 30000 : amount), (byte) 0);
                        }
                        if (item != null && !MapleInventoryManipulator.addFromDrop(chr.getClient(), item)) {
                            return 1;
                        }
                        amount -= (amount > 30000 ? 30000 : amount);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    /*
    public static class eventinstance extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            String event = splitted[1];
            if (player.getRaid() == null) {
                if (player.getParty() != null) {
                    if (player.isLeader()) {
                        if (!player.isMapChange() && player.isAlive()) {
                            if (player.getEventInstance() == null) {
                                EventManager em = c.getChannelServer().getEventSM().getEventManager(event);
                                if (em.getEligibleParty(player)) {
                                    if (!em.startPlayerInstance(player, player.getMapId())) {
                                        player.dropMessage("Error with making personal instance.");
                                    } else {
                                        player.dropMessage("Once time is ran out, you will be warped back to town.");
                                    }
                                } else {
                                    player.dropMessage("Not all party members are in same map.");
                                }
                            } else {
                                player.dropMessage("You are already inside an instance..");
                            }
                        } else {
                            player.dropMessage("Cannot create an instance while changing maps or dead.");
                        }
                    } else {
                        player.dropMessage("Only party leader can use this command.");
                    }
                } else {
                    player.dropMessage("Must be in a party to use this command.");
                }
            } else {
                player.dropMessage("Cannot create an instance while in a raid.");
            }
            return 1;
        }
    }
     */
    //unused===========================================================================================================================================================================================
    /*
    public static class StripEveryone extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            ChannelServer cs = c.getChannelServer();
            for (MapleCharacter mchr : cs.getPlayerStorage().getAllCharacters()) {
                if (mchr.isGM()) {
                    continue;
                }
                MapleInventory equipped = mchr.getInventory(MapleInventoryType.EQUIPPED);
                MapleInventory equip = mchr.getInventory(MapleInventoryType.EQUIP);
                List<Short> ids = new ArrayList<Short>();
                for (Item item : equipped.newList()) {
                    ids.add(item.getPosition());
                }
                for (short id : ids) {
                    MapleInventoryManipulator.unequip(mchr.getClient(), id, equip.getNextFreeSlot());
                }
            }
            return 1;
        }
    }

    public static class MesoEveryone extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.gainMeso(Integer.parseInt(splitted[1]), true);
                }
            }
            return 1;
        }
    }

    public static class ExpRate extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length > 1) {
                final int rate = Integer.parseInt(splitted[1]);
                if (splitted.length > 2 && splitted[2].equalsIgnoreCase("all")) {
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        cserv.setExpRate(rate);
                    }
                } else {
                    c.getChannelServer().setExpRate(rate);
                }
                c.getPlayer().dropMessage(6, "Exprate has been changed to " + rate + "x");
            } else {
                c.getPlayer().dropMessage(6, "Syntax: !exprate <number> [all]");
            }
            return 1;
        }
    }

    public static class MesoRate extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length > 1) {
                final int rate = Integer.parseInt(splitted[1]);
                if (splitted.length > 2 && splitted[2].equalsIgnoreCase("all")) {
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        cserv.setMesoRate(rate);
                    }
                } else {
                    c.getChannelServer().setMesoRate(rate);
                }
                c.getPlayer().dropMessage(6, "Meso Rate has been changed to " + rate + "x");
            } else {
                c.getPlayer().dropMessage(6, "Syntax: !mesorate <number> [all]");
            }
            return 1;
        }
    }
    
    public static class StartProfiling extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            CPUSampler sampler = CPUSampler.getInstance();
            sampler.addIncluded("client");
            sampler.addIncluded("constants"); //or should we do Packages.constants etc.?
            sampler.addIncluded("database");
            sampler.addIncluded("handling");
            sampler.addIncluded("provider");
            sampler.addIncluded("scripting");
            sampler.addIncluded("server");
            sampler.addIncluded("tools");
            sampler.start();
            return 1;
        }
    }

    public static class StopProfiling extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            CPUSampler sampler = CPUSampler.getInstance();
            try {
                String filename = "odinprofile.txt";
                if (splitted.length > 1) {
                    filename = splitted[1];
                }
                File file = new File(filename);
                if (file.exists()) {
                    c.getPlayer().dropMessage(6, "The entered filename already exists, choose a different one");
                    return 0;
                }
                sampler.stop();
                FileWriter fw = new FileWriter(file);
                sampler.save(fw, 1, 10);
                fw.close();
            } catch (IOException e) {
                System.err.println("Error saving profile" + e);
            }
            sampler.reset();
            return 1;
        }
    }
     */
    public static class globalEvent extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            String toggle = splitted[1];
            if ("true".equals(toggle)) {
                GameConstants.setGlobalEvent(true);
                c.getPlayer().dropMessage(6, "All bonus rates are active.");
            }
            if ("false".equals(toggle)) {
                GameConstants.setGlobalEvent(false);
                c.getPlayer().dropMessage(6, "All bonus rates are de-active.");
            }
            return 1;
        }
    }

    public static class setServerVar extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            String var = splitted[1];
            final long amount = Long.parseLong(splitted[2]);
            GameConstants.setServerVar(var, amount);
            return 1;
        }
    }

    public static class addServerVar extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            String var = splitted[1];
            final long amount = Long.parseLong(splitted[2]);
            GameConstants.addServerVar(var, amount);
            return 1;
        }
    }

    public static class Spawn extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            final int id = Integer.parseInt(splitted[1]);
            final int level = Integer.parseInt(splitted[2]);
            final int scale = Integer.parseInt(splitted[3]);
            final int type = Integer.parseInt(splitted[4]);
            MapleMonster mob = null;
            if (type == 1) {
                mob = MapleLifeFactory.getMonster(id, level, scale);
            }
            if (type == 2) {
                mob = MapleLifeFactory.getMonsterNoLink(id, level, scale);
            }
            if (type == 3) {
                mob = MapleLifeFactory.getMonsterNoDrops(id, level, scale);
            }
            if (type == 4) {
                mob = MapleLifeFactory.getMonsterNoAll(id, level, scale);
            }
            if (type == 5) {
                mob = MapleLifeFactory.getMonster(id, level, scale);
                mob.getStats().setRaidBoss(true);
                mob.getStats().setBoss(true);
                mob.getStats().setExplosiveReward(true);
                mob.getStats().setBar(true);
            }
            if (type == 6) {
                mob = MapleLifeFactory.getMonsterNoAll(id, level, scale);
                mob.getStats().setRaidBoss(true);
                mob.getStats().setBoss(true);
                mob.getStats().setExplosiveReward(true);
                mob.getStats().setBar(true);
            }
            if (type == 7) {
                mob = MapleLifeFactory.getKaoticMonster(id, level, scale);
                mob.getStats().setBar(true);
            }
            if (type == 8) {
                mob = MapleLifeFactory.getKaoticMonster(id, level, scale);
                mob.getStats().disableRevives();
                mob.getStats().setBar(true);
            }
            if (type == 9) {
                final int fixed = Integer.parseInt(splitted[5]);
                mob = MapleLifeFactory.getKaoticMonster(id, level, scale, scale >= 5, false, false, fixed);
                mob.getStats().disableRevives();
                mob.getStats().setBar(true);
            }
            if (type == 10) {
                mob = MapleLifeFactory.getKaoticMonster(id, level, scale, true, false, false);
                mob.setOwner(c.getPlayer());
                mob.setTotem();
                c.getPlayer().getMap().setTotem(c.getPlayer(), mob);
            }
            if (type == 11) {
                mob = MapleLifeFactory.getKaoticLinkMonsterMega(id, level, scale, true, false, false, 1, true);
            }
            if (type == 12) {
                final int fixed = Integer.parseInt(splitted[5]);
                mob = MapleLifeFactory.getKaoticLinkMonsterMega(id, level, scale, true, false, true, fixed, true);
            }
            if (mob != null) {
                if (!mob.getStats().getBar() && mob.getStats().getLives() > 0) {
                    mob.getStats().getBar();
                }
                c.getPlayer().getMap().forceSpawnMonster(mob, c.getPlayer().getPosition());
            }
            return 1;
        }
    }

    public static class npc extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            final int id = Integer.parseInt(splitted[1]);
            final int flip = Integer.parseInt(splitted[2]);
            c.getPlayer().getMap().spawnNpc(id, c.getPlayer().getPosition(), flip != 0);

            try (Connection con = DatabaseConnection.getWorldConnection()) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO npc_spawns (id, mapid, x, y, flip) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setInt(1, id);
                    ps.setInt(2, c.getPlayer().getMap().getId());
                    ps.setInt(3, c.getPlayer().getPosition().x);
                    ps.setInt(4, c.getPlayer().getPosition().y);
                    ps.setInt(5, flip);
                    ps.addBatch();
                    ps.executeBatch();
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            c.getSession().write(MobPacket.showMonsterHP(c.getPlayer().getObjectId(), 100));
            return 1;
        }
    }

    public static class testquest extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleNPC npc : c.getPlayer().getMap().getAllNPCs()) {
                System.out.println("test");
                //c.announce(CField.updateQuestInfo(c.getPlayer(), 1000, npc.getId(), (byte) 1));
                c.announce(CField.harvestMessage(npc.getObjectId(), GameConstants.GMS ? 13 : 11)); //ok to harvest, gogo
            }
            return 1;
        }
    }

    public static class damage extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {

            int x = Randomizer.Max(Integer.parseInt(splitted[1]), 250);//number of mobs
            int y = Randomizer.Max(Integer.parseInt(splitted[2]), 15);//number of lines
            int range = Integer.parseInt(splitted[3]);
            //List<MapleMonster> mobs = c.getPlayer().getMap().getAllMobsRange(c.getPlayer().getPosition(), 10000, 2500);
            c.getPlayer().getMap().damageMobs(c.getPlayer(), x, y, range, c.getPlayer().getPosition());

            return 1;
        }
    }
}
