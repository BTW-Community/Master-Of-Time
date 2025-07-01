package net.dravigen.time_master.mixin;

import net.dravigen.time_master.TimeMasterAddon;
import net.dravigen.time_master.packet.PacketHandlerS2C;
import net.minecraft.src.NetClientHandler;
import net.minecraft.src.Packet250CustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetClientHandler.class)
public class NetClientHandlerMixin {

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void tm_onCustomPayloadS2C(Packet250CustomPayload packet, CallbackInfo ci) {
        if (packet.channel.equals(TimeMasterAddon.TMChannel.SERVER_TO_CLIENT_CHANNEL)) {
            PacketHandlerS2C.handle(packet);
            ci.cancel();
        }
    }
}
