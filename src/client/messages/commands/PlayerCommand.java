package client.messages.commands;

//import client.MapleInventory;
//import client.MapleInventoryType;
import client.MapleCharacter;
import constants.ServerConstants.PlayerGMRank;
import client.MapleClient;
import client.MapleDisease;
import client.inventory.MapleInventoryType;
import client.maplepal.CraftingProcessor;
import client.maplepal.MaplePal;
import client.maplepal.PalTemplateProvider;
import constants.GameConstants;
import handling.channel.ChannelServer;
import handling.world.guild.MapleGuild;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import scripting.EventManager;
import scripting.NPCScriptManager;
import server.MaplePortal;
import server.Randomizer;
import server.life.MapleMonster;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleReactor;
import tools.StringUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;

/**
 *
 * @author Emilyx3
 */
public class PlayerCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.NORMAL;
    }

    public static class quest extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            NPCScriptManager.getInstance().startNPC(c, 9010106, "quest");
            return 1;
        }
    }

    public static class ping extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.senderPing();
            c.getPlayer().dropMessage(6, "Current Ping: " + c.getLatency() + "ms");
            return 1;
        }
    }

    public static class help extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            NPCScriptManager.getInstance().startNPC(c, 9010106, "help");
            return 1;
        }
    }

    public static class mask extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (splitted.length == 2 && splitted[1].length() < 5) {
                long id = Long.parseLong(splitted[1]);
                if (id > 0 && id < 10000) {
                    if (player.hasSkin((int) id)) {
                        player.setSkinMask((int) id);
                        player.dropMessage(5, "Damage Skin Mask applied.");
                    } else {
                        player.dropMessage(5, "You currently do not own this skin.");
                    }
                } else {
                    player.dropMessage(5, "Usage is @mask skinid - Example: @mask 562");
                }
            } else {
                player.dropMessage(5, "Usage is @mask skinid - Example: @mask 562");
            }
            return 1;
        }
    }

    public static class lost extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            NPCScriptManager.getInstance().startNPC(c, 9010106, "quest");
            return 1;
        }
    }

    public static class GHelp extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            if (StringUtil.joinStringFrom(splitted, 1) == null) {
                c.getPlayer().dropMessage(5, "Usage is @help msg");
            } else {
                String type = "[" + c.getPlayer().getName() + "]\r\n";
                int count = 0;
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    for (MapleCharacter victim : cserv.getPlayerStorage().getAllCharacters()) {
                        if (victim.isGM()) {
                            count++;
                            victim.dropMessage(1, type + StringUtil.joinStringFrom(splitted, 1));
                        }
                    }
                    if (count == 0) {
                        c.getPlayer().dropMessage(1, "No current GM online at the moment, please refer to discord server with your issue.");
                    } else {
                        c.getPlayer().dropMessage(1, "Your message was sent to all GMs.");
                    }
                }
            }
            return 1;
        }
    }

    public static class Say extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().getMute()) {
                if (c.getPlayer().isJailed()) {
                    c.getPlayer().dropMessage(6, "You are currently jailed");
                    return 1;
                }
                String type;
                if (c.getPlayer().isGM()) {
                    type = "[GM]-[" + c.getPlayer().getName() + "] ";
                } else {
                    type = "[" + c.getPlayer().getName() + "] ";
                }
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    for (MapleCharacter victim : cserv.getPlayerStorage().getAllCharacters()) {
                        if (c.getPlayer().isGM()) {
                            victim.dropMessage(-6, type + StringUtil.joinStringFrom(splitted, 1)); //-6
                        } else {
                            victim.dropMessage(6, type + StringUtil.joinStringFrom(splitted, 1));
                        }
                    }
                }
            } else {
                c.getPlayer().dropMessage(6, "You are currently muted from world chat. Repeat abuse of @say will lead to ban, you have been warned.");
            }
            return 1;
        }
    }

    public static class Info extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "Account ID (Voting ID): " + c.getAccID());
            c.getPlayer().dropMessage(6, "Char ID (Voting ID): " + c.getPlayer().getId());
            if (c.getCMS() != null) {
                c.getPlayer().dropMessage(6, "Npc Script: " + c.getCMS().getName());
            }
            if (c.getPlayer().getEventInstance() != null) {
                c.getPlayer().dropMessage(6, "Event Instance: " + c.getPlayer().getEventInstance().getName());
            }
            if (c.getPlayer().getMap().getEventInstance() != null) {
                c.getPlayer().dropMessage(6, "Map Event Instance: " + c.getPlayer().getMap().getEventInstance().getName());
            }
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getCSPoints(1) + " Achievement Points.");
            //c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getPoints() + " donation points.");
            //c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getVPoints() + " voting points.");
            c.getPlayer().dropMessage(6, "You currently have " + StringUtil.getUnitNumber(c.getPlayer().getBank()) + " Mesos in your bank.");

            c.getPlayer().dropMessage(6, "Max Level: " + c.getPlayer().getTotalMaxLevel());
            c.getPlayer().dropMessage(6, "Max Skin Level: 999");
            c.getPlayer().dropMessage(6, "Max Upgrade Tier: " + c.getPlayer().getBaseTier());
            c.getPlayer().dropMessage(6, "Max Stamina: " + c.getPlayer().getMaxStamFromChar());
            c.getPlayer().dropMessage(6, "Max Combo: " + c.getPlayer().getMaxCombo());
            int boo = (int) ((1.0 + c.getPlayer().getAccVara("KP_BOOST")) + (c.getPlayer().getStat().getItemKpRate()));
            c.getPlayer().dropMessage(6, "Kaotic Drop Rate: " + boo + "x");
            c.getPlayer().dropMessage(6, "Current Morale: " + (c.getPlayer().getAccVara("Morale")));
            c.getPlayer().dropMessage(6, "Reborns: " + (c.getPlayer().getVarZero("reborn")));
            int votes = c.getPlayer().getVP();
            if (votes > 0) {
                c.getPlayer().dropColorMessage(6, "You have " + votes + " Unclaimed votes. Talk to Paperboy in Henesys to claim.");
            }
            //c.getPlayer().getClient().announce(CField.getPVPHPBar(c.getPlayer().getId(), c.getPlayer().getStat().getHp(), c.getPlayer().getStat().getMaxHp()));
            //c.getPlayer().dropMessage(6, "Session ID: " + c.getSession().getAttributeKeys());
            return 1;
        }
    }

    public static class Server extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {

            c.getPlayer().dropMessage(6, "Server Time: " + Calendar.getInstance().getTime());
            c.getPlayer().dropMessage(6, "Server Day: " + c.getPlayer().getCurrentDay());
            c.getPlayer().dropMessage(6, GameConstants.getMeta());

            int rate = (int) ((GameConstants.getExpRate() * 100.0) - 100.0);
            c.getPlayer().dropMessage(6, "Server caps currently: " + GameConstants.getCapped());
            c.getPlayer().dropMessage(6, "Vote Exp Boost: +" + rate + "% (" + GameConstants.getVotes() + ") Votes");
            if (GameConstants.getServerVar("KP_Rate") > 0) {
                c.getPlayer().dropMessage(6, "Server Kaotic Point Rate: DOUBLE - Time Remainging " + GameConstants.getServerVar("KP_Rate") + " Minutes");
            } else {
                c.getPlayer().dropMessage(6, "Server Kaotic Point Rate: NORMAL");
            }
            c.getPlayer().dropMessage(6, "Chaos Damage Rate: " + GameConstants.getStatRate());
            c.getPlayer().dropMessage(6, "Chaos Change in: " + GameConstants.getDonationRate() + " Minutes");
            c.getPlayer().dropMessage(6, "Donation Chaos Bank: " + GameConstants.getServerVar("donation_rate") + " (IP)");
            return 1;
        }
    }

    public static class getPal extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int e = Randomizer.random(0, 8);
            //int e = mob.getId() - 2000;
            PalTemplateProvider.getPalsbyType(e).size();
            List<Integer> p = PalTemplateProvider.getPalsbyType(e);
            int pid = p.get(Randomizer.nextInt(p.size()));
            c.getPlayer().dropMessage(6, "Pal ID: " + pid + " type: " + e);
            return 1;
        }
    }

    public static class guild extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            MapleGuild guild = player.getGuild();
            if (guild != null) {

                int prevLevel = guild.calculateExp(guild.getLevel() - 1);
                int percent = (int) (((double) (guild.getGP() - prevLevel) / (double) (guild.calculateExp(guild.getLevel()) - prevLevel)) * 100);
                if (guild.getLevel() >= 100) {
                    c.getPlayer().dropMessage(6, "Guild Rank: " + guild.getLevel() + " - Guild Points: " + StringUtil.getUnitFullNumber(guild.getGP()));
                } else {
                    c.getPlayer().dropMessage(6, "Guild Rank: " + guild.getLevel() + " - Guild Points: " + StringUtil.getUnitFullNumber(guild.getGP()) + " (" + percent + "%)");
                    c.getPlayer().dropMessage(6, "Guild Next Rank Up: " + StringUtil.getUnitFullNumber(guild.calculateExp(guild.getLevel())) + " Guild Points");
                }
            } else {
                c.getPlayer().dropMessage(6, "You are currently not in a Guild.");
            }
            return 1;
        }
    }

    public static class fix extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().fix();
            return 1;
        }
    }

    public static class Mega extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().setSmega();
            return 1;
        }
    }

    public static class Helper extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().setHasSummon(!c.getPlayer().hasSummon());
            c.getPlayer().dropMessage("Helper Status: " + c.getPlayer().hasSummon());
            if (c.getPlayer().hasSummon()) {
                c.getPlayer().getClient().announce(CField.playSound("Zelda/Hey"));
            }
            return 1;
        }
    }

    public static class equipdrops extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().equipDrops = !c.getPlayer().equipDrops;
            c.getPlayer().dropMessage("Equip drops: " + c.getPlayer().equipDrops);
            return 1;
        }
    }

    public static class permloot extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().permVac = !c.getPlayer().permVac;
            c.getPlayer().dropMessage("Perm Vac Mode: " + c.getPlayer().permVac);
            return 1;
        }
    }

    public static class Effect extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().toggleEffects();
            return 1;
        }
    }

    public static class Save extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c == null || GameConstants.getLock()) {
                return 1;
            }
            long cooldown = 30000;
            long curr = System.currentTimeMillis();
            if (curr - c.getPlayer().getCoolDown() >= cooldown) {
                c.getPlayer().saveToDB(false, false);
                c.getPlayer().dropMessage(6, "Progress has been saved.");
                c.getPlayer().setCoolDown(System.currentTimeMillis());
            } else {
                c.getPlayer().dropMessage(6, "Time until next available @save " + ((long) (cooldown - (curr - c.getPlayer().getCoolDown())) / 1000) + " seconds.");
            }
            return 1;
        }
    }

    public static class Who extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            long cooldown = 1000 * 10;
            long curr = System.currentTimeMillis();
            if ((curr - c.getPlayer().getCoolDown() >= cooldown) || c.getPlayer().isGM()) {
                c.getPlayer().dropMessage(6, "------------------------PLAYERS ONLINE--------------------------");
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    List<MapleCharacter> charz = new ArrayList<>(cserv.getPlayerStorage().getAllCharacters());
                    int count = 0;
                    String Names = "";
                    for (MapleCharacter victim : charz) {
                        if (!victim.isGM() && !victim.isHidden()) {
                            Names += victim.getName() + ", ";
                            count++;
                        }
                    }
                    c.getPlayer().dropMessage(6, Names);
                    c.getPlayer().dropMessage(6, "Current players online: " + count);
                    charz.clear();
                }
                c.getPlayer().setCoolDown(System.currentTimeMillis());
            } else {
                c.getPlayer().dropMessage(6, "Time until next available @who " + ((long) (cooldown - (curr - c.getPlayer().getCoolDown())) / 1000) + " seconds.");
            }

            return 1;
        }
    }

    public static class Stats extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            player.getStat().recalcLocalStats(player);
            short job = player.getJob();
            switch ((int) player.getVarZero("Main")) {
                case 0 ->
                    player.dropMessage(6, "[Primary (10%)] Base Str: " + StringUtil.getUnitFullNumber(player.getStat().getStr()) + " - Total Str: " + StringUtil.getUnitFullNumber(player.getStat().getTStr()) + " (" + ((int) (player.getStat().percent_str)) + "%)");
                case 1 ->
                    player.dropMessage(6, "[Primary (10%)] Base Dex: " + StringUtil.getUnitFullNumber(player.getStat().getDex()) + " - Total Dex: " + StringUtil.getUnitFullNumber(player.getStat().getTDex()) + " (" + ((int) (player.getStat().percent_dex)) + "%)");
                case 2 ->
                    player.dropMessage(6, "[Primary (10%)] Base Int: " + StringUtil.getUnitFullNumber(player.getStat().getInt()) + " - Total Int: " + StringUtil.getUnitFullNumber(player.getStat().getTInt()) + " (" + ((int) (player.getStat().percent_int)) + "%)");
                case 3 ->
                    player.dropMessage(6, "[Primary (10%)] Base Luk: " + StringUtil.getUnitFullNumber(player.getStat().getLuk()) + " - Total Luk: " + StringUtil.getUnitFullNumber(player.getStat().getTLuk()) + " (" + ((int) (player.getStat().percent_luk)) + "%)");
            }
            switch ((int) player.getVarZero("Sub")) {
                case 0 ->
                    player.dropMessage(6, "[Secondary (5%)] Total Str: " + StringUtil.getUnitFullNumber(player.getStat().getTStr()) + " (" + ((int) (player.getStat().percent_str)) + "%)");
                case 1 ->
                    player.dropMessage(6, "[Secondary (5%)] Total Dex: " + StringUtil.getUnitFullNumber(player.getStat().getTDex()) + " (" + ((int) (player.getStat().percent_dex)) + "%)");
                case 2 ->
                    player.dropMessage(6, "[Secondary (5%)] Total Int: " + StringUtil.getUnitFullNumber(player.getStat().getTInt()) + " (" + ((int) (player.getStat().percent_int)) + "%)");
                case 3 ->
                    player.dropMessage(6, "[Secondary (5%)] Total Luk: " + StringUtil.getUnitFullNumber(player.getStat().getTLuk()) + " (" + ((int) (player.getStat().percent_luk)) + "%)");
            }
            switch ((int) player.getVarZero("Attack")) {
                case 0 ->
                    player.dropMessage(6, "[Melee (25%)] Total W-Atk: " + StringUtil.getUnitFullNumber(player.getStat().getTatk()) + " (" + ((int) (player.getStat().percent_atk)) + "%)");
                case 1 ->
                    player.dropMessage(6, "[Magic (25%)] Total M-Atk: " + StringUtil.getUnitFullNumber(player.getStat().getTmatk()) + " (" + ((int) (player.getStat().percent_matk)) + "%)");
            }
            player.dropMessage(6, "Total W-Def: " + StringUtil.getUnitFullNumber(player.getStat().getWDef()));
            player.dropMessage(6, "Total M-Def: " + StringUtil.getUnitFullNumber(player.getStat().getMDef()));
            player.dropMessage(6, "Total HP: " + StringUtil.getUnitFullNumber(player.getStat().getCurrentMaxHp()));
            player.dropMessage(6, "Total MP: " + StringUtil.getUnitFullNumber(player.getStat().getCurrentMaxMp(player.getJob())));
            player.dropMessage(6, "Total Damage: +" + (int) (player.getStat().dam_r) + "%");
            player.dropMessage(6, "Total Boss Damage: +" + (int) (player.getStat().bossdam_r) + "%");
            player.dropMessage(6, "Total Ignore Defense: " + player.getStat().getPDR());
            player.dropMessage(6, "Total Crit Damage: " + (int) player.getStat().passive_sharpeye_percent + "%");
            player.dropMessage(6, "Total Exp Rate: " + (long) player.getEXPMod() * 100 + "%");
            player.dropMessage(6, "Total Drop Rate: " + (int) (player.getDropMod() * 100.0) + "%");
            player.dropMessage(6, "Total Meso Rate: " + (int) (player.getMesoMod() * 100.0) + "%");
            player.dropMessage(6, "Total Damage Resist: " + (int) (player.getStat().damResist * 100.0) + "%");
            player.dropMessage(6, "Total OverPower Bonus: " + (int) (player.getStat().overpower * 100.0) + "%");
            player.dropMessage(6, "Total Status Immunity: " + (int) (player.getStat().ASR) + "%");
            player.dropMessage(6, "Total Etc Bonus: +" + (int) (player.getETCMod() * 100.0) + "%");
            player.dropMessage(6, "Power Level: " + StringUtil.getUnitFullNumber(player.getLimit()));
            return 1;
        }
    }

    public static class rates extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            player.getStat().recalcLocalStats(player);
            player.dropMessage(6, "Current Stamina: " + player.getStamina() + "/1000");
            player.dropMessage(6, "Total Min Etc Bonus Rate: +" + (int) (100.0) + "%");
            player.dropMessage(6, "Total Max Etc Bonus Rate: +" + (int) (player.getETCMod() * 100.0) + "%");
            player.dropMessage(6, "Exp Rate: +" + (int) (player.getEXPMod() * 100.0) + "%");
            player.dropMessage(6, "Drop Rate: +" + (int) (player.getDropMod() * 100.0) + "%");
            return 1;
        }
    }

    public static class exit extends CommandExecute {

        public String getNumber(long value) {
            return StringUtil.getUnitFullNumber(value);
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            if (!player.isMapChange()) {
                if (player.getEventInstance() != null) {
                    player.getEventInstance().exitPlayer(player);
                    player.dropMessage(6, "You have left the instance.");
                } else {
                    player.dropMessage(6, "Command is only usable in an event-instance.");
                }
            } else {
                player.dropMessage(6, "Command is only usable when not changing maps.");
            }
            return 1;
        }
    }

    public static class level extends CommandExecute {

        public String getNumber(long value) {
            return StringUtil.getUnitFullNumber(value);
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            player.getExpLevel();
            if (player.getFullTotalLevel() > 9999) {
                player.dropMessage(6, "[Player Level] " + player.getTotalLevel() + " - [Paragon Level] " + player.getLevelDataLvl(200));
            } else {
                player.dropMessage(6, "[Player Level] " + player.getTotalLevel());
            }
            player.getExpLevel();
            player.dropMessage(6, "[Player EXP] " + StringUtil.getUnitBigNumber(player.getOverExp()) + " / " + StringUtil.getUnitBigNumber(player.getXpLvl.toBigInteger()) + " (" + player.getOverExpPerc() + "%)");
            if (player.getAndroid() != null) {
                player.dropMessage(6, "[Android] " + player.getAndroid().getName() + " - Level: " + player.getAndroid().getLevel() + ".");
                player.dropMessage(6, "[Android] " + player.getAndroid().getName() + " - Exp: " + StringUtil.getUnitNumber(player.getAndroid().getExp()) + "/" + StringUtil.getUnitNumber(player.getAndroidNeededExp(player.getAndroid().getLevel())) + "  ");
            }
            player.dropMessage(6, "[Dojo Level] " + player.getDojoLevel());
            player.dropMessage(6, "[Dojo Exp] " + StringUtil.getUnitNumber(player.getDojoExp()) + " / " + StringUtil.getUnitNumber(player.getAndroidNeededExp(player.getDojoLevel())) + " (" + player.getDojoPercent() + "%)");
            if (player.getDamageSkin() < 9000) {
                int level = player.getSkinLevel(player.getDamageSkin());
                player.dropMessage(6, "[Skin Level] " + level);
                player.dropMessage(6, "[Skin Exp] " + StringUtil.getUnitNumber(player.getSkinExp()) + " / " + StringUtil.getUnitNumber(player.getSkinNeededExp(level)) + " (" + player.getSkinExpPercent() + "%)");
            }
            int type = player.getWeaponType();
            player.dropMessage(6, "[Mastery] " + player.getBaseLevelDataWeaponPerc() + "% " + player.getWeaponName());
            player.dropMessage(6, "[Mastery Exp] " + StringUtil.getUnitNumber(player.getLevelDataExp(type)) + " / " + StringUtil.getUnitNumber(player.getLevelDataNeededExp(player.getLevelDataLvl(type))) + " (" + player.getLevelExpDataPerc(type) + "%)");
            player.dropMessage(6, "[Super Level] " + player.getLevelDataLvl(999));
            player.dropMessage(6, "[Super Exp] " + StringUtil.getUnitNumber(player.getLevelDataExp(999)) + " / " + StringUtil.getUnitNumber(player.getLevelDataNeededExp(player.getLevelDataLvl(999))) + " (" + player.getLevelExpDataPerc(999) + "%)");

            c.announce(CField.customMainStatUpdate(c.getPlayer()));
            c.announce(CField.customStatDetail(c.getPlayer()));
            return 1;
        }
    }

    public static class battlemode extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            boolean simp = player.getAccVara("battle_mode") > 0;
            player.setAccVar("battle_mode", simp ? 0 : 1);
            simp = player.getAccVara("battle_mode") > 0;
            player.dropMessage(6, "Pal Trainer Battles: " + simp);
            return 1;
        }
    }

    public static class kp extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            player.dropMessage(6, "Kaotic Points: " + player.getVar("eDrop"));
            return 1;
        }
    }

    public static class mastery extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            player.getMasterys();
            return 1;
        }
    }

    public static class pqbonus extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            player.getMasterys();
            return 1;
        }
    }

    public static class Roll extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            int roll = Randomizer.random(0, 100);
            player.getMap().broadcastColorMessage(6, player.getName() + " rolled " + roll);
            return 1;
        }
    }

    public static class Boss extends CommandExecute {

        public String getNumber(long value) {
            return StringUtil.getUnitFullNumber(value);
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            player.getQuestStat();
            player.dropMessage(6, "Boss Bonus Stats");
            player.dropMessage(6, "-------------------------------------------------");
            //player.dropMessage(6, "Exp Rate: " + StringUtil.getUnitFullNumber((long) (player.getStat(1))) + "% - (MAX: " + StringUtil.getUnitFullNumber((long) (player.getMaxStat(1))) + ")");
            //player.dropMessage(6, "Drop Rate: " + StringUtil.getUnitFullNumber((long) (player.getStat(2))) + "% - (MAX: " + StringUtil.getUnitFullNumber((long) (player.getMaxStat(2))) + ")");
            //player.dropMessage(6, "Meso Rate: " + StringUtil.getUnitFullNumber((long) (player.getStat(5))) + "% - (MAX: " + StringUtil.getUnitFullNumber((long) (player.getMaxStat(5))) + ")");
            player.dropMessage(6, "All Stats: " + StringUtil.getUnitFullNumber((long) (player.getStat(3))) + "%");
            player.dropMessage(6, "Mob Damage: " + StringUtil.getUnitFullNumber((long) (player.getStat(6))) + "%");
            player.dropMessage(6, "Boss Damage: " + StringUtil.getUnitFullNumber((long) (player.getStat(7))) + "%");
            player.dropMessage(6, "Overpower: " + StringUtil.getUnitFullNumber((long) (player.getStat(4))) + "%");
            player.dropMessage(6, "Ignore Defense: " + StringUtil.getUnitFullNumber((long) (player.getStat(8))) + "%");
            player.dropMessage(6, "Crit Damage: " + StringUtil.getUnitFullNumber((long) (player.getStat(11))) + "%");
            //player.dropMessage(6, "ETC Bonus: " + StringUtil.getUnitFullNumber((long) (player.getStat(10))) + "% - (MAX: " + StringUtil.getUnitFullNumber((long) (player.getMaxStat(10))) + ")");
            //player.dropMessage(6, "Drop Power: " + StringUtil.getUnitFullNumber((long) (player.getStat(9))) + "% - (MAX: " + StringUtil.getUnitFullNumber((long) (player.getMaxStat(9))) + ")");
            return 1;
        }
    }

    public static class exp extends CommandExecute {

        public String getNumber(long value) {
            return StringUtil.getUnitFullNumber(value);
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.expMode) {
                player.getQuestStat();
                player.dropMessage(6, "EXP Formula");
                player.dropMessage(6, "-----BASE-EXP--------------------------------------------");
                //player.dropMessage(6, "Exp Base Rate Cap: " + StringUtil.getUnitFullNumber((long) Math.floor(player.expMax() * 100.0)) + "%");
                double bonus = (player.getMap().getTotemType(player) * player.getStat().getItemExpRate());
                player.dropMessage(6, "Exp Base Rate: " + StringUtil.getUnitFullNumber((long) player.getStat().expMod) + "%");
                player.dropMessage(6, "Exp Bonus Rate (Totem)(Chaos Hour)(ExpItemBuff): " + (long) (bonus * 100.0) + "%");
                player.dropMessage(6, "-----GROWTH-RATE------------------------------------------");
                //double rate = (1.0 - player.rebornExpRate()) * 100.0;
                player.dropMessage(6, "Exp Growth Rate: " + (Math.floor(player.getExpGrowth() * 100.0) * 0.01) + "");
                player.dropMessage(6, "-----TOTAL-EXP-------------------------------------------");
                //player.dropMessage(6, "Exp Rate (+Reborn): " + StringUtil.getUnitFullNumber((long) Math.floor(player.getStat().expMod * player.rebornExpRate() * 100.0)) + "%");
                //player.dropMessage(6, "Exp Rate (+Reborn): " + StringUtil.getUnitFullNumber((long) Math.floor((Randomizer.DoubleMax(player.getStat().expMod * player.rebornExpRate(), 20000000.0)) * 100.0)) + "%");
                player.dropMessage(6, "Exp Rate (+Bonus): " + StringUtil.getUnitFullNumber((long) Math.floor(player.getEXPMod() * 100.0)) + "%");
                player.getExpLevel();
                player.dropMessage(6, "Exp Needed: " + StringUtil.getUnitBigNumber(player.getXpLvl.toBigInteger()));
                player.dropMessage(6, "---------------------------------------------------------");
            } else {
                player.dropMessage(6, "You currently have exp rates disabled, use @expmode to toggle on/off.");
            }
            return 1;
        }
    }

    public static class Monster extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            long cooldown = 1000 * 10;
            long curr = System.currentTimeMillis();
            if (curr - c.getPlayer().getCoolDown() >= cooldown) {
                MapleCharacter player = c.getPlayer();
                HashSet<Integer> aList = new HashSet<Integer>();
                String kills = StringUtil.getUnitFullNumber(c.getPlayer().getMap().getMapKills());
                c.getPlayer().yellowMessage(kills + " monsters killed on this map.");
                c.getPlayer().yellowMessage("There is " + c.getPlayer().getMap().countMonsters() + " monsters currently found on this map.");
                c.getPlayer().yellowMessage("There is " + c.getPlayer().getMap().getSummonCount() + " summons currently found on this map.");
                c.getPlayer().yellowMessage("There is " + c.getPlayer().getMap().getMapObjectSize() + " objects currently found on this map.");
                c.getPlayer().yellowMessage("Spawn Cap: " + c.getPlayer().getMap().spawnCap() + " spawns currently found on this map.");
                c.getPlayer().yellowMessage("Spawnpoints: " + c.getPlayer().getMap().getMonsterSpawn().size() + " spawnpoints currently found on this map.");
                if (c.getPlayer().getMap().getBoost() > 0) {
                    c.getPlayer().yellowMessage("Map Boost: " + StringUtil.secondsToString(c.getPlayer().getMap().getBoost()) + " remaining.");
                }
                c.getPlayer().yellowMessage("---------------------------------------------------------------------------------------------------------------------------------");
                try {
                    for (MapleMonster monster : c.getPlayer().getMap().getAllMonsters()) {

                        if (monster != null && !monster.isSummon() && !aList.contains(monster.getId()) && monster.getHp() > 0) {
                            aList.add(monster.getId());
                            long basexp = monster.getStats().getExp();
                            double expRate = player.getEXPMod();
                            BigDecimal mobexp = BigDecimal.valueOf(basexp).multiply(BigDecimal.valueOf(expRate));
                            String exp = StringUtil.getUnitNumber(basexp);
                            String exp2 = StringUtil.getUnitBigNumber(mobexp.toBigInteger());
                            int tier = monster.getStats().getTier();
                            int scale = monster.getStats().getScale();
                            int mobatk = (int) (Randomizer.MaxLong((long) (monster.getStats().getPhysicalAttack()), 99999999));
                            int mobmatk = (int) (Randomizer.MaxLong((long) (monster.getStats().getMagicAttack()), 99999999));
                            String watk = StringUtil.getUnitNumber(mobatk);
                            String kaotic = monster.getStats().getKaotic() ? " (Boss) " : "";
                            double resist = Randomizer.DoubleMax(player.getStat().getResist(), 0.75);
                            String explosive = monster.getStats().isExplosiveReward() ? " (Explosive) " : "";
                            if (!monster.getStats().getRevives().isEmpty()) {
                                c.getPlayer().yellowMessage("Monster: " + monster.getStats().getName() + " - ID: " + monster.getId() + " - Revives: " + monster.getStats().getRevives() + " - Cap: " + monster.getStats().getHits());
                            } else {
                                c.getPlayer().yellowMessage("Monster: " + monster.getStats().getName() + " - ID: " + monster.getId() + " - Cap: " + monster.getStats().getHits());
                            }

                            c.getPlayer().yellowMessage("Level: " + monster.getStats().getPower() + " - " + kaotic + "Scale: " + scale + " - Tier: " + tier + " - Armor: " + monster.getStats().getArmor() + " - Drops: " + !monster.dropsDisabled() + " " + explosive);
                            //c.getPlayer().yellowMessage("Defense: " + StringUtil.getUnitFullNumber(def) + " - Diff: " + ddiff);
                            c.getPlayer().yellowMessage("Max HP: " + StringUtil.getUnitBigNumber(monster.getGrandHP()) + " - Attack Power: " + watk + " - Defense: " + StringUtil.getUnitNumber(monster.getStats().getDef()));
                            //System.out.println("def: " + player.getStat().getWDef());
                            double dscale = Randomizer.DoubleMin(Randomizer.DoubleMax((double) mobatk / (double) player.getStat().getWDef(), 1.0), 0.1);
                            //System.out.println("defscale: " + dscale);
                            String sddef = StringUtil.getUnitFullNumber((int) ((dscale - (dscale * resist)) * 100));

                            double mscale = Randomizer.DoubleMin(Randomizer.DoubleMax((double) mobmatk / (double) player.getStat().getMDef(), 1.0), 0.1);
                            String smddef = StringUtil.getUnitFullNumber((int) ((mscale - (mscale * resist)) * 100));
                            String drops = "";
                            String fixed = monster.getStats().getFixedDamage() > 0 ? " - Fixed Dam: " + StringUtil.getUnitFullNumber(monster.getStats().getFixedDamage()) : "";
                            c.getPlayer().yellowMessage("Melee-Damage Taken: " + sddef + "% - Magic-Damage Taken: " + smddef + "%" + drops + " - Attackers: " + monster.getTags() + fixed);
                            c.getPlayer().yellowMessage("Exp: " + exp + " -> P-Exp: " + exp2 + " (" + (int) (expRate * 100) + "%)");
                            if (!monster.dropsDisabled() && monster.getStats().isExplosiveReward()) {
                                double dropRate = c.getPlayer().getDropMod();
                                double mesoRate = c.getPlayer().getMesoMod();
                                c.getPlayer().yellowMessage("Meso Rate: +" + ((int) (mesoRate * 100)) + "% - Drop Rate: +" + ((int) (dropRate * 100)) + "%");
                            }

                            c.getPlayer().yellowMessage("---------------------------------------------------------------------------------------------------------------------------------");

                        }
                    }
                    c.getPlayer().setCoolDown(System.currentTimeMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                c.getPlayer().dropMessage(6, "Time until next available @monster " + ((long) (cooldown - (curr - c.getPlayer().getCoolDown())) / 1000) + " seconds.");
            }

            return 1;
        }
    }

    public static class Map extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            long cooldown = 1000 * 10;
            long curr = System.currentTimeMillis();
            if (curr - c.getPlayer().getCoolDown() >= cooldown) {
                int count = 0;
                for (MapleMapItem item : c.getPlayer().getMap().getAllItems()) {
                    if (GameConstants.getInventoryType(item.getItemId()) == MapleInventoryType.EQUIP) {
                        count++;
                    }
                }

                MapleCharacter player = c.getPlayer();
                MapleMap map = player.getMap();
                HashSet<Integer> aList = new HashSet<Integer>();
                String kills = StringUtil.getUnitFullNumber(map.getMapKills());
                player.yellowMessage(kills + " monsters killed on this map.");
                player.yellowMessage("There is " + map.countMonsters() + " monsters currently found on this map.");
                player.yellowMessage("There is " + map.getParkMonsters().size() + " park monsters currently found on this map.");
                player.yellowMessage("There is " + map.getSummonCount() + " summons currently found on this map.");
                player.yellowMessage("Egg? " + map.eggSpawn);
                player.yellowMessage("There is " + count + " items currently found on this map.");
                player.yellowMessage("There is " + map.getMapObjectSize() + " objects currently found on this map.");
                player.yellowMessage("Spawn Cap: " + map.spawnCap() + " spawns currently found on this map.");
                player.yellowMessage("Spawnpoints: " + map.getMonsterSpawn().size() + " spawnpoints currently found on this map.");
                player.yellowMessage("Reactors: " + map.getReactors().size() + " reactors currently found on this map.");
                player.yellowMessage("---------------------------------------------------------------------------------------------------------------------------------");
                player.dropMessage(6, "Position: (" + player.getPosition().x + ", " + player.getPosition().y + ") on Map ID:" + player.getMapId() + " - Map Name: " + map.getMapName());
                if (map.palTimer > 0) {
                    player.dropColorMessage(6, "Pal Egg has recently been captured...");
                }

            } else {
                c.getPlayer().dropMessage(6, "Time until next available @map " + ((long) (cooldown - (curr - c.getPlayer().getCoolDown())) / 1000) + " seconds.");
            }

            return 1;
        }
    }

    public static class Log extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            player.yellowMessage("log 1: +" + Math.floor((Math.log10(1) + 0.01) * 100) + "%");
            player.yellowMessage("log 5: +" + Math.floor((Math.log10(5) + 0.05) * 100) + "%");
            player.yellowMessage("log 10: +" + Math.floor((Math.log10(10) + 0.10) * 100) + "%");
            player.yellowMessage("log 25: +" + Math.floor((Math.log10(25) + 0.25) * 100) + "%");
            player.yellowMessage("log 50: +" + Math.floor((Math.log10(50) + 0.50) * 100) + "%");
            player.yellowMessage("log 99: +" + Math.floor((Math.log10(99) + 0.99) * 100) + "%");
            player.yellowMessage("log 100: +" + Math.floor((Math.log10(100) + 1.0) * 100) + "%");
            player.yellowMessage("log 150: +" + Math.floor((Math.log10(150) + 1.50) * 100) + "%");
            player.yellowMessage("log 200: +" + Math.floor((Math.log10(200) + 2.00) * 100) + "%");
            player.yellowMessage("log 250: +" + Math.floor((Math.log10(250) + 2.50) * 100) + "%");

            return 1;
        }
    }

    public static class Pal extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().getPalStorage().getActivePals().isEmpty()) {
                int stamina = 0;
                int p_str = 0;
                int p_dex = 0;
                int p_int = 0;
                int p_luk = 0;
                int p_atk = 0;
                int p_def = 0;
                int p_matk = 0;
                int p_mdef = 0;
                for (MaplePal pal : c.getPlayer().getPalStorage().getActivePals()) {
                    p_str += pal.getStats()[1];
                    p_dex += pal.getStats()[2];
                    p_int += pal.getStats()[3];
                    p_luk += pal.getStats()[4];
                    p_atk += pal.getStats()[5];
                    p_matk += pal.getStats()[6];
                    p_def += pal.getStats()[7];
                    p_mdef += pal.getStats()[8];
                }
                MapleCharacter player = c.getPlayer();
                player.yellowMessage("Current Maple Pal Stat Bonuses:");
                player.yellowMessage("-----------------------------------------------------------------");
                //player.yellowMessage("Stamina: +" + stamina + "");
                player.yellowMessage("Str: +" + p_str + "");
                player.yellowMessage("Dex: +" + p_dex + "");
                player.yellowMessage("Int: +" + p_int + "");
                player.yellowMessage("Luk: +" + p_luk + "");
                player.yellowMessage("Attack: +" + p_atk + "");
                player.yellowMessage("M-Attack: +" + p_matk + "");
                player.yellowMessage("Defense: +" + p_def + "");
                player.yellowMessage("M-Defense: +" + p_mdef + "");
            } else {
                c.getPlayer().dropMessage(6, "You have no Maple Pals.");
            }

            return 1;
        }
    }

    public static class craft extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getEventInstance() == null && !c.getPlayer().battle) {
                List<Integer> unlockedRecipes = CraftingProcessor.getAllRecipeIds();
                c.announce(CraftingProcessor.sendCraftingRecipes(unlockedRecipes));
                c.announce(CraftingProcessor.sendOpenWindow(c.getPlayer().getOverflowInv()));
            } else {
                c.getPlayer().dropMessage("Crafting command no usable here.");
            }
            return 1;
        }
    }

    public static class item extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.announce(CraftingProcessor.sendOverflowWindow(c.getPlayer().getOverflowInv()));
            return 1;
        }
    }

    public static class pos extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            c.getPlayer().dropColorMessage(6, "-------------------------------------------------");
            c.getPlayer().dropColorMessage(6, "Position: (" + c.getPlayer().getPosition().x + ", " + c.getPlayer().getPosition().y + ")");
            c.getPlayer().dropColorMessage(6, "Map ID:" + c.getPlayer().getMapId() + " - Map Name: " + c.getPlayer().getMap().getMapName());
            MapleMap map = c.getPlayer().getMap();
            c.getPlayer().dropColorMessage(6, "Map Top/Bottom: Top: " + map.getTop() + " - Bottom: " + map.getBottom());
            c.getPlayer().dropColorMessage(6, "Map Left/Right: Left: " + map.getLeft() + " - Right: " + map.getRight());
            c.getPlayer().dropColorMessage(6, "-------------------------------------------------");
            c.getPlayer().dropColorMessage(6, "MapArea Top/Bottom: Top: " + map.mapArea.getMinY() + " - Bottom: " + map.mapArea.getMaxY());
            c.getPlayer().dropColorMessage(6, "MapArea Left/Right: Left: " + map.mapArea.getMinX() + " - Right: " + map.mapArea.getMaxX());
            c.getPlayer().dropColorMessage(6, "-------------------------------------------------");
            c.getPlayer().dropColorMessage(6, "MapAreaLimit Left/Right: Left: " + map.leftLimit + " - Right: " + map.rightLimit);
            c.getPlayer().dropColorMessage(6, "-------------------------------------------------");

            if (c.getPlayer().getParty() != null) {
                try {
                    MapleCharacter leader = c.getPlayer().getParty().getLeader().getPlayer();
                    String name = leader.getName();
                    c.getPlayer().dropColorMessage(6, "Party Leader Map id: " + leader.getMapId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return 1;
        }
    }

    public static class debug extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            c.getPlayer().dropMessage("Position: (" + c.getPlayer().getPosition().x + ", " + c.getPlayer().getPosition().y + ") on Map ID:" + c.getPlayer().getMapId() + " - Map Name: " + c.getPlayer().getMap().getMapName());
            if (c.getPlayer().getEventInstance() != null) {
                c.getPlayer().dropMessage(6, "Event Instance - " + c.getPlayer().getEventInstance().getName());
            }
            if (c.getPlayer().getMap().getEventInstance() != null) {
                c.getPlayer().dropMessage(6, "Map Event Instance - " + c.getPlayer().getMap().getEventInstance().getName());
            }
            c.getPlayer().dropMessage(6, "Map - " + c.getPlayer().getMap().getMapName());
            c.getPlayer().dropMessage(6, "Map Items - " + c.getPlayer().getMap().getAllItems().size());
            c.getPlayer().dropMessage(6, "Map Objects - " + c.getPlayer().getMap().getAllObjects().size());
            c.getPlayer().dropMessage(6, "Map Player Count - " + c.getPlayer().getMap().getPlayerCount());
            return 1;
        }
    }

    public static class stuck extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            long cooldown = 30000;
            long curr = System.currentTimeMillis();
            if (curr - c.getPlayer().getCoolDown() >= cooldown) {
                if (c.getPlayer().getConversation() == 0 && c.getPlayer().isAlive() && !c.getPlayer().isStorageOpened()) {
                    c.getPlayer().changeMap(c.getPlayer().getMap());
                    c.getPlayer().dropMessage(6, "You have warped to a save spot.");
                    c.getPlayer().setCoolDown(System.currentTimeMillis());
                    c.getPlayer().saveToDB();
                } else {
                    c.getPlayer().dropMessage(6, "Unable to use command, please relog." + c.getPlayer().getConversation());
                }
            } else {
                c.getPlayer().dropMessage(6, "Time until next available @stuck " + ((long) (cooldown - (curr - c.getPlayer().getCoolDown())) / 1000) + " seconds.");
            }
            return 1;
        }
    }

    public static class expMode extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            player.expMode = !player.expMode;
            if (player.expMode) {
                player.dropMessage(6, "Exp Gain is enabled.");
            } else {
                player.dropMessage(6, "Exp Gain is disabled.");
            }
            return 1;
        }
    }

    public static class loot extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.getLoot()) {
                player.dropMessage(6, "Mobile loot enabled and Afk loot is disabled.");
                player.setVar("loot", 1);
            } else {
                player.dropMessage(6, "Mobile loot disabled and Afk loot is enabled.");
                player.setVar("loot", 0);
            }
            return 1;
        }
    }

    public static class global extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.getEventInstance() == null) {
                if (player.getGlobal()) {
                    player.dropMessage(6, "Global Drops disabled.");
                    player.setGlobal(false);
                } else {
                    player.dropMessage(6, "Global Drops enabled.");
                    player.setGlobal(true);
                }
            } else {
                player.dropMessage(6, "Command is not usable inside boss instances.");
            }
            return 1;
        }
    }

    public static class battle extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.getAccVara("pal_battles") == 0) {
                player.dropMessage(6, "Random Pal Battles Enabled.");
                player.setAccVar("pal_battles", 1);
            } else {
                player.dropMessage(6, "Random Pal Battles Disabled.");
                player.setAccVar("pal_battles", 0);
            }
            return 1;
        }
    }

    public static class expo extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.getAccVara("expo") == 0) {
                player.dropMessage(6, "Boss Hp set to Letter Mode (Compressed).");
                player.setAccVar("expo", 1);
            } else {
                player.dropMessage(6, "Boss Hp set to (Uncompressed).");
                player.setAccVar("expo", 0);
            }
            return 1;
        }
    }

    public static class cool extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (!player.getCool()) {
                player.dropMessage(6, "Cool Damage Enabled.");
                player.setCool(true);
            } else {
                player.dropMessage(6, "Cool Damage Disabled.");
                player.setCool(false);
            }
            return 1;
        }
    }

    public static class etcloot extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            player.toggleETC();
            if (player.getEtc()) {
                player.dropMessage(6, "Auto Etc Storage enabled.");
            } else {
                player.dropMessage(6, "Auto Etc Storage disabled.");
            }
            return 1;
        }
    }

    public static class showlevel extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            player.showLevel = !player.showLevel;
            if (!player.showLevel) {
                player.dropMessage(6, "Level up effects disabled.");
            } else {
                player.dropMessage(6, "Level up effects enabled.");
            }
            return 1;
        }
    }

    public static class skill extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            NPCScriptManager.getInstance().startNPC(c, 9010106, "skill");
            return 1;
        }
    }

    public static class duey extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            NPCScriptManager.getInstance().start(c, 9010009, "random_job");
            return 1;
        }
    }

    public static class equip extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.getEventInstance() == null) {
                NPCScriptManager.getInstance().startNPC(c, 9010106, "equips");
            } else {
                player.dropMessage(5, "equips npc not useable while in an event.");
            }
            return 1;
        }
    }

    /*

    public static class boost extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }

            MapleCharacter player = c.getPlayer();
            if (!player.getMap().getSpawnPoints().isEmpty()) {
                NPCScriptManager.getInstance().startNPC(c, 9010106, "boost");
            } else {
                player.dropMessage(1, "Boost is not available here. Boost only works on maps with natural monsters.");
            }
            return 1;
        }
    }
     */
    public static class achievement extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.getEventInstance() == null) {
                NPCScriptManager.getInstance().startNPC(c, 9010106, "achievements");
            } else {
                player.dropMessage(5, "npc not useable while in an event.");
            }
            return 1;
        }
    }

    public static class mobinfo extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.isAlive()) {
                NPCScriptManager.getInstance().startNPC(c, 9070003, null);
            } else {
                player.dropMessage(5, "@mobinfo not useable while dead.");
            }
            return 1;
        }
    }

    public static class pet extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            int petPower = (int) player.getTotalLevel();
            player.dropMessage(5, "Pet Level: " + petPower);
            return 1;
        }
    }

    public static class buff extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.vipBuff > 0) {
                player.dropMessage(5, "VIP Buff: " + StringUtil.secondsToString(player.vipBuff) + " remaining.");
                player.dropMessage(5, "VIP Buff: Exp-Drop-Meso-ETC Buff currently doubled.");
            } else {
                player.dropMessage(5, "No VIP Buff currently on account. You can get more VIP by buying Plex in DP Lands.");
            }
            if (player.dropBuff > 0) {
                player.dropMessage(5, "Pet Buff: " + StringUtil.secondsToString(player.dropBuff) + " remaining.");
            }
            return 1;
        }
    }

    public static class link extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            NPCScriptManager.getInstance().startNPC(c, 9010106, "link");
            return 1;
        }
    }

    public static class togglebuff extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.getEventInstance() == null) {
                player.toggleBuff();
                if (player.getBuff()) {
                    player.dropMessage(5, "DP buffs enabled.");
                } else {
                    player.dropMessage(5, "DP buffs disabled.");
                }
            } else {
                player.dropMessage(5, "DP buffs cannot be toggled while inside an event instance.");
            }
            return 1;
        }
    }

    public static class npc extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.getNPC()) {
                player.setNPC(false);
                player.dropMessage(5, "NPC animations and movements enabled.");
            } else {
                player.setNPC(true);
                player.dropMessage(5, "NPC animations and movements disabled.");
            }
            return 1;
        }
    }

    public static class expel extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.getRaid() != null && player.getRaid().getLeader() == player) {
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim != null && victim.getRaid() != null && victim.getRaid().getId() == player.getRaid().getId()) {
                    player.getRaid().removeMember(player, victim);
                    player.dropMessage(5, victim.getName() + " has been expelled from the raid.");
                }
            } else {
                player.dropMessage(5, "This command is only usable as a raid leader.");
            }

            return 1;
        }
    }

    public static class raid extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.getRaid() == null) {
                if (player.isGroup()) {
                    c.getPlayer().dropMessage(5, "You are alrdy inside a group.");
                    return 1;
                } else {
                    player.createRaid();
                    return 1;
                }
            } else {
                if (splitted.length == 1) {
                    int count = 0;
                    String Names = "";
                    String Leader = "";
                    for (MapleCharacter victim : player.getRaid().getMembers()) {
                        if (victim == player.getRaid().getLeader()) {
                            Leader += victim.getName();
                        } else {
                            Names += victim.getName() + ", ";
                        }
                        count++;
                    }
                    c.getPlayer().dropMessage(6, "Current players in the raid: " + count);
                    c.getPlayer().dropMessage(6, "Leader: " + Leader);
                    c.getPlayer().dropMessage(6, "Member: " + Names);
                    c.getPlayer().dropMessage(5, "Use @raid leave to leave the raid or disband.");
                    c.getPlayer().dropMessage(5, "Use @add or @kick to add players or remove players.");
                } else {
                    switch (splitted[1]) {
                        case "leave": {
                            if (player.getRaid().getLeader() == player) {
                                player.getRaid().disbandRaid();
                            } else {
                                player.getRaid().leaveRaid(player);
                            }
                            break;
                        }
                        case "kick": {
                            if (player.getRaid() != null && player.getRaid().getLeader() == player) {
                                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[2]);
                                if (victim != null) {
                                    if (victim.getRaid() != null && victim.getRaid().getId() == player.getRaid().getId()) {
                                        player.getRaid().removeMember(player, victim);
                                        player.dropMessage(5, victim.getName() + " has been expelled from the raid.");
                                    }
                                } else {
                                    player.dropMessage(5, splitted[2] + " is offline or does not exist.");
                                }
                            } else {
                                player.dropMessage(5, "This command is only usable as a raid leader.");
                            }
                            break;
                        }
                        case "add": {
                            if (player.getRaid() != null && player.getRaid().getLeader() == player) {
                                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[2]);
                                if (victim != null) {
                                    if (!victim.isGroup()) {
                                        victim.getClient().announce(CWvsContext.ExpeditionPacket.expeditionInvite(c.getPlayer(), 2009));
                                        //victim.getClient().announce(CWvsContext.followRequest(player.getId()));
                                        //player.getRaid().invite(player, victim);
                                        player.dropMessage(5, victim.getName() + " has been invited to the raid.");
                                    } else {
                                        player.dropMessage(5, victim.getName() + " is already in a group.");
                                    }
                                } else {
                                    player.dropMessage(5, splitted[2] + " is offline or does not exist.");
                                }
                            } else {
                                player.dropMessage(5, "This command is only usable as a raid leader.");
                            }
                            break;
                        }
                        case "leader": {
                            if (player.getRaid() != null && player.getRaid().getLeader() == player) {
                                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[2]);
                                if (victim != null) {
                                    if (victim.getRaid() != null && victim.getRaid().getId() == player.getRaid().getId()) {
                                        player.getRaid().changeLeader(player, victim);
                                        player.dropMessage(5, victim.getName() + " is now the raid leader.");
                                    } else {
                                        player.dropMessage(5, victim.getName() + "This command is only usable as a raid leader.");
                                    }
                                } else {
                                    player.dropMessage(5, "Player is offline or does not exist.");
                                }
                            } else {
                                player.dropMessage(5, "This command is only usable as a raid leader.");
                            }
                            break;
                        }
                        default: {
                            c.getPlayer().dropMessage(5, "Use @raid to see raid member list.");
                            c.getPlayer().dropMessage(5, "Use @add or @kick to add players or remove players.");
                            c.getPlayer().dropMessage(5, "Use @raid leave to leave the raid or disband.");
                            c.getPlayer().dropMessage(5, "Use @leader to change raid leader.");
                        }
                    }
                }
            }
            return 1;
        }
    }

    public static class totem extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (player.getMap().getTotem(player) != null) {
                if (player.getMap().getTotem(player).getOwner() == player) {
                    player.getMap().removeTotem(player);
                } else {
                    player.dropMessage(5, "Totem on map does not belong to you.");
                }
            } else {
                player.dropMessage(5, "There either no totems on map.");
            }
            return 1;
        }
    }

    public static class toggletotem extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getTrade() != null || c.getPlayer().isStorageOpened()) {
                c.getPlayer().dropMessage(6, "Not possible here.");
                return 1;
            }
            NPCScriptManager.getInstance().startNPC(c, 9200000, "totem");
            return 1;
        }
    }

    public static class instance extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getEventInstance() != null) {
                c.getPlayer().dropMessage(5, "Command not usable inside instances.");
                return 1;
            }
            if (c.getPlayer().getMap().getMonsterSpawn().isEmpty()) {
                c.getPlayer().dropMessage(5, "Command not usable without monsters on map.");
                return 1;
            }
            NPCScriptManager.getInstance().start(c, 9000000, "instance");
            return 1;
        }
    }

    /*
    public static class clone extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            if (player.isGroup() && player.isLeader()) {
                if (!player.isMapChange() && player.isAlive()) {
                    if (!player.getMap().getSpawnCount().isEmpty()) {
                        if (player.getEventInstance() == null) {
                            int min = (int) (c.getPlayer().getTotalLevel() * 0.75);
                            int max = (int) (c.getPlayer().getTotalLevel() * 1.5);
                            EventManager em = c.getChannelServer().getEventSM().getEventManager("kaotic_instance");
                            if (em.getEligiblePartyLevel(c.getPlayer(), min, max)) {
                                if (!em.startPlayerInstance(player, player.getMapId())) {
                                    player.dropMessage("Error with making personal instance.");
                                } else {
                                    player.dropMessage("Once time is ran out, you will be warped back out.");
                                }
                            } else {
                                player.dropMessage("One or more players does not meet level requirements.");
                            }
                        } else {
                            player.dropMessage("You are already inside an instance..");
                        }
                    } else {
                        player.dropMessage("Cannot make instance on a map with no monster spawns.");
                    }
                } else {
                    player.dropMessage("Cannot create an instance while changing maps or dead.");
                }
            } else {
                player.dropMessage("Cannot be solo and group leader for this command.");
            }
            return 1;
        }
    }
     */
    public static class ap extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            NPCScriptManager.getInstance().startNPC(c, 9200000, "ap");
            return 1;
        }
    }

    public static class etc extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            if (c.getPlayer().getTrade() != null || c.getPlayer().isStorageOpened()) {
                c.getPlayer().dropMessage(6, "Not possible here.");
                return 1;
            }
            NPCScriptManager.getInstance().start(c, 9000000);
            return 1;
        }
    }

    public static class blacklist extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            NPCScriptManager.getInstance().start(c, 9000000, "blacklist");
            return 1;
        }
    }

    public static class recycle extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            if (c.getPlayer().getTrade() != null || c.getPlayer().isStorageOpened()) {
                c.getPlayer().dropMessage(6, "Not possible here.");
                return 1;
            }
            NPCScriptManager.getInstance().start(c, 9010038);
            return 1;
        }
    }

    public static class bank extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            if (c.getPlayer().getTrade() != null || c.getPlayer().isStorageOpened()) {
                c.getPlayer().dropMessage(6, "Not possible here.");
                return 1;
            }
            NPCScriptManager.getInstance().start(c, 9010000);
            return 1;
        }
    }

    public static class job extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            NPCScriptManager.getInstance().startNPC(c, 9200000, "job");
            return 1;
        }
    }

    public static class bait extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getTrade() != null || c.getPlayer().isStorageOpened()) {
                c.getPlayer().dropMessage(6, "Not possible here.");
                return 1;
            }
            NPCScriptManager.getInstance().startNPC(c, 9200000, "fish");
            return 1;
        }
    }

    public static class skin extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            NPCScriptManager.getInstance().startNPC(c, 9400819, "skin");
            return 1;
        }
    }

    public static class storeAll extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            if (c.getPlayer().getTrade() != null || c.getPlayer().isStorageOpened()) {
                c.getPlayer().dropMessage(6, "This command is currently not usable");
                return 1;
            }
            try {
                if (c == null || GameConstants.getLock()) {
                    return 1;
                }
                long cooldown = 1000 * 10;
                long curr = System.currentTimeMillis();
                if (curr - c.getPlayer().getCoolDown() >= cooldown) {
                    MapleCharacter player = c.getPlayer();
                    boolean save = false;
                    if (!player.storeEtc()) {
                        player.dropMessage(5, "Nothing to store.");
                    } else {
                        save = true;
                    }
                    if (!player.storeUse()) {
                        player.dropMessage(5, "Nothing to store.");
                    } else {
                        save = true;
                    }
                    if (save) {
                        player.saveOverflow();
                    }
                } else {
                    c.getPlayer().dropMessage(6, "Time until next available @storeETC " + ((long) (cooldown - (curr - c.getPlayer().getCoolDown())) / 1000) + " seconds.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    public static class storeETC extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            if (c.getPlayer().getTrade() != null || c.getPlayer().isStorageOpened()) {
                c.getPlayer().dropMessage(6, "This command is currently not usable");
                return 1;
            }
            try {
                if (c == null || GameConstants.getLock()) {
                    return 1;
                }
                long cooldown = 1000 * 10;
                long curr = System.currentTimeMillis();
                if (curr - c.getPlayer().getCoolDown() >= cooldown) {
                    MapleCharacter player = c.getPlayer();
                    if (!player.storeEtc()) {
                        player.dropMessage(5, "Nothing to store.");
                    } else {
                        player.saveOverflow();
                    }
                } else {
                    c.getPlayer().dropMessage(6, "Time until next available @storeETC " + ((long) (cooldown - (curr - c.getPlayer().getCoolDown())) / 1000) + " seconds.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    public static class storeUSE extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            if (c.getPlayer().getTrade() != null || c.getPlayer().isStorageOpened()) {
                c.getPlayer().dropMessage(6, "This command is currently not usable");
                return 1;
            }
            try {
                if (c == null || GameConstants.getLock()) {
                    return 1;
                }
                long cooldown = 1000 * 10;
                long curr = System.currentTimeMillis();
                if (curr - c.getPlayer().getCoolDown() >= cooldown) {
                    MapleCharacter player = c.getPlayer();
                    if (!player.storeUse()) {
                        player.dropMessage(5, "Nothing to store.");
                    } else {
                        player.saveOverflow();
                    }
                } else {
                    c.getPlayer().dropMessage(6, "Time until next available @storeETC " + ((long) (cooldown - (curr - c.getPlayer().getCoolDown())) / 1000) + " seconds.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    public static class showdroid extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getAndroid() == null) {
                c.getPlayer().dropMessage(5, "You have no android.");
                return 1;
            }
            c.getPlayer().hideAndroid = !c.getPlayer().hideAndroid;
            if (c.getPlayer().hideAndroid) {
                c.getPlayer().getMap().broadcastMessage(CField.deactivateAndroid(c.getPlayer().getId()));
                c.getPlayer().dropMessage(5, "Android appearance hidden.");
            } else {
                c.getPlayer().getMap().broadcastMessage(CField.spawnAndroid(c.getPlayer(), c.getPlayer().getAndroid(), true));
                c.getPlayer().dropMessage(5, "Android appearance shown.");
            }
            return 1;
        }
    }

    /*

    public static class power extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().boosted = !c.getPlayer().boosted;
            if (!c.getPlayer().boosted) {
                c.getPlayer().dropMessage(5, "Stamina Booost disabled.");
            } else {
                c.getPlayer().dropMessage(5, "Stamina Booost enabled. Damage scale based on amount of stamina.%");
                c.getPlayer().dropMessage(5, "Each attack will consume 1 stamina.");
            }
            return 1;
        }
    }
    
     */
    public static class test extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            MapleCharacter player = c.getPlayer();
            if (player.getConversation() > 0) {
                player.dropMessage("Command is not useable at this time.");
                return 1;
            }
            if (player.getRaid() == null && player.getParty() == null) {
                if (!player.isMapChange() && player.isAlive()) {
                    if (player.getEventInstance() == null) {
                        if (splitted.length < 4) {
                            player.dropMessage("Invalid inputs. Command is @test level scale mode.");
                            return 1;
                        }
                        long level = 1;
                        try {
                            level = Long.parseLong(splitted[1]);
                        } catch (NumberFormatException nfe) {
                            player.dropMessage("Level must be a Number IE: @test 9999 99 1");
                            return 1;
                        }
                        if (level < 1 || level > 9999) {
                            player.dropMessage("Max level input is 9999.");
                            return 1;
                        }
                        long scale = 1;
                        try {
                            scale = Long.parseLong(splitted[2]);
                        } catch (NumberFormatException nfe) {
                            player.dropMessage("Tier must be a Number IE: @test 9999 99 1");
                            return 1;
                        }
                        if (scale < 1 || scale > 999) {
                            player.dropMessage("Max Scale input is 999.");
                            return 1;
                        }
                        long mode = 1;
                        try {
                            mode = Long.parseLong(splitted[3]);
                        } catch (NumberFormatException nfe) {
                            player.dropMessage("Tier must be a Number IE: @test 9999 99 1");
                            return 1;
                        }
                        EventManager em = c.getChannelServer().getEventSM().getEventManager("test");
                        if (!em.startPlayerInstance(player, (int) level, (int) scale, (int) mode)) {
                            player.dropMessage("Error with making personal instance.");
                        } else {
                            player.dropMessage("Take the portal when you are done here.");
                        }
                    } else {
                        player.dropMessage("You are already inside an instance..");
                    }
                } else {
                    player.dropMessage("Cannot create an instance while changing maps or dead.");
                }
            } else {
                player.dropMessage("Cannot create an instance while in a group.");
            }
            return 1;
        }
    }

    public static class portals extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            MapleMap map = c.getPlayer().getMap();
            for (MaplePortal pto : map.getPortals()) {
                c.getPlayer().dropMessage("Name: " + pto.getPortalName() + " - ID: " + pto.getId() + " - Mapto: " + pto.getTargetMapId() + " - Script: " + pto.getScriptName());
            }
            return 1;
        }
    }

    public static class reactors extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            MapleMap map = c.getPlayer().getMap();
            for (MapleReactor pto : map.getAllReactor()) {
                c.getPlayer().dropMessage("Reactor Name: " + pto.getName() + " - ID: " + pto.getId());
                System.out.println("Reactor ID: " + pto.getId());
            }
            return 1;
        }
    }

    public static class slow extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "@slow [minute]");
                return 0;
            }
            int speed = Integer.parseInt(splitted[1]);
            if (speed <= 0) {
                c.getPlayer().dropMessage(1, "minutes ​​must be greater than 0 ");
                return 1;
            }
            c.getPlayer().giveForceDebuff(MapleDisease.getBySkill(126), MobSkillFactory.getMobSkill(126, 1), speed * 1000 * 60);
            return 1;
        }
    }

    public static class vote extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            String vote = "https://gtop100.com/topsites/MapleStory/sitedetails/Kaotic-Maple-Reborn-Custom-Modded-Imported-Rebirth-Server-v111-101240?vote=1&pingUsername=";
            c.announce(CField.open_url(vote + c.getPlayer().getAccountID()));
            //c.getPlayer().dropMessage(1, "Voting is temp closed.");
            return 1;
        }
    }

    public static class discord extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.announce(CField.open_url("https://discord.gg/95knr9nD9M"));
            //c.getPlayer().dropMessage(1, "Voting is temp closed.");
            return 1;
        }
    }

    public static class claim extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().isJailed()) {
                c.getPlayer().dropMessage(6, "You are currently jailed");
                return 1;
            }
            if (!c.getPlayer().getShopItems().isEmpty()) {
                if (c.getPlayer().gainAllShopItems()) {
                    c.getPlayer().dropMessage(1, "All Shop items have been returned");
                    c.getPlayer().saveToDB();
                } else {
                    c.getPlayer().dropMessage(1, "Make sure you have room for 48 Items in Equip, Use, and ETC.");
                }
            } else {
                c.getPlayer().dropMessage(1, "You have no shop items to claim.");
            }
            return 1;
        }
    }

    public static class compress extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().toggleCompressed();
            c.getPlayer().dropMessage("Compressed damage is currently set to " + c.getPlayer().getCompressed());
            return 1;
        }
    }

    public static class park extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            //int job = Integer.parseInt(splitted[1]);
            //c.getPlayer().resetKeys(job);
            c.getPlayer().getMap().broadcastMessage(CField.getPVPHPBar(c.getPlayer().getId(), c.getPlayer().getStat().getHp(), c.getPlayer().getStat().getCurrentMaxHp()));
            return 1;
        }
    }

    public static class eff extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            //int job = Integer.parseInt(splitted[1]);
            //c.getPlayer().resetKeys(job);
            String effe = splitted[1];
            int oid = Integer.parseInt(splitted[2]);
            //c.announce(EffectPacket.ShowWZEffectOID(effe, oid));
            return 1;
        }
    }

    public static class shop extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter chr = c.getPlayer();
            if (chr.canPlaceShop()) {
                NPCScriptManager.getInstance().startNPC(c, 9200000, "shop");
            } else {
                c.getPlayer().dropMessage("Change maps to fix this issue.");
            }
            return 1;
        }
    }

}
