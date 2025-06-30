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
import handling.channel.ChannelServer;

public class VoteWorker implements Runnable {

    @Override
    public void run() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter player : cserv.getPlayerStorage().getAllCharacters()) {
                if (player != null) {
                    player.saveCharToDB2();
                    player.dropMessage(6, "You have been auto saved.");
                }
            }
        }
        //System.out.println("Vote points processed " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");
    }
}
