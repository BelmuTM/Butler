package com.belmu.butler.commands.music;

import com.belmu.butler.lavaplayer.GuildMusicManager;
import com.belmu.butler.lavaplayer.PlayerManager;
import com.belmu.butler.utility.CooldownMessages;
import com.belmu.butler.utility.Duration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Objects;

public class QueueCommand extends ListenerAdapter {

    public String cmdName = "queue";
    public String cmdDescription = "Displays the tracks queue";

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String cmd = event.getName();

        if(cmd.equals(cmdName)) {
            Member member = event.getMember();

            final GuildMusicManager guildMusicManager = PlayerManager.getInstance().getMusicManager(Objects.requireNonNull(event.getGuild()), event.getChannel());
            final AudioPlayer audioPlayer = guildMusicManager.audioPlayer;

            if(audioPlayer.getPlayingTrack() == null) {
                event.deferReply(true).queue();
                CooldownMessages.reply(event, PlayerManager.getInstance().notPlaying.build());
                return;
            }
            event.deferReply().queue();

            AudioTrack playing = guildMusicManager.audioPlayer.getPlayingTrack();
            String formattedDuration = Duration.getFormattedDuration(playing.getDuration());

            assert member != null;
            final EmbedBuilder queue = new EmbedBuilder()
                    .setColor(event.getGuild().getSelfMember().getColor())
                    .setFooter("Requested by " + member.getEffectiveName(), member.getUser().getAvatarUrl())
                    .setTimestamp(Instant.now());

            String nowPlaying = "\uD83C\uDFB5 `" + playing.getInfo().title + "` [" + formattedDuration + "]";
            StringBuilder trackList = new StringBuilder();

            final AudioTrack[] audioTracks = guildMusicManager.trackScheduler.queue.toArray(new AudioTrack[0]);

            if(audioTracks.length > 0) {
                final int queueInfoSize = 5;
                final String separator = "\n\uD83D\uDD3B ";

                for (int i = 1; i <= queueInfoSize && i <= audioTracks.length; i++) {
                    AudioTrack track = audioTracks[i - 1];
                    trackList.append(separator).append(track.getInfo().title);
                    if (i == queueInfoSize) trackList.append(separator).append(". . .");
                }
            }
            queue.addField(nowPlaying, trackList.toString().trim(), true);

            event.getHook().sendMessageEmbeds(queue.build()).queue();
        }
    }
}
