package net.crystalixs.foo.paper;

import org.bukkit.plugin.java.JavaPlugin;

public class FooPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled!");
    }

}
