package com.isacofff.clientbase.modules.features;

import com.isacofff.clientbase.Client;
import com.isacofff.clientbase.modules.Module;
import net.lax1dude.eaglercraft.EagRuntime;
import net.lax1dude.eaglercraft.Mouse;
import net.lax1dude.eaglercraft.EaglerInputStream;
import net.lax1dude.eaglercraft.EaglerOutputStream;
import net.lax1dude.eaglercraft.opengl.GlStateManager;
import net.lax1dude.eaglercraft.opengl.GuiShaderRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ClickGuiScreen;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ClientOverlay {

    private static HudInfoModule dragging;
    private static int dragOffsetX;
    private static int dragOffsetY;
    private static boolean positionsLoaded;

    private ClientOverlay() {
    }

    public static void render(Minecraft minecraft, FontRenderer fontRenderer, int screenWidth, int screenHeight) {
        if (!GuiShaderRenderer.isEnabled() || Client.manager == null || minecraft.player == null || minecraft.world == null
                || minecraft.gameSettings.hideGUI) {
            return;
        }

        List<HudInfoModule> visibleModules = new ArrayList<>();
        for (Module module : Client.manager.getModules()) {
            if (module instanceof HudInfoModule && module.isEnabled()) {
                visibleModules.add((HudInfoModule) module);
            }
        }
        loadPositions(visibleModules);

        int nextDefaultY = 8;
        for (HudInfoModule module : visibleModules) {
            if (module.getHudX() < 0 || module.getHudY() < 0) {
                module.setHudPosition(8, nextDefaultY);
            }
            nextDefaultY += getHeight(module) + 6;
        }

        updateDrag(minecraft, visibleModules, screenWidth, screenHeight);
        float time = (Minecraft.getSystemTime() % 100000L) / 1000.0F;
        for (HudInfoModule module : visibleModules) {
            if (module.getInfo() == HudInfoModule.Info.ARMOR) {
                drawArmor(minecraft, fontRenderer, module, screenWidth, screenHeight, time);
                continue;
            }
            String text = module.getDisplayText(minecraft);
            if (text != null) {
                int width = fontRenderer.getStringWidth(text) + 12;
                GuiShaderRenderer.drawPanel(module.getHudX(), module.getHudY(), width, 17, screenWidth,
                        screenHeight, 0xE10A0C10, 0xFFE1283D, 5.0F, time);
                fontRenderer.drawString(text, module.getHudX() + 6, module.getHudY() + 5, 0xFFF3F0F1);
            }
        }
    }

    private static void drawArmor(Minecraft minecraft, FontRenderer fontRenderer, HudInfoModule module,
            int screenWidth, int screenHeight, float time) {
        int x = module.getHudX();
        int y = module.getHudY();
        GuiShaderRenderer.drawPanel(x, y, 84, 42, screenWidth, screenHeight, 0xE10A0C10, 0xFFE1283D,
                6.0F, time);
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < minecraft.player.inventory.armorInventory.size(); ++i) {
            ItemStack stack = minecraft.player.inventory.armorInventory.get(i);
            int slotX = x + 4 + i * 19;
            if (!stack.func_190926_b()) {
                minecraft.getRenderItem().renderItemAndEffectIntoGUI(stack, slotX, y + 2);
                if (stack.isItemStackDamageable()) {
                    int remaining = Math.max(0, stack.getMaxDamage() - stack.getItemDamage());
                    int percent = remaining * 100 / stack.getMaxDamage();
                    String durability = percent + "%";
                    int color = percent > 60 ? 0xFF79E89A : (percent > 25 ? 0xFFFFC15D : 0xFFFF5266);
                    float scale = 0.65F;
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(slotX, y + 23, 0.0F);
                    GlStateManager.scale(scale, scale, 1.0F);
                        int labelX = (int) ((18.0F / scale - fontRenderer.getStringWidth(durability)) / 2.0F);
                        fontRenderer.drawString(durability, labelX, 0, color);
                    GlStateManager.popMatrix();
                }
            }
        }
        RenderHelper.disableStandardItemLighting();
    }

    private static int getWidth(HudInfoModule module, Minecraft minecraft, FontRenderer fontRenderer) {
        if (module.getInfo() == HudInfoModule.Info.ARMOR) {
            return 84;
        }
        String text = module.getDisplayText(minecraft);
        return text == null ? 40 : fontRenderer.getStringWidth(text) + 12;
    }

    private static int getHeight(HudInfoModule module) {
        return module.getInfo() == HudInfoModule.Info.ARMOR ? 42 : 17;
    }

    private static void updateDrag(Minecraft minecraft, List<HudInfoModule> modules, int screenWidth,
            int screenHeight) {
        boolean editing = minecraft.currentScreen instanceof ClickGuiScreen;
        boolean mouseDown = editing && Mouse.isButtonDown(0);
        int mouseX = Mouse.getX() * screenWidth / Math.max(1, minecraft.displayWidth);
        int mouseY = screenHeight - Mouse.getY() * screenHeight / Math.max(1, minecraft.displayHeight) - 1;

        if (mouseDown && dragging == null) {
            for (int i = modules.size() - 1; i >= 0; --i) {
                HudInfoModule module = modules.get(i);
                int width = getWidth(module, minecraft, minecraft.fontRendererObj);
                int height = getHeight(module);
                if (mouseX >= module.getHudX() && mouseX < module.getHudX() + width
                        && mouseY >= module.getHudY() && mouseY < module.getHudY() + height) {
                    dragging = module;
                    dragOffsetX = mouseX - module.getHudX();
                    dragOffsetY = mouseY - module.getHudY();
                    break;
                }
            }
        }

        if (mouseDown && dragging != null) {
            int width = getWidth(dragging, minecraft, minecraft.fontRendererObj);
            int height = getHeight(dragging);
            int x = Math.max(0, Math.min(screenWidth - width, mouseX - dragOffsetX));
            int y = Math.max(0, Math.min(screenHeight - height, mouseY - dragOffsetY));
            dragging.setHudPosition(x, y);
        } else if (!mouseDown && dragging != null) {
            savePositions(modules);
            dragging = null;
        }
    }

    private static void loadPositions(List<HudInfoModule> modules) {
        if (positionsLoaded) {
            return;
        }
        positionsLoaded = true;
        byte[] storage = EagRuntime.getStorage("redline_hud");
        if (storage == null) {
            return;
        }
        try {
            NBTTagCompound root = CompressedStreamTools.readCompressed(new EaglerInputStream(storage));
            for (HudInfoModule module : modules) {
                String key = getStorageKey(module);
                if (root.hasKey(key + "_x", 99) && root.hasKey(key + "_y", 99)) {
                    module.setHudPosition(root.getInteger(key + "_x"), root.getInteger(key + "_y"));
                }
            }
        } catch (IOException ex) {
        }
    }

    private static void savePositions(List<HudInfoModule> modules) {
        NBTTagCompound root = new NBTTagCompound();
        for (HudInfoModule module : modules) {
            String key = getStorageKey(module);
            root.setInteger(key + "_x", module.getHudX());
            root.setInteger(key + "_y", module.getHudY());
        }
        EaglerOutputStream output = new EaglerOutputStream();
        try {
            CompressedStreamTools.writeCompressed(root, output);
        } catch (IOException ex) {
            return;
        }
        EagRuntime.setStorage("redline_hud", output.toByteArray());
    }

    private static String getStorageKey(HudInfoModule module) {
        return "module_" + module.getName().replace(' ', '_');
    }
}
