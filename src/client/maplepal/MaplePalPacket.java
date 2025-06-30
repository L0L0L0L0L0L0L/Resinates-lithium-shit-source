package client.maplepal;

import java.util.Collection;
import java.util.List;

import client.MapleCharacter;
import handling.SendPacketOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MaplePalPacket {

    public static byte[] openPalWindow(MapleCharacter chr) {
        MaplePalStorage storage = chr.getPalStorage();
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_PAL_OPEN_UI.getValue());
        mplew.write(0);
        List<MaplePal> activePals = storage.getActivePals();
        mplew.write(activePals.size());
        for (MaplePal pal : activePals) {
            pal.encode(mplew);
        }
        Collection<MaplePal> storedPals = storage.getStoredPals().stream().filter(p -> !p.isEgg()).toList();
        mplew.writeShort(storedPals.size());
        for (MaplePal pal : storedPals) {
            pal.encodeBasic(mplew);
        }

        mplew.writeLong(chr.getAccVar("active_pal"));//TODO: send currently selected leveling pal
        mplew.writeInt(chr.getPalSlots());
        return mplew.getPacket();
    }

    public static byte[] openPalHatchUI(MapleCharacter chr) {
        MaplePalStorage storage = chr.getPalStorage();
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_PAL_OPEN_UI.getValue());
        mplew.write(1);
        Collection<MaplePal> hatching = storage.getHatchingEggs();
        long slots = chr.getAccVar("Hatch_Slot");
        mplew.write((byte) slots);
        mplew.write(hatching.size());
        for (MaplePal egg : hatching) {
            egg.encodeBasic(mplew);
            egg.encodeHatchingInfo(mplew);
        }

        List<MaplePal> eggs = storage.getEggs().stream().toList();
        mplew.writeShort(eggs.size());
        for (MaplePal pal : eggs) {
            pal.encodeBasic(mplew);
        }
        mplew.writeInt((int) slots);
        return mplew.getPacket();
    }

    public static byte[] sendBattle(MaplePalBattle battle) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_PAL_BATTLE_OPEN.getValue());
        mplew.writeShort(battle.backgroundId); //nBackgroundType
        mplew.writeShort(battle.selfPals.size());
        for (MaplePal pal : battle.selfPals) {
            pal.encode(mplew);
            pal.setupHP();
            mplew.writeInt(pal.getStats()[0]);
        }
        mplew.writeShort(battle.foePals.size());
        for (MaplePal pal : battle.foePals) {
            pal.encode(mplew);
            pal.setupHP();
            mplew.writeInt(pal.getStats()[0]);
        }
        battle.encode(mplew);
        return mplew.getPacket();
    }

    public static byte[] sendPalWindowDetails(MaplePal pal) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_PAL_WIDOW_RES.getValue());
        mplew.write(MaplePalRes.RES_PAL_WINDOW_DETAIL);
        pal.encode(mplew);
        return mplew.getPacket();
    }

    public static byte[] sendMoveToActive(long palId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_PAL_WIDOW_RES.getValue());
        mplew.write(MaplePalRes.RES_PAL_WINDOW_MOVE_TO_ACTIVE);
        mplew.writeLong(palId);
        return mplew.getPacket();
    }

    public static byte[] sendMoveToStorage(long palId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_PAL_WIDOW_RES.getValue());
        mplew.write(MaplePalRes.RES_PAL_WINDOW_MOVE_TO_STORAGE);
        mplew.writeLong(palId);
        return mplew.getPacket();
    }

    public static byte[] sendRecycleSuccessful(List<Long> ids) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_PAL_WIDOW_RES.getValue());
        mplew.write(MaplePalRes.RES_PAL_WINDOW_RECYCLE_SUCCESSFUL);
        mplew.writeShort(ids.size());
        for (long id : ids) {
            mplew.writeLong(id);
        }
        return mplew.getPacket();
    }

    public static byte[] updateCurrentLevelingPal(long id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_PAL_WIDOW_RES.getValue());
        mplew.write(MaplePalRes.RES_PAL_WINDOW_SET_ACTIVE_LEVELING);
        mplew.writeLong(id);
        return mplew.getPacket();
    }

    public static byte[] sendBeginHatching(MaplePal egg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_PAL_HATCH_RES.getValue());
        mplew.write(MaplePalRes.RES_PAL_HATCH_BEGIN);
        mplew.writeLong(egg.id);
        mplew.writeInt(egg.hatchingDuration);
        return mplew.getPacket();
    }

    public static byte[] sendCancelHatching(long id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_PAL_HATCH_RES.getValue());
        mplew.write(MaplePalRes.RES_PAL_HATCH_CANCEL);
        mplew.writeLong(id);
        return mplew.getPacket();
    }

    public static byte[] sendHatchingComplete(MaplePal pal) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_PAL_HATCH_RES.getValue());
        mplew.write(MaplePalRes.RES_PAL_HATCH_COMPLETE);
        pal.encode(mplew);
        return mplew.getPacket();
    }

    public static byte[] sendError(String error) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.KAOTIC_PAL_WIDOW_RES.getValue());
        mplew.write(MaplePalRes.RES_PAL_ERROR);
        mplew.writeMapleAsciiString(error);
        return mplew.getPacket();
    }
}
