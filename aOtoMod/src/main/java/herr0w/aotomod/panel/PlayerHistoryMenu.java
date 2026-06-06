package herr0w.aotomod.panel;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class PlayerHistoryMenu implements PanelMenu {
    private final ModerationPanelManager panelManager;

    public PlayerHistoryMenu(ModerationPanelManager panelManager) {
        this.panelManager = panelManager;
    }

    @Override
    public void open(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(null, 54, panelManager.title());
        Map<Integer, Consumer<ClickType>> actions = new HashMap<>();
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        int pageSize = panelManager.configManager().moderationPanelPageSize();
        int start = page * pageSize;
        for (int i = 0; i < pageSize && start + i < players.size(); i++) {
            Player target = players.get(start + i);
            panelManager.set(inventory, actions, i, Material.PLAYER_HEAD, "panel.history.player", Map.of("player", target.getName()), () -> open(player, target));
        }
        if (page > 0) {
            panelManager.set(inventory, actions, 45, Material.ARROW, "panel.previous", Map.of(), () -> open(player, page - 1));
        }
        if (start + pageSize < players.size()) {
            panelManager.set(inventory, actions, 53, Material.ARROW, "panel.next", Map.of(), () -> open(player, page + 1));
        }
        panelManager.back(inventory, actions, player);
        panelManager.open(player, inventory, actions);
    }

    public void open(Player viewer, OfflinePlayer target) {
        Inventory inventory = Bukkit.createInventory(null, 54, panelManager.title());
        Map<Integer, Consumer<ClickType>> actions = new HashMap<>();
        List<String> history = panelManager.historyManager().get(target.getUniqueId());
        for (int i = 0; i < Math.min(45, history.size()); i++) {
            panelManager.set(inventory, actions, i, Material.BOOK, "panel.history.item", Map.of("player", target.getName() == null ? target.getUniqueId().toString() : target.getName(), "message", history.get(i)), () -> {
            });
        }
        panelManager.back(inventory, actions, viewer);
        panelManager.open(viewer, inventory, actions);
    }
}
