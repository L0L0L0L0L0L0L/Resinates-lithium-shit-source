package client.maplepal;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import client.MapleClient;
import client.maplepal.CraftingProcessor.CraftingEntry;
import handling.RecvPacketOpcode;
import io.github.bucket4j.Bucket;
import java.util.ArrayList;
import java.util.List;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.CField;

public final class KaoticCraftHandler {

    private record CraftingBucket(int recipeId, Bucket bucket) {

    }
    private static final Cache<Integer, CraftingBucket> buckets = Caffeine.newBuilder().expireAfterAccess(3, TimeUnit.HOURS).build();

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c, RecvPacketOpcode recvOp) {
        if (recvOp == RecvPacketOpcode.KAOTIC_CRAFT_ATTEMPT_CRAFT) {
            int test = 0;
            int itemId = slea.readInt();
            int nRemaining = slea.readInt(); //For now this won't be enforced since it doesn't change anything
            if (c.getPlayer().getShop() != null || c.getPlayer().getTrade() != null) {
                c.announce(CraftingProcessor.sendCraftResult(1));
                return;
            }
            int max = CraftingProcessor.getMaxCraftable(c.getPlayer(), itemId);
            //System.out.println("craft base: " + nRemaining + " - max2: " + max);
            if (max > 0) {
                //System.out.println("test1a: " + CraftingProcessor.getMaxCraftable(c.getPlayer(), itemId));
                c.announce(CraftingProcessor.sendCraftResult(0));
            } else {
                //System.out.println("test1b");
                //c.announce(CraftingProcessor.sendCraftResult(1));
                //c.getPlayer().dropMessage("Unable to craft items.");
            }
        } else if (recvOp == RecvPacketOpcode.KAOTIC_CRAFT_FINISH_CRAFT) {
            int test = 0;
            if (c.getPlayer().getShop() != null || c.getPlayer().getTrade() != null) {
                c.announce(CraftingProcessor.sendCraftResult(1));
                return;
            }
            //System.out.println("test2");
            int itemId = slea.readInt();
            CraftingEntry recipe = CraftingProcessor.getRecipe(itemId);
            if (recipe == null) {
                System.out.println("Possible PE hacking attempt from " + c.getPlayer().getName() + " with crafting system.");
                return;
            }
            int nRemaining = slea.readInt() + 2;

            //Check timing to prevent cheating (Note: there is still a potential exploit, switching between crafting recipes to reset bucket)
            CraftingBucket existingBucket = buckets.getIfPresent(c.getPlayer().getId());
            if (existingBucket == null || existingBucket.recipeId != itemId) {
                Bucket newBucket = Bucket.builder().addLimit(limit -> limit.capacity(5).refillGreedy(1, Duration.ofMillis(CraftingProcessor.getRecipeCraftingTime(itemId)))).build();
                existingBucket = new CraftingBucket(itemId, newBucket);
                newBucket.addTokens(5);
                buckets.put(c.getPlayer().getId(), existingBucket);
            }
            if (existingBucket.bucket().tryConsume(1)) {
                int max = CraftingProcessor.getMaxCraftable(c.getPlayer(), itemId);
                //System.out.println("craft: " + nRemaining + " - max: " + max);
                if (max > 0) {
                    if (c.getPlayer().canHold(itemId, recipe.quantity())) {
                        List<Pair<Integer, Long>> itemz = new ArrayList<>();
                        for (var i : recipe.ingredients()) {
                            if (i.itemId() != 0) {
                                if (!c.getPlayer().haveItem(i.itemId(), i.quantity())) {
                                    c.getPlayer().dropMessage("Not enough ingredients to craft item.");
                                    return;
                                }
                            }
                        }
                        for (var i : recipe.ingredients()) {
                            if (i.itemId() != 0) {
                                c.getPlayer().gainItem(i.itemId(), -i.quantity());
                                itemz.add(new Pair<>(i.itemId(), c.getPlayer().countTotalItem(i.itemId())));
                            }
                        }
                        c.getPlayer().gainItem(itemId, recipe.quantity());
                        itemz.add(new Pair<>(itemId, c.getPlayer().countTotalItem(itemId)));
                        recipe.ingredients()[0].itemId();
                        c.announce(CraftingProcessor.sendCraftResult(0));
                        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), CField.EffectPacket.showCraftingEffect(c.getPlayer().getId(), "Effect/BasicEff.img/professions/equip_product", 750, 0), false);
                        c.announce(CraftingProcessor.sendUpdateOverflowItems(itemz));
                        //TODO:check inventory space, take ingredients, give item for one craft
                    } else {
                        c.getPlayer().dropMessage("Not enough space to craft item.");
                    }
                } else {
                    //c.announce(CraftingProcessor.sendCraftResult(2));
                    c.getPlayer().dropMessage("Not enough ingredients to craft item.");
                }
            } else {
                //c.announce(CraftingProcessor.sendCraftResult(2));
                c.getPlayer().dropMessage("Unable to craft items.");
            }
        }
    }
}
