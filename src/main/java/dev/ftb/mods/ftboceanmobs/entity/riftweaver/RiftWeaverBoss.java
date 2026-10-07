package dev.ftb.mods.ftboceanmobs.entity.riftweaver;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.constant.DefaultAnimations;
import com.geckolib.util.GeckoLibUtil;
import com.mojang.serialization.Codec;
import dev.ftb.mods.ftboceanmobs.Config;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobs;
import dev.ftb.mods.ftboceanmobs.FTBOceanMobsTags;
import dev.ftb.mods.ftboceanmobs.entity.AnimatedHeadTracking;
import dev.ftb.mods.ftboceanmobs.entity.BaseRiftMob;
import dev.ftb.mods.ftboceanmobs.mobai.RandomAttackableTargetGoal;
import dev.ftb.mods.ftboceanmobs.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import javax.annotation.Nullable;

public class RiftWeaverBoss extends Monster implements GeoEntity, AnimatedHeadTracking {
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

    private static final Identifier FRENZY_DMG_ID = FTBOceanMobs.id("frenzy_damage");
    private static final AttributeModifier FRENZY_DMG = new AttributeModifier(
            FRENZY_DMG_ID, 6.0f, AttributeModifier.Operation.ADD_VALUE
    );
    public static final TargetingConditions NOT_RIFT_MOBS = TargetingConditions.DEFAULT.copy()
            .selector((e, level) -> !e.is(FTBOceanMobsTags.Entity.RIFT_MOBS));

