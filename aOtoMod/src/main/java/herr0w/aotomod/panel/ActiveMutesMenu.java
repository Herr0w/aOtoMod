package herr0w.aotomod.panel;

import herr0w.aotomod.mute.MuteRecord;
import herr0w.aotomod.util.TimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ActiveMutesMenu implements PanelMenu {
    private final ModerationPanelManager panelManager;

    public ActiveMutesMenu(ModerationPanelManager panelManager) {
        this.panelManager = panelManager;
    }

    @Override
    public void open(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(null, 54, panelManager.title());
        Map<Integer, Consumer<ClickType>> actions = new HashMap<>();
        List<MuteRecord> mutes = new ArrayList<>(panelManager.muteManager().activeMutes());
        int pageSize = panelManager.configManager().moderationPanelPageSize();
        int start = page * pageSize;
        for (int i = 0; i < pageSize && start + i < mutes.size(); i++) {
            MuteRecord mute = mutes.get(start + i);
            Map<String, String> replacements = Map.of("player", mute.playerName(), "level", String.valueOf(mute.level()), "remaining", TimeFormatter.format(Duration.ofMillis(mute.remainingMillis())), "word", mute.bannedWord());
            panelManager.set(inventory, actions, i, Material.PLAYER_HEAD, "panel.mutes.item", replacements, click -> {
                if (click == ClickType.RIGHT) {
                    panelManager.muteManager().unmute(mute.uuid());
                    player.sendMessage(panelManager.messageManager().get("unmute-success", Map.of("player", mute.playerName())));
                    open(player, page);
                } else if (click == ClickType.SHIFT_RIGHT) {
                    panelManager.muteManager().extend(mute.uuid(), panelManager.configManager().additionalMuteDuration());
                    player.sendMessage(panelManager.messageManager().get("panel-extend-success", Map.of("player", mute.playerName())));
                    open(player, page);
                } else if (click == ClickType.SHIFT_LEFT) {
                    String command = panelManager.configManager().banCommand().replace("%player%", mute.playerName()).replace("%uuid%", mute.uuid().toString());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    player.sendMessage(panelManager.messageManager().get("panel-ban-success", Map.of("player", mute.playerName())));
                    open(player, page);
                } else {
                    player.sendMessage(panelManager.messageManager().get("panel-mute-detail", replacements));
                }
            });
        }
        if (page > 0) {
            panelManager.set(inventory, actions, 45, Material.ARROW, "panel.previous", Map.of(), () -> open(player, page - 1));
        }
        if (start + pageSize < mutes.size()) {
            panelManager.set(inventory, actions, 53, Material.ARROW, "panel.next", Map.of(), () -> open(player, page + 1));
        }
        panelManager.back(inventory, actions, player);
        panelManager.open(player, inventory, actions);
    }
}
