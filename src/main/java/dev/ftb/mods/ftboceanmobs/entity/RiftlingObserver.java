package dev.ftb.mods.ftboceanmobs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RiftlingObserver extends Monster {
    public static final double GAZE_MIN_ANGLE = 0.25;

    private long lastGazeTime = 0L;

    protected static final EntityDataAccessor<Boolean> DATA_GAZE_WARMING_UP = SynchedEntityData.defineId(RiftlingObserver.class, EntityDataSerializers.BOOLEAN);

    public RiftlingObserver(EntityType<? extends RiftlingObserver> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.23F)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new ObserverGazeAttackGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));

        this.targetSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_GAZE_WARMING_UP, false);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (level().isClientSide) {
            if (isGazeWarmingUp()) {
                Vec3 eye = getEyePosition();
                Vec3 look = getLookAngle().normalize().scale(4);
                level().addParticle(ParticleTypes.ELECTRIC_SPARK, eye.x, eye.y, eye.z, look.x, look.y, look.z);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            boolean potion = source.getDirectEntity() instanceof ThrownPotion;
            if (!source.is(DamageTypeTags.IS_PROJECTILE) && !potion) {
                boolean wasHurt = super.hurt(source, amount);
                if (!this.level().isClientSide() && this.random.nextInt(10) != 0) {
                    this.teleport();
                }
                return wasHurt;
            } else {
                for (int i = 0; i < 64; i++) {
                    if (this.teleport()) {
                        return true;
                    }
                }
                return potion;
            }
        }
    }

    protected boolean teleport() {
        if (!this.level().isClientSide() && this.isAlive()) {
            double d0 = this.getX() + (this.random.nextDouble() - 0.5) * 16.0;
            double d1 = this.getY() + (double)(this.random.nextInt(16) - 8);
            double d2 = this.getZ() + (this.random.nextDouble() - 0.5) * 16.0;
            return this.teleport(d0, d1, d2);
        } else {
            return false;
        }
    }

    private boolean teleport(double x, double y, double z) {
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(x, y, z);

        while (mPos.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(mPos).blocksMotion()) {
            mPos.move(Direction.DOWN);
        }

        BlockState blockstate = this.level().getBlockState(mPos);
        if (blockstate.blocksMotion() && !blockstate.getFluidState().is(FluidTags.WATER)) {
            net.neoforged.neoforge.event.entity.EntityTeleportEvent.EnderEntity event = net.neoforged.neoforge.event.EventHooks.onEnderTeleport(this, x, y, z);
            if (event.isCanceled()) {
                return false;
            }
            Vec3 vec3 = this.position();
            boolean teleported = this.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true);
            if (teleported) {
                this.level().gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(this));
                if (!this.isSilent()) {
                    this.level().playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }

            return teleported;
        } else {
            return false;
        }
    }

    private void setGazeWarmingUp(boolean warmingUp) {
        getEntityData().set(DATA_GAZE_WARMING_UP, warmingUp);
    }

    public boolean isGazeWarmingUp() {
        return getEntityData().get(DATA_GAZE_WARMING_UP);
    }

    boolean isLookingAtMe(LivingEntity player) {
        Vec3 vec3 = player.getViewVector(1.0F).normalize();
        Vec3 vec31 = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        return d1 > 1.0 - GAZE_MIN_ANGLE / d0 && player.hasLineOfSight(this);
    }

    private static class ObserverGazeAttackGoal extends Goal {
        public static final int WARMUP_TIME = 20;

        private final RiftlingObserver observer;
        private int chargeTime;

        public ObserverGazeAttackGoal(RiftlingObserver observer) {
            this.observer = observer;

            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (chargeTime > 0 || observer.level().getGameTime() - observer.lastGazeTime < 60) {
                return false;
            }

            LivingEntity target = observer.getTarget();
            return target != null && target.isAlive() && observer.canAttack(target);
        }

        @Override
        public boolean canContinueToUse() {
            if (chargeTime > WARMUP_TIME) {
                return false;
            }
            LivingEntity target = observer.getTarget();
            return target != null && target.isAlive() && observer.canAttack(target) && observer.getSensing().hasLineOfSight(target);
        }

        @Override
        public void start() {
            chargeTime = 0;
            observer.setGazeWarmingUp(true);
            observer.getNavigation().stop();
        }

        @Override
        public void stop() {
            chargeTime = 0;
            observer.setGazeWarmingUp(false);
        }

        @Override
        public void tick() {
            if (++chargeTime == WARMUP_TIME) {
                observer.lastGazeTime = observer.level().getGameTime();

                observer.level().playSound(null, observer.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1f, 1f);

                LivingEntity target = observer.getTarget();
                if (target != null && target.isAlive() && observer.isLookingAtMe(target)) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 10));
                }
            }
        }
    }
}
