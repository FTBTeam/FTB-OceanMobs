package dev.ftb.mods.ftboceanmobs.mixin;

import dev.ftb.mods.ftboceanmobs.entity.TentacledHorror;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method="wantsToStopRiding", at = @At("HEAD"), cancellable = true)
    private void wantsToStopRiding(CallbackInfoReturnable<Boolean> cir) {
        //noinspection ConstantConditions
        if (TentacledHorror.isPlayerPassenger((Player)(Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
