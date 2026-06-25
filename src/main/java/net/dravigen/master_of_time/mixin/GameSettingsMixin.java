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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;

@Mixin(GameSettings.class)
public abstract class GameSettingsMixin {
	
	@Shadow
	public KeyBinding[] keyBindings;
	
	@Shadow
	public abstract void loadOptions();
	
	@Shadow
	protected abstract float parseFloat(String par1Str);
	
	@Redirect(method = "loadOptions", at = @At(value = "INVOKE", target = "Ljava/lang/String;split(Ljava/lang/String;)[Ljava/lang/String;"))
	private String[] loadOptions(String instance, String regex) {
		String[] var3 = instance.split(regex);
		
		if (var3[0].equals("upSpeedValue")) {
			MasterOfTimeAddon.upSpeedValue = this.parseFloat(var3[1]);
		}
		if (var3[0].equals("downSpeedValue")) {
			MasterOfTimeAddon.downSpeedValue = this.parseFloat(var3[1]);
		}
		
		return var3;
	}
	
	@Inject(method = "saveOptions", at = @At(value = "INVOKE", target = "Ljava/io/PrintWriter;close()V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
	private void saveOptions(CallbackInfo ci, PrintWriter var1) {
		var1.println("upSpeedValue:" + MasterOfTimeAddon.upSpeedValue);
		var1.println("downSpeedValue:" + MasterOfTimeAddon.downSpeedValue);
	}
	
	
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
