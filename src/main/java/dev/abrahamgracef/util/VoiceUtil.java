package dev.abrahamgracef.util;

import discord4j.core.object.entity.channel.VoiceChannel;

public class VoiceUtil {

    public static void join(VoiceChannel channel) {
        channel.join()
                .withSelfDeaf(true)
                .subscribe();
    }
}
