package dev.abrahamgracef.core;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {

    private final Map<String, Command> commands = new HashMap<>();

    public void register(Command command) {
        commands.put(command.name(), command);
    }

    public Command get(String name) {
        return commands.get(name);
    }
}
