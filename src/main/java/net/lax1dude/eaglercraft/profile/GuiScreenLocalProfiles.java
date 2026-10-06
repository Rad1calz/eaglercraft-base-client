package net.lax1dude.eaglercraft.profile;

import java.io.IOException;
import java.util.List;

import net.lax1dude.eaglercraft.opengl.GuiShaderRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class GuiScreenLocalProfiles extends GuiScreen {

    private final GuiScreen parent;
    private GuiTextField profileNameField;
    private List<String> profileNames;
    private String status = "";
    private int left;
    private int top;

    public GuiScreenLocalProfiles(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        left = (width - 420) / 2;
        top = Math.max(28, (height - 390) / 2);
        profileNameField = new GuiTextField(0, fontRendererObj, left + 18, top + 53, 250, 20);
        profileNameField.setMaxStringLength(24);
        profileNameField.setText(EaglerProfile.getName());
        profileNames = LocalProfileStore.getNames();
        buttonList.add(new GuiButton(1, left + 278, top + 53, 124, 20, "Save profile"));

        int rowLimit = Math.min(profileNames.size(), Math.max(0, (height - top - 76) / 26));
        for (int i = 0; i < rowLimit; ++i) {
            int rowY = top + 92 + i * 26;
            buttonList.add(new GuiButton(10 + i, left + 18, rowY, 286, 20, profileNames.get(i)));
            buttonList.add(new GuiButton(100 + i, left + 310, rowY, 92, 20, "Delete"));
        }
        buttonList.add(new GuiButton(2, width / 2 - 50, height - 34, 100, 20, I18n.format("gui.done")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        GuiShaderRenderer.drawPanel(left, top, 420, height - top - 54, width, height, 0xE70A0C10, 0xFFE1283D,
                8.0F, (net.minecraft.client.Minecraft.getSystemTime() % 100000L) / 1000.0F);
        drawCenteredString(fontRendererObj, "LOCAL PROFILES", width / 2, top + 18, 0xFFF5F0F1);
        drawString(fontRendererObj, "Profile name", left + 18, top + 42, 0xFFB9B3B6);
        profileNameField.drawTextBox();
        if (profileNames.isEmpty()) {
            drawString(fontRendererObj, "No saved profiles", left + 18, top + 96, 0xFFB9B3B6);
        }
        if (!status.isEmpty()) {
            drawCenteredString(fontRendererObj, status, width / 2, height - 53, 0xFFFF586A);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            if (LocalProfileStore.saveCurrent(profileNameField.getText())) {
                status = "Saved locally";
            } else if (profileNameField.getText().trim().isEmpty()) {
                status = "Enter a profile name";
            } else {
                status = "Profile limit reached; delete one first";
            }
            initGui();
        } else if (button.id == 2) {
            mc.displayGuiScreen(parent);
        } else if (button.id >= 100 && button.id < 100 + profileNames.size()) {
            String name = profileNames.get(button.id - 100);
            LocalProfileStore.delete(name);
            status = "Deleted " + name;
            initGui();
        } else if (button.id >= 10 && button.id < 10 + profileNames.size()) {
            String name = profileNames.get(button.id - 10);
            if (LocalProfileStore.load(name)) {
                if (mc.world != null) {
                    LocalProfileStore.disconnectForSwitch(mc);
                } else {
                    mc.displayGuiScreen(parent);
                }
            } else {
                status = "Could not load " + name;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        profileNameField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        profileNameField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }
}
