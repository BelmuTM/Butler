package com.belmu.butler.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class PingCommand extends ListenerAdapter {

    public String cmdName = "ping";
    public String cmdDescription = "Checks Butler's response time in milliseconds (ms)";

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals(cmdName)) {
            event.deferReply().queue();
            event.getHook().sendMessage("Pong! :ping_pong: ").queue(
                    (message) -> message.editMessageFormat(":satellite: **Ping**: `%sms`", event.getJDA().getGatewayPing()).queueAfter(85, TimeUnit.MILLISECONDS)
            );
        }
    }
}
