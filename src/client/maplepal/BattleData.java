package client.maplepal;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import provider.MapleData;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.data.output.MaplePacketLittleEndianWriter;

public class BattleData {

    private static enum EventType {
        UPDATE_HP,
        ACTION_BAR,
        ATTACK_EFFECT,
        SHOW_DAMAGE,
        MISC_EFFECT,
        SET_PAL,
        BATTLE_END,
        PLAY_SOUND,
        SHOW_TOOLTIP,
    }

    private static record BattleEvent(EventType type, int tEvent, int tDuration, boolean bSelf, int nValue) {

    }

    private int currentTime;
    private List<BattleEvent> events = new LinkedList<>();

    public BattleData delay(int delay) {
        currentTime += delay;
        return this;
    }

    public BattleData setHp(boolean isSelf, int newHp, int duration) {
        events.add(new BattleEvent(EventType.UPDATE_HP, currentTime, duration, isSelf, newHp));
        return this;
    }

    public BattleData actionBar(boolean isSelf, int endPercent, int duration) {
        events.add(new BattleEvent(EventType.ACTION_BAR, currentTime, duration, isSelf, endPercent));
        return this;
    }

    public BattleData showDamage(boolean isSelf, int damage, int delay) {
        events.add(new BattleEvent(EventType.SHOW_DAMAGE, currentTime, delay, isSelf, damage));
        return this;
    }

    public BattleData attackEffect(boolean isSelf, int effectId, boolean simple) {
        events.add(new BattleEvent(EventType.ATTACK_EFFECT, currentTime, 0, isSelf, simple ? 9999 : effectId));
        currentTime += (getEffectDuration(effectId) * 0.25);
        //currentTime += getEffectDuration(effectId);
        return this;
    }

    public BattleData summonEffect(boolean isSelf) {
        events.add(new BattleEvent(EventType.MISC_EFFECT, currentTime, 0, isSelf, 0));
        currentTime += getEffectDuration(0, true);
        return this;
    }

    public BattleData deathEffect(boolean isSelf) {
        events.add(new BattleEvent(EventType.MISC_EFFECT, currentTime, 1000, isSelf, 1)); //Duration = fade time
        currentTime += getEffectDuration(1, true);
        return this;
    }

    public BattleData startBattleEffect() {
        events.add(new BattleEvent(EventType.MISC_EFFECT, currentTime, 0, false, 2));
        currentTime += getEffectDuration(2, true);
        return this;
    }

    public BattleData endBattleEffect(boolean isVictory) {
        events.add(new BattleEvent(EventType.MISC_EFFECT, currentTime, 0, false, isVictory ? 3 : 4));
        currentTime += getEffectDuration(isVictory ? 3 : 4, true);
        return this;
    }

    public BattleData setPal(boolean isSelf, int newIndex) {
        events.add(new BattleEvent(EventType.SET_PAL, currentTime, 0, isSelf, newIndex));
        return this;
    }

    public BattleData finishBattle(boolean isWin) {
        events.add(new BattleEvent(EventType.BATTLE_END, currentTime, 0, false, isWin ? 1 : 0));
        return this;
    }

    public BattleData playSound(int id) {
        events.add(new BattleEvent(EventType.PLAY_SOUND, currentTime, 0, false, id));
        return this;
    }

    public BattleData showToolTip(int skillId) {
        events.add(new BattleEvent(EventType.SHOW_TOOLTIP, currentTime, getEffectDuration(skillId), false, skillId));
        return this;
    }

    public int getTotalTime() {
        return currentTime;
    }

    public void encode(MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort(events.size());
        for (BattleEvent e : events) {
            mplew.writeInt(e.tEvent);
            mplew.write(e.type);
            mplew.writeBool(e.bSelf);
            mplew.writeShort(e.tDuration);
            mplew.writeInt(e.nValue);
        }
    }

    //--------------Static wz loading code for effect delays-------------
    private static final MapleData uiDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/UI.wz")).getData("Custom.img");
    private static final Map<Integer, Integer> effectTimes = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> effectSize = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> effectMiscTimes = new ConcurrentHashMap<>();

    public static int getEffectDuration(int effectId) {
        return getEffectDuration(effectId, false);
    }

    public static int getEffectDurationSize(int effectId) {
        return effectSize.computeIfAbsent(effectId, k -> {
            return uiDataWZ.getChildByPath(String.format("palBattle/%s/%d", "effect", k)).getChildren().size();
        });
    }

    public static int getEffectDuration(int effectId, boolean misc) {
        if (!misc) {
            return effectTimes.computeIfAbsent(effectId, k -> {
                return uiDataWZ.getChildByPath(String.format("palBattle/%s/%d", "effect", k)).getChildren().stream().mapToInt(d -> MapleDataTool.getIntConvert("delay", d, 120)).sum();
            });
        } else {
            return effectMiscTimes.computeIfAbsent(effectId, k -> {
                return uiDataWZ.getChildByPath(String.format("palBattle/%s/%d", "effect_misc", k)).getChildren().stream().mapToInt(d -> MapleDataTool.getIntConvert("delay", d, 120)).sum();
            });
        }
    }
}
