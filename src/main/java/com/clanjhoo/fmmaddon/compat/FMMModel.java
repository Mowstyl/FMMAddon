package com.clanjhoo.fmmaddon.compat;

import com.clanjhoo.fmmaddon.FMMAddon;
import com.magmaguy.freeminecraftmodels.customentity.DynamicEntity;
import com.magmaguy.freeminecraftmodels.dataconverter.FileModelConverter;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.core.mobs.model.MobModel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


public class FMMModel extends MobModel {
    private static final Map<UUID, Entity> boneMap = new ConcurrentHashMap<>();

    private final String modelId;
    private DynamicEntity modelEntity;

    public FMMModel(MythicMob baseMob, MythicConfig config) {
        super(baseMob, config);
        modelId = config.getString("Model");
    }

    public static LivingEntity getLivingEntity(@NonNull AbstractEntity abstractEntity) {
        Entity raw = abstractEntity.getBukkitEntity();
        if (!(raw instanceof LivingEntity entity))
            throw new IllegalArgumentException("FreeMinecraftModels only works with LivingEntity entities");
        return entity;
    }

    public static boolean modelExists(@NonNull String modelId) {
        FileModelConverter fileModelConverter = FileModelConverter.getConvertedFileModels().get(modelId);
        return fileModelConverter != null;
    }

    public static UUID getParentUUID(UUID child) {
        Entity parent = boneMap.get(child);
        if (parent != null)
            return parent.getUniqueId();
        return null;
    }

    public static Entity getParent(UUID child) {
        return boneMap.get(child);
    }

    private static void addBones(DynamicEntity dynamic) {
        dynamic.getNametagArmorstands().forEach(
                (armorStand) -> boneMap.put(
                        armorStand.getUniqueId(),
                        dynamic.getLivingEntity()
                )
        );
    }

    public static DynamicEntity apply(@NonNull String modelId, @NonNull AbstractEntity abstractEntity) {
        DynamicEntity modelEntity = DynamicEntity.create(modelId, getLivingEntity(abstractEntity));
        if (modelEntity == null)
            throw new IllegalArgumentException("Could not find model with id " + modelId);
        addBones(modelEntity);
        return modelEntity;
    }

    public static void applyTask(@NonNull String modelId, @NonNull AbstractEntity abstractEntity, @Nullable Consumer<DynamicEntity> consumer) {
        if (!modelExists(modelId))
            throw new IllegalArgumentException("Could not find model with id " + modelId);
        LivingEntity entity = getLivingEntity(abstractEntity);
        Bukkit.getScheduler().runTask(FMMAddon.getInstance(), () -> {
            DynamicEntity dynamic = DynamicEntity.create(modelId, entity);
            if (dynamic != null) {
                addBones(dynamic);
                if (consumer != null)
                    consumer.accept(dynamic);
            }
        });
    }

    @Override
    public void apply(AbstractEntity abstractEntity) {
        modelEntity = apply(modelId, abstractEntity);
    }

    public void playAnimationByName(String animationName) {
        modelEntity.playAnimation(animationName, false);
    }

    public void setName(String nametagName, boolean visible) {
        modelEntity.setName(nametagName);
        modelEntity.setNameVisible(visible);
    }

    public void setNameVisible(boolean visible) {
        modelEntity.setNameVisible(visible);
    }

    public void switchPhase() {
        modelEntity.stopCurrentAnimations();
    }
}
