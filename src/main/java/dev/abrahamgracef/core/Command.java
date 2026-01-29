package dev.abrahamgracef.core;

import discord4j.core.event.domain.message.MessageCreateEvent;

public interface Command {

    String name();

    void execute(MessageCreateEvent event);
}
