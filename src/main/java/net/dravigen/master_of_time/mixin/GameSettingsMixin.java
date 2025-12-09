package net.dravigen.master_of_time.mixin;

import net.dravigen.master_of_time.MasterOfTimeAddon;
import net.minecraft.src.GameSettings;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Arrays;

@Mixin(GameSettings.class)
public abstract class GameSettingsMixin {
	
	@Shadow
	public KeyBinding[] keyBindings;
	
	@Shadow
	public abstract void loadOptions();
	
	@Unique
	private void KeyMappings$addKeyBinds() {
		keyBindings = Arrays.copyOf(keyBindings, keyBindings.length + 5);
		keyBindings[keyBindings.length - 1] = MasterOfTimeAddon.reset_time_speed_key;
		keyBindings[keyBindings.length - 2] = MasterOfTimeAddon.upSpeedKey;
		keyBindings[keyBindings.length - 3] = MasterOfTimeAddon.downSpeedKey;
		keyBindings[keyBindings.length - 4] = MasterOfTimeAddon.freeze_time_speed_key;
		keyBindings[keyBindings.length - 5] = MasterOfTimeAddon.step_time_key;
		
		
	}
	
	@Inject(method = "<init>()V", at = @At(value = "TAIL"))
	private void KeyMapping$initTail(CallbackInfo ci) {
		KeyMappings$addKeyBinds();
	}
	
	@Inject(method = "<init>(Lnet/minecraft/src/Minecraft;Ljava/io/File;)V", at = @At(value = "TAIL"))
	private void KeyMapping$initTailLoadOpts(Minecraft par1Minecraft, File par2File, CallbackInfo ci) {
		KeyMappings$addKeyBinds();
		loadOptions();
	}
}
