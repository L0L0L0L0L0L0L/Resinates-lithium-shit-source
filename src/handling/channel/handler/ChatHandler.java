/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package handling.channel.handler;

import client.MapleClient;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.messages.CommandProcessor;
import constants.ServerConstants.CommandType;
import handling.channel.ChannelServer;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.World;
import tools.StringUtil;
import tools.packet.CField;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CWvsContext;

public class ChatHandler {

    public static final void GeneralChat(final String text, final boolean isMacro, final MapleClient c, final MapleCharacter chr) {
        if (text.length() > 0 && chr != null && chr.getMap() != null && (isMacro || !CommandProcessor.processCommand(c, text, CommandType.NORMAL))) {
            if (!chr.isIntern() && text.length() >= 250) {
                return;
            }
            if (chr.getCanTalk() || chr.isStaff()) {
                //Note: This patch is needed to prevent chat packet from being broadcast to people who might be packet sniffing.
                if (!c.getPlayer().isGM()) {
                    if (c.getPlayer().isRaid()) {
                        for (MapleCharacter player : c.getPlayer().getRaidMembers()) {
                            player.dropColorMessage(3, "[RAID] " + chr.getName() + ": " + text);
                        }

                    } else {
                        chr.getMap().broadcastMessage(CField.getChatText(chr.getId(), text, c.getPlayer().isGM(), isMacro ? 1 : 0), c.getPlayer().getTruePosition());
                    }
                } else {
                    if (c.getPlayer().getGMLevel() == 4) {
                        chr.getMap().broadcastMessage(chr, CWvsContext.serverNotice(14, "[OWNER] " + chr.getName() + " : " + text), true);
                    } else if (c.getPlayer().getGMLevel() == 1) {
                        chr.getMap().broadcastMessage(chr, CWvsContext.serverNotice(14, "[DONOR] " + chr.getName() + " : " + text), true);
                    } else {
                        chr.getMap().broadcastMessage(chr, CWvsContext.serverNotice(2, "[GM] " + chr.getName() + " : " + text), true);
                    }
                }
                System.out.println(chr.getName() + ": " + text);
            } else {
                c.announce(CWvsContext.serverNotice(6, "You have been muted and are therefore unable to talk."));
            }
        }
    }

    public static final void Others(final SeekableLittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        final int type = slea.readByte();
        final byte numRecipients = slea.readByte();
        if (numRecipients <= 0) {
            return;
        }
        int recipients[] = new int[numRecipients];

        for (byte i = 0; i < numRecipients; i++) {
            recipients[i] = slea.readInt();
        }
        final String chattext = slea.readMapleAsciiString();
        if (chr == null || !chr.getCanTalk()) {
            c.announce(CWvsContext.serverNotice(6, "You have been muted and are therefore unable to talk."));
            return;
        }

        if (c.isMonitored()) {
            String chattype = "Unknown";
            switch (type) {
                case 0:
                    chattype = "Buddy";
                    break;
                case 1:
                    chattype = "Party";
                    break;
                case 2:
                    chattype = "Guild";
                    break;
                case 3:
                    chattype = "Alliance";
                    break;
                case 4:
                    chattype = "Expedition";
                    break;
            }
            World.Broadcast.broadcastGMMessage(
                    CWvsContext.serverNotice(6, "[GM Message] " + MapleCharacterUtil.makeMapleReadable(chr.getName())
                            + " said (" + chattype + "): " + chattext));

        }
        if (chattext.length() <= 0) {
            return;
        }
        //chr.getCheatTracker().checkMsg();
        switch (type) {
            case 0:
                World.Buddy.buddyChat(recipients, chr.getId(), chr.getName(), chattext);
                System.out.println("[Buddy] " + chr.getName() + ": " + chattext);
                break;
            case 1:
                if (chr.getParty() == null) {
                    break;
                }
                World.Party.partyChat(chr.getParty().getId(), chattext, chr.getName());
                System.out.println("[Party] " + chr.getName() + ": " + chattext);
                break;
            case 2:
                if (chr.getGuildId() <= 0) {
                    break;
                }
                World.Guild.guildChat(chr.getGuildId(), chr.getName(), chr.getId(), chattext);
                System.out.println("[Guild] " + chr.getName() + ": " + chattext);
                break;
            case 3:
                if (chr.getGuildId() <= 0) {
                    break;
                }
                World.Alliance.allianceChat(chr.getGuildId(), chr.getName(), chr.getId(), chattext);
                System.out.println("[Alliance] " + chr.getName() + ": " + chattext);
                break;
            case 4:
                if (chr.getParty() == null || chr.getParty().getExpeditionId() <= 0) {
                    break;
                }
                World.Party.expedChat(chr.getParty().getExpeditionId(), chattext, chr.getName());
                break;
        }

    }

    public static final void Messenger(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        String input;
        MapleMessenger messenger = c.getPlayer().getMessenger();

        switch (slea.readByte()) {
            case 0x00: // open
                if (messenger == null) {
                    int messengerid = slea.readInt();
                    if (messengerid == 0) { // create
                        c.getPlayer().setMessenger(World.Messenger.createMessenger(new MapleMessengerCharacter(c.getPlayer())));
                    } else { // join
                        messenger = World.Messenger.getMessenger(messengerid);
                        if (messenger != null) {
                            final int position = messenger.getLowestPosition();
                            if (position > -1 && position < 4) {
                                c.getPlayer().setMessenger(messenger);
                                World.Messenger.joinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getPlayer()), c.getPlayer().getName(), c.getChannel());
                            }
                        }
                    }
                }
                break;
            case 0x02: // exit
                if (messenger != null) {
                    final MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
                    World.Messenger.leaveMessenger(messenger.getId(), messengerplayer);
                    c.getPlayer().setMessenger(null);
                }
                break;
            case 0x03: // invite

