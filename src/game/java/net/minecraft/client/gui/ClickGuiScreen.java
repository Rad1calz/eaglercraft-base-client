package net.minecraft.client.gui;

import com.isacofff.clientbase.Client;
import com.isacofff.clientbase.modules.Module;
import com.isacofff.clientbase.settings.Setting;
import com.isacofff.clientbase.settings.Setting.BooleanSetting;
import com.isacofff.clientbase.settings.Setting.ModeSetting;
import com.isacofff.clientbase.settings.Setting.NumberSetting;
import com.isacofff.clientbase.Category;
import net.lax1dude.eaglercraft.opengl.GuiShaderRenderer;
import net.lax1dude.eaglercraft.Keyboard;
import net.lax1dude.eaglercraft.KeyboardConstants;
import net.lax1dude.eaglercraft.Mouse;
import net.minecraft.client.Minecraft;
import com.isacofff.clientbase.modules.features.ClickGui;
import java.io.IOException;
import java.util.ArrayList;

public class ClickGuiScreen extends GuiScreen {

    //I won't go into too much detail here but if you need help contact me on discord, reach out to me.
    //Discord : isacofff

    private static final int PANEL_BG = 0xE10D0F13;
    private static final int PANEL_OUTLINE = 0xFFE1283D;
    private static final int MODULE_ENABLED = 0xE3291118;
    private static final int MODULE_DISABLED = 0xD915171B;
    private static final int TEXT_COLOR = 0xFFF4F0F1;
    private static final int ACCENT_COLOR = 0xFFFF465A;
    private static final int PANEL_MOVE_SPEED = 15;
    private static final int SLIDER_BG = 0xFF35151C;
    private static final int SLIDER_FILL = 0xFFEA2D43;
    private static final int SLIDER_KNOB = -1;

    private final ArrayList<Panel> panels = new ArrayList<>();

    public ClickGuiScreen() {
        int x = 12;
        int y = 38;

        for (Category cat : Category.values()) {
            //Cat :3
            panels.add(new Panel(cat, x, y));
            x += 126;
        }
    }



    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (Panel panel : panels) {
            panel.mouseDragged(mouseX, mouseY, clickedMouseButton);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }


