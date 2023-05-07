package com.belmu.butler.lavaplayer;

import com.belmu.butler.Butler;
import com.belmu.butler.utility.CooldownMessages;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PlayerManager {

    private static PlayerManager INSTANCE;

    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.audioPlayerManager.setFrameBufferDuration(1000);
        this.audioPlayerManager.setItemLoaderThreadPoolSize(500);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public static PlayerManager getInstance() {
        if(INSTANCE == null) INSTANCE = new PlayerManager(); return INSTANCE;
    }

    public GuildMusicManager getMusicManager(Guild guild){
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, String url) {
        GuildMusicManager musicManager = getMusicManager(Objects.requireNonNull(event.getGuild()));

        audioPlayerManager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                User user = event.getUser();

                long duration = track.getDuration();

                long hours   = TimeUnit.MILLISECONDS.toHours(duration);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;

                String formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                final EmbedBuilder trackLoaded = new EmbedBuilder()
                        .setColor(Butler.gold)
                        .setDescription("\uD83D\uDCE5 **Added** `" + track.getInfo().title + "` to the queue")
                        .addField("Duration", formattedDuration, true)
                        .addField("Author", track.getInfo().author, true)
                        .addField("Source", "[Link](" + track.getInfo().uri + ")", true)
                        .setFooter("Requested by " + user.getName(), user.getAvatarUrl())
                        .setTimestamp(Instant.now());
                event.getHook().sendMessageEmbeds(trackLoaded.build()).queue();

                musicManager.trackScheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();
                if(tracks.isEmpty()) return;

                if(url.startsWith("ytsearch:")) {
                    trackLoaded(tracks.get(0));
                    return;
                }

                long totalDuration = 0;
                for(AudioTrack track : tracks) {
                    musicManager.trackScheduler.queue(track);
                    totalDuration += track.getDuration();
                }

                User user = event.getUser();

                long hours   = TimeUnit.MILLISECONDS.toHours(totalDuration);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(totalDuration) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(totalDuration) % 60;

                String formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                final EmbedBuilder playlistLoaded = new EmbedBuilder()
                        .setColor(Butler.gold)
                        .setDescription("\uD83D\uDCE5 **Added** `" + playlist.getName() + "` [" + playlist.getTracks().size() + " songs] to the queue")
                        .addField("Total Duration", formattedDuration, true)
                        .addField("Source", "[Link](" + url + ")", true)
                        .setFooter("Requested by " + user.getName(), user.getAvatarUrl())
                        .setTimestamp(Instant.now());
                event.getHook().sendMessageEmbeds(playlistLoaded.build()).queue();
            }

            @Override
            public void noMatches() {
                final EmbedBuilder noMatch = new EmbedBuilder()
                        .setColor(Butler.darkGray)
                        .setDescription(":gear: **Error:** No song matches found.");
                CooldownMessages.reply(event, noMatch.build());
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                final EmbedBuilder failed = new EmbedBuilder()
                        .setColor(Butler.darkGray)
                        .setDescription(":gear: **Error:** " + exception.getMessage());
                CooldownMessages.reply(event, failed.build());
            }
        });
    }

    public void silentLoadAndPlay(SlashCommandInteractionEvent event, String url) {
        GuildMusicManager musicManager = getMusicManager(Objects.requireNonNull(event.getGuild()));

        audioPlayerManager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.trackScheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();
                if(tracks.isEmpty()) return;

                if(url.startsWith("ytsearch:")) {
                    trackLoaded(tracks.get(0));
                }
            }

            @Override
            public void noMatches() {}

            @Override
            public void loadFailed(FriendlyException exception) {}
        });
    }

    public final EmbedBuilder notConnected = new EmbedBuilder()
            .setColor(Butler.darkGray)
            .setDescription(":gear: I am not connected to any channel.");

    public final EmbedBuilder sameChannel = new EmbedBuilder()
            .setColor(Butler.darkGray)
            .setDescription(":gear: Please join my channel first.");

    public final EmbedBuilder noTrackNext = new EmbedBuilder()
            .setColor(Butler.darkGray)
            .setDescription(":gear: There is no track to play next.");

    public final EmbedBuilder notPlaying = new EmbedBuilder()
            .setColor(Butler.darkGray)
            .setDescription(":gear: There is no track playing.");

    public final EmbedBuilder alreadyVoted = new EmbedBuilder()
            .setColor(Butler.darkGray)
            .setDescription(":gear: You've already voted to stop the music.");
}
