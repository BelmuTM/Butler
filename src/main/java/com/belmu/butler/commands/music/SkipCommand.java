package com.belmu.butler.commands.music;

import com.belmu.butler.lavaplayer.GuildMusicManager;
import com.belmu.butler.lavaplayer.PlayerManager;
import com.belmu.butler.utility.CooldownMessages;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Objects;

public class SkipCommand extends ListenerAdapter {

    public String cmdName = "skip";
    public String cmdDescription = "Skips the ongoing track to play the next";

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmd = event.getName();

        if(cmd.equals(cmdName)) {
            Guild guild = event.getGuild();
            assert guild != null;

            if (!Objects.requireNonNull(guild.getSelfMember().getVoiceState()).inAudioChannel()) {
                event.deferReply(true).queue();
                CooldownMessages.reply(event, PlayerManager.getInstance().notConnected.build());
                return;
            }

            if (!Objects.requireNonNull(guild.getAudioManager().getConnectedChannel()).getMembers().contains(event.getMember())) {
                event.deferReply(true).queue();
                CooldownMessages.reply(event, PlayerManager.getInstance().sameChannel.build());
                return;
            }

            final GuildMusicManager guildMusicManager = PlayerManager.getInstance().getMusicManager(guild, event.getChannel());

            if (guildMusicManager.trackScheduler.queue.isEmpty()) {
                event.deferReply(true).queue();
                CooldownMessages.reply(event, PlayerManager.getInstance().noTrackNext.build());
                return;
            }

            AudioTrack playing = guildMusicManager.audioPlayer.getPlayingTrack();
            User user = event.getUser();

            final EmbedBuilder skip = new EmbedBuilder()
                    .setColor(guild.getSelfMember().getColor())
                    .setDescription("⏭️ **Skipped** » `" + playing.getInfo().title+ "`")
                    .setFooter("Requested by " + user.getName(), user.getAvatarUrl())
                    .setTimestamp(Instant.now());

            event.deferReply().queue();

            guildMusicManager.trackScheduler.nextTrack();

            event.getHook().sendMessageEmbeds(skip.build()).queue();
        }
    }
}
