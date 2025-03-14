package xyz.nucleoid.bedwars.custom;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;

public class BridgeEggEntity extends EggEntity {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final BlockState trailBlock;

    public BridgeEggEntity(World world, LivingEntity thrower, BlockState trailBlock) {
        super(world, thrower, new ItemStack(Items.EGG));
        this.trailBlock = trailBlock;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient()) {
            return;
        }

        var game = GameSpaceManager.get().byWorld(this.getWorld());
        if (game == null) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        BlockPos pos = this.getBlockPos().down();
        this.tryPlaceAt(pos);

        for (Direction direction : DIRECTIONS) {
            if (this.random.nextInt(3) != 0) {
                this.tryPlaceAt(pos.offset(direction));
            }
        }
    }

    private void tryPlaceAt(BlockPos pos) {
        if (this.getWorld().getBlockState(pos).isAir()) {
            this.getWorld().setBlockState(pos, this.trailBlock);
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        // ignore self-collisions
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            if (this.getWorld().getBlockState(((BlockHitResult) hitResult).getBlockPos()) == this.trailBlock) {
                return;
            }
        }

        super.onCollision(hitResult);
    }
}
