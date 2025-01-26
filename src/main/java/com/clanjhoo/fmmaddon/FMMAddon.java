package com.clanjhoo.fmmaddon;

import com.clanjhoo.fmmaddon.compat.FMMSupport;
import com.clanjhoo.fmmaddon.listeners.FMMListener;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.logging.MythicLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

public class FMMAddon extends JavaPlugin {
    private static FMMAddon instance;
    private FMMSupport support;
    private FMMListener listener;

    public static @Nullable FMMAddon getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        MythicBukkit mm = MythicBukkit.inst();
        MythicLogger.log("Epa");
        support = new FMMSupport(mm);
        mm.getCompatibility().setModelEngine(Optional.of(support));
        listener = new FMMListener();
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {

    }
}
