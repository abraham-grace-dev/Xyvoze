package dev.abrahamgracef.commands.utility;

import dev.abrahamgracef.core.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class PingCommand implements Command {

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public void execute(MessageCreateEvent event) {

        event.getMessage()
                .getChannel()
                .flatMap(channel ->
                        channel.createMessage("🏓 Pong! "))
                .subscribe();
    }
}
