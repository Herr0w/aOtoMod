package herr0w.aotomod.panel;

import herr0w.aotomod.AOtoMod;
import herr0w.aotomod.config.ConfigManager;
import herr0w.aotomod.config.MessageManager;
import herr0w.aotomod.history.PlayerMessageHistoryManager;
import herr0w.aotomod.mute.MuteManager;
import herr0w.aotomod.stats.PluginStatsManager;
import herr0w.aotomod.violation.ViolationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.inventory.ClickType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class ModerationPanelManager {
    private final AOtoMod plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final MuteManager muteManager;
    private final ViolationManager violationManager;
    private final PlayerMessageHistoryManager historyManager;
    private final PluginStatsManager statsManager;
    private final Map<UUID, PanelSession> sessions = new HashMap<>();
    private final ActiveMutesMenu activeMutesMenu;
    private final RecentViolationsMenu recentViolationsMenu;
    private final PlayerHistoryMenu playerHistoryMenu;
    private final SettingsMenu settingsMenu;

    public ModerationPanelManager(AOtoMod plugin, ConfigManager configManager, MessageManager messageManager, MuteManager muteManager, ViolationManager violationManager, PlayerMessageHistoryManager historyManager, PluginStatsManager statsManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageManager = messageManager;
        this.muteManager = muteManager;
        this.violationManager = violationManager;
        this.historyManager = historyManager;
        this.statsManager = statsManager;
        this.activeMutesMenu = new ActiveMutesMenu(this);
        this.recentViolationsMenu = new RecentViolationsMenu(this);
        this.playerHistoryMenu = new PlayerHistoryMenu(this);
        this.settingsMenu = new SettingsMenu(this);
    }

    public void openMain(Player player) {
        if (!configManager.moderationPanelEnabled()) {
            player.sendMessage(messageManager.get("panel-disabled"));
            return;
        }
        Inventory inventory = Bukkit.createInventory(null, 54, color(configManager.moderationPanelTitle()));
        Map<Integer, Consumer<ClickType>> actions = new HashMap<>();
        set(inventory, actions, 10, Material.BOOK, "panel.main.active-mutes", Map.of(), () -> activeMutesMenu.open(player, 0));
        set(inventory, actions, 12, Material.PAPER, "panel.main.recent-violations", Map.of(), () -> recentViolationsMenu.open(player, 0));
        set(inventory, actions, 14, Material.COMPARATOR, "panel.main.stats", Map.of("total", String.valueOf(statsManager.totalViolations()), "mutes", String.valueOf(statsManager.activeMutes()), "spam", String.valueOf(statsManager.spamViolations()), "ads", String.valueOf(statsManager.advertisementViolations()), "profanity", String.valueOf(statsManager.profanityViolations())), () -> openStats(player));
        set(inventory, actions, 16, Material.REDSTONE_TORCH, "panel.main.settings", Map.of(), () -> settingsMenu.open(player, 0));
        set(inventory, actions, 28, Material.COMPASS, "panel.main.player-history", Map.of(), () -> playerHistoryMenu.open(player, 0));
        set(inventory, actions, 31, Material.EMERALD, "panel.main.reload", Map.of(), () -> {
            plugin.reloadPlugin();
            player.sendMessage(messageManager.get("reload-success"));
            openMain(player);
        });
        player.openInventory(inventory);
        sessions.put(player.getUniqueId(), new PanelSession(actions));
    }

    public void openStats(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, color(configManager.moderationPanelTitle()));
        Map<Integer, Consumer<ClickType>> actions = new HashMap<>();
        set(inventory, actions, 22, Material.COMPARATOR, "panel.stats.summary", Map.of("total", String.valueOf(statsManager.totalViolations()), "mutes", String.valueOf(statsManager.activeMutes()), "spam", String.valueOf(statsManager.spamViolations()), "ads", String.valueOf(statsManager.advertisementViolations()), "profanity", String.valueOf(statsManager.profanityViolations())), () -> openStats(player));
        back(inventory, actions, player);
        player.openInventory(inventory);
        sessions.put(player.getUniqueId(), new PanelSession(actions));
    }

    public void set(Inventory inventory, Map<Integer, Consumer<ClickType>> actions, int slot, Material material, String path, Map<String, String> replacements, Runnable action) {
        set(inventory, actions, slot, material, path, replacements, ignored -> action.run());
    }

    public void set(Inventory inventory, Map<Integer, Consumer<ClickType>> actions, int slot, Material material, String path, Map<String, String> replacements, Consumer<ClickType> action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(messageManager.get(path + ".name", replacements));
        meta.setLore(messageManager.list(path + ".lore", replacements));
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
        actions.put(slot, action);
    }

    public void back(Inventory inventory, Map<Integer, Consumer<ClickType>> actions, Player player) {
        set(inventory, actions, 49, Material.ARROW, "panel.back", Map.of(), () -> openMain(player));
    }

    public void open(Player player, Inventory inventory, Map<Integer, Consumer<ClickType>> actions) {
        player.openInventory(inventory);
        sessions.put(player.getUniqueId(), new PanelSession(actions));
    }

    public void click(Player player, int slot, ClickType clickType) {
        PanelSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }
        Consumer<ClickType> action = session.actions().get(slot);
        if (action != null) {
            action.accept(clickType);
        }
    }

    public boolean hasSession(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public void close(Player player) {
        sessions.remove(player.getUniqueId());
    }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public ConfigManager configManager() {
        return configManager;
    }

    public MessageManager messageManager() {
        return messageManager;
    }

    public MuteManager muteManager() {
        return muteManager;
    }

    public ViolationManager violationManager() {
        return violationManager;
    }

    public PlayerMessageHistoryManager historyManager() {
        return historyManager;
    }

    public String title() {
        return color(configManager.moderationPanelTitle());
    }

    private record PanelSession(Map<Integer, Consumer<ClickType>> actions) {
    }
}
