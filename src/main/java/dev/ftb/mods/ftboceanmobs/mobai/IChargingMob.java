package dev.ftb.mods.ftboceanmobs.mobai;

public interface IChargingMob {
    void setWarmingUp();
    void setActuallyCharging();
    void resetCharging();

    default boolean canBreakBlocksWhenCharging() {
        return true;
    }
}
