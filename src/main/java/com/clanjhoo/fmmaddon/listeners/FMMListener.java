package com.clanjhoo.fmmaddon.listeners;

import com.clanjhoo.fmmaddon.mechanics.AnimationMechanic;
import com.clanjhoo.fmmaddon.mechanics.ModelMechanic;
import com.clanjhoo.fmmaddon.targeters.PartTargeter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.targeters.ISkillTargeter;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicTargeterLoadEvent;
import io.lumine.mythic.core.logging.MythicLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FMMListener implements Listener {
    private final ConcurrentMap<String, Constructor<? extends ISkillMechanic>> mechanics = new ConcurrentHashMap<>(2);
    private final ConcurrentMap<String, Constructor<? extends ISkillTargeter>> targeters = new ConcurrentHashMap<>(1);

    public FMMListener() {
        try {
            mechanics.put("MODEL", ModelMechanic.class.getConstructor(MythicLineConfig.class));
            mechanics.put("ANIMATION", AnimationMechanic.class.getConstructor(MythicLineConfig.class));
            targeters.put("MODELPART", PartTargeter.class.getConstructor(MythicLineConfig.class));
        }
        catch (NoSuchMethodException e) {
            MythicLogger.error("Failed to add Free Minecraft Models mechanics");
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onMythicMechanic(MythicMechanicLoadEvent event) {
        String name = event.getMechanicName().toUpperCase();
        if (mechanics.containsKey(name)) {
            Constructor<? extends ISkillMechanic> constructor = mechanics.get(name);
            try {
                event.register(constructor.newInstance(event.getConfig()));
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                MythicLogger.error("Failed to construct mechanic {0}", name);
                ex.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onMythicMechanic(MythicTargeterLoadEvent event) {
        String name = event.getTargeterName().toUpperCase();
        if (targeters.containsKey(name)) {
            Constructor<? extends ISkillTargeter> constructor = targeters.get(name);
            try {
                event.register(constructor.newInstance(event.getConfig()));
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                MythicLogger.error("Failed to construct targeter {0}", name);
                ex.printStackTrace();
            }
        }
    }
}
