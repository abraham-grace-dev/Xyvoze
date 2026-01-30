package dev.abrahamgracef.jda.music;

import dev.arbjerg.lavalink.client.player.Track;

import java.util.LinkedList;
import java.util.Queue;

public class TrackQueue {

    private final Queue<Track> queue = new LinkedList<>();

    // Add track to queue
    public void add(Track track) {
        queue.offer(track);
    }

    // Get and remove next track
    public Track poll() {
        return queue.poll();
    }

    // Peek next track without removing
    public Track peek() {
        return queue.peek();
    }

    // Check if queue is empty
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    // Get queue size
    public int size() {
        return queue.size();
    }

    // Clear queue
    public void clear() {
        queue.clear();
    }
}
