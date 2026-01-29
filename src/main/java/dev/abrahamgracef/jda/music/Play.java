package dev.abrahamgracef.xyvoze.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

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

        // Must start with !play
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
                play(link, trackLoaded.getTrack(), event);

            } else if (result instanceof SearchResult searchResult) {
                if (!searchResult.getTracks().isEmpty()) {
                    play(link, searchResult.getTracks().get(0), event);
                } else {
                    event.getChannel()
                            .sendMessage("❌ No results found")
                            .queue();
                }

            } else if (result instanceof PlaylistLoaded playlistLoaded) {
                play(link, playlistLoaded.getTracks().get(0), event);

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
}
