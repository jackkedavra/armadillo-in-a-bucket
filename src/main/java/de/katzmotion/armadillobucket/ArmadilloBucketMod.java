package de.katzmotion.armadillobucket;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ArmadilloBucketMod implements ModInitializer {
    public static final String MOD_ID = "armadillobucket";

    public static final ResourceKey<Item> ARMADILLO_BUCKET_KEY = ResourceKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "armadillo_bucket"));

    public static final Item ARMADILLO_BUCKET = Registry.register(
            BuiltInRegistries.ITEM,
            ARMADILLO_BUCKET_KEY,
            new ArmadilloBucketItem(new Item.Properties().setId(ARMADILLO_BUCKET_KEY).stacksTo(1)));

    @Override
    public void onInitialize() {
        // Rechtsklick mit leerem Eimer auf ein Gürteltier
        UseEntityCallback.EVENT.register(ArmadilloBucketItem::tryCapture);
    }
}
