package mods.mechanicalgapfillers.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class FluidiserBlock extends Block implements EntityBlock {

    public static final IntegerProperty WORKING_STAGE = IntegerProperty.create("working_stage", 0, 4);
    // which direction is the block facing when placed.
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public FluidiserBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
        this.registerDefaultState(this.stateDefinition.any().setValue(WORKING_STAGE, 0));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // when placed, block faces user.
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(WORKING_STAGE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidiserBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (type != MGFBlocks.FLUIDISER_BLOCK_ENTITY.get()) {
            return null;
        }

        return (lvl, pos, st, be) -> {
            if (be instanceof FluidiserBlockEntity fluidiserBe) {
                if (lvl.isClientSide()) {
                    FluidiserBlockEntity.clientTick(lvl, pos, st, fluidiserBe);
                } else {
                    FluidiserBlockEntity.serverTick(lvl, pos, st, fluidiserBe);
                }
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FluidiserBlockEntity fluidiserBe) {
                // Safely opens the screen container on the server side
                player.openMenu(fluidiserBe, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.SUCCESS;
    }

    // TODO: might move this code to a per-tick function so it's more consistent
    @Override
    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        int stage = state.getValue(WORKING_STAGE);
        if (stage == 0) {
            return;
        }

        int numParticles = stage == 2 || stage == 4 ? 10
                : 2;

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.52D;
        double z = pos.getZ() + 0.5D;

        double speedX = (random.nextDouble() - 0.5D) * 0.005D;
        double speedY = 0.005D + (random.nextDouble() * 0.01D);
        double speedZ = (random.nextDouble() - 0.5D) * 0.005D;

        for(int i = 0; i < numParticles; i++) {
            level.addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    x, y, z,
                    speedX, speedY, speedZ
            );
        }

    }
}