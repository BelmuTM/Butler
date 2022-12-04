package com.belmu.butler.util;

import net.dv8tion.jda.api.entities.MessageEmbed;
import com.belmu.butler.Butler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CooldownMessages {

    public static void a(SlashCommandInteractionEvent event, MessageEmbed embed)  {
        event.replyEmbeds(embed).queue(msg -> msg.deleteOriginal().queueAfter(Butler.deleteTime, Butler.unit, null, fail -> {}));
    }

    public static void b(SlashCommandInteractionEvent event, MessageEmbed embed, long del)  {
        event.replyEmbeds(embed).queue(msg -> msg.deleteOriginal().queueAfter(del, Butler.unit, null, fail -> {}));
    }
}
