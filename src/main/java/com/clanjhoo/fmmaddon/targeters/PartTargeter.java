package com.clanjhoo.fmmaddon.targeters;

import com.magmaguy.freeminecraftmodels.customentity.DynamicEntity;
import com.magmaguy.freeminecraftmodels.dataconverter.BoneBlueprint;
import com.magmaguy.freeminecraftmodels.dataconverter.FileModelConverter;
import com.magmaguy.freeminecraftmodels.dataconverter.SkeletonBlueprint;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.placeholders.PlaceholderFloat;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.api.skills.targeters.ILocationTargeter;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.logging.MythicLogger;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;


public class PartTargeter implements ILocationTargeter {
    private final static int LOCAL_MODE = 0;
    private final static int GLOBAL_MODE = 1;
    private final static int MODEL_MODE = 2;

    private final PlaceholderString modelIdPh;
    private final PlaceholderString partIdPh;
    private final PlaceholderString offsetPh;
    private final boolean exactMatch;
    //private final boolean scale;
    private final PlaceholderFloat xPh;
    private final PlaceholderFloat yPh;
    private final PlaceholderFloat zPh;

    public PartTargeter(MythicLineConfig mlc) {
        modelIdPh = mlc.getPlaceholderString(new String[]{"m", "mid", "model", "modelid"}, null);
        partIdPh = mlc.getPlaceholderString(new String[]{"p", "pid", "part", "partid"}, null);
        offsetPh = mlc.getPlaceholderString(new String[]{"o", "off", "offset"}, "LOCAL");
        String coords = mlc.getString(new String[]{"location", "loc", "l", "coordinates", "c"}, null);
        PlaceholderFloat x, y, z;
        if (coords != null) {
            String[] split = coords.split(",");
            try {
                x = PlaceholderFloat.of(split[0]);
                y = PlaceholderFloat.of(split[1]);
                z = PlaceholderFloat.of(split[2]);
            } catch (Exception var5) {
                MythicLogger.error("The 'coordinates' attribute must be in the format c=x,y,z.");
                x = PlaceholderFloat.of("0");
                y = PlaceholderFloat.of("0");
                z = PlaceholderFloat.of("0");
            }
        } else {
            x = mlc.getPlaceholderFloat("x", 0F);
            y = mlc.getPlaceholderFloat("y", 0F);
            z = mlc.getPlaceholderFloat("z", 0F);
        }
        xPh = x;
        yPh = y;
        zPh = z;
        exactMatch = mlc.getBoolean(new String[]{"em", "exact", "match", "exactmatch"}, true);
        //scale = mlc.getBoolean(new String[]{"s", "sc", "scale"}, true);
    }

    private static Quaternionfc getRotationQuaternion(BoneBlueprint bone) {
        Vector3f rotVec = bone.getBlueprintOriginalBoneRotation();
        return new Quaternionf(rotVec.x, rotVec.y, rotVec.z, 0);
    }

    private Location getLocation(DynamicEntity dynamic, BoneBlueprint bone, Vector3f offset, int offsetMode) {
        //if (scale) {
        //    offset.mul(scale of the model);
        //}
        Vector3f pos = switch (offsetMode) {
            case LOCAL_MODE -> bone.getModelCenter()
                    .add(offset.rotate(getRotationQuaternion(bone)), new Vector3f())
                    .rotateY(0);  // Rotate YAW radians
            case GLOBAL_MODE -> bone.getModelCenter()
                    .rotateY(0, new Vector3f())  // Rotate YAW radians
                    .add(offset);
            case MODEL_MODE -> bone.getModelCenter()
                    .add(offset, new Vector3f())
                    .rotateY(0);  // Rotate YAW radians
            default -> throw new MatchException(null, null);
        };
        Location result = dynamic.getLocation().clone();
        Vector3f pivot = bone.getBlueprintModelPivot();
        result.setX(pivot.x + pos.x);
        result.setY(pivot.y + pos.y);
        result.setZ(pivot.z + pos.z);
        return result;
    }

    private Collection<AbstractLocation> getLocations(SkillCaster caster, String modelId, String partId, int offsetMode, Vector3f offset) {
        Collection<AbstractLocation> targets = new HashSet<>();
        FileModelConverter fileModelConverter = FileModelConverter.getConvertedFileModels().get(modelId);
        if (fileModelConverter == null || !(caster.getEntity() instanceof LivingEntity entity))
            return targets;
        DynamicEntity dynamic = DynamicEntity.getDynamicEntity(entity);
        if (dynamic == null)
            return targets;
        SkeletonBlueprint sbp = dynamic.getSkeletonBlueprint();
        if (sbp.getModelName().equalsIgnoreCase(modelId)) {
            if (exactMatch) {
                BoneBlueprint bbp = sbp.getBoneMap().get(partId);
                if (bbp != null)
                    targets.add(BukkitAdapter.adapt(getLocation(dynamic, bbp, offset, offsetMode)));
            }
            else {
                for (Map.Entry<String, BoneBlueprint> entry : sbp.getBoneMap().entrySet()) {
                    if (entry.getKey().contains(partId))
                        targets.add(BukkitAdapter.adapt(getLocation(dynamic, entry.getValue(), offset, offsetMode)));
                }
            }
        }
        return targets;
    }

    @Override
    public Collection<AbstractLocation> getLocations(SkillMetadata skillMetadata) {
        String modelId = null;
        if (modelIdPh != null) {
            modelId = modelIdPh.get(skillMetadata);
        }
        String partId = null;
        if (partIdPh != null) {
            partId = partIdPh.get(skillMetadata);
        }
        int offsetMode = LOCAL_MODE;
        if (offsetPh != null) {
            String offset = offsetPh.get(skillMetadata).toUpperCase();
            if (offset.equals("GLOBAL"))
                offsetMode = GLOBAL_MODE;
            else if (offset.equals("MODEL"))
                offsetMode = MODEL_MODE;
            else if (!offset.equals("LOCAL"))
                throw new IllegalArgumentException("Offset must be either LOCAL, GLOBAL or MODEL");
        }
        float x = 0;
        if (xPh != null) {
            x = xPh.get(skillMetadata);
        }
        float y = 0;
        if (yPh != null) {
            y = yPh.get(skillMetadata);
        }
        float z = 0;
        if (zPh != null) {
            z = zPh.get(skillMetadata);
        }
        return getLocations(skillMetadata.getCaster(), modelId, partId, offsetMode, new Vector3f(x, y, z));
    }
}
