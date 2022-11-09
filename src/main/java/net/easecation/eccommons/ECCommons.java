package net.easecation.eccommons;

import cn.nukkit.plugin.PluginBase;

public class ECCommons extends PluginBase {

    private static ECCommons instance;

    public static ECCommons getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        getLogger().info("ECCommons enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ECCommons disabled!");
    }

}
