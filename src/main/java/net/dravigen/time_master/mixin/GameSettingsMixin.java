package net.dravigen.time_master.mixin;

import net.dravigen.time_master.TimeMasterAddon;
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

    @Shadow public abstract void loadOptions();

    @Unique
    private void KeyMappings$addKeyBinds(){
        keyBindings = Arrays.copyOf(keyBindings,  keyBindings.length + 3);
        keyBindings[keyBindings.length - 1] = TimeMasterAddon.reset_time_speed_key;
        keyBindings[keyBindings.length - 2] = TimeMasterAddon.increase_time_speed_key;
        keyBindings[keyBindings.length - 3] = TimeMasterAddon.decrease_time_speed_key;
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
