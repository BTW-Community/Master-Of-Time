package net.dravigen.time_master.mixin;

import net.dravigen.time_master.packet.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.dravigen.time_master.TimeMasterAddon.*;

@Mixin(EntityClientPlayerMP.class)
public abstract class EntityClientPlayerMPMixin extends EntityPlayerSP {


    @Unique
    private boolean iKeyPressed = false;
    @Unique
    private boolean dKeyPressed = false;

    public EntityClientPlayerMPMixin(Minecraft par1Minecraft, World par2World, Session par3Session, int par4) {
        super(par1Minecraft, par2World, par3Session, par4);
    }

    @Unique
    private float prevSpeed = 1;

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void keyBindInputUpdate(CallbackInfo ci) {
        if (step){
            worldSpeedModifier=prevSpeed;
            step=false;
        }
        if (this.capabilities.isCreativeMode) {
            if (this.mc.currentScreen == null) {
                if (reset_time_speed_key.isPressed()) {
                    PacketSender.sendClientToServerMessage("reset");
                    if (MinecraftServer.getServer()!=null) {
                        worldSpeedModifier = 1;
                    }
                }
                if (freeze_time_speed_key.isPressed()&& MinecraftServer.getServer()!=null) {
                    worldSpeedModifier = 0;
                    this.sendChatToPlayer(ChatMessageComponent.createFromText("The game is frozen"));

                }
                if (increase_time_speed_key.isPressed() && !iKeyPressed && !decrease_time_speed_key.isPressed()) {
                    iKeyPressed = true;
                    if (worldSpeedModifier!=0) PacketSender.sendClientToServerMessage("increase");
                    else this.sendChatToPlayer(ChatMessageComponent.createFromText("You need to unfreeze time first"));

                } else iKeyPressed = false;

                if (decrease_time_speed_key.isPressed() && !dKeyPressed) {
                    dKeyPressed = true;
                    if (worldSpeedModifier!=0) PacketSender.sendClientToServerMessage("decrease");
                    else this.sendChatToPlayer(ChatMessageComponent.createFromText("You need to unfreeze time first"));

                } else dKeyPressed = false;

                if (step_time_key.isPressed()&&worldSpeedModifier==0&& MinecraftServer.getServer()!=null) {
                    prevSpeed=0;
                    worldSpeedModifier=1;
                    step=true;
                    this.sendChatToPlayer(ChatMessageComponent.createFromText("Stepping 1 tick"));
                }
            }
        }
    }
}
