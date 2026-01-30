package dev.abrahamgracef.jda.music;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class GuildMusicManager {

    // One queue per guild (server)
    private static final Map<Long, TrackQueue> queues = new ConcurrentHashMap<>();

    // Get or create queue for a guild
    public static TrackQueue getQueue(long guildId) {
        return queues.computeIfAbsent(guildId, id -> new TrackQueue());
    }

    // Remove queue (useful later for auto-leave / cleanup)
    public static void removeQueue(long guildId) {
        queues.remove(guildId);
    }
}