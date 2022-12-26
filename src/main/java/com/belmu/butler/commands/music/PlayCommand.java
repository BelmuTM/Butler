
package com.belmu.butler.commands.music;

import com.belmu.butler.Butler;
import com.belmu.butler.lavaplayer.PlayerManager;
import com.belmu.butler.utility.CooldownMessages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;

public class PlayCommand extends ListenerAdapter {

    public String cmdName = "play";
    public String cmdDescription = "Plays the desired track(s) in your voice channel";

    public static OptionData[] options = new OptionData[] {
            new OptionData(OptionType.STRING, "song", "The song you want me to play", true)
    };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmd = event.getName();

        if(cmd.equals(cmdName)) {
            Member member = event.getMember();
            GuildVoiceState memberVoiceState = member.getVoiceState();

            if (!memberVoiceState.inAudioChannel()) {
                event.deferReply(true).queue();

                EmbedBuilder a = new EmbedBuilder()
                        .setColor(Butler.darkGray)
                        .setDescription("Please join a channel first.");
                CooldownMessages.reply(event, a.build());
                return;
            }
            event.deferReply().queue();

            String url = event.getOption("song").getAsString();

            try{
                new URI(url);
            } catch (URISyntaxException use) {
                url = "ytsearch:" + String.join(" ", url) + " audio";
            }

            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
            PlayerManager.getInstance().loadAndPlay(event, url);
        }
    }
}
