package xyz.nucleoid.bedwars.custom;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;

public final class BwChorusFruitItem extends Item implements PolymerItem {
    private static final int ATTEMPTS = 32;
    private static final double MIN_DISTANCE_SQ = 6.0 * 6.0;

    public BwChorusFruitItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        if (!world.isClient) {
            stack.decrement(1);

            double originX = entity.getX();
            double originY = entity.getY();
            double originZ = entity.getZ();

            for (int i = 0; i < ATTEMPTS; ++i) {
                Vec3d target = generateTarget(entity);
                if (target == null) {
                    continue;
                }

                if (entity.hasVehicle()) {
                    entity.stopRiding();
                }

                if (entity.teleport(target.x, target.y, target.z, true)) {
                    SoundEvent sound = SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                    world.playSound(null, originX, originY, originZ, sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    entity.playSound(sound, 1.0F, 1.0F);
                    break;
                }
            }

            if (entity instanceof PlayerEntity) {
                ((PlayerEntity) entity).getItemCooldownManager().set(stack, 20);
            }

            return stack;
        }

        return stack;
    }

    @Nullable
    private static Vec3d generateTarget(LivingEntity entity) {
        Random random = entity.getRandom();

        double deltaX = (random.nextDouble() - 0.5) * 20.0;
        double deltaZ = (random.nextDouble() - 0.5) * 20.0;
        int deltaY = random.nextInt(16) - 8;

        if (deltaX * deltaX + deltaZ * deltaZ < MIN_DISTANCE_SQ) {
            return null;
        }

        return new Vec3d(
                entity.getX() + deltaX,
                MathHelper.clamp(entity.getY() + deltaY, entity.getWorld().getBottomY(), entity.getWorld().getTopYInclusive()),
                entity.getZ() + deltaZ
        );
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.CHORUS_FRUIT;
    }

    @Override
    public Text getName(ItemStack stack) {
        var name = stack.getComponentChanges().get(DataComponentTypes.ITEM_NAME);
        return name != null && name.isPresent() ? name.get() : Items.CHORUS_FRUIT.getName();
    }

    @Override
    @Nullable
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return null;
    }
}
