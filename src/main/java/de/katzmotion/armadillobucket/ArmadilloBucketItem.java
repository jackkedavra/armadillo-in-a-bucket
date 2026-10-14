package de.katzmotion.armadillobucket;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ArmadilloBucketItem extends Item {

    public ArmadilloBucketItem(Properties properties) {
        super(properties);
    }

    /** Gürteltier einfangen: leerer Eimer + Rechtsklick. */
    public static InteractionResult tryCapture(Player player, Level level, InteractionHand hand,
                                               Entity entity, EntityHitResult hit) {
        if (!(entity instanceof Armadillo armadillo)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.BUCKET)) return InteractionResult.PASS;

        if (level instanceof ServerLevel serverLevel) {
            // Alle Daten des Tiers speichern (Name, Alter, usw.)
            TagValueOutput output = TagValueOutput.createWithContext(
                    ProblemReporter.DISCARDING, serverLevel.registryAccess());
            armadillo.saveWithoutId(output);
            CompoundTag tag = output.buildResult();

            ItemStack filled = new ItemStack(ArmadilloBucketMod.ARMADILLO_BUCKET);
            filled.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, filled));
            level.playSound(null, armadillo.blockPosition(), SoundEvents.BUCKET_FILL,
                    SoundSource.NEUTRAL, 1.0F, 1.0F);
            armadillo.discard();
        }
        return InteractionResult.SUCCESS;
    }

    /** Gürteltier absetzen: Rechtsklick auf einen Block. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        Armadillo armadillo = EntityType.ARMADILLO.create(level, EntitySpawnReason.BUCKET);
        if (armadillo == null) return InteractionResult.FAIL;

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            armadillo.load(TagValueInput.create(
                    ProblemReporter.DISCARDING, level.registryAccess(), data.copyTag()));
        }

        Vec3 spot = Vec3.atBottomCenterOf(pos);
        float yaw = context.getPlayer() != null ? context.getPlayer().getYRot() + 180F : 0F;
        armadillo.snapTo(spot.x, spot.y, spot.z, yaw, 0F);
        armadillo.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(armadillo);

        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.NEUTRAL, 1.0F, 1.0F);

        Player player = context.getPlayer();
        if (player != null && !player.getAbilities().instabuild) {
            player.setItemInHand(context.getHand(), new ItemStack(Items.BUCKET));
        }
        return InteractionResult.SUCCESS;
    }
}