                if (messenger != null) {
                    final int position = messenger.getLowestPosition();
                    if (position <= -1 || position >= 4) {
                        return;
                    }
                    input = slea.readMapleAsciiString();
                    final MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(input);

                    if (target != null) {
                        if (target.getMessenger() == null) {
                            if (!target.isIntern() || c.getPlayer().isIntern()) {
                                c.announce(CField.messengerNote(input, 4, 1));
                                target.getClient().announce(CField.messengerInvite(c.getPlayer().getName(), messenger.getId()));
                            } else {
                                c.announce(CField.messengerNote(input, 4, 0));
                            }
                        } else {
                            c.announce(CField.messengerChat(c.getPlayer().getName(), " : " + target.getName() + " is already using Maple Messenger."));
                        }
                    } else {
                        if (World.isConnected(input)) {
                            World.Messenger.messengerInvite(c.getPlayer().getName(), messenger.getId(), input, c.getChannel(), c.getPlayer().isIntern());
                        } else {
                            c.announce(CField.messengerNote(input, 4, 0));
                        }
                    }
                }
                break;
            case 0x05: // decline
                final String targeted = slea.readMapleAsciiString();
                final MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(targeted);
                if (target != null) { // This channel
                    if (target.getMessenger() != null) {
                        target.getClient().announce(CField.messengerNote(c.getPlayer().getName(), 5, 0));
                    }
                } else { // Other channel
                    if (!c.getPlayer().isIntern()) {
                        World.Messenger.declineChat(targeted, c.getPlayer().getName());
                    }
                }
                break;
            case 0x06: // message
                if (messenger != null) {
                    final String charname = slea.readMapleAsciiString();
                    final String text = slea.readMapleAsciiString();
                    final String chattext = charname + "" + text;
                    World.Messenger.messengerChat(messenger.getId(), charname, text, c.getPlayer().getName());
                    if (messenger.isMonitored() && chattext.length() > c.getPlayer().getName().length() + 3) { //name : NOT name0 or name1
                        World.Broadcast.broadcastGMMessage(
                                CWvsContext.serverNotice(
                                        6, "[GM Message] " + MapleCharacterUtil.makeMapleReadable(c.getPlayer().getName()) + "(Messenger: "
                                        + messenger.getMemberNamesDEBUG() + ") said: " + chattext));
                    }
                }
                break;
        }
    }

    public static final void Whisper_Find(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        final byte mode = slea.readByte();
        slea.readInt(); //ticks
        switch (mode) {
            case 68: //buddy
            case 5: { // Find

                final String recipient = slea.readMapleAsciiString();
                if ("waldo".equals(recipient) || "Waldo".equals(recipient)) {
                    c.getPlayer().dropMessage("Waldo is hiding somewhere in the world");
                    return;
                }
                MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
                if (player != null) {
                    if (!player.isIntern() || c.getPlayer().isIntern() && player.isIntern()) {
                        c.announce(CField.getFindReplyWithMap(player.getName(), player.getMap().getId(), mode == 68));
                    } else {
                        c.announce(CField.getWhisperReply(recipient, (byte) 0));
                    }
                } else { // Not found
                    int ch = World.Find.findChannel(recipient);
                    if (ch > 0) {
                        player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipient);
                        if (player == null) {
                            break;
                        }
                        if (player != null) {
                            if (!player.isIntern() || (c.getPlayer().isIntern() && player.isIntern())) {
                                c.announce(CField.getFindReply(recipient, (byte) ch, mode == 68));
                            } else {
                                c.announce(CField.getWhisperReply(recipient, (byte) 0));
                            }
                            return;
                        }
                    }
                    if (ch == -10) {
                        c.announce(CField.getFindReplyWithCS(recipient, mode == 68));
                    } else if (ch == -20) {
                        c.getPlayer().dropMessage(5, "'" + recipient + "' is at the MTS."); //idfc
                    } else {
                        c.announce(CField.getWhisperReply(recipient, (byte) 0));
                    }
                }
                break;
            }
            case 6: { // Whisper
                if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
                    return;
                }
                if (!c.getPlayer().getCanTalk()) {
                    c.announce(CWvsContext.serverNotice(6, "You have been muted and are therefore unable to talk."));
                    return;
                }
                //c.getPlayer().getCheatTracker().checkMsg();
                final String recipient = slea.readMapleAsciiString();
                final String text = slea.readMapleAsciiString();
                final int ch = World.Find.findChannel(recipient);
                if (ch > 0) {
                    MapleCharacter player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipient);
                    if (player == null) {
                        break;
                    }
                    player.getClient().announce(CField.getWhisper(c.getPlayer().getName(), c.getChannel(), text));
                    if (!c.getPlayer().isIntern() && player.isIntern()) {
                        c.announce(CField.getWhisperReply(recipient, (byte) 0));
                    } else {
                        c.announce(CField.getWhisperReply(recipient, (byte) 1));
                    }
                    if (c.isMonitored()) {
                        World.Broadcast.broadcastGMMessage(CWvsContext.serverNotice(6, c.getPlayer().getName() + " whispered " + recipient + " : " + text));
                    } else if (player.getClient().isMonitored()) {
                        World.Broadcast.broadcastGMMessage(CWvsContext.serverNotice(6, c.getPlayer().getName() + " whispered " + recipient + " : " + text));
                    }
                    System.out.println("[Whisper] " + c.getPlayer().getName() + ": " + text);
                } else {
                    c.announce(CField.getWhisperReply(recipient, (byte) 0));
                }
            }
            break;
        }
    }
}
