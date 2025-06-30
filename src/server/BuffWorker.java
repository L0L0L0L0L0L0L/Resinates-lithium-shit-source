/*
 This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
 Copyleft (L) 2016 - 2018 RonanLana

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

 /*
 @Author: Arthur L - Refactored command content into modules
 */
package server;

import client.MapleCharacter;
import constants.GameConstants;
import constants.ServerConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import scripting.EventInstanceManager;
import server.maps.MapleMap;
import tools.packet.CField;

public class BuffWorker implements Runnable {

    private int numTimes = 0;
    private boolean loadshops = false;
    private double exp = 1.0;
    private int month = 0;

    @Override
    public void run() {
        numTimes++;
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            /*
             boolean friday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;
             if (friday && !GameConstants.kaotic) {
             GameConstants.kaotic = true;
             }
             if (!friday && GameConstants.kaotic) {
             GameConstants.kaotic = false;
             }
             */
            int month = Calendar.getInstance().get(Calendar.MONTH);
            long cMonth = GameConstants.getServerVar("Month");
            if (month != cMonth) {
                GameConstants.setServerVar("Month", month);
                cserv.dropMessage("Meta Month has changed to !");
            }
            if (!cserv.getPlayerStorage().getAllCharacters().isEmpty()) {
                long now = System.currentTimeMillis();
                if (numTimes % 10 == 0) {
                    int votez = getTotalVotes();
                    exp = 1 + (votez * 0.0001);
                    GameConstants.setExp(exp);
                    GameConstants.setVotes(votez);
                }
                if (numTimes % 60 == 0) {
                    //GameConstants.setDonationRate(GameConstants.getDonationRate() - 1);
                }
                if (numTimes % 300 == 0) {
                    for (EventInstanceManager eim : cserv.getAllEvents()) {
                        if (eim != null && eim.getPlayers().isEmpty()) {
                            if (!eim.isDisposed()) {
                                for (MapleMap map : eim.getMapFactory().getAllMaps()) {
                                    map.forceDispose();
                                }
                                eim.dispose();
                            }
                        }
                    }
                }
                String tip = "";
                /*
                if (numTimes % 3600 == 0) {
                    GameConstants.setDonationRate(60);
                    long dono = pullFromPool("donation_rate", 99);
                    int base = 1;
                    if (dono > 0) {
                        base = (int) (1 + dono);
                    }
                    GameConstants.setStatRate(base);
                    //GameConstants.setMasteryRate((double) Math.floor(base * 0.1));
                    cserv.dropMessage("[CHAOS HOUR] Damage Rate is now set to " + GameConstants.getStatRate() + "x!");
                    System.out.println("[CHAOS HOUR] Damage Rate is now set to " + GameConstants.getStatRate() + "x!");
                }
                */
                if (numTimes % 14400 == 0) {
                    MapleShopFactory.getInstance().getShop(9300004).changeNXShop();
                    cserv.dropMessage("[Wu Yuan] My NX shop has been restocked!");
                    System.out.println("[Wu Yuan] Shop Updated.");
                    //GameConstants.shufflePals();
                }
                if (numTimes % 28800 == 0) {
                    MapleShopFactory.getInstance().getShop(2050012).changeQuestShop();
                    cserv.dropMessage("[Agent M] My Quest shop has been restocked!");
                    System.out.println("[Agent M] Shop Updated.");
                }
                for (MapleCharacter player : cserv.getPlayerStorage().getAllCharacters()) {
                    if (player != null) {
                        player.handleBuffs();
                        player.handleCooldowns(numTimes, player.getMap().canHurt(now), now);
                        player.setAttack(0);
                        if (player.hackCount > 10) {
                            System.out.println("[" + Calendar.getInstance().getTime() + "] " + player.getName() + " has been removed for too many hack detections.");
                            player.kill();
                        }
                        if (numTimes % 300 == 0) {
                            if (!GameConstants.isBeginnerJob(player.getJob())) {
                                if (!player.battle && player.hasSummon()) {
                                    player.getClient().announce(CField.UIPacket.summonMessage(player.handleTip(), 200, 10));
                                }
                            }
                        }
                        if (numTimes % 60 == 0) {
                            player.expirationTask(false, false);
                            player.saveDojo();
                            player.hackCount = 0;
                        }
                    }
                }
            }
        }
        ServerConstants.setPool(0);
        //System.out.println("Vote points processed " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");
    }

    public long pullFromPool(String var, int max) {
        long dono = GameConstants.getServerVar("donation_rate");
        if (dono > 0) {
            long amountToPull = Randomizer.LongMax(dono, max);
            if (amountToPull > dono) {
                amountToPull = dono;
            }
            GameConstants.setServerVar("donation_rate", dono - amountToPull);
            return amountToPull;
        }
        return dono;
    }

    public void msgAllGms(String text, List<MapleCharacter> charz) {
        for (MapleCharacter player : charz) {
            if (player.isGM()) {
                player.dropMessage(6, text);
            }
        }
    }

    public int getTotalVotes() {
        int count = 0;
        try (Connection con = DatabaseConnection.getPlayerConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM ipvotelog where success = 0"); ResultSet rs = ps.executeQuery()) {
            rs.last();
            count += rs.getRow();
        } catch (SQLException se) {
            System.err.println("unable to read vote count from sql");
            se.printStackTrace();
        }
        return count;
    }

}
