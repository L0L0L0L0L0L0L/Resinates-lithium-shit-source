package client.messages.commands;

import client.MapleClient;
import constants.ServerConstants.PlayerGMRank;

/**
 *
 * @author Emilyx3
 */
public class DonatorCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.DONATOR;
    }

    public static class Hide extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (!c.getPlayer().isGroup() && c.getPlayer().isAlive() && c.getPlayer().getEventInstance() == null) {
                if (c.getPlayer().isHidden()) {
                    c.getPlayer().setHidden(false);
                    c.getPlayer().dropMessage("Hide Disabled");
                } else {
                    c.getPlayer().setHidden(true);
                    c.getPlayer().dropMessage("Hide Enabled");
                }
                c.getPlayer().getMap().updatePlayer(c.getPlayer());
            } else {
                c.getPlayer().dropMessage("Hide not usable in party or instances.");
            }
            return 1;
        }
    }
}
