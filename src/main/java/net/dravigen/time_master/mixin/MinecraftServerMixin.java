package net.dravigen.time_master.mixin;

import net.dravigen.time_master.TimeMasterAddon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow public WorldServer[] worldServers;
    @Shadow public abstract ServerConfigurationManager getConfigurationManager();

    @Unique
    private long prevTime=-1;
    @Unique
    private long prevTick=-1;
    @Unique
    private boolean iKeyPressed = false;
    @Unique
    private boolean dKeyPressed = false;



    @Redirect(method = "sendTimerSpeedUpdate(F)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"),remap = false)
    private float redirectMax(float a, float b) {
        return a;
    }

    @ModifyConstant(method = "run", constant = @Constant(floatValue = 1.0F))
    private float below1value(float value) { return Float.MIN_VALUE; }

    @Redirect(method = "run",at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getMinSpeedModifier()F"),remap = false)
    private float setWorldSpeed(MinecraftServer instance) {
        float speedModifier = this.getMinSpeedModifier();
        if (TimeMasterAddon.reset_time_speed_key.isPressed()) {
            TimeMasterAddon.worldSpeedModifier = 1;
            this.getConfigurationManager().sendPacketToAllPlayers(new Packet3Chat(ChatMessageComponent.createFromText("The world speed got reset")));

            return speedModifier;
        }
        if(TimeMasterAddon.increase_time_speed_key.isPressed()&&!iKeyPressed&&!TimeMasterAddon.decrease_time_speed_key.isPressed()){
            iKeyPressed=true;
            TimeMasterAddon.worldSpeedModifier = worldServers[0].getData(TimeMasterAddon.INCREASE_VALUE);
            this.getConfigurationManager().sendPacketToAllPlayers(new Packet3Chat(ChatMessageComponent.createFromText("World speed got set to " + TimeMasterAddon.worldSpeedModifier + "x")));
        }else iKeyPressed=false;
        if(TimeMasterAddon.decrease_time_speed_key.isPressed()&&!dKeyPressed){
            dKeyPressed=true;
            TimeMasterAddon.worldSpeedModifier = worldServers[0].getData(TimeMasterAddon.DECREASE_VALUE);
            this.getConfigurationManager().sendPacketToAllPlayers(new Packet3Chat(ChatMessageComponent.createFromText("World speed got set to " + TimeMasterAddon.worldSpeedModifier + "x")));
        }else dKeyPressed=false;
        if (TimeMasterAddon.worldSpeedModifier != 1F) {
            speedModifier = TimeMasterAddon.worldSpeedModifier;
        }
        if (TimeMasterAddon.currentSpeedTest) {
            long currentTime = System.currentTimeMillis();
            if (prevTime==-1){
                prevTime = currentTime;
            }
            long relativeTime= TimeMasterAddon.maxSpeedTest ? 25000-(currentTime-prevTime) : 6000-(currentTime-prevTime);
            if (((TimeMasterAddon.maxSpeedTest&& relativeTime<=20000)||(relativeTime<=5000))&&prevTick==-1) {
                prevTick = instance.getTickCounter();
            }
            if (relativeTime <= 0) {
                float result = (float) (instance.getTickCounter() - prevTick);
                result/= TimeMasterAddon.maxSpeedTest ? 400 : 100;
                if (TimeMasterAddon.maxSpeedTest){
                    TimeMasterAddon.worldSpeedModifier=1;
                    this.getConfigurationManager().sendPacketToAllPlayers(new Packet3Chat(ChatMessageComponent.createFromText("Your pc could handle at most: " + result + "x")));
                }else {
                    this.getConfigurationManager().sendPacketToAllPlayers(new Packet3Chat(ChatMessageComponent.createFromText("Your game currently run at: " + result + "x")));
                }
                TimeMasterAddon.currentSpeedTest = false;
                TimeMasterAddon.maxSpeedTest = false;
                prevTime = -1;
                prevTick = -1;
            }
        }
        return speedModifier;
    }

    @Inject(method = "stopServer",at = @At("HEAD"))
    private void resetWorldSpeedWhenLeaving(CallbackInfo ci){
        TimeMasterAddon.worldSpeedModifier=1;
    }

    @Unique
    private float getMinSpeedModifier() {
        float minSpeedModifier = Float.MAX_VALUE;

        for(WorldServer world : this.worldServers) {
            if (world != null && !world.playerEntities.isEmpty()) {
                float speedModifier = world.getMinSpeedModifier();
                if (speedModifier < minSpeedModifier) {
                    minSpeedModifier = speedModifier;
                }
            }
        }

        return minSpeedModifier == Float.MAX_VALUE ? 1.0F : minSpeedModifier;
    }

}
