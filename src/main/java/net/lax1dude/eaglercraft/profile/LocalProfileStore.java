package net.lax1dude.eaglercraft.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.lax1dude.eaglercraft.EagRuntime;
import net.lax1dude.eaglercraft.EaglerInputStream;
import net.lax1dude.eaglercraft.EaglerOutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentString;

public final class LocalProfileStore {

    private static final String STORAGE_KEY = "redline_profiles";
    private static final int MAX_PROFILES = 4;

    private LocalProfileStore() {
    }

    public static List<String> getNames() {
        NBTTagList profiles = readRoot().getTagList("profiles", 10);
        List<String> names = new ArrayList<>();
        for (int i = 0; i < profiles.tagCount(); ++i) {
            String name = profiles.getCompoundTagAt(i).getString("name");
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    public static boolean saveCurrent(String profileName) {
        String name = profileName == null ? "" : profileName.trim();
        if (name.isEmpty()) {
            return false;
        }
        if (name.length() > 24) {
            name = name.substring(0, 24);
        }

        byte[] snapshot = EaglerProfile.write();
        if (snapshot == null) {
            return false;
        }

        NBTTagCompound root = readRoot();
        NBTTagList profiles = root.getTagList("profiles", 10);
        NBTTagList updated = new NBTTagList();
        boolean replaced = false;
        for (int i = 0; i < profiles.tagCount(); ++i) {
            NBTTagCompound profile = profiles.getCompoundTagAt(i);
            if (profile.getString("name").equalsIgnoreCase(name)) {
                if (!replaced) {
                    updated.appendTag(createEntry(name, snapshot));
                    replaced = true;
                }
            } else {
                updated.appendTag(profile);
            }
        }
        if (!replaced) {
            if (updated.tagCount() >= MAX_PROFILES) {
                return false;
            }
            updated.appendTag(createEntry(name, snapshot));
        }
        root.setTag("profiles", updated);
        return writeRoot(root);
    }

    public static boolean load(String profileName) {
        NBTTagList profiles = readRoot().getTagList("profiles", 10);
        for (int i = 0; i < profiles.tagCount(); ++i) {
            NBTTagCompound profile = profiles.getCompoundTagAt(i);
            if (profile.getString("name").equalsIgnoreCase(profileName)) {
                byte[] snapshot = profile.getByteArray("data");
                if (snapshot.length == 0) {
                    return false;
                }
                EaglerProfile.read(snapshot);
                EaglerProfile.setName(EaglerProfile.getName());
                EaglerProfile.save();
                return true;
            }
        }
        return false;
    }

    public static boolean delete(String profileName) {
        NBTTagCompound root = readRoot();
        NBTTagList profiles = root.getTagList("profiles", 10);
        NBTTagList updated = new NBTTagList();
        boolean removed = false;
        for (int i = 0; i < profiles.tagCount(); ++i) {
            NBTTagCompound profile = profiles.getCompoundTagAt(i);
            if (profile.getString("name").equalsIgnoreCase(profileName)) {
                removed = true;
            } else {
                updated.appendTag(profile);
            }
        }
        if (removed) {
            root.setTag("profiles", updated);
            return writeRoot(root);
        }
        return false;
    }

    public static void disconnectForSwitch(Minecraft minecraft) {
        if (minecraft.world == null) {
            return;
        }

        boolean integratedServer = minecraft.isIntegratedServerRunning();
        minecraft.world.sendQuittingDisconnectingPacket();
        minecraft.loadWorld((WorldClient) null);
        GuiScreen message = new GuiDisconnected(new GuiMainMenu(), "disconnect.genericReason",
                new TextComponentString("Switching characters, bro. Rejoin to apply."));
        if (integratedServer) {
            minecraft.shutdownIntegratedServer(message);
        } else {
            minecraft.displayGuiScreen(message);
        }
    }

    private static NBTTagCompound createEntry(String name, byte[] snapshot) {
        NBTTagCompound profile = new NBTTagCompound();
        profile.setString("name", name);
        profile.setByteArray("data", snapshot);
        return profile;
    }

    private static NBTTagCompound readRoot() {
        byte[] stored = EagRuntime.getStorage(STORAGE_KEY);
        if (stored != null) {
            try {
                NBTTagCompound root = CompressedStreamTools.readCompressed(new EaglerInputStream(stored));
                if (root != null) {
                    return root;
                }
            } catch (IOException ex) {
            }
        }
        return new NBTTagCompound();
    }

    private static boolean writeRoot(NBTTagCompound root) {
        EaglerOutputStream output = new EaglerOutputStream();
        try {
            CompressedStreamTools.writeCompressed(root, output);
        } catch (IOException ex) {
            return false;
        }
        EagRuntime.setStorage(STORAGE_KEY, output.toByteArray());
        return true;
    }
}
