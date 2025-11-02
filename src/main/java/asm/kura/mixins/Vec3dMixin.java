package asm.kura.mixins;

import dev.kura.net.asmimpl.IVec3d;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3d.class)
public class Vec3dMixin implements IVec3d {
    @Shadow
    @Final
    @Mutable
    public double x;
    @Shadow
    @Final
    @Mutable
    public double y;
    @Shadow
    @Final
    @Mutable
    public double z;

    @Override
    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void setXZ(double x, double z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void set(@NotNull Vec3i vec) {
        set(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public void set(@NotNull Vector3d vec) {
        set(vec.x, vec.y, vec.z);
    }
}
