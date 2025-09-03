package net.dravigen.time_master.mixin;

import net.dravigen.time_master.TimeMasterAddon;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow @Final public Profiler theProfiler;

    @Shadow public List loadedEntityList;

    @Shadow protected abstract void onEntityRemoved(Entity par1Entity);

    @Shadow public abstract Chunk getChunkFromChunkCoords(int par1, int par2);

    @Shadow protected abstract boolean chunkExists(int par1, int par2);

    @Shadow public abstract void updateEntity(Entity par1Entity);

    @Inject(method = "updateEntities",at = @At("HEAD"), cancellable = true)
    private void freeze(CallbackInfo ci){
        if (TimeMasterAddon.worldSpeedModifier==0){
            ci.cancel();
            int var13;
            int var3;
            Entity var2;
            int var1;
            this.theProfiler.startSection("entities");
            this.theProfiler.startSection("global");
            for (Object o : this.loadedEntityList) {
                Entity tempEntity = (Entity) o;
                if (!(tempEntity instanceof EntityPlayer))continue;
                if (tempEntity.isDead) continue;
                tempEntity.lastTickPosX = tempEntity.posX;
                tempEntity.lastTickPosY = tempEntity.posY;
                tempEntity.lastTickPosZ = tempEntity.posZ;
                tempEntity.prevRotationYaw = tempEntity.rotationYaw;
                tempEntity.prevRotationPitch = tempEntity.rotationPitch;
            }
            this.theProfiler.endStartSection("regular");
            for (var1 = 0; var1 < this.loadedEntityList.size(); ++var1) {
                var2 = (Entity)this.loadedEntityList.get(var1);
                if (!(var2 instanceof EntityPlayer))continue;
                if (var2.ridingEntity != null) {
                    if (!var2.ridingEntity.isDead && var2.ridingEntity.riddenByEntity == var2) continue;
                    var2.ridingEntity.riddenByEntity = null;
                    var2.ridingEntity = null;
                }
                this.theProfiler.startSection("tick");
                if (!var2.isDead) {
                    try {
                        this.updateEntity(var2);
                    } catch (Throwable var7) {
                        CrashReport var4 = CrashReport.makeCrashReport(var7, "Ticking entity");
                        CrashReportCategory var5 = var4.makeCategory("Entity being ticked");
                        var2.addEntityCrashInfo(var5);
                        throw new ReportedException(var4);
                    }
                }
                this.theProfiler.endSection();
                this.theProfiler.startSection("remove");
                if (var2.isDead) {
                    var3 = var2.chunkCoordX;
                    var13 = var2.chunkCoordZ;
                    if (var2.addedToChunk && this.chunkExists(var3, var13)) {
                        this.getChunkFromChunkCoords(var3, var13).removeEntity(var2);
                    }
                    this.loadedEntityList.remove(var1--);
                    this.onEntityRemoved(var2);
                }
                this.theProfiler.endSection();
            }
            this.theProfiler.endSection();
            this.theProfiler.endSection();
        }
    }
}
