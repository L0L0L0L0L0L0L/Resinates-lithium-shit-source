package server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import constants.GameConstants;
import client.MapleCharacter;
import client.MapleTrait.MapleTraitType;
import client.inventory.EquipAdditions;
import client.inventory.MapleAndroid.AndroidTemplate;
import client.inventory.MapleInventoryType;
import constants.EquipSlot;
import database.DatabaseConnection;
import handling.channel.handler.InventoryHandler;
import handling.world.World;
import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import provider.MapleDataType;
import server.StructSetItem.SetItem;
import server.life.MapleMonster;
import tools.FilePrinter;
import tools.Pair;
import tools.Triple;
import tools.packet.CWvsContext;

public class MapleItemInformationProvider {

    private final static MapleItemInformationProvider instance = new MapleItemInformationProvider();
    protected final MapleDataProvider chrData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Character.wz"));
    protected final MapleDataProvider equipData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Character.wz"));
    protected final MapleDataProvider etcData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));
    protected final MapleDataProvider itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));
    protected final MapleDataProvider stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
    protected MapleData consumeStringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Consume.img");
    protected MapleData etcStringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Etc.img");
    protected final Map<Integer, ItemInformation> dataCache = new HashMap<Integer, ItemInformation>();
    protected final Map<String, List<Triple<String, Point, Point>>> afterImage = new HashMap<String, List<Triple<String, Point, Point>>>();
    protected final Map<Integer, List<StructItemOption>> potentialCache = new HashMap<Integer, List<StructItemOption>>();
    protected final Map<Integer, Map<Integer, StructItemOption>> socketCache = new HashMap<>(); // Grade, (id, data)
    protected final Map<Integer, MapleStatEffect> itemEffects = new HashMap<Integer, MapleStatEffect>();
    protected final Map<Integer, MapleStatEffect> itemEffectsEx = new HashMap<Integer, MapleStatEffect>();
    protected final Map<Integer, Integer> mobIds = new HashMap<Integer, Integer>();
    protected final Map<Integer, Pair<Integer, Integer>> potLife = new HashMap<Integer, Pair<Integer, Integer>>(); //itemid to lifeid, levels
    protected final Map<Integer, StructFamiliar> familiars = new HashMap<Integer, StructFamiliar>(); //by familiarID
    protected final Map<Integer, StructFamiliar> familiars_Item = new HashMap<Integer, StructFamiliar>(); //by cardID
    protected final Map<Integer, StructFamiliar> familiars_Mob = new HashMap<Integer, StructFamiliar>(); //by mobID
    protected final Map<Integer, AndroidTemplate> androids = new HashMap<Integer, AndroidTemplate>();
    protected final Map<Integer, Triple<Integer, List<Integer>, List<Integer>>> monsterBookSets = new HashMap<Integer, Triple<Integer, List<Integer>, List<Integer>>>();
    protected final Map<Integer, StructSetItem> setItems = new HashMap<Integer, StructSetItem>();
    protected Map<Integer, String> equipmentSlotCache = new HashMap<>();
    public static List<Integer> bannedItems = Arrays.asList(new Integer[]{4009180, 4001237, 5820000, 5052000, 5450200, 2450024, 2450025, 2450050, 2450051, 2450052, 2450053});

    protected final Map<Integer, List<GachaponEntry>> gachEntries = new HashMap<>();
    protected final List<String> ANames = new ArrayList<String>();

    public List<Integer> pots = new ArrayList<>();
    public List<Integer> advpots = new ArrayList<>();
    public List<Integer> endpots = new ArrayList<>();
    public List<Integer> godpots = new ArrayList<>();
    public List<Integer> allpots = new ArrayList<>();
    public List<Integer> ASpots = new ArrayList<>();
    public List<Integer> XPpots = new ArrayList<>();
    public List<Integer> Droppots = new ArrayList<>();
    public List<Integer> Powerpots = new ArrayList<>();
    public List<Integer> Mesopots = new ArrayList<>();
    public List<Integer> TDpots = new ArrayList<>();
    public List<Integer> BDpots = new ArrayList<>();
    public List<Integer> OPpots = new ArrayList<>();
    public List<Integer> IEDpots = new ArrayList<>();
    private final List<Integer> power = new ArrayList<>();
    public List<Integer> allHairs = new ArrayList<>();
    public List<Integer> allFaces = new ArrayList<>();
    public List<Integer> allSkins = new ArrayList<>();
    public List<Integer> nxweapons = new ArrayList<>();

    public List<Integer> nxhat = new ArrayList<>();
    public List<Integer> nxoverall = new ArrayList<>();
    public List<Integer> nxtop = new ArrayList<>();
    public List<Integer> nxbottom = new ArrayList<>();
    public List<Integer> nxglove = new ArrayList<>();
    public List<Integer> nxcape = new ArrayList<>();
    public List<Integer> nxshoe = new ArrayList<>();
    public List<Integer> nxshield = new ArrayList<>();
    public List<Integer> emotes = new ArrayList<>();

    public List<Integer> pets = new ArrayList<>();

    public List<Integer> nxequips = new ArrayList<>();
    public List<Integer> nxacc = new ArrayList<>();
    public List<Integer> chairs = new ArrayList<>();
    public List<Integer> allNX = new ArrayList<>();
    public List<Integer> NXPool = new ArrayList<>();
    public List<Integer> filler = new ArrayList<>();

    public void limits(int id, int a, int b, int c) {
        if (allow(id)) {
            int subid = id % 100;
            if (subid <= a) {
                if (!pots.contains(id)) {
                    pots.add(id);
                }
            }
            if (subid <= b && subid >= a) {
                if (!advpots.contains(id)) {
                    advpots.add(id);
                }
            }
            if (subid <= c && subid >= b) {
                if (!endpots.contains(id)) {
                    endpots.add(id);
                }
            }
            if (subid >= c) {
                if (!godpots.contains(id)) {
                    godpots.add(id);
                }
            }
            allpots.add(id);
        }
    }

    public MapleData getStringData(int itemId) {
        if (itemId >= 2000000 && itemId < 3000000) {
            return consumeStringData;
        }
        if (itemId >= 4000000 && itemId < 5000000) {
            return etcStringData;
        }
        return null;
    }

    public void cubes(int id) {
        if (id >= 50000 && id < 50100) {//TD
            limits(id, 4, 9, 24);
            if (!TDpots.contains(id)) {
                TDpots.add(id);
            }
        }
        if (id >= 50100 && id < 50200) {//BD
            limits(id, 9, 24, 49);
            if (!BDpots.contains(id)) {
                BDpots.add(id);
            }
        }
        /*
        if (id >= 50200 && id < 50300) {//Drop rate
            limits(id, 9, 14, 24);
            if (!Droppots.contains(id)) {
                Droppots.add(id);
            }
        }
        /*
        if (id >= 50300 && id < 50400) {//Exp Rate
            limits(id, 9, 14, 24);
            if (!XPpots.contains(id)) {
                XPpots.add(id);
            }
        }
        if (id >= 50400 && id < 50500) {//Meso Rate
            limits(id, 9, 29, 49);
            if (!Mesopots.contains(id)) {
                Mesopots.add(id);
            }
        }
         */
        if (id >= 50500 && id < 50600) {//OP
            limits(id, 4, 9, 29);
            if (!OPpots.contains(id)) {
                OPpots.add(id);
            }
        }
        /*
        if (id >= 50600 && id < 50700) {//IDP
            limits(id, 4, 9, 19);
            if (!Powerpots.contains(id)) {
                Powerpots.add(id);
            }
        }
        if (id >= 50700 && id < 50800) {//IED
            limits(id, 9, 29, 49);
            if (!IEDpots.contains(id)) {
                IEDpots.add(id);
            }
        }
        if (id >= 51000 && id < 51100) {//All stat
            limits(id, 4, 9, 24);
            if (!ASpots.contains(id)) {
                ASpots.add(id);
            }
        }
         */
        if (id >= 51100 && id < 51700) {//All base stats
            limits(id, 9, 19, 29);
        }
        if (id >= 50900 && id < 51000) {//Resist
            limits(id, 0, 2, 4);
        }
        /*
        if (id >= 50800 && id < 50900) {//Cooldown
            limits(id, 0, 2, 4);
        }
         */
    }

    public boolean isBanned(int id) {
        return bannedItems.contains(id);
    }

    public void addCubes(int id) {
        //basic cube
        /*
         if (id < 50000) {
         if (allow(id)) {
         if (!pots.contains(id)) {
         pots.add(id);
         }
         if (!advpots.contains(id)) {
         advpots.add(id);
         }
         }
         }
         */
        //red cube
        if (id >= 50000 && id < 60000) {
            int subid = id % 100;
            if (subid < 5) {
                if (!pots.contains(id)) {
                    if (allow(id)) {
                        pots.add(id);
                    }
                }
            }
            if (subid < 10) {
                if (!advpots.contains(id)) {
                    if (allow(id)) {
                        advpots.add(id);
                    }
                }
            }
            if (!endpots.contains(id)) {
                if (allow(id)) {
                    endpots.add(id);
                }
            }
        }
    }

    public void runEtc() {
        int base = 41;
        for (int i = 0; i < 41; i++) {
            for (int j = 0; j < base; j++) {
                power.add(i);
            }
            base--;
        }
        System.out.println(power.size() + " Item Power weights loaded to cache");

        if (!setItems.isEmpty() || !potentialCache.isEmpty() || !socketCache.isEmpty()) {
            return;
        }
        final MapleData setsData = etcData.getData("SetItemInfo.img");
        StructSetItem itemz;
        SetItem itez;
        for (MapleData dat : setsData) {
            itemz = new StructSetItem();
            itemz.setItemID = Integer.parseInt(dat.getName());
            itemz.completeCount = (byte) MapleDataTool.getIntConvert("completeCount", dat, 0);
            for (MapleData level : dat.getChildByPath("ItemID")) {
                if (level.getType() != MapleDataType.INT) {
                    for (MapleData leve : level) {
                        if (!leve.getName().equals("representName") && !leve.getName().equals("typeName")) {
                            itemz.itemIDs.add(MapleDataTool.getInt(leve));
                        }
                    }
                } else {
                    itemz.itemIDs.add(MapleDataTool.getInt(level));
                }
            }
            for (MapleData level : dat.getChildByPath("Effect")) {
                itez = new SetItem();
                itez.incPDD = MapleDataTool.getIntConvert("incPDD", level, 0);
                itez.incMDD = MapleDataTool.getIntConvert("incMDD", level, 0);
                itez.incSTR = MapleDataTool.getIntConvert("incSTR", level, 0);
                itez.incDEX = MapleDataTool.getIntConvert("incDEX", level, 0);
                itez.incINT = MapleDataTool.getIntConvert("incINT", level, 0);
                itez.incLUK = MapleDataTool.getIntConvert("incLUK", level, 0);
                itez.incACC = MapleDataTool.getIntConvert("incACC", level, 0);
                itez.incPAD = MapleDataTool.getIntConvert("incPAD", level, 0);
                itez.incMAD = MapleDataTool.getIntConvert("incMAD", level, 0);
                itez.incSpeed = MapleDataTool.getIntConvert("incSpeed", level, 0);
                itez.incMHP = MapleDataTool.getIntConvert("incMHP", level, 0);
                itez.incMMP = MapleDataTool.getIntConvert("incMMP", level, 0);
                itez.incMHPr = MapleDataTool.getIntConvert("incMHPr", level, 0);
                itez.incMMPr = MapleDataTool.getIntConvert("incMMPr", level, 0);
                itez.incAllStat = MapleDataTool.getIntConvert("incAllStat", level, 0);
                itez.option1 = MapleDataTool.getIntConvert("Option/1/option", level, 0);
                itez.option2 = MapleDataTool.getIntConvert("Option/2/option", level, 0);
                itez.option1Level = MapleDataTool.getIntConvert("Option/1/level", level, 0);
                itez.option2Level = MapleDataTool.getIntConvert("Option/2/level", level, 0);
                itemz.items.put(Integer.parseInt(level.getName()), itez);
            }
            setItems.put(itemz.setItemID, itemz);
        }
        StructItemOption item;
        final MapleData potsData = itemData.getData("ItemOption.img");
        List<StructItemOption> items;
        for (MapleData dat : potsData) {

            items = new LinkedList<>();
            for (MapleData potLevel : dat.getChildByPath("level")) {
                item = new StructItemOption();
                item.opID = Integer.parseInt(dat.getName());
                item.optionType = MapleDataTool.getIntConvert("info/optionType", dat, 0);
                item.reqLevel = MapleDataTool.getIntConvert("info/reqLevel", dat, 0);
                item.potName = MapleDataTool.getString("info/string", dat, "");
                for (final String i : StructItemOption.types) {
                    if (i.equals("face")) {
                        item.face = MapleDataTool.getString("face", potLevel, "");
                    } else {
                        final int level = MapleDataTool.getIntConvert(i, potLevel, 0);
                        if (level > 0) { // Save memory
                            item.data.put(i, level);
                        }
                    }
                }
                switch (item.opID) {
                    case 31001: // Haste
                    case 31002: // Mystic Door
                    case 31003: // Sharp Eyes
                    case 31004: // Hyper Body
                        item.data.put("skillID", (item.opID - 23001));
                        break;
                    case 41005: // Combat Orders
                    case 41006: // Advanced Blessing
                    case 41007: // Speed Infusion
                        item.data.put("skillID", (item.opID - 33001));
                        break;
                }
                items.add(item);
                cubes(item.opID);
            }

            potentialCache.put(Integer.parseInt(dat.getName()), items);
        }
        final Map<Integer, StructItemOption> gradeS = new HashMap<>();
        final Map<Integer, StructItemOption> gradeA = new HashMap<>();
        final Map<Integer, StructItemOption> gradeB = new HashMap<>();
        final Map<Integer, StructItemOption> gradeC = new HashMap<>();
        final Map<Integer, StructItemOption> gradeD = new HashMap<>();
        final MapleData nebuliteData = itemData.getData("Install/0306.img");
        for (MapleData dat : nebuliteData) {
            item = new StructItemOption();
            item.opID = Integer.parseInt(dat.getName()); // Item Id
            item.optionType = MapleDataTool.getInt("optionType", dat.getChildByPath("socket"), 0);
            for (MapleData info : dat.getChildByPath("socket/option")) {
                final String optionString = MapleDataTool.getString("optionString", info, "");
                final int level = MapleDataTool.getInt("level", info, 0);
                if (level > 0) { // Save memory
                    item.data.put(optionString, level);
                }
            }
            switch (item.opID) {
                case 3063370: // Haste
                    item.data.put("skillID", 8000);
                    break;
                case 3063380: // Mystic Door
                    item.data.put("skillID", 8001);
                    break;
                case 3063390: // Sharp Eyes
                    item.data.put("skillID", 8002);
                    break;
                case 3063400: // Hyper Body
                    item.data.put("skillID", 8003);
                    break;
                case 3064470: // Combat Orders
                    item.data.put("skillID", 8004);
                    break;
                case 3064480: // Advanced Blessing
                    item.data.put("skillID", 8005);
                    break;
                case 3064490: // Speed Infusion
                    item.data.put("skillID", 8006);
                    break;
            }
            switch (GameConstants.getNebuliteGrade(item.opID)) {
                case 4: //S
                    gradeS.put(Integer.parseInt(dat.getName()), item);
                    break;
                case 3: //A
                    gradeA.put(Integer.parseInt(dat.getName()), item);
                    break;
                case 2: //B
                    gradeB.put(Integer.parseInt(dat.getName()), item);
                    break;
                case 1: //C
                    gradeC.put(Integer.parseInt(dat.getName()), item);
                    break;
                case 0: //D
                    gradeD.put(Integer.parseInt(dat.getName()), item);
                    break; // impossible to be -1 since we're looping in 306.img.xml					
            }
        }
        socketCache.put(4, gradeS);
        socketCache.put(3, gradeA);
        socketCache.put(2, gradeB);
        socketCache.put(1, gradeC);
        socketCache.put(0, gradeD);

        final MapleDataDirectoryEntry e = (MapleDataDirectoryEntry) etcData.getRoot().getEntry("Android");
        for (MapleDataEntry d : e.getFiles()) {
            final MapleData iz = etcData.getData("Android/" + d.getName());
            AndroidTemplate template = new AndroidTemplate();
            for (MapleData ds : iz.getChildByPath("costume/hair")) {
                //System.out.println("hair: " + hair);
                template.hairs.add(MapleDataTool.getInt(ds));
            }
            for (MapleData ds : iz.getChildByPath("costume/face")) {
                //System.out.println("face: " + face);
                template.faces.add(MapleDataTool.getInt(ds));
            }
            for (MapleData ds : iz.getChildByPath("costume/skin")) {
                //System.out.println("skin: " + skin);
                template.skins.add(MapleDataTool.getInt(ds));
            }

            template.capId = MapleDataTool.getIntConvert("basic/cap", iz, 0);
            template.accId = MapleDataTool.getIntConvert("basic/accessory", iz, 0);
            template.overallId = MapleDataTool.getIntConvert("basic/longcoat", iz, 0);
            template.gloveId = MapleDataTool.getIntConvert("basic/glove", iz, 0);
            template.capeId = MapleDataTool.getIntConvert("basic/cape", iz, 0);
            template.pantsId = MapleDataTool.getIntConvert("basic/pants", iz, 0);
            template.shoeId = MapleDataTool.getIntConvert("basic/shoes", iz, 0);

            androids.put(Integer.parseInt(d.getName().substring(0, 4)), template);
        }

        final MapleData lifesData = etcData.getData("ItemPotLifeInfo.img");
        for (MapleData d : lifesData) {
            if (d.getChildByPath("info") != null && MapleDataTool.getInt("type", d.getChildByPath("info"), 0) == 1) {
                potLife.put(MapleDataTool.getInt("counsumeItem", d.getChildByPath("info"), 0), new Pair<Integer, Integer>(Integer.parseInt(d.getName()), d.getChildByPath("level").getChildren().size()));
            }
        }
        List<Triple<String, Point, Point>> thePointK = new ArrayList<Triple<String, Point, Point>>();
        List<Triple<String, Point, Point>> thePointA = new ArrayList<Triple<String, Point, Point>>();

        final MapleDataDirectoryEntry a = (MapleDataDirectoryEntry) chrData.getRoot().getEntry("Afterimage");
        for (MapleDataEntry b : a.getFiles()) {
            final MapleData iz = chrData.getData("Afterimage/" + b.getName());
            List<Triple<String, Point, Point>> thePoint = new ArrayList<Triple<String, Point, Point>>();
            Map<String, Pair<Point, Point>> dummy = new HashMap<String, Pair<Point, Point>>();
            for (MapleData i : iz) {
                for (MapleData xD : i) {
                    if (xD.getName().contains("prone") || xD.getName().contains("double") || xD.getName().contains("triple")) {
                        continue;
                    }
                    if ((b.getName().contains("bow") || b.getName().contains("Bow")) && !xD.getName().contains("shoot")) {
                        continue;
                    }
                    if ((b.getName().contains("gun") || b.getName().contains("cannon")) && !xD.getName().contains("shot")) {
                        continue;
                    }
                    if (dummy.containsKey(xD.getName())) {
                        if (xD.getChildByPath("lt") != null) {
                            Point lt = (Point) xD.getChildByPath("lt").getData();
                            Point ourLt = dummy.get(xD.getName()).left;
                            if (lt.x < ourLt.x) {
                                ourLt.x = lt.x;
                            }
                            if (lt.y < ourLt.y) {
                                ourLt.y = lt.y;
                            }
                        }
                        if (xD.getChildByPath("rb") != null) {
                            Point rb = (Point) xD.getChildByPath("rb").getData();
                            Point ourRb = dummy.get(xD.getName()).right;
                            if (rb.x > ourRb.x) {
                                ourRb.x = rb.x;
                            }
                            if (rb.y > ourRb.y) {
                                ourRb.y = rb.y;
                            }
                        }
                    } else {
                        Point lt = null, rb = null;
                        if (xD.getChildByPath("lt") != null) {
                            lt = (Point) xD.getChildByPath("lt").getData();
                        }
                        if (xD.getChildByPath("rb") != null) {
                            rb = (Point) xD.getChildByPath("rb").getData();
                        }
                        dummy.put(xD.getName(), new Pair<Point, Point>(lt, rb));
                    }
                }
            }
            for (Entry<String, Pair<Point, Point>> ez : dummy.entrySet()) {
                if (ez.getKey().length() > 2 && ez.getKey().substring(ez.getKey().length() - 2, ez.getKey().length() - 1).equals("D")) { //D = double weapon
                    thePointK.add(new Triple<String, Point, Point>(ez.getKey(), ez.getValue().left, ez.getValue().right));
                } else if (ez.getKey().contains("PoleArm")) { //D = double weapon
                    thePointA.add(new Triple<String, Point, Point>(ez.getKey(), ez.getValue().left, ez.getValue().right));
                } else {
                    thePoint.add(new Triple<String, Point, Point>(ez.getKey(), ez.getValue().left, ez.getValue().right));
                }
            }
            afterImage.put(b.getName().substring(0, b.getName().length() - 4), thePoint);
        }
        afterImage.put("katara", thePointK); //hackish
        afterImage.put("aran", thePointA); //hackish
    }

    public List<Integer> getPotentials() {
        return pots;
    }

    public boolean allow(int id) {
        /*
         if (id >= 50500 && id < 50600) {//IDP
         return false;
         }
         */
        switch (id) {
            case 901:
            case 902:
            case 903:
            case 904:
            case 905:
            case 30701://auto steal
            case 30702://auto steal
            case 40701://auto steal
            case 40702://auto steal
            case 40703://auto steal
            case 30371://% invniicible
            case 40371://% invniicible
            case 20366://invincible
            case 30366://invincible
            case 40366://invincible
            case 30356://ignore% damage
            case 30357://ignore% damage
            case 40356://ignore% damage
            case 40357://invincible
            case 20351://ignore% damage
            case 20352://ignore% damage
            case 40376://reflect
            case 40377://reflect
            case 41005://Skill
            case 41006://Skill
            case 41007://Skill
            case 30106://Inc Skill
            case 30107://Inc Skill
            case 40106://Inc Skill
            case 40107://Inc Skill
            case 31001://Skill
            case 31002://Skill
            case 31003://Skill
            case 31004://Skill
                return false;
        }
        return true;
    }

    public void runItems() {
        if (GameConstants.GMS) { //these must be loaded before items..
            final MapleData fData = etcData.getData("FamiliarInfo.img");
            for (MapleData d : fData) {
                StructFamiliar f = new StructFamiliar();
                f.grade = 0;
                f.mob = MapleDataTool.getInt("mob", d, 0);
                f.passive = MapleDataTool.getInt("passive", d, 0);
                f.itemid = MapleDataTool.getInt("consume", d, 0);
                f.familiar = Integer.parseInt(d.getName());
                familiars.put(f.familiar, f);
                familiars_Item.put(f.itemid, f);
                familiars_Mob.put(f.mob, f);
            }
            final MapleDataDirectoryEntry e = (MapleDataDirectoryEntry) chrData.getRoot().getEntry("Familiar");
            for (MapleDataEntry d : e.getFiles()) {
                final int id = Integer.parseInt(d.getName().substring(0, d.getName().length() - 4));
                if (familiars.containsKey(id)) {
                    familiars.get(id).grade = (byte) MapleDataTool.getInt("grade", chrData.getData("Familiar/" + d.getName()).getChildByPath("info"), 0);
                }
            }

            final MapleData mSetsData = etcData.getData("MonsterBookSet.img");
            for (MapleData d : mSetsData.getChildByPath("setList")) {
                if (MapleDataTool.getInt("deactivated", d, 0) > 0) {
                    continue;
                }
                final List<Integer> set = new ArrayList<Integer>(), potential = new ArrayList<Integer>(3);
                for (MapleData ds : d.getChildByPath("stats/potential")) {
                    if (ds.getType() != MapleDataType.STRING && MapleDataTool.getInt(ds, 0) > 0) {
                        potential.add(MapleDataTool.getInt(ds, 0));
                        if (potential.size() >= 5) {
                            break;
                        }
                    }
                }
                for (MapleData ds : d.getChildByPath("cardList")) {
                    set.add(MapleDataTool.getInt(ds, 0));
                }
                monsterBookSets.put(Integer.parseInt(d.getName()), new Triple<Integer, List<Integer>, List<Integer>>(MapleDataTool.getInt("setScore", d, 0), set, potential));
            }
        }

        try (Connection con = DatabaseConnection.getWorldConnection()) {

            // Load Item Data
            PreparedStatement ps = con.prepareStatement("SELECT * FROM wz_itemdata");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                initItemInformation(rs);
            }
            rs.close();
            ps.close();

            // Load Item Equipment Data
            ps = con.prepareStatement("SELECT * FROM wz_itemequipdata ORDER BY itemid");
            rs = ps.executeQuery();
            while (rs.next()) {
                initItemEquipData(rs);
            }
            rs.close();
            ps.close();

            // Load Item Addition Data
            ps = con.prepareStatement("SELECT * FROM wz_itemadddata ORDER BY itemid");
            rs = ps.executeQuery();
            while (rs.next()) {
                initItemAddData(rs);
            }
            rs.close();
            ps.close();

            // Load Item Reward Data
            ps = con.prepareStatement("SELECT * FROM wz_itemrewarddata ORDER BY itemid");
            rs = ps.executeQuery();
            while (rs.next()) {
                initItemRewardData(rs);
            }
            rs.close();
            ps.close();

            // Finalize all Equipments
            for (Entry<Integer, ItemInformation> entry : dataCache.entrySet()) {
                if (GameConstants.getInventoryType(entry.getKey()) == MapleInventoryType.EQUIP) {
                    finalizeEquipData(entry.getValue());
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        /*
         for (int id : dataCache.keySet()) {
         if (getSetItemID(id) > 0) {
         if (!setItems.containsKey(getSetItemID(id))) {
         System.out.println("Item ID: " + id + " has invalid setid: " + getSetItemID(id));
         }
         }
         }
         */
        System.out.println(dataCache.size() + " items loaded.");
    }

    public void loadCosmetics() {
        /*
         for (int i = 3010000; i < 3020000; i++) {
         //if (i == face(i)) {
         //System.out.println(i);
         //if (item.getType().) {
         //allHairs.add(i);
         //System.out.println(i);
         //}
         //}
         MapleData item = getItemData(i);

         if (item != null) {
         if (getName(i) != null) {
         MapleData smEntry = item.getChildByPath("info/tamingMob");
         if (smEntry == null) {
         System.out.println("item id:" + i);
         filler.add(i);
         }
         }
         }
         //}
         }
         //for (int i : morehairs) {
         //    allHairs.add(i);
         //}
         System.out.println(filler.size());
         //type
        
         1= weapons
         2=cap
         3=cape
         4=coat
         5=glove
         6=longcoat
         7=pants
         8=shield
         9=shoes
         19=chairs
         11=faces
         12=hairs
         
         try (Connection con = DatabaseConnection.getConnection()) {//import script
         try (PreparedStatement ps = con.prepareStatement("INSERT INTO cashitems (id, type, name) VALUES (?, ?, ?)")) {
         for (int value : filler) {
         ps.setInt(1, value);
         ps.setInt(2, 19);
         ps.setString(3, "chair");
         ps.executeUpdate();
         }
         }
         } catch (SQLException ex) {
         ex.printStackTrace();
         }
         System.out.println("acc inserted");
         */

        try (Connection con = DatabaseConnection.getWorldConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 21")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        allFaces.add(rs.getInt("id"));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 20")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        allHairs.add(rs.getInt("id"));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 1")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        if (getSetItemID(id) > 0) {
                            if (setItems.containsKey(getSetItemID(id))) {
                                nxequips.add(rs.getInt("id"));
                                nxweapons.add(rs.getInt("id"));
                            }
                        } else {
                            nxequips.add(rs.getInt("id"));
                            nxweapons.add(rs.getInt("id"));
                        }
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 2")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        if (getSetItemID(id) > 0) {
                            if (setItems.containsKey(getSetItemID(id))) {
                                nxequips.add(rs.getInt("id"));
                                nxhat.add(rs.getInt("id"));
                            }
                        } else {
                            nxequips.add(rs.getInt("id"));
                            nxhat.add(rs.getInt("id"));
                        }
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 4")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        if (getSetItemID(id) > 0) {
                            if (setItems.containsKey(getSetItemID(id))) {
                                nxequips.add(rs.getInt("id"));
                                nxtop.add(rs.getInt("id"));
                            }
                        } else {
                            nxequips.add(rs.getInt("id"));
                            nxtop.add(rs.getInt("id"));
                        }

                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 5")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        if (getSetItemID(id) > 0) {
                            if (setItems.containsKey(getSetItemID(id))) {
                                nxequips.add(rs.getInt("id"));
                                nxglove.add(rs.getInt("id"));
                            }
                        } else {
                            nxequips.add(rs.getInt("id"));
                            nxglove.add(rs.getInt("id"));
                        }

                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 6")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        if (getSetItemID(id) > 0) {
                            if (setItems.containsKey(getSetItemID(id))) {
                                nxequips.add(rs.getInt("id"));
                                nxoverall.add(rs.getInt("id"));
                            }
                        } else {
                            nxequips.add(rs.getInt("id"));
                            nxoverall.add(rs.getInt("id"));
                        }

                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 7")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        if (getSetItemID(id) > 0) {
                            if (setItems.containsKey(getSetItemID(id))) {
                                nxequips.add(rs.getInt("id"));
                                nxbottom.add(rs.getInt("id"));
                            }
                        } else {
                            nxequips.add(rs.getInt("id"));
                            nxbottom.add(rs.getInt("id"));
                        }

                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 8")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        if (getSetItemID(id) > 0) {
                            if (setItems.containsKey(getSetItemID(id))) {
                                nxequips.add(rs.getInt("id"));
                                nxshield.add(rs.getInt("id"));
                            }
                        } else {
                            nxequips.add(rs.getInt("id"));
                            nxshield.add(rs.getInt("id"));
                        }

                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 9")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        if (getSetItemID(id) > 0) {
                            if (setItems.containsKey(getSetItemID(id))) {
                                nxequips.add(rs.getInt("id"));
                                nxshoe.add(rs.getInt("id"));
                            }
                        } else {
                            nxequips.add(rs.getInt("id"));
                            nxshoe.add(rs.getInt("id"));
                        }

                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 10")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        if (getSetItemID(id) > 0) {
                            if (setItems.containsKey(getSetItemID(id))) {
                                nxequips.add(rs.getInt("id"));
                                nxcape.add(rs.getInt("id"));
                            }
                        } else {
                            nxequips.add(rs.getInt("id"));
                            nxcape.add(rs.getInt("id"));
                        }
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 11")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        if (!GameConstants.isRing(id)) {
                            nxacc.add(rs.getInt("id"));
                        }
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 19")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        chairs.add(rs.getInt("id"));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 30")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        pets.add(rs.getInt("id"));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM cashitems WHERE type = 31")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        emotes.add(rs.getInt("id"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("failed to load cashitem. " + e);
        }
        allNX.addAll(nxequips);
        allNX.addAll(nxacc);
        System.out.println(allNX.size() + " NX Items Cached");
        NXPool();
        allSkins.add(0);
        allSkins.add(1);
        allSkins.add(2);
        allSkins.add(3);
        allSkins.add(4);
        allSkins.add(5);
        allSkins.add(9);
        allSkins.add(11);
        allSkins.add(12);
        allSkins.add(13);
        System.out.println(NXPool.size() + " NX Pool Items Cached");
    }

    public void NXPool() {
        if (!NXPool.isEmpty()) {
            NXPool.clear();
        }
        List<Integer> temp = new ArrayList<>(nxequips);
        temp.addAll(nxacc);
        Collections.shuffle(temp);
        for (int i = 0; i < 250; i++) {
            NXPool.add(temp.get(i));
        }
    }

    public List<Integer> getNXPool() {
        return NXPool;
    }

    public void loadGachFromDB() {
        try (Connection con = DatabaseConnection.getWorldConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM gach_data");
            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int townId = rs.getInt("gachid");
                    List<GachaponEntry> items = gachEntries.get(townId);
                    if (items == null) {
                        items = new LinkedList<>();
                        gachEntries.put(townId, items);
                    }
                    GachaponEntry itemEntry = new GachaponEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"));
                    items.add(itemEntry);
                }
            }
            ps.close();
        } catch (SQLException e) {
            FilePrinter.printError(FilePrinter.SQL_EXCEPTION, e, "Failed to get DB connection.");
        }
        System.out.println(gachEntries.size() + " Gach-Pools Cached");
    }

    public void loadGachFromDBbyId(int id) {
        try (Connection con = DatabaseConnection.getWorldConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM gach_data WHERE gachid = ?");
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int townId = rs.getInt("gachid");
                    List<GachaponEntry> items = gachEntries.get(townId);
                    if (items == null) {
                        items = new LinkedList<>();
                        gachEntries.put(townId, items);
                    }
                    GachaponEntry itemEntry = new GachaponEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"));
                    items.add(itemEntry);
                }
            }
            ps.close();
        } catch (SQLException e) {
            FilePrinter.printError(FilePrinter.SQL_EXCEPTION, e, "Failed to get DB connection.");
        }
        System.out.println(gachEntries.size() + " Gach-Pools Cached");
    }

    public List<GachaponEntry> getGachRewards(int townId) {
        List<GachaponEntry> ids = new ArrayList<>();
        if (!gachEntries.containsKey(townId)) {
            loadGachFromDBbyId(townId);
        }
        if (gachEntries.get(townId) != null && !gachEntries.get(townId).isEmpty()) {
            ids = gachEntries.get(townId);
        }
        return ids;
    }

    public void loadNamesFromDB() {
        try (Connection con = DatabaseConnection.getWorldConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM random_names");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ANames.add(rs.getString("string"));
                }
            }
            ps.close();
        } catch (SQLException e) {
            FilePrinter.printError(FilePrinter.SQL_EXCEPTION, e, "Failed to get DB connection.");
        }
        System.out.println(ANames.size() + " Android Names Cached");
    }

    public List<String> getRandomNames() {
        return ANames;
    }

    public String setRandomName() {
        return getRandomNames().get(Randomizer.nextInt(getRandomNames().size() - 1)) + getRandomNames().get(Randomizer.nextInt(getRandomNames().size() - 1));
    }

    public final List<StructItemOption> getPotentialInfo(final int potId) {
        return potentialCache.get(potId);
    }

    public final Map<Integer, List<StructItemOption>> getAllPotentialInfo() {
        return potentialCache;
    }

    public final StructItemOption getSocketInfo(final int potId) {
        final int grade = GameConstants.getNebuliteGrade(potId);
        if (grade == -1) {
            return null;
        }
        return socketCache.get(grade).get(potId);
    }

    public final Map<Integer, StructItemOption> getAllSocketInfo(final int grade) {
        return socketCache.get(grade);
    }

    public final Collection<Integer> getMonsterBookList() {
        return mobIds.values();
    }

    public final Map<Integer, Integer> getMonsterBook() {
        return mobIds;
    }

    public final Pair<Integer, Integer> getPot(int f) {
        return potLife.get(f);
    }

    public final StructFamiliar getFamiliar(int f) {
        return familiars.get(f);
    }

    public final Map<Integer, StructFamiliar> getFamiliars() {
        return familiars;
    }

    public final StructFamiliar getFamiliarByItem(int f) {
        return familiars_Item.get(f);
    }

    public final StructFamiliar getFamiliarByMob(int f) {
        return familiars_Mob.get(f);
    }

    public static final MapleItemInformationProvider getInstance() {
        return instance;
    }

    public final Collection<ItemInformation> getAllItems() {
        return dataCache.values();
    }

    public final AndroidTemplate getAndroidInfo(int i) {
        return androids.get(i);
    }

    public final Triple<Integer, List<Integer>, List<Integer>> getMonsterBookInfo(int i) {
        return monsterBookSets.get(i);
    }

    public final Map<Integer, Triple<Integer, List<Integer>, List<Integer>>> getAllMonsterBookInfo() {
        return monsterBookSets;
    }

    protected final MapleData getItemData(final int itemId) {
        MapleData ret = null;
        final String idStr = "0" + String.valueOf(itemId);
        MapleDataDirectoryEntry root = itemData.getRoot();
        for (final MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            // we should have .img files here beginning with the first 4 IID
            for (final MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr.substring(0, 4) + ".img")) {
                    ret = itemData.getData(topDir.getName() + "/" + iFile.getName());
                    if (ret == null) {
                        return null;
                    }
                    ret = ret.getChildByPath(idStr);
                    return ret;
                } else if (iFile.getName().equals(idStr.substring(1) + ".img")) {
                    ret = itemData.getData(topDir.getName() + "/" + iFile.getName());
                    return ret;
                }
            }
        }
        //equips dont have item effects :)
        /*root = equipData.getRoot();
         for (final MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
         for (final MapleDataFileEntry iFile : topDir.getFiles()) {
         if (iFile.getName().equals(idStr + ".img")) {
         ret = equipData.getData(topDir.getName() + "/" + iFile.getName());
         return ret;
         }
         }
         }*/

        return ret;
    }

    public Integer getItemIdByMob(int mobId) {
        return mobIds.get(mobId);
    }

    public Integer getSetId(int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return Integer.valueOf(i.cardSet);
    }

    /**
     * returns the maximum of items in one slot
     */
    public final short getSlotMax(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.slotMax;
    }

    public final int getWholePrice(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.wholePrice;
    }

    public final double getPrice(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return -1.0;
        }
        return i.price;
    }

    protected int rand(int min, int max) {
        return Math.abs((int) Randomizer.rand(min, max));
    }

    public Equip levelUpEquip(Equip equip, Map<String, Integer> sta) {
        Equip nEquip = (Equip) equip.copy();
        int bonus = 1;
        //is this all the stats?
        try {
            for (Entry<String, Integer> stat : sta.entrySet()) {
                if (stat.getKey().equals("STRMin")) {
                    nEquip.setStr((nEquip.getStr() + rand(stat.getValue().intValue() * bonus, sta.get("STRMax").intValue())));
                } else if (stat.getKey().equals("DEXMin")) {
                    nEquip.setDex((nEquip.getDex() + rand(stat.getValue().intValue() * bonus, sta.get("DEXMax").intValue())));
                } else if (stat.getKey().equals("INTMin")) {
                    nEquip.setInt((nEquip.getInt() + rand(stat.getValue().intValue() * bonus, sta.get("INTMax").intValue())));
                } else if (stat.getKey().equals("LUKMin")) {
                    nEquip.setLuk((nEquip.getLuk() + rand(stat.getValue().intValue() * bonus, sta.get("LUKMax").intValue())));
                } else if (stat.getKey().equals("PADMin")) {
                    nEquip.setWatk((nEquip.getWatk() + rand(stat.getValue().intValue() * bonus, sta.get("PADMax").intValue())));
                } else if (stat.getKey().equals("PDDMin")) {
                    nEquip.setWdef((nEquip.getWdef() + rand(stat.getValue().intValue() * bonus, sta.get("PDDMax").intValue())));
                } else if (stat.getKey().equals("MADMin")) {
                    nEquip.setMatk((nEquip.getMatk() + rand(stat.getValue().intValue() * bonus, sta.get("MADMax").intValue())));
                } else if (stat.getKey().equals("MDDMin")) {
                    nEquip.setMdef((nEquip.getMdef() + rand(stat.getValue().intValue() * bonus, sta.get("MDDMax").intValue())));
                } else if (stat.getKey().equals("SpeedMin")) {
                    nEquip.setSpeed((nEquip.getSpeed() + rand(stat.getValue().intValue() * bonus, sta.get("SpeedMax").intValue())));
                } else if (stat.getKey().equals("JumpMin")) {
                    nEquip.setJump((nEquip.getJump() + rand(stat.getValue().intValue() * bonus, sta.get("JumpMax").intValue())));
                } else if (stat.getKey().equals("MHPMin")) {
                    nEquip.setHp((nEquip.getHp() + rand(stat.getValue().intValue() * bonus, sta.get("MHPMax").intValue())));
                } else if (stat.getKey().equals("MMPMin")) {
                    nEquip.setMp((nEquip.getMp() + rand(stat.getValue().intValue() * bonus, sta.get("MMPMax").intValue())));
                } else if (stat.getKey().equals("MaxHPMin")) {
                    nEquip.setHp((nEquip.getHp() + rand(stat.getValue().intValue() * bonus, sta.get("MaxHPMax").intValue())));
                } else if (stat.getKey().equals("MaxMPMin")) {
                    nEquip.setMp((nEquip.getMp() + rand(stat.getValue().intValue() * bonus, sta.get("MaxMPMax").intValue())));
                }
            }
        } catch (NullPointerException e) {
            //catch npe because obviously the wz have some error XD
            e.printStackTrace();
        }
        return nEquip;
    }

    public final EnumMap<EquipAdditions, Pair<Integer, Integer>> getEquipAdditions(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.equipAdditions;
    }

    public final Map<Integer, Map<String, Integer>> getEquipIncrements(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.equipIncs;
    }

    public final List<Integer> getEquipSkills(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.incSkill;
    }

    public final Map<String, Integer> getEquipStats(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.equipStats;
    }

    public final boolean canEquip(final Map<String, Integer> stats, final int itemid, final int level, final int job, final int fame, final int str, final int dex, final int luk, final int int_, final int supremacy) {
        if (str >= (stats.containsKey("reqSTR") ? stats.get("reqSTR") : 0) && dex >= (stats.containsKey("reqDEX") ? stats.get("reqDEX") : 0) && luk >= (stats.containsKey("reqLUK") ? stats.get("reqLUK") : 0) && int_ >= (stats.containsKey("reqINT") ? stats.get("reqINT") : 0)) {
            final Integer fameReq = stats.get("reqPOP");
            return fameReq == null || fame >= fameReq;
        }
        return false;
    }

    public final int getAndroid(final int itemId) {
        if (getEquipStats(itemId) == null || !getEquipStats(itemId).containsKey("android")) {
            return 0;
        }
        return getEquipStats(itemId).get("android");
    }

    public final int getReqLevel(final int itemId) {
        if (getEquipStats(itemId) == null || !getEquipStats(itemId).containsKey("reqLevel")) {
            return 0;
        }
        return getEquipStats(itemId).get("reqLevel");
    }

    public final int getSlots(final int itemId) {
        if (getEquipStats(itemId) == null || !getEquipStats(itemId).containsKey("tuc")) {
            return 0;
        }
        return getEquipStats(itemId).get("tuc");
    }

    public final Integer getSetItemID(final int itemId) {
        if (getEquipStats(itemId) == null || !getEquipStats(itemId).containsKey("setItemID")) {
            return 0;
        }
        return getEquipStats(itemId).get("setItemID");
    }

    public final StructSetItem getSetItem(final int setItemId) {
        return setItems.get(setItemId);
    }

    public final List<Integer> getScrollReqs(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.scrollReqs;
    }

    public void scrollOptionEquipWithChaos(Equip nEquip, long perc) {
        double range = perc;
        if (GameConstants.isDoubleSlot(nEquip.getItemId())) {
            range *= 2;
        }
        nEquip.setOStr((long) statBasePercent(nEquip.getTStr(), nEquip.getOStr(), Randomizer.randomDouble(range, range * 2)));
        nEquip.setODex((long) statBasePercent(nEquip.getTDex(), nEquip.getODex(), Randomizer.randomDouble(range, range * 2)));
        nEquip.setOInt((long) statBasePercent(nEquip.getTInt(), nEquip.getOInt(), Randomizer.randomDouble(range, range * 2)));
        nEquip.setOLuk((long) statBasePercent(nEquip.getTLuk(), nEquip.getOLuk(), Randomizer.randomDouble(range, range * 2)));
        nEquip.setOAtk((long) statBasePercent(nEquip.getTAtk(), nEquip.getOAtk(), (Randomizer.randomDouble(range, range * 2))));
        nEquip.setODef((long) statBasePercent(nEquip.getTDef(), nEquip.getODef(), (Randomizer.randomDouble(range, range * 2))));
        nEquip.setOMatk((long) statBasePercent(nEquip.getTMatk(), nEquip.getOMatk(), (Randomizer.randomDouble(range, range * 2))));
        nEquip.setOMdef((long) statBasePercent(nEquip.getTMdef(), nEquip.getOMdef(), (Randomizer.randomDouble(range, range * 2))));
    }

    public void scrollOptionEquipWithBulkChaos(Equip nEquip, long perc, int scrolls) {
        double range = perc * scrolls;
        if (GameConstants.isDoubleSlot(nEquip.getItemId())) {
            range *= 2;
        }
        nEquip.setOStr((long) statBasePercent(nEquip.getTStr(), nEquip.getOStr(), Randomizer.randomDouble(range, range * 2)));
        nEquip.setODex((long) statBasePercent(nEquip.getTDex(), nEquip.getODex(), Randomizer.randomDouble(range, range * 2)));
        nEquip.setOInt((long) statBasePercent(nEquip.getTInt(), nEquip.getOInt(), Randomizer.randomDouble(range, range * 2)));
        nEquip.setOLuk((long) statBasePercent(nEquip.getTLuk(), nEquip.getOLuk(), Randomizer.randomDouble(range, range * 2)));
        nEquip.setOAtk((long) statBasePercent(nEquip.getTAtk(), nEquip.getOAtk(), (Randomizer.randomDouble(range, range * 2))));
        nEquip.setODef((long) statBasePercent(nEquip.getTDef(), nEquip.getODef(), (Randomizer.randomDouble(range, range * 2))));
        nEquip.setOMatk((long) statBasePercent(nEquip.getTMatk(), nEquip.getOMatk(), (Randomizer.randomDouble(range, range * 2))));
        nEquip.setOMdef((long) statBasePercent(nEquip.getTMdef(), nEquip.getOMdef(), (Randomizer.randomDouble(range, range * 2))));
    }

    public void scrollOptionEquipWithPower(Equip nEquip, int rolls) {
        double range = (1 + (nEquip.getPower() * 0.1));
        if (GameConstants.isDoubleSlot(nEquip.getItemId())) {
            range *= 2;
        }
        for (int i = 0; i < rolls; i++) {
            nEquip.setOStr((long) statBasePercent(nEquip.getTStr(), nEquip.getOStr(), (Randomizer.random(5, 75)) * Randomizer.randomDouble(range, range * 2)));
            nEquip.setODex((long) statBasePercent(nEquip.getTDex(), nEquip.getODex(), (Randomizer.random(5, 75)) * Randomizer.randomDouble(range, range * 2)));
            nEquip.setOInt((long) statBasePercent(nEquip.getTInt(), nEquip.getOInt(), (Randomizer.random(5, 75)) * Randomizer.randomDouble(range, range * 2)));
            nEquip.setOLuk((long) statBasePercent(nEquip.getTLuk(), nEquip.getOLuk(), (Randomizer.random(5, 75)) * Randomizer.randomDouble(range, range * 2)));
            nEquip.setOAtk((long) statBasePercent(nEquip.getTAtk(), nEquip.getOAtk(), (Randomizer.random(5, 75)) * (Randomizer.randomDouble(range, range * 2))));
            nEquip.setODef((long) statBasePercent(nEquip.getTDef(), nEquip.getODef(), (Randomizer.random(5, 75)) * (Randomizer.randomDouble(range, range * 2))));
            nEquip.setOMatk((long) statBasePercent(nEquip.getTMatk(), nEquip.getOMatk(), (Randomizer.random(5, 75)) * (Randomizer.randomDouble(range, range * 2))));
            nEquip.setOMdef((long) statBasePercent(nEquip.getTMdef(), nEquip.getOMdef(), (Randomizer.random(5, 75)) * (Randomizer.randomDouble(range, range * 2))));
        }
    }

    public void scrollOptionEquipWithChaosBonus(Equip equip, int rolls) {
        int boost = GameConstants.isDoubleSlot(equip.getItemId()) ? 2 : 1;
        for (int i = 0; i < rolls; i++) {
            if (equip.getOverPower() > 0) {
                equip.setOverPower(equip.getOverPower() + (Randomizer.random(1, 3) * boost));
            }
            if (equip.getTotalDamage() > 0) {
                equip.setTotalDamage(equip.getTotalDamage() + (Randomizer.random(1, 3) * boost));
            }
            if (equip.getBossDamage() > 0) {
                equip.setBossDamage(equip.getBossDamage() + (Randomizer.random(1, 3) * boost));
            }
            if (equip.getIED() > 0) {
                equip.setIED(equip.getIED() + (Randomizer.random(1, 3) * boost));
            }
            if (equip.getCritDamage() > 0) {
                equip.setCritDamage(equip.getCritDamage() + (Randomizer.random(1, 3) * boost));
            }
            if (equip.getAllStat() > 0) {
                equip.setAllStat(equip.getAllStat() + (Randomizer.random(1, 3) * boost));
            }
        }
    }

    public double statPercent(long base, double percent, int max) {
        return (base > 0 ? base + percent > max ? max : base + percent : 0);
    }

    public double statPercent(long base, double percent) {
        return (base > 0 ? base + percent : 0);
    }

    public double statBasePercent(long base, long add, double percent) {
        return (base > 0 ? add + percent : 0);
    }

    public void scrollOptionEquipWithEE(Equip nEquip, int perc) {//FIX randomizer
        double range = perc;
        if (GameConstants.isDoubleSlot(nEquip.getItemId())) {
            range *= 2;
        }
        nEquip.setOStr(randomBonus(nEquip.getOStr(), 5));
        nEquip.setODex(randomBonus(nEquip.getODex(), 5));
        nEquip.setOInt(randomBonus(nEquip.getOInt(), 5));
        nEquip.setOLuk(randomBonus(nEquip.getOLuk(), 5));
        nEquip.setOAtk(randomBonus(nEquip.getOAtk(), 5));
        nEquip.setOMatk(randomBonus(nEquip.getOMatk(), 5));
        nEquip.setODef(randomBonus(nEquip.getODef(), 5));
        nEquip.setOMdef(randomBonus(nEquip.getOMdef(), 5));
    }

    public void scrollOptionEquipWithEE(Equip nEquip, int[] stats) {//FIX randomizer
        double range = 1;
        if (GameConstants.isDoubleSlot(nEquip.getItemId())) {
            range *= 2;
        }
        nEquip.setStr((int) statPercent(nEquip.getStr(), range * stats[0]));
        nEquip.setDex((int) statPercent(nEquip.getDex(), range * stats[1]));
        nEquip.setInt((int) statPercent(nEquip.getInt(), range * stats[2]));
        nEquip.setLuk((int) statPercent(nEquip.getLuk(), range * stats[3]));
        nEquip.setWatk((int) statPercent(nEquip.getWatk(), range * stats[4]));
        nEquip.setWdef((int) statPercent(nEquip.getWdef(), range * 2 * stats[5]));
        nEquip.setMatk((int) statPercent(nEquip.getMatk(), range * stats[6]));
        nEquip.setMdef((int) statPercent(nEquip.getMdef(), range * 2 * stats[7]));
    }

    public final Item scrollEquipWithId(final Item equip, final Item scrollId, final boolean ws, final MapleCharacter chr, final int vegas) {

        if (equip.getType() == 1) { // See Item.java
            final Equip nEquip = (Equip) equip;
            final Map<String, Integer> stats = getEquipStats(scrollId.getItemId());
            final Map<String, Integer> eqstats = getEquipStats(equip.getItemId());
            if (scrollId == null || stats == null || eqstats == null) {
                if (scrollId != null) {
                    System.out.println("Error with scroll: " + scrollId.getItemId());
                }
                return equip;
            }
            int succ = (GameConstants.isTablet(scrollId.getItemId()) ? GameConstants.getSuccessTablet(scrollId.getItemId(), nEquip.getLevel()) : ((GameConstants.isEquipScroll(scrollId.getItemId()) || GameConstants.isPotentialScroll(scrollId.getItemId()) || !stats.containsKey("success") ? 0 : stats.get("success"))));
            //int curse = (GameConstants.isTablet(scrollId.getItemId()) ? GameConstants.getCurseTablet(scrollId.getItemId(), nEquip.getLevel()) : ((GameConstants.isEquipScroll(scrollId.getItemId()) || GameConstants.isPotentialScroll(scrollId.getItemId()) || !stats.containsKey("cursed") ? 0 : stats.get("cursed"))));
            final int added = (ItemFlag.LUCKS_KEY.check(equip.getFlag()) ? 10 : 0);
            int success = succ + (vegas == 5610000 && succ == 10 ? 20 : (vegas == 5610001 && succ == 60 ? 30 : 0)) + added;

            if (GameConstants.isEquipScroll(scrollId.getItemId())) {
                success = stats.containsKey("success") ? stats.get("success") : 10;
            }

            //if (ItemFlag.LUCKS_KEY.check(equip.getFlag()) && !GameConstants.isPotentialScroll(scrollId.getItemId()) && !GameConstants.isEquipScroll(scrollId.getItemId()) && !GameConstants.isSpecialScroll(scrollId.getItemId())) {
            //    equip.setFlag((short) (equip.getFlag() - ItemFlag.LUCKS_KEY.getValue()));
            //}
            boolean landed = false;
            if (GameConstants.isChaosScroll(scrollId.getItemId())) {
                success = 100;
            }
            int roll = Randomizer.random(1, 100);
            if (roll <= success) {
                switch (scrollId.getItemId()) {
                    case 2040727: { // Spikes on shoe, prevents slip
                        short flag = nEquip.getFlag();
                        flag |= ItemFlag.SPIKES.getValue();
                        nEquip.setFlag(flag);
                        break;
                    }
                    case 2041058: { // Cape for Cold protection
                        short flag = nEquip.getFlag();
                        flag |= ItemFlag.COLD.getValue();
                        nEquip.setFlag(flag);
                        break;
                    }
                    case 5063000:
                    case 2530000:
                    case 2530001: {
                        short flag = nEquip.getFlag();
                        flag |= ItemFlag.LUCKS_KEY.getValue();
                        nEquip.setFlag(flag);
                        break;
                    }
                    case 5064000:
                    case 2531000: {
                        short flag = nEquip.getFlag();
                        flag |= ItemFlag.SHIELD_WARD.getValue();
                        nEquip.setFlag(flag);
                        break;
                    }
                    case 2049180://class
                    case 2049181://class
                    case 2049182://class
                    case 2049183://class
                    case 2049184://class
                    case 2049190://asbo
                    case 2049191://asbo
                    case 2049192://asbo
                    case 2049193://asbo
                    case 2049194://asbo
                    case 2049195://arc
                    case 2049196://arc
                    case 2049197://arc
                    case 2049198://arc
                    case 2049199://arc
                        improveEquipStats(nEquip, stats);
                        break;
                    case 2049041:
                    case 2049030:
                    case 2049031:
                    case 2049032://100% slate
                        nEquip.setUpgradeSlots((short) (nEquip.getUpgradeSlots() + 1));
                        break;
                    case 2049116://black scroll
                        if (roll <= 10) {
                            chr.getClient().getChannelServer().dropColorMessage(6, chr.getName() + " has blown up their " + equip.getItemName());
                            return null;
                        }
                        scrollOptionEquipWithChaos(nEquip, (long) (500 * nEquip.getUpgradeSlots()));
                        nEquip.setUpgradeSlots((short) (0));
                        break;
                    case 2049117://super black scroll
                        if (roll <= 25) {
                            chr.getClient().getChannelServer().dropColorMessage(6, chr.getName() + " has blown up their " + equip.getItemName());
                            return null;
                        }
                        scrollOptionEquipWithChaos(nEquip, (long) (750 * nEquip.getUpgradeSlots()));
                        nEquip.setUpgradeSlots((short) (0));
                        break;
                    case 2049118://black scroll
                        if (roll <= 50) {
                            chr.getClient().getChannelServer().dropColorMessage(6, chr.getName() + " has blown up their " + equip.getItemName());
                            return null;
                        }
                        scrollOptionEquipWithChaos(nEquip, (long) (1000 * nEquip.getUpgradeSlots()));
                        nEquip.setUpgradeSlots((short) (0));
                        break;
                    default: {
                        if (GameConstants.isChaosScroll(scrollId.getItemId())) {
                            if (!ws) {
                                if (Randomizer.random(1, 100) == 1) {
                                    chr.getClient().getChannelServer().dropColorMessage(6, chr.getName() + " has blown up their " + equip.getItemName());
                                    return null;
                                }
                            }
                            scrollOptionEquipWithChaos(nEquip, GameConstants.getChaosNumber(scrollId.getItemId()));
                            break;
                        } else if (GameConstants.isEquipScroll(scrollId.getItemId())) {
                            for (int i = 0; i < (scrollId.getItemId() == 2049305 ? 4 : (scrollId.getItemId() == 2049304 ? 3 : 1)); i++) {
                                if (nEquip.getEnhance() < 50) {
                                    scrollOptionEquipWithEE(nEquip, 5);
                                    nEquip.setEnhance((int) (nEquip.getEnhance() + 1));
                                    if (nEquip.getEnhance() == 50) {
                                        chr.finishAchievement(126);
                                    }
                                }
                            }
                            break;
                        } else if (GameConstants.isPotentialScroll(scrollId.getItemId())) {//disabled
                            break;
                        } else {
                            improveEquipStats(nEquip, stats);
                            break;
                        }
                    }
                }
                if (!GameConstants.isCleanSlate(scrollId.getItemId()) && !GameConstants.isSpecialScroll(scrollId.getItemId()) && !GameConstants.isEquipScroll(scrollId.getItemId()) && !GameConstants.isPotentialScroll(scrollId.getItemId())) {
                    nEquip.setUpgradeSlots((short) (nEquip.getUpgradeSlots() - 1));
                    nEquip.setLevel((short) (nEquip.getLevel() + 1));
                }
            } else {
                if (!GameConstants.isCleanSlate(scrollId.getItemId()) && !GameConstants.isSpecialScroll(scrollId.getItemId()) && !GameConstants.isEquipScroll(scrollId.getItemId()) && !GameConstants.isPotentialScroll(scrollId.getItemId())) {
                    if (!ws) {
                        nEquip.setUpgradeSlots((short) (nEquip.getUpgradeSlots() - 1));
                    }
                }
            }
        }

        return equip;
    }

    public void improveEquipStats(Equip nEquip, Map<String, Integer> stats) {
        for (Entry<String, Integer> stat : stats.entrySet()) {
            final String key = stat.getKey();
            double bonus = stat.getValue().intValue();
            if (GameConstants.isDoubleSlot(nEquip.getItemId())) {
                bonus *= 2.5;
            }
            if (key.equals("STR")) {
                nEquip.setStr((int) (Randomizer.Max((int) (nEquip.getStr() + bonus), 2000000000)));
            }
            if (key.equals("DEX")) {
                nEquip.setDex((int) (Randomizer.Max((int) (nEquip.getDex() + bonus), 2000000000)));
            }
            if (key.equals("INT")) {
                nEquip.setInt((int) (Randomizer.Max((int) (nEquip.getInt() + bonus), 2000000000)));
            }
            if (key.equals("LUK")) {
                nEquip.setLuk((int) (Randomizer.Max((int) (nEquip.getLuk() + bonus), 2000000000)));
            }
            if (key.equals("PAD")) {
                nEquip.setWatk((int) (Randomizer.Max((int) (nEquip.getWatk() + bonus), 2000000000)));
            }
            if (key.equals("PDD")) {
                nEquip.setWdef((int) (Randomizer.Max((int) (nEquip.getWdef() + bonus), 2000000000)));
            }
            if (key.equals("MAD")) {
                nEquip.setMatk((int) (Randomizer.Max((int) (nEquip.getMatk() + bonus), 2000000000)));
            }
            if (key.equals("MDD")) {
                nEquip.setMdef((int) (Randomizer.Max((int) (nEquip.getMdef() + bonus), 2000000000)));
            }
            if (key.equals("Speed")) {
                nEquip.setSpeed((int) (Randomizer.Max((int) (nEquip.getSpeed() + bonus), 100)));
            }
            if (key.equals("MHP")) {
                nEquip.setHp((int) (Randomizer.Max((int) (nEquip.getHp() + bonus), Short.MAX_VALUE)));
            }
            if (key.equals("MMP")) {
                nEquip.setMp((int) (Randomizer.Max((int) (nEquip.getMp() + bonus), Short.MAX_VALUE)));
            }
        }
    }

    public final Item getEquipById(final int equipId) {
        return getEquipById(equipId, -1);
    }

    public final Item getEquipById(final int equipId, final int ringId) {
        final ItemInformation i = getItemInformation(equipId);
        if (i == null) {
            return new Equip(equipId, (short) 0, ringId, (byte) 0);
        }
        final Item eq = i.eq.copy();
        eq.setUniqueId(ringId);
        return eq;
    }

    protected final int getRandStat(final int defaultValue, final int maxRange, int max) {
        int base = Randomizer.nextInt(9) == 0 ? defaultValue + 1 : defaultValue;
        if (base == 0) {
            return 0;
        }
        // vary no more than ceil of 10% of stat
        final int lMaxRange = (int) Math.min(Math.ceil(base * 0.1), maxRange);

        return Randomizer.Max((int) ((base - lMaxRange) + Randomizer.nextInt(lMaxRange * 2 + 1)), max);
    }

    public final Equip randomizeStats(final Equip equip) {
        int scale = Randomizer.random(1, 4);
        if (isCash(equip.getItemId())) {
            equip.setStr(0);
            equip.setDex(0);
            equip.setLuk(0);
            equip.setInt(0);
            equip.setWatk(0);
            equip.setMatk(0);
            equip.setWdef(0);
            equip.setMdef(0);
            equip.setJump(0);
            equip.setSpeed(0);
            equip.setPotential1(0);
            equip.setPotential2(0);
            equip.setPotential3(0);
            equip.setPotential4(0);
            equip.setPotential5(0);
            equip.setViciousHammer((byte) 0);
            equip.setEnhance(0);
            if (equip.getUpgradeSlots() > 0) {
                equip.setUpgradeSlots((short) 0);
            }
            equip.setPower(0);
            equip.setLevel((short) 0);
            equip.setItemEXP((short) 0);
            equip.setHp(0);
            equip.setMp(0);
            equip.setHpr(0);
            equip.setMpr(0);
        } else {
            baseStats(equip, scale, scale);
        }
        return equip;
    }

    public final Equip randomizeStats(final Equip equip, int scale) {
        baseStats(equip, scale, scale);
        return equip;
    }

    public void baseStats(Equip equip, double level, int scale) {
        if (GameConstants.isWeapon(equip.getItemId())) {
            if (Randomizer.nextBoolean()) {
                equip.setWatk((scale * 4) + generateStat((int) scale * 4));
                equip.setMatk(0);
            } else {
                equip.setWatk(0);
                equip.setMatk((scale * 4) + generateStat((int) scale * 4));
            }
            equip.setWdef(generateStat((int) scale));
            equip.setMdef(generateStat((int) scale));
        } else {
            if (Randomizer.nextBoolean()) {
                equip.setWatk(generateStat((int) scale));
                equip.setMatk(0);
            } else {
                equip.setWatk(0);
                equip.setMatk(generateStat((int) scale));
            }
            equip.setWdef((scale * 2) + generateStat((int) scale * 4));
            equip.setMdef((scale * 2) + generateStat((int) scale * 4));
        }
        equip.setStr(generateStat((int) scale * 2));
        equip.setDex(generateStat((int) scale * 2));
        equip.setLuk(generateStat((int) scale * 2));
        equip.setInt(generateStat((int) scale * 2));
        equip.setSpeed((int) (equip.getSpeed()));
        List<Integer> LootPots = pots;
        equip.setPotential1(LootPots.get(Randomizer.nextInt(LootPots.size())));
        equip.setPotential2(0);
        equip.setPotential3(0);
        equip.setPotential4(0);
        equip.setPotential5(0);
        equip.setViciousHammer((byte) 0);
        equip.setEnhance((int) 0);
        equip.setUpgradeSlots((short) (10 + Randomizer.nextInt(scale)));
        equip.setPower(scale);
        if (GameConstants.isAndroid(equip.getItemId())) {
            equip.setOwner("T:" + scale + " Lv.1");
        }
        equip.setLevel((short) 0);
        equip.setItemEXP((short) 0);
        equip.setPVPDamage(0);
        equip.setHp(0);
        equip.setMp(0);
        equip.setHpr(0);
        equip.setMpr(0);
        int lines = 1;
        if (scale > 10) {
            lines += 1;
        }
        if (scale > 25) {
            lines += 1;
        }
        if (scale > 50) {
            lines += 1;
        }
        randomMonsterBonusStats(equip, rand(1, lines), Math.pow(1.0 + (scale * 0.01), 2.0), scale);
    }

    public final Equip randomizeStats(MapleCharacter chr, Equip equip) {
        int scale = (int) (Math.floor(chr.getLevel() / 100) + 1);
        baseStats(equip, scale, scale);
        return equip;
    }

    public final Equip randomizeStats(MapleCharacter chr, Equip equip, int scale) {
        baseStats(equip, scale, scale);
        return equip;
    }

    public final Equip randomizeStats(MapleCharacter chr, Equip equip, double level, int scale) {
        baseStats(equip, level, scale);
        return equip;
    }

    public final Equip randomizeStatsCustom(MapleCharacter chr, Equip equip, double lvl, double scale) {
        randomizePlayerStats(chr, equip, lvl, (int) scale);
        return equip;
    }

    public final Equip randomizeMonsterStats(MapleMonster mob, MapleCharacter chr, Equip equip, double lvl, double scaled) {
        return randomizeMonsterStats(mob, chr, equip, lvl, scaled, false);
    }

    public long generateStat(int item, int base, int scale, double idp, double extra, boolean legend, double kd) {
        double two = (GameConstants.isDoubleSlot(item) ? 2.0 : 1.0);
        double total = idp * scale * extra;
        double bs = 2.5 + (scale * 0.005) * kd;
        double level = (Math.pow(total, Randomizer.randomDouble(1.2, 1.5)) + (Math.pow(idp, Randomizer.randomDouble(2.0, bs)))) * two;
        if (legend) {
            double random = Randomizer.randomDouble(1.5, 1.8);
            level = (Math.pow(total, random) + (Math.pow(idp, Randomizer.randomDouble(2.5, bs)))) * two;
        }
        long tot = (long) (base + level);
        return Randomizer.LongMax(tot, 2000000000);
    }

    public long generateBigStat(int item, int base, int scale, double idp, double extra, boolean legend, double kRate, boolean kDrop) {
        double two = (GameConstants.isDoubleSlot(item) ? 2.0 : 1.0);
        double bb = Randomizer.randomDouble(100.0, Randomizer.randomDouble(100.0, Randomizer.randomDouble(100.0, 500.0)));
        double level = bb * idp * extra * Math.pow(scale, 2.25) * two * Math.pow(kRate, 2);//10
        if (kDrop) {
            bb = Randomizer.randomDouble(250.0, Randomizer.randomDouble(250.0, Randomizer.randomDouble(250.0, 1000.0)));
            level = bb * idp * extra * Math.pow(scale, 2.25) * two * Math.pow(kRate, 5);//10
        }
        long tot = (long) (base + Math.floor(level * 0.001));
        return tot;
    }

    public int generateStat(int scale) {
        long value = (Randomizer.nextBoolean() ? 1 : 0);
        if (value > 0) {
            value = (int) (scale + Randomizer.random(scale));
        }
        return (int) Randomizer.LongMax(value, Integer.MAX_VALUE);
    }

    public int generateStatBonus(int scale, double bonus) {
        long value = (Randomizer.nextBoolean() ? 1 : 0);
        if (value > 0) {
            value = (int) ((scale + Randomizer.random(scale)) * rand(1, (int) (scale * bonus)));
        }
        return (int) Randomizer.LongMax(value, Integer.MAX_VALUE);
    }

    public final Equip randomizeMonsterStats(MapleMonster mob, MapleCharacter chr, Equip equip, double lvl, double bscale, boolean boss) {
        int scale = Randomizer.Max((int) bscale, 999);
        double kdRate = 1.0;
        if (mob != null) {
            kdRate = (mob.getStats().kdRate + (mob.getAttackerSize() * 0.25));
        }
        kdRate += Randomizer.nextDouble(1.0);
        if (GameConstants.isWeapon(equip.getItemId())) {
            if (Randomizer.nextBoolean()) {
                equip.setWatk((int) ((scale * kdRate) + generateStatBonus(scale, kdRate)));
                equip.setMatk(0);
            } else {
                equip.setWatk(0);
                equip.setMatk((int) ((scale * kdRate) + generateStatBonus(scale, kdRate)));
            }
            equip.setWdef(generateStatBonus(scale, kdRate));
            equip.setMdef(generateStatBonus(scale, kdRate));
        } else {
            if (Randomizer.nextBoolean()) {
                equip.setWatk(generateStatBonus(scale, kdRate));
                equip.setMatk(0);
            } else {
                equip.setWatk(0);
                equip.setMatk(generateStatBonus(scale, kdRate));
            }
            equip.setWdef((int) ((scale * kdRate) + generateStatBonus(scale, kdRate)));
            equip.setMdef((int) ((scale * kdRate) + generateStatBonus(scale, kdRate)));
        }
        equip.setStr(generateStatBonus(scale, kdRate));
        equip.setDex(generateStatBonus(scale, kdRate));
        equip.setLuk(generateStatBonus(scale, kdRate));
        equip.setInt(generateStatBonus(scale, kdRate));
        equip.setSpeed(equip.getSpeed());
        List<Integer> LootPots = endpots;
        equip.setPotential1(LootPots.get(Randomizer.nextInt(LootPots.size())));
        equip.setPotential2(0);
        equip.setPotential3(0);
        equip.setPotential4(0);
        equip.setPotential5(0);
        equip.setViciousHammer((byte) 0);
        equip.setEnhance((int) 0);
        short slots = (short) 10;
        if (mob.getStats().mega) {
            slots = 15;
        }
        if (mob.getStats().omega) {
            slots = 25;
        }
        if (mob.getStats().ultimate) {
            slots = 50;
        }
        equip.setUpgradeSlots((short) (slots + Randomizer.random(scale)));
        equip.setPower(scale);
        equip.setHp(0);
        equip.setMp(0);
        equip.setHpr(0);
        equip.setMpr(0);
        if (GameConstants.isAndroid(equip.getItemId())) {
            equip.setOwner("T:" + scale + " Lv.1");
        }
        equip.setLevel((short) 0);
        equip.setItemEXP((short) 0);
        equip.setPVPDamage(0);

        if (mob != null) {
            if (mob.getStats().isExplosiveReward() && mob.getStats().getTrueBoss()) {
                int lines = 1;
                int count = 0;
                if (Randomizer.random(1, 5) == 1) {
                    lines += 1;
                    count++;
                }
                if (Randomizer.random(1, 10) == 1) {
                    lines += 1;
                    count++;
                }
                if (Randomizer.random(1, 5) == 1) {
                    equip.setPotential2(LootPots.get(Randomizer.nextInt(LootPots.size())));
                    count++;
                }
                if (Randomizer.random(1, 10) == 1) {
                    equip.setPotential3(LootPots.get(Randomizer.nextInt(LootPots.size())));
                    count++;
                }
                double max = Randomizer.MinDouble(kdRate, 4.0);
                double pow = Math.pow(1.0 + (scale * 0.01), max);
                randomMonsterBonusStats(equip, lines, pow, scale);
                if (count == 4) {
                    final StringBuilder sb = new StringBuilder();
                    InventoryHandler.addMedalString(chr, sb);
                    sb.append("[Perfect Drop] " + chr.getName());
                    sb.append(" : ");
                    sb.append(" from " + mob.getStats().getName());
                    World.Broadcast.broadcastSmega(CWvsContext.itemMegaphone(sb.toString(), false, chr.getClient().getChannel(), (Item) equip));
                }
            } else {
                randomMonsterBonusStats(equip, 1, 1.0, scale);
            }
        }
        equip.setPVPDamage(0);
        return equip;
    }

    public final Equip randomizePlayerStats(MapleCharacter chr, Equip equip, double lvl, double scale) {
        if (GameConstants.isWeapon(equip.getItemId())) {
            equip.setWatk((int) (equip.getWatk() * 2));
            equip.setMatk((int) (equip.getMatk() * 2));
        } else {
            equip.setWatk((int) (equip.getWatk()));
            equip.setMatk((int) (equip.getMatk()));
        }
        equip.setStr((int) (equip.getStr()));
        equip.setDex((int) (equip.getDex()));
        equip.setLuk((int) (equip.getLuk()));
        equip.setInt((int) (equip.getInt()));
        equip.setWdef((int) (equip.getWdef()));
        equip.setMdef((int) (equip.getMdef()));
        equip.setSpeed((int) (equip.getSpeed()));
        List<Integer> LootPots = new ArrayList<>();
        if (scale >= 0 && scale < 10) {
            LootPots = pots;
        }
        if (scale >= 10 && scale < 25) {
            LootPots = advpots;
        }
        if (scale >= 25) {
            LootPots = endpots;
        }
        equip.setPotential1(LootPots.get(Randomizer.nextInt(LootPots.size())));
        equip.setPotential2(0);
        equip.setPotential3(0);
        equip.setPotential4(0);
        equip.setPotential5(0);
        equip.setViciousHammer((byte) 0);
        equip.setEnhance((int) 0);
        if (equip.getUpgradeSlots() > 0) {
            equip.setUpgradeSlots((short) (10 + Randomizer.nextInt((int) scale)));
        }
        equip.setPower(1);
        if (GameConstants.isAndroid(equip.getItemId())) {
            equip.setOwner("T:" + scale + " Lv.1");
        }
        equip.setLevel((short) 0);
        equip.setItemEXP((short) 0);
        equip.setPVPDamage(0);
        equip.setHp(0);
        equip.setMp(0);
        equip.setHpr(0);
        equip.setMpr(0);
        return equip;
    }

    public final Equip randomizePlayerStats(MapleCharacter chr, Equip equip, double lvl, double scale, int slots) {
        if (GameConstants.isWeapon(equip.getItemId())) {
            equip.setWatk((int) (equip.getWatk() * 2));
            equip.setMatk((int) (equip.getMatk() * 2));
        } else {
            equip.setWatk((int) (equip.getWatk()));
            equip.setMatk((int) (equip.getMatk()));
        }
        equip.setStr((int) (equip.getStr()));
        equip.setDex((int) (equip.getDex()));
        equip.setLuk((int) (equip.getLuk()));
        equip.setInt((int) (equip.getInt()));
        equip.setWdef((int) (equip.getWdef()));
        equip.setMdef((int) (equip.getMdef()));
        equip.setSpeed((int) (equip.getSpeed()));
        List<Integer> LootPots = new ArrayList<>();
        if (scale >= 0 && scale < 10) {
            LootPots = pots;
        }
        if (scale >= 10 && scale < 25) {
            LootPots = advpots;
        }
        if (scale >= 25) {
            LootPots = endpots;
        }
        equip.setPotential1(LootPots.get(Randomizer.nextInt(LootPots.size())));
        equip.setPotential2(0);
        equip.setPotential3(0);
        equip.setPotential4(0);
        equip.setPotential5(0);
        equip.setViciousHammer((byte) 0);
        equip.setEnhance((int) 0);
        if (equip.getUpgradeSlots() > 0) {
            equip.setUpgradeSlots((short) (10 + Randomizer.nextInt((int) scale)));
        }
        equip.setPower(1);
        if (GameConstants.isAndroid(equip.getItemId())) {
            equip.setOwner("T:" + scale + " Lv.1");
        }
        equip.setLevel((short) 0);
        equip.setItemEXP((short) 0);
        equip.setPVPDamage(0);
        equip.setHp(0);
        equip.setMp(0);
        equip.setHpr(0);
        equip.setMpr(0);
        return equip;
    }

    public Equip randomPot(Equip equip) {
        equip.setPotential1(equip.getPotential1() != 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0);
        equip.setPotential2(equip.getPotential2() != 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0);
        equip.setPotential3(equip.getPotential3() != 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0);
        equip.setPotential4(equip.getPotential4() != 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0);
        equip.setPotential5(equip.getPotential5() != 0 ? pots.get(Randomizer.nextInt(pots.size())) : 0);
        return equip;
    }

    public Equip randomMonsterBonusStats(Equip equip, int tier, double bstats, double scale) {
        List<Integer> bonus = Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5});
        Collections.shuffle(bonus);
        for (int i = 0; i < tier; i++) {
            double stats = Math.pow(scale, Randomizer.randomDouble(1.0, 1.25)) * bstats;
            if (GameConstants.isDoubleSlot(equip.getItemId())) {
                stats *= 2;
            }
            int cap = 999999;
            switch (bonus.get(i)) {
                case 0 ->
                    equip.setOverPower(Randomizer.Max((int) stats, cap));
                case 1 ->
                    equip.setTotalDamage(Randomizer.Max((int) stats, cap));
                case 2 ->
                    equip.setBossDamage(Randomizer.Max((int) stats, cap));
                case 3 ->
                    equip.setIED(Randomizer.Max((int) stats, cap));
                case 4 ->
                    equip.setCritDamage(Randomizer.Max((int) stats, cap));
                case 5 ->
                    equip.setAllStat(Randomizer.Max((int) stats, cap));
            }
        }
        return equip;
    }

    public Equip randomBonusStats(Equip equip, int tier, int bstats, double scale) {
        List<Integer> bonus = Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5});
        Collections.shuffle(bonus);
        for (int i = 0; i < tier; i++) {
            int stats = (int) (Randomizer.DoubleMin(1, scale));
            switch (bonus.get(i)) {
                case 0 ->
                    equip.setOverPower(Randomizer.Max(Randomizer.random((int) Math.floor(stats * 0.25), stats), 9999999));
                case 1 ->
                    equip.setTotalDamage(Randomizer.Max(Randomizer.random((int) Math.floor(stats * 0.25), stats), 9999999));
                case 2 ->
                    equip.setBossDamage(Randomizer.Max(Randomizer.random((int) Math.floor(stats * 0.25), stats), 9999999));
                case 3 ->
                    equip.setIED(Randomizer.Max(Randomizer.random((int) Math.floor(stats * 0.25), stats), 9999999));
                case 4 ->
                    equip.setCritDamage(Randomizer.Max(Randomizer.random((int) Math.floor(stats * 0.25), stats), 9999999));
                case 5 ->
                    equip.setAllStat(Randomizer.Max(Randomizer.random((int) Math.floor(stats * 0.25), stats), 9999999));
            }
        }
        return equip;
    }

    public Equip randomBonusStatsPlayer(Equip equip, int tier, int bstats, double scale) {
        List<Integer> bonus = Arrays.asList(new Integer[]{0, 1, 2, 3, 4});
        Collections.shuffle(bonus);
        for (int i = 0; i < tier; i++) {
            int stats = (int) (bstats * Randomizer.DoubleMin(1, scale));
            switch (bonus.get(i)) {
                case 0 ->
                    equip.setOverPower(Randomizer.Max(Randomizer.random((int) Math.floor(stats * 0.25), stats), 9999999));
                case 1 ->
                    equip.setTotalDamage(Randomizer.Max(Randomizer.random((int) Math.floor(stats * 0.25), stats), 9999999));
                case 2 ->
                    equip.setBossDamage(Randomizer.Max(Randomizer.random((int) Math.floor(stats * 0.25), stats), 9999999));
                case 3 ->
                    equip.setCritDamage(Randomizer.Max(Randomizer.random((int) Math.floor(stats * 0.25), stats), 9999999));
                case 4 ->
                    equip.setAllStat(Randomizer.Max(Randomizer.random((int) Math.floor(stats * 0.25), stats), 9999999));
            }
        }
        return equip;
    }

    public final Equip heartStats(Equip equip) {
        equip.setStr(50000);
        equip.setDex(50000);
        equip.setLuk(50000);
        equip.setInt(50000);
        equip.setWatk(5000);
        equip.setMatk(5000);
        equip.setWdef(50000);
        equip.setMdef(50000);
        List<Integer> LootPots = new ArrayList<>(allpots);
        equip.setPotential1(equip.getPotential1() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setPotential2(equip.getPotential2() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setPotential3(equip.getPotential3() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setPotential4(equip.getPotential4() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setPotential5(equip.getPotential5() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setViciousHammer((byte) 0);
        equip.setEnhance((int) 0);
        if (equip.getUpgradeSlots() > 0) {
            equip.setUpgradeSlots((short) (10));
        }
        equip.setPower((int) 10);
        equip.setLevel((short) 0);
        equip.setItemEXP((short) 0);
        equip.setIED(500);
        equip.setCritDamage(500);
        equip.setAllStat(500);
        return equip;
    }

    public final Equip heartStats(Equip equip, int tier) {
        equip.setStr(5000 * tier);
        equip.setDex(5000 * tier);
        equip.setLuk(5000 * tier);
        equip.setInt(5000 * tier);
        equip.setWatk(500 * tier);
        equip.setMatk(500 * tier);
        equip.setWdef(5000 * tier);
        equip.setMdef(5000 * tier);
        List<Integer> LootPots = new ArrayList<>(allpots);
        equip.setPotential1(equip.getPotential1() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setPotential2(equip.getPotential2() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setPotential3(equip.getPotential3() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setPotential4(equip.getPotential4() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setPotential5(equip.getPotential5() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setViciousHammer((byte) 0);
        equip.setEnhance((int) 0);
        if (equip.getUpgradeSlots() > 0) {
            equip.setUpgradeSlots((short) (10 + tier));
        }
        equip.setPower((int) tier);
        equip.setLevel((short) 0);
        equip.setItemEXP((short) 0);
        equip.setIED(50 * tier);
        equip.setOverPower(50 * tier);
        equip.setCritDamage(50 * tier);
        equip.setAllStat(50 * tier);
        return equip;
    }

    public final Equip godStats(Equip equip, int tier, int TD, int BD, int OP, int IED, int CD, int AS) {
        equip.setStr(5000 * tier);
        equip.setDex(5000 * tier);
        equip.setLuk(5000 * tier);
        equip.setInt(5000 * tier);
        equip.setWatk(500 * tier);
        equip.setMatk(500 * tier);
        equip.setWdef(5000 * tier);
        equip.setMdef(5000 * tier);
        List<Integer> LootPots = new ArrayList<>(allpots);
        equip.setPotential1(equip.getPotential1() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setPotential2(equip.getPotential2() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setPotential3(equip.getPotential3() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setPotential4(equip.getPotential4() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setPotential5(equip.getPotential5() != 0 ? LootPots.get(Randomizer.nextInt(LootPots.size())) : 0);
        equip.setViciousHammer((byte) 0);
        equip.setEnhance((int) 0);
        if (equip.getUpgradeSlots() > 0) {
            equip.setUpgradeSlots((short) (10 + tier));
        }
        equip.setPower((int) tier);
        equip.setLevel((short) 0);
        equip.setItemEXP((short) 0);
        equip.setTotalDamage(TD * tier);
        equip.setBossDamage(BD * tier);
        equip.setIED(IED * tier);
        equip.setOverPower(OP * tier);
        equip.setCritDamage(CD * tier);
        equip.setAllStat(AS * tier);
        return equip;
    }

    public int randomBonusChance(int value) {
        return Randomizer.nextInt(5) == 0 ? value + 1 : value;
    }

    public long randomIntBonus(long value) {
        return Randomizer.LongMax(value + 1, 2000000000);
    }

    public long randomBonus(long value) {
        return Randomizer.LongMax(value + 1, Long.MAX_VALUE);
    }

    public long randomBonus(long value, int max) {
        return value + 1;
    }

    public int randomBonus(int value, int chance, int amount) {
        int random = Randomizer.nextInt(10);
        while (chance > 0 && random > 0) {
            random = Randomizer.nextInt(10);
            chance--;
        }
        return random == 0 ? value + amount : value;
    }

    public int randomAllBonus(int value) {
        return value + 1;
    }

    private static int getRandUpgradedStat(int defaultValue, int maxRange) {
        int value = (short) Randomizer.MinMax((int) (defaultValue + Math.floor(Randomizer.nextDouble() * (maxRange + 1))), 0, Short.MAX_VALUE);
        return defaultValue > 0 ? value > 60000 ? 60000 : value : defaultValue;
    }

    private static int getRandUpgradedMaxStat(int defaultValue, int maxRange, int max) {
        int value = Randomizer.MinMax((int) (defaultValue + Math.floor(Randomizer.nextDouble() * (maxRange + 1))), 0, max);
        return defaultValue > 0 ? value > max ? max : value : defaultValue;
    }

    public final int getTotalStat(final Equip equip) { //i get COOL when my defense is higher on gms...
        return equip.getStr() + equip.getDex() + equip.getInt() + equip.getLuk() + equip.getMatk() + equip.getWatk() + equip.getAcc() + equip.getAvoid() + equip.getJump()
                + equip.getHands() + equip.getSpeed() + equip.getHp() + equip.getMp() + equip.getWdef() + equip.getMdef();
    }

    public final MapleStatEffect getItemEffect(final int itemId) {
        MapleStatEffect ret = itemEffects.get(Integer.valueOf(itemId));
        if (ret == null) {
            final MapleData item = getItemData(itemId);
            if (item == null) {
                return null;
            }
            if (item.getChildByPath("spec") == null) {
                return null;
            }

            ret = MapleStatEffect.loadItemEffectFromData(item.getChildByPath("spec"), itemId);
            itemEffects.put(itemId, ret);
        }
        return ret;
    }

    public final MapleStatEffect getItemEffectEX(final int itemId) {
        MapleStatEffect ret = itemEffectsEx.get(itemId);
        if (ret == null) {
            final MapleData item = getItemData(itemId);
            if (item == null || item.getChildByPath("specEx") == null) {
                return null;
            }
            ret = MapleStatEffect.loadItemEffectFromData(item.getChildByPath("specEx"), itemId);
            itemEffectsEx.put(Integer.valueOf(itemId), ret);
        }
        return ret;
    }

    public final int getCreateId(final int id) {
        final ItemInformation i = getItemInformation(id);
        if (i == null) {
            return 0;
        }
        return i.create;
    }

    public final int getCardMobId(final int id) {
        final ItemInformation i = getItemInformation(id);
        if (i == null) {
            return 0;
        }
        return i.monsterBook;
    }

    public final int getBagType(final int id) {
        final ItemInformation i = getItemInformation(id);
        if (i == null) {
            return 0;
        }
        return i.flag & 0xF;
    }

    public final int getWatkForProjectile(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null || i.equipStats == null || i.equipStats.get("incPAD") == null) {
            return 0;
        }
        return i.equipStats.get("incPAD");
    }

    public final boolean canScroll(final int scrollid, final int itemid) {
        return (scrollid / 100) % 100 == (itemid / 10000) % 100;
    }

    public final String getName(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return "????";
        }
        return i.name;
    }

    public final String getDesc(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.desc;
    }

    public final String getMsg(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.msg;
    }

    public final short getItemMakeLevel(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.itemMakeLevel;
    }

    public final boolean isDropRestricted(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return ((i.flag & 0x200) != 0 || (i.flag & 0x400) != 0 || GameConstants.isDropRestricted(itemId));
    }

    public final boolean isPickupRestricted(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return ((i.flag & 0x80) != 0 || GameConstants.isPickupRestricted(itemId)) && itemId != 4001168 && itemId != 4031306 && itemId != 4031307;
    }

    public final boolean isAccountShared(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x100) != 0;
    }

    public final int getStateChangeItem(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.stateChange;
    }

    public final int getMeso(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return 0;
        }
        return i.meso;
    }

    public final boolean isShareTagEnabled(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x800) != 0;
    }

    public final boolean isKarmaEnabled(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return i.karmaEnabled == 1;
    }

    public final boolean isPKarmaEnabled(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return i.karmaEnabled == 2;
    }

    public final boolean isPickupBlocked(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x40) != 0;
    }

    public final boolean isLogoutExpire(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x20) != 0;
    }

    public final boolean cantSell(final int itemId) { //true = cant sell, false = can sell
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x10) != 0;
    }

    public final Pair<Integer, List<StructRewardItem>> getRewardItem(final int itemid) {
        final ItemInformation i = getItemInformation(itemid);
        if (i == null) {
            return null;
        }
        return new Pair<Integer, List<StructRewardItem>>(i.totalprob, i.rewardItems);
    }

    public final boolean isMobHP(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x1000) != 0;
    }

    public final boolean isQuestItem(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return false;
        }
        return (i.flag & 0x200) != 0 && itemId / 10000 != 301;
    }

    public final Pair<Integer, List<Integer>> questItemInfo(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return new Pair<Integer, List<Integer>>(i.questId, i.questItems);
    }

    public final Pair<Integer, String> replaceItemInfo(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return new Pair<Integer, String>(i.replaceItem, i.replaceMsg);
    }

    public final List<Triple<String, Point, Point>> getAfterImage(final String after) {
        return afterImage.get(after);
    }

    public final String getAfterImage(final int itemId) {
        final ItemInformation i = getItemInformation(itemId);
        if (i == null) {
            return null;
        }
        return i.afterImage;
    }

    public final boolean itemExists(final int itemId) {
        if (GameConstants.getInventoryType(itemId) == MapleInventoryType.UNDEFINED) {
            return false;
        }
        return getItemInformation(itemId) != null;
    }

    public final boolean isCash(final int itemId) {
        if (getEquipStats(itemId) == null) {
            return GameConstants.getInventoryType(itemId) == MapleInventoryType.CASH;
        }
        return GameConstants.getInventoryType(itemId) == MapleInventoryType.CASH || getEquipStats(itemId).get("cash") != null;
    }

    public final ItemInformation getItemInformation(final int itemId) {
        if (itemId <= 0) {
            return null;
        }
        return dataCache.get(itemId);
    }
    private ItemInformation tmpInfo = null;

    public void initItemRewardData(ResultSet sqlRewardData) throws SQLException {
        final int itemID = sqlRewardData.getInt("itemid");
        if (tmpInfo == null || tmpInfo.itemId != itemID) {
            if (!dataCache.containsKey(itemID)) {
                //System.out.println("[initItemRewardData] Tried to load an item while this is not in the cache: " + itemID);
                return;
            }
            tmpInfo = dataCache.get(itemID);
        }

        if (tmpInfo.rewardItems == null) {
            tmpInfo.rewardItems = new ArrayList<StructRewardItem>();
        }

        StructRewardItem add = new StructRewardItem();
        add.itemid = sqlRewardData.getInt("item");
        add.period = (add.itemid == 1122017 ? Math.max(sqlRewardData.getInt("period"), 7200) : sqlRewardData.getInt("period"));
        add.prob = sqlRewardData.getInt("prob");
        add.quantity = sqlRewardData.getShort("quantity");
        add.worldmsg = sqlRewardData.getString("worldMsg").length() <= 0 ? null : sqlRewardData.getString("worldMsg");
        add.effect = sqlRewardData.getString("effect");

        tmpInfo.rewardItems.add(add);
    }

    public void initItemAddData(ResultSet sqlAddData) throws SQLException {
        final int itemID = sqlAddData.getInt("itemid");
        if (tmpInfo == null || tmpInfo.itemId != itemID) {
            if (!dataCache.containsKey(itemID)) {
                //System.out.println("[initItemAddData] Tried to load an item while this is not in the cache: " + itemID);
                return;
            }
            tmpInfo = dataCache.get(itemID);
        }

        if (tmpInfo.equipAdditions == null) {
            tmpInfo.equipAdditions = new EnumMap<EquipAdditions, Pair<Integer, Integer>>(EquipAdditions.class);
        }

        EquipAdditions z = EquipAdditions.fromString(sqlAddData.getString("key"));
        if (z != null) {
            tmpInfo.equipAdditions.put(z, new Pair<Integer, Integer>(sqlAddData.getInt("value1"), sqlAddData.getInt("value2")));
        }
    }

    public void initItemEquipData(ResultSet sqlEquipData) throws SQLException {
        final int itemID = sqlEquipData.getInt("itemid");
        if (tmpInfo == null || tmpInfo.itemId != itemID) {
            if (!dataCache.containsKey(itemID)) {
                return;
            }
            tmpInfo = dataCache.get(itemID);
        }

        if (tmpInfo.equipStats == null) {
            tmpInfo.equipStats = new HashMap<String, Integer>();
        }

        final int itemLevel = sqlEquipData.getInt("itemLevel");
        if (itemLevel == -1) {
            tmpInfo.equipStats.put(sqlEquipData.getString("key"), sqlEquipData.getInt("value"));
        } else {
            if (tmpInfo.equipIncs == null) {
                tmpInfo.equipIncs = new HashMap<Integer, Map<String, Integer>>();
            }

            Map<String, Integer> toAdd = tmpInfo.equipIncs.get(itemLevel);
            if (toAdd == null) {
                toAdd = new HashMap<String, Integer>();
                tmpInfo.equipIncs.put(itemLevel, toAdd);
            }
            toAdd.put(sqlEquipData.getString("key"), sqlEquipData.getInt("value"));
        }
    }

    public void finalizeEquipData(ItemInformation item) {
        int itemId = item.itemId;

        // Some equips do not have equip data. So we initialize it anyway if not initialized
        // already
        // Credits: Jay :)
        if (item.equipStats == null) {
            item.equipStats = new HashMap<String, Integer>();
        }

        item.eq = new Equip(itemId, (byte) 0, -1, (byte) 0);
        short stats = GameConstants.getStat(itemId, 0);
        if (stats > 0) {
            item.eq.setStr(stats);
            item.eq.setDex(stats);
            item.eq.setInt(stats);
            item.eq.setLuk(stats);
        }
        stats = GameConstants.getATK(itemId, 0);
        if (stats > 0) {
            item.eq.setWatk(stats);
            item.eq.setMatk(stats);
        }
        stats = GameConstants.getHpMp(itemId, 0);
        if (stats > 0) {
            item.eq.setHp(stats);
            item.eq.setMp(stats);
        }
        stats = GameConstants.getDEF(itemId, 0);
        if (stats > 0) {
            item.eq.setWdef(stats);
            item.eq.setMdef(stats);
        }
        if (!item.equipStats.isEmpty()) {
            for (Entry<String, Integer> stat : item.equipStats.entrySet()) {
                final String key = stat.getKey();
                switch (key) {
                    case "STR" ->
                        item.eq.setStr(GameConstants.getStat(itemId, stat.getValue()));
                    case "DEX" ->
                        item.eq.setDex(GameConstants.getStat(itemId, stat.getValue()));
                    case "INT" ->
                        item.eq.setInt(GameConstants.getStat(itemId, stat.getValue()));
                    case "LUK" ->
                        item.eq.setLuk(GameConstants.getStat(itemId, stat.getValue()));
                    case "PAD" ->
                        item.eq.setWatk(GameConstants.getATK(itemId, stat.getValue()));
                    case "PDD" ->
                        item.eq.setWdef(GameConstants.getDEF(itemId, stat.getValue()));
                    case "MAD" ->
                        item.eq.setMatk(GameConstants.getATK(itemId, stat.getValue()));
                    case "MDD" ->
                        item.eq.setMdef(GameConstants.getDEF(itemId, stat.getValue()));
                    case "Speed" ->
                        item.eq.setSpeed((short) stat.getValue().shortValue());
                    case "MHP" ->
                        item.eq.setHp(GameConstants.getHpMp(itemId, stat.getValue()));
                    case "MMP" ->
                        item.eq.setMp(GameConstants.getHpMp(itemId, stat.getValue()));
                    case "tuc" ->
                        item.eq.setUpgradeSlots(stat.getValue().shortValue());
                    case "Craft" ->
                        item.eq.setHands(stat.getValue().shortValue());
                    case "durability" ->
                        item.eq.setDurability(stat.getValue());
                    case "charmEXP" ->
                        item.eq.setCharmEXP(stat.getValue().shortValue());
                    case "epicItem" ->
                        item.eq.setEpicItem(true);
                    default -> {
                    }
                }
            }
            if (item.equipStats.get("cash") != null && item.eq.getCharmEXP() <= 0) { //set the exp
                short exp = 0;
                int identifier = itemId / 10000;
                if (GameConstants.isWeapon(itemId) || identifier == 106) { //weapon overall
                    exp = 60;
                } else if (identifier == 100) { //hats
                    exp = 50;
                } else if (GameConstants.isAccessory(itemId) || identifier == 102 || identifier == 108 || identifier == 107) { //gloves shoes accessory
                    exp = 40;
                } else if (identifier == 104 || identifier == 105 || identifier == 110) { //top bottom cape
                    exp = 30;
                }
                item.eq.setCharmEXP(exp);
            }
        }
    }

    public void initItemInformation(ResultSet sqlItemData) throws SQLException {
        final ItemInformation ret = new ItemInformation();
        final int itemId = sqlItemData.getInt("itemid");
        ret.itemId = itemId;
        ret.slotMax = sqlItemData.getShort("slotMax");
        ret.price = Double.parseDouble(sqlItemData.getString("price"));
        ret.wholePrice = sqlItemData.getInt("wholePrice");
        ret.stateChange = sqlItemData.getInt("stateChange");
        ret.name = sqlItemData.getString("name");
        ret.desc = sqlItemData.getString("desc");
        ret.msg = sqlItemData.getString("msg");
        ret.flag = sqlItemData.getInt("flags");
        ret.karmaEnabled = sqlItemData.getByte("karma");
        ret.meso = sqlItemData.getInt("meso");
        ret.monsterBook = sqlItemData.getInt("monsterBook");
        ret.itemMakeLevel = sqlItemData.getShort("itemMakeLevel");
        ret.questId = sqlItemData.getInt("questId");
        ret.create = sqlItemData.getInt("create");
        ret.replaceItem = sqlItemData.getInt("replaceId");
        ret.replaceMsg = sqlItemData.getString("replaceMsg");
        ret.afterImage = sqlItemData.getString("afterImage");
        ret.cardSet = 0;
        if (ret.monsterBook > 0 && itemId / 10000 == 238) {
            mobIds.put(ret.monsterBook, itemId);
            for (Entry<Integer, Triple<Integer, List<Integer>, List<Integer>>> set : monsterBookSets.entrySet()) {
                if (set.getValue().mid.contains(itemId)) {
                    ret.cardSet = set.getKey();
                    break;
                }
            }
        }

        final String scrollRq = sqlItemData.getString("scrollReqs");
        if (scrollRq != null) {
            if (scrollRq.length() > 0) {
                ret.scrollReqs = new ArrayList<Integer>();
                final String[] scroll = scrollRq.split(",");
                for (String s : scroll) {
                    if (s.length() > 1) {
                        ret.scrollReqs.add(Integer.parseInt(s));
                    }
                }
            }
            final String consumeItem = sqlItemData.getString("consumeItem");
            if (consumeItem.length() > 0) {
                ret.questItems = new ArrayList<Integer>();
                final String[] scroll = scrollRq.split(",");
                for (String s : scroll) {
                    if (s.length() > 1) {
                        ret.questItems.add(Integer.parseInt(s));
                    }
                }
            }
        }

        ret.totalprob = sqlItemData.getInt("totalprob");
        final String incRq = sqlItemData.getString("incSkill");
        if (incRq.length() > 0) {
            ret.incSkill = new ArrayList<Integer>();
            final String[] scroll = incRq.split(",");
            for (String s : scroll) {
                if (s.length() > 1) {
                    ret.incSkill.add(Integer.parseInt(s));
                }
            }
        }
        dataCache.put(itemId, ret);
    }

    public List<Integer> getWeapons() {
        return nxweapons;
    }

    public List<Integer> getEquips() {
        return nxequips;
    }

    public List<Integer> getAllNX() {
        return allNX;
    }

    public List<Integer> getAllNXPool() {
        return NXPool;
    }

    public List<Integer> getChairs() {
        return chairs;
    }

    public List<Integer> getHairs() {
        return allHairs;
    }

    public List<Integer> getFaces() {
        return allFaces;
    }

    public List<Integer> getSkins() {
        return allSkins;
    }

    public List<Integer> getPets() {
        return pets;
    }

    public List<Integer> getEmotes() {
        return emotes;
    }

    private MapleData getEquipData(int itemId) {
        MapleData ret = null;
        String idStr = "0" + String.valueOf(itemId);
        MapleDataDirectoryEntry root = equipData.getRoot();
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            for (MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr + ".img")) {
                    return equipData.getData(topDir.getName() + "/" + iFile.getName());
                }
            }
        }
        return ret;
    }

    protected String getEquipmentSlot(int itemId) {
        if (equipmentSlotCache.containsKey(itemId)) {
            return equipmentSlotCache.get(itemId);
        }

        String ret = "";

        MapleData item = getEquipData(itemId);

        if (item == null) {
            return null;
        }

        MapleData info = item.getChildByPath("info");

        if (info == null) {
            return null;
        }

        ret = MapleDataTool.getString("islot", info, "");

        equipmentSlotCache.put(itemId, ret);

        return ret;
    }

    public boolean canWearEquipment(MapleCharacter chr, Equip equip, int dst) {
        int id = equip.getItemId();
        if (GameConstants.isAndroid(id) && dst == -54) {
            return true;
        }
        if (GameConstants.isAndroidHeart(id) && dst == -53) {
            return true;
        }
        if (GameConstants.isPocket(id) && dst == -52) {
            return true;
        }
        String islot = getEquipmentSlot(id);
        return EquipSlot.getFromTextSlot(islot).isAllowed(dst, isCash(id));
    }
}
