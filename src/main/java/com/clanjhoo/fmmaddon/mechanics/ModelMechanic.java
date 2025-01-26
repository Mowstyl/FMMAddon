package com.clanjhoo.fmmaddon.mechanics;

import com.clanjhoo.fmmaddon.compat.FMMModel;
import com.magmaguy.freeminecraftmodels.customentity.DynamicEntity;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.logging.MythicLogger;
import org.bukkit.entity.LivingEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ModelMechanic implements ITargetedEntitySkill {
    private final PlaceholderString modelIdPh;
    private final PlaceholderString nametagPh;
    private final boolean remove;
    private final boolean killOwner;

    public ModelMechanic(MythicLineConfig mlc) {
        modelIdPh = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, null);
        nametagPh = mlc.getPlaceholderString(new String[]{"n", "name", "nametag"}, null);
        remove = mlc.getBoolean(new String[]{"r", "remove"}, false);
        killOwner = mlc.getBoolean(new String[]{"ko", "killowner"}, false);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        String modelId = null;
        if (modelIdPh != null) {
            modelId = modelIdPh.get(skillMetadata, abstractEntity);
        }
        String nametag = null;
        if (nametagPh != null) {
            nametag = nametagPh.get(skillMetadata, abstractEntity);
        }
        return this.remove ? this.removeModel(abstractEntity, modelId, nametag) : this.updateModel(abstractEntity, modelId, nametag);
    }

    private @NonNull SkillResult removeModel(@NonNull AbstractEntity abstractEntity, @Nullable String modelId, @Nullable String nametag) {
        if (!(abstractEntity.getBukkitEntity() instanceof LivingEntity entity))
            return SkillResult.INVALID_TARGET;
        DynamicEntity dynamic = DynamicEntity.getDynamicEntity(entity);
        if (dynamic != null)
            dynamic.remove();
        if (killOwner)
            entity.remove();
        return SkillResult.SUCCESS;
    }

    private void setNametag(@NonNull DynamicEntity dynamic, @Nullable String nametag) {
        if (nametag != null) {
            if (!nametag.isEmpty()) {
                dynamic.setName(nametag);
                dynamic.setNameVisible(true);
            }
            else {
                dynamic.setNameVisible(false);
            }
        }
        else {
            dynamic.setName(dynamic.getLivingEntity().getCustomName());
            dynamic.setNameVisible(dynamic.getLivingEntity().isCustomNameVisible());
        }
    }

    private @NonNull SkillResult updateModel(@NonNull AbstractEntity abstractEntity, @Nullable String modelId, @Nullable String nametag) {
        if (modelId == null && nametag == null)
            return SkillResult.INVALID_CONFIG;
        if (modelId != null) {
            try {
                FMMModel.applyTask(modelId, abstractEntity, (dynamic) -> {
                    setNametag(dynamic, nametag);
                });
            } catch (IllegalArgumentException ex) {
                MythicLogger.log(modelId + " is not a valid model");
                return SkillResult.INVALID_CONFIG;
            }
        }
        else {
            if (!(abstractEntity.getBukkitEntity() instanceof LivingEntity entity))
                return SkillResult.INVALID_TARGET;
            DynamicEntity dynamic = DynamicEntity.getDynamicEntity(entity);
            if (dynamic == null)
                return SkillResult.INVALID_CONFIG;
            setNametag(dynamic, nametag);
        }

        return SkillResult.SUCCESS;
    }
}
