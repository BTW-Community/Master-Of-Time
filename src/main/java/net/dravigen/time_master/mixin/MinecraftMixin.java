package net.dravigen.time_master.mixin;

import net.dravigen.time_master.TimeMasterAddon;
import net.minecraft.src.Minecraft;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MinecraftMixin {

/*
    @Inject(method = "runGameLoop",at = @At("HEAD"),cancellable = true)
    private void freeze(CallbackInfo ci){
        if (TimeMasterAddon.worldSpeedModifier==0) ci.cancel();
    }*/
    @Redirect(method = "runGameLoop",at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z",ordinal = 0,opcode = Opcodes.GETFIELD))
    private boolean freezeWorld(Minecraft instance){
        if (TimeMasterAddon.worldSpeedModifier==0)return true;
        else return instance.getIsGamePaused();
    }
    @Redirect(method = "runGameLoop",at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z",ordinal = 1,opcode = Opcodes.GETFIELD))
    private boolean freezeSound(Minecraft instance){
        if (TimeMasterAddon.worldSpeedModifier==0)return true;
        else return instance.getIsGamePaused();
    }
/*
    @Redirect(method = "runTick",at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;theWorld:Lnet/minecraft/src/WorldClient;",ordinal = 3,opcode = Opcodes.GETFIELD))
    private WorldClient freezeWorld(Minecraft instance){
        if (TimeMasterAddon.worldSpeedModifier==0)return null;
        else return instance.theWorld;
    }
*/

    @Redirect(method = "runTick",at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z",ordinal = 3,opcode = Opcodes.GETFIELD))
    private boolean freezeGameRenderer(Minecraft instance){
        if (TimeMasterAddon.worldSpeedModifier==0)return true;
        else return instance.getIsGamePaused();
    }


    @Redirect(method = "runTick",at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z",ordinal = 4,opcode = Opcodes.GETFIELD))
    private boolean freezeLevelRenderer(Minecraft instance){
        if (TimeMasterAddon.worldSpeedModifier==0)return true;
        else return instance.getIsGamePaused();
    }
/*
    @Redirect(method = "runTick",at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z",ordinal = 5,opcode = Opcodes.GETFIELD))
    private boolean freezeLevel(Minecraft instance){
        if (TimeMasterAddon.worldSpeedModifier==0)return true;
        else return instance.getIsGamePaused();
    }*/

    @Redirect(method = "runTick",at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z",ordinal = 6,opcode = Opcodes.GETFIELD))
    private boolean freezeLevel2(Minecraft instance){
        if (TimeMasterAddon.worldSpeedModifier==0)return true;
        else return instance.getIsGamePaused();
    }

    @Redirect(method = "runTick",at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z",ordinal = 7,opcode = Opcodes.GETFIELD))
    private boolean freezeAnimateTick(Minecraft instance){
        if (TimeMasterAddon.worldSpeedModifier==0)return true;
        else return instance.getIsGamePaused();
    }


    @Redirect(method = "runTick",at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z",ordinal = 8,opcode = Opcodes.GETFIELD))
    private boolean freezeParticles(Minecraft instance){
        if (TimeMasterAddon.worldSpeedModifier==0)return true;
        else return instance.getIsGamePaused();
    }
}
