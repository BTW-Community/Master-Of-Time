package net.dravigen.master_of_time.mixin;

import net.dravigen.master_of_time.packet.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

import static net.dravigen.master_of_time.MasterOfTimeAddon.*;

@Mixin(EntityClientPlayerMP.class)
public abstract class EntityClientPlayerMPMixin extends EntityPlayerSP {
	
	
	@Unique
	private boolean iKeyPressed = false;
	@Unique
	private boolean dKeyPressed = false;
	@Unique
	private float prevSpeed = 1;
	
	public EntityClientPlayerMPMixin(Minecraft par1Minecraft, World par2World, Session par3Session, int par4) {
		super(par1Minecraft, par2World, par3Session, par4);
	}
	
	@Inject(method = "onUpdate", at = @At("HEAD"))
	private void keyBindInputUpdate(CallbackInfo ci) throws IOException {
		if (step) {
			worldSpeedModifier = prevSpeed;
			step = false;
		}
		if (this.getData(PLAYER_OP) && this.mc.isSingleplayer()) {
			if (this.mc.currentScreen == null) {
				if (reset_time_speed_key.isPressed()) {
					PacketSender.sendClientToServerMessage("reset");
					
					if (MinecraftServer.getServer() != null) {
						worldSpeedModifier = 1;
					}
				}
				
				if (freeze_time_speed_key.isPressed() && MinecraftServer.getServer() != null) {
					worldSpeedModifier = 0;
					
					this.sendChatToPlayer(ChatMessageComponent.createFromText("The game is frozen"));
				}
				
				if (upSpeedKey.isPressed() && !iKeyPressed && !downSpeedKey.isPressed()) {
					iKeyPressed = true;
					if (worldSpeedModifier != 0) {
						PacketSender.sendClientToServerMessage("increase");
					}
					else {
						if (MinecraftServer.getServer() != null) {
							worldSpeedModifier = 1;
							PacketSender.sendClientToServerMessage("increase");
						}
					}
					
				}
				else iKeyPressed = false;
				
				if (downSpeedKey.isPressed() && !dKeyPressed) {
					dKeyPressed = true;
					if (worldSpeedModifier != 0) {
						PacketSender.sendClientToServerMessage("decrease");
					}
					else if (MinecraftServer.getServer() != null) {
						worldSpeedModifier = 1;
						PacketSender.sendClientToServerMessage("decrease");
					}
					
				}
				else dKeyPressed = false;
				
				if (step_time_key.isPressed() && worldSpeedModifier == 0 && MinecraftServer.getServer() != null) {
					prevSpeed = 0;
					worldSpeedModifier = 1;
					step = true;
					this.sendChatToPlayer(ChatMessageComponent.createFromText("Stepping 1 tick"));
				}
			}
		}
	}
}