    @Override
    public void updateScreen() {

        int movex = 0;
        int movey = 0;

        if (Keyboard.isKeyDown(KeyboardConstants.KEY_UP))
            movey = - PANEL_MOVE_SPEED;
        if (Keyboard.isKeyDown(KeyboardConstants.KEY_DOWN))
            movey =  PANEL_MOVE_SPEED;
        if (Keyboard.isKeyDown(KeyboardConstants.KEY_LEFT))
            movex = - PANEL_MOVE_SPEED;
        if (Keyboard.isKeyDown(KeyboardConstants.KEY_RIGHT))
            movex =  PANEL_MOVE_SPEED;

        if (movex != 0 || movey != 0) {
            for (Panel panel : panels) {
                panel.x += movex;
                panel.y += movey;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        this.drawDefaultBackground();

        GuiShaderRenderer.drawPanel(10, 8, this.width - 20, 22, this.width, this.height, 0xEE0C0E12,
            0xFFE1283D, 7.0F, (Minecraft.getSystemTime() % 100000L) / 1000.0F);
        fontRendererObj.drawString("EAGLER CLIENT  /  MODULES", 18, 15, TEXT_COLOR);

        for (Panel panel : panels) {
            panel.draw(mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        String descriptionn = null;

        for (int panelSize = panels.size() - 1; panelSize >= 0; panelSize--) {
            String description = panels.get(panelSize).getHoveredDescription(mouseX, mouseY);
            if (description != null) {
                descriptionn = description;
                break;
            }
        }

        if (descriptionn != null) {
            drawDescription(descriptionn, mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        for (Panel panel : panels) {
            panel.mouseClicked(mouseX, mouseY, mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == KeyboardConstants.KEY_ESCAPE) {
            ClickGui clickGui = Client.INSTANCE.manager.getModule(ClickGui.class);
            if (clickGui != null && clickGui.isEnabled()) {
                clickGui.toggle();
            } else {
                mc.displayGuiScreen(null);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void drawDescription(String text, int mouseX, int mouseY) {

        int padding = 5;
        int textWidth = fontRendererObj.getStringWidth(text);
        int boxX = mouseX + 8;
        int boxY = mouseY + 8;

        GuiShaderRenderer.drawPanel(boxX, boxY, textWidth + padding * 2, 12 + padding, this.width, this.height,
            0xF00A0C10, 0xFFE1283D, 4.0F, (Minecraft.getSystemTime() % 100000L) / 1000.0F);
        fontRendererObj.drawString(text, boxX + padding, boxY + padding, TEXT_COLOR);
    }

    private void drawOutline(int x1, int y1, int x2, int y2, int color) {
        drawRect(x1, y1, x2, y1 + 1, color);
        drawRect(x1, y2 - 1, x2, y2, color);
        drawRect(x1, y1, x1 + 1, y2, color);
        drawRect(x2 - 1, y1, x2, y2, color);
    }


    public class Panel {
        public Category category;
        public int x, y, width = 116, height = 22;
        public boolean dragging = false;
        public int dragX, dragY;
        public boolean open = true;
        private NumberSetting draggingSlider = null;
        private int sliderX = 0;
        private int sliderWidth = 0;
        public Panel(Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
        }

        private boolean hasSettings(Module module) {
            return module.getSettings() != null && !module.getSettings().isEmpty();
        }

        public String getHoveredDescription(int mouseX, int mouseY) {
            if (!open) return null;

            int offset = height;

            for (Module module : Client.INSTANCE.manager.getModulesByCategory(category)) {

                if (isHover(mouseX, mouseY, x, y + offset, width, 14)) {
                    return module.getDescription();
                }
                offset += 14;

                if (module.open && hasSettings(module)) {
                    for (Setting<?> setting : module.getSettings()) {
                        if (isHover(mouseX, mouseY, x, y + offset, width, 14)) {
                            return setting.getName();
                        }
                        offset += 14;
                    }
                }
            }

            return null;
        }

        public void draw(int mouseX, int mouseY) {

            if (!Mouse.isButtonDown(0)) {
                dragging = false;
                draggingSlider = null;
            }
            if (dragging) {
                this.x = mouseX - dragX;
                this.y = mouseY - dragY;
            }

            int panelHeight = open ? getTotalHeight() : height;
            GuiShaderRenderer.drawPanel(x, y, width, panelHeight, ClickGuiScreen.this.width,
                    ClickGuiScreen.this.height, PANEL_BG, PANEL_OUTLINE, 7.0F,
                    (Minecraft.getSystemTime() % 100000L) / 1000.0F);
            GuiShaderRenderer.drawPanel(x, y, width, height, ClickGuiScreen.this.width,
                    ClickGuiScreen.this.height, 0xF71A0E13, PANEL_OUTLINE, 6.0F,
                    (Minecraft.getSystemTime() % 100000L) / 1000.0F);

            fontRendererObj.drawString(category.name(), x + 4, y + 4, TEXT_COLOR);

            if (open) {

                int offset = height;

                for (Module module : Client.INSTANCE.manager.getModulesByCategory(category)) {

                        GuiShaderRenderer.drawPanel(x + 2, y + offset, width - 4, 13, ClickGuiScreen.this.width,
                            ClickGuiScreen.this.height, module.isEnabled() ? MODULE_ENABLED : MODULE_DISABLED,
                            module.isEnabled() ? PANEL_OUTLINE : 0xFF55575D, 4.0F,
                            (Minecraft.getSystemTime() % 100000L) / 1000.0F);

                    String symbol = module.open ? "-" : "+";
                    fontRendererObj.drawString(symbol, x + width - 10, y + offset + 4, ACCENT_COLOR);
                    fontRendererObj.drawString(module.getName(), x + 4, y + offset + 4, TEXT_COLOR);

                    offset += 14;

                    if (module.open && hasSettings(module)) {
                        for (Setting<?> setting : module.getSettings()) {

                                GuiShaderRenderer.drawPanel(x + 3, y + offset, width - 6, 13,
                                    ClickGuiScreen.this.width, ClickGuiScreen.this.height, MODULE_DISABLED,
                                    0xFF76202D, 4.0F, (Minecraft.getSystemTime() % 100000L) / 1000.0F);
                            drawSetting(setting, x, y + offset);

                            offset += 14;
                        }
                    }
                }
            }
        }

        private void drawSetting(Setting<?> s, int x, int y) {

            if (s instanceof BooleanSetting bs) {
                fontRendererObj.drawString(bs.getName() + " : " + bs.getValue(), x + 4, y + 4, TEXT_COLOR);
                return;
            }

            if (s instanceof ModeSetting ms) {
                fontRendererObj.drawString(ms.getName() + " : " + ms.getValue() + " ...", x + 4, y + 4, TEXT_COLOR);
                return;
            }

            if (s instanceof NumberSetting number) {
                fontRendererObj.drawString(number.getName() + " : " + number.getValue(), x + 4, y + 4, TEXT_COLOR);
                int barX = x + 4;
                int barY = y + 12;
                int barW = width - 8;
                int barH = 2;
                drawRect(barX, barY, barX + barW, barY + barH, SLIDER_BG);
                double percent = (number.getValue() - number.getMin()) / (number.getMax() - number.getMin());
                int fillW = (int)(percent * barW);
                drawRect(barX, barY, barX + fillW, barY + barH, SLIDER_FILL);
                int knobX = barX + fillW;
                drawRect(knobX - 1, barY, knobX + 1, barY + barH, SLIDER_KNOB);
            }
        }

        public void mouseDragged(int mouseX, int mouseY, int mouseButton) {
            if (draggingSlider != null && mouseButton == 0) {
                double percent = (mouseX - sliderX) / (double) sliderWidth;
                percent = Math.max(0.0, Math.min(1.0, percent));
                double rawValue = draggingSlider.getMin() + percent * (draggingSlider.getMax() - draggingSlider.getMin());
                double increment = draggingSlider.getIncrement();
                if (increment > 0) {
                    double steps = (rawValue - draggingSlider.getMin()) / increment;
                    double snapped = draggingSlider.getMin() + Math.round(steps) * increment;
                    snapped = Math.round(snapped * 10000.0) / 10000.0;
                    snapped = Math.max(draggingSlider.getMin(), Math.min(draggingSlider.getMax(), snapped));
                    draggingSlider.setValue(snapped);
                } else {
                    draggingSlider.setValue(Math.max(draggingSlider.getMin(), Math.min(draggingSlider.getMax(), rawValue)));
                }
            }

            if (dragging && mouseButton == 0) {
                this.x = mouseX - dragX;
                this.y = mouseY - dragY;
            }
        }

        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            if (isHover(mouseX, mouseY, x, y, width, height)) {

                if (mouseButton == 0) {
                    dragging = true;
                    dragX = mouseX - x;
                    dragY = mouseY - y;
                }

                if (mouseButton == 1) {
                    dragging = false;
                    open = !open;
                }

                return;
            }

            if (open) {

                int offset = height;

                for (Module module : Client.INSTANCE.manager.getModulesByCategory(category)) {

                    if (isHover(mouseX, mouseY, x, y + offset, width, 14)) {

                        int gearSize = 12;
                        int gearX = x + width - gearSize - 4;
                        int gearY = y + offset + 1;

                        boolean overGear = mouseX >= gearX && mouseX <= gearX + gearSize && mouseY >= gearY && mouseY <= gearY + gearSize;

                        if (overGear && mouseButton == 0) {
                            module.open = !module.open;
                            return;
                        }

                        if (mouseButton == 0) {
                            module.toggle();
                        }

                        if (mouseButton == 1 || (hasSettings(module) && mouseX >= x + width - 12)) {
                            module.open = !module.open;
                        }

                        return;
                    }

                    offset += 14;

                    if (module.open && hasSettings(module)) {
                        for (Setting<?> setting : module.getSettings()) {

                            if (isHover(mouseX, mouseY, x, y + offset, width, 14)) {

                                if (setting instanceof BooleanSetting && mouseButton == 0) {
                                    ((BooleanSetting) setting).toggle();
                                }

                                if (setting instanceof ModeSetting && mouseButton == 0) {
                                    ((ModeSetting) setting).cycle();
                                }

                                if (setting instanceof NumberSetting && mouseButton == 0) {
                                    draggingSlider = (NumberSetting) setting;
                                    sliderX = x;
                                    sliderWidth = width;
                                    double snapped = getSnapped(mouseX);
                                    draggingSlider.setValue(snapped);
                                }

                                return;
                            }

                            offset += 14;
                        }
                    }
                }
            }
        }

        private double getSnapped(int mouseX) {
            double percent = (mouseX - x) / (double) width;
            percent = Math.max(0.0, Math.min(1.0, percent));
            double rawValue = draggingSlider.getMin() + percent * (draggingSlider.getMax() - draggingSlider.getMin());
            double increment = draggingSlider.getIncrement();
            double steps = (rawValue - draggingSlider.getMin()) / increment;
            double snapped = draggingSlider.getMin() + Math.round(steps) * increment;
            snapped = Math.round(snapped * 10000.0) / 10000.0;
            snapped = Math.max(draggingSlider.getMin(), Math.min(draggingSlider.getMax(), snapped));
            return snapped;
        }

        private boolean isHover(int mx, int my, int x, int y, int w, int h) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }

        private int getTotalHeight() {
            int total = height;
            for (Module module : Client.INSTANCE.manager.getModulesByCategory(category)) {
                total += 14;
                if (module.open && hasSettings(module)) {
                    total += module.getSettings().size() * 14;
                }
            }
            return total;
        }
    }

}

