package dev.abrahamgracef;

import dev.abrahamgracef.commands.utility.PingCommand;
import dev.abrahamgracef.core.Command;
import dev.abrahamgracef.core.CommandRegistry;
import dev.abrahamgracef.jda.music.LavalinkConfig;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import io.github.cdimascio.dotenv.Dotenv;

public class Main {

    public static void main(String[] args) throws Exception {

        String token = Dotenv.load().get("DISCORD_TOKEN");

        GatewayDiscordClient client =
                DiscordClientBuilder.create(token)
                        .build()
                        .login()
                        .block();

        System.out.println("✅ Bot is online!");

        CommandRegistry registry = new CommandRegistry();
        registry.register(new PingCommand()); // you already have this

        client.on(MessageCreateEvent.class)
                .subscribe(event -> {

                    if (event.getMessage().getAuthor()
                            .map(user -> user.isBot())
                            .orElse(false)) return;

                    String content = event.getMessage().getContent();
                    if (!content.startsWith("!")) return;

                    String commandName = content.substring(1).split(" ")[0];
                    Command command = registry.get(commandName);

                    if (command != null) {
                        command.execute(event);
                    }
                });
        new LavalinkConfig(token);

        client.onDisconnect().block();


    }
}
