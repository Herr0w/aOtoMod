package herr0w.aotomod.panel;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class SettingsMenu implements PanelMenu {
    private final ModerationPanelManager panelManager;

    public SettingsMenu(ModerationPanelManager panelManager) {
        this.panelManager = panelManager;
    }

    @Override
    public void open(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(null, 54, panelManager.title());
        Map<Integer, Consumer<ClickType>> actions = new HashMap<>();
        panelManager.set(inventory, actions, 20, Material.STRING, "panel.settings.spam", Map.of("state", state(panelManager.configManager().spamEnabled())), () -> {
            panelManager.configManager().setPath("spam.enabled", !panelManager.configManager().spamEnabled());
            panelManager.configManager().reload();
            open(player, 0);
        });
        panelManager.set(inventory, actions, 22, Material.MAP, "panel.settings.advertisement", Map.of("state", state(panelManager.configManager().advertisementEnabled())), () -> {
            panelManager.configManager().setPath("advertisement.enabled", !panelManager.configManager().advertisementEnabled());
            panelManager.configManager().reload();
            open(player, 0);
        });
        panelManager.set(inventory, actions, 24, Material.REDSTONE, "panel.settings.discord", Map.of("state", state(panelManager.configManager().discordEnabled())), () -> {
            panelManager.configManager().setPath("discord.enabled", !panelManager.configManager().discordEnabled());
            panelManager.configManager().reload();
            open(player, 0);
        });
        panelManager.back(inventory, actions, player);
        panelManager.open(player, inventory, actions);
    }

    private String state(boolean enabled) {
        return enabled ? "Açık" : "Kapalı";
    }
}
