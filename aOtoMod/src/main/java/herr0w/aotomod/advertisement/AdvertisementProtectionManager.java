package herr0w.aotomod.advertisement;

import herr0w.aotomod.config.ConfigManager;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AdvertisementProtectionManager {
    private static final Pattern IP = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Pattern DOMAIN = Pattern.compile("\\b(?:[a-z0-9-]+\\.)+(?:com|net|org|gg|io|me|xyz|co|tr|tk|ml|cf|fun|site|online|store|network|dev)\\b");
    private static final Pattern DISCORD = Pattern.compile("\\b(?:discord\\.gg|discord\\.com/invite|discordapp\\.com/invite)/?([a-z0-9-]+)\\b");
    private final ConfigManager configManager;

    public AdvertisementProtectionManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public AdvertisementCheckResult check(String message) {
        if (!configManager.advertisementEnabled()) {
            return AdvertisementCheckResult.clean();
        }
        String normalized = normalize(message);
        if (configManager.detectDiscordInvites()) {
            Matcher matcher = DISCORD.matcher(normalized);
            if (matcher.find()) {
                String code = matcher.group(1);
                if (!allowedDiscord(code)) {
                    return new AdvertisementCheckResult(true, "Discord daveti", matcher.group());
                }
            }
        }
        if (configManager.detectIps()) {
            Matcher matcher = IP.matcher(normalized);
            if (matcher.find() && !allowedIp(matcher.group())) {
                return new AdvertisementCheckResult(true, "IP adresi", matcher.group());
            }
        }
        if (configManager.detectDomains()) {
            Matcher matcher = DOMAIN.matcher(normalized);
            while (matcher.find()) {
                String domain = matcher.group();
                if (!allowedDomain(domain)) {
                    return new AdvertisementCheckResult(true, "Alan adı", domain);
                }
            }
        }
        return AdvertisementCheckResult.clean();
    }

    private String normalize(String input) {
        String value = input == null ? "" : input.toLowerCase(Locale.ROOT);
        value = value.replaceAll("\\s*\\.\\s*", ".");
        value = value.replaceAll("\\s*[/]\\s*", "/");
        value = value.replaceAll("(?i)d\\s*i\\s*s\\s*c\\s*o\\s*r\\s*d", "discord");
        value = value.replaceAll("[\\[\\]{}()<>_,;:|]+", "");
        if (configManager.detectObfuscatedLinks()) {
            value = value.replace(" dot ", ".");
            value = value.replace(" nokta ", ".");
        }
        return value.replaceAll("\\s+", "");
    }

    private boolean allowedDomain(String domain) {
        for (String allowed : configManager.allowedDomains()) {
            String clean = allowed.toLowerCase(Locale.ROOT);
            if (domain.equals(clean) || domain.endsWith("." + clean)) {
                return true;
            }
        }
        return false;
    }

    private boolean allowedDiscord(String code) {
        for (String allowed : configManager.allowedDiscordInvites()) {
            if (code.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }

    private boolean allowedIp(String ip) {
        for (String allowed : configManager.allowedIps()) {
            if (ip.equals(allowed)) {
                return true;
            }
        }
        return false;
    }
}
