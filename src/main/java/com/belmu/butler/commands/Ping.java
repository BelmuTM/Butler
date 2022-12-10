package com.belmu.butler.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class Ping extends ListenerAdapter {

    public String cmdName = "ping";
    public String cmdDescription = "Gives you the bot's latency in milliseconds (ms)";

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmd = event.getName();

        if(cmd.equals(cmdName)) {
            event.deferReply().queue();

            event.getHook().sendMessage("Pong! :ping_pong: ").queue(
                    (message) -> message.editMessageFormat(":satellite: **Ping**: `%sms`", event.getJDA().getGatewayPing()).queueAfter(85, TimeUnit.MILLISECONDS)
            );
        }
    }
}
