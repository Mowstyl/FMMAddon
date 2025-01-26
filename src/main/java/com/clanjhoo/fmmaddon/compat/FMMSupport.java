package com.clanjhoo.fmmaddon.compat;

import com.magmaguy.freeminecraftmodels.customentity.DynamicEntity;
import com.magmaguy.freeminecraftmodels.dataconverter.BoneBlueprint;
import com.magmaguy.freeminecraftmodels.dataconverter.FileModelConverter;
import com.magmaguy.freeminecraftmodels.dataconverter.SkeletonBlueprint;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.compatibility.AbstractModelEngineSupport;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.mobs.model.MobModel;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class FMMSupport extends AbstractModelEngineSupport {
    public FMMSupport(MythicBukkit mythicBukkit) {
        super(mythicBukkit);
    }

    private @Nullable DynamicEntity getDynamicEntity(@Nullable AbstractEntity abstractEntity) {
        if (abstractEntity == null)
            return null;
        Entity raw = abstractEntity.getBukkitEntity();
        if (!(raw instanceof LivingEntity entity))
            return null;
        return DynamicEntity.getDynamicEntity(entity);
    }

    @Override
    public boolean isSubHitbox(UUID uuid) {
        return false;
    }

    @Override
    public boolean isBoundToSubHitbox(UUID uuid, UUID uuid1) {
        return false;
    }

    @Override
    public UUID getParentUUID(UUID uuid) {
        return uuid;
    }

    @Override
    public AbstractEntity getParent(AbstractEntity abstractEntity) {
        //DynamicEntity dynamic = getDynamicEntity(abstractEntity);
        //if (dynamic == null)
        //    return null;
        return abstractEntity;
    }

    @Override
    public boolean overlapsOOBB(BoundingBox boundingBox, AbstractEntity abstractEntity) {
        DynamicEntity dynamic = getDynamicEntity(abstractEntity);
        if (dynamic == null)
            return false;

        return dynamic.getHitbox().overlaps(boundingBox);
    }

    @Override
    public ModelConfig getBoneModel(String modelId, String boneId) throws IllegalArgumentException {
        FileModelConverter fileModelConverter = FileModelConverter.getConvertedFileModels().get(modelId);
        if (fileModelConverter == null)
            throw new IllegalArgumentException("Unknown model " + modelId);
        SkeletonBlueprint blueprint = fileModelConverter.getSkeletonBlueprint();
        if (blueprint == null)
            throw new IllegalArgumentException("Model " + modelId + " has no bones");
        BoneBlueprint bone = blueprint.getBoneMap().get(boneId);
        if (bone == null)
            throw new IllegalArgumentException("Unknown bone " + boneId);
        if (!bone.isDisplayModel())
            throw new IllegalArgumentException(boneId + " is not rendered");
        return new ModelConfig(bone.hashCode(), Material.ARMOR_STAND, false);
    }

    @Override
    public MobModel createMobModel(MythicMob mythicMob, MythicConfig mythicConfig) {
        return new FMMModel(mythicMob, mythicConfig);
    }

    @Override
    public void queuePostModelRegistration(Runnable runnable) {

    }

    @Override
    public void load(MythicBukkit mythicBukkit) {
        MythicLogger.log("Free Minecraft Models compatibility loaded.");
    }

    @Override
    public void unload() {

    }
}
