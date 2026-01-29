package dev.abrahamgracef.jda.music;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.net.URI;

public class LavalinkConfig {

    private final LavalinkClient lavalinkClient;
    private final JDA jda;

    public LavalinkConfig(String token) throws Exception {

        long botId = Helpers.getUserIdFromToken(token);
        this.lavalinkClient = new LavalinkClient(botId);

        NodeOptions node = new NodeOptions.Builder()
                .setName("main-node")
                .setServerUri(URI.create("http://localhost:25635"))
                .setPassword("youshallnotpass")
                .build();

        lavalinkClient.addNode(node);

        this.jda = JDABuilder.createDefault(token)
                .enableIntents(
                        net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES,
                        net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT,
                        net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES
                )
                .setVoiceDispatchInterceptor(
                        new JDAVoiceUpdateListener(lavalinkClient)
                )
                .addEventListeners(new dev.abrahamgracef.xyvoze.music.Play(lavalinkClient))
                .build();

        System.out.println("✅ JDA + Lavalink (v3.3.0) started");
    }

    public JDA getJda() {
        return jda;
    }

    public LavalinkClient getLavalinkClient() {
        return lavalinkClient;
    }
}
