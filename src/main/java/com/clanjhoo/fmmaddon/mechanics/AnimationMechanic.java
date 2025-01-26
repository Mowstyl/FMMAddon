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

public class AnimationMechanic implements ITargetedEntitySkill {
    private final PlaceholderString animIdPh;

    public AnimationMechanic(MythicLineConfig mlc) {
        animIdPh = mlc.getPlaceholderString(new String[]{"a", "aid", "animation", "animid"}, null);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if (animIdPh == null)
            return SkillResult.INVALID_CONFIG;
        String animId = animIdPh.get(skillMetadata, abstractEntity);
        if (!(abstractEntity.getBukkitEntity() instanceof LivingEntity entity))
            return SkillResult.INVALID_TARGET;
        DynamicEntity dynamic = DynamicEntity.getDynamicEntity(entity);
        if (dynamic == null)
            return SkillResult.INVALID_TARGET;
        if (!dynamic.hasAnimation(animId))
            return SkillResult.INVALID_CONFIG;
        if (!dynamic.playAnimation(animId, false))
            return SkillResult.CONDITION_FAILED;
        return SkillResult.SUCCESS;
    }
}
