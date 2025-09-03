package net.dravigen.time_master.mixin;

import btw.network.packet.TimerSpeedPacket;
import net.dravigen.time_master.TimeMasterAddon;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Packet;
import net.minecraft.src.ServerConfigurationManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerConfigurationManager.class)
public class ServerConfigManagerMixin {
    @Shadow @Final public List playerEntityList;

    @Inject(method = "sendPacketToAllPlayers",at = @At(value = "HEAD"),cancellable = true)
    private void sendToAffectedPlayerOnly(Packet par1Packet, CallbackInfo ci){
        if (par1Packet instanceof TimerSpeedPacket){
            ci.cancel();
            for (Object o : this.playerEntityList) {
                EntityPlayerMP player = ((EntityPlayerMP) o);
                if (player.getData(TimeMasterAddon.TIME_AFFECTED)&&((TimerSpeedPacket) par1Packet).timerSpeed!=0){
                    player.playerNetServerHandler.sendPacketToPlayer(par1Packet);
                }else {
                    TimerSpeedPacket packet = new TimerSpeedPacket(1);
                    player.playerNetServerHandler.sendPacketToPlayer(packet);
                }
            }
        }
    }
}
