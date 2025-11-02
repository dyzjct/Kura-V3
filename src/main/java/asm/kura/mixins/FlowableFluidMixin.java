package asm.kura.mixins;

import dev.m7thh4ck.net.mod.module.impl.movement.Velocity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

@Mixin(FlowableFluid.class)
public class FlowableFluidMixin {

    @Redirect(method = "getVelocity", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0))
    private boolean getVelocity_hasNext(Iterator<Direction> var9) {
        if (Velocity.INSTANCE.isEnabled() && Velocity.INSTANCE.getWaterPush()) {
            return false;
        }
        return var9.hasNext();
    }

}