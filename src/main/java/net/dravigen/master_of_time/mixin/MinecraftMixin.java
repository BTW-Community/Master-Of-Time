package net.dravigen.master_of_time.mixin;

import net.dravigen.master_of_time.MasterOfTimeAddon;
import net.minecraft.src.Minecraft;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	
	@Redirect(method = "runGameLoop", at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z", ordinal = 0, opcode = Opcodes.GETFIELD))
	private boolean freezeWorld(Minecraft instance) {
		if (MasterOfTimeAddon.worldSpeedModifier == 0) return true;
		else return instance.getIsGamePaused();
	}
	
	@Redirect(method = "runGameLoop", at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z", ordinal = 1, opcode = Opcodes.GETFIELD))
	private boolean freezeSound(Minecraft instance) {
		if (MasterOfTimeAddon.worldSpeedModifier == 0) return true;
		else return instance.getIsGamePaused();
	}
	
	@Redirect(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z", ordinal = 3, opcode = Opcodes.GETFIELD))
	private boolean freezeGameRenderer(Minecraft instance) {
		if (MasterOfTimeAddon.worldSpeedModifier == 0) return true;
		else return instance.getIsGamePaused();
	}
	
	@Redirect(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z", ordinal = 4, opcode = Opcodes.GETFIELD))
	private boolean freezeLevelRenderer(Minecraft instance) {
		if (MasterOfTimeAddon.worldSpeedModifier == 0) return true;
		else return instance.getIsGamePaused();
	}
	
	@Redirect(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z", ordinal = 7, opcode = Opcodes.GETFIELD))
	private boolean freezeAnimateTick(Minecraft instance) {
		if (MasterOfTimeAddon.worldSpeedModifier == 0) return true;
		else return instance.getIsGamePaused();
	}
	
	@Redirect(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/src/Minecraft;isGamePaused:Z", ordinal = 8, opcode = Opcodes.GETFIELD))
	private boolean freezeParticles(Minecraft instance) {
		if (MasterOfTimeAddon.worldSpeedModifier == 0) return true;
		else return instance.getIsGamePaused();
	}
}
