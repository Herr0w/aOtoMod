package herr0w.aotomod.command;

import herr0w.aotomod.AOtoMod;
import herr0w.aotomod.config.ConfigManager;
import herr0w.aotomod.config.MessageManager;
import herr0w.aotomod.discord.DiscordBotManager;
import herr0w.aotomod.filter.WordFilter;
import herr0w.aotomod.history.PlayerMessageHistoryManager;
import herr0w.aotomod.mute.MuteManager;
import herr0w.aotomod.mute.MuteRecord;
import herr0w.aotomod.panel.ModerationPanelManager;
import herr0w.aotomod.stats.PluginStatsManager;
import herr0w.aotomod.violation.ViolationManager;
import herr0w.aotomod.violation.ViolationRecord;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class AutomodCommand implements CommandExecutor, TabCompleter {
    private final AOtoMod plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final MuteManager muteManager;
    private final ModerationPanelManager panelManager;
    private final ViolationManager violationManager;
    private final PluginStatsManager statsManager;

    public AutomodCommand(AOtoMod plugin, ConfigManager configManager, MessageManager messageManager, MuteManager muteManager, WordFilter wordFilter, PlayerMessageHistoryManager historyManager, DiscordBotManager botManager, ModerationPanelManager panelManager, ViolationManager violationManager, PluginStatsManager statsManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageManager = messageManager;
        this.muteManager = muteManager;
        this.panelManager = panelManager;
        this.violationManager = violationManager;
        this.statsManager = statsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messageManager.get("usage"));
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            if (!sender.hasPermission(configManager.permission("reload"))) {
                sender.sendMessage(messageManager.get("no-permission"));
                return true;
            }
            plugin.reloadPlugin();
            sender.sendMessage(messageManager.get("reload-success"));
            return true;
        }
        if (sub.equals("unmute")) {
            return unmute(sender, args);
        }
        if (sub.equals("panel")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(messageManager.get("only-player"));
                return true;
            }
            if (!sender.hasPermission(configManager.moderationPanelPermission())) {
                sender.sendMessage(messageManager.get("no-permission"));
                return true;
            }
            panelManager.openMain(player);
            return true;
        }
        if (sub.equals("history") || sub.equals("violations")) {
            return history(sender, args);
        }
        if (sub.equals("stats")) {
            if (!sender.hasPermission("automod.stats")) {
                sender.sendMessage(messageManager.get("no-permission"));
                return true;
            }
            sender.sendMessage(messageManager.get("stats-message", Map.of("total", String.valueOf(statsManager.totalViolations()), "mutes", String.valueOf(statsManager.activeMutes()), "spam", String.valueOf(statsManager.spamViolations()), "ads", String.valueOf(statsManager.advertisementViolations()), "profanity", String.valueOf(statsManager.profanityViolations()))));
            return true;
        }
        sender.sendMessage(messageManager.get("usage"));
        return true;
    }

    private boolean unmute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(configManager.permission("unmute"))) {
            sender.sendMessage(messageManager.get("no-permission"));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(messageManager.get("usage"));
            return true;
        }
        Optional<MuteRecord> record = muteManager.findByName(args[1]);
        if (record.isEmpty()) {
            sender.sendMessage(messageManager.get("not-muted"));
            return true;
        }
        muteManager.unmute(record.get().uuid());
        sender.sendMessage(messageManager.get("unmute-success", Map.of("player", record.get().playerName())));
        return true;
    }

    private boolean history(CommandSender sender, String[] args) {
        if (!sender.hasPermission("automod.history")) {
            sender.sendMessage(messageManager.get("no-permission"));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(messageManager.get("usage"));
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        List<ViolationRecord> records = violationManager.byPlayer(args[1]);
        sender.sendMessage(messageManager.get("history-header", Map.of("player", target.getName() == null ? args[1] : target.getName(), "count", String.valueOf(records.size()))));
        for (ViolationRecord record : records.stream().limit(10).toList()) {
            sender.sendMessage(messageManager.get("history-line", Map.of("type", record.type(), "reason", record.detectionReason(), "punishment", record.punishmentType() + " " + record.punishmentDuration())));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(List.of("reload", "unmute", "panel", "history", "violations", "stats"));
            return filter(completions, args[0]);
        }
        if (args.length == 2 && List.of("unmute", "history", "violations").contains(args[0].toLowerCase(Locale.ROOT))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
            return filter(completions, args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }
}