    private static final float DAMAGE_CAP_ARMOR = 3f;
    private static final float DAMAGE_CAP_NO_ARMOR = 15f;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Mth.createInsecureUUID(random),
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
    private final Deque<RiftWeaverMode> phaseTransitions = new ArrayDeque<>();
    private BlockPos spawnPos = null;
    private int modeTicksRemaining = 0;
    BlockPos roamTarget;
    private long nextFireballTime = 0L;
    long nextMeleeSlash = 0L;
    long nextChainsAttack = 0L;
    private float armorDurability = 0f;
    SeismicSmasher seismicSmasher;
    ChainsEncaser chainsEncaser;
    private float accumulatedDmg = 0;

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
                .add(Attributes.MAX_HEALTH, 1000.0)
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
        flyingpathnavigation.getNodeEvaluator().setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new RandomAttackableTargetGoal<>(this,
                LivingEntity.class, 60,
                true, false,
                (e, level) -> !e.is(FTBOceanMobsTags.Entity.RIFT_MOBS))
        );
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        fightPhase = input.getIntOr("fightPhase", 0);
        currentMode = RiftWeaverModes.byNameElseHold(input.getStringOr("currentMode", ""));
        getEntityData().set(MODE, currentMode.getName());
        lastMode = RiftWeaverModes.byNameElseHold(input.getStringOr("lastMode", ""));
        queuedMode = input.getString("queuedMode").map(RiftWeaverModes::byNameElseHold).orElse(null);
        modeTicksRemaining = input.getIntOr("modeCounter", 0);
        spawnPos = input.read("spawnPos", BlockPos.CODEC).orElse(null);
        armorDurability = input.getFloatOr("armorDurability", 0f);
        setArmorActive(input.getBooleanOr("armorActive", false));
        setFrenzied(input.getBooleanOr("frenzied", false));
        accumulatedDmg = input.getFloatOr("accumulatedDmg", 0f);
        phaseTransitions.clear();
        var savedTransitions = input.read("phaseTransitions", Codec.STRING.listOf());
        if (savedTransitions.isPresent()) {
            savedTransitions.get().stream().map(RiftWeaverModes::byNameElseHold)
                    .filter(mode -> mode == RiftWeaverModes.TIDAL_SURGE || mode == RiftWeaverModes.RIFTCLAW_FRENZY)
                    .forEach(phaseTransitions::addLast);
        } else {
            // Recover phase transitions from saves made before the separate queue existed.
            if (queuedMode == RiftWeaverModes.TIDAL_SURGE) {
                phaseTransitions.addLast(queuedMode);
                queuedMode = null;
            }
            if (fightPhase == 3 && !isFrenzied() && currentMode != RiftWeaverModes.RIFTCLAW_FRENZY) {
                if (currentMode != RiftWeaverModes.TIDAL_SURGE && phaseTransitions.isEmpty()) {
                    phaseTransitions.addLast(RiftWeaverModes.TIDAL_SURGE);
                }
                phaseTransitions.addLast(RiftWeaverModes.RIFTCLAW_FRENZY);
            }
        }
        if (queuedMode == RiftWeaverModes.RIFTCLAW_FRENZY) queuedMode = null;
        if (isFrenzied() || currentMode == RiftWeaverModes.RIFTCLAW_FRENZY) {
            phaseTransitions.removeIf(mode -> mode == RiftWeaverModes.RIFTCLAW_FRENZY);
        }
        if (!lastMode.isIdleMode()) lastMode = RiftWeaverModes.HOLD_POSITION;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.putInt("fightPhase", fightPhase);
        output.store("phaseTransitions", Codec.STRING.listOf(), phaseTransitions.stream().map(RiftWeaverMode::getName).toList());
        output.putString("currentMode", currentMode.getName());
        output.putString("lastMode", lastMode.getName());
        if (queuedMode != null) output.putString("queuedMode", queuedMode.getName());
        if (modeTicksRemaining != 0) output.putInt("modeCounter", modeTicksRemaining);
        if (spawnPos != null) output.store("spawnPos", BlockPos.CODEC, spawnPos);
        if (armorDurability > 0f) output.putFloat("armorDurability", armorDurability);
        if (isArmorActive()) output.putBoolean("armorActive", true);
        if (isFrenzied()) output.putBoolean("frenzied", true);
        if (accumulatedDmg > 0f) output.putFloat("accumulatedDmg", accumulatedDmg);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        if (!level().isClientSide() && spawnPos == null) {
            spawnPos = blockPosition();
        }
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

        if (this.level().isClientSide() && !this.isSilent()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDER_DRAGON_FLAP,
                    this.getSoundSource(),
                    5.0F, 0.6F + this.random.nextFloat() * 0.3F,
                    false
            );
        }
    }

    @Override
    public float getHeadTrackingWeight() {
        return currentMode.isIdleMode() ? 1f : 0f;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("Attacking", 10, this::animState));
    }

    private PlayState animState(AnimationTest<RiftWeaverBoss> state) {
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
        if (deathTime >= 40 && level() instanceof ServerLevel serverLevel && !isRemoved()) {
            this.remove(Entity.RemovalReason.KILLED);
            this.gameEvent(GameEvent.ENTITY_DIE);
            level().getEntities(this, new AABB(spawnPos).inflate(Config.arenaRadius), e -> e instanceof BaseRiftMob)
                    .forEach(e -> e.kill(serverLevel));
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (fightPhase == -1) {
            playSound(ModSounds.RIFT_WEAVER_SUMMON.get());
            fightPhase = 0;
        }

        if (tickCount % 20 == 0 && isAlive()) {
            int nPlayers = countPlayersInArena();
            if (getHealth() < getMaxHealth()) {
                float regen = 0f;
                if (nPlayers == 0) {
                    // fast health regen if no player in arena (we allow creative mode players though)
                    regen = 50f;
                } else if (nPlayers > 1) {
                    regen = 5f + 3f * (nPlayers - 2);
                }
                if (regen != 0) {
                    setHealth(Math.min(getMaxHealth(), getHealth() + regen));
                }
            }
        }

        if (getEyePosition().y - 5 < level().getHeight(Heightmap.Types.WORLD_SURFACE, blockPosition().getX(), blockPosition().getZ())) {
            // boss sometimes clips too far into the ground
            snapTo(position().x, position().y + 2, position().z);
        }

        if (!hasHome()) {
            // spawnPos == null: newly spawned
            // non-null: loaded from NBT
            if (spawnPos == null) {
                spawnPos = blockPosition();
            }
            setHomeTo(spawnPos, Config.arenaRadius);
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

        if (currentMode.isIdleMode()) {
            if (!phaseTransitions.isEmpty()) {
                switchMode(phaseTransitions.removeFirst());
            } else if (queuedMode != null) {
                RiftWeaverMode nextMode = queuedMode;
                queuedMode = null;
                switchMode(nextMode);
            }
        }

        if (getTarget() != null && getTarget().isAlive()) {
            lookControl.setLookAt(getTarget());
            if (fightPhase >= 3 && tickCount > nextChainsAttack) {
                queueMode(RiftWeaverModes.CHAINS);
            } else if (accumulatedDmg > getMaxHealth() / 5 && queuedMode == null && isAlive()) {
                queueMode(RiftWeaverModes.REINFORCE);
                accumulatedDmg = 0;
            } else if (fightPhase >= 2 && random.nextInt(300) == 0) {
                queueMode(RiftWeaverModes.SEISMIC_SMASH);
            } else if (tickCount >= nextMeleeSlash) {
                queueMode(RiftWeaverModes.MELEE_SLASH);
            }
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
    protected AABB makeBoundingBox(Vec3 position) {
        return super.makeBoundingBox(position).move(0.0, 3.0, 0.0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (MODE == key) {
            currentMode = RiftWeaverModes.byNameElseHold(entityData.get(MODE));
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!source.is(Tags.DamageTypes.IS_TECHNICAL)) {
            amount = Math.min(amount, isArmorActive() && !source.is(Tags.DamageTypes.IS_MAGIC) ? DAMAGE_CAP_ARMOR : DAMAGE_CAP_NO_ARMOR);
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource damageSource, float damageAmount) {
        float prevHealth = getHealth();
        super.actuallyHurt(level, damageSource, damageAmount);
        float newHealthPct = getHealth() / getMaxHealth();

        if (armorDurability > 0f) {
            armorDurability = Math.max(0f, armorDurability - (prevHealth - getHealth()));
            if (armorDurability == 0f) {
                setArmorActive(false);
                removeEffect(MobEffects.REGENERATION);
            }
        }

        if (isAlive()) {
            if (newHealthPct < 0.25f) advanceFightPhase(3);
            else if (newHealthPct < 0.5f) advanceFightPhase(2);
            else if (newHealthPct < 0.75f) advanceFightPhase(1);
        }

        accumulatedDmg += damageAmount;
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
            for (int nextPhase = Math.max(1, fightPhase + 1); nextPhase <= phase; nextPhase++) {
                phaseTransitions.addLast(RiftWeaverModes.TIDAL_SURGE);
                if (nextPhase == 3) phaseTransitions.addLast(RiftWeaverModes.RIFTCLAW_FRENZY);
            }
            fightPhase = phase;
            armorDurability = 20f;
            if (getHealth() > 0f) {
                setArmorActive(true);
            }
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
            if (currentMode.isIdleMode()) lastMode = currentMode;
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
        playSound(active ? SoundEvents.ARMOR_EQUIP_NETHERITE.value() : SoundEvents.SHIELD_BREAK.value(), 5f, 1f);
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

    int countPlayersInArena() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return 0;
        }
        AABB aabb = new AABB(blockPosition()).inflate(Config.arenaRadius);
        return (int) serverLevel.getNearbyPlayers(TargetingConditions.forNonCombat(), this, aabb).stream()
                .filter(this::isInArena)
                .count();
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
            if (event.getProjectile().getOwner() instanceof RiftWeaverBoss boss
                    && event.getRayTraceResult() instanceof EntityHitResult hit
                    && (hit.getEntity() == boss || hit.getEntity() instanceof RiftWeaverPart part && part.getParent() == boss)) {
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
                cloud.setCustomParticle(ParticleTypes.SOUL_FIRE_FLAME);
            }
        }
    }
}
