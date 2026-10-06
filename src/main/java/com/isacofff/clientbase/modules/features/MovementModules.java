package com.isacofff.clientbase.modules.features;

import com.isacofff.clientbase.Category;
import com.isacofff.clientbase.modules.Module;
import net.minecraft.client.Minecraft;

public final class MovementModules {

    private MovementModules() {
    }

    public static class AutoSprint extends Module {
        public AutoSprint() {
            super("Auto Sprint", "Sprints while moving forward", Category.Movement);
        }

        @Override
        public void onUpdate() {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft.player != null) {
                minecraft.player.setSprinting(minecraft.player.moveForward > 0.0F && !minecraft.player.isSneaking());
            }
        }

        @Override
        public void onDisable() {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft.player != null) {
                minecraft.player.setSprinting(false);
            }
        }
    }

    public static class AutoJump extends Module {
        public AutoJump() {
            super("Auto Jump", "Jumps when moving forward and grounded", Category.Movement);
        }

        @Override
        public void onUpdate() {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft.player != null && minecraft.player.moveForward > 0.0F && minecraft.player.onGround
                    && !minecraft.player.isInWater()) {
                minecraft.player.jump();
            }
        }
    }
}
