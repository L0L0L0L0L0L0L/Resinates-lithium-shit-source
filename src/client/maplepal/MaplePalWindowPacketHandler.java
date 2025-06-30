package client.maplepal;

import java.util.ArrayList;
import java.util.List;

import client.MapleClient;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javax.script.ScriptEngine;
import server.MapleItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;

public class MaplePalWindowPacketHandler {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        switch (slea.readEnum(MaplePalReq.class)) {
            case REQ_PAL_WINDOW_DETAIL -> {
                long id = slea.readLong();
                MaplePal pal = c.getPlayer().getPalStorage().getPal(id);
                if (pal != null) {
                    c.announce(MaplePalPacket.sendPalWindowDetails(pal));
                }
            }
            case REQ_PAL_WINDOW_ACTIVE_TO_STORAGE -> {
                long id = slea.readLong();
                if (c.getPlayer().getPalStorage().moveActiveToStorage(id)) {
                    c.announce(MaplePalPacket.sendMoveToStorage(id));
                    c.getPlayer().recalcLocalStats();
                }
            }
            case REQ_PAL_WINDOW_STORAGE_TO_ACTIVE -> {
                long id = slea.readLong();
                if (c.getPlayer().getPalStorage().moveToActive(id)) {
                    c.announce(MaplePalPacket.sendMoveToActive(id));
                    c.getPlayer().recalcLocalStats();
                }
            }
            case REQ_PAL_WINDOW_RECYCLE -> {
                List<Long> idsToRecycle = new ArrayList<>();
                Map<Integer, Integer> energies = new HashMap<Integer, Integer>();
                int count = slea.readShort();
                for (int i = 0; i < count; i++) {
                    idsToRecycle.add(slea.readLong());
                }
                List<MaplePal> recycledPals = idsToRecycle.stream().map(c.getPlayer().getPalStorage()::getPal).filter(Objects::nonNull).toList();
                int soulCount = 0;
                int upgrades = 0;
                for (MaplePal p : recycledPals) {
                    int i = 4200000 + p.element;
                    if (energies.get(i) == null) {
                        energies.put(i, 0);
                    }
                    int total = energies.get(i) + (p.tier * 10 + p.rank);
                    energies.put(i, total);
                    int evo = PalTemplateProvider.getTemplate(p.getModel()).evo();
                    soulCount += (evo * evo);
                    upgrades += p.tier;
                    if (c.getPlayer().getActivePal() != null) {
                        if (p == c.getPlayer().getActivePal()) {
                            c.getPlayer().setAccVar("Active_Pal", 0);
                        }
                    }
                }
                //TODO:calculate rewards for recycling to check for inventory space before actually recycling
                if (c.getPlayer().getPalStorage().removeAll(idsToRecycle)) {
                    //TODO:give rewards
                    if (!energies.isEmpty()) {
                        String txt = "Gained Following Energies:\r\n";
                        Iterator<Integer> iter = energies.keySet().iterator();

                        while (iter.hasNext()) {
                            int i = iter.next();
                            c.getPlayer().gainItem(i, energies.get(i));
                            txt += (MapleItemInformationProvider.getInstance().getName(i) + ": x(" + energies.get(i) + ")\r\n");
                        }
                        if (upgrades > 0) {
                            c.getPlayer().gainItem(4201000, upgrades);
                            txt += (MapleItemInformationProvider.getInstance().getName(4201000) + ": x(" + upgrades + ")\r\n");
                        }
                        if (soulCount > 0) {
                            c.getPlayer().gainItem(4201001, soulCount);
                            txt += (MapleItemInformationProvider.getInstance().getName(4201001) + ": x(" + soulCount + ")");
                        }
                        c.getPlayer().dropMessage(1, txt);
                    }
                    c.announce(MaplePalPacket.sendRecycleSuccessful(idsToRecycle));
                    c.getPlayer().updateStats();
                } else {
                    c.announce(MaplePalPacket.sendError("An error occured.\r\nPlease try again."));
                }
                energies.clear();
            }
            case REQ_PAL_WINDOW_SET_ACTIVE_LEVELING -> {
                long id = slea.readLong();
                if (c.getPlayer().getPalStorage().getActivePals().stream().anyMatch(p -> p.id == id)) {
                    //TODO:save currently selected leveling pal
                    c.announce(MaplePalPacket.updateCurrentLevelingPal(id));
                    c.getPlayer().setAccVar("active_pal", id);
                } else {
                    c.announce(MaplePalPacket.sendError("An error occured.\r\nPlease try again."));
                }
            }
            default -> {
            }
        }
    }

}
