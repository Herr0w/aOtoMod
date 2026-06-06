package herr0w.aotomod.panel;

import herr0w.aotomod.violation.ViolationRecord;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class RecentViolationsMenu implements PanelMenu {
    private final ModerationPanelManager panelManager;

    public RecentViolationsMenu(ModerationPanelManager panelManager) {
        this.panelManager = panelManager;
    }

    @Override
    public void open(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(null, 54, panelManager.title());
        Map<Integer, Consumer<ClickType>> actions = new HashMap<>();
        List<ViolationRecord> violations = panelManager.violationManager().recent(500);
        int pageSize = panelManager.configManager().moderationPanelPageSize();
        int start = page * pageSize;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(panelManager.configManager().dateFormat()).withZone(ZoneId.systemDefault());
        for (int i = 0; i < pageSize && start + i < violations.size(); i++) {
            ViolationRecord record = violations.get(start + i);
            Map<String, String> replacements = Map.of("player", record.playerName(), "type", record.type(), "reason", record.detectionReason(), "message", record.originalMessage(), "punishment", record.punishmentType() + " " + record.punishmentDuration(), "date", formatter.format(Instant.ofEpochMilli(record.timestamp())));
            panelManager.set(inventory, actions, i, Material.PAPER, "panel.violations.item", replacements, () -> player.sendMessage(panelManager.messageManager().get("panel-violation-detail", replacements)));
        }
        if (page > 0) {
            panelManager.set(inventory, actions, 45, Material.ARROW, "panel.previous", Map.of(), () -> open(player, page - 1));
        }
        if (start + pageSize < violations.size()) {
            panelManager.set(inventory, actions, 53, Material.ARROW, "panel.next", Map.of(), () -> open(player, page + 1));
        }
        panelManager.back(inventory, actions, player);
        panelManager.open(player, inventory, actions);
    }
}
