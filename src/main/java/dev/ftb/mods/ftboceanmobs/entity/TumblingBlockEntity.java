/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ftb.mods.ftboceanmobs.entity;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftboceanmobs.client.ClientUtils;
import dev.ftb.mods.ftboceanmobs.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;
import org.apache.commons.lang3.Validate;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

/**
 * A bit like a FallingBlockEntity but tumbles as it flies
 */
public class TumblingBlockEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<BlockPos> ORIGIN = SynchedEntityData.defineId(TumblingBlockEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<ItemStack> STATE_STACK = SynchedEntityData.defineId(TumblingBlockEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final GameProfile DEFAULT_FAKE_PROFILE = UUIDUtil.createOfflineProfile("[Tumbling Block]");

    private static final Vec3 Y_POS = new Vec3(0, 1, 0);

    public final Vector3f tumbleVec;  // used for rendering
    private HitBehaviour hitBehaviour = HitBehaviour.SHATTER;
    private boolean canDropItem = true;

    public TumblingBlockEntity(EntityType<TumblingBlockEntity> type, Level worldIn) {
        super(type, worldIn);
        this.tumbleVec = makeTumbleVec(worldIn, null);
    }

    public TumblingBlockEntity(Level worldIn, LivingEntity thrower, double x, double y, double z, @Nonnull ItemStack stack) {
        super(ModEntityTypes.TUMBLING_BLOCK.get(), worldIn);
        Validate.isTrue(!stack.isEmpty() && stack.getItem() instanceof BlockItem);

        setOwner(thrower);
        this.blocksBuilding = true;
        this.setPos(x, y + (double)((1.0F - this.getBbHeight()) / 2.0F), z);
        this.setDeltaMovement(0, 0, 0);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.tumbleVec = makeTumbleVec(worldIn, thrower);
        this.setOrigin(blockPosition());
        entityData.set(STATE_STACK, stack);
    }

    private Vector3f makeTumbleVec(Level world, LivingEntity thrower) {
        if (thrower != null) {
            return thrower.getLookAngle().cross(Y_POS).toVector3f();
        } else if (world != null && world.isClientSide) {
            return ClientUtils.getOptionalClientPlayer()
                    .map(p -> p.getLookAngle().cross(Y_POS).toVector3f())
                    .orElse(null);
        } else {
            return null;
        }
    }

    public TumblingBlockEntity setHitBehaviour(HitBehaviour hitBehaviour) {
        this.hitBehaviour = hitBehaviour;
        return this;
    }

    public TumblingBlockEntity setCanDropItem(boolean canDropItem) {
        this.canDropItem = canDropItem;
        return this;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ORIGIN, BlockPos.ZERO);
        builder.define(STATE_STACK, ItemStack.EMPTY);
    }

    @Override
    public void shootFromRotation(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
        // do nothing, since velocities etc. get set up in ItemLaunching#launchEntity()
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
    }

    public ItemStack getStack() {
        return entityData.get(STATE_STACK);
    }

    public BlockPos getOrigin()
    {
        return this.entityData.get(ORIGIN);
    }

    private void setOrigin(BlockPos pos) {
        entityData.set(ORIGIN, pos);
    }

    @Override
    public void tick() {
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        super.tick();  // handles nearly all the in-flight logic

        if (!level().isClientSide) {
            BlockPos pos = blockPosition();
            if (!onGround() && (tickCount > 100 && (pos.getY() < 1 || pos.getY() > 256) || tickCount > 600)) {
                dropAsItem();
                discard();
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!level().isClientSide) {
            discard();
            switch (hitBehaviour) {
                case PLACE_BLOCK -> {
                    if (result.getType() != HitResult.Type.BLOCK || !tryPlaceAsBlock((BlockHitResult) result)) {
                        dropAsItem();
                    }
                }
                case PLACE_BLOCK_ON_ENTITY -> {
                    if (result instanceof BlockHitResult bhr && !tryPlaceAsBlock(bhr)) {
                        dropAsItem();
                    } else if (result instanceof EntityHitResult ehr && !ehr.getEntity().noPhysics && !tryPlaceOnEntity(ehr)) {
                        shatter();
                    }
                }
                case DROP_ITEM -> dropAsItem();
                case SHATTER -> shatter();
            }
            level().getEntities(this, getBoundingBox().inflate(1.0), EntitySelector.LIVING_ENTITY_STILL_ALIVE)
                    .forEach(e -> e.hurt(level().damageSources().fallingBlock(this), 6f));
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean tryPlaceAsBlock(BlockHitResult brtr) {
        ItemStack stack = getStack();
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        BlockPos pos0 = brtr.getBlockPos();
        Direction face = brtr.getDirection();
        Player placer = getOwner() instanceof Player p ? p : getFakePlayer();
        BlockState state = level().getBlockState(pos0);
        BlockPlaceContext ctx = new BlockPlaceContext(new UseOnContext(placer, InteractionHand.MAIN_HAND, brtr));
        BlockPos pos = state.canBeReplaced(ctx) ? pos0 : pos0.relative(face);

        if (level().getBlockState(pos).canBeReplaced(ctx)) {
            BlockSnapshot snapshot = BlockSnapshot.create(level().dimension(), level(), pos);
            if (!EventHooks.onBlockPlace(placer, snapshot, face)) {
                InteractionResult res = blockItem.place(ctx);
                return res == InteractionResult.SUCCESS || res == InteractionResult.CONSUME;
            }
        }
        return false;
    }

    private boolean tryPlaceOnEntity(EntityHitResult ehr) {
        ItemStack stack = getStack();
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        AABB aabb = ehr.getEntity().getBoundingBox().inflate(1, 0, 1);
        Player placer = getOwner() instanceof Player p ? p : getFakePlayer();

        int placed = 0;
        for (BlockPos pos : BlockPos.randomBetweenClosed(level().random, 1,
                Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ),
                Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
            BlockSnapshot snapshot = BlockSnapshot.create(level().dimension(), level(), pos);
            if (!EventHooks.onBlockPlace(placer, snapshot, Direction.UP)) {
                level().setBlock(pos, blockItem.getBlock().defaultBlockState(), Block.UPDATE_ALL);
                placed++;
            }
        }

        return placed > 0;
    }

    private void dropAsItem() {
        if (canDropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            spawnAtLocation(getStack().copy(), 0.0F);
        } else {
            shatter();
        }
    }

    private void shatter() {
        if (getStack().getItem() instanceof BlockItem bi) {
            BlockState state = bi.getBlock().defaultBlockState();
            level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, blockPosition(), Block.getId(state));
        }
    }

    private Player getFakePlayer() {
        FakePlayer fakePlayer = FakePlayerFactory.get((ServerLevel) level(), DEFAULT_FAKE_PROFILE);
        fakePlayer.setPos(getX(), getY(), getZ());
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, getStack());
        return fakePlayer;
    }

    public enum HitBehaviour {
        PLACE_BLOCK,        // place as block, only if hitting a block
        PLACE_BLOCK_ON_ENTITY,  // always place as block, even if hitting an entity
        DROP_ITEM,          // drop as an item if possible
        SHATTER             // just shatter into a cloud of particles
    }
}
