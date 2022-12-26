package com.belmu.butler.utility;

import net.dv8tion.jda.api.entities.MessageEmbed;
import com.belmu.butler.Butler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CooldownMessages {

    public static void send(SlashCommandInteractionEvent event, MessageEmbed embed)  {
        event.replyEmbeds(embed).queue(msg -> msg.deleteOriginal().queueAfter(Butler.deleteTime, Butler.unit, null, fail -> {}));
    }

    public static void send(SlashCommandInteractionEvent event, MessageEmbed embed, long del)  {
        event.replyEmbeds(embed).queue(msg -> msg.deleteOriginal().queueAfter(del, Butler.unit, null, fail -> {}));
    }

    public static void reply(SlashCommandInteractionEvent event, MessageEmbed embed)  {
        event.getHook().sendMessageEmbeds(embed).queue(msg -> {
            if(!msg.isEphemeral()) msg.delete().queueAfter(Butler.deleteTime, Butler.unit, null, fail -> {});
        });
    }

    public static void reply(SlashCommandInteractionEvent event, MessageEmbed embed, long del)  {
        event.getHook().sendMessageEmbeds(embed).queue(msg -> {
            if(!msg.isEphemeral()) msg.delete().queueAfter(del, Butler.unit, null, fail -> {});
        });
    }
}
