package herr0w.aotomod;

import herr0w.aotomod.command.AutomodCommand;
import herr0w.aotomod.advertisement.AdvertisementProtectionManager;
import herr0w.aotomod.config.ConfigManager;
import herr0w.aotomod.config.MessageManager;
import herr0w.aotomod.discord.DiscordBotManager;
import herr0w.aotomod.discord.DiscordWebhookManager;
import herr0w.aotomod.filter.WordFilter;
import herr0w.aotomod.history.PlayerMessageHistoryManager;
import herr0w.aotomod.listener.ChatListener;
import herr0w.aotomod.mute.MuteManager;
import herr0w.aotomod.panel.ModerationPanelManager;
import herr0w.aotomod.panel.PanelClickListener;
import herr0w.aotomod.spam.SpamProtectionManager;
import herr0w.aotomod.stats.PluginStatsManager;
import herr0w.aotomod.violation.ViolationManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AOtoMod extends JavaPlugin {
    private ConfigManager configManager;
    private MessageManager messageManager;
    private MuteManager muteManager;
    private WordFilter wordFilter;
    private PlayerMessageHistoryManager historyManager;
    private DiscordWebhookManager webhookManager;
    private DiscordBotManager botManager;
    private SpamProtectionManager spamProtectionManager;
    private AdvertisementProtectionManager advertisementProtectionManager;
    private ViolationManager violationManager;
    private PluginStatsManager statsManager;
    private ModerationPanelManager panelManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        historyManager = new PlayerMessageHistoryManager(configManager);
        muteManager = new MuteManager(this, configManager);
        wordFilter = new WordFilter(configManager);
        webhookManager = new DiscordWebhookManager(this, configManager);
        botManager = new DiscordBotManager(this, configManager, messageManager, muteManager);
        spamProtectionManager = new SpamProtectionManager(configManager);
        advertisementProtectionManager = new AdvertisementProtectionManager(configManager);
        violationManager = new ViolationManager(this);
        botManager.setViolationManager(violationManager);
        muteManager.load();
        violationManager.load();
        statsManager = new PluginStatsManager(violationManager, muteManager);
        panelManager = new ModerationPanelManager(this, configManager, messageManager, muteManager, violationManager, historyManager, statsManager);
        muteManager.startCleanupTask();
        botManager.start();
        getServer().getPluginManager().registerEvents(new ChatListener(this, configManager, messageManager, muteManager, wordFilter, historyManager, webhookManager, botManager, spamProtectionManager, advertisementProtectionManager, violationManager), this);
        getServer().getPluginManager().registerEvents(new PanelClickListener(panelManager), this);
        PluginCommand command = getCommand("automod");
        if (command != null) {
            AutomodCommand automodCommand = new AutomodCommand(this, configManager, messageManager, muteManager, wordFilter, historyManager, botManager, panelManager, violationManager, statsManager);
            command.setExecutor(automodCommand);
            command.setTabCompleter(automodCommand);
        }
    }

    @Override
    public void onDisable() {
        if (muteManager != null) {
            muteManager.save();
        }
        if (violationManager != null) {
            violationManager.save();
        }
        if (botManager != null) {
            botManager.shutdown();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        configManager.reload();
        messageManager.reload();
        wordFilter.reload();
        historyManager.reload();
        muteManager.reload();
        violationManager.load();
        botManager.restart();
    }
}
