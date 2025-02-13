package dev.ftb.mods.ftboceanmobs.entity.riftweaver;

import dev.ftb.mods.ftboceanmobs.Config;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobsTags;
import dev.ftb.mods.ftboceanmobs.mobai.RandomAttackableTargetGoal;
import dev.ftb.mods.ftboceanmobs.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Objects;

public class RiftWeaverBoss extends Monster implements GeoEntity {
    public static final int ARENA_HEIGHT = 32;
    public static final int MAX_ROAM_HEIGHT = 7;

    protected static final EntityDataAccessor<Boolean> HAS_ARMOR = SynchedEntityData.defineId(RiftWeaverBoss.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> FRENZIED = SynchedEntityData.defineId(RiftWeaverBoss.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<String> MODE = SynchedEntityData.defineId(RiftWeaverBoss.class, EntityDataSerializers.STRING);

    public static final RawAnimation SLASH_ANIMATION = RawAnimation.begin().thenPlay("attack.slash");
    public static final RawAnimation SURGE_ANIMATION = RawAnimation.begin().thenPlay("attack.tidal_surge");
    public static final RawAnimation SMASH_ANIMATION = RawAnimation.begin().thenPlay("attack.seismic_smash");
    public static final RawAnimation FRENZY_ANIMATION = RawAnimation.begin().thenPlay("attack.riftclaw_frenzy");
    public static final RawAnimation CHAINS_ANIMATION = RawAnimation.begin().thenPlay("attack.chains");

    private static final ResourceLocation FRENZY_DMG_ID = FTBOceanMobs.id("frenzy_damage");
    private static final AttributeModifier FRENZY_DMG = new AttributeModifier(
            FRENZY_DMG_ID, 6.0f, AttributeModifier.Operation.ADD_VALUE
    );
    public static final TargetingConditions NOT_RIFT_MOBS = TargetingConditions.DEFAULT.copy()
            .selector(e -> !e.getType().is(FTBOceanMobsTags.Entity.RIFT_MOBS));

    private static final float DAMAGE_CAP_ARMOR = 3f;
    private static final float DAMAGE_CAP_NO_ARMOR = 15f;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("entity.ftboceanmobs.rift_weaver"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_12
    ).setDarkenScreen(true);
    private final RiftWeaverPart[] subParts;
    private final RiftWeaverPart body;
    private final RiftWeaverPart head;
    private final RiftWeaverPart arm1;
    private final RiftWeaverPart arm2;

    private int fightPhase = -1; // -1..3 based on health (does not tick backward; -1 means newly spawned)
    private RiftWeaverMode currentMode = RiftWeaverModes.HOLD_POSITION;
    private RiftWeaverMode lastMode = RiftWeaverModes.HOLD_POSITION;
    private RiftWeaverMode queuedMode = null;  // next special mode to go into, only from hold/roam modes
    private BlockPos spawnPos = null;
    private int modeTicksRemaining = 0;
    BlockPos roamTarget;
    private long nextFireballTime = 0L;
    long nextMeleeSlash = 0L;
    long nextChainsAttack = 0L;
    private float armorDurability = 0f;
    SeismicSmasher seismicSmasher;
    ChainsEncaser chainsEncaser;

    public RiftWeaverBoss(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);

        moveControl = new FlyingMoveControl(this, 10, true);

        head = new RiftWeaverPart(this, 4.0f, 3.0f);
        body = new RiftWeaverPart(this, 3.5f, 9.0f);
        arm1 = new RiftWeaverPart(this, 2.5f, 9.5f);
        arm2 = new RiftWeaverPart(this, 2.5f, 9.5f);
        subParts = new RiftWeaverPart[] { head, body, arm1, arm2};

        noPhysics = true;
        setNoGravity(true);

        this.setId(ENTITY_COUNTER.getAndAdd(subParts.length + 1) + 1);
    }

    @Override
    public void setId(int id) {
        super.setId(id);

        for (int i = 0; i < this.subParts.length; i++) {
            this.subParts[i].setId(id + i + 1);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FLYING_SPEED, 0.9F)
                .add(Attributes.MOVEMENT_SPEED, 0.27F)
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.ARMOR, 4F)
                .add(Attributes.ARMOR_TOUGHNESS, 2F)
                .add(Attributes.FOLLOW_RANGE, 48F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75F)
                .add(Attributes.ATTACK_KNOCKBACK, 2.5F)
                .add(Attributes.ATTACK_DAMAGE, 12.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(HAS_ARMOR, false);
        builder.define(FRENZIED, false);
        builder.define(MODE, RiftWeaverModes.HOLD_POSITION.getName());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new RandomAttackableTargetGoal<>(this,
                LivingEntity.class, 60,
                true, false,
                e -> !e.getType().is(FTBOceanMobsTags.Entity.RIFT_MOBS))
        );
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        fightPhase = compound.getInt("fightPhase");
        currentMode = RiftWeaverModes.byNameElseHold(compound.getString("currentMode"));
        getEntityData().set(MODE, currentMode.getName());
        lastMode = RiftWeaverModes.byNameElseHold(compound.getString("lastMode"));
        queuedMode = compound.contains("queuedMode", Tag.TAG_STRING) ? RiftWeaverModes.byNameElseHold(compound.getString("queuedMode")) : null;
        modeTicksRemaining = compound.getInt("modeCounter");
        spawnPos = NbtUtils.readBlockPos(compound, "spawnPos").orElse(null);
        armorDurability = compound.getFloat("armorDurability");
        setArmorActive(compound.getBoolean("armorActive"));
        setFrenzied(compound.getBoolean("frenzied"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putInt("fightPhase", fightPhase);
        compound.putString("currentMode", currentMode.getName());
        compound.putString("lastMode", lastMode.getName());
        if (queuedMode != null) compound.putString("queuedMode", queuedMode.getName());
        if (modeTicksRemaining != 0) compound.putInt("modeCounter", modeTicksRemaining);
        if (spawnPos != null) compound.put("spawnPos", NbtUtils.writeBlockPos(spawnPos));
        if (armorDurability > 0f) compound.putFloat("armorDurability", armorDurability);
        if (isArmorActive()) compound.putBoolean("armorActive", true);
        if (isFrenzied()) compound.putBoolean("frenzied", true);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    protected boolean isFlapping() {
        return tickCount % 40 == 0;
    }

    @Override
    protected void onFlap() {
        super.onFlap();

        if (this.level().isClientSide && !this.isSilent()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDER_DRAGON_FLAP,
                    this.getSoundSource(),
                    5.0F, 0.6F + this.random.nextFloat() * 0.3F,
                    false
            );
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Attacking", 10, this::animState));
    }

    private PlayState animState(AnimationState<RiftWeaverBoss> state) {
        RawAnimation animation = Objects.requireNonNullElseGet(
                currentMode.getAnimation(),
                () -> state.isMoving() ? DefaultAnimations.FLY : DefaultAnimations.IDLE
        );
        return state.setAndContinue(animation);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void aiStep() {
        super.aiStep();

        processFlappingMovement();
        positionSubparts();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RIFT_WEAVER_DEATH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.RIFT_WEAVER_HURT.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.RIFT_WEAVER_AMBIENT.get();
    }

    private double fudge(double val, double amount) {
        return val + random.nextDouble() * amount - amount / 2.0;
    }

    private void positionSubparts() {
        Vec3[] prevPartPos = new Vec3[subParts.length];
        for (int i = 0; i < subParts.length; i++) {
            prevPartPos[i] = new Vec3(subParts[i].getX(), subParts[i].getY(), subParts[i].getZ());
        }

        updatePartPos(head, 0f, 14f, 0f);
        Vec3 velocity = getDeltaMovement();
        float avgVelocity = (float)(Math.abs(velocity.x) + Math.abs(velocity.z) / 2f);
        if (avgVelocity > 0.015f) {
            head.setPos(head.getX() + velocity.x * 16, head.getY() - 3.0, head.getZ() + velocity.z * 16);
        }

        updatePartPos(body, 0f, 5f, 0f);

        float yawRad = yBodyRot * Mth.DEG_TO_RAD;
        float xOff = Mth.cos(yawRad);
        float zOff = Mth.sin(yawRad);
        updatePartPos(arm1, xOff * 2.5f, 4.5f, zOff * 4.5f);
        updatePartPos(arm2, -xOff * 2.5f, 4.5f, -zOff * 4.5f);

        for (int i = 0; i < subParts.length; i++) {
            subParts[i].xo = prevPartPos[i].x;
            subParts[i].yo = prevPartPos[i].y;
            subParts[i].zo = prevPartPos[i].z;
            subParts[i].xOld = prevPartPos[i].x;
            subParts[i].yOld = prevPartPos[i].y;
            subParts[i].zOld = prevPartPos[i].z;
        }
    }

    private void updatePartPos(RiftWeaverPart part, float xOff, float yOff, float zOff) {
        part.setPos(getX() + xOff, getY() + yOff, getZ() + zOff);
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return subParts;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        if (deathTime < 40 && deathTime % 4 == 0) {
            double x = getBoundingBox().minX + random.nextDouble() * getBoundingBox().getXsize();
            double y = getBoundingBox().minY + random.nextDouble() * getBoundingBox().getYsize();
            double z = getBoundingBox().minZ + random.nextDouble() * getBoundingBox().getZsize();
            level().addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0.0, 0.0, 0.0);
        }
        if (deathTime >= 40 && !level().isClientSide() && !isRemoved()) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    protected void customServerAiStep() {
        if (fightPhase == -1) {
            playSound(ModSounds.RIFT_WEAVER_SUMMON.get());
            fightPhase = 0;
        }

        if (!hasRestriction()) {
            // anchorPos == null: newly spawned
            // non-null: loaded from NBT
            if (spawnPos == null) {
                spawnPos = blockPosition();
            }
            restrictTo(spawnPos, Config.arenaRadius);
        }

        if (modeTicksRemaining > 0) {
            if (--modeTicksRemaining == 0) {
                switchMode(lastMode);
            }
        }
        currentMode.tickMode(this, modeTicksRemaining);

        if (tickCount >= nextFireballTime) {
            shootFireball();
        }

        if (queuedMode != null && currentMode.isIdleMode()) {
            switchMode(queuedMode);
            queuedMode = null;
        }

        if (getTarget() != null && getTarget().isAlive()) {
            lookControl.setLookAt(getTarget());
            if (fightPhase >= 3 && tickCount > nextChainsAttack) {
                queueMode(RiftWeaverModes.CHAINS);
            } else if (fightPhase >= 2 && random.nextInt(300) == 0) {
                queueMode(RiftWeaverModes.SEISMIC_SMASH);
            } else if (tickCount >= nextMeleeSlash) {
                queueMode(RiftWeaverModes.MELEE_SLASH);
            }
        }

        if (fightPhase == 3 && !isFrenzied()) {
            forceQueueMode(RiftWeaverModes.RIFTCLAW_FRENZY);
        }

        if (armorDurability > 0) {
            addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 2));
        }

        if (seismicSmasher != null && !seismicSmasher.tick()) {
            seismicSmasher = null;
        }
        if (chainsEncaser != null && !chainsEncaser.tick(this)) {
            chainsEncaser = null;
        }

        bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    protected AABB makeBoundingBox() {
        return super.makeBoundingBox().move(0.0, 3.0, 0.0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (MODE == key) {
            currentMode = RiftWeaverModes.byNameElseHold(entityData.get(MODE));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!source.is(Tags.DamageTypes.IS_TECHNICAL)) {
            amount = Math.min(amount, isArmorActive() && !source.is(Tags.DamageTypes.IS_MAGIC) ? DAMAGE_CAP_ARMOR : DAMAGE_CAP_NO_ARMOR);
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float damageAmount) {
        float prevHealth = getHealth();
        float prevHealthPct = getHealth() / getMaxHealth();
        super.actuallyHurt(damageSource, damageAmount);
        float newHealthPct = getHealth() / getMaxHealth();

        if (armorDurability > 0f) {
            armorDurability = Math.max(0f, armorDurability - (prevHealth - getHealth()));
            if (armorDurability == 0f) {
                setArmorActive(false);
                removeEffect(MobEffects.REGENERATION);
            }
        }

        if (prevHealthPct >= 0.75f && newHealthPct < 0.75f) {
            advanceFightPhase(1);
        } else if (prevHealthPct >= 0.5f && newHealthPct < 0.5f) {
            advanceFightPhase(2);
        } else if (prevHealthPct >= 0.25f && newHealthPct < 0.25f) {
            advanceFightPhase(3);
        }
    }

    @Override
    protected float getFlyingSpeed() {
        return currentMode == RiftWeaverModes.MELEE_SLASH ? 0.15f : 0.05f;
    }

    @Override
    public void checkDespawn() {
        // do nothing, don't despawn naturally
    }

    private void advanceFightPhase(int phase) {
        if (fightPhase < phase) {
            fightPhase = phase;
            forceQueueMode(RiftWeaverModes.TIDAL_SURGE);
            armorDurability = 20f;
            setArmorActive(true);
        }
    }

    public void forceQueueMode(RiftWeaverMode newMode) {
        queuedMode = newMode;
    }

    public void queueMode(RiftWeaverMode newMode) {
        if (queuedMode == null) {
            queuedMode = newMode;
        }
    }

    public void switchMode(RiftWeaverMode newMode) {
        if (newMode != currentMode) {
            currentMode.onModeEnd(this);
            lastMode = currentMode;
            currentMode = newMode;
            modeTicksRemaining = newMode.durationTicks();
            entityData.set(MODE, currentMode.getName());
            currentMode.onModeStart(this);
        }
    }

    private void shootFireball() {
        if (getTarget() != null && getTarget().isAlive() && !(getTarget() instanceof Player)) {
            Vec3 launchPos = getEyePosition(1f).add(getViewVector(1.0F).normalize().scale(6.0));
            Vec3 delta = getTarget().position().subtract(launchPos);
            AbstractHurtingProjectile fireball = switch (fightPhase) {
                case 0 -> new SmallFireball(level(), this, delta.normalize());
                case 1,2 -> new LargeFireball(level(), this, delta.normalize().scale(2f), 0);
                default -> new DragonFireball(level(), this, delta.normalize());
            };
            fireball.setPos(launchPos);
            level().addFreshEntity(fireball);
            level().levelEvent(null, LevelEvent.SOUND_DRAGON_FIREBALL, blockPosition(), 0);

            long next = random.nextInt(isFrenzied() ? 5 : 3) == 0 ? 70 + random.nextInt(50) : 5;
            nextFireballTime = tickCount + next;
        }
    }

    public void setArmorActive(boolean active) {
        getEntityData().set(HAS_ARMOR, active);
        playSound(active ? SoundEvents.ARMOR_EQUIP_NETHERITE.value() : SoundEvents.SHIELD_BREAK, 5f, 1f);
    }

    public boolean isArmorActive() {
        return getEntityData().get(HAS_ARMOR);
    }

    public void setFrenzied(boolean frenzied) {
        getEntityData().set(FRENZIED, frenzied);
        AttributeInstance instance = Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE));
        instance.removeModifier(FRENZY_DMG_ID);
        if (frenzied) {
            instance.addTransientModifier(FRENZY_DMG);
        }
    }

    public boolean isFrenzied() {
        return getEntityData().get(FRENZIED);
    }

    public boolean isInArena(Entity entity) {
        return spawnPos.distToCenterSqr(entity.getX(), entity.getY(), entity.getZ()) < Config.arenaRadiusSq;
    }

    public boolean isInArena(BlockPos pos) {
        return pos.distSqr(spawnPos) < Config.arenaRadiusSq;
    }

    public BlockPos getSpawnPos() {
        return spawnPos;
    }

    @EventBusSubscriber
    public static class Listener {
        @SubscribeEvent
        public static void onProjectileImpact(ProjectileImpactEvent event) {
            // prevents the boss fireballing itself
            if (event.getEntity() instanceof RiftWeaverBoss && event.getProjectile().getOwner() == event.getEntity()) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onIncomingDamage(LivingIncomingDamageEvent event) {
            // prevents the boss doing indirect damage to itself (e.g. dragon fireball clouds)
            if (event.getEntity() instanceof RiftWeaverBoss && event.getSource().getEntity() == event.getEntity()) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onEntityJoin(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof AreaEffectCloud cloud && cloud.getOwner() instanceof RiftWeaverBoss) {
                cloud.setDuration(200);
                cloud.setParticle(ParticleTypes.SOUL_FIRE_FLAME);
            }
        }
    }
}
