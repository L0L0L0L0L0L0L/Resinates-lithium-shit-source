package client.maplepal;

import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;

public class MaplePalHatchPacketHandler {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        switch (slea.readEnum(MaplePalReq.class)) {
            case REQ_PAL_HATCH_BEGIN -> {
                long id = slea.readLong();
                if (c.getPlayer().getPalStorage().beginHatching(c.getPlayer(), id)) {
                    c.announce(MaplePalPacket.sendBeginHatching(c.getPlayer().getPalStorage().getPal(id)));
                } else {
                    c.announce(MaplePalPacket.sendError("An error occured.")); //Not an egg?
                }
            }
            case REQ_PAL_HATCH_CANCEL -> {
                long id = slea.readLong();
                if (c.getPlayer().getPalStorage().cancelHatching(id)) {
                    c.announce(MaplePalPacket.sendCancelHatching(id));
                } else {
                    c.announce(MaplePalPacket.sendError("An error occured.")); //Not an egg?
                }
            }
            case REQ_PAL_HATCH_COMPLETE -> {
                MaplePal pal = c.getPlayer().getPalStorage().getPal(slea.readLong());
                if (pal != null && pal.canHatch() && c.getPlayer().getPalStorage().tryHatch(pal.id)) {
                    c.announce(MaplePalPacket.sendHatchingComplete(pal));
                } else {
                    c.announce(MaplePalPacket.sendError("An error occured.")); //Not ready to hatch (or invalid pal ID)
                }
            }
            default -> {
            }
        }
    }

}
