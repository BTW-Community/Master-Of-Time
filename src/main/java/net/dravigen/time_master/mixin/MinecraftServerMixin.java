package net.dravigen.time_master.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static net.dravigen.time_master.TimeMasterAddon.*;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ICommandSender,
        Runnable,
        IPlayerUsage{

    @Shadow
    public WorldServer[] worldServers;

    @Shadow
    public abstract ServerConfigurationManager getConfigurationManager();

    @Unique
    long prevTime;


    @Inject(method = "tick", at = @At("HEAD"))
    private void tickHead(CallbackInfo ci) {
        tps = 1 / ((System.nanoTime() - prevTime) / 50000000d);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickTail(CallbackInfo ci) {
        prevTime = System.nanoTime();

    }
/*
    @Inject(method = "initialWorldChunkLoad",at = @At(value = "HEAD"))
    private void a(CallbackInfo ci) {
        new ConsoleListenerThread().start();
    }*/

    @Redirect(method = "sendTimerSpeedUpdate(F)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"), remap = false)
    private float redirectMax(float a, float b) {
        if (worldSpeedModifier<1) {
            return a;
        }
        return Math.max(a,b);
    }

    @ModifyConstant(method = "run", constant = @Constant(floatValue = 1.0F))
    private float below1value(float value) {
        if (worldSpeedModifier<1) {
            return Float.MIN_VALUE;
        }

        return value;
    }

    @Unique
    long pastTime = -1;
    @Unique
    List<Double> speedNumbers = new ArrayList<>();


    @Inject(method = "tick",at = @At("HEAD"),cancellable = true)
    private void freeze(CallbackInfo ci){
        if (worldSpeedModifier==0){
            ci.cancel();
        }
    }

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getMinSpeedModifier()F"), remap = false)
    private float setWorldSpeedServer(MinecraftServer instance) {
        float speedModifier = getMinSpeedModifier();
        if (worldSpeedModifier != 1F) {
            speedModifier = worldSpeedModifier;
        }
        if (currentSpeedTest) {
            if (pastTime == -1) {
                pastTime = System.currentTimeMillis();
            }
            long relativeTime = maxSpeedTest ? 25000 + pastTime : 10000 + pastTime;
            if (relativeTime > System.currentTimeMillis()) {
                if (pastTime + 2500 < System.currentTimeMillis()) {
                    speedNumbers.add(tps);
                }
            } else {
                double sum = 0;
                for (double tickSpeed : speedNumbers) {
                    sum += tickSpeed;
                }
                double result = sum / speedNumbers.size();

                if (maxSpeedTest) {
                    worldSpeedModifier = 1;
                    this.getConfigurationManager().sendPacketToAllPlayers(new Packet3Chat(ChatMessageComponent.createFromText("The game could run at max: " + String.format("%.3f", result) + "x (" + String.format("%.3f", result*20) + " t/s)")));
                } else {
                    this.getConfigurationManager().sendPacketToAllPlayers(new Packet3Chat(ChatMessageComponent.createFromText("The game currently run at: " + String.format("%.3f", result) + "x (" + String.format("%.3f", result*20) + " t/s)")));
                }
                currentSpeedTest = false;
                maxSpeedTest = false;
                pastTime = -1;
                speedNumbers.clear();
            }
        }
        return speedModifier;
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    private void resetWorldSpeedWhenLeaving(CallbackInfo ci) {
        worldSpeedModifier = 1;
    }

    @Unique
    private float getMinSpeedModifier() {
        float minSpeedModifier = Float.MAX_VALUE;

        for (WorldServer world : this.worldServers) {
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
