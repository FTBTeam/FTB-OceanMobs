package dev.ftb.mods.ftboceanmobs.mobai;

/** Server tick timings measured against the authored strike clips at their playback speeds. */
public record MeleeAttackTiming(int impactTick, int durationTicks, int cooldownTicks, float animationSpeed) {
    public static final MeleeAttackTiming SLUDGE = new MeleeAttackTiming(8, 15, 24, 2f);
    public static final MeleeAttackTiming WINGED = new MeleeAttackTiming(9, 17, 30, 2.5f);
    public static final MeleeAttackTiming CRAIG = new MeleeAttackTiming(13, 25, 32, 1f);
    public static final MeleeAttackTiming MINOTAUR = new MeleeAttackTiming(15, 25, 32, 1f);
    public static final MeleeAttackTiming DEMON = new MeleeAttackTiming(16, 30, 38, 1f);
    public static final MeleeAttackTiming SHADOW_BEAST = new MeleeAttackTiming(12, 20, 26, 1f);

    // Start immediately so the impact pose matches the server tick.
    public static final int TRANSITION_TICKS = 0;

    public MeleeAttackTiming {
        if (impactTick <= 0 || impactTick >= durationTicks || cooldownTicks <= durationTicks || animationSpeed <= 0) {
            throw new IllegalArgumentException("Attack timing must include windup, recovery and a gap between swings");
        }
    }
}
