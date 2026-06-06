package herr0w.aotomod.discord;

import herr0w.aotomod.config.ConfigManager;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public final class DiscordPermissionChecker {
    private final ConfigManager configManager;

    public DiscordPermissionChecker(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean allowed(Member member) {
        List<String> allowed = configManager.allowedRoleIds();
        if (allowed.isEmpty()) {
            return true;
        }
        if (member == null) {
            return false;
        }
        return member.getRoles().stream().anyMatch(role -> allowed.contains(role.getId()));
    }
}
