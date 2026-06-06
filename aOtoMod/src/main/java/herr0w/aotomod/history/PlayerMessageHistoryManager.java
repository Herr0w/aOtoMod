package herr0w.aotomod.history;

import herr0w.aotomod.config.ConfigManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerMessageHistoryManager {
    private final ConfigManager configManager;
    private final Map<UUID, Deque<String>> history = new ConcurrentHashMap<>();
    private int size;

    public PlayerMessageHistoryManager(ConfigManager configManager) {
        this.configManager = configManager;
        reload();
    }

    public void reload() {
        size = configManager.historySize();
        for (Deque<String> messages : history.values()) {
            trim(messages);
        }
    }

    public void add(UUID uuid, String message) {
        if (size <= 0) {
            return;
        }
        Deque<String> messages = history.computeIfAbsent(uuid, ignored -> new ArrayDeque<>());
        messages.addLast(message);
        trim(messages);
    }

    public List<String> get(UUID uuid) {
        Deque<String> messages = history.get(uuid);
        if (messages == null) {
            return List.of();
        }
        return new ArrayList<>(messages);
    }

    private void trim(Deque<String> messages) {
        while (messages.size() > size) {
            messages.removeFirst();
        }
    }
}
