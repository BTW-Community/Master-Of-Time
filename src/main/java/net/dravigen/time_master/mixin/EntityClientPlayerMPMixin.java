package net.dravigen.time_master.mixin;

import net.dravigen.time_master.TimeMasterAddon;
import net.dravigen.time_master.packet.PacketSender;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityClientPlayerMP.class)
public abstract class EntityClientPlayerMPMixin extends EntityPlayerSP {

    @Unique
    private boolean iKeyPressed = false;
    @Unique
    private boolean dKeyPressed = false;

    public EntityClientPlayerMPMixin(Minecraft par1Minecraft, World par2World, Session par3Session, int par4) {
        super(par1Minecraft, par2World, par3Session, par4);
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void keyBindInputUpdate(CallbackInfo ci) {
        if (this.capabilities.isCreativeMode) {
            if (this.mc.currentScreen == null) {
                if (TimeMasterAddon.reset_time_speed_key.isPressed()) {
                    PacketSender.sendClientToServerMessage("reset");
                }
                if (TimeMasterAddon.increase_time_speed_key.isPressed() && !iKeyPressed && !TimeMasterAddon.decrease_time_speed_key.isPressed()) {
                    iKeyPressed = true;
                    PacketSender.sendClientToServerMessage("increase");
                } else iKeyPressed = false;

                if (TimeMasterAddon.decrease_time_speed_key.isPressed() && !dKeyPressed) {
                    dKeyPressed = true;
                    PacketSender.sendClientToServerMessage("decrease");
                } else dKeyPressed = false;
            }
        }
    }
}
