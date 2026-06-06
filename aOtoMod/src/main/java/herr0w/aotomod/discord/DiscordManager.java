package herr0w.aotomod.discord;

public interface DiscordManager {
    void start();

    void restart();

    void shutdown();

    void send(ModerationAlert alert);
}
