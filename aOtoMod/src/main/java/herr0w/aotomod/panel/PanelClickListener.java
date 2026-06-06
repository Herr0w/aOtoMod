package herr0w.aotomod.panel;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public final class PanelClickListener implements Listener {
    private final ModerationPanelManager panelManager;

    public PanelClickListener(ModerationPanelManager panelManager) {
        this.panelManager = panelManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !panelManager.hasSession(player)) {
            return;
        }
        event.setCancelled(true);
        panelManager.click(player, event.getRawSlot(), event.getClick());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            panelManager.close(player);
        }
    }
}
