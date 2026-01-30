package dev.abrahamgracef.xyvoze.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import dev.abrahamgracef.jda.music.TrackQueue;
import dev.abrahamgracef.jda.music.GuildMusicManager;


public class Play extends ListenerAdapter {

    private final LavalinkClient lavalinkClient;

    public Play(LavalinkClient lavalinkClient) {
        this.lavalinkClient = lavalinkClient;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        // Ignore bots
        if (event.getAuthor().isBot()) return;

        String content = event.getMessage().getContentRaw();

        if (content.equalsIgnoreCase("!skip")) {
            handleSkip(event);
            return;
        }

        if (content.equalsIgnoreCase("!pause")) {
            handlePause(event);
            return;
        }

        if (content.equalsIgnoreCase("!resume")) {
            handleResume(event);
            return;
        }

        if (!content.startsWith("!play")) return;


        String[] parts = content.split(" ", 2);
        if (parts.length < 2) {
            event.getChannel()
                    .sendMessage("❌ Usage: `!play <song name or url>`")
                    .queue();
            return;
        }

        GuildVoiceState voiceState = event.getMember().getVoiceState();
        if (voiceState == null || !voiceState.inAudioChannel()) {
            event.getChannel()
                    .sendMessage("❌ You must be in a voice channel")
                    .queue();
            return;
        }

        // Join VC if not already connected
        if (!event.getGuild().getSelfMember()
                .getVoiceState().inAudioChannel()) {

            event.getGuild()
                    .getAudioManager()
                    .openAudioConnection(voiceState.getChannel());
        }

        String input = parts[1];
        String query = input.startsWith("http")
                ? input
                : "ytsearch:" + input;

        Link link = lavalinkClient
                .getOrCreateLink(event.getGuild().getIdLong());

        link.loadItem(query).subscribe(result -> {

            if (result instanceof TrackLoaded trackLoaded) {

                handleTrack(link, trackLoaded.getTrack(), event);

            } else if (result instanceof SearchResult searchResult) {

                if (!searchResult.getTracks().isEmpty()) {
                    handleTrack(link, searchResult.getTracks().get(0), event);
                } else {
                    event.getChannel()
                            .sendMessage("❌ No results found")
                            .queue();
                }

            } else if (result instanceof PlaylistLoaded playlistLoaded) {

                handleTrack(link, playlistLoaded.getTracks().get(0), event);

            } else if (result instanceof NoMatches) {

                event.getChannel()
                        .sendMessage("❌ No matches found")
                        .queue();

            } else if (result instanceof LoadFailed failed) {

                event.getChannel()
                        .sendMessage("❌ Load failed: " +
                                failed.getException().getMessage())
                        .queue();
            }

        });
    }

    private void play(Link link, Track track, MessageReceivedEvent event) {
        link.createOrUpdatePlayer()
                .setTrack(track)
                .subscribe(player ->
                        event.getChannel()
                                .sendMessage("🎶 Now playing: **" +
                                        track.getInfo().getTitle() + "**")
                                .queue()
                );
    }
    private void handlePause(MessageReceivedEvent event) {
        Link link = lavalinkClient.getOrCreateLink(event.getGuild().getIdLong());

        if (link.getCachedPlayer() == null) {
            event.getChannel().sendMessage("❌ Nothing is playing").queue();
            return;
        }

        link.getCachedPlayer().setPaused(true).subscribe();
        event.getChannel().sendMessage("⏸️ Paused").queue();
    }

    private void handleResume(MessageReceivedEvent event) {
        Link link = lavalinkClient.getOrCreateLink(event.getGuild().getIdLong());

        if (link.getCachedPlayer() == null) {
            event.getChannel().sendMessage("❌ Nothing is playing").queue();
            return;
        }

        link.getCachedPlayer().setPaused(false).subscribe();
        event.getChannel().sendMessage("▶️ Resumed").queue();
    }
    private void handleTrack(Link link, Track track, MessageReceivedEvent event) {

        TrackQueue queue = GuildMusicManager.getQueue(event.getGuild().getIdLong());

        // If something is already playing → queue it
        if (link.getCachedPlayer() != null &&
                link.getCachedPlayer().getTrack() != null) {

            queue.add(track);
            event.getChannel()
                    .sendMessage("➕ Added to queue: **" + track.getInfo().getTitle() + "**")
                    .queue();
            return;
        }

        // Nothing playing → play immediately
        play(link, track, event);
    }
    private void handleSkip(MessageReceivedEvent event) {

        Link link = lavalinkClient.getOrCreateLink(event.getGuild().getIdLong());

        if (link.getCachedPlayer() == null ||
                link.getCachedPlayer().getTrack() == null) {

            event.getChannel()
                    .sendMessage("❌ Nothing is playing")
                    .queue();
            return;
        }

        TrackQueue queue = GuildMusicManager.getQueue(event.getGuild().getIdLong());

        // If queue is empty → stop playback
        if (queue.isEmpty()) {
            link.getCachedPlayer().setTrack(null).subscribe();
            event.getChannel()
                    .sendMessage("⏹️ Skipped. Queue is empty.")
                    .queue();
            return;
        }

        // Play next track from queue
        Track next = queue.poll();

        link.createOrUpdatePlayer()
                .setTrack(next)
                .subscribe(player ->
                        event.getChannel()
                                .sendMessage("⏭️ Skipped! Now playing: **" +
                                        next.getInfo().getTitle() + "**")
                                .queue()
                );
    }

}
