package com.isacofff.clientbase.modules.features;

import com.isacofff.clientbase.Category;
import com.isacofff.clientbase.Client;
import com.isacofff.clientbase.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class HudInfoModule extends Module {

    public enum Info {
        WATERMARK,
        COORDINATES,
        FPS,
        SPEED,
        DIRECTION,
        BIOME,
        WORLD_TIME,
        ARMOR,
        ATTACK_COOLDOWN,
        HEALTH,
        HUNGER,
        EXPERIENCE,
        ACTIVE_MODULES
    }

    private final Info info;
    private int hudX = -1;
    private int hudY = -1;

    public HudInfoModule(String name, String description, Info info) {
        this(name, description, info, Category.Render);
    }

    public HudInfoModule(String name, String description, Info info, Category category) {
        super(name, description, category);
        this.info = info;
    }

    public Info getInfo() {
        return info;
    }

    public int getHudX() {
        return hudX;
    }

    public int getHudY() {
        return hudY;
    }

    public void setHudPosition(int x, int y) {
        hudX = x;
        hudY = y;
    }

    public String getDisplayText(Minecraft minecraft) {
        if (info == Info.WATERMARK) {
            return "EAGLER / REDLINE";
        }
        if (info == Info.FPS) {
            return "FPS  " + Minecraft.getDebugFPS();
        }
        if (info == Info.ACTIVE_MODULES) {
            int count = 0;
            for (Module module : Client.manager.getModules()) {
                if (module.isEnabled() && module != this) {
                    ++count;
                }
            }
            return "ACTIVE  " + count;
        }
        if (minecraft.player == null || minecraft.world == null) {
            return null;
        }

        switch (info) {
        case COORDINATES:
            return "XYZ  " + (int) Math.floor(minecraft.player.posX) + "  "
                    + (int) Math.floor(minecraft.player.posY) + "  " + (int) Math.floor(minecraft.player.posZ);
        case SPEED:
            double speed = Math.sqrt(minecraft.player.motionX * minecraft.player.motionX
                    + minecraft.player.motionZ * minecraft.player.motionZ) * 20.0;
            return "SPEED  " + (Math.round(speed * 100.0) / 100.0);
        case DIRECTION:
            return "FACING  " + minecraft.player.getHorizontalFacing().getName().toUpperCase();
        case BIOME:
            return minecraft.world.getBiome(new BlockPos(minecraft.player)).getBiomeName().toUpperCase();
        case WORLD_TIME:
            long dayTime = minecraft.world.getWorldTime() % 24000L;
            int hour = (int) ((dayTime / 1000L + 6L) % 24L);
            int minute = (int) (dayTime % 1000L * 60L / 1000L);
            return "TIME  " + (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute;
        case ARMOR:
            return "ARMOR  " + minecraft.player.getTotalArmorValue();
        case ATTACK_COOLDOWN:
            return "ATTACK  " + (int) (minecraft.player.getCooledAttackStrength(0.0F) * 100.0F) + "%";
        case HEALTH:
            return "HEALTH  " + (int) Math.ceil(minecraft.player.getHealth()) + "/"
                    + (int) Math.ceil(minecraft.player.getMaxHealth());
        case HUNGER:
            return "HUNGER  " + minecraft.player.getFoodStats().getFoodLevel();
        case EXPERIENCE:
            return "LEVEL  " + minecraft.player.experienceLevel;
        default:
            return null;
        }
    }
}
