package dev.ftb.mods.ftboceanmobs.entity.riftweaver;

import dev.ftb.mods.ftboceanmobs.Config;
import dev.ftb.mods.ftboceanmobs.registry.ModFluids;
import dev.ftb.mods.ftboceanmobs.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class RiftWeaverModes {
    private static final Map<String, RiftWeaverMode> MODES = new HashMap<>();

    public static RiftWeaverMode registerMode(RiftWeaverMode mode) {
        MODES.put(mode.getName(), mode);
        return mode;
    }

    public static Optional<RiftWeaverMode> byName(String name) {
        return Optional.ofNullable(MODES.get(name));
    }

    public static RiftWeaverMode byNameElseHold(String name) {
        return byName(name).orElse(HOLD_POSITION);
    }

    public static final RiftWeaverMode HOLD_POSITION = registerMode(new RiftWeaverMode("hold_position", 0) {
        @Override
        public void tickMode(RiftWeaverBoss boss, int modeTicksRemaining) {
            boss.roamTarget = null;

            if (boss.getSpawnPos() != null && boss.getSpawnPos().distToCenterSqr(boss.position()) > 9) {
                // return to anchor pos
                Vec3 anchor = Vec3.atCenterOf(boss.getSpawnPos());
                boss.getMoveControl().setWantedPosition(anchor.x, anchor.y, anchor.z, 1.5f);
            } else {
                // close enough to anchor, just sit here
                //  but with a chance to switch to roam mode
                if (boss.getRandom().nextInt(300) == 0) {
                    boss.switchMode(RiftWeaverModes.ROAM);
                }
            }
        }

        @Override
        boolean isIdleMode() {
            return true;
        }

        @Override
        RawAnimation getAnimation() {
            return null;
        }
    });

    public static final RiftWeaverMode ROAM = registerMode(new RiftWeaverMode("roam", 0) {
        @Override
        void tickMode(RiftWeaverBoss boss, int modeTicksRemaining) {
            if (boss.roamTarget == null || (boss.roamTarget.distSqr(boss.blockPosition()) <= 9 && boss.getRandom().nextInt(40) == 0)) {
                // pick a new random spot to roam to, or maybe just return to center
                if (boss.getRandom().nextInt(4) == 0) {
                    boss.switchMode(RiftWeaverModes.HOLD_POSITION);
                    return;
                }
                float angle = boss.getRandom().nextFloat() * Mth.TWO_PI;
                int x = Math.round(boss.getSpawnPos().getX() + Mth.sin(angle) * (boss.getRandom().nextInt(Config.arenaRadius - 5) + 3));
                int z = Math.round(boss.getSpawnPos().getZ() + Mth.cos(angle) * (boss.getRandom().nextInt(Config.arenaRadius - 5) + 3));
                int y = boss.level().getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1 + boss.getRandom().nextInt(RiftWeaverBoss.MAX_ROAM_HEIGHT);
                boss.roamTarget = new BlockPos(x, y, z);
            }
            Vec3 vec = Vec3.atCenterOf(boss.roamTarget);
            if (boss.roamTarget.distToCenterSqr(boss.position()) >= 9) {
                boss.getMoveControl().setWantedPosition(vec.x, vec.y, vec.z, 2.5f);
            }
        }

        @Override
        boolean isIdleMode() {
            return true;
        }

        @Override
        RawAnimation getAnimation() {
            return null;
        }
    });

    public static final RiftWeaverMode MELEE_SLASH = registerMode(new RiftWeaverMode("slash", 40) {
        @Override
        void tickMode(RiftWeaverBoss boss, int modeTicksRemaining) {
            var tgt = boss.getTarget();
            if (tgt != null && tgt.isAlive() && boss.distanceToSqr(tgt) > 64) {
                // surge toward target
                boss.getMoveControl().setWantedPosition(tgt.getX(), tgt.getY(), tgt.getZ(), 10f);
            }

            if (modeTicksRemaining == 5) {
                // hurt all targets in a wide area in front of the weaver
                for (LivingEntity e : boss.level().getNearbyEntities(LivingEntity.class, RiftWeaverBoss.NOT_RIFT_MOBS, boss, new AABB(boss.blockPosition()).inflate(10))) {
                    if (MiscUtil.isLookingAtMe(e, boss, 0.9)) {
                        boss.doHurtTarget(e);
                        if (boss.isFrenzied() && boss.getRandom().nextBoolean()) {
                            e.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 + boss.getRandom().nextInt(40), 2));
                        }
                    }
                }
            }
        }

        @Override
        void onModeEnd(RiftWeaverBoss boss) {
            boss.nextMeleeSlash = boss.tickCount + (boss.isFrenzied() ? boss.getRandom().nextInt(30) : 20 + boss.getRandom().nextInt(60));
        }

        @Override
        RawAnimation getAnimation() {
            return RiftWeaverBoss.SLASH_ANIMATION;
        }
    });

    public static final RiftWeaverMode TIDAL_SURGE = registerMode(new RiftWeaverMode("surge", 40) {
        @Override
        void tickMode(RiftWeaverBoss boss, int modeTicksRemaining) {
            Level level = boss.level();
            BlockPos origin = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, boss.blockPosition());

            // adjust TidalSurge.N_RINGS if the surge animation time ever gets changed
            if (modeTicksRemaining == TidalSurgeRings.N_RINGS + 1) {
                level.playSound(null, origin.above(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.HOSTILE, 2f, 0.5f);
            } else if (modeTicksRemaining <= TidalSurgeRings.N_RINGS) {
                int radius = TidalSurgeRings.N_RINGS - modeTicksRemaining;
                BlockState water = ModFluids.ABYSSAL_WATER_FLOWING.get().getFlowing(7, false).createLegacyBlock();
                TidalSurgeRings.getRingOffsets(radius).forEach(offset -> {
                    BlockPos pos = origin.above(boss.getRandom().nextInt(3) + 2).offset(offset);
                    if (level.getBlockState(pos).isEmpty() && boss.isInArena(pos)) {
                        level.setBlock(pos, water, Block.UPDATE_ALL);
                    }
                });
            }
        }

        @Override
        RawAnimation getAnimation() {
            return RiftWeaverBoss.SURGE_ANIMATION;
        }
    });

    public static final RiftWeaverMode SEISMIC_SMASH = registerMode(new RiftWeaverMode("smash", 40) {
        @Override
        void tickMode(RiftWeaverBoss boss, int modeTicksRemaining) {
            LivingEntity e = Objects.requireNonNullElse(boss.getTarget(), boss);
            double yGround = boss.level().getHeight(Heightmap.Types.OCEAN_FLOOR, e.getBlockX(), e.getBlockZ());
            boss.getMoveControl().setWantedPosition(e.getX(), yGround + 1, e.getZ(), 1f);

            if (modeTicksRemaining == 10) {
                boss.level().explode(boss, boss.getX(), boss.getY(), boss.getZ(), 2f, Level.ExplosionInteraction.MOB);
                boss.seismicSmasher = new SeismicSmasher(boss.level(), boss.blockPosition(),
                        Config.arenaRadius - 2, 5, boss::isInArena);
            }
        }

        @Override
        RawAnimation getAnimation() {
            return RiftWeaverBoss.SMASH_ANIMATION;
        }
    });

    public static final RiftWeaverMode RIFTCLAW_FRENZY = registerMode(new RiftWeaverMode("frenzy", 80) {
        @Override
        void tickMode(RiftWeaverBoss boss, int modeTicksRemaining) {
            if (modeTicksRemaining == 40) {
                boss.setFrenzied(true);
            }
        }

        @Override
        RawAnimation getAnimation() {
            return RiftWeaverBoss.FRENZY_ANIMATION;
        }
    });

    public static final RiftWeaverMode CHAINS = registerMode(new RiftWeaverMode("chains", 40) {
        @Override
        void tickMode(RiftWeaverBoss boss, int modeTicksRemaining) {
            if (modeTicksRemaining == 20 && boss.getTarget() != null) {
                boss.chainsEncaser = new ChainsEncaser(boss.getTarget(), 40);
            }
        }

        @Override
        void onModeEnd(RiftWeaverBoss boss) {
            boss.nextChainsAttack = boss.tickCount + 200 + boss.getRandom().nextInt(100);
        }

        @Override
        RawAnimation getAnimation() {
            return RiftWeaverBoss.CHAINS_ANIMATION;
        }
    });

    public static Stream<String> sortedNames() {
        return MODES.keySet().stream().sorted();
    }
}
