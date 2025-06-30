package client.maplepal;

import client.MapleClient;
import java.util.Iterator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import tools.StringUtil;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CField;

public class MaplePalBattlePacketHandler {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {

        boolean isClose = !slea.readBool();
        if (isClose) {
            /*
            if (!battle.isWin() && battle.pvp && battle.credits > 0 && battle.foe != null) {
                c.getChannelServer().dropMessage(-1, battle.foe.getName() + " has defeated " + c.getPlayer().getName() + " in a Pal Duel!");
                long tc = battle.credits * 5;
                battle.foe.dropMessage(c.getPlayer().getName() + " had challenged you and they lost, you have gained +" + StringUtil.getUnitFullNumber(tc) + " credits.");
                battle.foe.addAccVar("Pal_Credits", tc);
                c.getPlayer().addAccVar("Pal_Credits", -tc);
            }
             */
            c.announce(CField.musicChange(c.getPlayer().getMap().getBGM()));
            c.announce(CField.getPublicNPCInfo());
            c.getPlayer().battle = false;
        } else {
            //Finish battle
            MaplePalBattle battle = MaplePalBattleManager.tryFinish(c.getPlayer().getId());
            if (battle != null) {
                if (battle.isWin()) {
                    if (battle.ach > 0) {
                        c.getPlayer().finishAchievement(battle.ach);
                    }
                    c.getPlayer().addAccVar("Pal_Win", 1);
                    c.getPlayer().checkPalAch();
                    //Give rewards
                    c.announce(CField.musicChange("BgmCustom/Fanfare"));
                    if (battle.credits > 0) {
                        if (battle.foe != null) {
                            c.getChannelServer().dropMessage(-1, c.getPlayer().getName() + " has defeated " + battle.foe.getName() + " in a Pal Duel!");
                            battle.foe.dropMessage(StringUtil.getUnitFullNumber(battle.credits) + " Credits has been stolen from you by " + c.getPlayer().getName());
                            c.getPlayer().addAccVar("Pal_Credits", battle.credits);
                            battle.foe.addAccVar("Pal_Credits", -battle.credits);
                        }
                    }
                    for (MaplePal pal : battle.livePals) {
                        if (pal == battle.activePal) {
                            pal.gainLevelData(c.getPlayer(), battle.rewardEXP);
                        } else {
                            pal.gainLevelData(c.getPlayer(), Randomizer.LongMin((long) Math.floor(battle.rewardEXP * 0.25), 1));
                        }
                    }
                    if (!battle.loot.isEmpty()) {
                        Iterator<Integer> iter = battle.loot.keySet().iterator();
                        while (iter.hasNext()) {
                            int i = iter.next();
                            c.getPlayer().gainItem(i, (int) (battle.loot.get(i)));
                        }
                    }
                    if (battle.random && battle.caught) {
                        c.getPlayer().makeEgg(battle.randomPalId, c.getPlayer().getTotalLevel());
                    }
                    c.getPlayer().gainLevelData(108, battle.rewardEXP);
                } else {
                    if (battle.pvp && battle.credits > 0 && battle.foe != null) {
                        c.getChannelServer().dropMessage(-1, battle.foe.getName() + " has defeated " + c.getPlayer().getName() + " in a Pal Duel!");
                        battle.foe.dropMessage(c.getPlayer().getName() + " had challenged you and they lost, you have gained +" + StringUtil.getUnitFullNumber(battle.credits) + " credits.");
                        battle.foe.addAccVar("Pal_Credits", battle.credits);
                        c.getPlayer().addAccVar("Pal_Credits", -battle.credits);
                    }
                }
            } else if (battle == null) {
                c.getPlayer().dropMessage("An internal error occured when finishing the pal battle.");
            }
        }
    }

}
