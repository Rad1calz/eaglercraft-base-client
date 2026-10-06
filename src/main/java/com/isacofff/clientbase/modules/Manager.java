package com.isacofff.clientbase.modules;

import com.isacofff.clientbase.modules.features.ClickGui;
import com.isacofff.clientbase.modules.features.HudInfoModule;
import com.isacofff.clientbase.modules.features.MovementModules;
import com.isacofff.clientbase.modules.features.FullBright;
import com.isacofff.clientbase.Category;

import java.util.ArrayList;

public class Manager {

    public final ArrayList<Module> modules = new ArrayList<>();
    //Add the modules here for them to appear in the ClickGui
    public void init() {
        modules.add(new ClickGui());
        modules.add(new FullBright());
        modules.add(new MovementModules.AutoSprint());
        modules.add(new MovementModules.AutoJump());
        modules.add(new HudInfoModule("Watermark", "Shows the client name", HudInfoModule.Info.WATERMARK));
        modules.add(new HudInfoModule("Coordinates", "Shows your position", HudInfoModule.Info.COORDINATES));
        modules.add(new HudInfoModule("FPS", "Shows the current frame rate", HudInfoModule.Info.FPS));
        modules.add(new HudInfoModule("Speed", "Shows your movement speed", HudInfoModule.Info.SPEED));
        modules.add(new HudInfoModule("Direction", "Shows your facing direction", HudInfoModule.Info.DIRECTION));
        modules.add(new HudInfoModule("Biome", "Shows the current biome", HudInfoModule.Info.BIOME));
        modules.add(new HudInfoModule("World Time", "Shows the world time", HudInfoModule.Info.WORLD_TIME));
        modules.add(new HudInfoModule("Armor", "Shows your armor value", HudInfoModule.Info.ARMOR));
        modules.add(new HudInfoModule("Active Modules", "Shows the number of enabled modules",
            HudInfoModule.Info.ACTIVE_MODULES));
        modules.add(new HudInfoModule("Attack Cooldown", "Shows attack cooldown strength",
            HudInfoModule.Info.ATTACK_COOLDOWN, Category.Combat));
        modules.add(new HudInfoModule("Health", "Shows current and maximum health", HudInfoModule.Info.HEALTH,
            Category.Player));
        modules.add(new HudInfoModule("Hunger", "Shows current hunger", HudInfoModule.Info.HUNGER,
            Category.Player));
        modules.add(new HudInfoModule("Experience", "Shows current experience level",
            HudInfoModule.Info.EXPERIENCE, Category.Player));
    }

    public void onTick() {
        for (Module m : modules) {
            if (m.isEnabled()) {
                m.onUpdate();
            }
        }
    }

    public ArrayList<Module> getModules() {
        return modules;
    }

    public <T extends Module> T getModule(Class<T> classs) {

        for (Module m : modules) {
            if (classs.isInstance(m)) return classs.cast(m);
        }
        return null;
    }

    public Module getModuleByName(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public ArrayList<Module> getModulesByCategory(Category c) {
        ArrayList<Module> list = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == c) list.add(m);
        }
        return list;
    }
}
