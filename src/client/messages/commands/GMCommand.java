/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client.messages.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.maplepal.CraftingProcessor;
import client.maplepal.MaplePalPacket;
import constants.GameConstants;
import constants.ServerConstants.PlayerGMRank;
import database.DatabaseConnection;
import handling.MapleServerHandler;
import handling.SendPacketOpcode;
import handling.channel.ChannelServer;
import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import scripting.EventInstanceManager;
import scripting.NPCScriptManager;
import server.MapleDamageSkins;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleKQuests;
import server.Randomizer;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import tools.StringUtil;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CField;
import tools.packet.CField.EffectPacket;
import tools.packet.CField.UIPacket;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;

/**
 *
 * @author Emilyx3
 */
public class GMCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.GM;
    }

    public static class Warp extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getGMLevel() > 1) {
                final int id = Integer.parseInt(splitted[1]);
                c.getPlayer().changeMapbyId(id);
            } else {
                c.getPlayer().dropMessage("Not usable by player");
            }
            return 1;
        }
    }

    public static class Warpto extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                if (victim.getEventInstance() != null) {
                    victim.getEventInstance().registerPlayer(c.getPlayer());
                } else {
                    if (c.getPlayer().getGMLevel() > 1) {
                        c.getPlayer().forceChangeMap(victim.getMap(), victim.getMap().getPortal(0), victim.getPosition());
                    }
                }
            } else {
                c.getPlayer().dropMessage("Not usable by player");
            }
            return 1;
        }
    }

    public static class WarpMe extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (c.getPlayer().getGMLevel() > 1) {
                if (c.getPlayer().getEventInstance() != null) {
                    c.getPlayer().getEventInstance().warp(victim, c.getPlayer().getMapId());
                } else {
                    victim.forceChangeMap(c.getPlayer().getMap(), c.getPlayer().getMap().getPortal(0), c.getPlayer().getPosition());
                }

            } else {
                c.getPlayer().dropMessage("Not usable by player");
            }
            return 1;
        }
    }

    public static class killplayer extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                victim.kill();
            }
            return 1;
        }
    }

    public static class kick extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null && c.getPlayer().getGMLevel() >= victim.getGMLevel()) {
                //victim.getClient().announce(HexTool.getByteArrayFromHexString("1A 00")); //give_buff with no data :D
                victim.shoptime = 0;
                victim.kick();
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    for (MapleCharacter player : cserv.getPlayerStorage().getAllCharacters()) {
                        if (player != victim) {
                            player.dropMessage(6, victim.getName() + " has kicked the bucket.");
                        }
                    }
                }
                return 1;
            } else {
                c.getPlayer().dropMessage(6, "The victim does not exist.");
                return 1;
            }
        }
    }

    /*
    public static class bot extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null && !victim.getLock() && victim.isAlive() && victim.getEventInstance() == null && victim.getClient().getCMS() == null) {
                NPCScriptManager.getInstance().startNPC(victim.getClient(), 9010106, "bot");
                player.dropMessage(5, victim.getName() + " has been bot checked");
            }
            return 1;
        }
    }

    public static class APQ extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            player.getMap().setFlagsAQ_1();
            player.getMap().setFlagsAQ_2();
            player.getMap().setFlagsAQ_3();
            return 1;
        }
    }
     */
    public static class jail extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            int time = (Integer.parseInt(splitted[2]) * 3600);
            if (victim != null) {
                victim.setQuestLock(10000, time);
                victim.changeMapbyId(10);
                String times = StringUtil.secondsToString(victim.getQuestLock(10000));
                victim.dropMessage(1, "you have been jailed for breaking the rules\r\nTime remaining: " + times);
                player.dropMessage(victim.getName() + " has been sent to jail.");
            }
            return 1;
        }
    }

    public static class dropEvent extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getGMLevel() > 1) {
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final int count = Integer.parseInt(splitted[1]);
                MapleMap map = c.getPlayer().getMap();
                final int type = Integer.parseInt(splitted[2]);
                List<Integer> itemz = null;
                if (type == 1) {
                    itemz = Arrays.asList(new Integer[]{4310500, 4310501, 4310502, 4310506, 4420001, 4420002, 4420003, 4420004, 4420005, 2049189, 2049305, 2583000, 2585005, 2586002, 4420015, 4420009, 4420052});
                } else {
                    c.getPlayer().dropMessage("Not usable type.");
                    return 1;
                }
                if (itemz != null && !itemz.isEmpty()) {
                    for (int i = 0; i < count; i++) {
                        int itemid = itemz.get(Randomizer.nextInt(itemz.size() - 1));
                        Item idrop;
                        if (GameConstants.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
                            idrop = ii.randomizeStats((Equip) ii.getEquipById(itemid), Randomizer.random(1, 10));
                        } else {
                            idrop = new Item(itemid, (byte) 0, (short) 1, (byte) 0);
                        }
                        if (idrop != null) {
                            Point startpos = new Point(Randomizer.random(map.getLeft(), map.getRight()), Randomizer.random(map.getTop(), map.getBottom()));
                            map.spawnItemDrop(c.getPlayer(), c.getPlayer(), idrop, startpos, startpos, true, true);
                        }
                    }
                }
            } else {
                c.getPlayer().dropMessage("Not usable by player");
            }
            return 1;
        }
    }

    public static class ban extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            String reason = splitted[2];
            if (victim != null && c.getPlayer().getGMLevel() >= victim.getGMLevel()) {
                c.getPlayer().dropMessage("Char: " + victim.getName() + " on IP: " + victim.getClient().getSessionIPAddress() + " has been banned.");
                victim.shoptime = 0;
                victim.ban(reason);
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    for (MapleCharacter player : cserv.getPlayerStorage().getAllCharacters()) {
                        if (player != victim) {
                            player.dropMessage(6, victim.getName() + " has been banned.");
                        }
                    }
                }
                return 1;
            } else {
                c.getPlayer().dropMessage(6, "The victim does not exist.");
                return 1;
            }
        }
    }

    public static class mute extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setMute(victim.getMute() ? false : true);
            if (victim.getMute()) {
                victim.dropMessage(5, "You have been muted from @say.");
                player.dropMessage(5, victim.getName() + " has been muted.");
            } else {
                victim.dropMessage(5, "You have been unmuted from @say.");
                player.dropMessage(5, victim.getName() + " has been unmuted.");
            }
            return 1;
        }
    }

    public static class mapinfo extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            //int id = Integer.parseInt(splitted[1]);
            MapleMap map = c.getPlayer().getMap();
            //MapleMap map = c.getChannelServer().getMapFactory().getMap(id);
            for (MapleMapObject obj : map.getAllObjects()) {
                String name = null;
                switch (obj.getType()) {
                    case MONSTER:
                        MapleMonster mob = (MapleMonster) obj;
                        name = "" + mob.getId();
                        break;
                    case REACTOR:
                        MapleReactor reactor = (MapleReactor) obj;
                        name = "" + reactor.getReactorId();
                        break;
                    case PLAYER:
                        MapleCharacter chr = (MapleCharacter) obj;
                        name = "" + chr.getName();
                        break;
                }
                if (name != null) {
                    c.getPlayer().dropMessage(5, "Map Object ID: " + obj.getObjectId() + " - Type " + obj.getType() + " - Name/ID: " + name);
                } else {
                    c.getPlayer().dropMessage(5, "Map Object ID: " + obj.getObjectId() + " - Type " + obj.getType());
                }
            }
            return 1;
        }
    }

    public static class showhp extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getSession().write(MobPacket.showMonsterHP(c.getPlayer().getObjectId(), 100));
            return 1;
        }
    }

    public static class getAllObjects extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "Current objects in map: " + c.getPlayer().getMap().getAllObjects().size());

            for (MapleMapObject obj : c.getPlayer().getVisibleMapObjects()) {
                c.getPlayer().dropMessage(6, "Obj ID: " + obj.getObjectId() + " - Obj type: " + obj.getType().name());
            }
            c.getPlayer().dropMessage(6, "---------------------------------------------------");
            return 1;
        }
    }

    public static class GMWho extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "------------------------PLAYERS ONLINE--------------------------");
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                List<MapleCharacter> charz = new ArrayList<>(cserv.getPlayerStorage().getAllCharacters());
                for (MapleCharacter victim : charz) {
                    c.getPlayer().dropMessage(6, "[Lvl: " + victim.getTotalLevel() + "] " + victim.getName() + " - Acc: " + victim.getClient().getAccountName() + " - Map ID: " + victim.getMapId() + " - IP [" + victim.getClient().getSessionIPAddress() + "]");
                }
                c.getPlayer().dropMessage(6, "Current players online: " + charz.size());
                charz.clear();
            }
            return 1;
        }
    }

    public static class GMWhoNpc extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            NPCScriptManager.getInstance().startNPC(c, 9010106, "who");
            return 1;
        }

    }

    public static class fish extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            System.out.println("chair: " + c.getPlayer().getChair());
            c.announce(EffectPacket.showInfo("Effect/BasicEff.img/Gachapon/Open"));
            c.announce(CField.playSound("Custom/CatchSuccess"));
            return 1;
        }
    }

    public static class Say extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            final int id = Integer.parseInt(splitted[1]);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter victim : cserv.getPlayerStorage().getAllCharacters()) {
                    victim.getClient().announce(CField.startMapEffect(StringUtil.joinStringFrom(splitted, 2), id, true));
                }
            }
            return 1;
        }
    }

    public static class tele extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            final int x = Integer.parseInt(splitted[1]);
            final int y = Integer.parseInt(splitted[2]);
            c.getSession().write(CField.instantMapWarp(c.getPlayer(), x, y));
            return 1;
        }
    }

    public static class quest extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            final int x = Integer.parseInt(splitted[1]);
            final int y = Integer.parseInt(splitted[2]);
            final byte z = (byte) Integer.parseInt(splitted[2]);
            c.getSession().write(CField.updateQuestInfo(c.getPlayer(), x, y, z));
            return 1;
        }
    }

    public static class skin extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int id = Integer.parseInt(splitted[1]);
            c.getPlayer().setDamageSkin(id);
            return 1;
        }
    }

    public static class dam extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            NPCScriptManager.getInstance().startNPC(c, 9010106, "test");
            return 1;
        }
    }

    public static class go extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            NPCScriptManager.getInstance().start(c, 9010022, "maps");
            return 1;
        }
    }
}
