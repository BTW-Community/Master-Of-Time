package net.dravigen.master_of_time.mixin;

import net.dravigen.master_of_time.MasterOfTimeAddon;
import net.dravigen.master_of_time.packet.PacketHandlerC2S;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.Packet250CustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetServerHandler.class)
public class NetServerHandlerMixin {
	@Shadow
	public EntityPlayerMP playerEntity;
	
	@Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
	private void mot_onCustomPayloadC2S(Packet250CustomPayload packet, CallbackInfo ci) {
		if (packet.channel.equals(MasterOfTimeAddon.TMChannel.CLIENT_TO_SERVER_CHANNEL)) {
			PacketHandlerC2S.handle(packet, this.playerEntity);
			ci.cancel();
		}
	}
}
