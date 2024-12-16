package dev.ftb.mods.ftboceanmobs.entity.riftweaver;

import software.bernie.geckolib.animation.RawAnimation;

public abstract class RiftWeaverMode {
    private final String name;
    private final int durationTicks;

    public RiftWeaverMode(String name, int durationTicks) {
        this.name = name;
        this.durationTicks = durationTicks;
    }

    public String getName() {
        return name;
    }

    int durationTicks() {
        return durationTicks;
    }

    void onModeStart(RiftWeaverBoss boss) {
    }

    void onModeEnd(RiftWeaverBoss boss) {
    }

    boolean isIdleMode() {
        return false;
    }

    abstract RawAnimation getAnimation();

    abstract void tickMode(RiftWeaverBoss boss, int modeTicksRemaining);

    @Override
    public String toString() {
        return name;
    }
}
